package ciir.proteus.parse;

import org.lemurproject.galago.core.types.DocumentSplit;
import org.lemurproject.galago.tupleflow.Parameters;

class MBTEIMiscParser extends MBTEIEntityParser {

  public MBTEIMiscParser(DocumentSplit split, Parameters p) {
    super(split, p);
    restrict = "misc";
  }
}
