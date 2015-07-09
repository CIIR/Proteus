package ciir.proteus.util.logging;

/**
 * Created by michaelz on 7/8/2015.
 */
public class AddNoteLogData extends LogData {

  private String resource;
  private Integer corpus;
  private String data;
  private String corpusName;
  private Integer id;  // PK for this note in our database

  public AddNoteLogData(String id, String user) {
    super(id, user);
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
    this.data = data;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  @Override
  String getAction() {
    return "ADD-NOTE";
  }

  @Override
  public String toTSV() {

      return getCommon() + "\t"
            + id + "\t"
            + corpus + "\t"
            + corpusName + "\t"
            + resource + "\t"
            + data;
  }

  @Override
  public String toHTML() {
    return null;
  }


}
