package ciir.proteus.users.http;

import ciir.proteus.server.HTTPError;
import ciir.proteus.server.TestEnvironment;
import ciir.proteus.users.Credentials;
import ciir.proteus.users.error.DBError;
import ciir.proteus.users.error.DuplicateUser;
import ciir.proteus.users.error.NoTuplesAffected;
import ciir.proteus.util.MockHttpServletRequest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.lemurproject.galago.tupleflow.web.WebServerException;
import org.lemurproject.galago.utility.Parameters;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author michaelz
*/

public class NoteAPITest {

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
    public void noteTest() throws Exception {

        String user = "maxdcat";
        env.proteus.userdb.register(user);
        Parameters p = env.proteus.userdb.login(user);
        Credentials cred = new Credentials(p);

        env.proteus.userdb.createCorpus("a", "user");
        Integer corpus1 = 1;

        String resource = "res1";
        Parameters np = Parameters.create();
        np.set("uri", resource);
        np.set("data", "{ \"data\" : 123 }");
        np.set("corpus", corpus1);
        np.set("corpusName", "a");
        Parameters creds = cred.toJSON();
        np.copyFrom(creds);

        MockHttpServletRequest req = new MockHttpServletRequest();
        Parameters results = Parameters.create();
        try {
            InsertNote cc = new InsertNote(env.proteus);
            results = cc.handle(null, null, np, req);
        } catch (HTTPError httpError) {
            fail(httpError.getMessage());
        }

        Long id = results.get("id", -1L);

        assertEquals(id.intValue(), 1);
        Parameters notes = env.proteus.userdb.getNotesForResource(resource, corpus1);
        assertEquals(notes.get("total", -1), 1);
        List<Parameters> arr = notes.getAsList("rows");
        assertTrue(arr.get(0).get("data", "?").equals("{ \"data\" : 123 }"));

        np = Parameters.create();
        np.set("uri", resource);
        np.set("data", "{ \"new\" : \"data\" }");
        np.set("id", id);
        np.set("corpus", corpus1);
        np.set("corpusName", "a");
        np.copyFrom(creds);

        try {
            UpdateNote cc = new UpdateNote(env.proteus);
            results = cc.handle(null, null, np, req);
        } catch (HTTPError httpError) {
            fail(httpError.getMessage());
        }

        notes = env.proteus.userdb.getNotesForResource(resource, corpus1);
        assertEquals(notes.get("total", -1), 1);
        arr = notes.getAsList("rows");
        assertTrue(arr.get(0).get("data", "?").equals("{ \"new\" : \"data\" }"));

        try {
            GetNotesForResource cc = new GetNotesForResource(env.proteus);
            results = cc.handle(null, null, np, req);
        } catch (HTTPError httpError) {
            fail(httpError.getMessage());
        }

        assertEquals(results.get("total", -1), 1L);
        arr = results.getAsList("rows");
        assertTrue(arr.get(0).get("data", "?").equals("{ \"new\" : \"data\" }"));

        try {
            DeleteNote cc = new DeleteNote(env.proteus);
            results = cc.handle(null, null, np, req);
        } catch (HTTPError httpError) {
            fail(httpError.getMessage());
        }

        notes = env.proteus.userdb.getNotesForResource(resource, corpus1);
        assertEquals(notes.get("total", -1), 0);
        arr = notes.getAsList("rows");
        assertTrue(arr.size() == 0);

        // test that notes are retrieved if the resource is a number or string type
        resource = "123";
        np = Parameters.create();
        np.set("uri", resource);
        np.set("data", "abc");
        np.set("corpus", corpus1);
        np.set("corpusName", "a");
        creds = cred.toJSON();
        np.copyFrom(creds);

        results = Parameters.create();
        try {
            InsertNote cc = new InsertNote(env.proteus);
            results = cc.handle(null, null, np, req);
        } catch (HTTPError httpError) {
            fail(httpError.getMessage());
        }

        // resource as a string
        np = Parameters.create();
        np.set("uri", resource);
        np.set("corpus", corpus1);
        assertTrue(np.get("uri") instanceof String);

        try {
            GetNotesForResource cc = new GetNotesForResource(env.proteus);
            results = cc.handle(null, null, np, req);
        } catch (HTTPError httpError) {
            fail(httpError.getMessage());
        }

        assertEquals(results.get("total", -1), 1L);
        arr = results.getAsList("rows");
        assertTrue(arr.get(0).get("data", "?").equals("abc"));

        // resource as an integer - sometimes when passing JSON, the javascript
        // inisits on turning a string like "123" into an integer parameter.
        np = Parameters.create();
        np.set("uri", 123);
        np.set("corpus", corpus1);
        assertTrue(np.get("uri") instanceof Long);

        try {
            GetNotesForResource cc = new GetNotesForResource(env.proteus);
            results = cc.handle(null, null, np, req);
        } catch (HTTPError httpError) {
            fail(httpError.getMessage());
        }

        assertEquals(results.get("total", -1), 1L);
        arr = results.getAsList("rows");
        assertTrue(arr.get(0).get("data", "?").equals("abc"));


    }

}
