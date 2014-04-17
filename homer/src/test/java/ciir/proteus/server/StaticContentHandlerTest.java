package ciir.proteus.server;

import ciir.proteus.util.HTTPUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.lemurproject.galago.tupleflow.Parameters;
import org.lemurproject.galago.tupleflow.Utility;

import java.io.File;

import static org.junit.Assert.assertEquals;

/**
 * @author jfoley
 */
public class StaticContentHandlerTest {
  private static TestEnvironment env;

  @BeforeClass
  public static void setUp() throws Exception {
    env = new TestEnvironment();
  }

  @AfterClass
  public static void tearDown() throws Exception {
    env.close();
  }

  @Test
  public void testHandle() throws Exception {
    String url = env.url;

    HTTPUtil.Response response;

    response = HTTPUtil.get(url, "/", new Parameters());
    assertEquals(200, response.status);
    assertEquals(Utility.readFileToString(new File("web/index.html")), response.body);

    response = HTTPUtil.get(url, "/favicon.ico", new Parameters());
    assertEquals(HTTPError.NotFound, response.status);

    response = HTTPUtil.get(url, "/../evil/path.html", new Parameters());
    assertEquals(HTTPError.BadRequest, response.status);

    response = HTTPUtil.get(url, "/metadata/metadata.trectext", new Parameters());
    assertEquals(200, response.status);
    assertEquals(Utility.readFileToString(new File("src/test/resources/metadata/metadata.trectext")), response.body);
  }
}
