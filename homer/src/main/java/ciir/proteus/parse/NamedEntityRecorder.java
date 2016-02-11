package ciir.proteus.parse;

import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.parse.Tag;
import org.lemurproject.galago.core.parse.TagTokenizer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author David Wemhoener
 */

public class NamedEntityRecorder {

    static final List<String> entityTypes = Arrays.asList("location","date");
    String entityType;
    String writeOutLocation;
    int offset;

    public NamedEntityRecorder(String possibleWriteOutLocation) {

        this.writeOutLocation = possibleWriteOutLocation;
        offset = 10;

    }

    public void record(Document doc) throws IOException {
        //System.out.println("Recording Entities");
        Path currentRelativePath = Paths.get("");
        String s = currentRelativePath.toAbsolutePath().toString();
        //System.out.println("Current relative path is: " + s);

        Path targetPath = currentRelativePath.resolve(writeOutLocation);

        if(Files.notExists(targetPath)){
            Files.createDirectory(targetPath);
            Files.createDirectory(targetPath.resolve("location"));
            Files.createDirectory(targetPath.resolve("date"));
        }

        boolean isNew = true;
        //for(String entity: entityTypes) {
        //    System.out.println(entity);
        //}
        for(String entity: entityTypes){
            StringBuilder sb = new StringBuilder();
            TagTokenizer tok = new TagTokenizer();
            tok.addField(entity);
            tok.tokenize(doc);
            HashSet<String> names = new HashSet<String>();
            HashMap<String, Integer> nameCounts = new HashMap<String, Integer>();
            int totalCount = 0;
            int uniqueCount = 0;
            for(Tag tag: doc.tags) {
                StringBuilder nb = new StringBuilder();
                for (int i = tag.begin; i < tag.end; i++) {
                    nb.append(doc.terms.get(i) + " ");
                }
                if(!(nb.lastIndexOf(" ")==-1)){
                    nb.deleteCharAt(nb.lastIndexOf(" "));
                }
                String name = nb.toString();

                Pattern p = Pattern.compile("(\\d{4}|\\d{3})");
                Matcher m = p.matcher(name);

                List<String> years = new ArrayList<String>();
                while (m.find()) {
                    years.add(m.group());
                }
                if(years.size() > 0) {
                    for(String year: years)
                        sb.append(year);
                        sb.append(",");
                        sb.append(doc.name);
                        sb.append("\n");
                }
                else if(entity.equals("location")) {
                    sb.append(name);
                    sb.append(",");
                    sb.append(doc.name);
                    sb.append("\n");
                }
            }

            String docName = doc.name.split("_")[0];
            String outputName = writeOutLocation+entity+"/"+docName+".txt";
            isNew = Files.notExists(Paths.get(outputName));
            //if the name is not new, then append rather than start a new file
            BufferedWriter bw = new BufferedWriter(new FileWriter(outputName, !isNew));
            bw.write(sb.toString());
            bw.close();

        }
    }
}
