package ciir.proteus.users.http;

import ciir.proteus.server.HTTPError;
import ciir.proteus.system.ProteusSystem;
import org.lemurproject.galago.tupleflow.Parameters;

/**
 * @author jfoley.
 */
public class LoginUser extends DBAction {
  public LoginUser(ProteusSystem proteus) {
    super(proteus);
  }

  @Override
  public Parameters handle(String method, String path, Parameters reqp) throws HTTPError {
    String user = reqp.getString("user");

    String token = userdb.login(user);

    if(token == null) throw new HTTPError(HTTPError.BadRequest, "No such user!");

    Parameters creds = new Parameters();
    creds.set("token", token);

    return creds;
  }
}
