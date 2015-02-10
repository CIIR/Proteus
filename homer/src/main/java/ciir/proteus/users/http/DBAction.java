package ciir.proteus.users.http;

import ciir.proteus.server.action.JSONHandler;
import ciir.proteus.system.ProteusSystem;
import ciir.proteus.users.UserDatabase;

import java.util.logging.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * @author jfoley.
 */
public abstract class DBAction implements JSONHandler {

    protected static final Logger log = Logger.getLogger(DBAction.class.getName());
    protected static final org.apache.logging.log4j.Logger proteusLog = LogManager.getLogger("Proteus");

    public final UserDatabase userdb;

    public DBAction(ProteusSystem proteus) {
        this.userdb = proteus.userdb;
    }
}
