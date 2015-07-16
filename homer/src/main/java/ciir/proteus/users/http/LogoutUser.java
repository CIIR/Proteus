package ciir.proteus.users.http;

import ciir.proteus.server.HTTPError;
import ciir.proteus.system.ProteusSystem;
import ciir.proteus.users.Credentials;
import ciir.proteus.users.error.NoTuplesAffected;
import javax.servlet.http.HttpServletRequest;

import ciir.proteus.util.logging.LogHelper;
import ciir.proteus.util.logging.LoginLogData;
import ciir.proteus.util.logging.LogoutLogData;
import org.lemurproject.galago.utility.Parameters;

/**
 * @author jfoley.
 */
public class LogoutUser extends DBAction {

    public LogoutUser(ProteusSystem proteus) {
        super(proteus);
    }

    @Override
    public Parameters handle(String method, String path, Parameters reqp, HttpServletRequest req) throws HTTPError {
        Credentials creds = Credentials.fromJSON(reqp);

        try {
            userdb.logout(creds);
            LogoutLogData logData = new LogoutLogData(reqp.get("token").toString(), reqp.getString("user"));
            LogHelper.log(logData, system);
         } catch (NoTuplesAffected noTuplesAffected) {
            throw new HTTPError(HTTPError.BadRequest, "No such user/session.");
        }

        return Parameters.create();
    }
}
