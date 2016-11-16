package ciir.proteus.users.http;

import ciir.proteus.server.HTTPError;
import ciir.proteus.system.ProteusSystem;
import ciir.proteus.users.error.DBError;
import ciir.proteus.users.error.NoTuplesAffected;
import org.lemurproject.galago.utility.Parameters;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;

/**
 * @author michaelz.
 */
public class UpdateSubCorpora extends DBAction {

    public UpdateSubCorpora(ProteusSystem proteus) {
        super(proteus);
    }

    @Override
    public Parameters handle(String method, String path, Parameters reqp, HttpServletRequest req) throws HTTPError, DBError, SQLException {

        Parameters ret = Parameters.create();

        try {
            ret = userdb.upsertSubCorpus(reqp);
            //            log.info("user " + userName + " created corpus: " + corpusName);
            //            CreateCorpusLogData logData = new CreateCorpusLogData(ClickLogHelper.getID(reqp, req), reqp.get("user", ""));
            //            logData.setCorpusID(corpusID);
            //            logData.setCorpusName(corpusName);
            //            LogHelper.log(logData, system);

        } catch (NoTuplesAffected ex) {
            throw new HTTPError(ex);
        }

        return ret;
    }
}
