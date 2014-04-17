package ciir.proteus.server;

import ciir.proteus.system.ProteusSystem;
import ciir.proteus.users.Credentials;
import ciir.proteus.users.error.NoTuplesAffected;
import org.lemurproject.galago.tupleflow.FileUtility;
import org.lemurproject.galago.tupleflow.Parameters;
import org.lemurproject.galago.tupleflow.Utility;
import org.lemurproject.galago.tupleflow.web.WebServer;
import org.lemurproject.galago.tupleflow.web.WebServerException;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertNotNull;

/**
 * @author jfoley
 */
public class TestEnvironment {
  public HTTPRouter router;
  public WebServer server;
  public File folder;
  public ProteusSystem proteus;
  public String url;

  public Credentials creds;

  public TestEnvironment() throws IOException, WebServerException, NoTuplesAffected {
    folder =  FileUtility.createTemporaryDirectory();
    proteus = new ProteusSystem(testParams(folder));

    router = new HTTPRouter(proteus);

    int port = Utility.getFreePort();
    server = WebServer.start(port, router);
    url = server.getURL();

    createUser();
  }

  private void createUser() throws NoTuplesAffected {
    String user = "proteusTestUser";
    proteus.userdb.register(user);
    String token = proteus.userdb.login(user);
    assertNotNull(token);

    creds = new Credentials(user, token);
  }

  public static Parameters testParams(File tmpDir) {
    String dbpath = tmpDir.getPath()+"/users";
    Parameters dbp = new Parameters();
    dbp.set("path", dbpath);
    dbp.set("user", "junit");
    dbp.set("pass", "");

    Parameters content = new Parameters();
    content.set("dir", Arrays.asList("web", "src/test/resources"));

    Parameters testSetup = new Parameters();
    testSetup.set("defaultKind", "fake-kind");
    testSetup.set("kinds", new Parameters());
    testSetup.set("userdb", dbp);
    testSetup.set("content", content);

    return testSetup;
  }

  public void close() throws WebServerException, IOException {
    server.stop();
    proteus.close();
    Utility.deleteDirectory(folder);
  }
}
