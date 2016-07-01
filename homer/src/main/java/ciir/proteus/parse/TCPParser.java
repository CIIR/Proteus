package ciir.proteus.parse;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import org.apache.commons.lang3.StringEscapeUtils;
import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.parse.DocumentStreamParser;
import org.lemurproject.galago.core.types.DocumentSplit;
import org.lemurproject.galago.utility.Parameters;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author dasmith
 */
public abstract class TCPParser extends DocumentStreamParser {

  public static final Logger log = Logger.getLogger(TCPParser.class.getName());
  public static final XMLInputFactory xmlFactory = XMLInputFactory.newInstance();
  private final AbstractSequenceClassifier nerClassifier;

  static {
    xmlFactory.setProperty(XMLInputFactory.IS_COALESCING, true);
  }

  protected XMLStreamReader xml;
  protected final DocumentSplit split;
  protected Map<String, String> metadata;
  private int pageIndex = -1;

  abstract public Document nextDocument() throws IOException;

  public TCPParser(DocumentSplit split, Parameters p) throws Exception {
    super(split, p);

    this.split = split;
    metadata = null;
    this.xml = null;
    try {
      this.xml = xmlFactory.createXMLStreamReader(getBufferedInputStream(split));
    } catch (XMLStreamException | IOException e) {
      log.log(Level.WARNING, "Failed to create parser for split=" + split.fileName + " ", e);
    }
    String nullString = null;
    String serializedClassifier = p.get("ner-model", nullString);
    if (serializedClassifier != null && NamedEntityRecognizer.getClassifier() == null) {
      try {
        NamedEntityRecognizer.initClassifier(serializedClassifier);
      } catch (Exception e) {
        log.log(Level.WARNING, "Failed to load NER model: " + serializedClassifier + " ", e);
        throw new RuntimeException("Error loading NER model: " + serializedClassifier + ": " + e.toString());
      }
    }

    nerClassifier = NamedEntityRecognizer.getClassifier();

  }

  public static String getArchiveIdentifier(DocumentSplit split, Map<String, String> metadata) {
    // MCZ see if we can get the internet archive ID from the metadata
    String archiveID = metadata.get("identifier");

    if (archiveID != null)
      return archiveID;

    // if null - we'll try to figure it out from the file name.

    File f = new File(split.fileName);
    String basename = f.getName();

    String archiveId;
    if (basename.endsWith(".xml.gz"))
      archiveId = basename.split(".xml.gz")[0];
    else if (basename.endsWith(".xml"))
      archiveId = basename.split(".xml")[0];
    else {
      System.err.println("File extension was expected to be .xml.gz or .xml");
      archiveId = basename;
    }
    // put it in the metadata if we had to guess it.
    metadata.put("identifier", archiveId);
    return archiveId;
  }

  public static String scrub(String dirty) {
    if (dirty == null) {
      return "";
    }
    String cleaned = dirty.replaceAll("&apos;", "'");
    cleaned = cleaned.replaceAll("&quot;", "\"");
    cleaned = cleaned.replaceAll("&amp;", "&");
    cleaned = cleaned.replaceAll("[ ]+", " ");
    cleaned = cleaned.replaceAll("(-LRB-|-RRB-)", "");
    return cleaned.trim();
  }

  public static Map<String, String> parseMetadata(XMLStreamReader xml) throws XMLStreamException {

    Map<String, String> metadata = new HashMap<>();

    if (!xml.hasNext())
      return null;

    String currentMetaTag = null;
    StringBuilder tagBuilder = null;

    while (xml.hasNext()) {
      int event = xml.next();

      if (event == XMLStreamConstants.START_ELEMENT) {
        String tag = xml.getLocalName();

        // leave upon hitting a <text> tag; book specific
        if ("text".equals(tag)) {
          return metadata;
        }

        if (currentMetaTag == null) {
          // start reading characters
          currentMetaTag = tag;
          tagBuilder = new StringBuilder();
        }
        continue;
      }

      // do nothing until open tag hits
      if (currentMetaTag == null) continue;

      // collect any text or data
      if (event == XMLStreamConstants.CHARACTERS || event == XMLStreamConstants.CDATA) {
        tagBuilder.append(xml.getText()).append(' ');
        continue;
      }

      // add the built text when we hit a close
      if (event == XMLStreamConstants.END_ELEMENT) {
        if (metadata.containsKey(currentMetaTag)) {
          // separate values with a semicolon
          metadata.put(currentMetaTag, metadata.get(currentMetaTag) + " ; " + tagBuilder.toString().trim());
        } else {
           metadata.put(currentMetaTag, tagBuilder.toString().trim());
        }
        currentMetaTag = null;
        tagBuilder = null;
      }
    }
    if ( metadata.get("author") != null ) {
	metadata.put("creator", metadata.get("author"));
    }

    return metadata;
  }

  protected int getPageIndex() {
    // NOTE - we subtract 1 from th page number. the <pb> tags START
    // a page, but with the logic in nextPageText(), we hit the <pb> that starts the
    // NEXT page so we're one too many.
    return (pageIndex - 1);
  }

  public void close() throws IOException {
    if (xml == null) {
      return;
    }
    try {
      xml.close();
    } catch (XMLStreamException xml) {
      throw new IOException(xml);
    }
  }

  protected String nextPageText() throws XMLStreamException {
    if (!xml.hasNext()) {
      return null;
    }

    StringBuilder bodyBuffer = new StringBuilder();
    StringBuilder headerBuffer = new StringBuilder();
    StringBuilder footerBuffer = new StringBuilder();
    StringBuilder currentBuffer = bodyBuffer;

    boolean writing = true;
    boolean empty = true;
    // In the transition from DJVU to TOKTEI, some parts of the page
    // are given an <fw> tag. Sometimes these end up in the wrong place
    // in the text so a page's header may show up after a few lines. Same
    // issue with footers.
    String pagePart = "body";

    while (xml.hasNext()) {
      int event = xml.next();

      if (event == XMLStreamConstants.END_ELEMENT) {
        String tag = xml.getLocalName();

        if ("fw".equals(tag)) {
          currentBuffer = bodyBuffer;
        } else if ("lb".equals(tag)) {
          currentBuffer.append("<br>");
        } else if ("l".equals(tag)) {
          currentBuffer.append("<br>");
	}

        // since the books do NOT end with <pb> we need to "fake" that
        // so the last page number works
        if ("text".equals(tag)) {
          pageIndex++;
          break; // we're done
        }
      }
      if (event == XMLStreamConstants.CHARACTERS && writing) {
        currentBuffer.append(StringEscapeUtils.escapeHtml4(xml.getText()));
        empty = false;
      }
      // copy the "form" attribute out of word tags
      if (event == XMLStreamConstants.START_ELEMENT) {
        String tag = xml.getLocalName();

        if ("fw".equals(tag)) {
          pagePart = xml.getAttributeValue(null, "place");
	  if (pagePart == null) {
            currentBuffer = bodyBuffer;	      
	  } else if (pagePart.equalsIgnoreCase("top")) {
            currentBuffer = headerBuffer;
          } else if (pagePart.equalsIgnoreCase("bottom")) {
            currentBuffer = footerBuffer;
          } else {
            currentBuffer = bodyBuffer; // safety
          }
        }

	if ("pb".equals(tag)) {

          pageIndex++;
          if (!empty) {
            break;
          }
        }
      }
    }

    if (empty) {
      return null;
    }

    // put together the parts of the page
    String tmpPageText = headerBuffer.toString() + bodyBuffer.toString() + footerBuffer.toString();
    String pageText = doNER(tmpPageText) + "<br>";    // add a <br> to visually indicate the end of a page

    return pageText;
  }

  protected String doNER(String text) {

    if (text.length() == 0) {
      return "";
    }

    if (nerClassifier == null) {
      return text;
    } else {
      // if there is an error, just use the regular text
      try {
        return nerClassifier.classifyWithInlineXML(text);
      } catch (Exception e) {
        log.log(Level.WARNING, "Error running NER on: " + text, e);
        return text;
      }
    }
  }
}
