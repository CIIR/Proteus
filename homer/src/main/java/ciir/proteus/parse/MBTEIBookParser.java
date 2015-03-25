package ciir.proteus.parse;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.parse.DocumentStreamParser;
import org.lemurproject.galago.core.types.DocumentSplit;
import org.lemurproject.galago.utility.Parameters;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author jfoley, michaelz
 */
public class MBTEIBookParser extends DocumentStreamParser {
  public static final XMLInputFactory xmlFactory = XMLInputFactory.newInstance();
  public static final Logger log = Logger.getLogger(MBTEIBookParser.class.getName());
  static {
    xmlFactory.setProperty(XMLInputFactory.IS_COALESCING, true);
  }

  private final BufferedInputStream is;
  private final XMLStreamReader xml;
  private final DocumentSplit split;
  private final AbstractSequenceClassifier nerClassifier;

  public MBTEIBookParser(DocumentSplit split, Parameters p) throws Exception {
    super(split, p);

    this.split = split;
    this.is = getBufferedInputStream(split);
    this.xml = xmlFactory.createXMLStreamReader(is);

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

  @Override
  public void close() throws IOException {
    try {
      xml.close();
    } catch (XMLStreamException xml) {
      throw new IOException(xml);
    }
    is.close();
  }

  public void finishMetadata(StringBuilder buffer, Map<String, String> metadata) {
    // MCZ 31-JAN-2014 - adding Internet Archive identifier
    String archiveID = MBTEI.getArchiveIdentifier(split, metadata);
    // Note we do NOT want the archive ID to be parsed, we've seen some
    // that contain ":", ".", and lots of other characters that could be
    // interpreted as "word breaks"
    buffer.append("<archiveid tokenizetagcontent=\"false\">");
    buffer.append(archiveID);
    buffer.append("</archiveid>");
  }

  @Override
  public Document nextDocument() throws IOException {
    try {
      if (!xml.hasNext())
        return null;

      // local parser state
      StringBuilder buffer = new StringBuilder();
      StringBuilder pageBuffer = new StringBuilder();
      Map<String, String> metadata = MBTEI.parseMetadata(xml);
      boolean documentEmpty = true;

      while (xml.hasNext()) {
        int event = xml.next();

        // copy the "form" attribute out of word tags
        if (event == XMLStreamConstants.START_ELEMENT) {
          String tag = xml.getLocalName();
          if ("w".equals(tag)) {
            String formValue = MBTEI.scrub(xml.getAttributeValue(null, "form"));
            if (!formValue.isEmpty()) {
              pageBuffer.append(formValue).append(' ');
              documentEmpty = false;
            }
          }
          // echo a document when we find the </tei> tag
        } else if (event == XMLStreamConstants.END_ELEMENT) {
          String tag = xml.getLocalName();
          if ("pb".equals(tag)) {
            // NER the page and add it to the buffer (if we're doing NER)
            buffer.append(doNER(pageBuffer.toString()));
            pageBuffer.setLength(0);
          }
          if ("tei".equalsIgnoreCase(tag) && !documentEmpty) {

            // MBTEI books have the <pb> tag at the START of the page so we
            // need to process the last page
            buffer.append(doNER(pageBuffer.toString()));

            return MBTEI.makeDocument(split, metadata, buffer.toString());
          }
        }
      }

      // echo a document when we find the end of the stream
      if (!documentEmpty) {
        buffer.append(doNER(pageBuffer.toString()));
        return MBTEI.makeDocument(split, metadata, buffer.toString());
      }

    } catch (XMLStreamException xml) {
      throw new IOException(xml);
    }
    return null;
  }

  private String doNER(String text){
    if (text.length() == 0)
      return "";

    if (nerClassifier == null) {
      return text;
    } else {
      // if there is an error, just use the regular text
      try {
        return nerClassifier.classifyWithInlineXML(text);
      } catch (Exception e){
        log.log(Level.WARNING, "Error running NER on: " + text, e);
        return text;
      }
    }

  }

}
