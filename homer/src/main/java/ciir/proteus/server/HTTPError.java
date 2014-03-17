package ciir.proteus.server;

/**
 * @author jfoley.
 */
public class HTTPError extends Exception {
  public final int status;
  private final String message;

  public HTTPError(int status, String message) {
    this.status = status;
    this.message = message;
  }

  public HTTPError(Exception exc) {
    this.status = 501;
    exc.printStackTrace();
    this.message = exc.getMessage();
  }
}
