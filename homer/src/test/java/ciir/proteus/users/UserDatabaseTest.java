package ciir.proteus.users;

import ciir.proteus.users.error.BadSessionException;
import ciir.proteus.users.error.BadUserException;
import ciir.proteus.users.error.DBError;
import ciir.proteus.users.error.NoTuplesAffected;
import org.junit.*;
import org.lemurproject.galago.tupleflow.FileUtility;
import org.lemurproject.galago.tupleflow.Parameters;
import org.lemurproject.galago.tupleflow.Utility;

import java.io.File;
import java.util.Collections;
import java.util.List;
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
    db.initDB();
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
    assertFalse(db.validSession("user1", "bogus-token"));

    for(int i=0; i<NumIterations; i++) {
      String session = db.login("user1");
      assertNotNull(session);

      assertTrue(db.validSession("user1", session));

      // try our getTags sql
      assertEquals(0, db.getTags("user1", session, "fake-resource").size());

      db.logout("user1", session);
      assertFalse(db.validSession("user1", session));
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
        db.getTags(user, "no-such-session", "fake-resource");
        fail("Expected exception user="+user);
      } catch (BadSessionException e) {
        assertEquals(user, "real-user");
      } catch (BadUserException e) {
        assertEquals(user, "no-such-user");
      }

      try {
        db.addTag(user, "no-such-session", "fake-resource", "is-fake");
        fail("Expected exception user="+user);
      } catch (BadSessionException e) {
        assertEquals(user, "real-user");
      } catch (BadUserException e) {
        assertEquals(user, "no-such-user");
      }

      try {
        db.deleteTag(user, "no-such-session", "fake-resource", "is-fake");
        fail("Expected exception user="+user);
      } catch (BadSessionException e) {
        assertEquals(user, "real-user");
      } catch (BadUserException e) {
        assertEquals(user, "no-such-user");
      }

      // ignore logout to invalid users/sessions; for now
      db.logout(user, "no-such-session");
    }

  }

  @Test
  public void tagTest() throws DBError {
    String user = "tag-user";
    db.register(user);
    String token = db.login(user);
    assertNotNull(token);

    // test single tag
    db.addTag(user, token, "res1", "tag1");

    List<String> res1tags = db.getTags(user, token, "res1");
    Collections.sort(res1tags); // don't depend on db order
    assertArrayEquals(new String[]{"tag1"}, res1tags.toArray());

    // test multiple tags for the same thing
    db.addTag(user, token, "res2", "tag1");
    db.addTag(user, token, "res2", "tag2");
    db.addTag(user, token, "res2", "tag3");

    List<String> res2tags = db.getTags(user, token, "res2");
    Collections.sort(res2tags); // don't depend on db order
    assertArrayEquals(new String[]{"tag1", "tag2", "tag3"}, res2tags.toArray());

    // test delete!
    db.deleteTag(user, token, "res2", "tag2");

    res2tags = db.getTags(user, token, "res2");
    Collections.sort(res2tags); // don't depend on db order
    assertArrayEquals(new String[]{"tag1", "tag3"}, res2tags.toArray());

  }
}
