package ciir.proteus.system;

import ciir.proteus.users.UserDatabase;
import ciir.proteus.users.UserDatabaseFactory;
import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.retrieval.Retrieval;
import org.lemurproject.galago.core.retrieval.RetrievalFactory;
import org.lemurproject.galago.core.retrieval.ScoredDocument;
import org.lemurproject.galago.core.retrieval.processing.MaxPassageFinder;
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.utility.Parameters;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProteusSystem {

    public final String defaultKind;
    private final Parameters config;
    public Map<String, Retrieval> kinds;
    public UserDatabase userdb;

    public ProteusSystem(Parameters argp) throws Exception {
        this.config = argp;
        this.defaultKind = argp.getString("defaultKind");

        kinds = new HashMap<>();
        Parameters kindCfg = argp.getMap("kinds");
        for (String kind : kindCfg.keySet()) {
            try {
                kinds.put(kind, RetrievalFactory.create(kindCfg.getMap(kind)));
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
        }

        this.userdb = UserDatabaseFactory.instance(argp.getMap("userdb"));
    }

    public Retrieval getRetrieval(String kind) {
        Retrieval r = kinds.get(kind);
        if (r == null) {
            throw new IllegalArgumentException("No retrieval for kind=" + kind);
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

    public List<ScoredDocument> findPassages(String kind, Node query, List<String> names) {
        // find max passage for each document
        Parameters qp = Parameters.create();
        qp.set("working", names);
        qp.set("processingModel", MaxPassageFinder.class.getCanonicalName());
        qp.set("passageQuery", true);
        qp.set("passageSize", 100);
        qp.set("passageShift", 50);

        return search(kind, query, qp);
    }

    public void close() throws IOException {
        for (Retrieval ret : kinds.values()) {
            ret.close();
        }
        userdb.close();
    }

    public Map<String, Document> getDocs(String kind, List<String> names, boolean metadata, boolean text) {
        try {
            Document.DocumentComponents docOpts = new Document.DocumentComponents();
            docOpts.text = text;
            docOpts.tokenize = text;
            docOpts.metadata = metadata;
            return getRetrieval(kind).getDocuments(names, docOpts);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Parameters getConfig() {
        return config;
    }
}
