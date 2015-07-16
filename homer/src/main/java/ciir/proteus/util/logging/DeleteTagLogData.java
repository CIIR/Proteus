package ciir.proteus.util.logging;

/**
 * Created by michaelz on 7/8/2015.
 */
public class DeleteTagLogData extends LogData {

  private String resource;
  private String tag;

  public DeleteTagLogData(String id, String user) {
    super(id, user, "DEL-TAG");
  }


  public void setResource(String resource) {
    this.resource = resource;
  }

  public void setTag(String tag) {
    this.tag = tag;
  }


  @Override
  public String toTSV() {

    return getCommonTSV() + "\t"
            + resource + "\t"
            + tag;
  }


}
