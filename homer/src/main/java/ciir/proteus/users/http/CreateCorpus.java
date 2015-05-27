package ciir.proteus.users.http;

import ciir.proteus.server.HTTPError;
import ciir.proteus.system.ProteusSystem;
import ciir.proteus.users.Users;
import ciir.proteus.users.error.DuplicateCorpus;
import ciir.proteus.users.error.DuplicateUser;
import ciir.proteus.users.error.NoTuplesAffected;
import org.lemurproject.galago.utility.Parameters;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;

/**
 * @author michaelz.
 */
public class CreateCorpus extends DBAction {

    public CreateCorpus(ProteusSystem proteus) {
        super(proteus);
    }

    @Override
    public Parameters handle(String method, String path, Parameters reqp, HttpServletRequest req) throws HTTPError, DuplicateCorpus, SQLException {
        String userName = reqp.getString("user");
        String corpusName = reqp.getString("corpus");
        Integer corpusID = -1;

        try {
            corpusID = userdb.createCorpus(corpusName, userName);
            log.info("user " + userName + " created corpus: " + corpusName);
            proteusLog.info("CREATE_CORPUS\t{}\t{}\t{}", req.getRemoteAddr(), corpusName, userName);

        } catch (NoTuplesAffected ex) {
            throw new HTTPError(ex);
        }

        Parameters p = Parameters.create();
        p.put("id", corpusID);
        return p;
    }
}
