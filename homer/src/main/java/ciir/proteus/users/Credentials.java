package ciir.proteus.users;

import org.lemurproject.galago.tupleflow.Parameters;

/**
 * @author jfoley.
 */
public class Credentials {
  public String user;
  public String token;

  public Credentials(String user, String token) {
    this.user = user;
    this.token = token;
  }

  public static Credentials login(String user, UserDatabase db) {
    return new Credentials(user, db.login(user));
  }

  public static Credentials fromJSON(Parameters p) {
    return new Credentials(p.getAsString("user"), p.getAsString("token"));
  }

  @Override
  public String toString() {
    return toJSON().toString();
  }

  public Parameters toJSON() {
    Parameters p = new Parameters();
    p.put("user", user);
    p.put("token", token);
    return p;
  }
}
