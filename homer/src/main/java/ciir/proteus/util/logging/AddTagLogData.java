package ciir.proteus.util.logging;

/**
 * Created by michaelz on 7/8/2015.
 */
public class AddTagLogData extends LogData {

  private String resource;

  private String tag;
  private String comment;
  private Integer rating;

  public AddTagLogData(String id, String user) {
    super(id, user);
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
  String getAction() {
    return "ADD-TAG";
  }

  @Override
  public String toTSV() {

      return getCommon() + "\t"
            + resource + "\t"
            + tag + "\t"
            + rating + "\t"
            + comment;
  }

  @Override
  public String toHTML() {
    return null;
  }


}
