package ciir.proteus.server.action;

import ciir.proteus.server.HTTPError;
import ciir.proteus.users.error.DBError;
import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.http.HttpServletRequest;
import org.lemurproject.galago.utility.Parameters;

public interface JSONHandler {

    Parameters handle(String method, String path, Parameters reqp, HttpServletRequest req) throws Exception;
}
