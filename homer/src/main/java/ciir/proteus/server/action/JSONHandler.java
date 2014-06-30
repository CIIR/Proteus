package ciir.proteus.server.action;

import ciir.proteus.server.HTTPError;
import ciir.proteus.users.error.DBError;
import org.lemurproject.galago.utility.Parameters;

public interface JSONHandler {
  public abstract Parameters handle(String method, String path, Parameters reqp) throws HTTPError, DBError;
}
