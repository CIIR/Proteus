package ciir.proteus.users;


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
   * @return
   */
  public boolean register(String username);

  /**
   * Logs in a
   * @param username the user to login
   * @return a session token for that user
   */
  public String login(String username);

  /**
   * Logs out a user/session combo
   */
  public void logout(String username, String session);

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

  public List<String> getTags(String user, String resource);
  public boolean deleteTag(String user, String resource, String tag);
  public boolean addTag(String user, String resource, String tag);
}
