package ciir.proteus.server;

import ciir.proteus.server.action.JSONSearch;
import ciir.proteus.server.action.RequestHandler;
import ciir.proteus.system.SearchSystem;
import org.lemurproject.galago.tupleflow.Parameters;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class HTTPRouter {
  final SearchSystem system;
  private final JSONSearch jsonSearch;

  public HTTPRouter(Parameters argp) {
    system = new SearchSystem(argp);
    jsonSearch = new JSONSearch(system, argp);
  }

  public void handle(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.addHeader("Access-Control-Allow-Origin", "*");

    String method = req.getMethod();
    String path = req.getPathInfo();
    Parameters reqp = HTTPUtil.fromHTTPRequest(req);

    if(path.equals("/search/json") && (method.equals("GET") || method.equals("POST"))) {
      handleJSON(jsonSearch, reqp, resp);
    } else if(path.equals("/debug")) {
      PrintWriter pw = resp.getWriter();
      pw.println("Method: " + method);
      pw.println("Path: "+path);
      pw.println(reqp);
      pw.close();
      resp.setStatus(HttpServletResponse.SC_OK);
    }
  }

  private void handleJSON(RequestHandler which, Parameters reqp, HttpServletResponse resp) throws IOException {
    Parameters response = which.handle(reqp);

    PrintWriter pw = resp.getWriter();
    pw.write(response.toString());
    pw.flush();
    pw.close();
    resp.setStatus(200);
  }
}
