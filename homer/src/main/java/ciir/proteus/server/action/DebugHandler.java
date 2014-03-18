package ciir.proteus.server.action;

import org.lemurproject.galago.tupleflow.Parameters;

/**
 * @author jfoley.
 */
public class DebugHandler implements JSONHandler {
  @Override
  public Parameters handle(String method, String path, Parameters reqp) {
    Parameters debug = new Parameters();
    debug.set("http-method", method);
    debug.set("http-path", path);
    debug.set("request", reqp);
    return debug;
  }
}
