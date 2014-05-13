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
  private Map<String,String> metadata;

  public MBTEIPageParser(DocumentSplit split, Parameters p) throws IOException, XMLStreamException {
    super(split, p);


    this.split = split;
    this.is = getBufferedInputStream(split);
    this.xml = xmlFactory.createXMLStreamReader(is);
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
    try {
      if(metadata == null) {
        metadata = MBTEI.parseMetadata(xml);
      }
      return nextPage();
    } catch (XMLStreamException e) {
      throw new IOException(e);
    }
  }

  private Document nextPage() throws XMLStreamException {
    if(!xml.hasNext())
      return null;

    // local parser state
    StringBuilder buffer = new StringBuilder();
    String pageNumber = null;
    boolean empty = true;

    while(xml.hasNext()) {
      int event = xml.next();

      // copy the "form" attribute out of word tags
      if(event == XMLStreamConstants.START_ELEMENT) {
        String tag = xml.getLocalName();
        if("w".equals(tag)) {
          String formValue = MBTEI.scrub(xml.getAttributeValue(null, "form"));
          if (!formValue.isEmpty()) {
            buffer.append(formValue).append(' ');
            empty = false;
          }
        } else if("pb".equals(tag)) {
          // echo a document when we find the <pb /> tag
          pageNumber = xml.getAttributeValue(null, "n");
          if(!empty) break;
        }
      }
    }

    if(empty) return null;

    Document page = new Document();
    String archiveId = MBTEI.getArchiveIdentifier(split, metadata);
    page.text = buffer.toString();
    page.name = archiveId+"_"+pageNumber;
    page.metadata = metadata;
    page.metadata.put("pageNumber", pageNumber);
    return page;
  }
}
