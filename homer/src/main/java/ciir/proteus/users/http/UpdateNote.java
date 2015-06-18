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
public class UpdateNote extends DBAction {

    public UpdateNote(ProteusSystem proteus) {
        super(proteus);
    }

    @Override
    public Parameters handle(String method, String path, Parameters reqp, HttpServletRequest req) throws HTTPError, DBError {

        Credentials creds = null; // Credentials.fromJSON(reqp);
        String nullStr = null;
        Integer id =  reqp.get("id", -1);
        String data = reqp.toString();
        String res = reqp.get("uri", nullStr);
        Integer corpusid = reqp.get("corpus", -1);
        userdb.updateNote(creds, id, corpusid, data);
        proteusLog.info("UPD-NOTE\t{}\t{}\t{}\t{}\t{}", ClickLogHelper.getID(reqp, req), id, corpusid, res, data);

        return Parameters.create();
    }
}
