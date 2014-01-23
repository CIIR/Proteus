// BSD License (http://lemurproject.org/galago-license)
package org.lemurproject.galago.core.parse;

import java.io.IOException;
import org.lemurproject.galago.core.types.FieldNumberWordPosition;
import org.lemurproject.galago.tupleflow.InputClass;
import org.lemurproject.galago.tupleflow.OutputClass;
import org.lemurproject.galago.tupleflow.StandardStep;
import org.lemurproject.galago.tupleflow.Utility;
import org.lemurproject.galago.tupleflow.execution.Verified;

/**
 *
 * @author jykim
 */
@InputClass(className = "org.lemurproject.galago.core.parse.Document")
@OutputClass(className = "org.lemurproject.galago.core.types.FieldNumberWordPosition")
@Verified
public class NumberedExtentPostingsExtractor extends StandardStep<Document, FieldNumberWordPosition> {

  @Override
  public void process(Document object) throws IOException {
    int number = object.identifier;
    for (Tag tag : object.tags) {
      String field = tag.name;
      for (int position = tag.begin; position < tag.end; position++) {
        byte[] word = Utility.fromString(object.terms.get(position));
        processor.process(new FieldNumberWordPosition(field, number, word, position));
      }
    }
  }
}
