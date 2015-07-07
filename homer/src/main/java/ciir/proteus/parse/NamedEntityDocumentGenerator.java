package ciir.proteus.parse;

import ciir.proteus.retrieval.Entity;
import org.lemurproject.galago.core.index.corpus.CorpusReader;
import org.lemurproject.galago.core.index.corpus.DocumentReader;
import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.parse.Tag;
import org.lemurproject.galago.core.parse.TagTokenizer;
import org.lemurproject.galago.core.retrieval.LocalRetrieval;
import org.lemurproject.galago.utility.Parameters;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

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
            int start = tag.begin-offset;
            if(start < 0) start = 0;
            int end = tag.end + offset;
            if(end > doc.terms.size()) end = doc.terms.size();

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
            int start = tag.begin-offset;
            if(start < 0) start = 0;
            int end = tag.end + offset;
            if(end > doc.terms.size()) end = doc.terms.size();

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

    public void generateRawDocs(Document doc) throws IOException {
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
                System.out.println(name);
                names.add(name);
                isNew = true;
            }
            //if(isNew) sb.append("<DOC>\n<DOCNO>"
            //        + name
            //        + "</DOCNO>\n<DOCHDR>\n"
             //       + name
            //        + "\n</DOCHDR>\n"
            //        + "<TEXT>\n");
            int start = tag.begin-offset;
            if(start < 0) start = 0;
            int end = tag.end + offset;
            if(end > doc.terms.size()) end = doc.terms.size();

            for(int i = start; i < end; i++){
                sb.append(doc.terms.get(i));
                sb.append(" ");
            }
            String outputName = writeOutLocation + name + ".trectext";
            //if the name is not new, then append rather than start a new file
            BufferedWriter bw = new BufferedWriter(new FileWriter(outputName, !isNew));
            bw.write(sb.toString());
            bw.close();

        }
        //add the closing lines to each document
        //for(String name: names){
        //    String outputName = writeOutLocation + name + ".trectext";
        //    BufferedWriter bw = new BufferedWriter(new FileWriter(outputName, true));
        //    bw.write("\n</TEXT>\n</DOC>");
        //   bw.close();
        //}
    }

    //don't use this!
    //public void run(Parameters p, PrintStream output) throws Exception {
    //    CorpusReader reader = new CorpusReader(p.getString("path"));
    //    if (reader.getManifest().get("emptyIndexFile", false)) {
    //        output.println("Empty Corpus.");
    //        return;
    //    }

     //   DocumentReader.DocumentIterator iterator = reader.getIterator();
     //   Document.DocumentComponents dc = new Document.DocumentComponents(p);
//
     //   while (!iterator.isDone()) {
     //       output.println("#IDENTIFIER: " + iterator.getKeyString());
     //       Document document = iterator.getDocument(dc);
     //       output.println("#METADATA");
     //       for (Map.Entry<String, String> entry : document.metadata.entrySet()) {
     //           output.println(entry.getKey() + "," + entry.getValue());
     //       }
      //      output.println("#TEXT");
      //      output.println(document.text);
      //      iterator.nextKey();
      //  }
     //   reader.close();
    //}

    public void generateEntityDocs(String inputDirectory) throws IOException {
        //this is the maximum distance the bestMatch can have from a new name
        //in order for the two entities to be merged
        int maxAllowedDistance = 0;
        HashSet<Entity> entities = new HashSet<Entity>();
        Files.walk(Paths.get(inputDirectory)).forEach(filePath -> {
            if (Files.isRegularFile(filePath)) {
                System.out.println("File Name: " + filePath.getFileName());
                String[] segments = filePath.getFileName().toString().split("\\.");
                String rawName = segments[0].replace('_',' ');
                String bookIndentifier = segments[1];
                List<String> lines = null;
                try {
                    lines = Files.readAllLines(filePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String languageModel = lines.stream().collect(Collectors.joining("\n"));
                int bestScore = Integer.MAX_VALUE;
                Entity bestMatch = null;
                System.out.println("New Name: " + rawName);
                for(Entity e: entities){
                    System.out.println(" Comparing to: " + e.getName());
                    int newScore = e.compareAliasesBySegment(rawName);
                    if(newScore < bestScore){
                        System.out.println("  Distance of " + newScore + " Acknowledged");
                        bestScore = newScore;
                        bestMatch = e;
                    }
                }
                //if the best match is sufficiently close to the new entity
                //merge the new entity into the best match
                if(bestScore <= maxAllowedDistance && bestMatch != null){
                    System.out.println(" Appending to " + bestMatch.getName());
                    bestMatch.addAlias(rawName);
                    bestMatch.addLocation(bookIndentifier);
                    bestMatch.extendLanguageModel(languageModel);
                }
                //otherwise create a new entity and add it to the set
                else{
                    System.out.println(" Creating New Entity");
                    Entity e = new Entity(rawName);
                    e.addAlias(rawName);
                    e.addLocation(bookIndentifier);
                    e.extendLanguageModel(languageModel);
                    entities.add(e);
                }
            }
        });
        //now write out each entity to file
        for(Entity e: entities){
            String outputName = writeOutLocation + e.getName().replace(' ','_') + ".trectext";
            BufferedWriter bw = new BufferedWriter(new FileWriter(outputName));
            bw.write("<DOC>\n<DOCNO>"
                    + e.getName()
                    + "</DOCNO>\n<DOCHDR>\n"
                    + e.getName()
                    + "\n</DOCHDR>\n"
                    + "<TEXT>\n");
            for (String alias: e.getAliases()) bw.write("<alias>"+alias+"</alias>\n");
            for (String location: e.getLocations()) bw.write("<location>"+location+"</location>\n");
            bw.write(e.getLanguageModel());
            bw.write("\n</TEXT>\n</DOC>");
            bw.close();
        }
    }

    //public void testDoc(String s) throws IOException {
        //FileWriter fWriter = new FileWriter(new File(writeOutLocation + "docText.txt"));
        //BufferedWriter bWriter = new BufferedWriter(fWriter);
        //bWriter.write(s);
        //bWriter.flush();
        //bWriter.close();
    //}

    public static void main(String[] args) throws Exception {
        String inputDirectory = args[0];
        NamedEntityDocumentGenerator nedg = new NamedEntityDocumentGenerator("person", "entity-docs/");
        nedg.generateEntityDocs(inputDirectory);
    }
}
