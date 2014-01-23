/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lemurproject.galago.core.scoring;

import gnu.trove.map.hash.TObjectIntHashMap;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.parse.Tag;
import org.lemurproject.galago.tupleflow.Utility;

/**
 *
 * @author irmarc
 */
public class FieldLanguageModel {

  int docCount;
  int termCount;
  TObjectIntHashMap<String> termFrequencies;
  TObjectIntHashMap<String> termDFs;
  TObjectIntHashMap<String> fieldDFs;
  TObjectIntHashMap<String> fieldLengths;
  public static double smoothing = 0.000000001;
  HashMap<String, TObjectIntHashMap<String>> termInFieldFrequencies;
  int gramsize;

  public FieldLanguageModel() {
    this(1);
  }

  public FieldLanguageModel(int gs) {
    gramsize = gs;
    docCount = termCount = 0;
    termFrequencies = new TObjectIntHashMap<String>();
    fieldLengths = new TObjectIntHashMap<String>();
    termDFs = new TObjectIntHashMap<String>();
    fieldDFs = new TObjectIntHashMap<String>();
    termInFieldFrequencies = new HashMap<String, TObjectIntHashMap<String>>();
  }

  public void addDocuments(List<Document> docs) {
    for (Document d : docs) {
      addDocument(d);
    }
  }

  private List<String> getGrams(List<String> terms, Tag f) {
    //short-circuit
    if (gramsize == 1) {
      return terms.subList(f.begin, f.end);
    } else {
      ArrayList<String> grams = new ArrayList<String>();
      int i = f.begin;
      while (i <= (f.end - gramsize) + 1 && (i + gramsize <= terms.size())) {
        grams.add(Utility.join(terms.subList(i, i + gramsize).toArray(new String[0])));
        i++;
      }
      return grams;
    }
  }

  // Updates statistics using the contents of the document
  // Assumptions: terms vector is nonempty (this class does NOT do parsing)
  //              tags vector is nonempty, otherwise this reduces to a standard LM.
  public void addDocument(Document d) {
    if (d.text.length() == 0) {
      return; // nothing to do
    }
    // not cool 
    if ((d.terms == null || d.terms.size() == 0) && d.text.length() > 0) {
      throw new IllegalArgumentException("Adding unparsed document to language model is not ok.");
    }

    HashSet<String> termUniques = new HashSet<String>();
    HashSet<String> fieldUniques = new HashSet<String>();
    List<String> terms = d.terms;
    List<Tag> fields = d.tags;
    for (Tag f : fields) {
      fieldUniques.add(f.name);
      List<String> grams = getGrams(terms, f);
      for (int i = 0; i < grams.size(); i++) {
        String term = grams.get(i);
        termUniques.add(term);
        termFrequencies.adjustOrPutValue(term, 1, 1);
        if (!termInFieldFrequencies.containsKey(f.name)) {
          termInFieldFrequencies.put(f.name, new TObjectIntHashMap<String>());
        }
        termInFieldFrequencies.get(f.name).adjustOrPutValue(term, 1, 1);
      }

      int inc = terms.size();
      termCount += inc;
      fieldLengths.adjustOrPutValue(f.name, inc, inc);
    }

    // Increment termDFs
    for (String t : termUniques) {
      termDFs.adjustOrPutValue(t, 1, 1);
    }

    // And fieldDFs
    for (String f : fieldUniques) {
      fieldDFs.adjustOrPutValue(f, 1, 1);
    }
    docCount++;
  }

  public double getTermDF(String t) {
    return termDFs.containsKey(t) ? termDFs.get(t) : smoothing;
  }

  public double getFieldDF(String f) {
    return fieldDFs.containsKey(f) ? fieldDFs.get(f) : smoothing;
  }

  // P(F|T) -- Uses Bayes' rule
  public double getFieldProbGivenTerm(String f, String t) {
    double num = getTermProbGivenField(t, f) * getFieldProbability(f);
    double den = getTermProbability(t);
    return (num / den);
  }

  // P(F) -- statistics are at the term level
  public double getFieldProbability(String f) {
    if (!fieldLengths.containsKey(f)) {
      return smoothing;
    } else {
      return (fieldLengths.get(f) + 0.0) / termCount;
    }
  }

  // P(T|F)
  public double getTermProbGivenField(String t, String f) {
    TObjectIntHashMap<String> fieldMap = termInFieldFrequencies.get(f);
    if (fieldMap == null) {
      return smoothing;
      //throw new IllegalArgumentException(String.format("Field %s not found.", f));
    } else {
      if (!fieldMap.containsKey(t)) {
        return smoothing;
      } else {
        double num = fieldMap.get(t) + 0.0;
        return num / fieldLengths.get(f);
      }
    }
  }

  // P(T)
  public double getTermProbability(String t) {
    int num = termFrequencies.containsKey(t) ? termFrequencies.get(t) : 0;
    if (num == 0) {
      return smoothing;
    }
    double den = (termCount > 0) ? (termCount + 0.0) : Double.MIN_VALUE;
    return (num + 0.0) / den;
  }

  // |D|
  public int getDocCount() {
    return docCount;
  }

  // |D_f|
  public int getDocCount(String field) {
    if (!fieldDFs.containsKey(field)) {
      return 0;
    } else {
      return fieldDFs.get(field);
    }
  }

  // |C|
  public int getTermCount() {
    return termCount;
  }

  // |C_f|
  public int getTermCount(String field) {
    if (!fieldLengths.containsKey(field)) {
      return 0;
    } else {
      return fieldLengths.get(field);
    }
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("doccount :").append(this.docCount).append("\n");
    sb.append("termcount:").append(this.termCount).append("\n");
    sb.append("tfs:\n");
    String[] terms = termFrequencies.keys(new String[0]);
    Arrays.sort(terms);
    for (String term : terms) {
      sb.append(" ").append(term).append(":").append(termFrequencies.get(term)).append("\n");
    }
    String[] fields = fieldLengths.keys(new String[0]);
    Arrays.sort(fields);
    sb.append("field lengths:\n");
    for (String term : terms) {
      sb.append(" ").append(term).append(":").append(termFrequencies.get(term)).append("\n");
    }
    return sb.toString();
  }
}
