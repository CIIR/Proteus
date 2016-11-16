package ciir.proteus.build;

import org.lemurproject.galago.core.tools.App;

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
