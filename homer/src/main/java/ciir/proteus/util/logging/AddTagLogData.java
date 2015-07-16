package ciir.proteus.util.logging;

import com.cedarsoftware.util.io.JsonWriter;

/**
 * Created by michaelz on 7/8/2015.
 */
public class AddTagLogData extends LogData {

  private String resource;

  private String tag;
  private String comment;
  private Integer rating;

  public AddTagLogData(String id, String user) {
    super(id, user, "ADD-TAG");
  }

  public AddTagLogData(String id, String user, String action) {
    super(id, user, action);
  }


  public void setResource(String resource) {
    this.resource = resource;
  }

  public void setTag(String tag) {
    this.tag = tag;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public void setRating(Integer rating) {
    this.rating = rating;
  }

  @Override
  public String toTSV() {

    return getCommonTSV() + "\t"
            + resource + "\t"
            + tag + "\t"
            + rating + "\t"
            + comment;
  }


}
