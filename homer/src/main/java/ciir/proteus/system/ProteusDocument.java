// BSD License (http://lemurproject.org/galago-license)
package ciir.proteus.system;

import org.lemurproject.galago.core.parse.Tag;
import org.lemurproject.galago.utility.Parameters;

import java.util.Collections;
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
  public String snippet;
  public String snippetPage;

  public ProteusDocument() {
    metadata = new HashMap<>();
  }

  public ProteusDocument(Parameters p) {
    this.identifier = p.getLong("identifier");
    this.name = p.getString("name");
    this.metadata = (Map<String, String>) p.get("metadata");
    if (this.metadata == null){
      this.metadata = Collections.emptyMap();
    }
    this.text = p.getString("text");
    this.terms = p.getAsList("terms");
    this.tags = p.getAsList("tags");
    this.snippet = (p.containsKey("snippet") ? p.getAsString("snippet") : null);
    this.snippetPage = (p.containsKey("snippetPage") ? p.getAsString("snippetPage") :  "");

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

}

