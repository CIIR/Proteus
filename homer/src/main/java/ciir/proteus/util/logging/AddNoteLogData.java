package ciir.proteus.util.logging;

import com.cedarsoftware.util.io.JsonReader;

/**
 * Created by michaelz on 7/8/2015.
 */
public class AddNoteLogData extends LogData {

  private String resource;
  private Integer corpus;
  private Object data;
  private String corpusName;
  private Integer notePK;  // PK for this note in our database

  public AddNoteLogData(String id, String user) {
    super(id, user, "ADD-NOTE");
  }

  AddNoteLogData(String id, String user, String action) {
    super(id, user, action);
  }

  public void setResource(String resource) {
    this.resource = resource;
  }

  public void setCorpus(Integer corpus) {
    this.corpus = corpus;
  }

  public void setCorpusName(String corpusName) {
    this.corpusName = corpusName;
  }

  public void setData(String data) {
    Object obj = null;
    // we have to treat "data" special as it's a JSON object. If we just stored it
    // as a String any client side calls to JSON.parse() would fail.
    if (data != null) {
      obj = JsonReader.jsonToJava(data);
    }
    this.data = obj;
  }

  public void setId(Integer notePK) {
    this.notePK = notePK;
  }

  @Override
  public String toTSV() {

    return getCommonTSV() + "\t"
            + notePK + "\t"
            + corpus + "\t"
            + corpusName + "\t"
            + resource + "\t"
            + data;
  }


}
