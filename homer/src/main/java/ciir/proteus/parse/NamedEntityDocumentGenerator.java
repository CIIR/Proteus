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
import java.util.*;

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

    public void generateDocsOld(Document doc) throws IOException {
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
        HashMap<String, StringBuilder> entities = new HashMap<String, StringBuilder>();
        for(Tag tag: doc.tags){
            //FileWriter fWriter = new FileWriter(new File(writeOutLocation + "test.txt"));
            //BufferedWriter bWriter = new BufferedWriter(fWriter);
            StringBuilder name = new StringBuilder();
            for (int i = tag.begin; i < tag.end; i++) {
                name.append(doc.terms.get(i));
            }
            name.append("."+doc.name);
            if(!entities.containsKey(name.toString())) entities.put(name.toString(), new StringBuilder());
            StringBuilder sb = new StringBuilder();
            int start = 0;
            if(tag.begin-offset > start) start = tag.begin-offset;
            int end = doc.terms.size();
            if(tag.begin-offset < end) start = tag.end + offset;

            for(int i = start; i < end; i++){
                sb.append(doc.terms.get(i));
                sb.append(" ");
            }
            entities.get(name.toString()).append(sb);

        }
        for(String name: entities.keySet()){
            StringBuilder sb = new StringBuilder();
            sb.append("<DOC>\n<DOCNO>"
                    + name
                    + "</DOCNO>\n<DOCHDR>\n"
                    + name
                    + "\n</DOCHDR>\n"
                    + "<TEXT>\n");
            sb.append(entities.get(name));
            sb.append("\n</TEXT>\n");
            sb.append("</DOC>");
            String outputName = writeOutLocation + name + ".trectext";
            BufferedWriter bw = new BufferedWriter(new FileWriter(outputName));
            bw.write(sb.toString());
            bw.close();
        }
    }

    public void generateDocs(Document doc) throws IOException {
        System.out.println("Generating Entity Documents");
        Path currentRelativePath = Paths.get("");
        String s = currentRelativePath.toAbsolutePath().toString();
        System.out.println("Current relative path is: " + s);
        TagTokenizer tok = new TagTokenizer();
        tok.addField("person");
        tok.tokenize(doc);
        HashSet<String> names = new HashSet<String>();
        for(Tag tag: doc.tags){
            StringBuilder nb = new StringBuilder();
            for (int i = tag.begin; i < tag.end; i++) {
                nb.append(doc.terms.get(i) + "_");
            }
            nb.deleteCharAt(nb.lastIndexOf("_"));
            nb.append("."+doc.name);
            String name = nb.toString();
            boolean isNew = false;
            StringBuilder sb = new StringBuilder();
            if(!names.contains(name)){
                names.add(name);
                isNew = true;
            }
            if(isNew) sb.append("<DOC>\n<DOCNO>"
                    + name
                    + "</DOCNO>\n<DOCHDR>\n"
                    + name
                    + "\n</DOCHDR>\n"
                    + "<TEXT>\n");
            int start = 0;
            if(tag.begin-offset < start) start = tag.begin-offset;
            int end = doc.terms.size();
            if(tag.begin+offset > end) start = tag.end + offset;

            for(int i = start; i < end; i++){
                sb.append(doc.terms.get(i));
                sb.append(" ");
            }
            String outputName = writeOutLocation + name + ".trectext";
            BufferedWriter bw = new BufferedWriter(new FileWriter(outputName, !isNew));
            bw.write(sb.toString());
            bw.close();

        }
        //add the closing lines to each document
        for(String name: names){
            String outputName = writeOutLocation + name + ".trectext";
            BufferedWriter bw = new BufferedWriter(new FileWriter(outputName, true));
            bw.write("\n</TEXT>\n</DOC>");
            bw.close();
        }
    }

    public void testDoc(String s) throws IOException {
        FileWriter fWriter = new FileWriter(new File(writeOutLocation + "docText.txt"));
        BufferedWriter bWriter = new BufferedWriter(fWriter);
        bWriter.write(s);
        bWriter.flush();
        bWriter.close();
    }
}
