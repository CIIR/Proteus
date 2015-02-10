package ciir.proteus.users.http;

import ciir.proteus.server.HTTPError;
import ciir.proteus.system.ProteusSystem;
import ciir.proteus.users.Credentials;
import ciir.proteus.users.error.DBError;
import java.util.ArrayList;
import org.lemurproject.galago.utility.Parameters;

import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 * @author jfoley.
 */
public class GetTags extends DBAction {

    public GetTags(ProteusSystem proteus) {
        super(proteus);
    }

    @Override
    public Parameters handle(String method, String path, Parameters reqp, HttpServletRequest req) throws HTTPError {
        Credentials creds = Credentials.fromJSON(reqp);
        List<String> resources = reqp.getAsList("resource", String.class);

        log.info("GetTags creds=" + creds + " resources=" + resources);

        Parameters rtags = Parameters.create();

        for (String resource : resources) {
            try {
                Map<Integer, List<String>> tagsAndUsers;

                tagsAndUsers = userdb.getAllTags(resource);

        // currently Parameters doesn't quite handle the data structure
                // we're using so we have to trick it into using it.
                Parameters tmp = Parameters.create();
                for (Map.Entry<Integer, List<String>> entry : tagsAndUsers.entrySet()) {
                    tmp.put(entry.getKey().toString(), entry.getValue());
                }
                if (tmp.size() == 0) {
                    rtags.put(resource, new ArrayList<String>()); // return an empty list
                } else {
                    rtags.put(resource, tmp);
                }

            } catch (DBError dbError) {
                throw new HTTPError(dbError);
            }
        }

        return rtags;
    }
}
