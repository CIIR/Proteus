package ciir.proteus.util.logging;

import com.cedarsoftware.util.io.JsonWriter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by michaelz on 7/8/2015.
 */
public abstract class LogData {

  private String timestamp;
  private String id; // token or IP
  private String user;
  final protected String action;

  public LogData(String id, String user, String action) {
    this.timestamp  = getTimestamp();
    this.id = id;
    this.user = user;
    this.action = action;
  }

  protected String getId() {
    return id;
  }

  protected String getUser() {
    return user;
  }

  protected String getAction() { return action;};

  protected String getCommonTSV() {
    return getAction() + "\t"
            + getId() + "\t"
            + getUser();
  }

  private String getTimestamp(){
    Date date = new Date();
    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy H:mm:ss");
    return sdf.format(date);
  }

  // output tab separated info
  public abstract String toTSV();

  public String toJSON() {
    return JsonWriter.objectToJson(this);
  }

}
