package ciir.proteus.parse;

import java.io.IOException;
import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.parse.Tag;
import org.lemurproject.galago.tupleflow.Counter;
import org.lemurproject.galago.tupleflow.InputClass;
import org.lemurproject.galago.tupleflow.OutputClass;
import org.lemurproject.galago.tupleflow.StandardStep;
import org.lemurproject.galago.tupleflow.TupleFlowParameters;
import org.lemurproject.galago.tupleflow.Utility;
import org.lemurproject.galago.tupleflow.execution.Verified;
import ciir.proteus.types.IndexLink;

@Verified
@InputClass(className = "org.lemurproject.galago.core.parse.Document")
@OutputClass(className = "ciir.proteus.types.IndexLink")
public class IndexLinkGenerator extends StandardStep<Document, IndexLink> {

    private Counter linkCounter;

    public IndexLinkGenerator(TupleFlowParameters parameters) {
	linkCounter = parameters.getCounter("Links Generated");
    }

    public void process(Document document) throws IOException {
	int linkCount = 0;
	String srcid = document.metadata.get("id");
	String srctype = document.metadata.get("type");
	int srcpos = Integer.parseInt(document.metadata.get("pos"));
	for (Tag tag : document.tags) {
	    IndexLink indexLink = new IndexLink();
	    indexLink.id = Utility.fromString(srcid);
	    indexLink.srctype = srctype;
	    indexLink.pos = srcpos;
	    indexLink.targetid = tag.attributes.get("id");
	    indexLink.targettype = tag.attributes.get("type");
	    indexLink.targetpos = tag.begin;
	    processor.process(indexLink);
	    ++linkCount;
	}
	if (linkCounter != null) linkCounter.incrementBy(linkCount);
    }

    public void close() throws IOException {
	processor.close();
    }
}
