package ciir.proteus.parse;

//import ciir.proteus.parse.Pages.Word;
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
        if (xone <= (marginLeft + quarterInch) || yone >= (marginDown - quarterInch) || xtwo >= (marginRight - quarterInch) || ytwo <= (marginUp + quarterInch)) {
            //System.out.println("in margins");
            return true;
        } else {
            //System.out.println("out of margins");
            return false;
        }
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

    /*this method doesnt work quite right
     it will appropriately recognizes well formatted
     roman numerals, but will also recognize 
     incorrectly formatted numerals
     for example it interperts "iix" as 10.
     However, in the context it's being used I
     don't think this will cause problems
     */
    public static int romanToDecimal(String number) {
        assert(isRomanNum(number));
        int decimal = 0;
        int lastNumber = 0;
        String romanNumeral = number.toUpperCase();
        /* operation to be performed on upper cases even if user enters roman values in lower case chars */
        for (int x = romanNumeral.length() - 1; x >= 0; x--) {
            char convertToDecimal = romanNumeral.charAt(x);

            switch (convertToDecimal) {
                case 'M':
                    decimal = processDecimal(1000, lastNumber, decimal);
                    lastNumber = 1000;
                    break;

                case 'D':
                    decimal = processDecimal(500, lastNumber, decimal);
                    lastNumber = 500;
                    break;

                case 'C':
                    decimal = processDecimal(100, lastNumber, decimal);
                    lastNumber = 100;
                    break;

                case 'L':
                    decimal = processDecimal(50, lastNumber, decimal);
                    lastNumber = 50;
                    break;

                case 'X':
                    decimal = processDecimal(10, lastNumber, decimal);
                    lastNumber = 10;
                    break;

                case 'V':
                    decimal = processDecimal(5, lastNumber, decimal);
                    lastNumber = 5;
                    break;

                case 'I':
                    decimal = processDecimal(1, lastNumber, decimal);
                    lastNumber = 1;
                    break;
            }
        }
        return decimal;
    }

    public static int processDecimal(int decimal, int lastNumber, int lastDecimal) {
        if (lastNumber > decimal) {
            return lastDecimal - decimal;
        } else {
            return lastDecimal + decimal;
        }
    }

    public static boolean isRomanNum(String text) {
        text = text.toUpperCase();

        if (text.contains("A") || text.contains("B") || text.contains("E") || text.contains("F") || text.contains("G") || text.contains("H") || text.contains("J") || text.contains("K") || text.contains("N") || text.contains("O") || text.contains("P") || text.contains("Q") || text.contains("R") || text.contains("S") || text.contains("T") || text.contains("U") || text.contains("W") || text.contains("Y") || text.contains("Z") || text.contains(" ") || text.contains(".") || text.contains(",")) {
            return false;
        }
        return true;
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
                    //int pageCount = 0;
                    Pages page = new Pages();
                    page.acmPageNum = pageList.size();
                    //pageCount++;

                    //System.out.println("pagecount: " + pageCount);
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

    public static ArrayList<NumScheme> findPageNumbers(List<Pages> pagelist) {
        ArrayList<NumScheme> possPageSeqs = new ArrayList<NumScheme>();
        for (Pages p : pagelist) {
            System.out.println("searching pages " + possPageSeqs.size());

            for (Pages.Word w : p.wordsOnPage) {
                System.out.println("searching words");
                if (isArabicNum(w.text)) {
                    System.out.println("found arabic: " + w.text);
                    handleArabic(possPageSeqs, w);
                } else if (isRomanNum(w.text)) {
                    System.out.println("found roman: " + w.text);
                    handleRoman(possPageSeqs, w);

                }

            }
            for (NumScheme ns : possPageSeqs) {
                if (ns.foundElementOnCurrPage==false) {
                    System.out.println("adding blank");
                    ns.addBlank();
                }
            }

            for (NumScheme num : possPageSeqs) {
                System.out.println("setting to false");
                num.foundElementOnCurrPage = false;
            }

        }
        System.out.println("poss sequences: " + possPageSeqs.size());
        return possPageSeqs;
    }

    public static ArrayList<NumScheme> handleArabic(ArrayList<NumScheme> pps, Pages.Word w) {

        NumScheme temp = new NumScheme();
        System.out.println("calling findsequence");
        if (findSequence(pps, w) == null) {
            System.out.println("creating new an");
            temp.sequence.add(w);
            temp.foundElementOnCurrPage = true;
            pps.add(temp);
        }

        return pps;
    }

    public static NumScheme findSequence(ArrayList<NumScheme> pps, Pages.Word w) {
        if (pps.size() == 0) {
            System.out.println("returning null b/c of size");
            return null;
        }
        for (NumScheme ns : pps) {
            if (!ns.roman) {
                System.out.println("not roman");
                int numBlanks = ns.getNumOfBlanks();
                 System.out.println("word: " + w.text + "number of blanks: " + numBlanks);
                 int valueOfLast = Integer.valueOf(ns.sequence.get(ns.sequence.size() - (1 + numBlanks)).text);

                //int valueOfLast = Integer.valueOf(ns.sequence.get(ns.sequence.size() - 1).text);
                System.out.println("value of last: " + valueOfLast);
                int valueOfCurr = Integer.valueOf(w.text);
                System.out.println("value of curr: " + valueOfCurr);

                if (valueOfLast + (1+numBlanks) == valueOfCurr) {
                    ns.foundElementOnCurrPage = true;
                    ns.arabic = true;
                    System.out.println("adding to sequence");
                    ns.sequence.add(w);
                    return ns;
                }
            } else if (!ns.arabic) {
                int currNumeral = romanToDecimal(w.text);
                int numBlanks = ns.getNumOfBlanks();
                 System.out.println("word: " + w.text + "number of blanks: " + numBlanks);
                int valueOfLast = romanToDecimal(ns.sequence.get(ns.sequence.size() - (1 + numBlanks)).text);
               // int valueOfLast = romanToDecimal(ns.sequence.get(ns.sequence.size() - 1).text);
                if (valueOfLast + (1+numBlanks) == currNumeral) {
                    ns.foundElementOnCurrPage = true;
                    System.out.println("adding to sequence of rn");
                    ns.sequence.add(w);
                    ns.roman = true;
                    return ns;
                }
            }
        }
        System.out.println("returning null b/c no schemes fit");
        return null;
    }

    public static ArrayList<NumScheme> handleRoman(ArrayList<NumScheme> pps, Pages.Word w) {

        NumScheme temp = new NumScheme();
        System.out.println("calling findsequence");
        if (findSequence(pps, w) == null) {
            System.out.println("creating new rn");
            temp.foundElementOnCurrPage = true;
            temp.roman = true;
            temp.sequence.add(w);
            pps.add(temp);
        }

        return pps;

    }

    public static void main(String args[]) throws IOException, XMLStreamException {

        File readFile = new File(args[0]);
        //File writeFile = new File(args[1]);
        XMLTrimmer xr = new XMLTrimmer(readFile);

        XMLEvent event = null;

        List<Pages> work = xr.trim(event);
        String output = "";
        System.out.println("number of pages: " + work.size());
        for (Pages p : work) {
            //System.out.println("calling pages.tostring");
            System.out.println("number of words on page: " + p.wordsOnPage.size());
            output = p.toString();
            System.out.println(output);
        }

    }

    public static class NumScheme {

        int pagesLookedAt;
        int pageNumsFound;
        boolean foundElementOnCurrPage = false;
        boolean arabic;
        boolean roman;
        //double ratio = pagesLookedAt / pageNumsFound;
        List<Pages.Word> sequence = new ArrayList<Pages.Word>();

        public NumScheme() {
        }

        public Pages.Word getLast() {
            return sequence.get(sequence.size() - 1);
        }

        public void addBlank() {

            Pages.Word blank = new Pages.Word();
            blank.isBlank = true;
            blank.text = "";
            sequence.add(blank);

        }

        public String toString() {
            String result = "";
            for (Pages.Word w : this.sequence) {
                result = result + w.toString();
            }
            return result;
        }

        public int getNumOfBlanks() {
            int numOfBlanks = 0;
            for (Pages.Word word : this.sequence) {
                if(word.isBlank) numOfBlanks++;
            }
            return numOfBlanks;
        }

    }
}
