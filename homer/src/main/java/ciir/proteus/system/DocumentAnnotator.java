package ciir.proteus.system;

import ciir.proteus.users.error.DBError;
import ciir.proteus.util.ListUtil;
import ciir.proteus.util.RetrievalUtil;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.procedure.TObjectIntProcedure;
import org.lemurproject.galago.core.parse.Tag;
import org.lemurproject.galago.utility.Parameters;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author jfoley, michaelz
 */
public class DocumentAnnotator {

  class TermFrequencies {
    private TObjectIntHashMap<String> tf = null;

    TermFrequencies() {
      tf = new TObjectIntHashMap<>();
    }

    public void addTerm(String term) {
      int val = tf.adjustOrPutValue(term, 1, 1);
    }

    public List<Parameters> getTopK(int k) {

      if (tf.isEmpty()) {
        return new ArrayList<Parameters>();
      }
      TIntArrayList freqList = new TIntArrayList(tf.values());
      freqList.sort();
      freqList.reverse();
      // remove any terms that have a count less than the kth (or last
      // if we don't have k)
      int realK = Math.min(k, freqList.size() - 1);
      int cutoff = freqList.get(realK);

      List<Map.Entry<String, Integer>> topTen = new ArrayList<>(realK);

      tf.forEachEntry(new TObjectIntProcedure<String>() {
        public boolean execute(String s, int i) {
          if (i >= cutoff) {
            topTen.add(new AbstractMap.SimpleEntry<String, Integer>(s, i));
          }
          return true;
        }
      });

      topTen.sort(Collections.reverseOrder(Map.Entry.comparingByValue()));
      List<Parameters> tmp = new ArrayList<Parameters>(realK);

      topTen.subList(0, realK).forEach(term -> {
        Parameters p = Parameters.create();
        p.put("term", term.getKey());
        p.put("count", term.getValue());
        tmp.add(p);
      });

      return tmp;

    }
  }

  private static Set<String> exclusionTerms = null;

  private TermFrequencies totalTF = null;
  private TermFrequencies snippetTF = null;
  private TermFrequencies totalBiGramTF = null;
  private TermFrequencies totalTriGramTF = null;

  private Map<String, TermFrequencies> allEntities = null;

  private boolean snippets = true;
  private boolean needTermFrequencies = true;

  public Parameters annotate(ProteusSystem system, String kind, String query, Parameters reqp, List<String> names) throws DBError, IOException {
    snippets = reqp.get("snippets", true);
    boolean metadata = reqp.get("metadata", true);
    Map<String, ProteusDocument> pulled = system.getDocs(kind, names, metadata, snippets);
    return annotate(system, kind, new ArrayList<ProteusDocument>(pulled.values()), query, reqp);
  }

  // TODO query could be in parameters
  // note that the query could be null if we want to get all documents for a label or corpus.
  public Parameters annotate(ProteusSystem system, String kind, List<ProteusDocument> results, String query, Parameters reqp) throws DBError, IOException {

    if (exclusionTerms == null) {
      // in some cases, the stop word list is immutable, so
      // use a temp Set to get around that.
      Set<String> tmpStopList = system.getIndex().getStopWords();
      exclusionTerms = new HashSet<>(tmpStopList.size() + 3);
      exclusionTerms.addAll(tmpStopList);
      // add some custom words to ignore
      exclusionTerms.add("digitized"); // some books have "digitized by google" at the bottom of each page)
      exclusionTerms.add("google");
      exclusionTerms.add("archiveid"); // skip any archive id fields
    }

    // we only need to calculate term frequencies IF we're searching within a subcorpora
    // because that's when we show the query builder
    needTermFrequencies = reqp.containsKey("subcorpora");
    snippets = reqp.get("snippets", true);
    boolean metadata = reqp.get("metadata", true);
    boolean overlapOnly = reqp.get("overlapOnly", false);
    int numEntities = reqp.get("top_k_entities", 0);
    int corpusID = reqp.get("corpus", -1);

    allEntities = new HashMap<String, TermFrequencies>();
    List<String> names = RetrievalUtil.names(results);

    // retrieve snippets if requested AND we have a query
    if (snippets && query != null && !query.isEmpty() && !results.isEmpty()) {
      results = system.getIndex().findPassages(kind, query, names);
    }

    totalTF = new TermFrequencies();
    snippetTF = new TermFrequencies();
    totalBiGramTF = new TermFrequencies();
    totalTriGramTF = new TermFrequencies();

    Parameters noteParams = system.getConfig().get("notes", Parameters.create());
    List<String> noteFields = noteParams.getAsList("noteFields", String.class);

    // result data
    ArrayList<Parameters> resultData = new ArrayList<>(results.size());

    for (ProteusDocument doc : results) {

      if (doc == null) {
        continue;
      }

      Parameters labels = system.userdb.getResourceLabels(doc.name, corpusID);
      // "overlap" is a document that is in more than one subcorpus
      if (overlapOnly && labels.get("newLabels", Parameters.create()).size() < 2) {
        continue;
      }

      Parameters docp = Parameters.create();
      docp.copyFrom(labels);

      // if this is a note, use the whole text
      if (isNote(doc)) {

        docp.put("text", doc.text);
        docp.put("snippet", doc.text); // for notes, use the whole thing as a snippet

        //the 1st token is the person who created the comment and we don't want
        // to count that in the TF, so replace it with a stop word.
        doc.terms.set(0, "a");
        doc.passageBegin = 0;
        doc.passageEnd = doc.text.length();

      } else if (snippets) {
        // if the query was null, we'll just get the first part of the document.

        doc.passageBegin = Math.max(0, doc.passageBegin);
        doc.passageEnd = Math.max(100, doc.passageEnd);

        String snippet = String.join(" ", ListUtil.slice(doc.terms, doc.passageBegin, doc.passageEnd));
        docp.put("snippet", snippet);

        docp.put("snippetPage", findSnippetPage(doc));

      } // end if snippet

      // count terms frequencies
      countTerms(doc);

      // count the entities and if we want the "top K" entities, they'll be returned
      ArrayList<Parameters> entList = countEntities(numEntities, doc);
      docp.put("entities", entList);

      // default annotations
      docp.put("name", doc.name);
      docp.put("rank", doc.rank);
      docp.put("score", doc.score);

      // metadata annotation
      if (metadata) {
        docp.put("meta", Parameters.parseMap(doc.metadata));
      }

      // get any notes
      // TODO : pass a flag indicating if we want to get notes
      Parameters tmpNotes = system.userdb.getNotesForResource(doc.name, corpusID);
      Parameters notes = Parameters.create();
      notes.put("notes", tmpNotes);
      docp.copyFrom(notes);

      // return what (if any) queries were used to return this document when it was added to a sub-corpus
      Parameters q = system.userdb.getQueriesForResource(doc.name, corpusID);
      Parameters queries = Parameters.create();
      queries.put("queries", q);
      docp.copyFrom(queries);

      resultData.add(docp);

    }// loop through results

    Parameters ret = Parameters.create();

    // sort by frequency and get the top K

    ret.put("totalTF", totalTF.getTopK(10));
    ret.put("snippetTF", snippetTF.getTopK(10));
    ret.put("bigrams", totalBiGramTF.getTopK(10));
    ret.put("trigrams", totalTriGramTF.getTopK(10));
    ret.put("results", resultData);

    // loop through each entity type
    for (String entType : allEntities.keySet()) {

      Parameters p = Parameters.create();
      p.set(entType + "Entities", allEntities.get(entType).getTopK(10));
      ret.copyFrom(p);
    }

    ret.put("fields", noteFields);

    return ret;
  }

  private void countTerms(ProteusDocument doc) {

    // if we don't have any terms or we're not searching within a subcorport, just return
    if (doc.terms == null || !needTermFrequencies) {
      return;
    }

    int size = doc.terms.size();
    // for n-grams, we only count them if they are ALL non-stop words.
    // This keeps count of how many non-stop words we have in a row.
    int consecutiveTerms = 0;
    for (int termIdx = 0; termIdx < size; termIdx++) {
      String term = doc.terms.get(termIdx);

      // for unigrams, do the "stop word" check up front. You may argue that
      // it's silly to do it here for EVERY token, but we're actually saving
      // lookups. We do one here as opposed to one to check if we
      // add/increment the total TF, one for snippet TF, and two more
      // later on to filter the stop words once they're all counted up.
      // We also skip words that are three characters or less, they are not
      // very interesting.
      if (term.length() > 3 && exclusionTerms.contains(term) == false) {
        consecutiveTerms++;
        totalTF.addTerm(term);

        // TODO should pass in (or get from doc) snippet begin/end
        if (termIdx >= doc.passageBegin && termIdx < doc.passageEnd) {
          snippetTF.addTerm(term);
        }

        if (consecutiveTerms >= 2) {
          totalBiGramTF.addTerm(doc.terms.get(termIdx - 1) + " " + term);
          if (consecutiveTerms > 2) {
            totalTriGramTF.addTerm(doc.terms.get(termIdx - 2) + " " + doc.terms.get(termIdx - 1) + " " + term);
          }
        }
      } else {
        // hit a stop word, reset our counter
        consecutiveTerms = 0;
      }

    } // end loop through terms

  }

  private boolean isNote(ProteusDocument doc) {
    return doc.metadata.containsKey("docType") && doc.metadata.get("docType").equals("note");
  }

  private String findSnippetPage(ProteusDocument doc) {
    // If this is a book, find the page within the book that the
    // snippet is on via the start offset of the snippet. There is no
    // harm (aside from some wasted computing cycles) calling this on non-books, it'll just return an empty string.

    // for now, we'll just do a brain dead search for the page that contains the snippet.

    // for books, snippets can cross pages so we'll use the page that contains the largest
    // part of the snippet.
    String pg = "";

    // skip if this document is a page
    if (doc.metadata.containsKey("pageNumber")) {
      return pg;
    }
    // page breaks are <div> tags
    for (Tag t : doc.tags) {
      if (t.name.equals("div")) {

        if (doc.passageBegin <= t.end) {
          Integer termsOnPage = t.end - doc.passageBegin;
          Integer termsOnNextPage = doc.passageEnd - t.end;
          if (termsOnNextPage > termsOnPage) {
            continue; // use the next page
          }
          pg = t.attributes.get("page");
          break;
        }
      }
    }
    return pg;
  }

  // count the entities for this document and include them in the unigram TF counts.
  private ArrayList<Parameters> countEntities(int numEntities, ProteusDocument doc) {

    if (doc.tags == null) {
      return new ArrayList<Parameters>(); // empty list
    }
    // keep a count of each entity <entity type <name, count>>

    Map<String, TermFrequencies> docEntities = new HashMap<>();

    for (Tag tag : doc.tags) {

      // don't do anything with "div" entity types
      if (tag.name.equals("div")){
        continue;
      }

      String name = String.join(" ", ListUtil.slice(doc.terms, tag.begin, tag.end));

      // sometimes google gets tagged as an entity - so ignore it
      if (name.toString().equalsIgnoreCase("google")) {
        continue;
      }
      // document specific entities, note that tag.name is
      // the entity TYPE (person, location, etc.) not the
      // entity's name.
      if (!docEntities.containsKey(tag.name)) {
        docEntities.put(tag.name, new TermFrequencies());
      }
      docEntities.get(tag.name).addTerm(name);

      // we only need to count entities for all results and term/snippet
      // frequencies when searching within a subcorpus
      if (!needTermFrequencies){
        continue;
      }
      // add to the "all entities" list
      if (!allEntities.containsKey(tag.name)) {
        allEntities.put(tag.name, new TermFrequencies());
      }

      allEntities.get(tag.name).addTerm(name);

      // add this entity to the global TF counts
      String ent = tag.name + ":\"" + name.toString() + "\"";
      totalTF.addTerm(ent);
      // add to the snippet TF (if appropriate)
      if (snippets && tag.begin >= doc.passageBegin && tag.end < doc.passageEnd) {
        snippetTF.addTerm(ent);
      }

    } // end loop through tags

    ArrayList<Parameters> entList = new ArrayList<>();

    // if they didn't ask for any entities, we can skip the sorting.
    // we do it here, as opposed to at the start of this function so
    // we can add entities to the unigram counts.
    if (numEntities == 0) {
      return entList;
    }

    // loop through each entity type
    for (String entType : docEntities.keySet()) {
      Parameters p = Parameters.create();
      p.set(entType, docEntities.get(entType).getTopK(numEntities));
      entList.add(p);
    }

    return entList;

  } // end countEntities()

}
