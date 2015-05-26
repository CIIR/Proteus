package ciir.proteus.users.http;

import ciir.proteus.server.HTTPError;
import ciir.proteus.server.TestEnvironment;
import ciir.proteus.users.Credentials;
import ciir.proteus.users.error.DBError;
import ciir.proteus.users.error.DuplicateUser;
import ciir.proteus.users.error.NoTuplesAffected;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.lemurproject.galago.tupleflow.web.WebServerException;
import org.lemurproject.galago.utility.Parameters;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author michaelz
 */
public class UpdateUserSettingsTest {

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

        String user = "maxdcat";
        env.proteus.userdb.register(user);
        Parameters p = env.proteus.userdb.login(user);
        Parameters results = Parameters.create();
        Parameters settings = Parameters.create();

        settings.set("TestInt", 123);
        settings.set("TestStr", "hello");
        p.set("settings", settings);

        try {
            UpdateUserSettings us = new UpdateUserSettings(env.proteus);
            results = us.handle(null, null, p, null);
        } catch (HTTPError httpError) {
            fail(httpError.getMessage());
        }

        // re-log in, that returns the settings
        Parameters p2 = env.proteus.userdb.login(user);

        Parameters np = Parameters.create();
        try {
            np = Parameters.parseString(p2.get("settings", ""));
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertEquals(np.get("TestInt", -1), 123);
        assertEquals(np.get("TestStr", ""), "hello");
    }

}
