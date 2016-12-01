package ciir.proteus.tools.apps;

import ciir.proteus.system.ProteusDocument;
import org.lemurproject.galago.utility.Parameters;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by michaelz on 11/28/2016.
 */
public interface IndexType {

  void whoAmI();
  public void init(Parameters pargs);
  public List<ProteusDocument> doSearch(String kind, String query, Parameters qp) throws IOException ;
  public List<ProteusDocument> findPassages(String kind, String query, List<String> ids) throws IOException;
  public Map<String, ProteusDocument> getDocs(String kind, List<String> names, boolean metadata, boolean text);
  public ProteusDocument getDocument(String kind, String names, boolean metadata, boolean text);
  public void loadNoteIndex(Parameters notes) throws Exception;
  public Set<String> getKinds();
  public Set<String> getStopWords() throws IOException;
  public List<String> getQueryTerms(String query);
  public Parameters getQueryParameters();
  public void close() throws IOException;

}
