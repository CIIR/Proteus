package ciir.proteus.server.action;

import ciir.proteus.server.HTTPError;
import ciir.proteus.system.ProteusSystem;
import ciir.proteus.users.error.DBError;
import javax.servlet.http.HttpServletRequest;
import org.lemurproject.galago.utility.Parameters;

/**
 * @author jfoley.
 */
public class GetKinds implements JSONHandler {

    private final ProteusSystem system;

    public GetKinds(ProteusSystem sys) {
        this.system = sys;
    }

    @Override
    public Parameters handle(String method, String path, Parameters reqp, HttpServletRequest req) throws HTTPError, DBError {
        Parameters p = Parameters.create();
        p.put("defaultKind", system.defaultKind);
        p.put("kinds", system.getConfig().getMap("kinds"));
        p.put("title", system.getConfig().get("title"));
        return p;
    }
}
