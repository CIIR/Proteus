package ciir.proteus.users;

import ciir.proteus.users.error.*;
import org.junit.*;
import org.lemurproject.galago.tupleflow.FileUtility;
import org.lemurproject.galago.utility.FSUtil;
import org.lemurproject.galago.utility.Parameters;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

import static org.junit.Assert.*;

/**
 * @author jfoley, michaelz
 */
public class UserDatabaseTest {

  public static UserDatabase db;
  public static File folder;
  private static Logger log = Logger.getLogger("tests");
  public static final int NumIterations = 10;

  @Before
  public void setUp() throws Exception {
    folder = FileUtility.createTemporaryDirectory();
    String dbpath = folder.getPath() + "/users";
    log.info(dbpath);

    Parameters dbp = Parameters.create();
    dbp.set("path", dbpath);
    dbp.set("user", "junit");
    dbp.set("pass", "");
    dbp.set("AUTO_SERVER", "TRUE");


    db = UserDatabaseFactory.instance(dbp);
  }

  @After
  public void tearDown() throws Exception {
    db.close();
    FSUtil.deleteDirectory(folder);
  }

  @Test
  public void loginTest() throws DBError, SQLException {
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
    List<Parameters> corpora = new ArrayList<Parameters>();
    corpora = p.getAsList("corpora", Parameters.class);
    // MCZ: 10/2015 - there is always a default corpus
    assertEquals(1, corpora.size());

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

    // add some corpora to make sure they're returned
    db.createCorpus("a", "user");
    db.createCorpus("b", "user");
    db.createCorpus("c", "user");
    db.createCorpus("a b c", "user");
    p = db.login("user1");
    assertNotNull(p.get("token"));
    assertNotNull(p.get("userid"));
    assertEquals("user1", p.get("user"));

    corpora = p.getAsList("corpora", Parameters.class);
    assertEquals("a", corpora.get(0).getString("name"));
    assertEquals("a b c", corpora.get(1).getString("name"));
    assertEquals("b", corpora.get(2).getString("name"));
    assertEquals("c", corpora.get(3).getString("name"));

    assertEquals(p.get("settings", ""), "{ \"num_entities\" : 10 }");
    Parameters np = Parameters.create();
    try {
      np = Parameters.parseString(p.get("settings", ""));
    } catch (IOException e) {
      e.printStackTrace();
    }
    assertEquals(np.get("num_entities", -1), 10);

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
        db.addTag(new Credentials(p), "fake-resource", "is-fake", 0, null);
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
    db.addTag(cred, "res1", "tag1", 0, null);

    List<String> res1tags = db.getTags(cred, "res1");
    Collections.sort(res1tags); // don't depend on db order
    assertArrayEquals(new String[]{"*:tag1"}, res1tags.toArray());

    // test multiple tags for the same thing
    db.addTag(cred, "res2", "type1:tag1", 0, null);
    db.addTag(cred, "res2", "type1:tag2", 0, null);
    db.addTag(cred, "res2", "type2:tag3", 0, "comment: res2 type2:tag3");

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
    db.addTag(cred, "res1", "user2_res1_tag1", 0, null);
    db.addTag(cred, "res1", "user2_res1_tag2", 0, null);
    db.addTag(cred, "res2", "user2_res2_tag1", 0, null);

    Map<String, Map<Integer, Map<String, String>>> getAllTagsResult;

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

    assertEquals(getAllTagsResult.get("res1").get(tag_user_id).size(), 1);
    assertEquals(getAllTagsResult.get("res1").get(tag_user_id).get("*:tag1"), "0:");

    assertEquals(getAllTagsResult.get("res2").get(tag_user_id).size(), 2);
    assertEquals(getAllTagsResult.get("res2").get(tag_user_id).get("type1:tag1"), "0:");
    assertEquals(getAllTagsResult.get("res2").get(tag_user_id).get("type2:tag3"), "0:comment: res2 type2:tag3");

    assertEquals(getAllTagsResult.get("res1").get(user2_id).size(), 2);
    assertEquals(getAllTagsResult.get("res1").get(user2_id).get("*:user2_res1_tag1"), "0:");
    assertEquals(getAllTagsResult.get("res1").get(user2_id).get("*:user2_res1_tag2"), "0:");

    assertEquals(getAllTagsResult.get("res2").get(user2_id).size(), 1);
    assertEquals(getAllTagsResult.get("res2").get(user2_id).get("*:user2_res2_tag1"), "0:");

    Map<Integer, Map<String, String>> singleResourceResult = db.getAllTags("res1");

    assertNotNull(singleResourceResult.get(tag_user_id));
    assertNotNull(singleResourceResult.get(user2_id));

    assertEquals(singleResourceResult.get(tag_user_id).size(), 1);
    assertEquals(singleResourceResult.get(tag_user_id).get("*:tag1"), "0:");

    assertEquals(singleResourceResult.get(user2_id).size(), 2);
    assertEquals(singleResourceResult.get(user2_id).get("*:user2_res1_tag1"), "0:");
    assertEquals(singleResourceResult.get(user2_id).get("*:user2_res1_tag2"), "0:");

  }

  @Test
  public void getUsersTest() throws DBError {

    db.register("test-user-1");
    db.register("user2");
    db.register("user1");
    db.register("real-user");
    db.register("tag-user");

    Map<String, String> results = new HashMap<>();

    results = db.getUsers();

    assertTrue(results.size() == 5);
    assertTrue(results.values().contains("test-user-1"));
    assertTrue(results.values().contains("user1"));
    assertTrue(results.values().contains("tag-user"));
    assertTrue(results.values().contains("user2"));
    assertTrue(results.values().contains("real-user"));

  }

  @Test
  public void getAllTagsForResourcesTest() throws DBError {

    db.register("user1");
    Parameters p = db.login("user1");
    Credentials user1Cred = new Credentials(p);
    db.addTag(user1Cred, "res1", "tag1", 1, null);
    db.addTag(user1Cred, "res1", "tag2", 2, null);
    db.addTag(user1Cred, "res2", "tag1", 1, "user1 res2 tag1");
    db.addTag(user1Cred, "res2", "tag3", 3, null);


    db.register("user2");
    Parameters p2 = db.login("user2");
    Credentials user2Cred = new Credentials(p2);
    db.addTag(user2Cred, "res1", "tag1", 10, null);
    db.addTag(user2Cred, "res1", "tag4", 20, null);
    db.addTag(user2Cred, "res2", "tag1", 10, null);
    db.addTag(user2Cred, "res4", "tag3", 30, null);

    Map<String, Map<String, List<String>>> results;

    results = db.getAllTagsForResources(Arrays.asList("res1"));

    assertNotNull(results);
    assertTrue(results.size() == 1);
    assertNotNull(results.get("res1"));
    assertTrue(results.get("res1").get("*:tag1").size() == 2);
    assertTrue(results.get("res1").get("*:tag1").contains("1:1:"));
    assertTrue(results.get("res1").get("*:tag1").contains("2:10:"));

    results = db.getAllTagsForResources(Arrays.asList("res2", "res3", "res4"));

    assertNotNull(results);
    assertTrue(results.size() == 2);
    assertNotNull(results.get("res2"));

    assertTrue(results.get("res2").get("*:tag1").size() == 2);
    assertTrue(results.get("res2").get("*:tag1").contains("1:1:user1 res2 tag1"));
    assertTrue(results.get("res2").get("*:tag1").contains("2:10:"));

    assertTrue(results.get("res2").get("*:tag3").size() == 1);
    assertTrue(results.get("res2").get("*:tag3").contains("1:3:"));

    assertNull(results.get("res3"));

    assertNotNull(results.get("res4"));
    assertTrue(results.get("res4").get("*:tag3").size() == 1);
    assertTrue(results.get("res4").get("*:tag3").contains("2:30:"));

    results = db.getAllTagsForResources(Arrays.asList("%"));
    assertTrue(results.size() == 0);
  }

  @Test
  public void updateTagTest() throws DBError {

    db.register("user1");
    Parameters p = db.login("user1");
    Credentials user1Cred = new Credentials(p);
    db.addTag(user1Cred, "res1", "tag1", 1, null);
    db.addTag(user1Cred, "res1", "tag2", 2, "user1 res1 tag2");

    Map<String, Map<String, List<String>>> results;

    results = db.getAllTagsForResources(Arrays.asList("res1"));

    assertNotNull(results);
    assertTrue(results.size() == 1);
    assertNotNull(results.get("res1"));
    assertTrue(results.get("res1").get("*:tag1").size() == 1);
    assertTrue(results.get("res1").get("*:tag1").contains("1:1:"));
    assertTrue(results.get("res1").get("*:tag2").contains("1:2:user1 res1 tag2"));

    db.updateTag(user1Cred, "res1", "tag1", 10, "new comment");
    db.updateTag(user1Cred, "res1", "tag2", 20, "updated!");

    results = db.getAllTagsForResources(Arrays.asList("res1"));

    assertTrue(results.get("res1").get("*:tag1").contains("1:10:new comment"));
    assertTrue(results.get("res1").get("*:tag2").contains("1:20:updated!"));

    db.updateTag(user1Cred, "res1", "tag2", 2, null);

    results = db.getAllTagsForResources(Arrays.asList("res1"));

    assertTrue(results.get("res1").get("*:tag1").contains("1:10:new comment"));
    assertTrue(results.get("res1").get("*:tag2").contains("1:2:"));

  }

  // tests for corpus creation
  @Test
  public void testCreateCorpus() throws NoTuplesAffected, DuplicateCorpus, SQLException {

    db.createCorpus("test corpus 1", "user");
    // test duplicate
    try{
      db.createCorpus("test corpus 1", "user2");
      fail("Should throw duplicate corpus exception");
    } catch (DuplicateCorpus e){
      // OK
    } catch (Exception e){
      fail("Should throw duplicate corpus exception");
    }

    // test case insensitivity
    try{
      db.createCorpus("Test Corpus 1", "user2");
      fail("Should throw duplicate corpus exception");
    } catch (DuplicateCorpus e){
      // OK
    } catch (Exception e){
      fail("Should throw duplicate corpus exception");
    }

  }

  @Test
  public void getAllCorporaTest() throws DBError, SQLException {

    db.createCorpus("a", "user");
    db.createCorpus("b", "user");
    db.createCorpus("c", "user");
    db.createCorpus("a b c", "user");

    Parameters p = Parameters.create();
    p = db.getAllCorpora();

    List<Parameters> corpora = new ArrayList<Parameters>();

    corpora = p.getAsList("corpora", Parameters.class);

    assertEquals("a", corpora.get(0).getString("name"));
    assertEquals("a b c", corpora.get(1).getString("name"));
    assertEquals("b", corpora.get(2).getString("name"));
    assertEquals("c", corpora.get(3).getString("name"));
  }

  @Test
  public void updateUserSettingsTest() throws DBError {

    String user = "testUser";
    db.register(user);
    Parameters p = db.login(user);
    assertEquals(p.get("settings", ""), "{ \"num_entities\" : 10 }");

    Credentials cred = new Credentials(p);

    db.updateUserSettings(cred, "{ \"test\" : 123 }");

    // re-log in, that returns the settings
    p = db.login(user);

    Parameters np = Parameters.create();
    try {
      np = Parameters.parseString(p.get("settings", ""));
    } catch (IOException e) {
      e.printStackTrace();
    }
    assertEquals(np.get("test", -1), 123);

  }

  @Test
  public void resourceRankTest() throws DBError, SQLException {

    db.register("user1");
    db.register("user2");

    Parameters p = db.login("user1");
    Credentials cred = new Credentials(p);

    db.createCorpus("test corpus 1", "user");
    Integer corpus1 = 1;
    Integer corpus2 = 2;
    Integer queryid1 = 1;

    String res1 = "resource1";

    db.upsertResourceRating(cred, res1, cred.userid, corpus1, 4, queryid1);

    Parameters ratings = db.getResourceRatings(res1, corpus1 );
    assertEquals(ratings.get("aveRating", -1), 4);

    // get ratings for that resource in a corpus which it hasn't been rated in
    ratings = db.getResourceRatings(res1, corpus2 );
    assertEquals(ratings.get("aveRating", -1), 0);
    assertEquals(ratings.getAsList("ratings").size(), 0);

    // test update of rating
    db.upsertResourceRating(cred, res1, cred.userid, corpus1, 2, queryid1);
    ratings = db.getResourceRatings(res1, corpus1 );
    assertEquals(ratings.get("aveRating", -1), 2);
    assertEquals(ratings.getAsList("ratings").size(), 1);

    // have a 2nd user rate the resource
    p = db.login("user2");
    cred = new Credentials(p);

    db.upsertResourceRating(cred, res1, cred.userid, corpus1, 4, queryid1);
    ratings = db.getResourceRatings(res1,corpus1 );
    assertEquals(ratings.get("aveRating", -1), 3);
    assertEquals(ratings.getAsList("ratings").size(), 2);

    // rate another resource, make sure it doesn't change this rating.
    db.upsertResourceRating(cred, "different resource", cred.userid, corpus1, 4, queryid1);
    ratings = db.getResourceRatings(res1, corpus1);
    assertEquals(ratings.get("aveRating", -1), 3);
    assertEquals(ratings.getAsList("ratings").size(), 2);

    // update user2's rating to be zero - which should be ignored
    db.upsertResourceRating(cred, res1, cred.userid, corpus1, 0, queryid1);
    ratings = db.getResourceRatings(res1, corpus1);
    assertEquals(ratings.get("aveRating", -1), 2);
    assertEquals(ratings.getAsList("ratings").size(), 1);

    // resource with no ratings
    ratings = db.getResourceRatings("i don't exist", corpus1);
    assertEquals(ratings.get("aveRating", -1), 0);
    assertEquals(ratings.getAsList("ratings").size(), 0);

  }


  @Test
  public void resourceRank2Test() throws DBError, SQLException {

    db.register("user1");
    db.register("user2");

    Parameters p = db.login("user1");
    Credentials cred = new Credentials(p);

    db.createCorpus("test corpus 2", "user");
    Integer corpus1 = 1;
    Integer corpus2 = 2;
    Integer subcorpus1 = 1;
    Integer queryid1 = 1;

    String res1 = "resource1";
    String res1_pg1 = "resource1_1";

    db.addVoteForResource(cred, res1, corpus1, subcorpus1, queryid1);
    db.addVoteForResource(cred, res1_pg1, corpus1, subcorpus1, queryid1);

    Parameters labels = db.getResourceRatings2(res1, corpus1);
    List<Parameters> recs = new ArrayList<Parameters>();
    recs = labels.getAsList("labels");
    assertEquals(2, recs.size());

    // test for corpus that doesn't have anything
    labels = db.getResourceRatings2(res1, corpus2);
    recs = labels.getAsList("labels");
    assertEquals(0, recs.size());

    // have a 2nd user rate the resource
    p = db.login("user2");
    cred = new Credentials(p);

    db.addVoteForResource(cred, res1, corpus1, subcorpus1, queryid1);
    labels = db.getResourceRatings2(res1,corpus1 );
    recs = labels.getAsList("labels");
    assertEquals(3, recs.size());

  }

  @Test
  public void getResourcesForCorpusTest() throws DBError, SQLException {

    db.createCorpus("a", "user");
    db.createCorpus("b", "user");

    Integer corpus1 = 1;
    Integer corpus2 = 2;
    Integer queryid1 = 1;

    db.register("user1");
    db.register("user2");

    Parameters p = db.login("user1");
    Credentials cred = new Credentials(p);

    String res1 = "a_resource";
    String res2 = "b_resource";
    String res3 = "c_resource";
    String res4 = "d_resource";

    Integer subcorpus1 = 1;

    db.addVoteForResource(cred, res1, corpus1, subcorpus1, queryid1);
    db.addVoteForResource(cred, res3, corpus1, subcorpus1, queryid1);

    db.addVoteForResource(cred, res2, corpus2, subcorpus1, queryid1);

    List<String> results = new ArrayList<>();

    results = db.getResourcesForCorpus(cred.userid, corpus1, -1, -1) ;
    assertArrayEquals(new String[]{res1, res3}, results.toArray());

    results = db.getResourcesForCorpus( cred.userid, corpus1, 2, 1) ;
    assertArrayEquals(new String[]{res3}, results.toArray());

    results = db.getResourcesForCorpus( cred.userid, corpus1, 20, 10) ;
    assertEquals(0, results.toArray().length);

    results = db.getResourcesForCorpus( cred.userid, corpus2, -1, -1) ;
    assertArrayEquals(new String[]{res2}, results.toArray());

    p = db.login("user2");
    cred = new Credentials(p);

    results = db.getResourcesForCorpus(cred.userid, corpus1, -1, -1) ;
    assertArrayEquals(new String[]{res1, res3}, results.toArray());

    // now give it a non-zero so it'll be returned
    db.addVoteForResource(cred, res2, corpus1,  subcorpus1, queryid1);

    results = db.getResourcesForCorpus( cred.userid, corpus1, -1, -1) ;
    assertArrayEquals(new String[]{res1, res2, res3}, results.toArray());

    // test the wrapper version
    results = db.getAllResourcesForCorpus(cred.userid, corpus1) ;
    assertArrayEquals(new String[]{res1, res2, res3}, results.toArray());

    // now add some notes so we see if they are returned
    // add a note to a different corpus - shouldn't change results
    db.insertNote(cred, corpus2, res4, "{ data: '123' }");
    results = db.getAllResourcesForCorpus(cred.userid, corpus1) ;
    assertArrayEquals(new String[]{res1, res2, res3}, results.toArray());

    // now add it to the corpus we're getting the resources for
    Integer noteid = db.insertNote(cred, corpus1, res4, "{ data: '123' }");
    results = db.getAllResourcesForCorpus(cred.userid, corpus1) ;
    assertArrayEquals(new String[]{res1, res2, res3, res4, res4 + "_" + noteid}, results.toArray());

 }

  @Test
  public void getResourcesForCorpusByQueryTest() throws DBError, SQLException {

    db.createCorpus("a", "user");
    db.createCorpus("b", "user");
    Integer corpus1 = 1;
    Integer corpus2 = 2;
    Integer corpus3 = 3;

    db.register("user1");
    db.register("user2");

    Parameters p = db.login("user1");
    Credentials cred = new Credentials(p);

    String res1 = "a_resource";
    String res2 = "b_resource";
    String res3 = "c_resource";
    String res4 = "d_resource";

    String query_1 = "query one";
    String kind_1 = "kind1";
    Integer subcorpus1 = 1;

    Integer query_1_id = db.insertQuery(null, corpus1, query_1, kind_1);

    //db.insertQueryResourceXref(null, res1, corpus1, query_1_id);
    // TODO ^^^ should be private it's called inside upsertResourceRating - but that makes testing it stand alone kinda hard.

    db.addVoteForResource(cred, res1, corpus1, subcorpus1, query_1_id);
    db.addVoteForResource(cred, res3, corpus1, subcorpus1, query_1_id);
    // rate resource for another corpus
    db.addVoteForResource(cred, res4, corpus2, subcorpus1, query_1_id);

    Parameters results = db.getResourcesForCorpusByQuery(corpus1);

    assertEquals(1, results.size());
    List<Parameters> queryList = results.getAsList("queries");
    assertEquals(1, queryList.size());
    Parameters query = queryList.get(0);
    List<String> res = query.getAsList("resources");
    assertEquals(2, res.size());
    assertEquals(res1, res.get(0));
    assertEquals(res3, res.get(1));

    // add resources for a 2nd query
    String query_2 = "query two";
    Integer query_2_id = db.insertQuery(null, corpus1, query_2, kind_1);
    db.addVoteForResource(cred, res4, corpus1, subcorpus1, query_2_id);

    results = db.getResourcesForCorpusByQuery(corpus1);

    assertEquals(results.size(), 1);
    queryList = results.getAsList("queries");
    assertEquals(2, queryList.size());

    query = queryList.get(0);
    res = query.getAsList("resources");
    assertEquals(2, res.size());
    assertEquals(res1, res.get(0));
    assertEquals(res3, res.get(1));

    query = queryList.get(1);
    res = query.getAsList("resources");
    assertEquals(1, res.size());
    assertEquals(res4, res.get(0));

    // no results
    results = db.getResourcesForCorpusByQuery(corpus3);
    assertEquals(0, results.size());
  }

  @Test
  public void noteTest() throws DBError, IOException, SQLException {
    String user = "new-user";
    db.register(user);
    Parameters p = db.login(user);

    Credentials cred = new Credentials(p);

    db.createCorpus("a", "user");
    db.createCorpus("b", "user");
    Integer corpus1 = 1;
    Integer corpus2 = 2;

    // no notes for a resource
    Parameters notes = db.getNotesForResource("i do not exist", corpus1);
    assertEquals(notes.get("total", -1), 0);

    // add notes for a resource
    String resource = "res1";
    Integer id = db.insertNote(cred, corpus1, resource, "{ \"data\": \"123\" }");
    assertEquals(id.longValue(), 1L);
    id = db.insertNote(cred, corpus1, resource, "{ \"data\": \"abc\" }");

    // add note for a different resource
    String resource2 = "res2";
    id = db.insertNote(cred, corpus1, resource2, "{ \"name\": \"res2\" }");

    notes = db.getNotesForResource(resource, corpus1);
    assertEquals(notes.get("total", -1), 2);
    List<Parameters> arr = notes.getAsList("rows");

    // we can't be sure of the order, so check both
    Parameters data_123 = Parameters.create();
    data_123.put("data", "123");
    Parameters data_abc = Parameters.create();
    data_abc.put("data", "abc");

    assertTrue(arr.contains(data_123));
    assertTrue(arr.contains(data_abc));

    // test update
    db.updateNote(cred, corpus1, 1, "{ \"new\" : \"data\" }");
    notes = db.getNotesForResource(resource, corpus1);
    assertEquals(notes.get("total", -1), 2);
    arr = notes.getAsList("rows");

    Parameters new_data = Parameters.create();
    new_data.put("new", "data");

    assertTrue(arr.contains(new_data));
    assertTrue(arr.contains(data_abc));


    // test delete
    db.deleteNote(cred, corpus1, 1);
    notes = db.getNotesForResource( resource, corpus1);
    assertEquals(notes.get("total", -1), 1);
    arr = notes.getAsList("rows");

    assertTrue(arr.contains(data_abc));

  }

  @Test
  public void noteHistoryTest() throws DBError, IOException, SQLException, InterruptedException, ParseException {
    String user = "new-user";
    db.register(user);
    Parameters p = db.login(user);

    Credentials cred = new Credentials(p);

    db.createCorpus("a", "user");
    db.createCorpus("b", "user");
    Integer corpus1 = 1;
    Integer corpus2 = 2;

    Parameters notes = Parameters.create();

    // add notes for a resource
    String resource = "res1";
    Integer id = db.insertNote(cred, corpus1, resource, "{ \"" + resource + "\": \"1\" }");
    // wait a bit so we have different time stamps
    Thread.sleep(2000);

    id = db.insertNote(cred, corpus1, resource, "{ \"" + resource + "\": \"2\" }");

    Thread.sleep(2000);

    // add note for a different resource
    String resource2 = "res2";
    id = db.insertNote(cred, corpus1, resource2, "{ \"" + resource2 + "\": \"1\" }");

    // add note for different corpus - this will not get returned
    id = db.insertNote(cred, corpus2, resource2, "{ \"" + resource2 + "\": \"2\" }");

    Thread.sleep(2000);

    // update the first note
    db.updateNote(cred, corpus1, 1, "{ \"" + resource + "\": \"1\" }");

    notes = db.getNotesForCorpus(corpus1);
    List<Parameters> arr = notes.getAsList("rows");

    assertEquals(3, arr.size());

    // check that they're in the correct order
    assertEquals("1", arr.get(0).get(resource, "?"));
    assertEquals("1", arr.get(1).get(resource2, "?"));
    assertEquals("2", arr.get(2).get(resource, "?"));

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSSSSSSSS");
    Date date = (Date)formatter.parse(arr.get(0).get("dttm", "?"));
    long time1 = date.getTime();
    date = (Date)formatter.parse(arr.get(1).get("dttm", "?"));
    long time2 = date.getTime();
    date = (Date)formatter.parse(arr.get(2).get("dttm", "?"));
    long time3 = date.getTime();

    assertTrue(time1 > time2);
    assertTrue(time2 > time3);

  }

  @Test
  public void testInsertQuery() throws DBError {

    Integer corpus_id_1 = 1;
    String kind_1 = "kind1";
    String query_1 = "query1";

    Integer id = db.insertQuery(null, corpus_id_1, query_1, kind_1);
    assertTrue(id == 1);

    // test that queries are trimmed

    id = db.insertQuery(null, corpus_id_1, " " + query_1 + " ", kind_1);
    assertTrue(id == 1);

    // test case sensitivity
    id = db.insertQuery(null, corpus_id_1, query_1.toLowerCase(), kind_1);
    assertTrue(id == 1);
    id = db.insertQuery(null, corpus_id_1, query_1.toUpperCase(), kind_1);
    assertTrue(id == 1);

    // same query, diff corpus
    Integer corpus_id_2 = 2;
    id = db.insertQuery(null, corpus_id_2, query_1.toUpperCase(), kind_1);
    assertTrue(id == 2);

    // new query
    String query_2 = "query2";
    id = db.insertQuery(null, corpus_id_1, query_2, kind_1);
    assertTrue(id == 3);

    // diff kind
    String kind_2 = "kind2";
    id = db.insertQuery(null, corpus_id_1, query_1, kind_2);
    assertTrue(id == 4);

    // query too big
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 500; i++){
      sb.append(query_1);
    }
    id = db.insertQuery(null, corpus_id_1, sb.toString(), kind_2);
    assertTrue(id == 5);

    // make sure duplicate check works
    id = db.insertQuery(null, corpus_id_1, sb.toString(), kind_2);
    assertTrue(id == 5);


  }

  @Test
  public void testQueryResourceXref() throws DBError, SQLException {

    String res1 = "res1";
    Integer corpus1 = 1;
    Integer query1 = 1;

    db.insertQueryResourceXref(null, res1, corpus1, query1);

    Connection conn = db.getConnection();
    ResultSet results = conn.createStatement().executeQuery("SELECT * FROM query_res_xref");

    int count = 0;
    while (results.next()) {
      count++;
    }
    assert (count == 1);

    // test we didn't insert a dup.
    db.insertQueryResourceXref(null, res1, corpus1, query1);

    results = conn.createStatement().executeQuery("SELECT * FROM query_res_xref");

    count = 0;
    while (results.next()) {
      count++;
    }
    assert (count == 1);

    // add a new row
    Integer query2 = 2;
    db.insertQueryResourceXref(null, res1, corpus1, query2);

    results = conn.createStatement().executeQuery("SELECT * FROM query_res_xref");

    count = 0;
    while (results.next()) {
      count++;
    }
    assert (count == 2);

    conn.close();

  }

  @Test
  public void testUpsertSubCorpus() throws DBError, SQLException {

    Parameters p = Parameters.create();

    p.put("corpusid", 1); // default corpus inserted when Proteus starts

    // insert two subcorpora
    Parameters sc = Parameters.create();
    List<Parameters> plist = new ArrayList<Parameters>();
    sc.put("name", "sc1");
    plist.add(sc);
    sc = Parameters.create();
    sc.put("name", "sc2");
    plist.add(sc);

    p.set("subcorpora", plist);

    Parameters ret = db.upsertSubCorpus(p);
    plist = ret.getAsList("subcorpora");
    p = plist.get(0);
    assertEquals(p.get("name", "?"), "sc1");
    p = plist.get(1);
    assertEquals(p.get("name", "?"), "sc2");

    // this is a bit of overkill, but check that the data is actually in the DB
    Connection conn = db.getConnection();
    ResultSet results = conn.createStatement().executeQuery("SELECT subcorpus, id FROM subcorpora ORDER BY subcorpus");

    Integer id1 = -1;
    Integer id2 = -1;

    int count = 0;
    results.next();
    assertEquals("sc1", results.getString(1));
    id1 = results.getInt(2);
    count++;
    results.next();
    assertEquals("sc2", results.getString(1));
    id2 = results.getInt(2);
    count++;
    assert(results.next() == false); // no more records
    assert (count == 2);

    // update the name of them
    p = Parameters.create();

    p.put("corpusid", 1);

    // insert two subcorpora
    sc = Parameters.create();
    plist = new ArrayList<Parameters>();
    sc.put("name", "z");
    sc.put("id", id1);
    plist.add(sc);
    sc = Parameters.create();
    sc.put("name", "a");
    sc.put("id", id2);
    plist.add(sc);

    p.set("subcorpora", plist);

    ret = db.upsertSubCorpus(p);

    plist = ret.getAsList("subcorpora");
    p = plist.get(0);
    assertEquals(p.get("name", "?"), "a");
    p = plist.get(1);
    assertEquals(p.get("name", "?"), "z");

    results = conn.createStatement().executeQuery("SELECT subcorpus, id FROM subcorpora ORDER BY subcorpus");

    count = 0;
    results.next();
    assertEquals("a", results.getString(1));
    assert(id2 == results.getInt(2));
    count++;
    results.next();
    assertEquals("z", results.getString(1));
    assert(id1 == results.getInt(2));
    count++;
    assert(results.next() == false); // no more records
    assert (count == 2);


  }

  @Test
  public void getResourcesForSubcorporaTest() throws DBError, SQLException {
    String user = "maxdcat";
    db.register(user);
    Parameters p = db.login(user);

    Credentials cred = new Credentials(p);
    int userid = cred.userid;

    p = Parameters.create();

    p.put("corpusid", 1); // default corpus inserted when Proteus starts

    // insert a subcorpus
    Parameters sc = Parameters.create();
    List<Parameters> plist = new ArrayList<Parameters>();
    sc.put("name", "sc1");
    plist.add(sc);
    p.set("subcorpora", plist);

    Parameters ret = db.upsertSubCorpus(p);

    // associate a resource with that subcorpus
    String res1 = "resource_1";
    Integer corpus_id = 1;
    Integer subcorpus_1_id = 1;
    Integer query_id = 1;
    db.addVoteForResource(cred, res1, corpus_id, subcorpus_1_id, query_id);

    List<Long> subcorpora = new ArrayList<>();
    subcorpora.add(1L);
    List<String> resources = db.getResourcesForSubcorpora(userid, corpus_id, subcorpora);
    assertArrayEquals(new String[]{res1}, resources.toArray());

    // add some more resources
    String res2 = "resource_2";
    db.addVoteForResource(cred, res2, corpus_id, subcorpus_1_id, query_id);

    resources = db.getResourcesForSubcorpora(userid, corpus_id, subcorpora);
    assertArrayEquals(new String[]{res1, res2}, resources.toArray());

    // add a new subcorpus, existing recourse
    Integer subcorpus_2_id = 2;
    db.addVoteForResource(cred, res1, corpus_id, subcorpus_2_id, query_id);

    // add the second subcorpus the list of subcorpora to search
    subcorpora.add(2L);

    // should only return one instance of res1
    resources = db.getResourcesForSubcorpora(userid, corpus_id, subcorpora);
    assertEquals(2, resources.size());
    assertArrayEquals(new String[]{res1, res2}, resources.toArray());

    String res3 = "resource_3";
    db.addVoteForResource(cred, res3, corpus_id, subcorpus_2_id, query_id);

    resources = db.getResourcesForSubcorpora(userid, corpus_id, subcorpora);
    assertArrayEquals(new String[]{res1, res2, res3}, resources.toArray());

    // ignore subcorpus 2
    subcorpora.clear();
    subcorpora.add(1L);

    resources = db.getResourcesForSubcorpora(userid, corpus_id, subcorpora);
    assertArrayEquals(new String[]{res1, res2}, resources.toArray());

    // test number of results and start index - results are sorted by RESOURCE
    subcorpora.add(2L);
    resources = db.getResourcesForSubcorpora(userid, corpus_id, subcorpora, 1, 0);
    assertArrayEquals(new String[]{res1}, resources.toArray());

    resources = db.getResourcesForSubcorpora(userid, corpus_id, subcorpora, 1, 2);
    assertArrayEquals(new String[]{res3}, resources.toArray());

    resources = db.getResourcesForSubcorpora(userid, corpus_id, subcorpora, 10, 10);
    assertEquals(0, resources.size());

    resources = db.getResourcesForSubcorpora(userid, corpus_id, subcorpora, 3, 0);
    assertArrayEquals(new String[]{res1, res2, res3}, resources.toArray());

    resources = db.getResourcesForSubcorpora(userid, corpus_id, subcorpora, 100, 0);
    assertArrayEquals(new String[]{res1, res2, res3}, resources.toArray());

  }
}

