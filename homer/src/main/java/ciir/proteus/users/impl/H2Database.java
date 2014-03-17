package ciir.proteus.users.impl;

import ciir.proteus.users.UserDatabase;
import ciir.proteus.users.Users;
import org.lemurproject.galago.tupleflow.Parameters;

import java.sql.*;
import java.util.ArrayList;
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
      // open a connection
      conn = DriverManager.getConnection("jdbc:h2:" + path, dbuser, dbpass);
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      throw new IllegalArgumentException(e);
    } catch (SQLException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  @Override
  public void initDB() {
    try {
      conn.prepareStatement("create table users (" +
          "user varchar("+ Users.UserMaxLength+")"+
          ")").executeUpdate();
      conn.prepareStatement("create table sessions (" +
          "user varchar("+Users.UserMaxLength+"), "+
          "session char("+Users.SessionIdLength+"), "+
          "foreign key (user) references users(user)"+
          ")").executeUpdate();
      conn.prepareStatement("create table tags (" +
          "user varchar("+Users.UserMaxLength+"), " +
          "resource varchar(256), " +
          "tag varchar(256), " +
          "foreign key (user) references users(user)"+
          ")").executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean register(String username) {
    try {
      PreparedStatement stmt = conn.prepareStatement("insert into users (user) values (?)");
      stmt.setString(1, username);
      return stmt.executeUpdate() == 1;
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return false;
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
  public boolean validUser(String username) {
    if(username.length() > 64)
      return false;

    boolean found = false;
    try {
      PreparedStatement stmt = conn.prepareStatement("count * from users where user=?");
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
  public List<String> getTags(String user, String resource) {
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
  public boolean deleteTag(String user, String resource, String tag) {
    return false;
  }

  @Override
  public boolean addTag(String user, String resource, String tag) {
    return false;
  }

}
