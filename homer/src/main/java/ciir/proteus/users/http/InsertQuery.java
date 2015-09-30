package ciir.proteus.users.http;

import ciir.proteus.server.HTTPError;
import ciir.proteus.system.ProteusSystem;
import ciir.proteus.users.Credentials;
import ciir.proteus.users.error.DBError;
import ciir.proteus.util.logging.AddNoteLogData;
import ciir.proteus.util.logging.ClickLogHelper;
import ciir.proteus.util.logging.LogHelper;
import org.lemurproject.galago.utility.Parameters;

import javax.servlet.http.HttpServletRequest;

/**
 * @author michaelz.
 */
public class InsertQuery extends DBAction {

    public InsertQuery(ProteusSystem proteus) {
        super(proteus);
    }

    @Override
    public Parameters handle(String method, String path, Parameters reqp, HttpServletRequest req) throws HTTPError, DBError {
        Integer id = -1;

        // ???????? do we need this or can we do it before executing the query? do we CARE about things
        // like "skip" and "start" counts?
        Credentials creds = Credentials.fromJSON(reqp);
        String nullStr = null;
        String query = reqp.get("query", nullStr);
        String kind = reqp.get("kind", nullStr);
        Integer corpusid = reqp.get("corpus", -1);
        id = userdb.insertQuery(creds, corpusid, query, kind);

//        AddNoteLogData logData = new AddNoteLogData(ClickLogHelper.getID(reqp, req), reqp.get("user", ""));
//        logData.setCorpus(corpusid);
//        logData.setId(id);
//        logData.setData(data);
//        logData.setResource(res);
//        logData.setCorpusName(reqp.getAsString("corpusName"));
//        LogHelper.log(logData, system);

        Parameters ret = Parameters.create();
        ret.put("id", id);
        return ret;
    }
}
