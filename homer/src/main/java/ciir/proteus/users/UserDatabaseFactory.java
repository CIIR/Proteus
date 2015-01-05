package ciir.proteus.users;

import ciir.proteus.users.impl.H2Database;
import java.sql.SQLException;

import org.lemurproject.galago.utility.Parameters;

/**
 * @author jfoley.
 */
public class UserDatabaseFactory {

    public static UserDatabase instance(Parameters dbp) throws Exception {
        String impl = dbp.get("impl", "H2Database");

        if ("H2Database".equals(impl)) {
            try {
                return new H2Database(dbp);
            } catch (SQLException ex) {
                throw new Exception("Error creating H2 Database");
            }
        }

        throw new IllegalArgumentException("Unknown impl=" + impl);
    }
}
