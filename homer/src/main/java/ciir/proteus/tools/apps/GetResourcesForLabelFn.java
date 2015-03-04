package ciir.proteus.tools.apps;

import ciir.proteus.users.UserDatabase;
import ciir.proteus.users.UserDatabaseFactory;
import org.lemurproject.galago.utility.Parameters;
import org.lemurproject.galago.utility.tools.AppFunction;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by michaelz on 3/3/2015.
 */

public class GetResourcesForLabelFn extends AppFunction {
  @Override
  public String getName() {
    return "resources-for-label";
  }

  @Override
  public String getHelpString() {
    return makeHelpStr("label", "label to get the documents for", "path", "path to the database, relative to current location. Ex: localdb/users");
  }

  @Override
  public void run(Parameters p, PrintStream output) throws Exception {

    // allow to override the user - helpful when unit testing because
    // the TestEnvironment uses the user "junit"
    if (!p.containsKey("user")) {
      p.put("user", "sa");
    }
    UserDatabase userdb = UserDatabaseFactory.instance(p);

    List<String> resList;
    List<String> labels = new ArrayList<>();
    String label = p.getAsString("label");
    // if there is no ":", add the default type
    if (!label.contains(":")){
      label = "*:" + label;
    }
    labels.add(label);

    resList = userdb.getResourcesForLabels(-1, labels);
    for (String res : resList) {
      output.println(res);
    }

  }
}
