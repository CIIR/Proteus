package ciir.proteus.users.impl;

import ciir.proteus.users.UserDatabase;
import ciir.proteus.users.Users;
import ciir.proteus.users.error.BadSessionException;
import ciir.proteus.users.error.BadUserException;
import ciir.proteus.users.error.DBError;
import ciir.proteus.users.error.NoTuplesAffected;
import org.lemurproject.galago.tupleflow.Parameters;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @author jfoley.
 */
public class H2Database implements UserDatabase {
  private Connection conn;

  public H2Database(Parameters conf) {
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
      // open a connection
      conn = DriverManager.getConnection("jdbc:h2:" + path + ";AUTO_SERVER=" + autoServer, dbuser, dbpass);
    } catch (ClassNotFoundException e) {
      throw new IllegalArgumentException(e);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void initDB() {
    try {
     
      conn.prepareStatement("create table users (" +
          "user varchar("+ Users.UserMaxLength+")"+
          ")").execute();
      conn.prepareStatement("create table sessions (" +
          "user varchar("+Users.UserMaxLength+"), "+
          "session char("+Users.SessionIdLength+"), "+
          "foreign key (user) references users(user)"+
          ")").execute();
      conn.prepareStatement("create table tags (" +
          "user varchar("+Users.UserMaxLength+"), " +
          "resource varchar(256), " +
          "tag varchar(256), " +
          "foreign key (user) references users(user)"+
          ")").execute();
    } catch (SQLException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  @Override
  public void close() {
    try {
      conn.close();
    } catch (SQLException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  @Override
  public void register(String username) throws NoTuplesAffected {
    try {
      PreparedStatement stmt = conn.prepareStatement("insert into users (user) values (?)");
      stmt.setString(1, username);
      int numRows = stmt.executeUpdate();
      if(numRows == 0) throw new NoTuplesAffected();
    } catch (SQLException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  @Override
  public String login(String username) {
    if(!validUser(username)) {
      return null;
    }
    String session = Users.generateSessionId();

    try {
      PreparedStatement stmt = conn.prepareStatement("insert into sessions (user,session) values (?, ?)");

      stmt.setString(1, username);
      stmt.setString(2, session);

      // should create 1 row
      if(1 == stmt.executeUpdate()) {
        return session;
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return null;
  }

  @Override
  public void logout(String username, String token) throws NoTuplesAffected {
    if(!validSession(username, token))
      return;

    try {
      PreparedStatement stmt = conn.prepareStatement("delete from sessions where user=? and session=?");
      stmt.setString(1, username);
      stmt.setString(2, token);

      int numRows = stmt.executeUpdate();
      if(numRows == 0) {
        // since validSession was true,
        // this probably should have worked, barring race conditions
        throw new NoTuplesAffected();
      }
    } catch (SQLException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean validUser(String username) {
    if(username.length() > 64)
      return false;

    boolean found = false;
    try {
      PreparedStatement stmt = conn.prepareStatement("select count(*) from users where user=?");
      stmt.setString(1, username);
      ResultSet results = stmt.executeQuery();

      if(results.next()) {
        int numUsers = results.getInt(1);
        found = (numUsers == 1);
      }
      results.close();

    } catch (SQLException ex) {
      ex.printStackTrace();
    }
    return found;
  }

  @Override
  public boolean validSession(String user, String token) {
    boolean found = false;

    try {
      PreparedStatement stmt = conn.prepareStatement("select (user,session) from sessions where user=? and session=?");
      stmt.setString(1, user);
      stmt.setString(2, token);

      ResultSet results = stmt.executeQuery();
      if(results.next()) {
        found = true;
      }
      results.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return found;
  }

  @Override
  public void checkSession(String user, String token) throws DBError {
    if(!validUser(user)) {
      throw new BadUserException(user);
    }
    if(!validSession(user, token)) {
      throw new BadSessionException(user, token);
    }
  }

  @Override
  public List<String> getTags(String user, String token, String resource) throws DBError {
    checkSession(user, token);
    ArrayList<String> tags = new ArrayList<String>();
    try {
      PreparedStatement stmt = conn.prepareStatement("select tag from tags where user=? and resource=?");
      stmt.setString(1, user);
      stmt.setString(2, resource);

      ResultSet results = stmt.executeQuery();
      while(results.next()) {
        String tag = results.getString(1);
        tags.add(tag);
      }
      results.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return tags;
  }

  @Override
  public void deleteTag(String user, String token, String resource, String tag) throws DBError {
    checkSession(user, token);

    try {
      PreparedStatement stmt = conn.prepareStatement("delete from tags where user=? and resource=? and tag=?");
      stmt.setString(1, user);
      stmt.setString(2, resource);
      stmt.setString(3, tag);
      int numRows = stmt.executeUpdate();

      if(numRows == 0) {
        throw new NoTuplesAffected();
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void addTag(String user, String token, String resource, String tag) throws DBError {
    checkSession(user, token);

    try {
      PreparedStatement stmt = conn.prepareStatement("insert into tags (user,resource,tag) values (?,?,?)");
      stmt.setString(1, user);
      stmt.setString(2, resource);
      stmt.setString(3, tag);
      int numRows = stmt.executeUpdate();
      assert(numRows == 1);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

}
