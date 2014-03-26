package ciir.proteus.server;

/**
 * @author jfoley.
 */
public class HTTPError extends Exception {
  public static final int BadRequest = 400;
  public static final int NotFound = 404;
  public static final int InternalError = 501;

  public final int status;
  private final String message;

  public HTTPError(int status, String message) {
    this.status = status;
    this.message = message;
  }

  public HTTPError(Exception exc) {
    this.status = InternalError;
    this.message = exc.getMessage();
  }

  @Override
  public String getMessage() {
    return message;
  }
}
