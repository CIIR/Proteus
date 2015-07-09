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
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author michaelz
 */
public class RateResourceTest {

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
    public void rateResourceTest() throws DBError, SQLException {

        String user = "maxdcat";
        env.proteus.userdb.register(user);
        Parameters p = env.proteus.userdb.login(user);
        Credentials cred = new Credentials(p);

        env.proteus.userdb.createCorpus("test corpus 1", "user");

        String res1 = "document1";
        Integer corpus1=1;
        Parameters rating = Parameters.create();
        rating.set("resource", res1);
        rating.set("rating", 2);
        rating.set("corpus",corpus1);
        rating.set("corpusName","test corpus 1");
        Parameters c = cred.toJSON();
        rating.copyFrom(c);

        Parameters results = Parameters.create();
        try {
            RateResource rr = new RateResource(env.proteus);
            results = rr.handle(null, null, rating, null);
        } catch (HTTPError httpError) {
            fail(httpError.getMessage());
        }

        results = env.proteus.userdb.getResourceRatings(res1,corpus1 );
        assertEquals(results.get("aveRating", -1), 2);
        assertEquals(results.getAsList("ratings").size(), 1);

    }

}
