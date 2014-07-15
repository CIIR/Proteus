package ciir.proteus.users.http;

import ciir.proteus.server.HTTPError;
import ciir.proteus.system.ProteusSystem;
import org.lemurproject.galago.utility.Parameters;

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

        Parameters loginCreds = userdb.login(user);
        if (loginCreds == null || loginCreds.get("token") == null) {
            log.info("LoginUser FAIL user=" + user);
            throw new HTTPError(HTTPError.BadRequest, "No such user!");
        }

        log.info("LoginUser SUCCESS user=" + user + " token=" + loginCreds.get("token").toString() + " ID=" + loginCreds.get("userid").toString());

        return loginCreds;
    }
}
