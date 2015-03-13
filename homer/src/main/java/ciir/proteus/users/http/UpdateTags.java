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
public class UpdateTags extends DBAction {

    public UpdateTags(ProteusSystem proteus) {
        super(proteus);
    }

    @Override
    public Parameters handle(String method, String path, Parameters reqp, HttpServletRequest req) throws HTTPError, DBError {
        Credentials creds = Credentials.fromJSON(reqp);
        Parameters tags = reqp.getMap("tags");
        int rating = (int) reqp.getLong("rating");
        String nullStr = null;
        String comment = reqp.get("comment", nullStr);
        for (String tag : tags.keySet()) {
            List<String> resources = tags.getAsList(tag, String.class);
            for (String resource : resources) {
                userdb.updateTag(creds, resource, tag, rating, comment);
                proteusLog.info("UPD-TAG\t{}\t{}\t{}\t{}\t{}", ClickLogHelper.getID(reqp, req), resource, tag, rating, comment);

            }
        }

        return Parameters.create();
    }
}
