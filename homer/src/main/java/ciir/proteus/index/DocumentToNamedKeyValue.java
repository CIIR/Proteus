// BSD License (http://lemurproject.org/galago-license)
package ciir.proteus.index;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.types.KeyValuePair;
import org.lemurproject.galago.tupleflow.InputClass;
import org.lemurproject.galago.tupleflow.OutputClass;
import org.lemurproject.galago.tupleflow.StandardStep;
import org.lemurproject.galago.tupleflow.TupleFlowParameters;
import org.lemurproject.galago.tupleflow.Utility;
import org.lemurproject.galago.tupleflow.execution.Verified;
import org.xerial.snappy.SnappyOutputStream;

/**
 * Turns documents into KeyValuePair objects based upon their name field.
 * 
 * (String name, Document document)
 *
 * @author jfoley
 */
@Verified
@InputClass(className = "org.lemurproject.galago.core.parse.Document")
@OutputClass(className = "org.lemurproject.galago.core.types.KeyValuePair")
public class DocumentToNamedKeyValue  extends StandardStep<Document, KeyValuePair> implements KeyValuePair.Source {
  
  boolean compressed;

  public DocumentToNamedKeyValue(TupleFlowParameters parameters) {
    compressed = parameters.getJSON().get("compressed", true);
  }

  @Override
  public void process(Document document) throws IOException {
    ByteArrayOutputStream array = new ByteArrayOutputStream();
    ObjectOutputStream output;
    if (compressed) {
      output = new ObjectOutputStream(new SnappyOutputStream(array));
    } else {
      output = new ObjectOutputStream(array);
    }

    output.writeObject(document);
    output.close();

    byte[] key = Utility.fromString(document.name);
    byte[] value = array.toByteArray();
    KeyValuePair pair = new KeyValuePair(key, value);
    processor.process(pair);
  }
}
