package ciir.proteus.parse;

import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.types.DocumentSplit;
import org.lemurproject.galago.utility.Parameters;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.logging.Level;

/**
 * @author jfoley, michaelz
 */
public class MBTEIBookParser extends MBTEIParser {

  public MBTEIBookParser(DocumentSplit split, Parameters p) throws Exception {
    super(split, p);
  }

  @Override
  public Document nextDocument() throws IOException {
    try {
      if (xml == null || !xml.hasNext()) {
        return null;
      }

      metadata = parseMetadata(xml);
      String archiveID = getArchiveIdentifier(split, metadata);

      StringBuilder wholeDocBuffer = new StringBuilder();
      String pg;
      while ((pg = nextPageText()) != null) {
        // indicate the page
        wholeDocBuffer.append("<div class=\"page-break\" page=\"" + getPageIndex() + "\">");
        wholeDocBuffer.append(pg);
        wholeDocBuffer.append("</div>");
      }

      if (wholeDocBuffer.length() == 0){
        return null;
      }

      Document doc = new Document();

      // Note we do NOT want the archive ID to be parsed, we've seen some
      // that contain ":", ".", and lots of other characters that could be
      // interpreted as "word breaks"
//      wholeDocBuffer.append("<archiveid tokenizetagcontent=\"false\">");
//      wholeDocBuffer.append(archiveID);
//      wholeDocBuffer.append("</archiveid>");


      doc.text = wholeDocBuffer.toString();
      doc.metadata = metadata;
      doc.name = archiveID;

      return doc;

    } catch (XMLStreamException e) {
      log.log(Level.WARNING, "XML Exception", e);
      throw new IOException(e);
    }

  }


}
