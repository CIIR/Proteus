
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
public class XMLReader {
    int marginUp = 0;
        int marginDown = 0;
        int marginLeft = 0;
        int marginRight = 0;
        int pageWidth = 0;
        int pageHeight = 0;
        int dpi = 0;
        int quarterInch = 0;
        boolean goodWord = false;


    public static void main(String args[]) throws IOException, XMLStreamException {      
        int xOne = 0;
        int yOne = 0;
        int xTwo = 0;
        int yTwo = 0;
        
        XMLReader xr = new XMLReader();

        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLOutputFactory outputFactory = XMLOutputFactory.newFactory();

        XMLEventWriter writer = null;
        try {
            writer = outputFactory
                    .createXMLEventWriter(new FileOutputStream(args[1]), "UTF-8");
           

        } catch (XMLStreamException ex) {
            Logger.getLogger(XMLReader.class.getName()).log(Level.SEVERE, null, ex);
        }

        XMLEventFactory eventFactory = XMLEventFactory.newInstance();
        StartDocument startDocument = eventFactory.createStartDocument();

        writer.add(startDocument);
        XMLEventReader reader = null; //intializing the eventreader

        try {
            reader = factory.createXMLEventReader(new FileInputStream(args[0]));
            

            XMLEvent event = null;

            while (reader.hasNext()) {

                event = reader.nextEvent();

                if (event.isStartElement()) {
                    StartElement se = event.asStartElement();
                    if ("OBJECT".equals(se.getName().getLocalPart())) {

                        Iterator<Attribute> attributes = se.getAttributes();
                        while (attributes.hasNext()) {

                            Attribute attribute = attributes.next();

                            if (attribute.getName().toString().equals("height")) {
                                xr.pageHeight = Integer.valueOf(attribute.getValue());
                            }

                            if (attribute.getName().toString().equals("width")) {
                                xr.pageWidth = Integer.valueOf(attribute.getValue());
                            }
                        }
                        // System.out.println("<OBJECT>");
                        xr.calculateMargins(xr.pageHeight, xr.pageWidth); //how to make sure this isnt called everytime

                    } else if ("MAP".equals(se.getName().getLocalPart())) {
                        writer.add(se);
                        //System.out.println("<MAP>");

                    } else if ("BODY".equals(se.getName().getLocalPart())) {
                        writer.add(se);
                        //System.out.println("<MAP>");

                    } else if ("PARAM".equals(se.getName().getLocalPart())) {
                        //System.out.println("<PARAM>");
                        Iterator<Attribute> attributes = se.getAttributes();
                        while (attributes.hasNext()) {

                            Attribute attribute = attributes.next();

                            if (attribute.getValue().toString().equals("DPI") || attribute.getValue().toString().equals("PPI")) {
                                attributes.next();
                                if (attribute.getValue().toString().equals("value")) {
                                    xr.dpi = Integer.valueOf(attribute.getValue());
                                }
                                xr.quarterInch = (int) (xr.dpi * .25);

                            }

                        }
                    } else if ("PARAGRAPH".equals(se.getName().getLocalPart())) {
                        //System.out.println("<PARAGRAPH>");
                        writer.add(se);

                    } else if ("LINE".equals(se.getName().getLocalPart())) {

                        //System.out.println("<LINE>");
                        writer.add(se);

                    } else if ("WORD".equals(se.getName().getLocalPart())) {

                        Iterator<Attribute> coord = se.getAttributes();
                        Attribute position = coord.next();

                        if (position.getName().toString().equals("coords")) {//add else
                            StringTokenizer st = new StringTokenizer(position.getValue(), ",");

                            xOne = Integer.valueOf(st.nextToken());
                            yOne = Integer.valueOf(st.nextToken());
                            xTwo = Integer.valueOf(st.nextToken());
                            yTwo = Integer.valueOf(st.nextToken());

                        }

                        if (xr.inMargin2(xOne, yOne, xTwo, yTwo)) {
                            writer.add(se);
                            EndElement wordEnd = eventFactory.createEndElement("", "", "WORD");
                            Characters characters = eventFactory.createCharacters(reader.getElementText());
                            writer.add(characters);
                            writer.add(wordEnd);
                        }

                    }

                } else if (event.isEndElement()) {
                    
                    EndElement ee = event.asEndElement();
                    if ("LINE".equals(ee.getName().getLocalPart())) {
                        writer.add(ee);
                        
                    } else if ("PARAGRAPH".equals(ee.getName().getLocalPart())) {             
                        writer.add(ee);
                        
                    } else if ("MAP".equals(ee.getName().getLocalPart())) {
                        writer.add(ee);
                        
                    } else if ("BODY".equals(ee.getName().getLocalPart())) {
                        writer.add(ee);
                    }
                } else if (event.isCharacters()) {

                }
            }

        } catch (FileNotFoundException e) {
            System.out.println("Could not find the file. Try again.");
        } catch (XMLStreamException e) {
            e.printStackTrace();
            System.out.println("There was an XML Stream Exception, whatever that means");
        } finally {
            writer.flush();
            writer.close();
        }

    }

    public void calculateMargins(int ph, int pw) {
        marginUp = (int) (ph * .1);
        marginDown = pageHeight - ((int) (ph * .1));
        marginLeft = (int) (pw * .1);
        marginRight = pageWidth - ((int) (pw * .1));
    }

    public boolean inMargin2(int xone, int yone, int xtwo, int ytwo) {
        if (xone <= (marginLeft + quarterInch) || yone >= (marginDown - quarterInch) || xtwo >= (marginRight - quarterInch) || ytwo <= (marginUp + quarterInch)) {
            goodWord = true;
            return true;
        } else {
            goodWord = false;
            return false;
        }
    }

}
