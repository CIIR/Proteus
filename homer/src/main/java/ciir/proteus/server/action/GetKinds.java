package ciir.proteus.server.action;

import ciir.proteus.server.HTTPError;
import ciir.proteus.system.ProteusSystem;
import ciir.proteus.users.error.DBError;
import org.lemurproject.galago.tupleflow.Parameters;

import java.util.ArrayList;

/**
 * @author jfoley.
 */
public class GetKinds implements JSONHandler {
  private final ProteusSystem system;

  public GetKinds(ProteusSystem sys) {
    this.system = sys;
  }

  @Override
  public Parameters handle(String method, String path, Parameters reqp) throws HTTPError, DBError {
    Parameters p = new Parameters();
    p.put("kinds", new ArrayList<String>(system.kinds.keySet()));
    return p;
  }
}
