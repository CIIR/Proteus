package ciir.proteus.users.error;

/**
 * @author jfoley.
 */
public abstract class DBError extends Exception {
  DBError(String message) {
    super(message);
  }
}
