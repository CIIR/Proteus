package ciir.proteus.util.logging;

/**
 * Created by michaelz on 7/8/2015.
 */
public class ResultLogData extends LogData {

  private String docIDs; // search results

  public void setDocIDs(String docIDs) {
    this.docIDs = docIDs;
  }

  public ResultLogData(String id, String user) {
    super(id, user);
  }

  @Override
  String getAction() {
    return "RESULTS";
  }

  @Override
  public String toTSV() {

      return getCommon() + "\t"
            + docIDs;
  }

  @Override
  public String toHTML() {
    return null;
  }
}
