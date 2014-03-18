package ciir.proteus.users.http;

import ciir.proteus.server.HTTPError;
import ciir.proteus.system.ProteusSystem;
import ciir.proteus.users.error.NoTuplesAffected;
import org.lemurproject.galago.tupleflow.Parameters;

/**
 * @author jfoley.
 */
public class LogoutUser extends DBAction {
  public LogoutUser(ProteusSystem proteus) {
    super(proteus);
  }

  @Override
  public Parameters handle(String method, String path, Parameters reqp) throws HTTPError {
    String user = reqp.getString("user");
    String token = reqp.getString("token");

    try {
      userdb.logout(user, token);
    } catch (NoTuplesAffected noTuplesAffected) {
      throw new HTTPError(HTTPError.BadRequest, "No such user/session.");
    }

    return new Parameters();
  }
}
