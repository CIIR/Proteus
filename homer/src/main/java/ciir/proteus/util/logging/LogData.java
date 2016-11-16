package ciir.proteus.util.logging;

import com.cedarsoftware.util.io.JsonWriter;

/**
 * Created by michaelz on 7/8/2015.
 */
abstract class LogData {

  private final String id; // token or IP
  private final String user;
  private final String action;

  LogData(String id, String user, String action) {
    this.id = id;
    this.user = user;
    this.action = action;
  }

  private String getId() {
    return id;
  }

  private String getUser() {
    return user;
  }

  String getAction() { return action;}

  String getCommonTSV() {
    return getAction() + "\t"
            + getId() + "\t"
            + getUser();
  }

  // output tab separated info
  public abstract String toTSV();

  public String toJSON() {
    return JsonWriter.objectToJson(this);
  }

}
