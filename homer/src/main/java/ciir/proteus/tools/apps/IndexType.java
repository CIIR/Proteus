package ciir.proteus.tools.apps;

import ciir.proteus.system.ProteusDocument;
import org.lemurproject.galago.core.util.WordLists;
import org.lemurproject.galago.utility.Parameters;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by michaelz on 11/28/2016.
 */
public abstract class IndexType {

  protected Parameters config;
  protected Map<String, Object> kinds;
  IndexType(Parameters argp){
    config = argp;
    Parameters kindCfg = argp.getMap("kinds");
    kinds = new HashMap<String, Object>(kindCfg.size());
  }

  final public Set<String> getKinds() {
    return kinds.keySet();
  }

  public Set<String> getStopWords() throws IOException{
    // for now, use the Galago functionality to get stop words.
    // TODO - could be in base class if we change it from interface to abstract.
    return WordLists.getWordList("rmstop");
  }

  public abstract void whoAmI();
  public abstract void loadNoteIndex(Parameters notes) throws Exception ;
  public abstract List<ProteusDocument> doSearch(String kind, String query, Parameters qp) throws IOException ;
  public abstract List<ProteusDocument> findPassages(String kind, String query, List<String> ids) throws IOException;
  public abstract Map<String, ProteusDocument> getDocs(String kind, List<String> names, boolean metadata, boolean text);
  public abstract ProteusDocument getDocument(String kind, String name, boolean metadata, boolean text, String query);
  public abstract List<String> getQueryTerms(String query);
  public abstract List<String> getWorkingSetDocNames(String kind, String archiveid) throws IOException;
  public abstract Parameters getQueryParameters(String query);
  public abstract Boolean needPassage();
  public abstract void close() throws IOException;


}
