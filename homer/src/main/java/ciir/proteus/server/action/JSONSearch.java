package ciir.proteus.server.action;

import ciir.proteus.system.DocumentAnnotator;
import ciir.proteus.system.ProteusDocument;
import ciir.proteus.system.ProteusSystem;
import ciir.proteus.users.error.DBError;
import ciir.proteus.util.ListUtil;
import ciir.proteus.util.RetrievalUtil;
import ciir.proteus.util.logging.ClickLogHelper;
import ciir.proteus.util.logging.LogHelper;
import ciir.proteus.util.logging.ResultLogData;

import ciir.proteus.util.logging.SearchLogData;
import org.apache.logging.log4j.LogManager;

import org.lemurproject.galago.utility.Parameters;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class JSONSearch implements JSONHandler {

  private final ProteusSystem system;
  private static final Logger log = Logger.getLogger(JSONSearch.class.getName());
  private static final org.apache.logging.log4j.Logger proteusLog = LogManager.getLogger("Proteus");

  public JSONSearch(ProteusSystem sys) {
    this.system = sys;
  }

  @Override
  public Parameters handle(String method, String path, Parameters reqp, HttpServletRequest req) throws DBError, IOException {

    String query = reqp.getAsString("q");
    String kind = reqp.get("kind", system.defaultKind);
    int numResults = reqp.get("n", 10);
    int skipResults = reqp.get("skip", 0);
    int corpusid = reqp.get("corpus", -1);
    String userid = reqp.get("userid", "-1");
    String action = reqp.get("action", "search");

    List<Long> subcorpora = new ArrayList<>(); // empty list
    List<String> resList = new ArrayList<>(); // empty list

    if (reqp.containsKey("subcorpora")) {
      subcorpora = reqp.getAsList("subcorpora", Long.class);
      resList = system.userdb.getResourcesForSubcorpora(Integer.parseInt(userid), corpusid, subcorpora); // get all

      // if there are no resources for the subcorpora, we have nothing to search
      if (resList.isEmpty()) {
        return Parameters.create();
      }
    }

    // corpus resources
    if (action.equals("search-corpus") && corpusid > 0) {
      // if we're not searching by labels, use the existing list
      if (resList.isEmpty()) {
        resList = system.userdb.getAllResourcesForCorpus(Integer.parseInt(userid), corpusid);

        if (resList.isEmpty()) {
          throw new RuntimeException("The corpus is empty.");
        }
      }
    }
    // when searching wihing one or more subcorpora,
    // the client should turn off the "get more results" functionality, but just in case, make sure we
    // don't keep returning the same results over and over
    if (!resList.isEmpty() && skipResults > 0) {
      Parameters none = Parameters.create();
      none.set("results", Parameters.create());
      return none;
    }

    Parameters qp = Parameters.create();

    if (resList.size() > 0) {
      qp.put("working", resList);
    }

    // it's possible for the query to be empty IF we're searching just by labels or within a corpus
    if (!query.isEmpty()) {
      if (system.getConfig().get("queryType", "simple").equals("simple")) {
        qp.set("queryType", "SimpleQuery");
      } else {
        qp.set("queryType", "StructuredQuery");
      }
    }

    qp.put("requested", numResults + skipResults);

    // setting this to false, otherwise #scale queries fail
    // see: https://sourceforge.net/p/lemur/bugs/272/
    qp.put("deltaReady", false);

    // give them the ability to restrict the results to a working set
    // specified by a query passed in - most likely an archive id
    if (reqp.containsKey("workingSetQuery")) {

      List<ProteusDocument> workingSet = null;

      // NOTE: workingSetQuery is NOT expressed in the simple query language.
      // There are some archive IDs that would get parsed is using the simple
      // query language (ex: poems___00wott) so we use the regular Galago syntax.

      Parameters tmpParams = Parameters.create();
      tmpParams.set("queryType", "StructuredQuery");
      workingSet = system.doSearch(kind, reqp.getAsString("workingSetQuery"), tmpParams);
      if (!workingSet.isEmpty()) {
        qp.put("working", RetrievalUtil.ids(workingSet));
      }
    } // end if we use a query to get the working set

    Parameters response = Parameters.create();

    // save the query
    // TODO: right now we just saave the query, ignoring labels, etc.
    Integer queryid = system.userdb.insertQuery(null, corpusid, query, kind);
    response.set("queryid", queryid);

    Parameters annotations = Parameters.create();
    DocumentAnnotator da = new DocumentAnnotator();

    // ids can be String (external doc ID) or Long (internal doc ID)
    List<ProteusDocument> docs = null;
    // if we're searching using a working set
    if (resList.isEmpty()) {
      docs = ListUtil.drop(system.doSearch(kind, query, qp), skipResults);
      // ??? should do this in doSearch()
      if (!docs.isEmpty()) {

        annotations = da.annotate(this.system, kind, docs, query, reqp);
      }
    } else {

      log.info(resList.toString());
      reqp.set("tags", false); // 2/2016 - tags are currently not used
      reqp.remove("n");// remove the param that says how many to get
      // TODO perhaps pass resList as a param so don't need two annotate funcs with diff signatures - confusing.
      annotations = da.annotate(this.system, kind, query, reqp, resList);

    }

    List<Parameters> results = annotations.getAsList("results");

    response.copyFrom(system.getIndex().getQueryParameters());

    // TODO add subcorpora
    SearchLogData searchData = new SearchLogData(ClickLogHelper.getID(reqp, req), reqp.get("user", "* not logged in *"));
    searchData.setEnteredQuery(query);
    searchData.setExpandedQuery(query.isEmpty() ? "" : response.getAsString("parsedQuery"));
    searchData.setKind(kind);
  //  searchData.setCorpus(corpusName);
    //        searchData.setLabels(labels.toString());
    searchData.setStartAt(skipResults);
    searchData.setNumResults(numResults);
    LogHelper.log(searchData, system);

    ResultLogData logData = new ResultLogData(ClickLogHelper.getID(reqp, req), reqp.get("user", "* not logged in *"));
    logData.setDocIDs(ClickLogHelper.extractDocID(results).toString());
    LogHelper.log(logData, system);

    response.copyFrom(annotations);

    // add sub-corpora
    try {
      Parameters labels = system.userdb.getAllSubCorpora();
      response.copyFrom(labels);
    } catch (DBError dbError) {
      dbError.printStackTrace();
    }

    return response;
  }
}
