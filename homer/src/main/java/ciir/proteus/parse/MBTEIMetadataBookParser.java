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
 *
 * This parser is used to add field posting list(s) to an index.
 * field posting list(s) are written out to the "indexPath".
 * The postings lists can then be copied into the existing index and those fields will be searchable.
 * The "names-reverse-source" field points to the exising index and
 * is used to look up the internal doc id.
 * The "metadata-field-map" is used to rename the fields the
 * Internet Archive gives the fields.
 * Below is an example config file:
 *
{
  "server": true,
  "corpus": false,
  "indexPath": "F:\\data\\book-add-metadata",
  "inputPath": ["F:\\books\\caribbean\\"],
  "port": 2332,
  "tokenizer": {
    "fields": ["author", "publisher", "date"	]
  },
  "parser": {
    "externalParsers": [
      {
      "filetype": "toktei",
      "class": "ciir.proteus.parse.MBTEIMetadataParser"
      }
    ],
    "names-reverse-source": "F:\\data\\caribbean-books\\names.reverse",
    "metadata-field-map" : {
      "creator" : "author"
    }
  }
}
*/
public class MBTEIMetadataBookParser extends MBTEIMetadataParser {

  private String archiveID = null;


  public MBTEIMetadataBookParser(DocumentSplit split, Parameters p) throws Exception {
    super(split, p);
  }

  @Override
  public Document nextDocument() throws IOException {
    try  {
      // TODO: this data really should come from the existing index's corpus, that way
      // we don't have to hunt down the source documents. However the metadata is stored
      // in a hash map so prior to this version, only one value was stored. So books that
      // have more than one author lose data.

      // the UniversalParser calls this until it returns null. IF we already
      // have a document name (archiveid) we can just return null and skip
      // the bit of extra processing below.
      if (archiveID != null || xml == null || !xml.hasNext()) {
        return null;
      }

      metadata = parseMetadata(xml);
      archiveID = getArchiveIdentifier(split, metadata);

      Document doc = new Document();

      doc.identifier = reader.getDocumentIdentifier(archiveID);

      doc.text = getFields();
      doc.metadata = metadata;
      doc.name = archiveID;
      return doc;

    } catch (XMLStreamException e) {
      log.log(Level.WARNING, "XML Exception", e);
      throw new IOException(e);
    }

  }

}
