package ciir.proteus.users;

import ciir.proteus.server.HTTPError;
import ciir.proteus.server.TestEnvironment;
import ciir.proteus.users.error.DuplicateCorpus;
import ciir.proteus.users.error.DuplicateUser;
import ciir.proteus.users.error.NoTuplesAffected;
import ciir.proteus.util.HTTPUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.lemurproject.galago.utility.Parameters;
import org.lemurproject.galago.tupleflow.web.WebServerException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * @author jfoley, michaelz
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
  public void putDeleteTags() throws IOException, DuplicateUser, NoTuplesAffected {
    Parameters creds = env.creds.toJSON();

    // put
    Parameters put = Parameters.create();
    put.copyFrom(creds);
    put.set("tags", Parameters.parseArray(
            "type:funny", Arrays.asList("res1", "res2", "res12", "res22"),
            "type:romeo", Arrays.asList("res2", "res17")));
    put.set("rating", 0);
    assertOK(HTTPUtil.postJSON(env.url, "/api/tags/create", put));

    // log in a 2nd user
    String user = "2ndUser";
    env.proteus.userdb.register(user);
    Parameters p = env.proteus.userdb.login(user);
    assertNotNull(p.get("token"));
    Credentials user2creds = new Credentials(p);
    // have 2nd user label resource2
    Parameters put2 = Parameters.create();
    put2.copyFrom(user2creds.toJSON());
    put2.set("tags", Parameters.parseArray(
            "type:funny", Arrays.asList("res1")));
    put2.set("rating", 2);
    put2.set("comment", "user2 comment");
    assertOK(HTTPUtil.postJSON(env.url, "/api/tags/create", put2));

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
    String user2id = user2creds.toJSON().get("userid").toString();
    tmp = json.getMap("res1");
    assertEquals(tmp.size(), 2);
    Parameters labelAndRating = Parameters.create();
    labelAndRating = tmp.get(userid, Parameters.create());
    assertEquals(labelAndRating.get("type:funny"), "0:");
    labelAndRating = tmp.get(user2id, Parameters.create());
    assertEquals(labelAndRating.get("type:funny"), "2:user2 comment");

    tmp = json.get("res2", dummy);
    labelAndRating = tmp.get(userid, Parameters.create());
    assertEquals(labelAndRating.get("type:funny"), "0:");
    assertEquals(labelAndRating.get("type:romeo"), "0:");

    tmp = json.get("res12", dummy);
    labelAndRating = tmp.get(userid, Parameters.create());
    assertEquals(labelAndRating.get("type:funny"), "0:");

    tmp = json.get("res22", dummy);
    labelAndRating = tmp.get(userid, Parameters.create());
    assertEquals(labelAndRating.get("type:funny"), "0:");

    tmp = json.get("res17", dummy);
    labelAndRating = tmp.get(userid, Parameters.create());
    assertEquals(labelAndRating.get("type:romeo"), "0:");

    // delete all user1's labels
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
    // user2 still has a label
    assertEquals(1, json.getAsList("res1", String.class).size());
    assertEquals(0, json.getAsList("res2", String.class).size());
    assertEquals(0, json.getAsList("res12", String.class).size());
    assertEquals(0, json.getAsList("res22", String.class).size());
    assertEquals(0, json.getAsList("res17", String.class).size());

  }

  @Test
  public void testRateResource() throws NoTuplesAffected, DuplicateCorpus, IOException, SQLException {
    Parameters creds = env.creds.toJSON();
    Parameters put = Parameters.create();
    put.copyFrom(creds);
    env.proteus.userdb.createCorpus("test corpus 1", "user");

    String res1 = "document1";
    put.set("resource", res1);
    put.set("rating", 2);
    put.set("corpus", 1);
    put.set("corpusName", "test corpus 1");

    assertOK(HTTPUtil.postJSON(env.url, "/api/rateresource", put));
  }

  @Test
  public void testUpdateUserSettings() throws NoTuplesAffected, DuplicateCorpus, IOException {
    Parameters creds = env.creds.toJSON();
    Parameters put = Parameters.create();
    put.copyFrom(creds);

    Parameters settings = Parameters.create();

    settings.set("TestInt", 123);
    settings.set("TestStr", "hello");
    put.set("settings", settings);
    assertOK(HTTPUtil.postJSON(env.url, "/api/updatesettings", put));
  }

  @Test
  public void testCreateCorpus() throws NoTuplesAffected, DuplicateCorpus, IOException {
    Parameters creds = env.creds.toJSON();
    Parameters put = Parameters.create();
    put.copyFrom(creds);
    put.set("corpus", "new corpus name");
    assertOK(HTTPUtil.postJSON(env.url, "/api/newcorpus", put));
  }

}
