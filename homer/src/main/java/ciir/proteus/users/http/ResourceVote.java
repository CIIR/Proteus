package ciir.proteus.users.http;

import ciir.proteus.server.HTTPError;
import ciir.proteus.system.ProteusSystem;
import ciir.proteus.users.Credentials;
import ciir.proteus.users.error.DBError;
import ciir.proteus.util.logging.ClickLogHelper;
import ciir.proteus.util.logging.LogHelper;
import ciir.proteus.util.logging.RateResourceLogData;
import org.lemurproject.galago.utility.Parameters;

import javax.servlet.http.HttpServletRequest;

/**
 * @author michaelz.
 */
public class ResourceVote extends DBAction {

    public ResourceVote(ProteusSystem proteus) {
        super(proteus);
    }

    @Override
    public Parameters handle(String method, String path, Parameters reqp, HttpServletRequest req) throws HTTPError, DBError {
        Credentials creds = Credentials.fromJSON(reqp);
        String res = reqp.getString("resource");
        int corpusid = (int) reqp.getLong("corpusid");
        int subcorpusid = (int) reqp.getLong("subcorpusid");
        int queryid = (int) reqp.getLong("queryid");
        String action = reqp.get("action", "?");

        if (action.equals("add")) {
            userdb.addVoteForResource(creds, res, corpusid, subcorpusid, queryid);
        } else if (action.equals("remove")) {
            userdb.removeVoteForResource(creds, res, corpusid, subcorpusid);
        } else {
            throw new RuntimeException("unknown action for vote.");
        }

        //        RateResourceLogData logData = new RateResourceLogData(ClickLogHelper.getID(reqp, req), reqp.get("user", ""));
//        logData.setCorpus(corpus);
//        logData.setRating(rating);
//        logData.setResource(res);
//        logData.setCorpusName(reqp.getAsString("corpusName"));
//        LogHelper.log(logData, system);
        // TODO ??? include queryid?
        return Parameters.create();
    }
}
