package ciir.proteus.util.logging;

import com.cedarsoftware.util.io.JsonWriter;

/**
 * Created by michaelz on 7/8/2015.
 */
public class TagLogData extends LogData {

  private String resource;
  private String tag;
  private Integer subcorpusid;

  public TagLogData(String id, String user, String action) {
    super(id, user, action);
  }

  public void setResource(String resource) {
    this.resource = resource;
  }

  public void setTag(String tag) {
    this.tag = tag;
  }

  public void setSubcorpusID(Integer id){ this.subcorpusid = id;}

  @Override
  public String toTSV() {

    return getCommonTSV() + "\t"
            + resource + "\t"
            + tag + "\t"
            + subcorpusid;
  }

}
