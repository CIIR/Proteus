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
public class Lucene implements IndexType {

  public void whoAmI() {
    System.out.println("I'm the Lucene Version!");
  }

  @Override
  public void init(Parameters pargs) {

  }

  public List<ProteusDocument> doSearch(String kind, String query, Parameters qp) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<ProteusDocument> findPassages(String kind, String query, List<String> ids) throws IOException {
    return null;
  }


  @Override
  public Map<String, ProteusDocument> getDocs(String kind, List<String> names, boolean metadata, boolean text) {
    return null;
  }

  @Override
  public ProteusDocument getDocument(String kind, String names, boolean metadata, boolean text) {
    return null;
  }

  @Override
  public void loadNoteIndex(Parameters notes) throws Exception {

  }

  @Override
  public Set<String> getKinds() {
    return null;
  }

  @Override
  public Set<String> getStopWords() {
    return null;
  }

  @Override
  public List<String> getQueryTerms(String query) {
    return null;
  }

  @Override
  public Parameters getQueryParameters() {
    return null;
  }

  @Override
  public void close() throws IOException {

  }

}
