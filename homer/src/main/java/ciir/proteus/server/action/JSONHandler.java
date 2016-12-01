package ciir.proteus.server.action;

import javax.servlet.http.HttpServletRequest;
import org.lemurproject.galago.utility.Parameters;

public interface JSONHandler {

    Parameters handle(String method, String path, Parameters reqp, HttpServletRequest req) throws Exception;
}
