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
  List<ProteusDocument> doSearch(String kind, String query, Parameters qp) throws IOException ;
  List<ProteusDocument> findPassages(String kind, String query, List<String> ids) throws IOException;
  Map<String, ProteusDocument> getDocs(String kind, List<String> names, boolean metadata, boolean text);
  ProteusDocument getDocument(String kind, String name, boolean metadata, boolean text, String query);
  void loadNoteIndex(Parameters notes) throws Exception;
  Set<String> getKinds();
  Set<String> getStopWords() throws IOException;
  List<String> getQueryTerms(String query);
  List<String> getWorkingSetDocNames(String kind, String archiveid) throws IOException;
  Parameters getQueryParameters(String query);
  Boolean needPassage();
  void close() throws IOException;

}
