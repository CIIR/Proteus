package ciir.proteus.tools.apps;

import ciir.proteus.server.TestEnvironment;
import ciir.proteus.users.Credentials;
import ciir.proteus.users.error.DBError;
import ciir.proteus.users.error.DuplicateUser;
import ciir.proteus.users.error.NoTuplesAffected;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.lemurproject.galago.tupleflow.web.WebServerException;
import org.lemurproject.galago.utility.Parameters;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class GetResourcesForLabelFnTest {

  public static TestEnvironment env;

  @Before
  public void setup() throws IOException, WebServerException, NoTuplesAffected, DuplicateUser, Exception {
    env = new TestEnvironment();
  }

  @After
  public void tearDown() throws IOException, WebServerException {
    env.close();
  }

  @Test
  public void runTest() throws Exception {

    GetResourcesForLabelFn obj = new GetResourcesForLabelFn();
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    Parameters p = Parameters.create();
    p.put("path", env.folder + "/users");
    p.put("label", "type1:value1");
    p.put("user", "junit");

    // test no resources for label
    obj.run(p, new PrintStream(outContent));
    assertEquals("", outContent.toString());

    // insert some labels
    String user = "maxdcat";
    env.proteus.userdb.register(user);
    Parameters p1 = env.proteus.userdb.login(user);

    Credentials cred = new Credentials(p1);

    env.proteus.userdb.addTag(cred, "res1", "type1:value1", 0, null);
    env.proteus.userdb.addTag(cred, "res2", "type1:value1", 0, null);

    user = "newUser";
    env.proteus.userdb.register(user);
    Parameters p2 = env.proteus.userdb.login(user);

    cred = new Credentials(p2);
    env.proteus.userdb.addTag(cred, "res1", "type1:value1", 0, null);
    env.proteus.userdb.addTag(cred, "res4", "type1:value1", 0, null);
    env.proteus.userdb.addTag(cred, "res3", "type1:value2", 0, null);

    outContent = new ByteArrayOutputStream();
    obj.run(p, new PrintStream(outContent));
    assertEquals("res1\r\nres2\r\nres4\r\n", outContent.toString());

  }


}