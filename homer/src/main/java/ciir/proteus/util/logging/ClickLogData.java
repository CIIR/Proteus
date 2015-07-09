package ciir.proteus.util.logging;

/**
 * Created by michaelz on 7/8/2015.
 */
public class ClickLogData extends LogData {

  private String url; // url of the resutl clicked on
  private Integer rank; // rank of the result clicked

  public ClickLogData(String id, String user) {
    super(id, user);
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public void setRank(Integer rank) {
    this.rank = rank;
  }

  @Override
  String getAction() {
    return "CLICK";
  }

  @Override
  public String toTSV() {

      return getCommon() + "\t"
            + rank + "\t"
            + url;
  }

  @Override
  public String toHTML() {
    return null;
  }
}
