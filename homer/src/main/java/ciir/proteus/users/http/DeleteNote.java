package ciir.proteus.users.http;

import ciir.proteus.server.HTTPError;
import ciir.proteus.system.ProteusSystem;
import ciir.proteus.users.Credentials;
import ciir.proteus.users.error.DBError;
import ciir.proteus.util.logging.ClickLogHelper;
import ciir.proteus.util.logging.DeleteNoteLogData;
import ciir.proteus.util.logging.LogHelper;
import org.lemurproject.galago.utility.Parameters;

import javax.servlet.http.HttpServletRequest;

/**
 * @author michaelz.
 */
public class DeleteNote extends DBAction {

    public DeleteNote(ProteusSystem proteus) {
        super(proteus);
    }

    @Override
    public Parameters handle(String method, String path, Parameters reqp, HttpServletRequest req) throws HTTPError, DBError {
        Credentials creds = null; //  Credentials.fromJSON(reqp);
        Integer id =  reqp.get("id", -1);
        String nullStr = null;
        String res = reqp.get("uri", nullStr);
        String data = reqp.get("data", nullStr);
        Integer corpusid = reqp.get("corpus", -1);
        userdb.deleteNote(creds, id, corpusid);

        DeleteNoteLogData logData = new DeleteNoteLogData(ClickLogHelper.getID(reqp, req), reqp.get("user", ""));
        logData.setCorpus(corpusid);
        logData.setId(id);
        logData.setData(data);
        logData.setResource(res);
        logData.setCorpusName(reqp.getAsString("corpusName"));
        LogHelper.log(logData, system);

        return Parameters.create();
    }
}
