package ciir.proteus.server.action;

import ciir.proteus.server.HTTPError;
import ciir.proteus.system.ProteusSystem;
import ciir.proteus.users.error.DBError;
import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.utility.Parameters;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;

/**
 * @author jfoley
 */
public class ViewResource implements JSONHandler {
  private static final Logger log = Logger.getLogger(ViewResource.class.getName());
  private final ProteusSystem system;

  public ViewResource(ProteusSystem sys) {
    this.system = sys;
  }

  @Override
  public Parameters handle(String method, String path, Parameters reqp, HttpServletRequest req) throws HTTPError, DBError {
    assert(reqp.getString("action").equals("view"));
    String docId = reqp.getString("id");
    if(!reqp.isString("kind")) {
      throw new IllegalArgumentException("Expected argument 'kind'");
    }

    String kind = reqp.getString("kind");
    Document doc = null;
    try {
      doc = system.getRetrieval(kind).getDocument(docId, new Document.DocumentComponents(true, true, false));
    } catch (IOException e) {
      log.log(Level.WARNING, "IOException while trying to get document=" + docId + " for kind=" + kind, e);
    }

    Parameters response = Parameters.create();
    if(doc == null) {
      response.put("found", false);
      return response;
    }
    response.put("found", true);

    Parameters metadata = Parameters.create();
    for(Map.Entry<String,String> kv : doc.metadata.entrySet()) {
      metadata.put(kv.getKey(), kv.getValue());
    }
    response.put("metadata", metadata);
    response.put("text", doc.text);

    return response;
  }
}
