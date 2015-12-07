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
*/
public abstract class MBTEIMetadataParser extends MBTEIParser {

  protected static DiskNameReverseReader reader;
  protected Parameters fieldNameMapping;


  public MBTEIMetadataParser(DocumentSplit split, Parameters p) throws Exception {
    super(split, p);
    reader = LoadNamesReverseUtil.getReader(p.getAsString("names-reverse-source"));

    // the metadata has field names that aren't as obvious as we'd like so
    // we load a mapping from the IS field name to the name we'll use. "curator" to "author"
    // is the best example.

    fieldNameMapping = p.getMap("metadata-field-map");
  }

  public String getFields() throws IOException {

      StringBuilder sb = new StringBuilder();

      // write out ALL the metadata, only the "fields" specified in the config file
      // will get indexed and we can copy over whatever we need.

      for (Map.Entry<String, String> field : metadata.entrySet()){
        sb.append("<" + field.getKey() + ">" + field.getValue() + "</" + field.getKey() + ">");

        // see if we want to write out this value as a different field name.
        if (fieldNameMapping.containsKey(field.getKey())){
          sb.append("<" + fieldNameMapping.get(field.getKey()) + ">" + field.getValue() + "</" + fieldNameMapping.get(field.getKey()) + ">");
        }

      }

      return sb.toString();

  }

}
