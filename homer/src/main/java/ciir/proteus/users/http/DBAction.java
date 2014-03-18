package ciir.proteus.users.http;

import ciir.proteus.server.action.JSONHandler;
import ciir.proteus.system.ProteusSystem;
import ciir.proteus.users.UserDatabase;

/**
 * @author jfoley.
 */
public abstract class DBAction implements JSONHandler {
  public final UserDatabase userdb;

  public DBAction(ProteusSystem proteus) {
    this.userdb = proteus.userdb;
  }
}
