package ciir.proteus.users.http;

import ciir.proteus.server.HTTPError;
import ciir.proteus.system.ProteusSystem;
import ciir.proteus.users.Credentials;
import ciir.proteus.users.error.DBError;
import org.lemurproject.galago.utility.Parameters;

import java.util.List;

/**
 * @author jfoley.
 */
public class DeleteTags extends DBAction {

  public DeleteTags(ProteusSystem proteus) {
    super(proteus);
  }

  @Override
  public Parameters handle(String method, String path, Parameters reqp) throws HTTPError, DBError {
    Credentials creds = Credentials.fromJSON(reqp);
    Parameters tags = reqp.getMap("tags");

    for(String tag : tags.keySet()) {
      List<String> resources = tags.getAsList(tag, String.class);
      for(String resource : resources) {
        userdb.deleteTag(creds, resource, tag);
      }
    }

    return Parameters.instance();
  }
}
