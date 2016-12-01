package ciir.proteus.util;

import ciir.proteus.system.ProteusDocument;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jfoley, michaelz
 */
public class RetrievalUtil {

  public static List<String> names(List<ProteusDocument> docs) {
    ArrayList<String> names = new ArrayList<>();
    for (ProteusDocument doc : docs) {
      names.add(doc.name);
    }
    return names;
  }


  public static List<Long> ids(List<ProteusDocument> docs) {
    ArrayList<Long> ids = new ArrayList<>();
    for (ProteusDocument doc : docs) {
      ids.add(doc.identifier);
    }
    return ids;
  }


}
