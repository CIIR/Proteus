package ciir.proteus.users.http;

import ciir.proteus.server.HTTPError;
import ciir.proteus.server.TestEnvironment;
import ciir.proteus.users.error.DBError;
import ciir.proteus.users.error.DuplicateUser;
import ciir.proteus.users.error.NoTuplesAffected;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.lemurproject.galago.tupleflow.web.WebServerException;
import org.lemurproject.galago.utility.Parameters;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author michaelz
 */
public class GetUsersTest {

    public static TestEnvironment env;

    @BeforeClass
    public static void setup() throws IOException, WebServerException, NoTuplesAffected, DuplicateUser, Exception {
        env = new TestEnvironment();
    }

    @AfterClass
    public static void tearDown() throws IOException, WebServerException {
        env.close();
    }

    @Test
    public void getUsersTest() throws DBError {

        env.proteus.userdb.register("maxdcat");
        // Note the TestEnvironment creates a user: proteusTestUser

        Parameters results = Parameters.create();

        try {
            GetUsers uo = new GetUsers(env.proteus);
            results = uo.handle(null, null, null, null);
        } catch (HTTPError httpError) {
            fail(httpError.getMessage());
        }
        assertEquals(1, results.size());
        Map<Integer, String> users = (Map<Integer, String>) results.get("users");

        assertTrue(users.values().contains("proteustestuser"));
        assertTrue(users.values().contains("maxdcat"));


    }

}
