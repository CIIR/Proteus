package ciir.proteus.users.error;

/**
 * @author jfoley.
 */
public class DBError extends Exception {
  public DBError(String message) {
    super(message);
  }
}
