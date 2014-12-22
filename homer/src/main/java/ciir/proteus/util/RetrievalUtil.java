package ciir.proteus.util;

import org.lemurproject.galago.core.retrieval.ScoredDocument;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jfoley.
 */
public class RetrievalUtil {
  public static List<String> names(List<ScoredDocument> docs) {
    ArrayList<String> names = new ArrayList<>();
    for (ScoredDocument doc : docs) {
      names.add(doc.documentName);
    }
    return names;
  }

}
