package ciir.proteus.parse;

import ciir.proteus.parse.Pages.Word;
import javax.xml.stream.*;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 *
 * @author bzifkin
 */
/**
 * This class takes in a DjVU/XMl document and using the iterator API of StAX
 * creates another DjVU/XML document that contains only the token found in the
 * outer 10% of the original document
 */
public class XMLTrimmer {

    int marginUp = 0; //these are the margins which determines if a token is eligible or not
    int marginDown = 0;
    int marginLeft = 0;
    int marginRight = 0;
    int quarterInch = 0;
    int xOne = 0; //coordinates of the words
    int yOne = 0;
    int xTwo = 0;
    int yTwo = 0;

    static XMLInputFactory factory = XMLInputFactory.newInstance();  //initiate readers/writers/factories
    static XMLOutputFactory outputFactory = XMLOutputFactory.newFactory();
    XMLEventReader reader = null;
    XMLEventWriter writer = null;
    XMLEventFactory eventFactory = null;
//ask john what this does

    public XMLTrimmer(File ifp) throws XMLStreamException, FileNotFoundException {
        this(new FileInputStream(ifp));
    }

    public XMLTrimmer(InputStream str) throws XMLStreamException, FileNotFoundException {

        try {

            reader = factory.createXMLEventReader(str);
        } catch (XMLStreamException e) {
            e.printStackTrace();
            System.out.println("There was an XML Stream Exception, whatever that means");
        }

    }

    public void getPageHeightAndWidth(StartElement se) {
        int pHeight = -1;
        int pWidth = -1;
        Iterator<Attribute> attributes = se.getAttributes();
        while (attributes.hasNext()) {

            Attribute attribute = attributes.next();
            //grabs some attributes set the value to variables

            if (attribute.getName().toString().equals("height")) {
                pHeight = Integer.valueOf(attribute.getValue());
            }

            if (attribute.getName().toString().equals("width")) {
                pWidth = Integer.valueOf(attribute.getValue());
            }
        }
        //System.out.println("pHeigt: " + pHeight);
        //System.out.println("pWidth : " + pWidth);
        calculateMargins(pHeight, pWidth);

    }

    public void setQI(StartElement se) {
        int dpi = -1;
        Iterator<Attribute> attributes = se.getAttributes();
        while (attributes.hasNext()) {

            Attribute attribute = attributes.next();

            if (attribute.getValue().equals("DPI")) {

                dpi = Integer.valueOf(attributes.next().getValue());
            }
            //System.out.println("dpi: " + dpi);
            quarterInch = (int) (dpi * .25);

        }
        //System.out.println("quarter inch: " + quarterInch);
    }

    //calculate margins to be outer 10%
    public void calculateMargins(int ph, int pw) {
        marginUp = (int) (ph * .1);

        marginDown = ph - ((int) (ph * .1));

        marginLeft = (int) (pw * .1);

        marginRight = pw - ((int) (pw * .1));

    }
//this grabs the amount of pixels in an inch, allowing us to add a 
// quarter inch of wiggle room to the margins to account for
// noise, uneven scanning, etc

    public void getWordCoords(StartElement se) {
        Iterator<Attribute> coord = se.getAttributes();
        Attribute position = coord.next();

        StringTokenizer st = new StringTokenizer(position.getValue(), ",");

        xOne = Integer.valueOf(st.nextToken());
        yOne = Integer.valueOf(st.nextToken());
        xTwo = Integer.valueOf(st.nextToken());
        yTwo = Integer.valueOf(st.nextToken());

    }
//see if in margins

    public boolean inMargin(int xone, int yone, int xtwo, int ytwo) {
        if (xone <= (marginLeft + quarterInch) || yone >= (marginDown - quarterInch) || xtwo >= (marginRight - quarterInch) || ytwo <= (marginUp + quarterInch)) 
            return true;
         else 
            return false;
        
    }

    public List<Pages> trim(XMLEvent event) throws XMLStreamException {
        System.out.println("calling run");
        List<Pages> pageList = new ArrayList<Pages>();
        while (reader.hasNext()) {

            event = reader.nextEvent();

            if (event.isStartElement()) { //first it looks for start elements
                StartElement se = event.asStartElement();
                if ("OBJECT".equals(se.getName().getLocalPart())) {

                    getPageHeightAndWidth(se);

                } else if ("MAP".equals(se.getName().getLocalPart())) {
                    
                    Pages page = new Pages();
                    page.acmPageNum = pageList.size();
                    pageList.add(page);

                } else if ("PARAM".equals(se.getName().getLocalPart())) {
                    if (quarterInch >= 0) {
                        setQI(se);
                    }

                } else if ("WORD".equals(se.getName().getLocalPart())) {
                    getWordCoords(se);

                    if (inMargin(xOne, yOne, xTwo, yTwo)) { //check to see if in margins, if it is the end tag is written immediately, otherwise nothing is written
                        Pages.Word word = new Pages.Word();
                        word.xOne = xOne;
                        word.yOne = yOne;
                        word.xTwo = xTwo;
                        word.yTwo = yTwo;

                        word.text = reader.getElementText();
                        if (pageList.size() == 0) {

                            Pages page = new Pages();
                            page.acmPageNum = word.acmNum = 0;
                            page.wordsOnPage.add(word);
                            pageList.add(page);

                        } else if (pageList.size() > 0) {
                            Pages lastPage = pageList.get(pageList.size() - 1);
                            word.acmNum = lastPage.acmPageNum;
                            lastPage.wordsOnPage.add(word);

                        }

                    }

                }

            }
        }
        System.out.println("margin up : " + (marginUp + quarterInch));
        System.out.println("margin down : " + (marginDown - quarterInch));
        System.out.println("margin left : " + (quarterInch + marginLeft));
        System.out.println("margin right : " + (marginRight - quarterInch));
        return pageList;
    }

    public static boolean isArabicNum(String text) {
        String bs = text;

        try {
            Integer.parseInt(bs);

        } catch (NumberFormatException e) {

            return false;
        }

        return true;
    }
    /*this method is similar to the trim method
    lots of calls are made to toerh methods and 
    this one is just bones of the process
    */

    public static ArrayList<NumScheme> searchForSchemes(List<Pages> pagelist) {
        ArrayList<NumScheme> possPageSeqs = new ArrayList<NumScheme>(); // create a list of all the possible sequences
        //iterate through pages to find schemes
        for (Pages p : pagelist) {
          //  System.out.println("searching pages, poss # of seqs:  " + possPageSeqs.size());
            
            //setting the parking lot
            for (NumScheme ns : possPageSeqs) {
                if (ns.sequence.size() > 2) {
                 //   System.out.println("calling set parking lot");
                    ns.setParkingLot();
                }

            }
            for (Pages.Word w : p.wordsOnPage) {
               // System.out.println("searching words");
                if (isArabicNum(w.text)) {
                   // System.out.println("found arabic: " + w.text);
                    handleArabic(possPageSeqs, w);
                } else if (RomanNumeral.isRomanNum(w.text)) {
                    //System.out.println("found roman: " + w.text);
                    handleRoman(possPageSeqs, w);

                }

            }
            for (NumScheme ns : possPageSeqs) {
                //adding blanks if nothing was found and the scheme isnt in the parking lot
                if (ns.foundElementOnCurrPage == false && ns.inParkLot == false) {
                    //System.out.println("adding blank");

                    ns.addBlank();
                }
            }

            for (NumScheme num : possPageSeqs) {
                //System.out.println("setting to false");
                num.foundElementOnCurrPage = false;
            }

        }
        System.out.println("poss sequences: " + possPageSeqs.size());
        return possPageSeqs;
    }

    public static ArrayList<NumScheme> handleArabic(ArrayList<NumScheme> pps, Pages.Word w) {

        NumScheme temp = new NumScheme();
        //System.out.println("calling findArabsequence");
        if (findArabicSequence(pps, w) == null) {
          //  System.out.println("creating new an");
            temp.foundElementOnCurrPage = true;
            temp.sequence.add(w);
            pps.add(temp);
        }

        return pps;
    }

    public static ArrayList<NumScheme> handleRoman(ArrayList<NumScheme> pps, Pages.Word w) {

        NumScheme temp = new NumScheme();
       // System.out.println("calling findRomansequence");
        if (findRomanSequence(pps, w) == null) {
            
          //  System.out.println("creating new rn");
            temp.foundElementOnCurrPage = true;
            temp.sequence.add(w);
            pps.add(temp);
        }

        return pps;

    }

    public static NumScheme findArabicSequence(ArrayList<NumScheme> pps, Pages.Word w) {
      
        if (pps.size() == 0) {
          //  System.out.println("returning null b/c of size");
            return null;
        }
        for (NumScheme ns : pps) {
            
            if (isArabicNum(ns.sequence.get(0).text) && ns.inParkLot == false && ns.foundElementOnCurrPage ==false) {
                
                int numBlanks = ns.getNumOfBlanks();             
                int valueOfLast = Integer.valueOf(ns.sequence.get(ns.sequence.size() - (1 + numBlanks)).text);  
                int valueOfCurr = Integer.valueOf(w.text);
                
              // System.out.println("word: " + w.text + "number of blanks: " + numBlanks);
                if (valueOfLast + (1 + numBlanks) == valueOfCurr) {
                    
                    ns.foundElementOnCurrPage = true;
                    //System.out.println("adding to sequence of an");
                    ns.sequence.add(w);
                    return ns;
                }
            }
        }
        //System.out.println("returning null b/c no schemes fit");
        return null;
    }

    public static NumScheme findRomanSequence(ArrayList<NumScheme> pps, Pages.Word w) {
        
        if (pps.size() == 0) {
            //System.out.println("returning null b/c of size");
            return null;
        }
        for (NumScheme ns : pps) {
            if (RomanNumeral.isRomanNum(ns.sequence.get(0).text) && ns.inParkLot == false && ns.foundElementOnCurrPage ==false) {
                
                int currNumeral = RomanNumeral.romanToDecimal(w.text);
                int numBlanks = ns.getNumOfBlanks();              
                int valueOfLast = RomanNumeral.romanToDecimal(ns.sequence.get(ns.sequence.size() - (1 + numBlanks)).text);
                
 System.out.println("word: " + w.text + " number of blanks: " + numBlanks);
                if (valueOfLast + (1 + numBlanks) == currNumeral) {
                    
                    ns.foundElementOnCurrPage = true;
                   //System.out.println("adding to sequence of rn");
                    ns.sequence.add(w);
                    return ns;
                }
            }
        }
        //System.out.println("returning null b/c no schemes fit");
        return null;
    }

    public static void main(String args[]) throws IOException, XMLStreamException {

        File readFile = new File(args[0]);
        //File writeFile = new File(args[1]);
        XMLTrimmer xr = new XMLTrimmer(readFile);

        XMLEvent event = null;

        List<Pages> work = xr.trim(event);
        String output = "";
        
        System.out.println("number of pages: " + work.size());
       List<NumScheme> numbering  = xr.searchForSchemes(work);
       for (NumScheme ns : numbering){
          // if(ns.sequence.size()>3)
        output = output+ ns.toString();
       }
          System.out.println(output);

    }

  
}
