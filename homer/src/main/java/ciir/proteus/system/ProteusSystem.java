package ciir.proteus.system;

import ciir.proteus.users.UserDatabase;
import ciir.proteus.users.UserDatabaseFactory;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import org.lemurproject.galago.core.index.mem.FlushToDisk;
import org.lemurproject.galago.core.index.mem.MemoryIndex;
import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.parse.Tag;
import org.lemurproject.galago.core.parse.TagTokenizer;
import org.lemurproject.galago.core.retrieval.Retrieval;
import org.lemurproject.galago.core.retrieval.RetrievalFactory;
import org.lemurproject.galago.core.retrieval.ScoredDocument;
import org.lemurproject.galago.core.retrieval.processing.MaxPassageFinder;
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.utility.Parameters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProteusSystem {

  public final String defaultKind;
  private final Parameters config;
  public Map<String, Retrieval> kinds;
  public MemoryIndex noteIndex = null;
  public UserDatabase userdb;
  final private SocketIOServer broadcastServer;

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

    // RetrievalFactory can't handle memory indexes with disk based indexes.
    // for now we'll always create a memory based index for notes and add
    // it to the CORPUS kind. We could do it for ALL kinds but that'd be
    // inefficient because we need to reload them
    // each time we want to make the notes searchable - which could be
    // every time we add a note.

    loadNoteIndex();

    // only configure if we need to
    if (argp.containsKey("broadcast")) {
      Parameters broadcastParams = argp.getMap("broadcast");
      Configuration config = new Configuration();
      config.setHostname(broadcastParams.get("url", "localhost"));
      config.setPort(broadcastParams.getInt("port"));
      broadcastServer = new SocketIOServer(config);
      broadcastServer.start();
    } else {
      broadcastServer = null;
    }

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
    if (broadcastServer != null)
      broadcastServer.stop();
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

  public void broadcastMsg(BroadcastMsg msg) {

    if (broadcastServer == null)
      return;

    broadcastServer.getBroadcastOperations().sendEvent("ProteusEvent", msg);

  }

  public void loadNoteIndex() throws Exception {

    String noteIndexPath = config.get("noteIndex", "");
    if (noteIndexPath.length() == 0) {
      return;
    }

    TagTokenizer tok = new TagTokenizer();

    // add disk flushed memory index to the "ia-corpus" kind

    noteIndex = new MemoryIndex(Parameters.parseArray("makecorpus", true));

    //            // get all the notes and add them as documents
    // TODO : get corpus number
    Parameters notes = this.userdb.getNotesForCorpus(1);
    List<Parameters> arr = notes.getAsList("rows");

    for (Parameters p : arr) {
      Document d = new Document();
      d.name = p.get("resource") + "_" + p.get("id");
      d.text = p.getString("user").split("@")[0] + " : " + p.get("quote") + " : " + p.get("text");
      d.tags = new ArrayList<Tag>();
      d.metadata = new HashMap<String, String>();
      // TODO : do we use metadata for things like who made the note, etc?
      d.metadata.put("docType", "note");
      tok.process(d);
      noteIndex.process(d);
    }

    // flush the index to disk
    FlushToDisk.flushMemoryIndex(noteIndex, noteIndexPath);

    Retrieval retrieval = getRetrieval("ia-corpus");
    Parameters newParams = Parameters.create();
    Parameters globalParams = retrieval.getGlobalParameters();
    List<String> idx = globalParams.getAsList("index");

    // only add the note index path if it's not already there
    if (idx.contains(noteIndexPath) == false){
      idx.add(noteIndexPath);
    }

    newParams.put("index", idx);
    kinds.put("ia-corpus", RetrievalFactory.create(newParams));

  }
}
