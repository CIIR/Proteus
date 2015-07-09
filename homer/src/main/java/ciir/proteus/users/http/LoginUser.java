package ciir.proteus.users.http;

import ciir.proteus.server.HTTPError;
import ciir.proteus.system.ProteusSystem;
import javax.servlet.http.HttpServletRequest;

import ciir.proteus.util.logging.ClickLogData;
import ciir.proteus.util.logging.LogHelper;
import ciir.proteus.util.logging.LoginLogData;
import org.lemurproject.galago.utility.Parameters;

/**
 * @author jfoley.
 */
public class LoginUser extends DBAction {

    public LoginUser(ProteusSystem proteus) {
        super(proteus);
    }

    @Override
    public Parameters handle(String method, String path, Parameters reqp, HttpServletRequest req) throws HTTPError {
        String user = reqp.getString("user");

        Parameters loginCreds = userdb.login(user);
        if (loginCreds == null || loginCreds.get("token") == null) {
            log.info("LoginUser FAIL user=" + user);
            throw new HTTPError(HTTPError.BadRequest, "No such user!");
        }

        log.info("LoginUser SUCCESS user=" + user + " token=" + loginCreds.get("token").toString() + " ID=" + loginCreds.get("userid").toString());

        LoginLogData logData = new LoginLogData(loginCreds.get("token").toString(), user);
        logData.setIp(req.getRemoteAddr());
        logData.setUserid(loginCreds.getInt("userid"));
        LogHelper.log(logData);

        // return the broadcast info
        if (system.getConfig().containsKey("broadcast")) {
            loginCreds.put("broadcast", system.getConfig().getMap("broadcast"));
        }
        return loginCreds;
    }
}
