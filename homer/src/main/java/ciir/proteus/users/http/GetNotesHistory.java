package ciir.proteus.users.http;

import ciir.proteus.server.HTTPError;
import ciir.proteus.system.ProteusSystem;
import ciir.proteus.users.error.DBError;
import org.lemurproject.galago.utility.Parameters;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author michaelz.
 */
public class GetNotesHistory extends DBAction {

    public GetNotesHistory(ProteusSystem proteus) {
        super(proteus);
    }

    @Override
    public Parameters handle(String method, String path, Parameters reqp, HttpServletRequest req) throws HTTPError, DBError, IOException {

        Integer corpusid = reqp.get("corpus", -1);
        Parameters notes = Parameters.create();

        notes = userdb.getNotesForCorpus(corpusid);

        return notes;
    }
}
