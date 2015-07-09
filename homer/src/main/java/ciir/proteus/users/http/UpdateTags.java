package ciir.proteus.users.http;

import ciir.proteus.server.HTTPError;
import ciir.proteus.system.ProteusSystem;
import ciir.proteus.users.Credentials;
import ciir.proteus.users.error.DBError;
import ciir.proteus.util.logging.ClickLogHelper;
import ciir.proteus.util.logging.LogHelper;
import ciir.proteus.util.logging.UpdateTagLogData;
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

                UpdateTagLogData logData = new UpdateTagLogData(ClickLogHelper.getID(reqp, req), reqp.get("user", ""));
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
