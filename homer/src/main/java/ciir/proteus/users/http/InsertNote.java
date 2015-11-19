package ciir.proteus.users.http;

import ciir.proteus.server.HTTPError;
import ciir.proteus.system.ProteusSystem;
import ciir.proteus.users.Credentials;
import ciir.proteus.users.error.DBError;
import ciir.proteus.util.logging.ClickLogHelper;
import ciir.proteus.util.logging.AddNoteLogData;
import ciir.proteus.util.logging.LogHelper;
import org.lemurproject.galago.core.index.mem.FlushToDisk;
import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.parse.Tag;
import org.lemurproject.galago.core.parse.TagTokenizer;
import org.lemurproject.galago.core.retrieval.Retrieval;
import org.lemurproject.galago.core.retrieval.RetrievalFactory;
import org.lemurproject.galago.utility.Parameters;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * @author michaelz.
 */
public class InsertNote extends DBAction {

    public InsertNote(ProteusSystem proteus) {
        super(proteus);
    }

    @Override
    public Parameters handle(String method, String path, Parameters reqp, HttpServletRequest req) throws HTTPError, DBError, IOException {
        Integer id = -1;

        Credentials creds = Credentials.fromJSON(reqp);
        String nullStr = null;
        String res = reqp.get("uri", nullStr);
        String data = reqp.toString();
        Integer corpusid = reqp.get("corpus", -1);
        id = userdb.insertNote(creds, corpusid, res, data);

        if (system.noteIndex != null){
            TagTokenizer tok = new TagTokenizer();

            // TODO dup code, should have an addNoteToIndex() funciton
            Document d = new Document();
            d.name = reqp.get("uri") + "_" + id;
            d.text = reqp.getString("user").split("@")[0] + " : " + reqp.get("quote") + " : " + reqp.get("text");
            d.tags = new ArrayList<Tag>();
            d.metadata = new HashMap<String,String>();
            // TODO : do we use metadata for things like who made the note, etc?
            d.metadata.put("docType", "note");
            tok.process(d);
            system.noteIndex.process(d);

            String noteIndexPath = system.getConfig().get("noteIndex", "????");
            // flush the index to disk
            FlushToDisk.flushMemoryIndex(system.noteIndex, noteIndexPath);

            Retrieval retrieval = system.getRetrieval("ia-corpus");
            Parameters globalParams = retrieval.getGlobalParameters();
            List<String> idx = new ArrayList<String>();
            idx.addAll(globalParams.getAsList("index"));

            // only add the note index path if it's not already there
            if (idx.contains(noteIndexPath) == false){
                idx.add(noteIndexPath);
            }

            Parameters newParams = Parameters.create();
            newParams.put("index", idx);

            try {
                system.kinds.put("ia-corpus", RetrievalFactory.create(newParams));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        AddNoteLogData logData = new AddNoteLogData(ClickLogHelper.getID(reqp, req), reqp.get("user", ""));
        logData.setCorpus(corpusid);
        logData.setId(id);
        logData.setData(data);
        logData.setResource(res);
        logData.setCorpusName(reqp.getAsString("corpusName"));
        LogHelper.log(logData, system);

        Parameters ret = Parameters.create();
        ret.put("id", id);
        return ret;
    }
}
