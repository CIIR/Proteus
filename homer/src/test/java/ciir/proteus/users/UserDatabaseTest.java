package ciir.proteus.users;

import ciir.proteus.users.impl.H2Database;
import org.junit.*;
import org.lemurproject.galago.tupleflow.FileUtility;
import org.lemurproject.galago.tupleflow.Parameters;
import org.lemurproject.galago.tupleflow.Utility;

import java.io.File;
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

    db = new H2Database(dbp);
    db.initDB();
  }

  @AfterClass
  public static void tearDown() throws Exception {
    db.close();
    Utility.deleteDirectory(folder);
  }

  @Test
  public void loginTest() {
    assertNull(db.login("missing-user"));
    db.register("user1");

    assertTrue(db.validUser("user1"));
    assertFalse(db.validSession("user1", "bogus-token"));

    for(int i=0; i<NumIterations; i++) {
      String session = db.login("user1");
      assertNotNull(session);

      assertTrue(db.validSession("user1", session));

      // try our getTags sql
      assertEquals(0, db.getTags("user1", "fake-resource").size());

      db.logout("user1", session);
      assertFalse(db.validSession("user1", session));
    }
  }
}
