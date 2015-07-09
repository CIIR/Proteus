package ciir.proteus.util.logging;

/**
 * Created by michaelz on 7/8/2015.
 */
public abstract class LogData {

  private String id; // token or IP
  private String user;

  public LogData(String id, String user){
    this.id = id;
    this.user = user;
  };

  protected String getId() { return id; }
  protected String getUser() { return user; }

  // TODO ? should this take care of basic common stuff?
  abstract String getAction();

  protected String getCommon(){
    return getAction() + "\t"
            + getId() + "\t"
            + getUser();
  }
  // output tab separated info
  public abstract String toTSV();

  // output HTML - this should be the human readable version
  public abstract String toHTML();



}
