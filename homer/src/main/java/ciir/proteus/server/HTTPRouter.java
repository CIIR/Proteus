package ciir.proteus.server;

import ciir.proteus.server.action.DebugHandler;
import ciir.proteus.server.action.GetMetadata;
import ciir.proteus.server.action.JSONHandler;
import ciir.proteus.server.action.JSONSearch;
import ciir.proteus.server.action.JSONAdvSearch;
import ciir.proteus.system.ProteusSystem;
import ciir.proteus.users.error.DBError;
import ciir.proteus.users.http.*;
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
  private final JSONHandler advancedSearch;
  private final JSONHandler metadata;
  private final JSONHandler tags;
  private final JSONHandler putTags;
  private final JSONHandler deleteTags;
  private final JSONHandler login;
  private final JSONHandler logout;
  private final JSONHandler register;
  private final StaticContentHandler staticContent;

  public HTTPRouter(ProteusSystem proteus) {
    debug = new DebugHandler();

    staticContent = new StaticContentHandler(proteus.getConfig());

    search = new JSONSearch(proteus);
    advancedSearch = new JSONAdvSearch(proteus);
    metadata = new GetMetadata(proteus);
    tags = new GetTags(proteus);
    putTags = new PutTags(proteus);
    deleteTags = new DeleteTags(proteus);
    login = new LoginUser(proteus);
    logout = new LogoutUser(proteus);
    register = new RegisterUser(proteus);
  }

  // handle http requests
  @Override
  public void handle(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    try {
      String method = req.getMethod();
      String path = req.getPathInfo();
      Parameters reqp = HTTPUtil.fromHTTPRequest(req);

      final boolean GET = method.equals("GET");
      final boolean POST = method.equals("POST");

      JSONHandler handler;
      if((GET || POST) && path.equals("/api/search")) {
        handler = search;
      } else if((GET || POST) && path.equals("/api/advanced")) {
        handler = advancedSearch;
      } else if(GET && path.equals("/api/metadata")) {
        handler = metadata;
      } else if(GET && path.equals("/api/tags")) {
        handler = tags;
      } else if(POST && path.equals("/api/tags/create")) {
        handler = putTags;
      } else if(POST && path.equals("/api/tags/delete")) {
        handler = deleteTags;
      } else if(POST && path.equals("/api/login")) {
        handler = login;
      } else if(POST && path.equals("/api/logout")) {
        handler = logout;
      } else if(POST && path.equals("/api/register")) {
        handler = register;
      } else if(path.equals("/api/debug")) {
        handler = debug;
      } else if(GET && !path.startsWith("/api/")) {
        staticContent.handle(path, reqp, resp);
        return;
      } else {
        resp.sendError(404, "Not found");
        return;
      }
      handleJSON(handler, method, path, reqp, resp);
    } catch (Throwable th) {
      th.printStackTrace();
      resp.sendError(501, th.getMessage());
    }
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
    } catch (DBError dbError) {
      log.log(Level.WARNING, "database-error", dbError);
      resp.sendError(HTTPError.InternalError, dbError.getMessage());
    }

  }
}
