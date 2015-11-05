package ciir.proteus.users.impl;

import ciir.proteus.users.Credentials;
import ciir.proteus.users.UserDatabase;
import ciir.proteus.users.Users;
import ciir.proteus.users.error.*;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DataSources;
import org.h2.constant.ErrorCode;
import org.lemurproject.galago.utility.Parameters;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

/**
 * @author jfoley. Updates: MichaelZ
 */
public class H2Database implements UserDatabase {

  private static final Logger log = Logger.getLogger(H2Database.class.getName());

  private ComboPooledDataSource cpds = null;

  public H2Database(Parameters conf) throws SQLException {
    System.setProperty("com.mchange.v2.log.MLog", "com.mchange.v2.log.FallbackMLog");
    System.setProperty("com.mchange.v2.log.FallbackMLog.DEFAULT_CUTOFF_LEVEL", "WARNING");

    try {
      // prime JDBC driver
      Class.forName("org.h2.Driver");
      // configuration
      String path = conf.getString("path");
      String dbuser = conf.getString("user");
      String dbpass = conf.get("pass", "");
      // setting AUTO_SERVER to TRUE allows multiple processes to
      // connect to the DB - useful for debugging with a DB viewer
      String autoServer = conf.get("auto_server", "FALSE");
      // create the connection pool
      cpds = new ComboPooledDataSource();
      cpds.setDriverClass("org.h2.Driver"); //loads the jdbc driver
      cpds.setJdbcUrl("jdbc:h2:" + path + ";AUTO_SERVER=" + autoServer);
      cpds.setUser(dbuser);
      cpds.setPassword(dbpass);
      cpds.setAutoCommitOnClose(true); // without this - connections don't close unless you explicitly commit
      cpds.setMaxPoolSize((int) conf.get("max_pool_size", 1000));
      cpds.setInitialPoolSize((int) conf.get("initial_pool_size", 50));
      cpds.setMaxStatementsPerConnection((int) conf.get("max_prepared_statements_per_connection", 10));  // allow for prepared statements

      // create tables if needed
      initDB();

    } catch (ClassNotFoundException e) {
      throw new IllegalArgumentException(e);
    } catch (PropertyVetoException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  // this is public so we can use it when testing.
  // NOTE the caller is responsible for closing the connection.
  public Connection getConnection() {
    Connection conn = null;
    try {
      conn = cpds.getConnection();
    } catch (SQLException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }

    return conn;
  }

  private void initDB() {
    Connection conn = null;
    try {
      conn = getConnection();
      // NOTE: H2 will automatically create an index on any foreign keys.
      conn.createStatement().executeUpdate("create table IF NOT EXISTS users (ID BIGINT NOT NULL IDENTITY, EMAIL VARCHAR(" + Users.UserEmailMaxLength + ") NOT NULL, settings VARCHAR(200) NOT NULL DEFAULT '{ \"num_entities\" : 10 }', PRIMARY KEY (ID))");
      conn.createStatement().executeUpdate("create unique index IF NOT EXISTS user_uniq_email_idx on users(email)");

      conn.createStatement().executeUpdate("create table IF NOT EXISTS sessions (user_id bigint NOT NULL, session char(" + Users.SessionIdLength + "),foreign key (user_id) references users(id))");

      conn.createStatement().executeUpdate("create table IF NOT EXISTS tags (USER_ID BIGINT NOT NULL,  resource varchar(256) NOT NULL, LABEL_TYPE VARCHAR_IGNORECASE(256) NOT NULL, LABEL_VALUE VARCHAR_IGNORECASE(256) NOT NULL, rating int NOT NULL default 0,  comment CLOB,foreign key (user_id) references users(id))");
      conn.createStatement().executeUpdate("create index IF NOT EXISTS label_resource_idx on tags(resource)");

      // Assuming anyone who has access to this DB can see all the corpora
      conn.createStatement().executeUpdate("create table IF NOT EXISTS corpora (ID BIGINT NOT NULL IDENTITY, corpus VARCHAR(200) NOT NULL, PRIMARY KEY (ID))");
      conn.createStatement().executeUpdate("create table IF NOT EXISTS subcorpora (corpus_id BIGINT NOT NULL, ID BIGINT NOT NULL IDENTITY, subcorpus VARCHAR(200) NOT NULL, PRIMARY KEY (ID), foreign key (corpus_id) references corpora(id))");
      // ensure subcorpus names are unique
      conn.createStatement().executeUpdate("create unique index IF NOT EXISTS subcorpus_name_idx on subcorpora(subcorpus)");
      conn.createStatement().executeUpdate("create table IF NOT EXISTS resource_ratings (USER_ID BIGINT NOT NULL, CORPUS_ID BIGINT NOT NULL, resource varchar(256) NOT NULL, rating int NOT NULL default 0, foreign key (user_id) references users(id), foreign key (corpus_id) references corpora(id))");
      conn.createStatement().executeUpdate("create unique index IF NOT EXISTS resource_rating_idx on resource_ratings(user_id, corpus_id, resource)");

      conn.createStatement().executeUpdate("create sequence IF NOT EXISTS note_seq");
      conn.createStatement().executeUpdate("create table IF NOT EXISTS notes (ID BIGINT NOT NULL, CORPUS_ID BIGINT NOT NULL, resource VARCHAR(256) NOT NULL," +
              " data VARCHAR(2000) NOT NULL, ins_user BIGINT NOT NULL, ins_dttm DATETIME NOT NULL, upd_user BIGINT, upd_dttm DATETIME, " +
              " PRIMARY KEY (ID), foreign key (corpus_id) references corpora(id))");
      conn.createStatement().executeUpdate("create index IF NOT EXISTS notes_res_idx on notes(resource)");
      conn.createStatement().executeUpdate("create sequence IF NOT EXISTS query_seq");
      conn.createStatement().executeUpdate("create table IF NOT EXISTS queries (ID BIGINT NOT NULL, CORPUS_ID BIGINT NOT NULL, query VARCHAR(500) NOT NULL, kind VARCHAR(10) NOT NULL, PRIMARY KEY(ID))");
      conn.createStatement().executeUpdate("create unique index IF NOT EXISTS query_idx on queries(corpus_id, query, kind)");
      conn.createStatement().executeUpdate("create table IF NOT EXISTS query_res_xref (query_id BIGINT NOT NULL, CORPUS_ID BIGINT NOT NULL, resource VARCHAR(256) NOT NULL)");
      // TODO ??? use FKs for ^^^ this?
      conn.createStatement().executeUpdate("create unique index IF NOT EXISTS query_res_xref_idx on query_res_xref(query_id, corpus_id, resource)");

      // TODO ??? add keys, etc
      conn.createStatement().executeUpdate("create table IF NOT EXISTS resource_labels (USER_ID BIGINT NOT NULL, CORPUS_ID BIGINT NOT NULL, SUBCORPUS_ID BIGINT NOT NULL, resource VARCHAR(256) NOT NULL, foreign key (user_id) references users(id), foreign key (corpus_id) references corpora(id))");

      try {
        // 10/2015 MCZ : make sure we have a default corpus. We're moving toward a tagging model so the "select corpus"
        // in the UI is currently hidden so we'll do everything under the "default" corpus.
        conn.createStatement().executeUpdate("insert into corpora (id, corpus) values (1, 'default')");
      } catch (SQLException e) {
        // it's OK if it already exists
        if (e.getErrorCode() != ErrorCode.DUPLICATE_KEY_1) {
          e.printStackTrace();
          throw new RuntimeException(e);
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    } finally {
      attemptClose(conn);
    }
  }

  @Override
  public void close() {
    try {
      DataSources.destroy(cpds);
    } catch (SQLException ex) {
      ex.printStackTrace();
    }
  }

  @Override
  public void register(String username) throws NoTuplesAffected, DuplicateUser {
    Connection conn = null;
    try {
      conn = getConnection();
      int numRows = conn.createStatement().executeUpdate("insert into users (email) values (LOWER('" + username + "'))");

      if (numRows == 0) {
        throw new NoTuplesAffected();
      }

    } catch (SQLException e) {
      e.printStackTrace();
      throw new DuplicateUser();
    } finally {
      attemptClose(conn);
    }
  }

  @Override
  public Parameters login(String username) {
    if (!validUser(username)) {
      return null;
    }

    System.out.println("Logging in user " + username);
    Parameters ret = Parameters.create();
    ret.put("user", username);
    Integer userid = -1;
    Connection conn = null;
    try {
      conn = getConnection();
      // get the user id
      ResultSet results = conn.createStatement().executeQuery("select id, settings from users where email=LOWER('" + username + "')");

      if (results.next()) {
        userid = results.getInt(1);
        ret.put("userid", userid);
        // TODO: JSON parse
        ret.put("settings", results.getString(2));
      }

      results.close();

      String session = Users.generateSessionId();

      int numRows = conn.createStatement().executeUpdate("insert into sessions (user_id,session) values (" + userid + ",'" + session + "')");

      // should create 1 row
      if (1 == numRows) {
        ret.put("token", session);
      }

      // get all the corpora
      Parameters corpora = null;
      try {
        corpora = getAllCorpora();
      } catch (DBError dbError) {
        dbError.printStackTrace();
      }
      ret.copyFrom(corpora);

      // and sub-corpora
      Parameters subcorpora = null;
      try {
        subcorpora = getAllSubCorpora();
      } catch (DBError dbError) {
        dbError.printStackTrace();
      }
      ret.copyFrom(subcorpora);

    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      attemptClose(conn);
    }

    return ret;
  }

  @Override
  public void logout(Credentials creds) throws NoTuplesAffected {
    if (!validSession(creds)) {
      return;
    }
    Connection conn = null;
    try {
      conn = getConnection();
      int numRows = conn.createStatement().executeUpdate("delete from sessions where user_id=" + creds.userid + " and session='" + creds.token + "'");

      if (numRows == 0) {
        // since validSession was true,
        // this probably should have worked, barring race conditions
        throw new NoTuplesAffected();
      }

    } catch (SQLException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    } finally {
      attemptClose(conn);
    }
  }

  @Override
  public boolean validUser(String username) {
    if (username.length() > 64) {
      return false;
    }

    boolean found = false;
    Connection conn = null;
    try {
      conn = getConnection();
      ResultSet results = conn.createStatement().executeQuery("select count(*) from users where email=LOWER('" + username + "')");

      if (results.next()) {
        int numUsers = results.getInt(1);
        found = (numUsers == 1);
      }

    } catch (SQLException ex) {
      ex.printStackTrace();
    } finally {
      attemptClose(conn);
    }
    return found;
  }

  @Override
  public boolean validSession(Credentials creds) {
    boolean found = false;
    Connection conn = null;
    try {
      conn = getConnection();
      ResultSet results = conn.createStatement().executeQuery("select (user_id,session) from sessions where user_id=" + creds.userid + " and session='" + creds.token + "'");

      if (results.next()) {
        found = true;
      }

    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      attemptClose(conn);
    }

    return found;
  }

  @Override
  public void checkSession(Credentials creds) throws DBError {
    if (!validUser(creds.user)) {
      throw new BadUserException(creds.user);
    }
    if (!validSession(creds)) {
      throw new BadSessionException(creds.user, creds.token);
    }
  }

  @Override
  public List<String> getTags(Credentials creds, String resource) throws DBError {
    Map<String, List<String>> results = getTags(creds, Arrays.asList(resource));
    return results.get(resource);
  }

  public Map<String, List<String>> getTags(Credentials creds, List<String> resources) throws DBError {
    checkSession(creds);

    Map<String, List<String>> results = new HashMap<>();

    Connection conn = null;
    try {
      conn = getConnection();

      PreparedStatement sql = conn.prepareStatement("select label_type || ':' || label_value from tags where user_id=? and resource=?");

      sql.setInt(1, creds.userid);

      for (String resource : resources) {
        List<String> tags = new ArrayList<>();
        sql.setString(2, resource);

        ResultSet tuples = sql.executeQuery();
        while (tuples.next()) {
          String tag = tuples.getString(1);
          tags.add(tag);
        }
        tuples.close();
        results.put(resource, tags);
      }

    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      attemptClose(conn);
    }

    return results;
  }

  // returns resources and a list of users and their tags.
  @Override
  public Map<Integer, Map<String, String>> getAllTags(String resource) throws DBError {
    Map<String, Map<Integer, Map<String, String>>> results = getAllTags(Arrays.asList(resource));
    return results.get(resource);
  }

  // get all the tags for a list of resources along with all the labels and each user and their rating and
  // (possibly) a comment for the resource/label pair.
  @Override
  public Map<String, Map<String, List<String>>> getAllTagsForResources(List<String> resources) throws DBError {

    //map<resource, map<label, list<user:rating>>>
    Map<String, Map<String, List<String>>> results = new HashMap<>();

    Connection conn = null;
    try {
      conn = getConnection();

      PreparedStatement sql = conn.prepareStatement("SELECT label_type || ':' || label_value AS tag, user_id || ':' || rating  || ':' || NVL(comment, '') AS user_rating FROM tags WHERE resource = ? ORDER by resource, tag");
      for (String resource : resources) {
        Map<String, List<String>> tagRatings = new HashMap<>();
        sql.setString(1, resource);

        ResultSet tuples = sql.executeQuery();

        String currentTag = "";
        List<String> userRatings = new ArrayList<>();

        while (tuples.next()) {

          String tag = tuples.getString(1);

          if (currentTag.length() == 0) {   // first time through
            currentTag = tag;
          }

          String userRating = tuples.getString(2);

          if (!Objects.equals(currentTag, tag)) {
            tagRatings.put(currentTag, userRatings);
            results.put(resource, tagRatings); // put user/tags in results for the resource

            currentTag = tag;
            userRatings = new ArrayList<>();
          }

          userRatings.add(userRating);

        } // end while we have rows

        // put in the last user - if there is one
        if (currentTag.length() != 0) {   // first time through
          tagRatings.put(currentTag, userRatings);
        }

        if (tagRatings.size() != 0) {
          results.put(resource, tagRatings);
        }

        tuples.close();

      }
      //     getAllTagsSQL.close();

    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      attemptClose(conn);
    }

    return results;
  }

  @Override
  public Map<String, Map<Integer, Map<String, String>>> getAllTags(List<String> resources) throws DBError {

    Map<String, Map<Integer, Map<String, String>>> results = new HashMap<>();

    Connection conn = null;
    try {
      conn = getConnection();

      PreparedStatement sql = conn.prepareStatement("SELECT user_id, label_type || ':' || label_value AS label, rating || ':' || NVL(comment, '') AS user_data FROM tags WHERE resource LIKE ? GROUP BY user_id, label, user_data ORDER BY user_id, label");
      for (String resource : resources) {
        Map<Integer, Map<String, String>> userTags = new HashMap<>();
        sql.setString(1, resource);

        ResultSet tuples = sql.executeQuery();

        Integer currentUser = -1;
        //List<String> tags = new ArrayList<>();
        Map<String, String> tags = new HashMap<>();

        while (tuples.next()) {

          Integer user = tuples.getInt(1);

          if (currentUser == -1) {   // first time through
            currentUser = user;
          }

          String tag = tuples.getString(2);

          if (!Objects.equals(currentUser, user)) {
            userTags.put(currentUser, tags);
            results.put(resource, userTags); // put user/tags in results for the resource

            currentUser = user;
            tags = new HashMap<>();
          }

          tags.put(tag, tuples.getString(3));

        } // end while we have rows

        // put in the last user - if there is one
        if (currentUser != -1) {
          userTags.put(currentUser, tags);
        }
        results.put(resource, userTags);

        tuples.close();

      }
      //     getAllTagsSQL.close();

    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      attemptClose(conn);
    }

    return results;
  }

  @Override
  public void deleteTag(Credentials creds, String resource, String tag) throws DBError {
    checkSession(creds);
    String labelParts[] = tag.split(":");
    String labelType = null;
    String labelValue = null;
    if (labelParts.length == 1) {
      labelType = "*";
      labelValue = labelParts[0];
    } else {
      labelType = labelParts[0];
      labelValue = labelParts[1];
    }
    Connection conn = null;
    try {
      conn = getConnection();
      int numRows = conn.createStatement().executeUpdate("delete from tags where user_id= " + creds.userid + " and resource='" + resource + "' and label_type='" + labelType + "' and label_value='" + labelValue + "'");

      if (numRows == 0) {
        log.info("user: '" + creds.user + "', resource: '" + resource + "', tag: '" + tag + "'");
        throw new NoTuplesAffected();
      }

    } catch (SQLException e) {
      log.info("user: '" + creds.user + "', resource: '" + resource + "', tag: '" + tag + "'");
      throw new RuntimeException(e);
    } finally {
      attemptClose(conn);
    }
  }

  @Override
  public void addTag(Credentials creds, String resource, String tag, Integer rating, String comment) throws DBError {
    checkSession(creds);

    String labelParts[] = tag.split(":");
    Connection conn = null;
    try {
      conn = getConnection();
      PreparedStatement sql = conn.prepareStatement("insert into tags (user_id,resource,label_type, label_value, rating, comment) values (?,?,?,?,?,?)");

      sql.setInt(1, creds.userid);
      sql.setString(2, resource);
      sql.setInt(5, rating);
      sql.setString(6, comment);

      // if there is only 1 part to the tag, use the "wildcard" for the label type
      if (labelParts.length == 1) {
        sql.setString(3, "*");
        sql.setString(4, labelParts[0]);
      } else {
        sql.setString(3, labelParts[0]);
        sql.setString(4, labelParts[1]);
      }

      int numRows = sql.executeUpdate();

      assert (numRows == 1);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    } finally {
      attemptClose(conn);
    }
  }

  @Override
  public void updateTag(Credentials creds, String resource, String tag, Integer rating, String comment) throws DBError {
    checkSession(creds);

    String labelParts[] = tag.split(":");
    Connection conn = null;
    try {
      conn = getConnection();
      PreparedStatement sql = conn.prepareStatement("UPDATE tags SET rating = ?, comment = ? WHERE user_id = ? AND resource = ? AND label_type = ? AND label_value = ?");

      sql.setInt(1, rating);
      sql.setString(2, comment);
      sql.setInt(3, creds.userid);
      sql.setString(4, resource);

      // if there is only 1 part to the tag, use the "wildcard" for the label type
      if (labelParts.length == 1) {
        sql.setString(5, "*");
        sql.setString(6, labelParts[0]);
      } else {
        sql.setString(5, labelParts[0]);
        sql.setString(6, labelParts[1]);
      }

      int numRows = sql.executeUpdate();

      assert (numRows == 1);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    } finally {
      attemptClose(conn);
    }
  }

  @Override
  public List<String> getResourcesForLabels(Integer userid, List<String> labels) throws DBError {
    return getResourcesForLabels(userid, labels, -1, -1);
  }

  @Override
  public List<String> getResourcesForLabels(Integer userid, List<String> labels, Integer numResults, Integer startIndex) throws DBError {

    Connection conn = null;
    try {
      conn = getConnection();

      List<String> resources = new ArrayList<>();
      Object[] objLabels = new Object[labels.size()];
      int i = 0;
      for (String label : labels) {
        objLabels[i++] = label;
      }
      ResultSet results = null;

      // we may want to skip the user_id
      // MCZ 3/15 - allow anyone to retrieval using anyone's labels so we don't filter by user id
      userid = -1;
      String userClause = " ";
      //      if (userid != -1) {
      //        userClause = " AND tags.user_id = ? ";
      //      }

      String limitClause = " ";
      if (numResults != -1) {
        limitClause = " LIMIT ? OFFSET ?";
      }

      // prepared statements don't like "in(...)" clauses, we have to use a temp table rather than this:
      //  SELECT DISTINCT resource FROM tags WHERE user LIKE ? AND tag IN (?)
      // we need to ORDER BY to ensure the result sets will always be in the same order.
      PreparedStatement sql = conn.prepareStatement("SELECT DISTINCT resource FROM table(label VARCHAR=?) temp_table INNER JOIN tags ON temp_table.label = tags.label_type || ':' || tags.label_value " + userClause + " ORDER BY resource " + limitClause);
      int paramNum = 1;
      sql.setObject(paramNum, objLabels);
      if (userid != -1) {
        sql.setInt(++paramNum, userid);
      }
      if (numResults != -1) {
        sql.setInt(++paramNum, numResults);
        sql.setInt(++paramNum, startIndex);
      }

      results = sql.executeQuery();

      while (results.next()) {
        String res = results.getString(1);
        resources.add(res);
      }

      results.close();

      return resources;

    } catch (SQLException e) {
      throw new RuntimeException(e);
    } finally {
      attemptClose(conn);
    }
  }

  public Map<String, String> getUsers() throws DBError {

    Map<String, String> results = new HashMap<>();

    Connection conn = null;
    try {
      conn = getConnection();

      PreparedStatement sql = conn.prepareStatement("select id, email from users");

      ResultSet tuples = sql.executeQuery();
      while (tuples.next()) {
        results.put(tuples.getString(1), tuples.getString(2));
      }
      tuples.close();

    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      attemptClose(conn);
    }

    return results;
  }

  @Override
  public Parameters upsertSubCorpus(Parameters p) throws DBError, SQLException {
    Connection conn = null;

    try {
      Integer corpusid = p.get("corpusid", -1);
      List<Parameters> subcorpora = new ArrayList<Parameters>();
      subcorpora = p.getAsList("subcorpora");
      conn = getConnection();

      // loop through the subcorpora, if there is no ID field, we'll insert the data,
      // otherwise update
      for (Parameters rec : subcorpora) {
        Integer id = rec.get("id", -1);
        String name = rec.getString("name");

        if (id == -1) {
          PreparedStatement sql = conn.prepareStatement("INSERT INTO subcorpora (corpus_id, subcorpus) VALUES(?, ?)");

          sql.setInt(1, corpusid);
          sql.setString(2, name);
          int numRows = sql.executeUpdate();
          //assert (numRows == 1);

        } else {
          PreparedStatement sql = conn.prepareStatement("UPDATE subcorpora SET subcorpus = ? WHERE id = ? AND corpus_id = ?");

          sql.setString(1, name);
          sql.setInt(2, id);
          sql.setInt(3, corpusid);
          int numRows = sql.executeUpdate();
          //assert (numRows == 1);

        }
      } // end loop through subcorpora
      // TODO log who created/updated - logic in other functions is in the caller

      // return the new data
      Parameters ret = getAllSubCorpora();

      return (ret);

    } catch (SQLException e) {
      if (e.getErrorCode() == ErrorCode.DUPLICATE_KEY_1) {
        e.printStackTrace();
        throw new DuplicateSubCorpus();
      }
      e.printStackTrace();
      throw new RuntimeException(e);
    } finally {
      attemptClose(conn);
    }
  }

  @Override
  public Integer createCorpus(String corpus, String username) throws NoTuplesAffected, DuplicateCorpus, SQLException {
    Connection conn = null;
    Integer id = -1;
    try {
      conn = getConnection();

      // make sure we're not a duplicate
      ResultSet results = conn.createStatement().executeQuery("select count(*) from corpora where LOWER(corpus)=LOWER('" + corpus + "')");

      if (results.next()) {
        if (results.getInt(1) != 0) {
          System.err.println("Duplicate corpus: " + corpus);
          throw new DuplicateCorpus();
        }
        ;
      }

      int numRows = conn.createStatement().executeUpdate("insert into corpora (corpus) values ('" + corpus + "')");

      if (numRows == 0) {
        throw new NoTuplesAffected();
      }

      // send back the new corpus ID
      PreparedStatement sql = conn.prepareStatement("select id from corpora where corpus = '" + corpus + "'");
      ResultSet tuples = sql.executeQuery();
      while (tuples.next()) {
        id = tuples.getInt(1);
      }

    } catch (SQLException e) {
      e.printStackTrace();
      throw new DuplicateCorpus();
    } finally {
      attemptClose(conn);
    }

    return (id);
  }

  public Parameters getAllCorpora() throws DBError {

    Parameters results = Parameters.create();

    Connection conn = null;
    try {
      conn = getConnection();

      PreparedStatement sql = conn.prepareStatement("select id, corpus from corpora order by corpus");
      //  List<String> corpora = new ArrayList<>();
      List<Parameters> corpora = new ArrayList<>();
      //  Parameters corpora = Parameters.create();
      ResultSet tuples = sql.executeQuery();
      while (tuples.next()) {
        Integer id = tuples.getInt(1);
        String corpus = tuples.getString(2);
        //corpora.add(corpus);
        Parameters p = Parameters.create();
        p.put("id", id);
        p.put("name", corpus);
        corpora.add(p);
      }
      tuples.close();
      results.put("corpora", corpora);

    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      attemptClose(conn);
    }

    return results;
  }

  public Parameters getAllSubCorpora() throws DBError {

    Parameters results = Parameters.create();

    Connection conn = null;
    try {
      conn = getConnection();

      PreparedStatement sql = conn.prepareStatement("select id, corpus_id,  subcorpus from subcorpora order by corpus_id, subcorpus");

      List<Parameters> corpora = new ArrayList<>();

      ResultSet tuples = sql.executeQuery();
      while (tuples.next()) {
        Parameters p = Parameters.create();
        p.put("id", tuples.getInt(1));
        p.put("corpus_id", tuples.getInt(2));
        p.put("name", tuples.getString(3));
        corpora.add(p);
      }
      tuples.close();
      results.put("subcorpora", corpora);

    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      attemptClose(conn);
    }

    return results;
  }

  // Note: this will overwrite any existing settings.
  public void updateUserSettings(Credentials creds, String settings) throws DBError {
    checkSession(creds);

    Connection conn = null;
    try {
      conn = getConnection();
      PreparedStatement sql = conn.prepareStatement("UPDATE users SET settings = ? WHERE id = ?");

      sql.setString(1, settings);
      sql.setInt(2, creds.userid);

      int numRows = sql.executeUpdate();

      assert (numRows == 1);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    } finally {
      attemptClose(conn);
    }
  }

  public Parameters getResourceRatings2(String resource, Integer corpusID) {

    List<Parameters> userRating = new ArrayList<>(0);

    Connection conn = null;
    try {
      conn = getConnection();

      // using LIKE so if the resource is a book, we get all the rows including the book's pages so we
      // can show them per page in the book OCR view.
      PreparedStatement sql = conn.prepareStatement("SELECT user_id, email, subcorpus_id, resource  FROM resource_labels, users WHERE resource LIKE ? AND users.id = resource_labels.user_id AND corpus_id = ? ");

      sql.setString(1, resource + "%");
      sql.setInt(2, corpusID);

      ResultSet tuples = sql.executeQuery();
      while (tuples.next()) {
        Integer userid = tuples.getInt(1);
        String user = tuples.getString(2);
        Integer subcorpusid = tuples.getInt(3);
        String res = tuples.getString(4);

        Parameters p = Parameters.create();
        p.set("name", res);
        p.set("userid", userid);
        p.set("user", user);
        p.set("subcorpusid", subcorpusid);
        userRating.add(p);
      }
      tuples.close();

    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      attemptClose(conn);
    }

    Parameters results = Parameters.create();
    results.put("labels", userRating);
    return results;

  }

  public Parameters getResourceRatings(String resource, Integer corpusID) {

    Parameters tmp = Parameters.create();
    Parameters ave = Parameters.create();
    List<Parameters> userRating = new ArrayList<>(0);

    Integer count = 0;
    Integer sum = 0;

    Connection conn = null;
    try {
      conn = getConnection();

      PreparedStatement sql = conn.prepareStatement("SELECT user_id, email, rating FROM resource_ratings, users WHERE resource=? AND users.id = resource_ratings.user_id AND rating != 0 AND corpus_id = ? ");

      sql.setString(1, resource);
      sql.setInt(2, corpusID);

      ResultSet tuples = sql.executeQuery();
      while (tuples.next()) {
        count++;
        Integer userid = tuples.getInt(1);
        String user = tuples.getString(2);
        Integer rating = tuples.getInt(3);
        sum += rating;

        Parameters p = Parameters.create();
        p.set("userid", userid);
        p.set("user", user);
        p.set("rating", rating);
        userRating.add(p);

        //        ave.put("rating", sum / count);

      }
      tuples.close();

    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      attemptClose(conn);
    }

    Parameters results = Parameters.create();
    results.put("ratings", userRating);
    if (count == 0) {
      results.put("aveRating", 0);
    } else {
      results.put("aveRating", sum / count);
    }

    return results;

  }

  public void upsertResourceRating(Credentials creds, String resource, Integer userID, Integer corpusID, Integer rating, Integer queryid) throws DBError {
    checkSession(creds);

    Connection conn = null;

    try {
      conn = getConnection();
      PreparedStatement sql = conn.prepareStatement("INSERT INTO resource_ratings (user_id, corpus_id, resource, rating) VALUES(?, ?, ?, ?)");

      sql.setInt(1, userID);
      sql.setInt(2, corpusID);
      sql.setString(3, resource);
      sql.setInt(4, rating);

      int numRows = sql.executeUpdate();

      assert (numRows == 1);
    } catch (SQLException e) {

      // IFF duplicate key, try update
      if (e.getErrorCode() == ErrorCode.DUPLICATE_KEY_1) {
        try {
          PreparedStatement sql = conn.prepareStatement("UPDATE resource_ratings SET rating = ? WHERE user_id = ? AND corpus_id = ? AND resource = ?");

          sql.setInt(1, rating);
          sql.setInt(2, userID);
          sql.setInt(3, corpusID);
          sql.setString(4, resource);

          int numRows = sql.executeUpdate();

          assert (numRows == 1);
        } catch (SQLException eu) {
          eu.printStackTrace();
          throw new RuntimeException(eu);
        }
      } else {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
    } finally {
      // tie the resource the the query
      insertQueryResourceXref(creds, resource, corpusID, queryid);
      attemptClose(conn);
    }
  }


  // ??? want to include the query id in this - should probably drive the select from the query/resource xref.
  // ?? eihter way, think we're going to  have to return Parameters
  public Parameters getResourcesForCorpusByQuery(Integer corpusID) throws DBError {

    Connection conn = null;
    try {
      conn = getConnection();

      Parameters ret = Parameters.create();
      ResultSet results = null;

      PreparedStatement sql = conn.prepareStatement("SELECT LOWER(query) AS q, r.resource AS res, u.email " +
              "FROM queries q, " +
              "QUERY_RES_XREF x, " +
              "resource_labels r, " +
              "users u " +
              "WHERE " +
              "1=1 " +
              "AND x.query_id = q.id " +
              "AND x.resource = r.resource " +
              "AND r.corpus_id = q.corpus_id " +
              "AND r.corpus_id = x.corpus_id " +
              "AND r.corpus_id = ? " +
              "AND r.user_id = u.id " +
              "UNION " +
              "SELECT  'none - added via a note', resource, u.email " +
              "FROM notes n, " +
              "users u " +
              "WHERE n.corpus_id = ? " +
              "AND n.ins_user = u.id " +
              "ORDER BY q, res");

      sql.setInt(1, corpusID);
      sql.setInt(2, corpusID);

      results = sql.executeQuery();

      List<String> resources = new ArrayList<String>();
      List<Parameters> queryList = new ArrayList<Parameters>();
      List<String> users = new ArrayList<String>();

      String currentQuery = "";
      // GROUP them by query

      // get the first record
      if (results.next()) {
        currentQuery = results.getString(1);
        resources.add(results.getString(2));
      }

      while (results.next()) {
        String q = results.getString(1);

        if (!q.equals(currentQuery)) {
          Parameters tmp = Parameters.create();
          tmp.put("query", currentQuery);
          tmp.put("resources", resources);
          queryList.add(tmp);
          currentQuery = q;
          resources = new ArrayList<String>();
          resources.add(results.getString(2));
        } else {
          resources.add(results.getString(2));
        }

      } // end loop through results

      // save the last set
      if (currentQuery.length() > 0) {
        Parameters tmp = Parameters.create();
        tmp.put("query", currentQuery);
        tmp.put("resources", resources);
        queryList.add(tmp);
      }

      if (queryList.size() > 0) {
        ret.set("queries", queryList);
      }
      results.close();

      return ret;

    } catch (SQLException e) {
      throw new RuntimeException(e);
    } finally {
      attemptClose(conn);
    }
  }


  // TODO what type of functionality do we want? retrieval just for resources I rated? Only average ratings > 0? Any resource with a
  // positive rating?

  public List<String> getAllResourcesForCorpus(Integer userid, Integer corpusID) throws DBError {
    return getResourcesForCorpus(userid, corpusID, -1, -1);
  }

  public List<String> getResourcesForCorpus(Integer userid, Integer corpusID, Integer numResults, Integer startIndex) throws DBError {

    Connection conn = null;
    try {
      conn = getConnection();

      List<String> resources = new ArrayList<>();
      ResultSet results = null;

      // NOTE: we ignore any resources that have ONLY -1 ratings. If ANYONE said it belongs (+1) we'll include it.
      if (numResults == -1) {
        PreparedStatement sql = conn.prepareStatement("SELECT DISTINCT resource FROM resource_labels WHERE corpus_id = ? " +
                " UNION SELECT DISTINCT resource FROM notes WHERE corpus_id = ? ORDER BY resource");
        sql.setInt(1, corpusID);
        sql.setInt(2, corpusID);
        results = sql.executeQuery();
      } else {
        // we need to ORDER BY to ensure the result sets will always be in the same order.
        PreparedStatement sql = conn.prepareStatement("SELECT DISTINCT resource FROM resource_labels WHERE corpus_id = ? " +
                " UNION SELECT DISTINCT resource FROM notes WHERE corpus_id = ? ORDER BY resource LIMIT ? OFFSET ?");
        sql.setInt(1, corpusID);
        sql.setInt(2, corpusID);
        sql.setInt(3, numResults);
        sql.setInt(4, startIndex);
        results = sql.executeQuery();
      }

      while (results.next()) {
        String res = results.getString(1);
        resources.add(res);
      }

      results.close();

      return resources;

    } catch (SQLException e) {
      throw new RuntimeException(e);
    } finally {
      attemptClose(conn);
    }
  }

  // notes functions
  @Override
  public Integer insertNote(Credentials creds, Integer corpusID, String resource, String data) throws DBError {

    if (data.length() == 0)
      return -1;

    //checkSession(creds);

    Integer id = -1;

    Connection conn = null;
    try {
      conn = getConnection();

      ResultSet results = conn.createStatement().executeQuery("SELECT note_seq.nextval");

      if (results.next()) {
        id = results.getInt(1);
      }

      // add the unique ID to the data
      String newData = "{ \"id\" : " + id + "," + data.substring(1);
      PreparedStatement sql = conn.prepareStatement("INSERT INTO notes (id, resource, corpus_id, data, ins_user, ins_dttm) VALUES (?, ?, ?, ?, ?, NOW())");

      sql.setInt(1, id);
      sql.setString(2, resource);
      sql.setInt(3, corpusID);
      sql.setString(4, newData);
      sql.setInt(5, creds.userid);

      int numRows = sql.executeUpdate();

      assert (numRows == 1);

    } catch (SQLException e) {
      throw new RuntimeException(e);
    } finally {
      attemptClose(conn);
    }

    return (id);

  }

  @Override
  public void updateNote(Credentials creds, Integer id, Integer corpusID, String data) throws DBError {
    //checkSession(creds);

    Connection conn = null;
    try {
      conn = getConnection();
      PreparedStatement sql = conn.prepareStatement("UPDATE notes SET data = ?, upd_user = ?, upd_dttm = NOW() WHERE id = ? AND corpus_id = ?");

      sql.setString(1, data);
      sql.setInt(2, creds.userid);
      sql.setInt(3, id);
      sql.setInt(4, corpusID);

      int numRows = sql.executeUpdate();

      assert (numRows == 1);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    } finally {
      attemptClose(conn);
    }
  }

  @Override
  public Parameters getNotesForResource(String resource, Integer corpusID) throws DBError, IOException {

    Integer noteCount = 0;
    Parameters rows = Parameters.create();
    List<Parameters> notes = new ArrayList<>();

    Connection conn = null;
    try {
      conn = getConnection();

      PreparedStatement sql = conn.prepareStatement("SELECT data FROM notes WHERE resource=? AND corpus_id = ?");

      sql.setString(1, resource);
      sql.setInt(2, corpusID);

      ResultSet tuples = sql.executeQuery();
      while (tuples.next()) {
        noteCount++;
        notes.add(Parameters.parseString(tuples.getString(1)));
      }
      tuples.close();

    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      attemptClose(conn);
    }

    Parameters results = Parameters.create();
    results.put("rows", notes);
    results.put("total", noteCount);

    return results;
  }

  @Override
  public void deleteNote(Credentials creds, Integer id, Integer corpusID) throws DBError {
    //checkSession(creds);

    Connection conn = null;
    try {
      conn = getConnection();
      PreparedStatement sql = conn.prepareStatement("DELETE FROM notes WHERE id = ? AND corpus_id = ?");
      sql.setInt(1, id);
      sql.setInt(2, corpusID);
      int numRows = sql.executeUpdate();

      assert (numRows == 1);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    } finally {
      attemptClose(conn);
    }
  }

  // get all notes for a corpus ordered by most recent.
  // TODO: in the future we may want to limit the results to a certain number or only
  // go back a certain number of days.
  @Override
  public Parameters getNotesForCorpus(Integer corpusID) throws DBError, IOException {

    Parameters rows = Parameters.create();
    List<Parameters> notes = new ArrayList<>();

    Connection conn = null;
    try {
      conn = getConnection();

      PreparedStatement sql = conn.prepareStatement("SELECT GREATEST(upd_dttm, ins_dttm) as dttm, data FROM notes WHERE corpus_id = ? ORDER BY dttm  DESC");

      sql.setInt(1, corpusID);

      ResultSet tuples = sql.executeQuery();
      while (tuples.next()) {
        Parameters p = Parameters.parseString(tuples.getString(2));
        p.put("dttm", tuples.getString(1));
        notes.add(p);
      }
      tuples.close();

    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      attemptClose(conn);
    }

    Parameters results = Parameters.create();
    results.put("rows", notes);

    return results;
  }

  public Integer insertQuery(Credentials creds, Integer corpusID, String query, String kind) throws DBError {

    if (query.length() == 0)
      return -1;

    query = query.toLowerCase().trim();
    // don't go over the limit
    if (query.length() > 500) {
      query = query.substring(0, 500);
    }

    //checkSession(creds);

    Integer id = -1;

    Connection conn = null;
    try {
      conn = getConnection();

      // have we already seen this query?
      try {
        PreparedStatement sql = conn.prepareStatement("select id from queries where corpus_id = ? AND query = ? AND kind = ?");
        sql.setInt(1, corpusID);
        sql.setString(2, query);
        sql.setString(3, kind);

        ResultSet tuples = sql.executeQuery();
        while (tuples.next()) {
          id = tuples.getInt(1);
        }
        tuples.close();

      } catch (SQLException e1) {
        throw new RuntimeException(e1);
      }

      // if we found an id, we're done
      if (id > 0) {
        return id;
      }

      // else we'll insert it
      ResultSet results = conn.createStatement().executeQuery("SELECT query_seq.nextval");

      if (results.next()) {
        id = results.getInt(1);
      }
      results.close();

      PreparedStatement sql = conn.prepareStatement("INSERT INTO queries (id, corpus_id, query, kind) VALUES (?, ?, ?, ?)");

      sql.setInt(1, id);
      sql.setInt(2, corpusID);
      sql.setString(3, query);
      sql.setString(4, kind);

      int numRows = sql.executeUpdate();

      assert (numRows == 1);

    } catch (SQLException e) {
      throw new RuntimeException(e);
    } finally {
      attemptClose(conn);
    }

    return (id);

  }

  public void insertQueryResourceXref(Credentials creds, String resource, Integer corpusID, Integer queryid) throws DBError {

    Connection conn = null;

    try {
      conn = getConnection();
      PreparedStatement sql = conn.prepareStatement("INSERT INTO query_res_xref (query_id, corpus_id, resource) VALUES(?, ?, ?)");

      sql.setInt(1, queryid);
      sql.setInt(2, corpusID);
      sql.setString(3, resource);

      int numRows = sql.executeUpdate();

      assert (numRows == 1);
    } catch (SQLException e) {

      // duplicates are OK
      if (e.getErrorCode() != ErrorCode.DUPLICATE_KEY_1) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
    } finally {
      attemptClose(conn);
    }
  }

  public void addVoteForResource(Credentials creds, String resource, Integer corpusID, Integer subcorpusID, Integer queryid) throws DBError {

    Connection conn = null;

    try {
      conn = getConnection();
      PreparedStatement sql = conn.prepareStatement("INSERT INTO RESOURCE_LABELS (user_id, corpus_id, subcorpus_id, resource) VALUES(?, ?, ?, ?)");

      sql.setInt(1, creds.userid);
      sql.setInt(2, corpusID);
      sql.setInt(3, subcorpusID);
      sql.setString(4, resource);

      int numRows = sql.executeUpdate();

      assert (numRows == 1);
    } catch (SQLException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    } finally {
      // tie the resource the the query
      insertQueryResourceXref(creds, resource, corpusID, queryid);

      attemptClose(conn);
    }

  }

  public void removeVoteForResource(Credentials creds, String resource, Integer corpusID, Integer subcorpusID) throws DBError {

    Connection conn = null;

    try {
      conn = getConnection();
      PreparedStatement sql = conn.prepareStatement("DELETE FROM RESOURCE_LABELS WHERE user_id = ? AND corpus_id = ? AND subcorpus_id = ? AND  resource = ?");

      sql.setInt(1, creds.userid);
      sql.setInt(2, corpusID);
      sql.setInt(3, subcorpusID);
      sql.setString(4, resource);

      int numRows = sql.executeUpdate();

      assert (numRows == 1);
    } catch (SQLException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    } finally {
      // TODO ??? remove query xref rec IF no more "votes"?
      attemptClose(conn);
    }

  }


  // "borrowed" from the C3P0 examples: http://sourceforge.net/projects/c3p0/files/c3p0-src/c3p0-0.9.2.1/
  static void attemptClose(Connection o) {
    try {
      if (o != null) {
        o.close();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
