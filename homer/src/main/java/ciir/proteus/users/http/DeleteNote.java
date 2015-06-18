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
        proteusLog.info("DEL-NOTE\t{}\t{}\t{}\t{}\t{}", ClickLogHelper.getID(reqp, req), id, corpusid, res, data);

        return Parameters.create();
    }
}
