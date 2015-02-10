package ciir.proteus.server.action;

import javax.servlet.http.HttpServletRequest;
import org.lemurproject.galago.utility.Parameters;

/**
 * @author jfoley.
 */
public class DebugHandler implements JSONHandler {
  @Override
  public Parameters handle(String method, String path, Parameters reqp, HttpServletRequest req) {
    Parameters debug = Parameters.create();
    debug.set("http-method", method);
    debug.set("http-path", path);
    debug.set("request", reqp);
    return debug;
  }
}
