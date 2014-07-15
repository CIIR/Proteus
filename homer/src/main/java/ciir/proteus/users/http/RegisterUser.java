package ciir.proteus.users.http;

import ciir.proteus.server.HTTPError;
import ciir.proteus.system.ProteusSystem;
import ciir.proteus.users.Users;
import ciir.proteus.users.error.DuplicateUser;
import ciir.proteus.users.error.NoTuplesAffected;
import org.lemurproject.galago.utility.Parameters;

/**
 * @author jfoley.
 */
public class RegisterUser extends DBAction {

    public RegisterUser(ProteusSystem proteus) {
        super(proteus);
    }

    @Override
    public Parameters handle(String method, String path, Parameters reqp) throws HTTPError, DuplicateUser {
        String username = reqp.getString("user");
        if (username.length() > Users.UserEmailMaxLength) {
            throw new HTTPError(HTTPError.BadRequest, "User name too long.");
        }

        try {
            userdb.register(username);
            log.info("RegisterUser user=" + username);
        } catch (NoTuplesAffected ex) {
            throw new HTTPError(ex);
        }

        return Parameters.instance();
    }
}
