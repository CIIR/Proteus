package ciir.proteus.server;

import ciir.proteus.system.ProteusSystem;
import ciir.proteus.users.Credentials;
import ciir.proteus.users.error.DuplicateUser;
import ciir.proteus.users.error.NoTuplesAffected;
import org.lemurproject.galago.tupleflow.FileUtility;
import org.lemurproject.galago.utility.FSUtil;
import org.lemurproject.galago.utility.Parameters;
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

    public TestEnvironment() throws IOException, WebServerException, NoTuplesAffected, DuplicateUser, Exception {
        folder = FileUtility.createTemporaryDirectory();
        proteus = new ProteusSystem(testParams(folder));

        router = new HTTPRouter(proteus);

        int port = Utility.getFreePort();
        server = WebServer.start(port, router);
        url = server.getURL();

        createUser();
    }

    private void createUser() throws NoTuplesAffected, DuplicateUser {
        String user = "proteusTestUser";
        proteus.userdb.register(user);
        Parameters p = proteus.userdb.login(user);
        assertNotNull(p.get("token"));

        creds = new Credentials(p);
    }

    public static Parameters testParams(File tmpDir) {
        String dbpath = tmpDir.getPath() + "/users";
        Parameters dbp = Parameters.create();
        dbp.set("path", dbpath);
        dbp.set("user", "junit");
        dbp.set("pass", "");
        dbp.set("auto_server", "TRUE"); // allows us to query the DB outside this process

        Parameters content = Parameters.create();
        content.set("dir", Arrays.asList("web", "src/test/resources"));

        Parameters testSetup = Parameters.create();
        testSetup.set("defaultKind", "fake-kind");
        testSetup.set("kinds", Parameters.create());
        testSetup.set("userdb", dbp);
        testSetup.set("content", content);

        return testSetup;
    }

    public void close() throws WebServerException, IOException {
        server.stop();
        proteus.close();
        FSUtil.deleteDirectory(folder);
    }
}
