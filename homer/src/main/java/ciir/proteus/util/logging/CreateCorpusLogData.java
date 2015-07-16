package ciir.proteus.util.logging;

/**
 * Created by michaelz on 7/8/2015.
 */
public class CreateCorpusLogData extends LogData {

  private String corpusName;

  private Integer corpusID;

  public CreateCorpusLogData(String id, String user) {
    super(id, user, "CREATE-CORPUS");
  }

  public void setCorpusName(String corpusName) {
    this.corpusName = corpusName;
  }

  public void setCorpusID(Integer corpusID) {
    this.corpusID = corpusID;
  }

  @Override
  public String toTSV() {

    return getCommonTSV() + "\t"
            + corpusName + "\t"
            + corpusID;
  }

}
