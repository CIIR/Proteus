package ciir.proteus.parse;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;

/**
 * Created by michaelz on 3/19/2015.
 *
 * To use NER for the MBTEI Book or Page parser,
 * set the "ner-model" parameter via a JSON configuration file such as:

  "parser": {
      "externalParsers": [
      {
       "filetype": "mbtei",
       "class": "ciir.proteus.parse.MBTEIBookParser"
      }
     ],
     "ner-model" : "src/main/resources/ner-classifiers/english.all.3class.distsim.crf.ser.gz"
  }

 *
 */
public class NamedEntityRecognizer {
  private static AbstractSequenceClassifier classifier = null;
  private static NamedEntityRecognizer instance = null;

  public static NamedEntityRecognizer getInstance() {
    if (instance == null)
      instance = new NamedEntityRecognizer();

    return instance;
  }

  public static AbstractSequenceClassifier getClassifier() {
    return classifier;
  }

  public static void initClassifier(String model) {
    if (classifier == null) {
      classifier = CRFClassifier.getClassifierNoExceptions(model);
    }
  }

  private NamedEntityRecognizer() {
    classifier = null;
  }
}
