package ciir.proteus.build;

import ciir.proteus.server.HTTPMain;
import org.lemurproject.galago.core.tools.App;
import org.lemurproject.galago.core.tools.AppFunction;

/**
 * Pass through to Galago.
 * @author jfoley
 */
public class Main {
  private static void addFunction(AppFunction fn) {
    App.appFunctions.put(fn.getName(), fn);
  }

  public static void main(String[] args) throws Exception {
    System.out.println("java.class.path: "+System.getProperty("java.class.path"));

    addFunction(new HTTPMain());
    App.run(args);
  }
}
