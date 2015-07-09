package ciir.proteus.util.logging;

/**
 * Created by michaelz on 7/8/2015.
 */
public class CreateCorpusLogData extends LogData {

  private String corpusName;

  private Integer corpusID;

  public CreateCorpusLogData(String id, String user) {
    super(id, user);
  }

  public void setCorpusName(String corpusName) {
    this.corpusName = corpusName;
  }

  public void setCorpusID(Integer corpusID) {
    this.corpusID = corpusID;
  }

  @Override
  String getAction() {
    return "CREATE-CORPUS";
  }

  @Override
  public String toTSV() {

      return getCommon() + "\t"
            + corpusName + "\t"
            + corpusID;
  }

  @Override
  public String toHTML() {
    return null;
  }
}
