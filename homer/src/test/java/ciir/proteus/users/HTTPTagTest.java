package ciir.proteus.users;

import ciir.proteus.server.HTTPError;
import ciir.proteus.server.HTTPRouter;
import ciir.proteus.system.ProteusSystem;
import ciir.proteus.util.HTTPUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.lemurproject.galago.tupleflow.FileUtility;
import org.lemurproject.galago.tupleflow.Parameters;
import org.lemurproject.galago.tupleflow.Utility;
import org.lemurproject.galago.tupleflow.web.WebServer;
import org.lemurproject.galago.tupleflow.web.WebServerException;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * @author jfoley.
 */
public class HTTPTagTest {
  public static WebServer server;
  public static File folder;
  public static ProteusSystem proteus;
  public static String url;

  @BeforeClass
  public static void setup() throws IOException, WebServerException {
    folder =  FileUtility.createTemporaryDirectory();
    String dbpath = folder.getPath()+"/users";
    Parameters dbp = new Parameters();
    dbp.set("path", dbpath);
    dbp.set("user", "junit");
    dbp.set("pass", "");

    Parameters testSetup = new Parameters();
    testSetup.set("defaultKind", "fake-kind");
    testSetup.set("kinds", new Parameters());
    testSetup.set("userdb", dbp);

    proteus = new ProteusSystem(testSetup);
    proteus.userdb.initDB();

    final HTTPRouter router = new HTTPRouter(proteus);

    int port = Utility.getFreePort();
    server = WebServer.start(port, router);
    url = server.getURL();
  }

  @AfterClass
  public static void teardown() throws IOException, WebServerException {
    server.stop();
    proteus.close();
    Utility.deleteDirectory(folder);
  }

  /**
   * Bare bones test for setup()/teardown()
   */
  @Test
  public void serverRunning() {
    assertNotNull(server);
    assertNotNull(url);
    assertNotEquals("", url);
  }

  // sanity: does our GET work on normal urls?
  @Test
  public void httpGetTest() throws IOException {
    // list James Allan's publications to ensure that our get requests do actually work
    Parameters getp = new Parameters();
    getp.set("id", 1918);
    HTTPUtil.Response response = HTTPUtil.get("http://ciir-publications.cs.umass.edu", "/pub/web/browse_authors.php", getp);
    assertEquals(200, response.status);
    assertEquals("OK", response.reason);
  }

  // sanity: does our POST work on normal urls?
  @Test
  public void httpPostTest() throws IOException {
    // search for 'allan' to ensure that our post requests do actually work
    Parameters form = new Parameters();
    form.set("years", "");
    form.set("authors", "allan");
    form.set("query", "");
    form.set("pubtype", "IR");

    HTTPUtil.Response response = HTTPUtil.post("http://ciir-publications.cs.umass.edu", "/pub/web/search.php", form);

    assertEquals(200, response.status);
    assertEquals("OK", response.reason);
  }

  @Test
  public void httpLogin() throws IOException {
    Parameters requestJSON = null;
    Parameters responseJSON = null;
    HTTPUtil.Response response = null;

    // expect a bad request with unregistered user
    requestJSON = new Parameters();
    requestJSON.set("user", "fake-user");
    response = HTTPUtil.post(url, "/login", requestJSON);
    assertEquals(HTTPError.BadRequest, response.status);
    assertEquals("No such user!", response.reason);

    // empty success response to register
    requestJSON = new Parameters();
    requestJSON.set("user", "register-me");
    response = HTTPUtil.post(url, "/register", requestJSON);
    assertEquals(200, response.status);
    assertEquals("OK", response.reason);
    responseJSON = Parameters.parseString(response.body);
    assertEquals(0, responseJSON.size());

    // token success response to login
    requestJSON = new Parameters();
    requestJSON.set("user", "register-me");
    response = HTTPUtil.post(url, "/login", requestJSON);
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
    response = HTTPUtil.get(url, "/tags", requestJSON);
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
