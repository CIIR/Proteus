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
import ciir.proteus.util.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author michaelz
 */
public class CreateCorpusTest {

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
    public void createCorpusTest() throws DBError {

        String user = "maxdcat";
        env.proteus.userdb.register(user);
        Parameters p = env.proteus.userdb.login(user);
        Credentials cred = new Credentials(p);

        String corpusName = "new corpus name";
        Parameters np = Parameters.create();
        np.set("corpus", corpusName);
        Parameters c = cred.toJSON();
        np.copyFrom(c);

        MockHttpServletRequest req = new MockHttpServletRequest();
        Parameters results = Parameters.create();
        try {
            CreateCorpus cc = new CreateCorpus(env.proteus);
            results = cc.handle(null, null, np, req);
        } catch (HTTPError httpError) {
            fail(httpError.getMessage());
        }

        List<Parameters> corpora = new ArrayList<Parameters>();
        p = env.proteus.userdb.getAllCorpora();
        corpora = p.getAsList("corpora", Parameters.class);
        assertEquals(corpora.size(), 1);
        assertEquals(corpusName, corpora.get(0).getString("name"));
    }

}
