// BSD License (http://lemurproject.org/galago-license)
package org.lemurproject.galago.core.index.disk;

import java.io.FileNotFoundException;
import java.io.IOException;
import org.lemurproject.galago.core.index.GenericElement;
import org.lemurproject.galago.core.index.KeyValueWriter;
import org.lemurproject.galago.core.types.DocumentIndicator;
import org.lemurproject.galago.tupleflow.Counter;
import org.lemurproject.galago.tupleflow.InputClass;
import org.lemurproject.galago.tupleflow.Parameters;
import org.lemurproject.galago.tupleflow.TupleFlowParameters;
import org.lemurproject.galago.tupleflow.Utility;
import org.lemurproject.galago.tupleflow.execution.ErrorHandler;
import org.lemurproject.galago.tupleflow.execution.Verification;

/**
 * Writes the document indicator file 
 * 
 * @author sjh
 */
@InputClass(className = "org.lemurproject.galago.core.types.DocumentIndicator", order = {"+document"})
public class DocumentIndicatorWriter extends KeyValueWriter<DocumentIndicator> {

  int lastDocument = -1;
  Counter written;

  /** Creates a new instance of DocumentLengthsWriter */
  public DocumentIndicatorWriter(TupleFlowParameters parameters) throws FileNotFoundException, IOException {
    super(parameters, "Document indicators written");
    Parameters p = writer.getManifest();
    p.set("writerClass", DocumentIndicatorWriter.class.getName());
    p.set("readerClass", DocumentIndicatorReader.class.getName());

    // ensure we set a default value - default default value is 'false'
    p.set("default", parameters.getJSON().get("default", false));

    written = parameters.getCounter("Priors Written");
  }

  public GenericElement prepare(DocumentIndicator di) throws IOException {
    assert ((lastDocument < 0) || (lastDocument < di.document)) : "DocumentIndicatorWriter keys must be unique and in sorted order.";
    GenericElement element =
            new GenericElement(Utility.fromInt((int)di.document),
            Utility.fromBoolean(di.indicator));

    if (written != null) {
      written.increment();
    }
    return element;
  }

  public static void verify(TupleFlowParameters parameters, ErrorHandler handler) {
    if (!parameters.getJSON().isString("filename")) {
      handler.addError("KeyValueWriters require a 'filename' parameter.");
      return;
    }

    String index = parameters.getJSON().getString("filename");
    Verification.requireWriteableFile(index, handler);
  }
}
