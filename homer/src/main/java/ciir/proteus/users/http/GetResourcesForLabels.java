package ciir.proteus.users.http;

import ciir.proteus.server.HTTPError;
import ciir.proteus.system.ProteusSystem;
import ciir.proteus.users.Credentials;
import ciir.proteus.users.error.DBError;
import static ciir.proteus.users.http.DBAction.log;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.lemurproject.galago.tupleflow.Parameters;

/**
 *
 * @author michaelz
 */
public class GetResourcesForLabels extends DBAction {

    public GetResourcesForLabels(ProteusSystem proteus) {
        super(proteus);
    }

    @Override
    public Parameters handle(String method, String path, Parameters reqp) throws HTTPError {
        Credentials creds = Credentials.fromJSON(reqp);
        List<String> labels = reqp.getAsList("labels", String.class);

        log.info("GetResourcesForLabels creds=" + creds + " labels=" + labels);

        Parameters resources = new Parameters();
        try {

            List<String> results = userdb.getResourcesForLabels(creds.user, labels);

            if (results.size() == 0) {
                resources.put("resources", new ArrayList<String>()); // return an empty list
            } else {
                resources.put("resources", results);
            }

        } catch (DBError dbError) {
            throw new HTTPError(dbError);
        }

        return resources;
    }
}
