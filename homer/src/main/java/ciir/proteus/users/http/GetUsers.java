package ciir.proteus.users.http;

import ciir.proteus.server.HTTPError;
import ciir.proteus.system.ProteusSystem;
import ciir.proteus.users.error.DBError;
import org.lemurproject.galago.utility.Parameters;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Created by michaelz on 2/19/2015.
 */

public class GetUsers extends DBAction {

    public GetUsers(ProteusSystem proteus) {
        super(proteus);
    }

    // return user name and their user ID
    @Override
    public Parameters handle(String method, String path, Parameters reqp, HttpServletRequest req) throws HTTPError {

        Parameters users = Parameters.create();

        try {
            Map<String, String> allUsers = userdb.getUsers();
            Parameters tmp = Parameters.create();
            tmp.putAll(allUsers);
            users.put("users", tmp);
        } catch (DBError dbError) {
            throw new HTTPError(dbError);
        }

        return users;
    }
}
