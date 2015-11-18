package ciir.proteus.users.http;

import ciir.proteus.server.HTTPError;
import ciir.proteus.system.ProteusSystem;
import ciir.proteus.users.Credentials;
import ciir.proteus.users.error.DBError;
import ciir.proteus.util.logging.ClickLogHelper;
import ciir.proteus.util.logging.LogHelper;
import ciir.proteus.util.logging.UpdateNoteLogData;
import org.lemurproject.galago.core.index.mem.FlushToDisk;
import org.lemurproject.galago.core.index.mem.MemoryIndex;
import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.parse.Tag;
import org.lemurproject.galago.core.parse.TagTokenizer;
import org.lemurproject.galago.core.retrieval.Retrieval;
import org.lemurproject.galago.core.retrieval.RetrievalFactory;
import org.lemurproject.galago.utility.Parameters;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author michaelz.
 */
public class UpdateNote extends DBAction {

    public UpdateNote(ProteusSystem proteus) {
        super(proteus);
    }

    @Override
    public Parameters handle(String method, String path, Parameters reqp, HttpServletRequest req) throws Exception {

        Credentials creds = Credentials.fromJSON(reqp);
        String nullStr = null;
        Integer id =  reqp.get("id", -1);
        String data = reqp.toString();
        String res = reqp.get("uri", nullStr);
        Integer corpusid = reqp.get("corpus", -1);
        userdb.updateNote(creds, id, corpusid, data);

        // to delete a note from the index, we have to re-load from the database
        system.loadNoteIndex();

        UpdateNoteLogData logData = new UpdateNoteLogData(ClickLogHelper.getID(reqp, req), reqp.get("user", ""));
        logData.setCorpus(corpusid);
        logData.setId(id);
        logData.setData(data);
        logData.setResource(res);
        logData.setCorpusName(reqp.getAsString("corpusName"));
        LogHelper.log(logData, system);

        return Parameters.create();
    }
}
