package ciir.proteus.users;

import ciir.proteus.users.error.BadSessionException;
import ciir.proteus.users.error.BadUserException;
import ciir.proteus.users.error.DBError;
import ciir.proteus.users.error.DuplicateCorpus;
import ciir.proteus.users.error.DuplicateUser;
import ciir.proteus.users.error.NoTuplesAffected;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.lemurproject.galago.tupleflow.FileUtility;
import org.lemurproject.galago.utility.FSUtil;
import org.lemurproject.galago.utility.Parameters;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
  public void resourceRank2Test() throws DBError, SQLException {

    String user1 = "user1";
    String user2 = "user2";
    Integer user1id = 1;
    Integer user2id = 2;

    db.register(user1);
    db.register(user2);

    Parameters p = db.login(user1);
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

    assertEquals(res1, recs.get(0).getString("name"));
    assertEquals(subcorpus1.longValue(), recs.get(0).get("subcorpusid"));
    assertEquals(user1, recs.get(0).getString("user"));

    assertEquals(res1_pg1, recs.get(1).getString("name"));
    assertEquals(subcorpus1.longValue(), recs.get(1).get("subcorpusid"));
    assertEquals(user1, recs.get(1).getString("user"));

    recs = labels.getAsList("newLabels");
    assertEquals(1, recs.size());
    p = recs.get(0).get(subcorpus1.toString(), Parameters.create());
    assertEquals(p.get(user1id.toString()), user1);

    // test for corpus that doesn't have anything
    labels = db.getResourceRatings2(res1, corpus2);
    recs = labels.getAsList("labels");
    assertEquals(0, recs.size());

    // have a 2nd user rate the resource
    p = db.login(user2);
    cred = new Credentials(p);

    db.addVoteForResource(cred, res1, corpus1, subcorpus1, queryid1);
    labels = db.getResourceRatings2(res1, corpus1);
    recs = labels.getAsList("labels");
    assertEquals(3, recs.size());

    assertEquals(res1, recs.get(0).getString("name"));
    assertEquals(subcorpus1.longValue(), recs.get(0).get("subcorpusid"));
    assertEquals(user1, recs.get(0).getString("user"));

    assertEquals(res1_pg1, recs.get(1).getString("name"));
    assertEquals(subcorpus1.longValue(), recs.get(1).get("subcorpusid"));
    assertEquals(user1, recs.get(1).getString("user"));

    assertEquals(res1, recs.get(2).getString("name"));
    assertEquals(subcorpus1.longValue(), recs.get(2).get("subcorpusid"));
    assertEquals(user2, recs.get(2).getString("user"));

    recs = labels.getAsList("newLabels");
    assertEquals(1, recs.size());
    p = recs.get(0).get(subcorpus1.toString(), Parameters.create());
    assertEquals(p.get(user1id.toString()), user1);
    assertEquals(p.get(user2id.toString()), user2);
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

    results = db.getResourcesForCorpus(cred.userid, corpus1, 2, 1) ;
    assertArrayEquals(new String[]{res3}, results.toArray());

    results = db.getResourcesForCorpus(cred.userid, corpus1, 20, 10) ;
    assertEquals(0, results.toArray().length);

    results = db.getResourcesForCorpus( cred.userid, corpus2, -1, -1) ;
    assertArrayEquals(new String[]{res2}, results.toArray());

    p = db.login("user2");
    cred = new Credentials(p);

    results = db.getResourcesForCorpus(cred.userid, corpus1, -1, -1) ;
    assertArrayEquals(new String[]{res1, res3}, results.toArray());

    // now give it a non-zero so it'll be returned
    db.addVoteForResource(cred, res2, corpus1, subcorpus1, queryid1);

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

    // add a note
    Integer note_id = db.insertNote(cred, corpus1, res3, "{ \"name\": \"res_3\" }");

    results = db.getResourcesForCorpusByQuery(corpus1);

    assertEquals(results.size(), 1);
    queryList = results.getAsList("queries");
    assertEquals(3, queryList.size());

    query = queryList.get(0);
    res = query.getAsList("resources");
    assertEquals(1, res.size());
    assertEquals(res3, res.get(0));

    query = queryList.get(1);
    res = query.getAsList("resources");
    assertEquals(2, res.size());
    assertEquals(res1, res.get(0));
    assertEquals(res3, res.get(1));

    query = queryList.get(2);
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
  public void getNotesForBookTest() throws DBError, IOException, SQLException {
    String user = "new-user";
    db.register(user);
    Parameters p = db.login(user);

    Credentials cred = new Credentials(p);

    db.createCorpus("a", "user");
    db.createCorpus("b", "user");
    Integer corpus1 = 1;
    Integer corpus2 = 2;

    // no notes for a book
    Parameters notes = db.getNotesForBook("i do not exist", corpus1);
    assertEquals(0, notes.get("total", -1));

    // add notes for a book
    String resource = "res1";
    Integer id = db.insertNote(cred, corpus1, resource + "_1", "{ \"data\": \"a\" }");
    assertEquals(id.longValue(), 1L);
    id = db.insertNote(cred, corpus1, resource + "_1", "{ \"data\": \"b\" }");
    // add another note for this book
    id = db.insertNote(cred, corpus1, resource + "_5", "{ \"data\": \"c\" }");

    // add a resource that tests that we escapte the '_' in the SQL "like" statement
    // so it does not act like a wild card.
    id = db.insertNote(cred, corpus1, resource + "12", "{ \"data\": \"nomatch\" }");

    // add note for a different book
    String resource2 = "res2";
    id = db.insertNote(cred, corpus1, resource2 + "_1", "{ \"data\": \"d\" }");

    notes = db.getNotesForBook(resource, corpus1);
    assertEquals(2, notes.get("total", -1));
    List<Parameters> arr = notes.getAsList("rows");

    // we can't be sure of the order, so check all
    Parameters data_a = Parameters.create();
    data_a.put("page", resource + "_1");
    Parameters data_c = Parameters.create();
    data_c.put("page", resource + "_5");
    Parameters data_d = Parameters.create();
    data_d.put("page", resource2 + "_1");

    assertTrue(arr.contains(data_a));
    assertTrue(arr.contains(data_c));

    // check for same book, different corpus
    notes = db.getNotesForBook(resource, corpus2);
    assertEquals(0, notes.get("total", -1));

    // check different book
    notes = db.getNotesForBook(resource2, corpus1);
    assertEquals(1, notes.get("total", -1));
    arr = notes.getAsList("rows");
    assertTrue(arr.contains(data_d));

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
  public void testInsertAndGetQuery() throws DBError {

    Integer corpus_id_1 = 1;
    String kind_1 = "kind1";
    String query_1 = "query1";

    Integer id = db.insertQuery(null, corpus_id_1, query_1, kind_1);
    assertTrue(id == 1);

    String q = db.getQuery(null, id);
    assertEquals(q, query_1);

    // test that queries are trimmed

    id = db.insertQuery(null, corpus_id_1, " " + query_1 + " ", kind_1);
    assertTrue(id == 1);
    q = db.getQuery(null, id);
    assertEquals(q, query_1);

    // test case sensitivity
    id = db.insertQuery(null, corpus_id_1, query_1.toLowerCase(), kind_1);
    assertTrue(id == 1);
    q = db.getQuery(null, id);
    assertEquals(q, query_1);

    id = db.insertQuery(null, corpus_id_1, query_1.toUpperCase(), kind_1);
    assertTrue(id == 1);
    q = db.getQuery(null, id);
    assertEquals(q, query_1);

    // same query, diff corpus
    Integer corpus_id_2 = 2;
    id = db.insertQuery(null, corpus_id_2, query_1.toUpperCase(), kind_1);
    assertTrue(id == 2);
    q = db.getQuery(null, id);
    assertEquals(q, query_1);

    // new query
    String query_2 = "query2";
    id = db.insertQuery(null, corpus_id_1, query_2, kind_1);
    assertTrue(id == 3);

    // diff kind
    String kind_2 = "kind2";
    id = db.insertQuery(null, corpus_id_1, query_1, kind_2);
    assertTrue(id == 4);
    q = db.getQuery(null, id);
    assertEquals(q, query_1);

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
  public void testGetQueriesForResource() throws DBError, SQLException, IOException {

    String res_1 = "res1";
    Integer corpus_1 = 1;


    // resource does not exist
    Parameters q = db.getQueriesForResource("i-do-not-exist", corpus_1);
    assertEquals(q.get("total", -1), 0);

    String query_text_1 = "test query one";
    String kind_1 = "kind_1";

    Integer id = db.insertQuery(null, corpus_1, query_text_1, kind_1);
    assertTrue(id == 1);
    db.insertQueryResourceXref(null, res_1, corpus_1, id);

    q = db.getQueriesForResource(res_1, corpus_1);
    assertEquals(1, q.get("total", -1));
    List<Parameters> arr = q.getAsList("rows");
    assertEquals(arr.size(), 1);
    assertEquals(arr.get(0).get("kind"), kind_1);
    assertEquals(arr.get(0).get("query"), query_text_1);

    // add a 2nd query for this resource
    String query_text_2 = "test query two";
    id = db.insertQuery(null, corpus_1, query_text_2, kind_1);
    assertTrue(id == 2);
    db.insertQueryResourceXref(null, res_1, corpus_1, id);

    q = db.getQueriesForResource(res_1, corpus_1);
    assertEquals(2, q.get("total", -1));
    arr = q.getAsList("rows");
    assertEquals(arr.size(), 2);
    // can't be 100% sure of the order
    Parameters data_1 = Parameters.create();
    data_1.put("kind", kind_1);
    data_1.put("query", query_text_1);
    assertTrue(arr.contains(data_1));
    Parameters data_2 = Parameters.create();
    data_2.put("kind", kind_1);
    data_2.put("query", query_text_2);
    assertTrue(arr.contains(data_2));

    // insert the SAME query/resource/corpus triple
    id = db.insertQuery(null, corpus_1, query_text_2, kind_1);
    assertTrue(id == 2); // original ID is returned
    q = db.getQueriesForResource(res_1, corpus_1);
    assertEquals(2, q.get("total", -1));
    arr = q.getAsList("rows");
    assertEquals(arr.size(), 2);

    assertTrue(arr.contains(data_1));
    assertTrue(arr.contains(data_2));

    // insert the same query/resource but for a different corpus
    Integer corpus_2 = 2;
    id = db.insertQuery(null, corpus_2, query_text_2, kind_1);
    assertTrue(id == 3);
    db.insertQueryResourceXref(null, res_1, corpus_2, id);

    q = db.getQueriesForResource(res_1, corpus_1); // still 1st corpus
    assertEquals(2, q.get("total", -1));
    assertTrue(arr.contains(data_1));
    assertTrue(arr.contains(data_2));

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

