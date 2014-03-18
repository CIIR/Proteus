package ciir.proteus.users.http;

import ciir.proteus.server.HTTPError;
import ciir.proteus.server.action.JSONHandler;
import ciir.proteus.system.ProteusSystem;
import ciir.proteus.users.UserDatabase;
import ciir.proteus.users.Users;
import ciir.proteus.users.error.NoTuplesAffected;
import org.lemurproject.galago.tupleflow.Parameters;

/**
 * @author jfoley.
 */
public class RegisterUser extends DBAction {

  public RegisterUser(ProteusSystem proteus) {
    super(proteus);
  }

  @Override
  public Parameters handle(String method, String path, Parameters reqp) throws HTTPError {
    String username = reqp.getString("username");
    if(username.length() > Users.UserMaxLength) {
      throw new HTTPError(HTTPError.BadRequest, "User name too long.");
    }

    try {
      userdb.register(username);
    } catch (NoTuplesAffected ex) {
      throw new HTTPError(ex);
    }

    return new Parameters();
  }
}
