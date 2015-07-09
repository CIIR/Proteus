package ciir.proteus.users.http;

import ciir.proteus.server.HTTPError;
import ciir.proteus.system.ProteusSystem;
import ciir.proteus.users.Credentials;
import ciir.proteus.users.error.DBError;
import ciir.proteus.util.logging.ClickLogHelper;
import ciir.proteus.util.logging.AddTagLogData;
import ciir.proteus.util.logging.LogHelper;
import org.lemurproject.galago.utility.Parameters;

import java.util.List;
import javax.servlet.http.HttpServletRequest;

/**
 * @author jfoley.
 */
public class PutTags extends DBAction {

    public PutTags(ProteusSystem proteus) {
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
                userdb.addTag(creds, resource, tag, rating, comment);

                AddTagLogData logData = new AddTagLogData(ClickLogHelper.getID(reqp, req), reqp.get("user", ""));
                logData.setComment(comment);
                logData.setRating(rating);
                logData.setTag(tag);
                logData.setResource(resource);
                LogHelper.log(logData);

            }
        }

        return Parameters.create();
    }
}
