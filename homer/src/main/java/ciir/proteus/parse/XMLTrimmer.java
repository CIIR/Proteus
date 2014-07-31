//package ciir.proteus.parse;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

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

    boolean knowPageHeightWidth = false;

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
        System.out.println("pHeigt: " + pHeight);
        System.out.println("pWidth : " + pWidth);
        calculateMargins(pHeight, pWidth);

    }

    public void setQI(StartElement se) {
        int dpi = -1;
        Iterator<Attribute> attributes = se.getAttributes();
        while (attributes.hasNext()) {

            Attribute attribute = attributes.next();

            if (attribute.getValue().toString().equals("DPI")) {

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

    public List<Pages> run(XMLEvent event) throws XMLStreamException {
        System.out.println("calling run");
        List<Pages> pageList = new ArrayList<Pages>();
        while (reader.hasNext()) {

            event = reader.nextEvent();

            if (event.isStartElement()) { //first it looks for start elements
                StartElement se = event.asStartElement();
                if ("OBJECT".equals(se.getName().getLocalPart())) {
                    if (!knowPageHeightWidth) {
                        getPageHeightAndWidth(se);
                        knowPageHeightWidth = true;
                    }

                } else if ("MAP".equals(se.getName().getLocalPart())) {
                    int pageCount = 0;
                    Pages page = new Pages();
                    page.acmPageNum = pageCount;
                    pageList.add(page);
                    pageCount++;

                } else if ("PARAM".equals(se.getName().getLocalPart())) {
                    if (quarterInch >= 0) {
                        setQI(se);
                    }

                } else if ("WORD".equals(se.getName().getLocalPart())) {
                    getWordCoords(se);

                    if (inMargin(xOne, yOne, xTwo, yTwo)) { //check to see if in margins, if it is the end tag is written immediately, otherwise nothing is written
                        Word word = new Word();
                        word.xOne = xOne;
                        word.yOne = yOne;
                        word.xTwo = xTwo;
                        word.yTwo = yTwo;
                        word.pageNum = pageList.size();
                        word.text = reader.getElementText();
                        if (pageList.size() == 0) {

                            Pages page = new Pages();
                            page.acmPageNum = 0;
                            page.wordsOnPage.add(word);
                            pageList.add(page);

                        } else if (pageList.size()>0) {
                            Pages lastPage = pageList.get(pageList.size() - 1);
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

    public static void main(String args[]) throws IOException, XMLStreamException {

        File readFile = new File(args[0]);
        //File writeFile = new File(args[1]);
        XMLTrimmer xr = new XMLTrimmer(readFile);

        XMLEvent event = null;

        List<Pages> work = xr.run(event);
        String hope = "";
        System.out.println("number of pages: " + work.size());
        for (Pages p : work) {
            System.out.println("calling pages.tostring");
            System.out.println("number of words on page: " + p.wordsOnPage.size());
            hope = p.toString();
            System.out.println(hope);
        }

    }

    public static class Pages {

        int pageNum = -1;
        int acmPageNum = -1;
        List<Word> wordsOnPage = new ArrayList<Word>();

        public Pages() {
        }

        public void addWords(Word w) {
            wordsOnPage.add(w);
        }

        public List<Word> getWords() {
            return wordsOnPage;
        }

        public String toString() {
            String stuff = "";

            stuff = "ACM Page Num: " + acmPageNum + "\n";
            for (Word w : wordsOnPage) {

                stuff = stuff + w.toString();
            }
            return stuff;
        }
    }

    public static class Word {

        public int pageNum;
        String text;
        boolean memOfNumScheme;
        int xOne; //coordinates of the words
        int yOne;
        int xTwo;
        int yTwo;

        public Word() {
        }

        public String toString() {
            String stuff = "";

            stuff = "pagenum = " + pageNum + " " + text + "(" + xOne + "," + yOne + ") (" + xTwo + "," + yTwo + ")\n";
            return stuff;
        }
    }
}
