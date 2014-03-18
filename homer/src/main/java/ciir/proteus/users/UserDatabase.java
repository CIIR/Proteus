package ciir.proteus.users;

import ciir.proteus.users.error.DBError;
import ciir.proteus.users.error.NoTuplesAffected;

import java.util.List;

/**
 * @author jfoley.
 */
public interface UserDatabase {

  /**
   * generate tables, call this once; because "if not exists" is annoyingly non-standard
   */
  public void initDB();

  /**
   * shut down connection
   */
  public void close();

  /**
   * Add a user to the database
   * @param username
   */
  public void register(String username) throws NoTuplesAffected;

  /**
   * Logs in a
   * @param username the user to login
   * @return a session token for that user
   */
  public String login(String username);

  /**
   * Logs out a user/session combo
   */
  public void logout(String username, String session) throws NoTuplesAffected;

  /**
   * Returns true if user has registered
   * @param user a username
   * @return registered?
   */
  public boolean validUser(String user);

  /**
   * Returns a user record for a given session
   * @param token a token given as part of a request
   * @return user object for the logged in user or null
   */
  public boolean validSession(String user, String token);

  public void checkSession(String user, String token) throws DBError;

  public List<String> getTags(String user, String token, String resource) throws DBError;
  public void deleteTag(String user, String token, String resource, String tag) throws DBError;
  public void addTag(String user, String token, String resource, String tag) throws DBError;
}
