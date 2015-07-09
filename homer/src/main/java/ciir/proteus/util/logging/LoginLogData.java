package ciir.proteus.util.logging;

/**
 * Created by michaelz on 7/8/2015.
 */
public class LoginLogData extends LogData {

  private Integer userid;
  private String ip;

  public LoginLogData(String id, String user) {
    super(id, user);
  }

  public void setUserid(Integer userid) {
    this.userid = userid;
  }

  public void setIp(String ip) {
    this.ip = ip;
  }

  @Override
  String getAction() {
    return "LOGIN";
  }

  @Override
  public String toTSV() {

      return getCommon() + "\t"
            + ip + "\t"
            + userid;
  }

  @Override
  public String toHTML() {
    return null;
  }
}
