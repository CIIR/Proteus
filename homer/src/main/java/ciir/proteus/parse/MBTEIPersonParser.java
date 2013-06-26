package ciir.proteus.parse;

import org.lemurproject.galago.core.types.DocumentSplit;
import org.lemurproject.galago.tupleflow.Parameters;

public class MBTEIPersonParser extends MBTEIEntityParser {

  public MBTEIPersonParser(DocumentSplit split, Parameters p) {
    super(split, p);
    restrict = "per";
  }
}
