package ciir.proteus.server;

import org.lemurproject.galago.core.tools.AppFunction;
import org.lemurproject.galago.tupleflow.Parameters;
import org.lemurproject.galago.tupleflow.web.WebServer;

import java.io.File;
import java.io.PrintStream;

public class HTTPMain extends AppFunction {
  public static void main(String[] args) throws Exception {
    Parameters argp = Parameters.parseArgs(args);
    if(new File("server.conf").exists()) {
      argp.setBackoff(Parameters.parseFile("server.conf"));
    }
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

    WebServer server = WebServer.start(argp, router);
    out.println("Server started at: "+ server.getURL());
    server.join();
    out.println("Server shutting down.");
  }
}
