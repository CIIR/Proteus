package ciir.proteus.build;

import org.lemurproject.galago.core.tools.App;

/**
 * Pass through to Galago.
 * @author jfoley
 */
public class Main {
  public static void main(String[] args) throws Exception {
    System.out.println("java.class.path: "+System.getProperty("java.class.path"));
    App.run(args);
  }
}
