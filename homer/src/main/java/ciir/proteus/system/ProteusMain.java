package ciir.proteus.system;

import ciir.proteus.server.HTTPRouter;
import org.lemurproject.galago.utility.tools.AppFunction;
import org.lemurproject.galago.utility.Parameters;
import org.lemurproject.galago.tupleflow.web.WebServer;

import java.io.File;
import java.io.PrintStream;

public class ProteusMain extends AppFunction {

    public static void main(String[] args) throws Exception {
        Parameters argp = Parameters.parseArgs(args);
        if (new File("server.conf").exists()) {
            argp.setBackoff(Parameters.parseFile("server.conf"));
        }
        AppFunction self = new ProteusMain();
        self.run(argp, System.out);
    }

    @Override
    public String getName() {
        return "proteus";
    }

    @Override
    public String getHelpString() {
        return "java -jar homer.jar " + getName() + " conf.json\n\n"
                + "The configuration should be something like this:\n"
                + "{\n"
                + "  \"defaultKind\":\"pages\",\n"
                + "  \"kinds\": {\n"
                + "    \"books\": {\"index\": \"/path/to/indices/books\"},\n"
                + "    \"pages\": {\"index\": \"/path/to/indices/pages\"},\n"
                + "  }\n"
                + "}\n";
    }

    @Override
    public void run(Parameters argp, PrintStream out) throws Exception {
        final ProteusSystem proteus = new ProteusSystem(argp);
        final HTTPRouter router = new HTTPRouter(proteus);

        WebServer server = WebServer.start(argp, router);
        out.println("Server started at: " + server.getURL());
        server.join();
        out.println("Server shutting down.");
    }
}
