package ciir.proteus.index;

import java.io.FileNotFoundException;
import java.io.IOException;
import org.lemurproject.galago.core.index.GenericElement;
import org.lemurproject.galago.core.index.KeyValueWriter;
import org.lemurproject.galago.core.types.KeyValuePair;
import org.lemurproject.galago.tupleflow.InputClass;
import org.lemurproject.galago.tupleflow.Parameters;
import org.lemurproject.galago.tupleflow.TupleFlowParameters;

/**
 * @author irmarc
 */
@InputClass(className = "org.lemurproject.galago.core.types.KeyValuePair")
public class PictureDocumentWriter extends KeyValueWriter<KeyValuePair> {

  public PictureDocumentWriter(TupleFlowParameters parameters) throws FileNotFoundException, IOException {
    super(parameters, "Pic Documents Written");
    Parameters manifest = writer.getManifest();
    manifest.set("writerClass", this.getClass().getName());
  }

  public GenericElement prepare(KeyValuePair kvp) {
    return new GenericElement(kvp.key, kvp.value);
  }
}
