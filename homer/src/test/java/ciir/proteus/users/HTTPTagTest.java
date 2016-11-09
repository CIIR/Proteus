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
    put.set("queryid", 1);
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
