package ciir.proteus.users.http;

import ciir.proteus.server.HTTPError;
import ciir.proteus.server.action.JSONHandler;
import ciir.proteus.system.ProteusSystem;
import ciir.proteus.users.UserDatabase;
import ciir.proteus.users.error.DBError;
import org.lemurproject.galago.tupleflow.Parameters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jfoley.
 */
public class GetTags extends DBAction {
  public GetTags(ProteusSystem proteus) {
    super(proteus);
  }

  @Override
  public Parameters handle(String method, String path, Parameters reqp) throws HTTPError {
    String user = reqp.getString("user");
    String token = reqp.getString("token");
    List<String> resources = reqp.getAsList("resource");

    Parameters rtags = new Parameters();
    for(String resource : resources) {
      try {
        List<String> tags = userdb.getTags(user, token, resource);
        rtags.set(resource, tags);
      } catch (DBError dbError) {
        throw new HTTPError(dbError);
      }
    }

    return rtags;
  }
}
