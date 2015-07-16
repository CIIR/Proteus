package ciir.proteus.util.logging;

import com.cedarsoftware.util.io.JsonWriter;

/**
 * Created by michaelz on 7/8/2015.
 */
public class LoginLogData extends LogData {

  private Integer userid;
  private String ip;

  public LoginLogData(String id, String user) {
    super(id, user, "LOGIN");
  }

  public void setUserid(Integer userid) {
    this.userid = userid;
  }

  public void setIp(String ip) {
    this.ip = ip;
  }

  @Override
  public String toTSV() {

    return getCommonTSV() + "\t"
            + ip + "\t"
            + userid;
  }

}
