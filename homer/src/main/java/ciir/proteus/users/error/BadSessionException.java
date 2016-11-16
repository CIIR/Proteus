package ciir.proteus.users.error;

/**
 * @author jfoley.
 */
public class BadSessionException extends DBError {
  private final String user;
  private final String token;

  public BadSessionException(String user, String token) {
    super("No session found for "+user+" and "+token);
    this.user = user;
    this.token = token;
  }
}
