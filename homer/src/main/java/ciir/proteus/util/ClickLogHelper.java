package ciir.proteus.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.lemurproject.galago.core.retrieval.ScoredDocument;
import org.lemurproject.galago.utility.Parameters;

/**
 *
 * @author michaelz
 */
public class ClickLogHelper {

    // if we have a token (a user is logged in) use that unique ID, else
    // use their IP.
    static public String getID(Parameters reqp, HttpServletRequest req) {

        if (reqp == null || reqp.get("token") == null || reqp.getString("token").isEmpty()) {
            if (req == null) {
                return null;
            }
            return req.getRemoteAddr();
        } else {
            return reqp.get("token").toString();
        }

    }

    static public List<String> extractDocID(List<?> list) {

        if (list == null || list.isEmpty()) {
            List<String> emptyList = Collections.emptyList();
            return emptyList;
        }
        List<String> docs = new ArrayList<>();

        Object o = list.get(0);
        // we're assuming all objects are of the same type
        if (o instanceof ScoredDocument) {
            for (Object d : list) {
                docs.add(((ScoredDocument) d).getName());
            }
            return docs;
        } else if (o instanceof Parameters) {
            for (Object d : list) {
                String doc = ((Parameters) d).get("name", "");
                if (!doc.isEmpty()) {
                    docs.add(doc);
                }
            }
            return docs;
        } else {
            throw new RuntimeException("Unknown object type");
        }

    }

}
