package ciir.proteus.server.action;

import ciir.proteus.system.ProteusSystem;
import ciir.proteus.util.ListUtil;
import ciir.proteus.util.QueryUtil;
import ciir.proteus.util.RetrievalUtil;
import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.retrieval.ScoredDocument;
import org.lemurproject.galago.core.retrieval.ScoredPassage;
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.core.retrieval.query.StructuredQuery;
import org.lemurproject.galago.tupleflow.Parameters;
import org.lemurproject.galago.tupleflow.Utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class JSONSearch implements JSONHandler {
  private final ProteusSystem system;

  public JSONSearch(ProteusSystem sys) {
    this.system = sys;
  }

  @Override
  public Parameters handle(String method, String path, Parameters reqp) {
    String query = reqp.getAsString("q");
    String kind = reqp.get("kind", system.defaultKind);
    int numResults = (int) reqp.get("n", 10);
    int skipResults = (int) reqp.get("skip", 0);

    if(numResults > 1000) {
      throw new IllegalArgumentException("Let's not put too many on a page...");
    }

    List<ScoredDocument> docs = ListUtil.drop(system.search(kind, query, numResults+skipResults), skipResults);

    Parameters response = new Parameters();

    List<Parameters> results = annotate(kind, docs, query, reqp);

    Node pquery = StructuredQuery.parse(query);

    response.set("request", reqp);
    response.set("results", results);
    response.set("parsedQuery", pquery.toString());
    response.set("queryTerms", QueryUtil.queryTerms(pquery));
    return response;
  }


  public List<Parameters> annotate(String kind, List<ScoredDocument> results, String query, Parameters reqp) {
    boolean snippets = reqp.get("snippets", true);
    boolean metadata = reqp.get("metadata", true);

    if(snippets) {
      results = system.findPassages(kind, query, results);
    }

    // result data
    ArrayList<Parameters> resultData = new ArrayList<Parameters>(results.size());

    Map<String,Document> pulled = Collections.emptyMap();

    // if we need to pull the documents:
    if(snippets || metadata) {
      pulled = system.getDocs(kind, RetrievalUtil.names(results), metadata, snippets);
    }

    for(ScoredDocument sdoc : results) {
      Document doc = pulled.get(sdoc.documentName);
      Parameters docp = new Parameters();

      // default annotations
      docp.set("name", sdoc.documentName);
      docp.set("rank", sdoc.rank);
      docp.set("score", sdoc.score);

      // metadata annotation
      if(doc != null && metadata) {
        docp.set("meta", Parameters.parseMap(doc.metadata));
      }
      // snippet annotation
      if(doc != null && snippets) {
        ScoredPassage psg = (ScoredPassage) sdoc;
        String snippet =
            workaround(Utility.join(ListUtil.slice(doc.terms, psg.begin, psg.end), " "));

        docp.set("snippet", snippet);
      }

      resultData.add(docp);
    }

    // return annotated data:
    return resultData;
  }

  /**
   * Workaround for escaping issues in galago.core
   * @param input noisy string from Galago's corpus
   * @return valid JSON string at all costs
   */
  private String workaround(String input) {
    StringBuilder justSafeChars = new StringBuilder();
    for(int i=0; i<input.length(); i++) {
      int x = input.codePointAt(i);
      // restrict to ascii
      if(x > 127)
        continue;

      // drop escapable
      char ch = (char) x;
      if(ch == '"' || ch == '\'' || ch == '\\')
        continue;

      justSafeChars.append(ch);
    }
    return justSafeChars.toString();
  }
}
