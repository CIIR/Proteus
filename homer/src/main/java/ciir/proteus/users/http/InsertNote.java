package ciir.proteus.users.http;

import ciir.proteus.system.ProteusSystem;
import ciir.proteus.users.Credentials;
import ciir.proteus.util.logging.ClickLogHelper;
import ciir.proteus.util.logging.AddNoteLogData;
import ciir.proteus.util.logging.LogHelper;
import org.lemurproject.galago.utility.Parameters;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author michaelz.
 */
public class InsertNote extends DBAction {

    public InsertNote(ProteusSystem proteus) {
        super(proteus);
    }

    @Override
    public Parameters handle(String method, String path, Parameters reqp, HttpServletRequest req) throws Exception {
        Integer id = -1;

        Credentials creds = Credentials.fromJSON(reqp);
        String nullStr = null;
        String res = reqp.get("uri", nullStr);
        String data = reqp.toString();
        Integer corpusid = reqp.get("corpus", -1);
        id = userdb.insertNote(creds, corpusid, res, data);

        // add any subcorpus labels for this comment
        List<Parameters> labels = reqp.getAsList("subcorpusLabels", Parameters.class);
        for (Parameters label : labels){
            if (label.containsKey("checked") == false){
                continue;
            }
            if (label.get("checked", true)){
                userdb.addVoteForResource(creds, res + "_" + id, corpusid, label.getInt("subcorpusid"), -1);
            }
            // it's possible that we get a false value for checked, but since this is a new note,
            // there's no reason to remove it because it doesn't exist.

        } // end loop through labels

        system.loadNoteIndex();

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
