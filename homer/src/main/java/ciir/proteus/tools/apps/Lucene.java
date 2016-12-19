package ciir.proteus.tools.apps;

import ciir.proteus.system.ProteusDocument;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.TextFragment;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.lemurproject.galago.utility.Parameters;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by michaelz on 11/28/2016.
 */
public class Lucene extends IndexType {

  private Analyzer analyzer = null;
  private SearcherFactory searcherFactory = new SearcherFactory();
  private QueryParser parser = null;
  private List<ProteusDocument> resultData = null;

  public Lucene(Parameters argp) {
    super(argp);
    Parameters kindCfg = argp.getMap("kinds");
    for (String kind : kindCfg.keySet()) {
      try {
        kinds.put(kind, getReaderInstace(kindCfg.getMap(kind)));
      } catch (Exception e) {
        throw new IllegalArgumentException(e);
      }
    }
    analyzer = new StandardAnalyzer();
    parser = new QueryParser("tokens", analyzer);
  }

  // inspired by Galago's RetrievalFactory
  private IndexSearcher getReaderInstace(Parameters p) throws IOException {

    // if we have a single index:
    if (p.isString("index")) {
      IndexReader ir = DirectoryReader.open(FSDirectory.open(Paths.get(p.getString("index"))));
      return searcherFactory.newSearcher(ir, null);
      // if we have a list of index paths:
    } else if (p.isList("index")) {
      List<String> indexes = p.getList("index", String.class);
      int i = indexes.size();
      IndexReader readers[] = new IndexReader[i];

      for (int j = 0; j < i; j++) {
        readers[j] = DirectoryReader.open(FSDirectory.open(Paths.get(indexes.get(j))));
      }
      return searcherFactory.newSearcher(new MultiReader(readers), null);
      // otherwise we don't know what we have...
    } else {
      throw new RuntimeException("Could not open indexes from parameters :\n" + p.toString());
    }

  }

  @Override
  public void whoAmI() {
    System.out.println("I'm the Lucene Version!");
  }

  @Override
  public List<ProteusDocument> doSearch(String kind, String query, Parameters qp) throws IOException {

    IndexSearcher searcher = getSearcher(kind);
    BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();

    List<String> workingSetNames = new ArrayList<>();
    int numDocsRequested = qp.get("requested", 10);

    // TODO : add check for queries that may be too big
    try {
      if (qp.containsKey("working")) {
        workingSetNames = qp.getAsList("working");
        // don't include "id:" if it's alrady there
        String field = "id:";
        if (workingSetNames.get(0).contains("id:")){
          field = "";
        }
        booleanQuery.add(parser.parse(String.join(" OR " + field, workingSetNames)), BooleanClause.Occur.MUST);
        // we want to return every match in the working set, but don't use woring set size because
        // it could just be one term with a wild card
        numDocsRequested = 1000;
      }
      if (!query.isEmpty()) {
        booleanQuery.add(parser.parse(query), BooleanClause.Occur.MUST);
      }
    } catch (ParseException e) {
      e.printStackTrace();
    }

    // TODO : could be more efficient and user searchAfter() for results
    // beyond the first page. For now, we'll just get all requested and the
    // caller will grab the last N results to return.

    TopDocs luceneResults = searcher.search(booleanQuery.build(), numDocsRequested);

    ScoreDoc[] hits = luceneResults.scoreDocs;
    int numTotalHits = luceneResults.totalHits;

    resultData = new ArrayList<>(hits.length);

    System.out.println("found " + numTotalHits + " results");
    for (int k = 0; k < hits.length; k++) {
      ProteusDocument pd = getDocument(kind, hits[k].doc, true, true, query);
      pd.score = hits[k].score;
      pd.rank = k + 1;
      resultData.add(pd);
    }

    return resultData;
  }

  private IndexSearcher getSearcher(String kind) {
    IndexSearcher r = (IndexSearcher) kinds.get(kind);
    if (r == null) {
      throw new IllegalArgumentException("No IndexSearcher for kind=" + kind);
    }
    return r;
  }

  @Override
  public List<ProteusDocument> findPassages(String kind, String query, List<String> ids) throws IOException {
    return null;
  }

  @Override
  public Map<String, ProteusDocument> getDocs(String kind, List<String> names, boolean metadata, boolean text) {

    Map<String, ProteusDocument> pdocs = new HashMap<String, ProteusDocument>();

    for (String name : names) {
      // it is possible that a document may not exist, usually if it's a blank page
      ProteusDocument d = getDocument(kind, name, metadata, text, null);
      if (d != null) {
        pdocs.put(name, d);
      }
    }

    return pdocs;

  }

  // search by internal doc ID
  private ProteusDocument getDocument(String kind, int luceneDocID, boolean metadata, boolean text, String query) {
    IndexSearcher searcher = getSearcher(kind);
    Document doc = null;

    try {
      // TODO possible we need shard id too
      doc = searcher.doc(luceneDocID);

      String title = doc.get("id");

      Parameters docp = Parameters.create();
      docp.put("identifier", luceneDocID);
      org.jsoup.nodes.Document jsoup = Jsoup.parse(doc.get("body"));

      docp.put("text", jsoup.text());

      docp.put("name", title);
      docp.put("meta", Collections.emptyMap());
      docp.put("notes", Parameters.create());

      // parse on whitespace and punctuation
      List<String> tokens = Arrays.asList(jsoup.text().split("[\\p{Punct}\\s]+"));
      String terms[] = (String[]) tokens.toArray();
      docp.set("terms", terms);
      docp.put("tags", null);

      // find snippet(s) and highlight query terms
      docp.copyFrom(doHighlight(doc, luceneDocID, kind, query, tokens));

      return new ProteusDocument(docp);

    } catch (IOException | ParseException e) {
      e.printStackTrace();
    }
    return null;
  }

  private Parameters doHighlight(Document doc, int luceneDocID, String kind, String query, List<String> tokens) throws IOException, ParseException {

    Parameters ret = Parameters.create();

    if (query == null || query.isEmpty()) {
      ret.put("snippetPage", "1");
      // get first 100 terms
      ret.put("snippet", String.join(" ", tokens.subList(0, Math.min(100, tokens.size()))));
      return ret;
    }

    IndexSearcher searcher = getSearcher(kind);
    SimpleHTMLFormatter htmlFormatter = new SimpleHTMLFormatter("<lucene-hili>", "</lucene-hili>");
    Highlighter highlighter = null;

    highlighter = new Highlighter(htmlFormatter, new QueryScorer(parser.parse(query)));

    TokenStream tokenStream = TokenSources.getAnyTokenStream(searcher.getIndexReader(), luceneDocID, "body", analyzer);

    TextFragment[] frag = new TextFragment[0];
    String snippetText;
    // if there is no query, just use the first 100 terms
    if (query.isEmpty()) {
      snippetText = String.join(" ", tokens.subList(0, Math.min(100, tokens.size())));
    } else {
      try {
        // Note: fragment size is in characters (not words).
        // Make it dependent on the length of the doc, so short document (that may only have one fragment)
        // have longer fragments, long docs (like books) have multiple shorter fragments to better represent
        // the contents.
        SimpleFragmenter f = (SimpleFragmenter) highlighter.getTextFragmenter();
        // for now, we'll make an educated guess if it's book or a page and set accordingly
        if (doc.get("id").contains("_")) {
          f.setFragmentSize(500); // assume it's a page
        } else {
          f.setFragmentSize(100); // assume it's a book
        }
        highlighter.setTextFragmenter(f);
        // the number of fragments & tokens to analyze to get depends on the size of the document, we want to check the whole doc.
        // especially important for long documents like books. Note some docs may be less than 100 tokens.
        highlighter.setMaxDocCharsToAnalyze(doc.get("body").length());
        frag = highlighter.getBestTextFragments(tokenStream, doc.get("body"), false, tokens.size() / Math.min(tokens.size(), 100));

      } catch (InvalidTokenOffsetsException e) {
        e.printStackTrace();
      }
      List<String> txt = new ArrayList<>(frag.length);

      // get the relavent fragments.
      // For long documents like books, it may be nice in the future to have
      // each fragment link to the page it's on. We would probably want to use
      // the default fragment size of 100 if we implement something like that.
      for (int j = 0; j < frag.length; j++) {
        if ((frag[j] != null) && (frag[j].getScore() > 0)) {
          txt.add(frag[j].toString());
        }
      }

      StringBuilder fragments = null;
      try {
        // each fragment contains the entire document highlighted. Unfortunately it's
        // private so we need to do a little trickery to get it.
        fragments = (StringBuilder) FieldUtils.readField(frag[0], "markedUpText", true);
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }
      // to find the page the first snippet is on, we'll use the markedUpText field and search
      // for the first instance of "<lucene-hili>"
      org.jsoup.nodes.Document markupDoc = Jsoup.parse(fragments.toString());
      Element e = markupDoc.select("div lucene-hili").first();
      String pgNum = "1";
      // now go up until we find the parent <div>. Can't assume immediate parent because
      // it could be in an entity tag or something else.
      if (e != null) {
        Elements parents = e.parents();
        for (Element div : parents){
          if (div.tag().toString().equals("div")){
            pgNum = div.attr("page");
            break;
          }
        }
      }
      ret.put("snippetPage", pgNum);
      // limit to 10 fragments
      snippetText = String.join(" ... ", txt.subList(0, Math.min(10, txt.size())));
      // use this as the main text too since Lucene's done the hard work of highlighting query tersm
      ret.put("text", fragments.toString());
    }
    ret.put("snippet", snippetText);

    return ret;
  }

  // search by external doc ID
  @Override
  public ProteusDocument getDocument(String kind, String name, boolean metadata, boolean text, String query) {
    IndexSearcher searcher = getSearcher(kind);
    int luceneDocID = 0;
    try {
      // TODO possible we need shard too
      luceneDocID = findByDocno(searcher.getIndexReader(), "id", name);
    } catch (IOException e) {
      e.printStackTrace();
    }
    if (luceneDocID == -1) {
      System.out.println("No document found.");
      return null;
    }

    return getDocument(kind, luceneDocID, metadata, text, query);

  }


  @Override
  public void loadNoteIndex(Parameters notes) throws Exception {
    // Notes are not searchable (currently) in Lucene
  }

  @Override
  public List<String> getQueryTerms(String query) {
    return null; // not needed for lucene, highlighting of terms is done via the Highlighter class
  }

  @Override
  public Parameters getQueryParameters(String query) {
    return Parameters.create();
  }

  @Override
  public Boolean needPassage() {
    return false; // we get them when highlighting query terms.
  }

  @Override
  public List<String> getWorkingSetDocNames(String kind, String archiveid) throws IOException {
    return Arrays.asList("id:" + archiveid + "_*");
  }

  @Override
  public void close() throws IOException {
    for (Object is : kinds.values()) {
      ((IndexSearcher) is).getIndexReader().close();
    }
  }

  // from Jiepu https://github.com/jiepujiang/cs646_tutorials/blob/master/src/main/java/edu/umass/cs/cs646/utils/LuceneUtils.java
  private static int findByDocno(IndexReader index, String fieldDocno, String docno) throws IOException {
    BytesRef term = new BytesRef(docno);
    PostingsEnum posting = MultiFields.getTermDocsEnum(index, fieldDocno, term, PostingsEnum.NONE);
    if (posting != null) {
      int docid = posting.nextDoc();
      if (docid != PostingsEnum.NO_MORE_DOCS) {
        return docid;
      }
    }
    return -1;
  }

}
