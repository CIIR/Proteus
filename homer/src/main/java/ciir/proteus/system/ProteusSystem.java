package ciir.proteus.system;

import ciir.proteus.users.UserDatabase;
import ciir.proteus.users.impl.H2Database;
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

public class ProteusSystem {
  public final String defaultKind;
  public final Map<String,Retrieval> kinds;
  public UserDatabase userdb;

  public ProteusSystem(Parameters argp) {
    this.defaultKind = argp.getString("defaultKind");

    kinds = new HashMap<String,Retrieval>();
    Parameters kindCfg = argp.getMap("kinds");
    for(String kind : kindCfg.keySet()) {
      try {
        kinds.put(kind, RetrievalFactory.instance(kindCfg.getMap(kind)));
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    this.userdb = null;
    if(argp.isMap("userdb")) {
      Parameters dbp = argp.getMap("userdb");
      String impl = dbp.get("impl", "H2Database");
      if(impl.equals("H2Database")) {
        userdb = new H2Database(dbp);
      }
    }
  }

  public Retrieval getRetrieval(String kind) {
    Retrieval r = kinds.get(kind);
    if(r == null) {
      throw new IllegalArgumentException("No retrieval for kind="+kind);
    }
    return r;
  }

  public List<ScoredDocument> search(String kind, Node query, Parameters qp) {
    Retrieval retrieval = getRetrieval(kind);
    try {
      Node ready = retrieval.transformQuery(query, qp);
      return retrieval.executeQuery(ready, qp).scoredDocuments;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public List<ScoredDocument> search(String kind, String query, int requested) {
    Parameters qp = new Parameters();
    qp.set("requested", requested);

    return search(kind, StructuredQuery.parse(query), qp);
  }

  public Map<String, String> findPassages(String kind, String query, List<ScoredDocument> docs) {
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


    List<ScoredDocument> passages = search(kind, StructuredQuery.parse(query), qp);

    // pull all documents into a map by name
    Map<String,Document> pulledDocuments = Collections.emptyMap();
    try {
      pulledDocuments = getRetrieval(kind).getDocuments(names, new Document.DocumentComponents(true, false, true));
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
