package ciir.proteus.server.action;

import ciir.proteus.system.DocumentAnnotator;
import ciir.proteus.system.ProteusSystem;
import ciir.proteus.users.error.DBError;
import ciir.proteus.util.ListUtil;
import ciir.proteus.util.QueryUtil;
import org.lemurproject.galago.core.retrieval.ScoredDocument;
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.core.retrieval.query.StructuredQuery;
import org.lemurproject.galago.tupleflow.Parameters;

import java.util.Collections;
import java.util.List;

public class JSONSearch implements JSONHandler {

  private final ProteusSystem system;

  public JSONSearch(ProteusSystem sys) {
    this.system = sys;
  }

  @Override
  public Parameters handle(String method, String path, Parameters reqp) throws DBError {
    String query = reqp.getAsString("q");
    String kind = reqp.get("kind", system.defaultKind);
    int numResults = (int) reqp.get("n", 10);
    int skipResults = (int) reqp.get("skip", 0);

    if (numResults > 1000) {
      throw new IllegalArgumentException("Let's not put too many on a page...");
    }

    Node pquery = StructuredQuery.parse(query);
    Parameters qp = new Parameters();
    qp.put("requested", numResults + skipResults);
    List<ScoredDocument> docs = ListUtil.drop(system.search(kind, pquery, qp), skipResults);

    Parameters response = new Parameters();

    List<Parameters> results = Collections.emptyList();
    if (!docs.isEmpty()) {
      results = DocumentAnnotator.annotate(this.system, kind, docs, pquery, reqp);
    }

    response.set("results", results);
    response.set("parsedQuery", pquery.toString());
    response.set("queryTerms", QueryUtil.queryTerms(pquery));
    return response;
  }

}
