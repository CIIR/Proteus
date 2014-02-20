package ciir.proteus.server;

/**
 * Created by jfoley on 2/20/14.
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
