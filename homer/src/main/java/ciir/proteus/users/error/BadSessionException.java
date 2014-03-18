package ciir.proteus.users.error;

/**
 * @author jfoley.
 */
public class BadSessionException extends DBError {
  public final String user;
  public final String token;

  public BadSessionException(String user, String token) {
    super("No session found for "+user+" and "+token);
    this.user = user;
    this.token = token;
  }
}
