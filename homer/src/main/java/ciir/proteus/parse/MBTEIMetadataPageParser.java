package ciir.proteus.parse;

import ciir.proteus.util.LoadNamesReverseUtil;
import org.lemurproject.galago.core.index.disk.DiskNameReverseReader;
import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.types.DocumentSplit;
import org.lemurproject.galago.utility.Parameters;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;

/**
 * @author michaelz
 */
public class MBTEIMetadataPageParser extends MBTEIMetadataParser {

  public MBTEIMetadataPageParser(DocumentSplit split, Parameters p) throws Exception {
    super(split, p);
  }

  @Override
  public Document nextDocument() throws IOException {
    try {

      if (xml == null || !xml.hasNext()) {
        return null;
      }

      // metadata is only at the start of the book so we don't
      // re-read every time.
      if (metadata == null){
        metadata = parseMetadata(xml);
      }

      String pageText = nextPageText();
      if (pageText == null)
        return null;

      final String pageNumber = Integer.toString(getPageIndex());
      String archiveId = getArchiveIdentifier(split, metadata);
      Document page = new Document();

      page.text = getFields();
      page.name = archiveId + "_" + pageNumber;
      page.metadata = metadata;
      page.metadata.put("pageNumber", pageNumber);
      return page;

    } catch (XMLStreamException e) {
      log.log(Level.WARNING, "XML Exception", e);
      return null;
    }
  }

}
