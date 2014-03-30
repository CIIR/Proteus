package ciir.proteus.users;

import ciir.proteus.server.HTTPError;
import ciir.proteus.server.TestEnvironment;
import ciir.proteus.users.error.NoTuplesAffected;
import ciir.proteus.util.HTTPUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.lemurproject.galago.tupleflow.Parameters;
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
  public static void setup() throws IOException, WebServerException, NoTuplesAffected {
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
    requestJSON = new Parameters();
    requestJSON.set("user", "fake-user");
    response = post("/api/login", requestJSON);
    assertEquals(HTTPError.BadRequest, response.status);
    assertEquals("No such user!", response.reason);

    // empty success response to register
    requestJSON = new Parameters();
    requestJSON.set("user", "register-me");
    response = post("/api/register", requestJSON);
    assertOK(response);
    responseJSON = Parameters.parseString(response.body);
    assertEquals(0, responseJSON.size());

    // token success response to login
    requestJSON = new Parameters();
    requestJSON.set("user", "register-me");
    response = post("/api/login", requestJSON);
    assertOK(response);
    responseJSON = Parameters.parseString(response.body);
    assertTrue(responseJSON.isString("token"));
    assertNotNull(responseJSON.getString("token"));

    String token = responseJSON.getString("token");

    // use token to list tags
    requestJSON = new Parameters();
    requestJSON.set("user", "register-me");
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
  public void putTags() throws IOException {
    Parameters creds = env.creds.toJSON();

    Parameters put = new Parameters();
    put.copyFrom(creds);
    put.set("tags", Parameters.parseArray(
        "funny", Arrays.asList("res1", "res2", "res12", "res22"),
        "romeo", Arrays.asList("res2", "res17")));

    assertOK(HTTPUtil.putJSON(env.url, "/api/tags", put));

    Parameters getp = new Parameters();
    getp.copyFrom(creds);
    getp.set("resource", Arrays.asList("res1", "res12", "res22", "res2", "res17"));
    HTTPUtil.Response resp = get("/api/tags", getp);
    assertOK(resp);
    Parameters json = Parameters.parseString(resp.body);

    assertEquals("funny", json.getAsList("res1", String.class).get(0));
    assertEquals(1, json.getAsList("res1", String.class).size());

    Set<String> res2tags = new HashSet<String>(json.getAsList("res2", String.class));
    assertTrue(res2tags.contains("romeo"));
    assertTrue(res2tags.contains("funny"));

    assertEquals("funny", json.getAsList("res12", String.class).get(0));
    assertEquals(1, json.getAsList("res12", String.class).size());
    assertEquals("funny", json.getAsList("res22", String.class).get(0));
    assertEquals(1, json.getAsList("res22", String.class).size());
    assertEquals("romeo", json.getAsList("res17", String.class).get(0));
    assertEquals(1, json.getAsList("res17", String.class).size());
  }
}
