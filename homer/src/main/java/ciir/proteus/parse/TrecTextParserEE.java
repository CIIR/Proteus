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
 * doNER and getClassifier written byjfoley, michaelz
 *
 */
public class TrecTextParserEE extends TrecTextParser {

    public static final Logger log = Logger.getLogger(TrecTextParserEE.class.getName());

    String outputPath;
    private final AbstractSequenceClassifier nerClassifier;

    public TrecTextParserEE(DocumentSplit split, Parameters p) throws Exception {
        super(split, p);
        outputPath = p.get("outputPath","entity-records/");
        nerClassifier = getClassifier(p);
    }

    @Override
    public Document nextDocument() throws IOException {

        Document doc = super.nextDocument();
        if(!(doc == null)) {
            Document nerDoc = new Document();
            nerDoc.text = doNER(doc.text);
            nerDoc.metadata = doc.metadata;
            nerDoc.name = doc.name;
            System.out.println(nerDoc.text);
            NamedEntityRecorder ner = new NamedEntityRecorder(outputPath);
            ner.record(nerDoc);
            doc = nerDoc;
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

    protected AbstractSequenceClassifier getClassifier(Parameters p){
        String nullString = null;
        String serializedClassifier = p.get("ner-model", nullString);
        if (serializedClassifier != null && NamedEntityRecognizer.getClassifier() == null) {
            try {
                NamedEntityRecognizer.initClassifier(serializedClassifier);
            } catch (Exception e) {
                log.log(Level.WARNING, "Failed to load NER model: " + serializedClassifier + " ", e);
                throw new RuntimeException("Error loading NER model: " + serializedClassifier + ": " + e.toString());
            }
        }
        return NamedEntityRecognizer.getClassifier();
    }

}
