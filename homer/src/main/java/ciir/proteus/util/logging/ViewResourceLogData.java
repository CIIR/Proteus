package ciir.proteus.util.logging;

/**
 * Created by michaelz on 7/8/2015.
 */
public class ViewResourceLogData extends LogData {

  private String docIDs; // search results
  private String kind; // what "kind" we're searching

  public void setDocIDs(String docIDs) {
    this.docIDs = docIDs;
  }

  public void setKind(String kind) {
    this.kind = kind;
  }

  public ViewResourceLogData(String id, String user) {
    super(id, user, "VIEW-RES");
  }


  @Override
  public String toTSV() {

    return getCommonTSV() + "\t"
            + docIDs + "\t"
            + kind;
  }

}
