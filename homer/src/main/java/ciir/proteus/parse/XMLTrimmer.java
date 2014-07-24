//package ciir.proteus.parse;

import java.io.File;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.StringTokenizer;
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

    File readFile;
    File writeFile;
    int marginUp = 0; //these are the margins which determines if a token is eligible or not
    int marginDown = 0;
    int marginLeft = 0;
    int marginRight = 0;
    int pageWidth = 0;
    int pageHeight = 0;
    int dpi = 0;
    int quarterInch = 0;
    int xOne = 0; //coordinates of the words
    int yOne = 0;
    int xTwo = 0;
    int yTwo = 0;
    XMLEventReader reader = null;
    XMLEventWriter writer = null;
    XMLEventFactory eventFactory = null;

    public XMLTrimmer(XMLInputFactory ipf, XMLOutputFactory opf, File rfile, File wfile) throws FileNotFoundException, XMLStreamException {
        readFile = rfile;
        writeFile = wfile;
        try {
            reader = ipf.createXMLEventReader(new FileInputStream(readFile));
        } catch (FileNotFoundException e) {
            System.out.println("Could not find the file. Try again.");
        } catch (XMLStreamException e) {
            e.printStackTrace();
            System.out.println("There was an XML Stream Exception, whatever that means");
        }

        try {
            writer = opf
                    .createXMLEventWriter(new FileOutputStream(writeFile), "UTF-8");

        } catch (XMLStreamException ex) {
            Logger.getLogger(XMLTrimmer.class.getName()).log(Level.SEVERE, null, ex);
        }

        eventFactory = XMLEventFactory.newInstance();
        StartDocument startDocument = eventFactory.createStartDocument();

        writer.add(startDocument);
    }

    public void setPageHeightAndWidth(StartElement se) {
        Iterator<Attribute> attributes = se.getAttributes();
        while (attributes.hasNext()) {

            Attribute attribute = attributes.next();
            //grabs some attributes set the value to variables

            if (attribute.getName().toString().equals("height")) {
                pageHeight = Integer.valueOf(attribute.getValue());
            }

            if (attribute.getName().toString().equals("width")) {
                pageWidth = Integer.valueOf(attribute.getValue());
            }
        }
    }

    //calculate margins to be outer 10%
    public void calculateMargins(int ph, int pw) {
        marginUp = (int) (ph * .1);
        marginDown = pageHeight - ((int) (ph * .1));
        marginLeft = (int) (pw * .1);
        marginRight = pageWidth - ((int) (pw * .1));

    }
//this grabs the amount of pixels in an inch, allowing us to add a 
// quarter inch of wiggle room to the margins to account for
// noise, uneven scanning, etc
    public void setDPI(StartElement se) {
        Iterator<Attribute> attributes = se.getAttributes();
        while (attributes.hasNext()) {

            Attribute attribute = attributes.next();

            if (attribute.getValue().toString().equals("DPI")) {

                dpi = Integer.valueOf(attributes.next().getValue());
            }
            quarterInch = (int) (dpi * .25);

        }

    }

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
            return true;
        } else {
            return false;
        }
    }

    public static void main(String args[]) throws IOException, XMLStreamException {
        XMLInputFactory factory = XMLInputFactory.newInstance();  //initiate readers/writers/factories
        XMLOutputFactory outputFactory = XMLOutputFactory.newFactory();
        File rfile = new File(args[0]);
        File wfile = new File(args[1]);
        XMLTrimmer xr = new XMLTrimmer(factory, outputFactory, rfile, wfile);;

        XMLEvent event = null;

        while (xr.reader.hasNext()) {

            event = xr.reader.nextEvent();

            if (event.isStartElement()) { //first it looks for start elements
                StartElement se = event.asStartElement();
                if ("OBJECT".equals(se.getName().getLocalPart())) {

                    xr.setPageHeightAndWidth(se);

                    xr.calculateMargins(xr.pageHeight, xr.pageWidth); //how to make sure this isnt called everytime

                } else if ("MAP".equals(se.getName().getLocalPart())) {
                    xr.writer.add(se);

                } else if ("BODY".equals(se.getName().getLocalPart())) {
                    xr.writer.add(se);

                } else if ("PARAM".equals(se.getName().getLocalPart())) {

                    xr.setDPI(se);

                } else if ("WORD".equals(se.getName().getLocalPart())) {
                    xr.getWordCoords(se);

                    if (xr.inMargin(xr.xOne, xr.yOne, xr.xTwo, xr.yTwo)) { //check to see if in margins, if it is the end tag is written immediately, otherwise nothing is written
                        xr.writer.add(se);
                        EndElement wordEnd = xr.eventFactory.createEndElement("", "", "WORD");
                        Characters characters = xr.eventFactory.createCharacters(xr.reader.getElementText());
                        xr.writer.add(characters);
                        xr.writer.add(wordEnd);
                    }

                }

            } else if (event.isEndElement()) {

                EndElement ee = event.asEndElement();
                if ("MAP".equals(ee.getName().getLocalPart())) {
                    xr.writer.add(ee);

                } else if ("BODY".equals(ee.getName().getLocalPart())) {
                    xr.writer.add(ee);
                }
            }
        }
        xr.writer.flush();
        xr.writer.close();
        //xr.showMargins();
    }

}
