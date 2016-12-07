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
  void init(Parameters pargs);
  List<ProteusDocument> doSearch(String kind, String query, Parameters qp) throws IOException ;
  List<ProteusDocument> findPassages(String kind, String query, List<String> ids) throws IOException;
  Map<String, ProteusDocument> getDocs(String kind, List<String> names, boolean metadata, boolean text);
  ProteusDocument getDocument(String kind, String names, boolean metadata, boolean text);
  void loadNoteIndex(Parameters notes) throws Exception;
  Set<String> getKinds();
  Set<String> getStopWords() throws IOException;
  List<String> getQueryTerms(String query);
  Parameters getQueryParameters(String query);
  void close() throws IOException;

}
