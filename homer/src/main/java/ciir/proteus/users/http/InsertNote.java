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
public class InsertNote extends DBAction {

    public InsertNote(ProteusSystem proteus) {
        super(proteus);
    }

    @Override
    public Parameters handle(String method, String path, Parameters reqp, HttpServletRequest req) throws HTTPError, DBError {
        Integer id = -1;
        Credentials creds = null; //  Credentials.fromJSON(reqp);
        String nullStr = null;
        String res = reqp.get("uri", nullStr);
        String data = reqp.toString();
        Integer corpusid = reqp.get("corpus", -1);
        id = userdb.insertNote(creds, corpusid, res, data);
        proteusLog.info("ADD-NOTE\t{}\t{}\t{}\t{}\t{}", ClickLogHelper.getID(reqp, req), id, corpusid, res, data);

        Parameters ret = Parameters.create();
        ret.put("id", id);
        return ret;
    }
}
