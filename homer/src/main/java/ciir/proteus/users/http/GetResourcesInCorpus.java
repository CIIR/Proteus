package ciir.proteus.users.http;

import ciir.proteus.server.HTTPError;
import ciir.proteus.system.ProteusSystem;
import ciir.proteus.users.Credentials;
import ciir.proteus.users.error.DBError;
import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.utility.Parameters;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 * @author michaelz
 */
public class GetResourcesInCorpus extends DBAction {

    private final ProteusSystem system;

    public GetResourcesInCorpus(ProteusSystem proteus) {
        super(proteus);
        this.system = proteus;
    }

    @Override
    public Parameters handle(String method, String path, Parameters reqp, HttpServletRequest req) throws HTTPError {

        Credentials creds = Credentials.fromJSON(reqp);
        int corpus = reqp.get("corpus", -1);

        log.info("GetResourcesInCorpus corpus=" + corpus);
        List<String> resList;
        try {
            Parameters response = userdb.getResourcesForCorpusByQuery(corpus);

            // add the metadata for each resource
            resList = userdb.getAllResourcesForCorpus(creds.userid, corpus);
            Parameters resources = Parameters.create();
            // get the metadata for each resource

            // if they don't specify an "all" kind - which points to every index -
            // we'll just return nothing. Not the best solution we could loop through
            // all the kinds or create the "all" kind if it's not provided.
            Map<String, Document> metadata = Collections.emptyMap();
            if (system.kinds.containsKey("all")){
                metadata =  this.system.getDocs("all", resList, true, false);
                for (Map.Entry<String, Document> i : metadata.entrySet()){
                    resources.put(i.getKey(), Parameters.parseMap(i.getValue().metadata));
                }
            } else {
                System.out.println("No 'all' kind was specified, no metadata is available.");
                for (String res : resList){
                    resources.put(res, Parameters.create()); // empty metadata
                }
            }

            response.set("metadata", resources);

            return response;

        } catch (DBError dbError) {
            throw new HTTPError(dbError);
        }

    }
}
