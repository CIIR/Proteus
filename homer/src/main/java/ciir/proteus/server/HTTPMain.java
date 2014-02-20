package ciir.proteus.server;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.lemurproject.galago.core.tools.AppFunction;
import org.lemurproject.galago.tupleflow.Parameters;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintStream;

public class HTTPMain extends AppFunction {
  public static void main(String[] args) throws Exception {
    Parameters argp = Parameters.parseArgs(args);
    AppFunction self = new HTTPMain();
    self.run(argp, System.out);
  }

  @Override
  public String getName() {
    return "homer-http";
  }

  @Override
  public String getHelpString() {
    return "java -jar homer.jar "+ getName()+" conf.json\n\n"+
      "The configuration should be something like this:\n"+
      "{\n" +
      "  \"defaultKind\":\"pages\",\n" +
      "  \"kinds\": {\n" +
      "    \"books\": {\"index\": \"/path/to/indices/books\"},\n" +
      "    \"pages\": {\"index\": \"/path/to/indices/pages\"},\n" +
      "  }\n" +
      "}\n";
  }

  @Override
  public void run(Parameters argp, PrintStream out) throws Exception {
    final HTTPRouter router = new HTTPRouter(argp);

    Server server = new Server((short) argp.get("port", 8080));
    server.setHandler(new AbstractHandler() {
      @Override
      public void handle(String s, Request request, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException {
        try {
          router.handle(httpServletRequest, httpServletResponse);
        } catch (Exception ex) {
          ex.printStackTrace();
          httpServletResponse.sendError(501, ex.getMessage());
        }
      }
    });
    server.start();

    out.println("Server started at:"+server.getConnectors()[0].getHost()+":"+server.getConnectors()[0].getPort());

    server.join();
    out.println("Server shutting down.");
  }
}
