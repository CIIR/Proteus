package ciir.proteus.users;

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

  @Test
  public void httpGetTest() throws IOException {
    // list James Allan's publications to ensure that our get requests do actually work
    Parameters getp = new Parameters();
    getp.set("id", 1918);
    HTTPUtil.Response response = HTTPUtil.get("http://ciir-publications.cs.umass.edu", "/pub/web/browse_authors.php", getp);
    assertEquals(200, response.status);
    assertEquals("OK", response.reason);
  }

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
  public void httpLogin() {

  }
}
