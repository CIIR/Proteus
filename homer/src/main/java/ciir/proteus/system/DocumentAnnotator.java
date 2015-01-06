package ciir.proteus.system;

import ciir.proteus.users.error.DBError;
import ciir.proteus.util.ListUtil;
import ciir.proteus.util.RetrievalUtil;
import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.retrieval.ScoredDocument;
import org.lemurproject.galago.core.retrieval.ScoredPassage;
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.utility.Parameters;
import org.lemurproject.galago.tupleflow.Utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author jfoley.
 */
public class DocumentAnnotator {

    public static List<Parameters> annotate(ProteusSystem system, String kind, List<String> names, Parameters reqp) throws DBError {
        reqp.put("metadata", false);
        List<ScoredDocument> fakeDocs = new ArrayList<>();
        for (String id : names) {
            fakeDocs.add(new ScoredDocument(id, 0, 0.0));
        }
        return annotate(system, kind, fakeDocs, null, reqp);
    }

    public static List<Parameters> annotate(ProteusSystem system, String kind, List<ScoredDocument> results, Node query, Parameters reqp) throws DBError {
        boolean snippets = reqp.get("snippets", true);
        boolean metadata = reqp.get("metadata", true);
        boolean tags = reqp.get("tags", reqp.isString("user"));

        List<String> names = RetrievalUtil.names(results);

        // retrieve snippets if requested
        if (snippets) {
            results = system.findPassages(kind, query, names);
        }

        // if we need to pull the documents:
        Map<String, Document> pulled = Collections.emptyMap();
        if (snippets || metadata) {
            pulled = system.getDocs(kind, names, metadata, snippets);
        }

        // if we need to get tags for these documents:
        Map<String, Map<Integer, List<String>>> docTags = null;
        if (tags) {
            docTags = system.userdb.getAllTags(RetrievalUtil.names(results));
        }

        // result data
        ArrayList<Parameters> resultData = new ArrayList<>(results.size());
        for (ScoredDocument sdoc : results) {
            Document doc = pulled.get(sdoc.documentName);

            if (doc == null) {
                continue;
            }

            Parameters docp = Parameters.create();

            // default annotations
            docp.put("name", sdoc.documentName);
            docp.put("rank", sdoc.rank);
            docp.put("score", sdoc.score);

            // metadata annotation
            if (metadata) {
                docp.put("meta", Parameters.parseMap(doc.metadata));
            }
            // snippet annotation
            if (snippets) {
                ScoredPassage psg = (ScoredPassage) sdoc;
                String snippet
                        = (Utility.join(ListUtil.slice(doc.terms, psg.begin, psg.end), " "));

                docp.put("snippet", snippet);
            }

            // tags annotation
            if (docTags != null) {

                // get the tags for this resource
                if (docTags.containsKey(sdoc.documentName)) {
                    Parameters tmp = Parameters.create();
                    for (Map.Entry<Integer, List<String>> entry : docTags.get(sdoc.documentName).entrySet()) {
                        tmp.put(entry.getKey().toString(), entry.getValue());
                    }
                    if (tmp.size() == 0) {
                        docp.set("tags", new ArrayList<String>()); // empty list of tags
                    } else {
                        docp.set("tags", tmp);
                    }
                } else {
                    docp.set("tags", new ArrayList<String>()); // empty list of tags
                }
            } // end if we want tags

            resultData.add(docp);
        }

        // return annotated data:
        return resultData;
    }
}
