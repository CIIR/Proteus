package ciir.proteus.users.http;

import ciir.proteus.server.HTTPError;
import ciir.proteus.system.ProteusSystem;
import ciir.proteus.users.Credentials;
import ciir.proteus.users.error.DBError;
import org.lemurproject.galago.utility.Parameters;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author michaelz.
 */
public class GetNotesForResource extends DBAction {

    public GetNotesForResource(ProteusSystem proteus) {
        super(proteus);
    }

    @Override
    public Parameters handle(String method, String path, Parameters reqp, HttpServletRequest req) throws HTTPError, DBError, IOException {

        String nullStr = null;
        String resource = reqp.get("uri", nullStr);
        Integer corpusid = reqp.get("corpus", -1);
        Parameters notes = Parameters.create();

        notes = userdb.getNotesForResource(resource, corpusid);

        return notes;
    }
}
