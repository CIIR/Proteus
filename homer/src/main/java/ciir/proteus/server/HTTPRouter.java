package ciir.proteus.server;

import ciir.proteus.server.action.DebugHandler;
import ciir.proteus.server.action.GetMetadata;
import ciir.proteus.server.action.JSONHandler;
import ciir.proteus.server.action.JSONSearch;
import ciir.proteus.system.ProteusSystem;
import ciir.proteus.users.http.GetTags;
import ciir.proteus.users.http.LoginUser;
import ciir.proteus.users.http.LogoutUser;
import ciir.proteus.users.http.RegisterUser;
import ciir.proteus.util.HTTPUtil;
import org.lemurproject.galago.tupleflow.Parameters;
import org.lemurproject.galago.tupleflow.web.WebHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HTTPRouter implements WebHandler {
  private static final Logger log = Logger.getLogger(HTTPRouter.class.getName());
  private final JSONHandler debug;

  private final JSONHandler search;
  private final JSONHandler metadata;
  private final JSONHandler tags;
  private final JSONHandler login;
  private final JSONHandler logout;
  private final JSONHandler register;

  public HTTPRouter(ProteusSystem proteus) {
    debug = new DebugHandler();

    search = new JSONSearch(proteus);
    metadata = new GetMetadata(proteus);
    tags = new GetTags(proteus);
    login = new LoginUser(proteus);
    logout = new LogoutUser(proteus);
    register = new RegisterUser(proteus);
  }

  // handle http requests
  @Override
  public void handle(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String method = req.getMethod();
    String path = req.getPathInfo();
    Parameters reqp = HTTPUtil.fromHTTPRequest(req);

    final boolean GET = method.equals("GET");
    final boolean POST = method.equals("POST");
    final boolean PUT = method.equals("PUT");
    final boolean DELETE = method.equals("DELETE");

    JSONHandler handler = debug;
    if((GET || POST) && path.equals("/search")) {
      handler = search;
    } else if(GET && path.equals("/metadata")) {
      handler = metadata;
    } else if(GET && path.equals("/tags")) {
      handler = tags;
    } else if(POST && path.equals("/login")) {
      handler = login;
    } else if(POST && path.equals("/logout")) {
      handler = logout;
    } else if(POST && path.equals("/register")) {
      handler = register;
    }
    handleJSON(handler, method, path, reqp, resp);
  }

  // forward to JSON handler interface
  private void handleJSON(JSONHandler which, String method, String path, Parameters reqp, HttpServletResponse resp) throws IOException {
    resp.addHeader("Access-Control-Allow-Origin", "*");
    resp.addHeader("Content-Type", "application/json");

    try {
      Parameters response = which.handle(method, path, reqp);
      PrintWriter pw = resp.getWriter();
      pw.write(response.toString());
      pw.flush();
      pw.close();
      resp.setStatus(200);
    } catch (HTTPError httpError) {
      // custom error type carries a HTTP status code
      log.log(Level.INFO, "http-error", httpError);
      resp.sendError(httpError.status, httpError.getMessage());
    } catch (IllegalArgumentException argex) {
      // Parameters.get failed
      log.log(Level.INFO, "illegal argument received", argex);
      resp.sendError(HTTPError.BadRequest, argex.getMessage());
    }

  }
}
