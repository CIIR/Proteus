package ciir.proteus.server;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.lemurproject.galago.tupleflow.Parameters;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class HTTPMain {
  public static void main(String[] args) throws Exception {
    Parameters argp = Parameters.parseArgs(args);
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

    System.out.println("Server started at:"+server.getURI());

    server.join();
    System.out.println("Server shutting down.");
  }
}
