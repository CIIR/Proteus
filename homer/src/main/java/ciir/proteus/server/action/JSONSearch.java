package ciir.proteus.server.action;

import ciir.proteus.system.DocumentAnnotator;
import ciir.proteus.system.ProteusSystem;
import ciir.proteus.users.error.DBError;
import ciir.proteus.util.logging.ClickLogHelper;
import ciir.proteus.util.ListUtil;
import ciir.proteus.util.QueryUtil;
import java.io.IOException;

import ciir.proteus.util.logging.LogHelper;
import ciir.proteus.util.logging.ResultLogData;
import ciir.proteus.util.logging.SearchLogData;
import org.lemurproject.galago.core.retrieval.ScoredDocument;
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.core.retrieval.query.StructuredQuery;
import org.lemurproject.galago.core.retrieval.query.SimpleQuery;
import org.lemurproject.galago.utility.Parameters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;

public class JSONSearch implements JSONHandler {

    private final ProteusSystem system;
    public static final Logger log = Logger.getLogger(JSONSearch.class.getName());
    private static final org.apache.logging.log4j.Logger proteusLog = LogManager.getLogger("Proteus");

    public JSONSearch(ProteusSystem sys) {
        this.system = sys;
    }

    @Override
    public Parameters handle(String method, String path, Parameters reqp, HttpServletRequest req) throws DBError, IOException {

        String query = reqp.getAsString("q");
        String kind = reqp.get("kind", system.defaultKind);
        int numResults = (int) reqp.get("n", 10);
        int skipResults = (int) reqp.get("skip", 0);
        int corpusid = (int) reqp.get("corpus", -1);
        String userid = reqp.get("userid", "-1");
        String action = reqp.get("action", "search");
        String corpusName = reqp.get("corpusName", "");

        List<Long> subcorpora = new ArrayList<>(); // empty list
        List<String> resList =  new ArrayList<>(); // empty list
//        List<String> labels = new ArrayList<>(); // empty list
//        if (reqp.containsKey("labels")) {
//            labels = reqp.getAsList("labels", String.class);
//            // we pass in labels on the URL so it's possible that someone could share
//            // a URL with you that has THEIR tags. So we get the same results, we'll use the
//            // "labelOwner" to get the labels.
//            resList = system.userdb.getResourcesForLabels(Integer.parseInt(reqp.get("labelOwner", userid)), labels); // get all
//            log.info("We have labels: " + labels.toString());
        if (reqp.containsKey("subcorpora")) {
            subcorpora = reqp.getAsList("subcorpora", Long.class);
            resList = system.userdb.getResourcesForSubcorpora(Integer.parseInt(userid), corpusid, subcorpora); // get all

            // if there are no resources for the subcorpora, we have nothing to search
            if (resList.size() == 0){
                return Parameters.create();
            }

            log.info("We have subcorpora: " + subcorpora.toString());

            } else {
            // if we're searching by labels display ALL so only check if we
            // don't have labels
            if (numResults > 1000) {
                throw new IllegalArgumentException("Let's not put too many on a page...");
            }
        }

        // corpus resources
        if (action.equals("search-corpus") && corpusid > 0) {
            // if we're not searching by labels, use the existing list
            if (resList.isEmpty()) {
                resList = system.userdb.getAllResourcesForCorpus(Integer.parseInt(userid), corpusid);
            } else {
                // MCZ 11/2015 - this logic was when the resList was for labels. Now that we're using
                // sub-corpora, we don't need this step.
//                // TODO: may want to use a set for this, but order may be important to the user
//                // a bit more work to do...
//                List<String> tmpResList = new ArrayList<>();
//                tmpResList = system.userdb.getAllResourcesForCorpus(Integer.parseInt(userid), corpusid);
//                for (String s : tmpResList){
//                    if (!resList.contains(s)){
//                        resList.add(s);
//                    }
//                }
            }
            if (resList.isEmpty()) {
                throw new RuntimeException("The corpus is empty.");
            }
        }

        Node pquery = null;

        // save the query
        // TODO: right now we just saave the query, ignoring labels, etc.
        // TODO : ??? return query id?
        Integer queryid = system.userdb.insertQuery(null, corpusid, query, kind);

        // it's possible for the query to be empty IF we're searching just by labels or within a corpus
        if (!query.isEmpty()) {
            if (system.getConfig().get("queryType", "simple").equals("simple")) {
                pquery = SimpleQuery.parseTree(query);
            } else {
                pquery = StructuredQuery.parse(query);
            }
        }

        // TODO add subcorpora
        SearchLogData searchData = new SearchLogData(ClickLogHelper.getID(reqp, req), reqp.get("user", "* not logged in *"));
        searchData.setEnteredQuery(query);
        searchData.setExpandedQuery((pquery == null ? "" : pquery.toString()));
        searchData.setKind(kind);
        searchData.setCorpus(corpusName);
//        searchData.setLabels(labels.toString());
        searchData.setStartAt(skipResults);
        searchData.setNumResults(numResults);
        LogHelper.log(searchData, system);

        Parameters qp = Parameters.create();
        qp.put("requested", numResults + skipResults);

        // give them the ability to restrict the results to a working set
        // specified by a query passed in - most likely an archive id
        // TODO ???? add tests!!! and log?
        if (reqp.containsKey("workingSetQuery")) {
            List<ScoredDocument> workingSet = null;
            Node tmpQuery = null;

            if (system.getConfig().get("queryType", "simple").equals("simple")) {
                tmpQuery = SimpleQuery.parseTree( reqp.getAsString("workingSetQuery"));
            } else {
                tmpQuery = StructuredQuery.parse( reqp.getAsString("workingSetQuery"));
            }
            workingSet = system.search(kind, tmpQuery, Parameters.create());
            List<String> docNames = new ArrayList<String>(workingSet.size());
            for (ScoredDocument doc : workingSet){
                docNames.add(doc.getName());
            }
            if (docNames.size() > 0){
                qp.put("working", docNames);
            }
        }

        Parameters response = Parameters.create();

        List<Parameters> results = Collections.emptyList();
        List<ScoredDocument> docs = null;
        // if we're searching using a working set
        if (resList.isEmpty()) {
            docs = ListUtil.drop(system.search(kind, pquery, qp), skipResults);
            if (!docs.isEmpty()) {
                results = DocumentAnnotator.annotate(this.system, kind, docs, pquery, reqp);
            }
        } else {
            docs = new ArrayList<>();
            for (String id : resList) {
                docs.add(new ScoredDocument(id, 0, 0.0));
            }
            if (!docs.isEmpty()) {
                log.info(docs.toString());
                reqp.set("tags", true);
                // remove the param that says how many to get
                reqp.remove("n");
                results = DocumentAnnotator.annotate(this.system, kind, docs, pquery, reqp);
            }
        }

        ResultLogData logData = new ResultLogData(ClickLogHelper.getID(reqp, req), reqp.get("user", "* not logged in *"));
        logData.setDocIDs(ClickLogHelper.extractDocID(results).toString());
        LogHelper.log(logData, system);

        response.set("queryid", queryid);
        response.set("results", results);
        if (pquery != null) {
            response.set("parsedQuery", pquery.toString());
            response.set("queryTerms", QueryUtil.queryTerms(pquery));
        }

        return response;
    }

}
