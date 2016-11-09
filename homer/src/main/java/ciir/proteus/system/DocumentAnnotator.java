package ciir.proteus.system;

import ciir.proteus.users.error.DBError;
import ciir.proteus.util.ListUtil;
import ciir.proteus.util.RetrievalUtil;
import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.parse.Tag;
import org.lemurproject.galago.core.parse.TagTokenizer;
import org.lemurproject.galago.core.parse.stem.KrovetzStemmer;
import org.lemurproject.galago.core.parse.stem.Stemmer;
import org.lemurproject.galago.core.retrieval.ScoredDocument;
import org.lemurproject.galago.core.retrieval.ScoredPassage;
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.core.util.WordLists;
import org.lemurproject.galago.tupleflow.Utility;
import org.lemurproject.galago.utility.Parameters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author jfoley, michaelz
 */
public class DocumentAnnotator {

  private static Set<String> exclusionTerms;

  private Map<String, Integer> totalTF = null;
  private Map<String, Integer> snippetTF = null;

  private Map<String, Map<String, Integer>> allEntities = null;

  private int snippetBegin = 0;
  private int snippetEnd = 100;
  private boolean snippets = true;

  public Parameters annotate(ProteusSystem system, String kind, List<String> names, Parameters reqp) throws DBError, IOException {
    reqp.put("metadata", false);
    List<ScoredDocument> fakeDocs = new ArrayList<>();
    for (String id : names) {
      fakeDocs.add(new ScoredDocument(id, 0, 0.0));
    }
    return annotate(system, kind, fakeDocs, null, reqp);
  }

  // note that the query could be null if we want to get all documents for a label or corpus.
  public Parameters annotate(ProteusSystem system, String kind, List<ScoredDocument> results, Node query, Parameters reqp) throws DBError, IOException {

    if (exclusionTerms == null) {
      exclusionTerms = WordLists.getWordList("rmstop");
    }
    snippets = reqp.get("snippets", true);
    boolean metadata = reqp.get("metadata", true);
    boolean getTags = reqp.get("tags", false);
    boolean getRatings = reqp.get("ratings", false);
    boolean overlapOnly = reqp.get("overlapOnly", false);
    int numEntities = (int) reqp.get("top_k_entities", 0);
    int corpusID = (int) reqp.get("corpus", -1);

    allEntities = new HashMap<String, Map<String, Integer>>();
    List<String> names = RetrievalUtil.names(results);
    Stemmer stemmer = new KrovetzStemmer();
    // retrieve snippets if requested AND we have a query
    if (snippets && query != null) {
      results = system.findPassages(kind, query, names);
    }

    // if we need to pull the documents:
    Map<String, Document> pulled = Collections.emptyMap();
    if (snippets || metadata) {
      pulled = system.getDocs(kind, names, metadata, snippets);
    }

    totalTF = new HashMap<String, Integer>();
    snippetTF = new HashMap<String, Integer>();

    Map<String, Integer> totalBiGramTF = new HashMap<String, Integer>();
    Map<String, Integer> totalTriGramTF = new HashMap<String, Integer>();

    Parameters noteParams = system.getConfig().get("notes", Parameters.create());
    List<String> noteFields = noteParams.getAsList("noteFields", String.class);

    TagTokenizer tagTokenizer = new TagTokenizer();
    for (String field : noteFields) {
      tagTokenizer.addField(field);
    }
    tagTokenizer.addField("div");


    // result data
    ArrayList<Parameters> resultData = new ArrayList<>(results.size());

    for (ScoredDocument sdoc : results) {

      Document doc = pulled.get(sdoc.documentName);

      if (doc == null) {
        continue;
      }

      // modified getResourcRatings to get labels.
      Parameters labels = system.userdb.getResourceRatings2(doc.name, corpusID);
      // "overlap" is a document that is in more than one subcorpus
      if (overlapOnly && labels.get("newLabels", Parameters.create()).size() < 2) {
        continue;
      }

      Parameters docp = Parameters.create();
      docp.copyFrom(labels);

      List<String> snippetTerms = new ArrayList<String>();

      // if this is a note, use the whole text
      if (doc.metadata.containsKey("docType") && doc.metadata.get("docType").equals("note")) {
        // ??? we don't count these in the sniippetTF
        docp.put("text", doc.text);
        // for notes, use the whole thing as a snippet
        docp.put("snippet", doc.text);
        tagTokenizer.tokenize(doc);
        // remove the 1st token - that's the person who created the comment and we don't want
        // to count that in the TF
        doc.terms.set(0, "a");
        snippetBegin = 0;
        snippetEnd = doc.text.length();

      } else if (snippets) {
        // if the query was null, we'll just get the first part of
        // the document.
        snippetBegin = 0;
        snippetEnd = 100;
        if (query != null) {
          ScoredPassage psg = (ScoredPassage) sdoc;
          snippetBegin = psg.begin;
          snippetEnd = psg.end;
        }

        snippetTerms = ListUtil.slice(doc.terms, snippetBegin, snippetEnd);
        String snippet = (Utility.join(snippetTerms, " "));

        // If this is a book, find the page within the book that the
        // snippet is on via the start offset of the snippet. There is no
        // harm calling this on non-books, it'll just return an empty string.
        // TODO - should do a binary search for this
        /// for now, we'll just do a brain dead search for the page that contains the snippet.

        // for books, snippets can cross pages so we'll use the page that contains the largest
        // part of the snippet.
        String pg = "";
        // page breaks are <div> tags
        for (Tag t : doc.tags) {
          if (t.name.equals("div")) {

            if (snippetBegin <= t.end) {
              Integer termsOnPage = t.end - snippetBegin;
              Integer termsOnNextPage = snippetEnd - t.end;
              if (termsOnNextPage > termsOnPage) {
                continue; // use the next page
              }
              pg = t.attributes.get("page");
              break;
            }

          }
        }

        docp.put("snippetPage", pg);
        docp.put("snippet", snippet);

      } // end if snippet

      // count terms frequencies
      if (doc.terms != null) {
        int termIdx = 0;

        for (String term : doc.terms) {
          term = stemmer.stem(term);
          if (totalTF.containsKey(term)) {
            totalTF.put(term, totalTF.get(term) + 1);
          } else {
            totalTF.put(term, 1);
          }

          if (termIdx >= snippetBegin && termIdx < snippetEnd) {
            if (snippetTF.containsKey(term)) {
              snippetTF.put(term, snippetTF.get(term) + 1);
            } else {
              snippetTF.put(term, 1);
            }
          }
          termIdx++;

          if (termIdx >= 2) {
            String bi = doc.terms.get(termIdx - 2) + " " + doc.terms.get(termIdx - 1);
            if (totalBiGramTF.containsKey(bi)) {
              totalBiGramTF.put(bi, totalBiGramTF.get(bi) + 1);
            } else {
              totalBiGramTF.put(bi, 1);
            }
            if (termIdx >= 3) {
              String tri = doc.terms.get(termIdx - 3) + " " + bi;
              if (totalTriGramTF.containsKey(tri)) {
                totalTriGramTF.put(tri, totalTriGramTF.get(tri) + 1);
              } else {
                totalTriGramTF.put(tri, 1);
              }
            }
          }

        } // end loop through terms
      }

      // count the entities and if we want the "top K" entities, they'll be returned
      ArrayList<Parameters> entList = processEntities(numEntities, doc);
      docp.put("entities", entList);

      // default annotations
      docp.put("name", sdoc.documentName);
      docp.put("rank", sdoc.rank);
      docp.put("score", sdoc.score);

      // metadata annotation
      if (metadata) {
        docp.put("meta", Parameters.parseMap(doc.metadata));
      }

      // get any rankings of the document
      if (getRatings) {
        Parameters ratings = Parameters.create();
        ratings = system.userdb.getResourceRatings(doc.name, corpusID);
        docp.copyFrom(ratings);
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
    List<Map.Entry<String, Integer>> topBiGrams = totalBiGramTF.entrySet().stream()
            .filter(entry -> {
              // none of the words can be stop words
              String[] t = entry.getKey().split(" ");
              return (exclusionTerms.contains(t[0]) == false) && (exclusionTerms.contains(t[1]) == false);
            })
            .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
            .limit(10)
            .collect(Collectors.toList());

    Double bigramSum = topBiGrams.stream().mapToDouble(entry -> entry.getValue()).sum();

    ArrayList<Parameters> tmp3 = new ArrayList<>();
    topBiGrams.forEach((term -> {
      Parameters p = Parameters.create();
      p.put("ngram", term.getKey());
      p.put("count", term.getValue());
      p.put("weight", term.getValue().floatValue() / bigramSum);
      tmp3.add(p);
    }));
    ret.put("bigrams", tmp3);

    List<Map.Entry<String, Integer>> topTriGrams = totalTriGramTF.entrySet().stream()
            .filter(entry -> {
              // none of the words can be stop words
              String[] t = entry.getKey().split(" ");
              return (exclusionTerms.contains(t[0]) == false) && (exclusionTerms.contains(t[1]) == false) && (exclusionTerms.contains(t[2]) == false);
            })
            .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
            .limit(10)
            .collect(Collectors.toList());

    Double trigramSum = topTriGrams.stream().mapToDouble(entry -> entry.getValue()).sum();

    ArrayList<Parameters> tmp4 = new ArrayList<>();
    topTriGrams.forEach((term -> {
      Parameters p = Parameters.create();
      p.put("ngram", term.getKey());
      p.put("count", term.getValue());
      p.put("weight", term.getValue().floatValue() / trigramSum);
      tmp4.add(p);
    }));
    ret.put("trigrams", tmp4);

    List<Map.Entry<String, Integer>> topTen = totalTF.entrySet().stream()
            .filter(entry -> entry.getKey().length() > 3) // words 3 characters or less are not very interesting
            .filter(entry -> exclusionTerms.contains(entry.getKey()) == false) // remove stop words
            .filter(entry -> entry.getKey().equals("digitized") == false) // some books have "digitized by google" at the bottom of each page
            .filter(entry -> entry.getKey().contains("google") == false) // sometimes google is marked as an entity so use "contains"
            .filter(entry -> entry.getKey().startsWith("archiveid") == false) // skip any archive id fields
            .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
            .limit(10)
            .collect(Collectors.toList());
    // TODO - we could put "digitized" in the entityTerms to exclude, may be faster.
    Double sum = topTen.stream().mapToDouble(entry -> entry.getValue()).sum();
    List<Parameters> tmp = new ArrayList<Parameters>();
    ret.put("totalTF", tmp);

    final Double finalSum3 = sum;
    topTen.forEach((term -> {
      Parameters p = Parameters.create();
      p.put("term", term.getKey());
      p.put("count", term.getValue());
      p.put("weight", term.getValue().floatValue() / finalSum3);
      tmp.add(p);
    }));

    List<Map.Entry<String, Integer>> snippettopTen = snippetTF.entrySet().stream()
            .filter(entry -> entry.getKey().length() > 3) // words 3 characters or less are not very interesting
            .filter(entry -> exclusionTerms.contains(entry.getKey()) == false) // remove stop words
            .filter(entry -> entry.getKey().equals("digitized") == false) // some books have "digitized by google" at the bottom of each page
            .filter(entry -> entry.getKey().contains("google") == false) // sometimes google is marked as an entity so use "contains"
            .filter(entry -> entry.getKey().startsWith("archiveid") == false) // skip any archive id fields
            .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
            .limit(10)
            .collect(Collectors.toList());

    sum = snippettopTen.stream().mapToDouble(entry -> entry.getValue()).sum();

    List<Parameters> snippettmp = new ArrayList<Parameters>();
    final Double finalSum1 = sum;
    snippettopTen.forEach((term -> {
      Parameters p = Parameters.create();
      p.put("term", term.getKey());
      p.put("count", term.getValue());
      p.put("weight", term.getValue().floatValue() / finalSum1);
      snippettmp.add(p);
    }));
    ret.put("snippetTF", snippettmp);

    ret.put("results", resultData);

    ArrayList<Parameters> entList = new ArrayList<>();
    // loop through each entity type

    for (String entType : allEntities.keySet()) {
      ArrayList<Parameters> parr = new ArrayList<>();
      for (Map.Entry<String, Integer> ent : allEntities.get(entType).entrySet()) {
        Parameters tp = Parameters.create();
        tp.put("entity", ent.getKey());
        tp.put("count", ent.getValue());
        parr.add(tp);
      }
      Parameters p = Parameters.create();
      p.set(entType, parr);
      entList.add(p);

      try {
        List<Map.Entry<String, Integer>> entTopTen = allEntities.get(entType).entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .limit(10)
                .collect(Collectors.toList());

        sum = entTopTen.stream().mapToDouble(entry -> entry.getValue()).sum();

        ArrayList<Parameters> tmp1 = new ArrayList<>();
        final Double finalSum2 = sum;


        entTopTen.forEach((term -> {
          final Parameters p1 = Parameters.create();
          p1.put("entity", term.getKey());
          p1.put("count", term.getValue());
          p1.put("weight", term.getValue().floatValue() / finalSum2);
          tmp1.add(p1);
        }));
        ret.put(entType + "Entities", tmp1);
      } catch (Exception e) {
        // TODO ignnore for now

        ret.put(entType + "Entities", new ArrayList<Parameters>());
      }
    }

    ret.put("fields", noteFields);

    return ret;
  }

  private ArrayList<Parameters> processEntities(int numEntities, Document doc) {

    if (doc.tags == null) {
      return new ArrayList<Parameters>(); // empty list
    }
    // keep a count of each entity <entity type <name, count>>
    Map<String, Map<String, Integer>> docEntities = new HashMap<String, Map<String, Integer>>();

    for (Tag tag : doc.tags) {

      StringBuilder name = new StringBuilder();
      for (int i = tag.begin; i < tag.end; i++) {
        if (name.length() > 0)
          name.append(" ");
        name.append(doc.terms.get(i));
      }

      if (name.toString().equalsIgnoreCase("google")) {
        continue;
      }
      // document specific entities
      if (!docEntities.containsKey(tag.name)) {
        docEntities.put(tag.name, new HashMap<String, Integer>());
      }
      Integer count = 1;
      if (docEntities.get(tag.name).containsKey(name.toString())) {
        count = docEntities.get(tag.name).get(name.toString()) + 1;
      }
      docEntities.get(tag.name).put(name.toString(), count);

      // add to the "all entities" list
      if (!allEntities.containsKey(tag.name)) {
        allEntities.put(tag.name, new HashMap<String, Integer>());
      }
      count = 1;
      if (allEntities.get(tag.name).containsKey(name.toString())) {
        count = allEntities.get(tag.name).get(name.toString()) + 1;
      }
      allEntities.get(tag.name).put(name.toString(), count);

      // add this entity to the global TF counts
      count = 1;
      String ent = tag.name + ":\"" + name.toString() + "\"";
      if (totalTF.containsKey(ent)) {
        count = totalTF.get(ent) + 1;
      }
      totalTF.put(ent, count);

      // add to the snippet TF (if appropriate)
      if (snippets && tag.begin >= snippetBegin && tag.end < snippetEnd) {
        count = 1;
        if (snippetTF.containsKey(ent)) {
          count = snippetTF.get(ent) + 1;
        }
        snippetTF.put(ent, count);
      }

    } // end loop through tags
    ArrayList<Parameters> entList = new ArrayList<>();

    // if they didn't ask for any entities, we can skip the sorting
    if (numEntities == 0) {
      return entList;
    }

    class Ent {
      String name;
      Integer count;

      Ent(String name, Integer count) {
        this.name = name;
        this.count = count;
      }
    }

    // loop through each entity type
    for (String entType : docEntities.keySet()) {

      // Make a Priority Queue so we have them sorted
      PriorityQueue<Ent> PQ = new PriorityQueue<Ent>(docEntities.get(entType).entrySet().size(),
              new Comparator<Ent>() {
                public int compare(Ent p, Ent q) {
                  return (q.count - p.count);
                }
              });

      try {

        for (Map.Entry<String, Integer> ent : docEntities.get(entType).entrySet()) {
          PQ.add(new Ent(ent.getKey(), ent.getValue()));
        }

        ArrayList<Parameters> parr = new ArrayList<>();
        int imax = Math.min(numEntities, PQ.size());

        for (int i = 0; i < imax; i++) {
          Parameters tp = Parameters.create();
          Ent x = PQ.poll();
          tp.put("entity", x.name);
          tp.put("count", x.count);
          parr.add(tp);
        }
        Parameters p = Parameters.create();
        p.set(entType, parr);
        entList.add(p);

      } catch (Exception e) {
        System.out.println(e.toString());
      }
    } // end loop through entities

    return entList;

  }

}
