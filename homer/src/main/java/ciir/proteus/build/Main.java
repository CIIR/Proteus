package ciir.proteus.build;

import ciir.proteus.system.ProteusMain;
import org.lemurproject.galago.core.tools.App;
import org.lemurproject.galago.utility.tools.AppFunction;

import java.io.File;

/**
 * Pass through to Galago.
 *
 * @author jfoley
 */
public class Main {

    public static void main(String[] args) throws Exception {
//       System.out.println("java.class.path: " + System.getProperty("java.class.path"));
//        System.out.println("cwd: " + (new File(".")).getAbsolutePath());
        // "register" any Proteus functionality (children of AppFunction) that can be called via the command line
        App.processClassPath("ciir.proteus");

        App.run(args);
    }
}
