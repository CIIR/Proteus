package ciir.proteus.tools.apps;

import ciir.proteus.system.ProteusDocument;
import ciir.proteus.util.ListUtil;
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
import org.lemurproject.galago.utility.Parameters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by michaelz on 11/28/2016.
 */
public class Galago extends IndexType {

  Node parsedQuery = null;
  private MemoryIndex noteIndex = null;

  public Galago(Parameters argp) {
    super(argp);

    Parameters kindCfg = argp.getMap("kinds");
    for (String kind : kindCfg.keySet()) {
      try {
        kinds.put(kind, RetrievalFactory.create(kindCfg.getMap(kind)));
      } catch (Exception e) {
        throw new IllegalArgumentException(e);
      }
    }
  }

  @Override
  public void whoAmI() {
    System.out.println("I'm the Galago Version!");
  }

  private Retrieval getRetrieval(String kind) {
    Retrieval r = (Retrieval) kinds.get(kind);
    if (r == null) {
      throw new IllegalArgumentException("No retrieval for kind=" + kind);
    }
    return r;
  }

  @Override
  public List<String> getQueryTerms(String query) {
    if (query.isEmpty()) {
      return null;
    }
    // assume Simple query language
    Node parsed = null;
    try {
      parsed = SimpleQuery.parseTree(query);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return QueryUtil.queryTerms(parsed);
  }

  @Override
  public List<String> getWorkingSetDocNames(String kind, String archiveid) throws IOException {

    // There are some archive IDs that would get parsed is using the simple
    // query language (ex: poems___00wott) so we use the regular Galago syntax.
    String setQuery = "#combine(#inside( #text:" + archiveid + "() #field:archiveid() ))";
    Parameters tmpParams = Parameters.create();
    tmpParams.set("queryType", "StructuredQuery");
    List<ProteusDocument> workingSet = doSearch(kind, setQuery, tmpParams);
    List<String> ids = new ArrayList<>();
    for (ProteusDocument doc : workingSet) {
      ids.add(doc.name);
    }
    return ids;

  }

  @Override
  public Parameters getQueryParameters(String query) {
    Parameters p = Parameters.create();
    // parsedQuery is a class variable so we don't have to
    // re-parse the query when getting these parameters.
    // However, if they are searching within a corpus WITHOUT
    // a query, the old value would be returned, so we use the
    // actual query as a saftey check.
    if (query.isEmpty()) {
      parsedQuery = null;
      return p;
    }
    if (parsedQuery != null) {
      p.set("parsedQuery", parsedQuery.toString());
      p.set("queryTerms", QueryUtil.queryTerms(parsedQuery));
    }
    return p;
  }

  @Override
  public List<ProteusDocument> doSearch(String kind, String query, Parameters qp) throws IOException {

    // setting this to false, otherwise #scale queries fail
    // see: https://sourceforge.net/p/lemur/bugs/272/
    qp.put("deltaReady", false);

    // it's possible for the query to be empty IF we're searching just by labels or within a corpus
    if (!query.isEmpty() && !qp.containsKey("queryType")) {
      if (config.get("queryType", "simple").equals("simple")) {
        qp.set("queryType", "SimpleQuery");
      } else {
        qp.set("queryType", "StructuredQuery");
      }
    }

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
    List<ProteusDocument> results = new ArrayList<>();
    try {

      Node ready = retrieval.transformQuery(query, qp);
      List<ScoredDocument> docs = retrieval.executeQuery(ready, qp).scoredDocuments;
      for (ScoredDocument doc : docs) {
        ProteusDocument tmp = getDocument(kind, doc.documentName, true, true, null );
        tmp.rank = doc.getRank();
        tmp.score = doc.getScore();
        if (qp.get("passageQuery", false) && (doc instanceof ScoredPassage)) {
          ScoredPassage psg = (ScoredPassage) doc;
          tmp.snippet = String.join(" ", ListUtil.slice(tmp.terms, psg.begin, psg.end));
          // get the page the snippet is on
          // page breaks are <div> tags
          for (Tag t : tmp.tags) {
            if (t.name.equals("div")) {

              if (psg.begin <= t.end) {
                Integer termsOnPage = t.end - psg.begin;
                Integer termsOnNextPage = psg.end - t.end;
                if (termsOnNextPage > termsOnPage) {
                  continue; // use the next page
                }
                tmp.snippetPage = t.attributes.get("page");
                break;
              }
            }
          }

        }
        results.add(tmp);
      }
      return results;

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  } // end doSearch()

  @Override
  public ProteusDocument getDocument(String kind, String name, boolean metadata, boolean text, String query) {
    try {

      Document doc = getRetrieval(kind).getDocument(name, new Document.DocumentComponents(text, metadata, text));

      return (doc == null ? null : new ProteusDocument(doc2param(doc)));

    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

  @Override
  public Map<String, ProteusDocument> getDocs(String kind, List<String> names, boolean metadata, boolean text) {
    try {

      Map<String, Document> docs = getRetrieval(kind).getDocuments(names, new Document.DocumentComponents(text, metadata, text));

      Map<String, ProteusDocument> pdocs = new HashMap<>();

      Set<String> docNames = docs.keySet();
      for (String name : docNames) {
        // it is possible that a document may not exist, usually if it's a blank page
        Document d = docs.get(name);
        if (d != null) {
          pdocs.put(name, new ProteusDocument(doc2param(d)));
        }
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
  @Override
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

  @Override
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

  @Override
  public void close() throws IOException {
    for (Object ret : kinds.values()) {
      ((Retrieval)ret).close();
    }
  }

  private static Parameters doc2param(Document d) {
    // it is possible that a document may not exist, usually if it's a blank page
    if (d == null) {
      return null;
    }
    Parameters p = Parameters.create();
    p.set("identifier", d.identifier);
    p.set("name", d.name);
    p.set("text", d.text);
    p.put("metadata", d.metadata != null ? d.metadata : Collections.emptyMap());
    p.set("terms", d.terms != null ? d.terms : Collections.emptyList());
    p.set("tags", d.tags != null ? d.tags : Collections.emptyList());

    return p;
  }

  @Override
  public Boolean needPassage() {
    return true;
  }
}
