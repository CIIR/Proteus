package ciir.proteus.users.http;

import ciir.proteus.server.HTTPError;
import ciir.proteus.system.ProteusSystem;
import ciir.proteus.users.Credentials;
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
    Credentials creds = Credentials.fromJSON(reqp);
    List<String> resources = reqp.getAsList("resource", String.class);

    log.info("GetTags creds="+creds+" resources="+resources);

    Parameters rtags = new Parameters();
    for(String resource : resources) {
      try {
        List<String> tags = userdb.getTags(creds, resource);
        rtags.set(resource, tags);
      } catch (DBError dbError) {
        throw new HTTPError(dbError);
      }
    }

    return rtags;
  }
}
