package ciir.proteus.parse;

import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.types.DocumentSplit;
import org.lemurproject.galago.utility.Parameters;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by david on 1/14/17.
 */
public class TrecTextParserEEAutoDate extends TrecTextParserEE {
    public TrecTextParserEEAutoDate(DocumentSplit split, Parameters p) throws Exception {
        super(split, p);
    }

    @Override
    public Document nextDocument() throws IOException {

        Document doc = nextDocumentBasic();
        if(!(doc == null)) {
            Document nerDoc = new Document();
            nerDoc.text = doNER(doc.text);
            nerDoc.metadata = doc.metadata;
            nerDoc.name = doc.name;
            //System.out.println(nerDoc.text);
            nerDoc = addDate(nerDoc);
            NamedEntityRecorder ner = new NamedEntityRecorder(outputPath);
            ner.record(nerDoc);
            doc = nerDoc;
        }

        return doc;
    }

    public Document addDate(Document doc){
        //three cases:
        // Foreign Broadcast Information Service
        // Financial Times
        // LA Times

        //first check if it is a Foreign Broadcast Information Service
        //document. In this case the identifier will start with "FBIS3-"
        if(doc.name.startsWith("FBIS3-")){
            doc.text = "<DATE>1996</DATE>\n".concat(doc.text);
        }
        // <HEADLINE>
        // FT  29 JUN 92 / Paisley may meet Irish ministers
        // </HEADLINE>

        else if(doc.name.startsWith("FT")){
            BufferedReader reader = new BufferedReader(new StringReader(doc.text));
            String line=null;
            String year = "0000";
            boolean keepLooping = true;
            try {
                while( (line=reader.readLine()) != null && keepLooping){
                    if (line.trim().equals("<HEADLINE>")){
                        line=reader.readLine();
                        year = line.split("\\s+")[3];
                        year = year.replace("</DATE>","");
                        keepLooping = false;

                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            String yearBlock = String.format("<DATE>19%s</DATE>\n", year);
            doc.text = yearBlock.concat(doc.text);
        }

        else if(doc.name.startsWith("LA")){
            BufferedReader reader = new BufferedReader(new StringReader(doc.text));
            String line=null;
            String year = "0000";
            boolean keepLooping = true;
            try {
                while( (line=reader.readLine()) != null && keepLooping){
                    if (line.trim().equals("<P>")){
                        line = reader.readLine();
                        String pattern = "(\\d\\d\\d\\d)";
                        Pattern r = Pattern.compile(pattern);
                        Matcher m = r.matcher(line);
                        if (m.find()){
                            year = m.group(0);
                        }
                        else{
                            System.err.println("NO PUB DATE IN DOC: " + doc.name);
                        }
                        //try {
                        //    year = m.group(0);
                        //} catch(IllegalStateException ise){
                        //    System.err.println("ERROR IN DOC: " + doc.name);
                        //    throw new IllegalStateException(ise.getMessage());
                        //}
                        keepLooping = false;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(!(year.equals("0000"))) {
                String yearBlock = String.format("<DATE>%s</DATE>\n", year);
                doc.text = yearBlock.concat(doc.text);
            }
        }

        return doc;
    }

}
