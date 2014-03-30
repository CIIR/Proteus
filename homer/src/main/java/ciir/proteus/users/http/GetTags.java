package ciir.proteus.users.http;

import ciir.proteus.server.HTTPError;
import ciir.proteus.system.ProteusSystem;
import ciir.proteus.users.error.DBError;
import org.lemurproject.galago.tupleflow.Parameters;

import java.util.List;

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
    String token = reqp.getAsString("token");
    List<String> resources = reqp.getAsList("resource", String.class);

    log.info("GetTags user="+user+" token="+token+" resources="+resources);

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
