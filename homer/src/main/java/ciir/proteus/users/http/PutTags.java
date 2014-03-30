package ciir.proteus.users.http;

import ciir.proteus.server.HTTPError;
import ciir.proteus.system.ProteusSystem;
import ciir.proteus.users.error.DBError;
import org.lemurproject.galago.tupleflow.Parameters;

import java.util.List;

/**
 * @author jfoley.
 */
public class PutTags extends DBAction {
  public PutTags(ProteusSystem proteus) {
    super(proteus);
  }

  @Override
  public Parameters handle(String method, String path, Parameters reqp) throws HTTPError, DBError {
    String user = reqp.getString("user");
    String token = reqp.getAsString("token");
    Parameters tags = reqp.getMap("tags");

    for(String tag : tags.keySet()) {
      List<String> resources = tags.getAsList(tag, String.class);
      for(String resource : resources) {
        userdb.addTag(user, token, resource, tag);
      }
    }

    return new Parameters();
  }
}
