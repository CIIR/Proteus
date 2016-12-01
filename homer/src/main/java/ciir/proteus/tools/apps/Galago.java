package ciir.proteus.tools.apps;

import ciir.proteus.system.ProteusDocument;
import ciir.proteus.util.QueryUtil;
import org.lemurproject.galago.core.index.mem.FlushToDisk;
import org.lemurproject.galago.core.index.mem.MemoryIndex;
import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.parse.Tag;
import org.lemurproject.galago.core.parse.TagTokenizer;
import org.lemurproject.galago.core.retrieval.Retrieval;
import org.lemurproject.galago.core.retrieval.RetrievalFactory;
import org.lemurproject.galago.core.retrieval.ScoredDocument;
import org.lemurproject.galago.core.retrieval.ScoredPassage;
import org.lemurproject.galago.core.retrieval.processing.MaxPassageFinder;
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.core.retrieval.query.SimpleQuery;
import org.lemurproject.galago.core.retrieval.query.StructuredQuery;
import org.lemurproject.galago.core.util.WordLists;
import org.lemurproject.galago.utility.Parameters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by michaelz on 11/28/2016.
 */
public class Galago implements IndexType {

  private final Map<String, Retrieval> kinds = new HashMap<>();
  Node parsedQuery = null;
  private Parameters config;
  private MemoryIndex noteIndex = null;

  // TODO make IndexType abstract so ew have common code in one place.
  public void init(Parameters argp) {
    config = argp;
    Parameters kindCfg = argp.getMap("kinds");
    for (String kind : kindCfg.keySet()) {
      try {
        kinds.put(kind, RetrievalFactory.create(kindCfg.getMap(kind)));
      } catch (Exception e) {
        throw new IllegalArgumentException(e);
      }
    }
  }

  public void whoAmI() {
    System.out.println("I'm the Galago Version!");
  }

  public Set<String> getKinds() {
    return kinds.keySet();
  }

  @Override
  public Set<String> getStopWords() throws IOException {
    return WordLists.getWordList("rmstop");
  }

  private Retrieval getRetrieval(String kind) {
    Retrieval r = kinds.get(kind);
    if (r == null) {
      throw new IllegalArgumentException("No retrieval for kind=" + kind);
    }
    return r;
  }

  public Parameters getQueryParameters() {
    Parameters p = Parameters.create();
    if (parsedQuery != null) {
      p.set("parsedQuery", parsedQuery.toString());
      p.set("queryTerms", QueryUtil.queryTerms(parsedQuery));
    }
    return p;
  }

  public List<ProteusDocument> doSearch(String kind, String query, Parameters qp) throws IOException {

    // default to simple query language
    if (qp.get("queryType", "???").equals("StructuredQuery")) {
      parsedQuery = StructuredQuery.parse(query);
    } else {
      parsedQuery = SimpleQuery.parseTree(query);
    }
    return doSearch(kind, parsedQuery, qp, query);
  }

  private List<ProteusDocument> doSearch(String kind, Node query, Parameters qp, String queryText) throws IOException {

    Retrieval retrieval = getRetrieval(kind);
    ArrayList<ProteusDocument> results = new ArrayList<>();
    try {

      Node ready = retrieval.transformQuery(query, qp);
      List<ScoredDocument> docs = retrieval.executeQuery(ready, qp).scoredDocuments;
      for (ScoredDocument doc : docs) {
        Document gdoc = retrieval.getDocument(doc.documentName, Document.DocumentComponents.All);
        ProteusDocument tmp = new ProteusDocument(doc2param(gdoc));
        tmp.rank = doc.getRank();
        tmp.score = doc.getScore();
        if (qp.get("passageQuery", false) && (doc instanceof ScoredPassage)) {
          ScoredPassage psg = (ScoredPassage) doc;
          tmp.passageBegin = psg.begin;
          tmp.passageEnd = psg.end;
        }
        results.add(tmp);
      }
      return results;

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  } // end doSearch()

  @Override
  public ProteusDocument getDocument(String kind, String name, boolean metadata, boolean text) {
    // TODO common code with getDocs()
    try {
      Document.DocumentComponents docOpts = new Document.DocumentComponents();
      docOpts.text = text;
      docOpts.tokenize = text;
      docOpts.metadata = metadata;
      Retrieval r = getRetrieval(kind);

      Document doc = r.getDocument(name, docOpts);

      // convert to ProteusDocuments
      return new ProteusDocument(doc2param(doc));

    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

  public Map<String, ProteusDocument> getDocs(String kind, List<String> names, boolean metadata, boolean text) {
    try {
      Document.DocumentComponents docOpts = new Document.DocumentComponents();
      docOpts.text = text;
      docOpts.tokenize = text;
      docOpts.metadata = metadata;
      Retrieval r = getRetrieval(kind);

      Map<String, Document> docs = r.getDocuments(names, docOpts);

      // convert to ProteusDocuments
      Map<String, ProteusDocument> pdocs = new HashMap<String, ProteusDocument>();

      Set<String> docNames = docs.keySet();
      for (String name : docNames) {
        pdocs.put(name, new ProteusDocument(doc2param(docs.get(name))));
      }

      return pdocs;

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  // RetrievalFactory can't handle memory indexes with disk based indexes.
  // for now we'll always create a memory based index for notes and add
  // it to the CORPUS kind. We could do it for ALL kinds but that'd be
  // inefficient because we need to reload them
  // each time we want to make the notes searchable - which could be
  // every time we add a note.
  public void loadNoteIndex(Parameters notes) throws Exception {

    Parameters noteParams = config.get("notes", Parameters.create());
    String noteIndexPath = noteParams.get("noteIndex", "");
    if (noteIndexPath.isEmpty()) {
      return;
    }
    List<Parameters> arr = notes.getAsList("rows");

    // only have to do the below logic if we have notes
    if (arr.isEmpty()) {
      return;
    }

    List<String> noteFields = noteParams.getAsList("noteFields", String.class);

    Parameters memIdxParams = Parameters.create();
    memIdxParams.set("corpus", true);
    memIdxParams.set("tokenizer", Parameters.create());
    memIdxParams.getMap("tokenizer").set("fields", noteFields.toArray());
    memIdxParams.getMap("tokenizer").set("class", TagTokenizer.class.getCanonicalName());

    noteIndex = new MemoryIndex(memIdxParams);

    for (Parameters p : arr) {
      Document d = new Document();
      d.name = p.get("resource") + "_" + p.get("id");
      d.text = "<b>" + p.getString("user").split("@")[0] + " : <i>" + p.get("text") + "</i></b> : " + p.get("quote");
      d.tags = new ArrayList<Tag>();
      d.metadata = new HashMap<String, String>();
      // TODO : do we use metadata for things like who made the note, etc?
      d.metadata.put("docType", "note");
      noteIndex.process(d);
    }

    // flush the index to disk
    FlushToDisk.flushMemoryIndex(noteIndex, noteIndexPath);

    // add disk flushed memory index to the "all" kind

    Retrieval retrieval = getRetrieval("all");
    Parameters newParams = Parameters.create();
    Parameters globalParams = retrieval.getGlobalParameters();
    List<String> idx = new ArrayList<String>();
    idx.addAll(globalParams.getAsList("index"));

    // only add the note index path if it's not already there
    if (idx.contains(noteIndexPath) == false) {
      idx.add(noteIndexPath);
    }

    newParams.put("index", idx);
    kinds.put("all", RetrievalFactory.create(newParams));

  }

  // ??? if lucene - just return start of doc for now?
  public List<ProteusDocument> findPassages(String kind, String query, List<String> ids) throws IOException {
    // find max passage for each document
    Parameters qp = Parameters.create();
    qp.set("working", ids);
    qp.set("processingModel", MaxPassageFinder.class.getCanonicalName());
    qp.set("passageQuery", true);
    qp.set("passageSize", 100);
    qp.set("passageShift", 50);

    return doSearch(kind, query, qp);
  }


  public void close() throws IOException {
    for (Retrieval ret : kinds.values()) {
      ret.close();
    }
  }

  public static Parameters doc2param(Document d) {
    Parameters p = Parameters.create();
    p.set("identifier", d.identifier);
    p.set("name", d.name);
    p.put("metadata", d.metadata);
    p.set("text", d.text);
    p.set("terms", d.terms);
    p.set("tags", d.tags);
    return p;
  }

}
