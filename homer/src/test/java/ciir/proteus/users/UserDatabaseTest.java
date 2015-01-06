package ciir.proteus.users;

import ciir.proteus.users.error.BadSessionException;
import ciir.proteus.users.error.BadUserException;
import ciir.proteus.users.error.DBError;
import ciir.proteus.users.error.DuplicateUser;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.lemurproject.galago.tupleflow.FileUtility;
import org.lemurproject.galago.utility.FSUtil;
import org.lemurproject.galago.utility.Parameters;

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
        folder = FileUtility.createTemporaryDirectory();
        String dbpath = folder.getPath() + "/users";
        log.info(dbpath);

        Parameters dbp = Parameters.create();
        dbp.set("path", dbpath);
        dbp.set("user", "junit");
        dbp.set("pass", "");
        dbp.set("auto_server", "TRUE");

        db = UserDatabaseFactory.instance(dbp);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        db.close();
        FSUtil.deleteDirectory(folder);
    }

    @Test
    public void loginTest() throws DBError {
        assertNull(db.login("missing-user"));
        db.register("user1");

        assertTrue(db.validUser("user1"));
        assertTrue(db.validUser("uSEr1")); // test case insensitivity

        // don't allow a 2nd registration
        try {
            db.register("user1");
            fail("Expected dupliacte registration exception");
        } catch (DuplicateUser e) {
            assertNotNull(e);
        } catch (Exception e) {
            fail("Wrong execption thrown: " + e.toString());
        }

        // make sure we can log in regardless of case
        Parameters p = db.login("usER1");
        assertNotNull(p.get("token"));
        assertNotNull(p.get("userid"));
        assertEquals("usER1", p.get("user"));

        Credentials user1 = new Credentials(p);
        assertTrue(db.validSession(user1));
        db.logout(user1);
        assertFalse(db.validSession(user1));

        // test bad token, correct user id
        Parameters badParam = Parameters.create();
        badParam.put("token", "bogus-token");
        badParam.put("userid", p.get("userid"));
        badParam.put("user", "user1");

        assertFalse(db.validSession(new Credentials(badParam)));

        // test correct token, bad user id
        badParam.clear();
        badParam.put("token", p.get("token"));
        badParam.put("userid", -123);
        badParam.put("user", "user1");

        assertFalse(db.validSession(new Credentials(badParam)));

        for (int i = 0; i < NumIterations; i++) {
            p = db.login("user1");
            assertNotNull(p.get("token"));
            assertNotNull(p.get("userid"));
            assertEquals("user1", p.get("user"));

            user1 = new Credentials(p);

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
        Parameters p = db.login("real-user");

        String[] users = {"no-such-user", "real-user"};
        Integer[] userids = {-2332, (int) p.getLong("userid")};

        for (int i = 0; i < 2; i++) {
            p.clear();
            p.put("token", "no-such-session");
            p.put("user", users[i]);
            p.put("userid", userids[i]);
            try {
                db.getTags(new Credentials(p), "fake-resource");
                fail("Expected exception user=" + users[i]);
            } catch (BadSessionException e) {
                assertEquals(users[i], "real-user");
            } catch (BadUserException e) {
                assertEquals(users[i], "no-such-user");
            }

            try {
                db.addTag(new Credentials(p), "fake-resource", "is-fake", 0);
                fail("Expected exception user=" + users[i]);
            } catch (BadSessionException e) {
                assertEquals(users[i], "real-user");
            } catch (BadUserException e) {
                assertEquals(users[i], "no-such-user");
            }

            try {
                db.deleteTag(new Credentials(p), "fake-resource", "is-fake");
                fail("Expected exception user=" + users[i]);
            } catch (BadSessionException e) {
                assertEquals(users[i], "real-user");
            } catch (BadUserException e) {
                assertEquals(users[i], "no-such-user");
            }

            // ignore logout to invalid users/sessions; for now
            db.logout(new Credentials(p));
        }

    }

    @Test
    public void tagTest() throws DBError {
        String user = "tag-user";
        db.register(user);
        Parameters p = db.login(user);

        Credentials cred = new Credentials(p);
        int tag_user_id = cred.userid;

        // test single tag
        db.addTag(cred, "res1", "tag1", 0);

        List<String> res1tags = db.getTags(cred, "res1");
        Collections.sort(res1tags); // don't depend on db order
        assertArrayEquals(new String[]{"*:tag1"}, res1tags.toArray());

        // test multiple tags for the same thing
        db.addTag(cred, "res2", "type1:tag1", 0);
        db.addTag(cred, "res2", "type1:tag2", 0);
        db.addTag(cred, "res2", "type2:tag3", 0);

        List<String> res2tags = db.getTags(cred, "res2");
        Collections.sort(res2tags); // don't depend on db order
        assertArrayEquals(new String[]{"type1:tag1", "type1:tag2", "type2:tag3"}, res2tags.toArray());

        // test delete!
        db.deleteTag(cred, "res2", "type1:tag2");

        // re-add, then delete testing tag case insensitivity
//    db.addTag(cred, "res2", "tag2");
//    db.deleteTag(cred, "res2", "tAG2");
//
//    res2tags = db.getTags(cred, "res2");
//    Collections.sort(res2tags); // don't depend on db order
//    assertArrayEquals(new String[]{"tag1", "tag3"}, res2tags.toArray());
        // test get multiple
        Map<String, List<String>> getres = db.getTags(cred, Arrays.asList("res1", "res2", "res3"));
        assertNotNull(getres);
        assertNotNull(getres.get("res1"));
        assertNotNull(getres.get("res2"));
        assertNotNull(getres.get("res3"));

        assertArrayEquals(new String[]{"*:tag1"}, getres.get("res1").toArray());
        assertArrayEquals(new String[]{"type1:tag1", "type2:tag3"}, getres.get("res2").toArray());
        assertTrue(getres.get("res3").isEmpty());

        // testing getAllTags
        // insert a new user
        user = "user2";
        db.register(user);
        p = db.login(user);

        cred = new Credentials(p);
        int user2_id = cred.userid;

        // test single tag
        db.addTag(cred, "res1", "user2_res1_tag1", 0);
        db.addTag(cred, "res1", "user2_res1_tag2", 0);
        db.addTag(cred, "res2", "user2_res2_tag1", 0);

        Map<String, Map<Integer, List<String>>> getAllTagsResult;

        getAllTagsResult = db.getAllTags(Arrays.asList("res1", "res2", "res3"));

        assertNotNull(getAllTagsResult);
        assertNotNull(getAllTagsResult.get("res1"));
        assertNotNull(getAllTagsResult.get("res2"));
        assertNotNull(getAllTagsResult.get("res3"));
        assertTrue(getAllTagsResult.get("res3").isEmpty());

        assertNotNull(getAllTagsResult.get("res1").get(tag_user_id));
        assertNotNull(getAllTagsResult.get("res1").get(user2_id));
        assertNotNull(getAllTagsResult.get("res2").get(tag_user_id));
        assertNotNull(getAllTagsResult.get("res2").get(user2_id));

        assertArrayEquals(new String[]{"*:tag1@0"}, getAllTagsResult.get("res1").get(tag_user_id).toArray());
        assertArrayEquals(new String[]{"type1:tag1@0", "type2:tag3@0"}, getAllTagsResult.get("res2").get(tag_user_id).toArray());

        assertArrayEquals(new String[]{"*:user2_res1_tag1@0", "*:user2_res1_tag2@0"}, getAllTagsResult.get("res1").get(user2_id).toArray());
        assertArrayEquals(new String[]{"*:user2_res2_tag1@0"}, getAllTagsResult.get("res2").get(user2_id).toArray());

        Map<Integer, List<String>> singleResourceResult = db.getAllTags("res1");

        assertNotNull(singleResourceResult.get(tag_user_id));
        assertNotNull(singleResourceResult.get(user2_id));

        assertArrayEquals(new String[]{"*:tag1@0"}, singleResourceResult.get(tag_user_id).toArray());
        assertArrayEquals(new String[]{"*:user2_res1_tag1@0", "*:user2_res1_tag2@0"}, singleResourceResult.get(user2_id).toArray());

    }
}
