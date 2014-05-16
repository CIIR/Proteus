package ciir.proteus.parse;

import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.parse.DocumentStreamParser;
import org.lemurproject.galago.core.types.DocumentSplit;
import org.lemurproject.galago.tupleflow.Parameters;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
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
  private XMLStreamReader xml;
  private final DocumentSplit split;
  private Map<String,String> metadata;
  public int pageIndex = 0;

  public MBTEIPageParser(DocumentSplit split, Parameters p) throws IOException {
    super(split, p);

    this.split = split;
    metadata = null;
    this.xml = null;
    try {
      this.xml = xmlFactory.createXMLStreamReader(getBufferedInputStream(split));
    } catch (XMLStreamException e) {
      log.log(Level.WARNING, "Failed to create page parser for split="+split.fileName+" ",e);
    } catch (IOException e) {
      log.log(Level.WARNING, "Failed to create page parser for split="+split.fileName+" ",e);
    }
  }

  @Override
  public void close() throws IOException {
    if(xml == null) return;
    try {
      xml.close();
    } catch (XMLStreamException xml) {
      throw new IOException(xml);
    }
  }

  @Override
  public Document nextDocument() {
    if(xml == null) return null;
    try {
      if (metadata == null) {
        metadata = MBTEI.parseMetadata(xml);
      }
      return nextPage();
    } catch (XMLStreamException e) {
      log.log(Level.WARNING, "XML Exception", e);
      return null;
    }
  }

  private Document nextPage() throws XMLStreamException {
    if (!xml.hasNext()) {
      return null;
    }

    // local parser state
    StringBuilder buffer = new StringBuilder();
    boolean empty = true;

    while (xml.hasNext()) {
      int event = xml.next();

      // copy the "form" attribute out of word tags
      if (event == XMLStreamConstants.START_ELEMENT) {
        String tag = xml.getLocalName();
        if ("w".equals(tag)) {
          String formValue = MBTEI.scrub(xml.getAttributeValue(null, "form"));
          if (!formValue.isEmpty()) {
            buffer.append(formValue).append(' ');
            empty = false;
          }
        } else if ("pb".equals(tag)) {
          // echo a document when we find the <pb /> tag
          if(!empty) break;
        }
      }
    }

    if(empty) return null;

    final String pageNumber = Integer.toString(pageIndex++);
    Document page = new Document();
    String archiveId = MBTEI.getArchiveIdentifier(split, metadata);
    page.text = buffer.toString();
    page.name = archiveId + "_" + pageNumber;
    page.metadata = metadata;
    page.metadata.put("pageNumber", pageNumber);
    return page;
  }
}
