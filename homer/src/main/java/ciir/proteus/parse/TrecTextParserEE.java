package ciir.proteus.parse;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.parse.TrecTextParser;
import org.lemurproject.galago.core.types.DocumentSplit;
import org.lemurproject.galago.utility.Parameters;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by david on 12/2/16.
 * doNER written byjfoley, michaelz
 */
public class TrecTextParserEE extends TrecTextParser {

    public static final Logger log = Logger.getLogger(TrecTextParserEE.class.getName());

    String outputPath;
    private final AbstractSequenceClassifier nerClassifier;

    public TrecTextParserEE(DocumentSplit split, Parameters p) throws Exception {
        super(split, p);
        outputPath = p.get("outputPath","entity-records/");
        nerClassifier = NamedEntityRecognizer.getClassifier();
    }

    @Override
    public Document nextDocument() throws IOException {

        Document doc = super.nextDocument();
        if(!(doc == null)) {
            Document nerDoc = new Document();
            nerDoc.text = doNER(doc.text);
            nerDoc.metadata = doc.metadata;
            nerDoc.name = doc.name;
            NamedEntityRecorder ner = new NamedEntityRecorder(outputPath);
            ner.record(doc);
        }

        return doc;
    }

    protected String doNER(String text) {

        if (text.length() == 0) {
            return "";
        }

        if (nerClassifier == null) {
            return text;
        } else {
            // if there is an error, just use the regular text
            try {
                return nerClassifier.classifyWithInlineXML(text);
            } catch (Exception e) {
                log.log(Level.WARNING, "Error running NER on: " + text, e);
                return text;
            }
        }
    }

}
