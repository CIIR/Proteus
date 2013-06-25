package ciir.proteus.parse;

import java.util.regex.Pattern;
import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.types.DocumentSplit;
import org.lemurproject.galago.tupleflow.Parameters;

/**
 * STATES:
 *
 * S0 = starting state. Reads in header information. Stay in this state until
 * you see the "text" open tag, then head to S1.
 *
 * S1 = "w" tags trigger a read from the "form" attribute, which is the
 * outputted string. "pb" open tag triggers a document emission unless there's
 * no content. "name" opening and closing tags are echoed to the output string.
 * "text" and "TEI" closing tags are also echoed to the output.
 *
 * S1 is a terminal state.
 *
 */
class MBTEIPageParser extends MBTEIBookParser {

  Pattern pageBreakTag = Pattern.compile("pb");
  String pageNumber;

  public MBTEIPageParser(DocumentSplit split, Parameters p) {
    super(split, p);
    // set up to parse the header
  }

  public void moveToS1(int ignored) {
    header = buffer.toString();
    buffer = new StringBuilder();
    contentLength = 0;

    // Move on to the new rules
    // First remove old matchers
    clearStartElementActions();
    clearEndElementActions();
    unsetCharactersAction();

    // Now set up our normal processing matchers
    addStartElementAction(wordTag, "echoFormAttribute");
    addStartElementAction(nameTag, "transformNameTag");
    addEndElementAction(nameTag, "transformNameTag");
    addEndElementAction(textTag, "echo");
    addStartElementAction(pageBreakTag, "emitSingleDocument");
  }

  // Since we are emitting documents mid-stream, we need to
  // fake some of the window dressing around the content:
  //
  // <text>
  // ...content here...
  // </text></TEI>
  public void emitSingleDocument(int ignored) {
    StringBuilder documentContent = new StringBuilder(header);
    documentContent.append("<text>");
    documentContent.append(buffer.toString().trim());
    documentContent.append("</text>");
    String documentIdentifier = String.format("%s_%s",
            getArchiveIdentifier(),
            pageNumber);
    parsedDocument = new Document(documentIdentifier,
            documentContent.toString());
    parsedDocument.metadata = metadata;
    contentLength = 0;
    pageNumber = reader.getAttributeValue(null, "n");
  }
}
