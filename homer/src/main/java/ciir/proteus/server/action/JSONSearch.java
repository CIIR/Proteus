package ciir.proteus.server.action;

import ciir.proteus.system.SearchSystem;
import ciir.proteus.util.ListUtil;
import org.lemurproject.galago.core.retrieval.ScoredDocument;
import org.lemurproject.galago.tupleflow.Parameters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class JSONSearch implements RequestHandler {
  private final SearchSystem system;
  private final Parameters conf;

  public JSONSearch(SearchSystem sys, Parameters conf) {
    this.system = sys;
    this.conf = conf;
  }

  @Override
  public Parameters handle(Parameters reqp) {
    String query = reqp.getAsString("q");
    String kind = reqp.get("kind", system.defaultKind);
    int numResults = (int) reqp.get("n", 10);
    int skipResults = (int) reqp.get("skip", 0);
    boolean snippets = reqp.get("snippets", true);

    List<ScoredDocument> docs = ListUtil.drop(system.search(kind, query, numResults+skipResults), skipResults);

    Parameters response = new Parameters();
    ArrayList<Parameters> results = new ArrayList<Parameters>();

    Map<String,String> snippetText = Collections.emptyMap();
    if(snippets) {
      snippetText = system.findPassages(kind, query, docs);
    }

    for(ScoredDocument sdoc : docs) {
      Parameters docp = new Parameters();
      docp.set("name", sdoc.documentName);
      docp.set("rank", sdoc.rank);
      docp.set("score", sdoc.score);
      if(snippets) {
        docp.set("snippet", snippetText.get(sdoc.documentName));
      }
      results.add(docp);
    }



    response.set("results", results);
    return response;
  }


}
