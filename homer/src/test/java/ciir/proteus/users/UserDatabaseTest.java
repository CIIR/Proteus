package ciir.proteus.users;

import ciir.proteus.users.error.BadSessionException;
import ciir.proteus.users.error.BadUserException;
import ciir.proteus.users.error.DBError;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.lemurproject.galago.tupleflow.FileUtility;
import org.lemurproject.galago.tupleflow.Parameters;
import org.lemurproject.galago.tupleflow.Utility;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static org.junit.Assert.*;

/**
 * @author jfoley.
 */
public class UserDatabaseTest {
  public static UserDatabase db;
  public static File folder;
  private static Logger log = Logger.getLogger("tests");

  public static final int NumIterations = 10;

  @BeforeClass
  public static void setUp() throws Exception {
    folder =  FileUtility.createTemporaryDirectory();
    String dbpath = folder.getPath()+"/users";
    log.info(dbpath);

    Parameters dbp = new Parameters();
    dbp.set("path", dbpath);
    dbp.set("user", "junit");
    dbp.set("pass", "");

    db = UserDatabaseFactory.instance(dbp);
  }

  @AfterClass
  public static void tearDown() throws Exception {
    db.close();
    Utility.deleteDirectory(folder);
  }

  @Test
  public void loginTest() throws DBError {
    assertNull(db.login("missing-user"));
    db.register("user1");

    assertTrue(db.validUser("user1"));
    assertFalse(db.validSession(new Credentials("user1", "bogus-token")));

    for(int i=0; i<NumIterations; i++) {
      String session = db.login("user1");
      assertNotNull(session);

      Credentials user1 = new Credentials("user1", session);

      assertTrue(db.validSession(user1));

      // try our getTags sql
      assertEquals(0, db.getTags(user1, "fake-resource").size());

      db.logout(user1);
      assertFalse(db.validSession(user1));
    }
  }

  @Test
  public void expectBadSession() throws DBError {
    db.register("real-user");
    String token = db.login("real-user");
    assertNotNull(token);

    String[] users = {"no-such-user", "real-user"};

    for(String user : users) {
      try {
        db.getTags(new Credentials(user, "no-such-session"), "fake-resource");
        fail("Expected exception user="+user);
      } catch (BadSessionException e) {
        assertEquals(user, "real-user");
      } catch (BadUserException e) {
        assertEquals(user, "no-such-user");
      }

      try {
        db.addTag(new Credentials(user, "no-such-session"), "fake-resource", "is-fake");
        fail("Expected exception user="+user);
      } catch (BadSessionException e) {
        assertEquals(user, "real-user");
      } catch (BadUserException e) {
        assertEquals(user, "no-such-user");
      }

      try {
        db.deleteTag(new Credentials(user, "no-such-session"), "fake-resource", "is-fake");
        fail("Expected exception user="+user);
      } catch (BadSessionException e) {
        assertEquals(user, "real-user");
      } catch (BadUserException e) {
        assertEquals(user, "no-such-user");
      }

      // ignore logout to invalid users/sessions; for now
      db.logout(new Credentials(user, "no-such-session"));
    }

  }

  @Test
  public void tagTest() throws DBError {
    String user = "tag-user";
    db.register(user);
    String token = db.login(user);
    assertNotNull(token);

    Credentials cred = new Credentials(user, token);

    // test single tag
    db.addTag(cred, "res1", "tag1");

    List<String> res1tags = db.getTags(cred, "res1");
    Collections.sort(res1tags); // don't depend on db order
    assertArrayEquals(new String[]{"tag1"}, res1tags.toArray());

    // test multiple tags for the same thing
    db.addTag(cred, "res2", "tag1");
    db.addTag(cred, "res2", "tag2");
    db.addTag(cred, "res2", "tag3");

    List<String> res2tags = db.getTags(cred, "res2");
    Collections.sort(res2tags); // don't depend on db order
    assertArrayEquals(new String[]{"tag1", "tag2", "tag3"}, res2tags.toArray());

    // test delete!
    db.deleteTag(cred, "res2", "tag2");

    res2tags = db.getTags(cred, "res2");
    Collections.sort(res2tags); // don't depend on db order
    assertArrayEquals(new String[]{"tag1", "tag3"}, res2tags.toArray());

    // test get multiple
    Map<String,List<String>> getres = db.getTags(cred, Arrays.asList("res1", "res2", "res3"));
    assertNotNull(getres);
    assertNotNull(getres.get("res1"));
    assertNotNull(getres.get("res2"));
    assertNotNull(getres.get("res3"));

    assertArrayEquals(new String[]{"tag1"}, getres.get("res1").toArray());
    assertArrayEquals(new String[]{"tag1", "tag3"}, getres.get("res2").toArray());
    assertTrue(getres.get("res3").isEmpty());

  }
}
