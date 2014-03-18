package ciir.proteus.users.http;

import ciir.proteus.server.action.JSONHandler;
import ciir.proteus.system.ProteusSystem;
import ciir.proteus.users.UserDatabase;

import java.util.logging.Logger;

/**
 * @author jfoley.
 */
public abstract class DBAction implements JSONHandler {
  public static final Logger log = Logger.getLogger(DBAction.class.getName());
  public final UserDatabase userdb;

  public DBAction(ProteusSystem proteus) {
    this.userdb = proteus.userdb;
  }
}
