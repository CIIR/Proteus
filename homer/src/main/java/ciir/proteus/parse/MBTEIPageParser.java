package ciir.proteus.parse;

import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.parse.DocumentStreamParser;
import org.lemurproject.galago.core.types.DocumentSplit;
import org.lemurproject.galago.tupleflow.Parameters;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author jfoley.
 */
public class MBTEIPageParser extends DocumentStreamParser {
  public static final Logger log = Logger.getLogger(MBTEIPageParser.class.getName());
  public static final XMLInputFactory xmlFactory = XMLInputFactory.newInstance();
  static {
    xmlFactory.setProperty(XMLInputFactory.IS_COALESCING, true);
  }
  private final BufferedInputStream is;
  private final XMLStreamReader xml;
  private final DocumentSplit split;
  private LinkedList<Page> pages;
  private Map<String,String> metadata;

  public MBTEIPageParser(DocumentSplit split, Parameters p) throws IOException, XMLStreamException {
    super(split, p);


    this.split = split;
    this.is = getBufferedInputStream(split);
    this.xml = xmlFactory.createXMLStreamReader(is);
    pages = null;
    metadata = null;
  }

  @Override
  public void close() throws IOException {
    try {
      xml.close();
    } catch (XMLStreamException xml) {
      throw new IOException(xml);
    }
    is.close();
  }

  @Override
  public Document nextDocument() throws IOException {
    if(pages == null) {
      try {
        readPages();
      } catch (XMLStreamException e) {
        throw new IOException(e);
      }
    }

    if(pages == null || pages.isEmpty())
      return null;

    Page page = pages.pop();
    Document doc = new Document();
    doc.metadata = metadata;
    doc.text = page.text;
    doc.name = String.format("%s_%s", MBTEI.getArchiveIdentifier(split, metadata), page.number);
    return doc;
  }

  private void readPages() throws XMLStreamException {
    pages = new LinkedList<Page>();
    metadata = new HashMap<String,String>();

    if(!xml.hasNext())
      return;

    // local parser state
    StringBuilder buffer = new StringBuilder();
    boolean beforeOrDuringMetadata = true;
    String currentMetaTag = null;
    StringBuilder tagBuilder = null;
    boolean documentEmpty = true;

    while(xml.hasNext()) {
      int event = xml.next();

      if(beforeOrDuringMetadata) {
        if (event == XMLStreamConstants.START_ELEMENT) {
          String tag = xml.getLocalName();
          if("text".equals(tag)) {
            beforeOrDuringMetadata = false;
          } else {
            currentMetaTag = tag;
            tagBuilder = new StringBuilder();
          }
        } else if(event == XMLStreamConstants.END_ELEMENT) {
          if (currentMetaTag != null) {
            metadata.put(currentMetaTag, tagBuilder.toString().trim());
            currentMetaTag = null;
            tagBuilder = null;
          }
        } else if(event == XMLStreamConstants.CHARACTERS || event == XMLStreamConstants.CDATA) {
          if(currentMetaTag != null) {
            tagBuilder.append(xml.getText()).append(' ');
          }
        }
      } else {
        // copy the "form" attribute out of word tags
        if(event == XMLStreamConstants.START_ELEMENT) {
          String tag = xml.getLocalName();
          if("w".equals(tag)) {
            String formValue = MBTEI.scrub(xml.getAttributeValue(null, "form"));
            if (!formValue.isEmpty()) {
              buffer.append(formValue).append(' ');
              documentEmpty = false;
            }
          } else if("pb".equals(tag) && !documentEmpty) {
            // echo a document when we find the <pb /> tag
            String pageNumber = xml.getAttributeValue(null, "n");
            pages.add(new Page(pageNumber, buffer.toString()));
            buffer = new StringBuilder();
          }
        }
      }
    }

    // echo a document when we find the end of the stream
    if(!documentEmpty) {
      // TODO: turn this back on after it's not going to fire for every book...
      //log.warning("Missing final <pb /> tag for book: "+MBTEI.getArchiveIdentifier(split, metadata));
      pages.add(new Page("last", buffer.toString()));
    }
  }

  private static final class Page {
    public final String number;
    public final String text;

    public Page(String number, String text) {
      this.number = number;
      this.text = text;
    }
  }
}
