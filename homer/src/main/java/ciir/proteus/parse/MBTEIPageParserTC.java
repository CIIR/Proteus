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
public class MBTEIPageParserTC extends MBTEIParser {

  public MBTEIPageParserTC(DocumentSplit split, Parameters p) throws Exception {
    super(split, p);
  }

  @Override
  public Document nextDocument() throws IOException {
    try {

      if (xml == null || !xml.hasNext()) {
        return null;
      }

      // only get the metadata the first time, otherwise we read real text
      if (metadata == null){
        metadata = parseMetadata(xml);
      }

      String pageText = nextPageText();
      if (pageText == null)
        return null;


      final String pageNumber = Integer.toString(getPageIndex());
      String archiveId = getArchiveIdentifier(split, metadata);
      Document page = new Document();
      // Note we do NOT want the archive ID to be parsed, we've seen some
      // that contain ":", ".", and lots of other characters that could be
      // interpreted as "word breaks"
      pageText += "<archiveid tokenizetagcontent=\"false\">" + archiveId + "</archiveid>";
      page.text = pageText;
      page.name = archiveId + "_" + pageNumber;
      page.metadata = metadata;
      page.metadata.put("pageNumber", pageNumber);
      TermCounter tc = new TermCounter("counts/");
      tc.count(page);
      return page;

    } catch (XMLStreamException e) {
      log.log(Level.WARNING, "XML Exception", e);
      return null;
    }
  }

}
