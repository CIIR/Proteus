package ciir.proteus.users;

import ciir.proteus.server.HTTPError;
import ciir.proteus.server.TestEnvironment;
import ciir.proteus.util.HTTPUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.lemurproject.galago.tupleflow.Parameters;
import org.lemurproject.galago.tupleflow.web.WebServerException;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * @author jfoley.
 */
public class HTTPTagTest {
  public static TestEnvironment env;

  @BeforeClass
  public static void setup() throws IOException, WebServerException {
    env = new TestEnvironment();
  }

  @AfterClass
  public static void tearDown() throws IOException, WebServerException {
    env.close();
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
    String url = env.url;

    Parameters requestJSON;
    Parameters responseJSON;
    HTTPUtil.Response response;

    // expect a bad request with unregistered user
    requestJSON = new Parameters();
    requestJSON.set("user", "fake-user");
    response = HTTPUtil.post(url, "/api/login", requestJSON);
    assertEquals(HTTPError.BadRequest, response.status);
    assertEquals("No such user!", response.reason);

    // empty success response to register
    requestJSON = new Parameters();
    requestJSON.set("user", "register-me");
    response = HTTPUtil.post(url, "/api/register", requestJSON);
    assertEquals(200, response.status);
    assertEquals("OK", response.reason);
    responseJSON = Parameters.parseString(response.body);
    assertEquals(0, responseJSON.size());

    // token success response to login
    requestJSON = new Parameters();
    requestJSON.set("user", "register-me");
    response = HTTPUtil.post(url, "/api/login", requestJSON);
    assertEquals(200, response.status);
    assertEquals("OK", response.reason);
    responseJSON = Parameters.parseString(response.body);
    assertTrue(responseJSON.isString("token"));
    assertNotNull(responseJSON.getString("token"));

    String token = responseJSON.getString("token");

    // use token to list tags
    requestJSON = new Parameters();
    requestJSON.set("user", "register-me");
    requestJSON.set("token", token);
    requestJSON.set("resource", Arrays.asList("fake-resource0", "fake-resource1"));
    response = HTTPUtil.get(url, "/api/tags", requestJSON);
    System.out.println(response);
    assertEquals(200, response.status);
    assertEquals("OK", response.reason);
    responseJSON = Parameters.parseString(response.body);
    assertTrue(responseJSON.containsKey("fake-resource0"));
    assertTrue(responseJSON.containsKey("fake-resource1"));
    assertTrue(responseJSON.getList("fake-resource0").isEmpty());
    assertTrue(responseJSON.getList("fake-resource1").isEmpty());
  }
}
