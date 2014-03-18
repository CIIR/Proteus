package ciir.proteus.users.error;

/**
 * @author jfoley.
 */
public class NoTuplesAffected extends DBError {
  public NoTuplesAffected() {
    super("No tuples affected by SQL operation.");
  }
}
