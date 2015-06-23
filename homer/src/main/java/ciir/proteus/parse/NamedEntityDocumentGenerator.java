package ciir.proteus.parse;

import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.parse.Tag;
import org.lemurproject.galago.core.parse.TagTokenizer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Arrays;

/**
 * @author David Wemhoener
 */

public class NamedEntityDocumentGenerator{

//Need to seperate out people, places, etc. (perhaps in the object?)
//Also need to store the write out location (also in the object?)

    static final List<String> validEntityTypes = Arrays.asList("person");

    String entityType;
    String writeOutLocation;
    int offset;

    public NamedEntityDocumentGenerator(String possibleEntityType, String possibleWriteOutLocation){

        //throw an exception is the possibleEntityType is unrecognized
        if (!validEntityTypes.contains(possibleEntityType)){
            throw new IllegalArgumentException("possibleEntityType " + possibleEntityType + " is not a valid entity type");
        }

        this.entityType = possibleEntityType;
        this.writeOutLocation = possibleWriteOutLocation;
        offset = 10;

    }

    public void generateDocs(Document doc) throws IOException {
        System.out.println("Generating Entity Documents");
        Path currentRelativePath = Paths.get("");
        String s = currentRelativePath.toAbsolutePath().toString();
        System.out.println("Current relative path is: " + s);
        TagTokenizer tok = new TagTokenizer();
        tok.addField("person");
        tok.tokenize(doc);
        //bWriter.write("Test Output:");
        //bWriter.write(doc.name + "\n");
        //bWriter.write("Tags:");
        //bWriter.write(Integer.toString(doc.tags.size()) + "\n");
        //for(Tag tag: doc.tags){
            //StringBuilder name = new StringBuilder();
            //for (int i = tag.begin; i < tag.end; i++) {
                //if (name.length() > 0)
                    //name.append(" ");
                //name.append(doc.terms.get(i));
            //}
            //bWriter.write(name.toString() + "\n");
        //}
        for(Tag tag: doc.tags){
            FileWriter fWriter = new FileWriter(new File(writeOutLocation + "test.txt"));
            BufferedWriter bWriter = new BufferedWriter(fWriter);
            StringBuilder name = new StringBuilder();
            for (int i = tag.begin; i < tag.end; i++) {
                name.append(doc.terms.get(i));
            }
            name.append("."+doc.name);
            StringBuilder sb = new StringBuilder();
            sb.append("<DOC>\n<DOCNO>"
                    + name
                    + "</DOCNO>\n<DOCHDR>\n"
                    + name
                    + "\n</DOCHDR>\n"
                    + "<TEXT>\n");
            for(int i = tag.begin-offset; i < tag.end + offset; i++){
                sb.append(doc.terms.get(i));
                sb.append(" ");
            }
            sb.append("\n</TEXT>\n");
            sb.append("</DOC>");
            String outputName = writeOutLocation + name + ".trectext";
            BufferedWriter bw = new BufferedWriter(new FileWriter(outputName));
            bw.write(sb.toString());
            bw.close();
        }
    }

    public void writeDoc(){

    }

    public void testDoc(String s) throws IOException {
        FileWriter fWriter = new FileWriter(new File(writeOutLocation + "docText.txt"));
        BufferedWriter bWriter = new BufferedWriter(fWriter);
        bWriter.write(s);
        bWriter.flush();
        bWriter.close();
    }
}
