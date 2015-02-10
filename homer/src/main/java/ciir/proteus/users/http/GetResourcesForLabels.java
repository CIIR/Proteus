package ciir.proteus.users.http;

import ciir.proteus.server.HTTPError;
import ciir.proteus.system.DocumentAnnotator;
import ciir.proteus.system.ProteusSystem;
import ciir.proteus.users.Credentials;
import ciir.proteus.users.error.DBError;
import static ciir.proteus.users.http.DBAction.log;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import org.lemurproject.galago.core.retrieval.ScoredDocument;
import org.lemurproject.galago.utility.Parameters;

/**
 *
 * @author michaelz
 */
public class GetResourcesForLabels extends DBAction {

    private final ProteusSystem system;

    public GetResourcesForLabels(ProteusSystem proteus) {
        super(proteus);
        this.system = proteus;
    }

    @Override
    public Parameters handle(String method, String path, Parameters reqp, HttpServletRequest req) throws HTTPError {

        Credentials creds = Credentials.fromJSON(reqp);
        List<String> labels = reqp.getAsList("labels", String.class);
        String kind = reqp.get("kind", system.defaultKind);
        int numResults = (int) reqp.get("n", 10);
        int skipResults = (int) reqp.get("skip", 0);

        log.info("GetResourcesForLabels creds=" + creds + " labels=" + labels + " kind=" + kind);
        List<String> resList;
        try {

            resList = userdb.getResourcesForLabels(creds.userid, labels, numResults, skipResults);

            // now get results
            Parameters param = Parameters.create();
            param.set("snippets", false);
            param.set("tags", true);

            List<ScoredDocument> fakeDocs = new ArrayList<>();
            for (String id : resList) {
                fakeDocs.add(new ScoredDocument(id, 0, 0.0));
            }

            List<Parameters> results = DocumentAnnotator.annotate(system, kind, fakeDocs, null, param);

            Parameters response = Parameters.create();
            response.set("results", results);
            return response;

        } catch (DBError dbError) {
            throw new HTTPError(dbError);
        }

    }
}
