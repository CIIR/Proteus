package ciir.proteus.system;

import ciir.proteus.users.UserDatabase;
import ciir.proteus.users.UserDatabaseFactory;
import ciir.proteus.util.RetrievalUtil;
import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.retrieval.Retrieval;
import org.lemurproject.galago.core.retrieval.RetrievalFactory;
import org.lemurproject.galago.core.retrieval.ScoredDocument;
import org.lemurproject.galago.core.retrieval.processing.MaxPassageFinder;
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.core.retrieval.query.StructuredQuery;
import org.lemurproject.galago.tupleflow.Parameters;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProteusSystem {
  public final String defaultKind;
  private final Parameters config;
  public Map<String,Retrieval> kinds;
  public UserDatabase userdb;

  public ProteusSystem(Parameters argp) {
    this.config = argp;
    this.defaultKind = argp.getString("defaultKind");

    kinds = new HashMap<String,Retrieval>();
    Parameters kindCfg = argp.getMap("kinds");
    for(String kind : kindCfg.keySet()) {
      try {
        kinds.put(kind, RetrievalFactory.instance(kindCfg.getMap(kind)));
      } catch (Exception e) {
        throw new IllegalArgumentException(e);
      }
    }

    this.userdb = UserDatabaseFactory.instance(argp.getMap("userdb"));
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

  public List<ScoredDocument> findPassages(String kind, String query, List<ScoredDocument> docs) {
    List<String> names = RetrievalUtil.names(docs);

    // find max passage for each document
    Parameters qp = new Parameters();
    qp.set("working", names);
    qp.set("processingModel", MaxPassageFinder.class.getCanonicalName());
    qp.set("passageQuery", true);
    qp.set("passageSize", 100);
    qp.set("passageShift", 50);


    return search(kind, StructuredQuery.parse(query), qp);
  }

  public void close() throws IOException {
    for(Retrieval ret : kinds.values()) {
      ret.close();
    }
    userdb.close();
  }

  public Map<String, Document> getDocs(String kind, List<String> names, boolean metadata, boolean text) {
    final boolean tokenize = true;
    try {
      return getRetrieval(kind).getDocuments(names, new Document.DocumentComponents(metadata, text, tokenize));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public Parameters getConfig() { return config; }
}
