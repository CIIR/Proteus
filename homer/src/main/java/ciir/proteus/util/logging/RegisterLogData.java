package ciir.proteus.util.logging;

/**
 * Created by michaelz on 7/8/2015.
 */
public class RegisterLogData extends LogData {

  public RegisterLogData(String id, String user) {
    super(id, user);
  }

  @Override
  String getAction() {
    return "REGISTER";
  }

  @Override
  public String toTSV() {

      return getCommon();
  }

  @Override
  public String toHTML() {
    return null;
  }
}
