package ciir.proteus.users;

import ciir.proteus.server.HTTPError;
import ciir.proteus.server.TestEnvironment;
import ciir.proteus.users.error.DuplicateUser;
import ciir.proteus.users.error.NoTuplesAffected;
import ciir.proteus.util.HTTPUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.lemurproject.galago.utility.Parameters;
import org.lemurproject.galago.tupleflow.web.WebServerException;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * @author jfoley.
 */
public class HTTPTagTest {

    public static TestEnvironment env;

    @BeforeClass
    public static void setup() throws IOException, WebServerException, NoTuplesAffected, DuplicateUser, Exception {
        env = new TestEnvironment();
    }

    @AfterClass
    public static void tearDown() throws IOException, WebServerException {
        env.close();
    }

    private static HTTPUtil.Response post(String path, Parameters req) throws IOException {
        return HTTPUtil.post(env.url, path, req);
    }

    private static HTTPUtil.Response get(String path, Parameters req) throws IOException {
        return HTTPUtil.get(env.url, path, req);
    }

    public static void assertOK(HTTPUtil.Response resp) {
        assertEquals(200, resp.status);
        assertEquals("OK", resp.reason);
    }

    /**
     * Bare bones test for setup()/teardown()
     */
    @Test
    public void serverRunning() {
        assertNotNull(env.server);
        assertNotNull(env.url);
        assertNotEquals("", env.url);
    }

    @Test
    public void httpLogin() throws IOException {
        Parameters requestJSON;
        Parameters responseJSON;
        HTTPUtil.Response response;

        // expect a bad request with unregistered user
        requestJSON = Parameters.create();
        requestJSON.set("user", "fake-user");
        response = post("/api/login", requestJSON);
        assertEquals(HTTPError.BadRequest, response.status);
        assertEquals("No such user!", response.reason);

        // empty success response to register
        requestJSON = Parameters.create();
        requestJSON.set("user", "register-me");
        response = post("/api/register", requestJSON);
        assertOK(response);
        responseJSON = Parameters.parseString(response.body);
        assertEquals(0, responseJSON.size());

        // token success response to login
        requestJSON = Parameters.create();
        requestJSON.set("user", "register-me");
        response = post("/api/login", requestJSON);
        assertOK(response);
        responseJSON = Parameters.parseString(response.body);
        assertTrue(responseJSON.isString("token"));
        assertNotNull(responseJSON.getString("token"));
        int userid = (int) responseJSON.getLong("userid");
        String token = responseJSON.getString("token");

        // use token to list tags
        requestJSON = Parameters.create();
        requestJSON.set("user", "register-me");
        requestJSON.set("userid", userid);
        requestJSON.set("token", token);
        requestJSON.set("resource", Arrays.asList("fake-resource0", "fake-resource1"));
        response = get("/api/tags", requestJSON);
        assertOK(response);
        responseJSON = Parameters.parseString(response.body);
        assertTrue(responseJSON.containsKey("fake-resource0"));
        assertTrue(responseJSON.containsKey("fake-resource1"));

        assertTrue(responseJSON.getList("fake-resource0").isEmpty());
        assertTrue(responseJSON.getList("fake-resource1").isEmpty());

    }

    @Test
    public void putDeleteTags() throws IOException {
        Parameters creds = env.creds.toJSON();

        // put
        Parameters put = Parameters.create();
        put.copyFrom(creds);
        put.set("tags", Parameters.parseArray(
                "type:funny", Arrays.asList("res1", "res2", "res12", "res22"),
                "type:romeo", Arrays.asList("res2", "res17")));
        put.set("rating", 0);
        assertOK(HTTPUtil.postJSON(env.url, "/api/tags/create", put));

        // get
        Parameters getp = Parameters.create();
        getp.copyFrom(creds);
        getp.set("resource", Arrays.asList("res1", "res12", "res22", "res2", "res17"));
        HTTPUtil.Response resp = get("/api/tags", getp);
        assertOK(resp);
        Parameters json = Parameters.parseString(resp.body);

        // validate
        Parameters dummy = Parameters.create();
        dummy.set("wrongKey", "wrongValue");
        Parameters tmp = Parameters.create();

        String userid = creds.get("userid").toString();
        tmp = json.get("res1", dummy);
        assertEquals("type:funny@0", tmp.getAsList(userid, String.class).get(0));
        assertEquals(1, tmp.getAsList(userid, String.class).size());

        tmp = json.get("res2", dummy);
        Set<String> res2tags = new HashSet<>(tmp.getAsList(userid, String.class));
        assertTrue(res2tags.contains("type:romeo@0"));
        assertTrue(res2tags.contains("type:funny@0"));

        tmp = json.get("res12", dummy);
        assertEquals("type:funny@0", tmp.getAsList(userid, String.class).get(0));
        assertEquals(1, tmp.getAsList(userid, String.class).size());

        tmp = json.get("res22", dummy);
        assertEquals("type:funny@0", tmp.getAsList(userid, String.class).get(0));
        assertEquals(1, tmp.getAsList(userid, String.class).size());

        tmp = json.get("res17", dummy);
        assertEquals("type:romeo@0", tmp.getAsList(userid, String.class).get(0));
        assertEquals(1, tmp.getAsList(userid, String.class).size());

        // delete
        Parameters del = Parameters.create();
        del.copyFrom(creds);
        del.set("tags", Parameters.parseArray(
                "type:funny", Arrays.asList("res1", "res2", "res12", "res22"),
                "type:romeo", Arrays.asList("res2", "res17")));

        assertOK(HTTPUtil.postJSON(env.url, "/api/tags/delete", del));

        resp = get("/api/tags", getp);
        assertOK(resp);
        json = Parameters.parseString(resp.body);

        // validate
        assertEquals(0, json.getAsList("res1", String.class).size());
        assertEquals(0, json.getAsList("res2", String.class).size());
        assertEquals(0, json.getAsList("res12", String.class).size());
        assertEquals(0, json.getAsList("res22", String.class).size());
        assertEquals(0, json.getAsList("res17", String.class).size());

    }
}
