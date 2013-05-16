// BSD License (http://lemurproject.org/galago-license)

package org.lemurproject.galago.core.index.disk;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import org.lemurproject.galago.tupleflow.execution.ErrorHandler;
import org.lemurproject.galago.tupleflow.execution.Verification;
import org.lemurproject.galago.tupleflow.Parameters;
import org.lemurproject.galago.tupleflow.Processor;
import org.lemurproject.galago.tupleflow.TupleFlowParameters;

/**
 * Writes a stream of objects to a text file.  Useful for debugging or as
 * output for simple jobs. The original version of this is the TextWriter
 * class in the TupleFlow package, however that is restricted to emitting
 * tuples (subclasses of the Type interface). This is more general.
 *
 * @author trevor
 */

public class DiskTextWriter<T> implements Processor<T> {
    BufferedWriter writer;

    public DiskTextWriter(TupleFlowParameters parameters) throws IOException {
        writer = new BufferedWriter(new FileWriter(parameters.getJSON().getString("filename")));
    }

    public void process(T object) throws IOException {
        writer.write(object.toString());
        writer.write("\n");
    }

    public void close() throws IOException {
        writer.close();
    }

    public static String getInputClass(TupleFlowParameters parameters) {
        return parameters.getJSON().getString("class");
    }

    public static boolean verify(TupleFlowParameters parameters, ErrorHandler handler) {
        Parameters p = parameters.getJSON();
        if (!Verification.requireParameters(new String[] { "filename", "class" }, p, handler))
            return false;
        if (!Verification.requireClass(p.getString("class"), handler))
            return false;
        if (!Verification.requireWriteableFile(p.getString("filename"), handler))
            return false;
        return true;
    }
}
