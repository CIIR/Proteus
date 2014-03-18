package ciir.proteus.server;

import ciir.proteus.server.action.DebugHandler;
import ciir.proteus.server.action.GetMetadata;
import ciir.proteus.server.action.JSONHandler;
import ciir.proteus.server.action.JSONSearch;
import ciir.proteus.system.ProteusSystem;
import ciir.proteus.util.HTTPUtil;
import org.lemurproject.galago.tupleflow.Parameters;
import org.lemurproject.galago.tupleflow.web.WebHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class HTTPRouter implements WebHandler {
  final ProteusSystem system;
  private final JSONSearch jsonSearch;
  private final GetMetadata metadata;
  private final JSONHandler debug;

  public HTTPRouter(Parameters argp) {
    system = new ProteusSystem(argp);
    jsonSearch = new JSONSearch(system, argp);
    metadata = new GetMetadata(system, argp);
    debug = new DebugHandler();
  }

  // handle http requests
  @Override
  public void handle(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String method = req.getMethod();
    String path = req.getPathInfo();
    Parameters reqp = HTTPUtil.fromHTTPRequest(req);

    if(path.equals("/search/json") &&
        (method.equals("GET") || method.equals("POST"))) {
      handleJSON(jsonSearch, method, path, reqp, resp);
    } else if(path.equals("/metadata") &&
        method.equals("GET")) {
      handleJSON(metadata, method, path, reqp, resp);
    }

    handleJSON(debug, method, path, reqp, resp);
  }

  // forward to JSON handler interface
  private void handleJSON(JSONHandler which, String method, String path, Parameters reqp, HttpServletResponse resp) throws IOException {
    resp.addHeader("Access-Control-Allow-Origin", "*");
    resp.addHeader("Content-Type", "application/json");

    Parameters response = which.handle(method, path, reqp);

    PrintWriter pw = resp.getWriter();
    pw.write(response.toString());
    pw.flush();
    pw.close();
    resp.setStatus(200);
  }
}
