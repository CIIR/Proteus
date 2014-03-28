package ciir.proteus.users;

import ciir.proteus.users.impl.H2Database;
import org.lemurproject.galago.tupleflow.Parameters;

/**
 * @author jfoley.
 */
public class UserDatabaseFactory {
  public static UserDatabase instance(Parameters dbp) {
    String impl = dbp.get("impl", "H2Database");

    if("H2Database".equals(impl)) {
      UserDatabase newDB = new H2Database(dbp);
      newDB.initDB();
      return newDB;
    }

    throw new IllegalArgumentException("Unknown impl="+impl);
  }
}
