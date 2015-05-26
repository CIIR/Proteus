package ciir.proteus.users.http;

import ciir.proteus.server.HTTPError;
import ciir.proteus.system.ProteusSystem;
import ciir.proteus.users.Credentials;
import ciir.proteus.users.error.DBError;
import ciir.proteus.util.ClickLogHelper;
import org.lemurproject.galago.utility.Parameters;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author michaelz.
 */
public class RateResource extends DBAction {

    public RateResource(ProteusSystem proteus) {
        super(proteus);
    }

    @Override
    public Parameters handle(String method, String path, Parameters reqp, HttpServletRequest req) throws HTTPError, DBError {
        Credentials creds = Credentials.fromJSON(reqp);
        String res = reqp.getString("resource");
        int rating = (int) reqp.getLong("rating");
        int corpus = (int) reqp.getLong("corpus");

        userdb.upsertResourceRating(creds, res, creds.userid, corpus, rating);
        proteusLog.info("RATE-RESOURCE\t{}\t{}\t{}\t{}", ClickLogHelper.getID(reqp, req), res, corpus, rating);

        return Parameters.create();
    }
}
