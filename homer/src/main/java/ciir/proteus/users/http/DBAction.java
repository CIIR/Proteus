package ciir.proteus.users.http;

import ciir.proteus.server.action.JSONHandler;
import ciir.proteus.system.ProteusSystem;
import ciir.proteus.users.UserDatabase;

import java.util.logging.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * @author jfoley.
 */
abstract class DBAction implements JSONHandler {

    static final Logger log = Logger.getLogger(DBAction.class.getName());
    protected static final org.apache.logging.log4j.Logger proteusLog = LogManager.getLogger("Proteus");

    final UserDatabase userdb;
    final ProteusSystem system;

    DBAction(ProteusSystem proteus) {
        system = proteus;
        this.userdb = proteus.userdb;
    }
}
