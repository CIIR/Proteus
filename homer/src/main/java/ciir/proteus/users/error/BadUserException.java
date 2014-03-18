package ciir.proteus.users.error;

/**
 * @author jfoley.
 */
public class BadUserException extends DBError {
  public BadUserException(String user) {
    super("No such user: "+user);
  }
}
