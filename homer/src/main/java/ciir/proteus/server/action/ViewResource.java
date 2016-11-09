package ciir.proteus.server.action;

import ciir.proteus.server.HTTPError;
import ciir.proteus.system.ProteusSystem;
import ciir.proteus.users.error.DBError;
import ciir.proteus.util.QueryUtil;
import ciir.proteus.util.logging.ClickLogHelper;
import ciir.proteus.util.logging.LogHelper;
import ciir.proteus.util.logging.ViewResourceLogData;
import org.apache.logging.log4j.LogManager;
import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.core.retrieval.query.SimpleQuery;
import org.lemurproject.galago.utility.Parameters;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
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
    String query = null;
    if (queryid != -1){
      query = system.userdb.getQuery(null, queryid);
      response.put("query", query);
      // assume simple query language
      Node tq = null;
      try {
        tq = SimpleQuery.parseTree(query);
      } catch (IOException e) {
        e.printStackTrace();
      }
      List<String> terms = QueryUtil.queryTerms(tq);
      response.put("queryTerms", terms);
    }

    // get labels.
    Parameters labels = Parameters.create();
    labels = system.userdb.getResourceRatings2(doc.name, reqp.getInt("corpusID"));
    response.copyFrom(labels);

    // get any notes associated with the book
    String bookid = doc.name.split("_")[0];
    Parameters notes = Parameters.create();
    notes = system.userdb.getNotesForBook(bookid, reqp.getInt("corpusID"));
    response.put("bookNotes", notes);

    return response;
  }
}
