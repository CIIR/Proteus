package ciir.proteus.build;

import ciir.proteus.jobs.BuildEntityCorpus;
import ciir.proteus.jobs.BuildPictureStore;
import ciir.proteus.jobs.BuildWordDateIndex;
import java.io.File;
import org.lemurproject.galago.core.tools.App;
import org.lemurproject.galago.core.tools.AppFunction;

/**
 *
 * @author jfoley
 */
public class Main {
  private static void addFunction(AppFunction fn) {
    App.appFunctions.put(fn.getName(), fn);
  }
  public static void main(String[] args) throws Exception {
    System.out.println("java.class.path: "+System.getProperty("java.class.path"));
    
    addFunction(new BuildEntityCorpus());
    addFunction(new BuildPictureStore());
    addFunction(new BuildWordDateIndex());
    App.run(args);
  }
}
