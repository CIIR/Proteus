package ciir.proteus.system;

import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.retrieval.Retrieval;
import org.lemurproject.galago.core.retrieval.RetrievalFactory;
import org.lemurproject.galago.core.retrieval.ScoredDocument;
import org.lemurproject.galago.core.retrieval.ScoredPassage;
import org.lemurproject.galago.core.retrieval.processing.MaxPassageFinder;
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.core.retrieval.query.StructuredQuery;
import org.lemurproject.galago.tupleflow.Parameters;

import java.io.IOException;
import java.util.*;

public class SearchSystem {
  private final Retrieval retrieval;

  public SearchSystem(Parameters argp) {
    try {
      this.retrieval = RetrievalFactory.instance(argp);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public List<ScoredDocument> search(Node query, Parameters qp) {
    try {
      Node ready = retrieval.transformQuery(query, qp);
      return retrieval.executeQuery(ready, qp).scoredDocuments;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public List<ScoredDocument> search(String query, int requested) {
    Parameters qp = new Parameters();
    qp.set("requested", requested);

    return search(StructuredQuery.parse(query), qp);
  }

  public Map<String, String> findPassages(String query, List<ScoredDocument> docs) {
    ArrayList<String> names = new ArrayList<String>();
    for(ScoredDocument doc : docs) {
      names.add(doc.documentName);
    }

    // find max passage for each document
    Parameters qp = new Parameters();
    qp.set("working", names);
    qp.set("processingModel", MaxPassageFinder.class.getCanonicalName());
    qp.set("passageQuery", true);
    qp.set("passageSize", 200);
    qp.set("passageShift", 100);

    List<ScoredDocument> passages = search(StructuredQuery.parse(query), qp);

    // pull all documents into a map by name
    Map<String,Document> pulledDocuments = Collections.emptyMap();
    try {
      pulledDocuments = retrieval.getDocuments(names, new Document.DocumentComponents(true, false, true));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    HashMap<String,String> results = new HashMap<String,String>();

    // collect terms from passage into results
    for(ScoredDocument psgdoc : passages) {
      ScoredPassage psg = (ScoredPassage) psgdoc;
      int start = psg.begin;
      int end = psg.end;
      List<String> terms = pulledDocuments.get(psg.documentName).terms;

      StringBuilder passageText = new StringBuilder();
      for(int i=start; i<end&&i<terms.size(); i++) {
        passageText.append(terms.get(i)).append(' ');
      }
      results.put(psg.documentName, passageText.toString());
    }

    return results;
  }
}
