// BSD License (http://lemurproject.org/galago-license)
package ciir.proteus.system;

import org.lemurproject.galago.core.parse.Tag;
import org.lemurproject.galago.utility.Parameters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// This is a "merge" of Galago's Document, ScoredDocument, and ScoredPassage classes.
// Its purpose is to limit dependencies on Galago classes to make integrating with
// other "back ends" such as Lucene easier.
public class ProteusDocument {

  public long identifier = -1;
  public String name;
  public Map<String, String> metadata;
  public String text;
  public List<String> terms;
  public List<Tag> tags;
  public int rank = -1;
  public double score = -1.0;

  // if doing passage retrieval, this will hold start/end which are indexes into the terms array.
  public int passageBegin = -1;
  public int passageEnd = -1;

  public ProteusDocument() {
    metadata = new HashMap<>();
  }

  public ProteusDocument(Parameters p) {
    this.identifier = p.getLong("identifier");
    this.name = p.getString("name");
    this.metadata = (Map<String, String>) p.get("metadata");
    this.text = p.getString("text");
    this.terms = p.getAsList("terms");
    this.tags = p.getAsList("tags");
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Identifier: ").append(name).append("\n");
    if (metadata != null) {
      sb.append("Metadata: \n");
      for (Map.Entry<String, String> entry : metadata.entrySet()) {
        sb.append("<");
        sb.append(entry.getKey()).append(",").append(entry.getValue());
        sb.append("> ");
      }
    }

    if (tags != null) {
      int count = 0;
      sb.append("\nTags: \n");
      for (Tag t : tags) {
        sb.append(count).append(" : ");
        sb.append(t.toString()).append("\n");
        count += 1;
      }
    }

    if (terms != null) {
      int count = 0;
      sb.append("\nTerm vector: \n");
      for (String s : terms) {
        sb.append(count).append(" : ");
        sb.append(s).append("\n");
        count += 1;
      }
    }

    if (text != null) {
      sb.append("\nText :").append(text);
    }
    sb.append("\n");

    return sb.toString();
  }
/*

    public TObjectIntHashMap<String> getBagOfWords() {
    TObjectIntHashMap<String> termCounts = new TObjectIntHashMap<>();
    for(String term : terms) {
      termCounts.adjustOrPutValue(term, 1, 1);
    }
    return termCounts;
  }
*/

  /**
   * This class allows the selection of parts of the document to serialize or deserialize.
   public static class DocumentComponents implements Serializable {
   private static final long serialVersionUID = -5134430303276805133L;
   public static DocumentComponents All = new DocumentComponents(true, true, true);
   public static DocumentComponents JustMetadata = new DocumentComponents(false, true, false);
   public static DocumentComponents JustText = new DocumentComponents(true, false, false);
   public static DocumentComponents JustTerms = new DocumentComponents(false, false, true);

   public boolean text = true;
   public boolean metadata = true;
   public boolean tokenize = false;
   // these variables can be used to restrict the text to just a short section at the start of the document
   // useful for massive files
   // start and end are byte offsets
   // -1 indicates no restriction
   public int subTextStart = -1;
   public int subTextLen = -1;

   // defaults
   public DocumentComponents() {
   }

   public DocumentComponents(boolean text, boolean metadata, boolean tokenize) {
   this.text = text;
   this.metadata = metadata;
   this.tokenize = tokenize;
   }

   public DocumentComponents(Parameters p) {
   this.text = p.get("text", text);
   this.metadata = p.get("metadata", metadata);
   this.tokenize = p.get("tokenize", tokenize);
   }

   public Parameters toJSON() {
   Parameters p = Parameters.create();
   p.put("text", text);
   p.put("metadata", metadata);
   p.put("tokenize", tokenize);
   return p;
   }

   @Override public String toString() {
   return toJSON().toString();
   }
   }
   */
}

