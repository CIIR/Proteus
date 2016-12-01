package ciir.proteus.server.action;

import ciir.proteus.server.HTTPError;
import ciir.proteus.system.ProteusDocument;
import ciir.proteus.system.ProteusSystem;
import ciir.proteus.users.error.DBError;
import ciir.proteus.util.logging.ClickLogHelper;
import ciir.proteus.util.logging.LogHelper;
import ciir.proteus.util.logging.ViewResourceLogData;
import org.apache.logging.log4j.LogManager;

import org.lemurproject.galago.utility.Parameters;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;

/**
 * @author jfoley, michaelz
 */
public class ViewResource implements JSONHandler {
  private static final Logger log = Logger.getLogger(ViewResource.class.getName());
  private final ProteusSystem system;
  private static final org.apache.logging.log4j.Logger proteusLog = LogManager.getLogger("Proteus");

  public ViewResource(ProteusSystem sys) {
    this.system = sys;
  }

  @Override
  public Parameters handle(String method, String path, Parameters reqp, HttpServletRequest req) throws HTTPError, DBError, IOException {
    assert(reqp.getString("action").equals("view"));
    String docId = reqp.getString("id");
    if(!reqp.isString("kind")) {
      throw new IllegalArgumentException("Expected argument 'kind'");
    }
    Integer queryid = -1;

    if (reqp.isString("queryid")){
      queryid = Integer.parseInt(reqp.get("queryid", "-1"));
    } else {
      queryid = reqp.get("queryid", -1);
    }

    String kind = reqp.getString("kind");
    ProteusDocument doc = system.getIndex().getDocument(kind, docId, true, true);

    Parameters response = Parameters.create();
    if(doc == null) {
      response.put("found", false);
      return response;
    }
    response.put("found", true);

    Parameters noteParams = system.getConfig().get("notes", Parameters.create());
    response.put("fields", noteParams.getAsList("noteFields", String.class));

    ViewResourceLogData logData = new ViewResourceLogData(ClickLogHelper.getID(reqp, req), reqp.get("user", ""));
    logData.setDocIDs(docId);
    logData.setKind(kind);
    LogHelper.log(logData, system);

    Parameters metadata = Parameters.create();
    for(Map.Entry<String,String> kv : doc.metadata.entrySet()) {
      metadata.put(kv.getKey(), kv.getValue());
    }
    response.put("metadata", metadata);
    response.put("text", doc.text);

    // get the query that "found" this document
    if (queryid != -1){
      String query = system.userdb.getQuery(null, queryid);
      response.put("query", query);
      response.put("queryTerms", system.getIndex().getQueryTerms(query));
    }

    // get labels.
    Parameters labels = system.userdb.getResourceLabels(doc.name, reqp.getInt("corpusID"));
    response.copyFrom(labels);

    // get any notes associated with the book
    String bookid = doc.name.split("_")[0];
    Parameters notes = Parameters.create();
    notes = system.userdb.getNotesForBook(bookid, reqp.getInt("corpusID"));
    response.put("bookNotes", notes);

    return response;
  }
}
