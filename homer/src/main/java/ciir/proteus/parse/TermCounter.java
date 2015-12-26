package ciir.proteus.parse;

import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.parse.TagTokenizer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author David Wemhoener
 */

public class TermCounter {

    String writeOutLocation;
    int offset;

    public TermCounter(String possibleWriteOutLocation) {

        this.writeOutLocation = possibleWriteOutLocation;
        offset = 10;

    }

    public void count(Document doc) throws IOException {
        //System.out.println("Recording Entities");
        Path currentRelativePath = Paths.get("");
        String s = currentRelativePath.toAbsolutePath().toString();
        //System.out.println("Current relative path is: " + s);

        Path targetPath = currentRelativePath.resolve(writeOutLocation);

        if(Files.notExists(targetPath)){
            Files.createDirectory(targetPath);
        }

        boolean isNew = true;
        //for(String entity: entityTypes) {
        //    System.out.println(entity);
        //}
        TagTokenizer tok = new TagTokenizer();
        tok.tokenize(doc);
        HashSet<String> terms = new HashSet<String>();
        HashMap<String, Integer> termCounts = new HashMap<String, Integer>();
        int totalCount = 0;
        int uniqueCount = 0;
        for(String term: doc.terms) {
            if(!terms.contains(term)){
                terms.add(term);
                termCounts.put(term,1);
            }
            else termCounts.put(term,termCounts.get(term)+1);

        }

        //String docName = doc.name.split("_")[0];
        String docName = doc.name;
        String outputName = writeOutLocation+docName+".txt";
        //if the name is not new, then append rather than start a new file
        BufferedWriter bw = new BufferedWriter(new FileWriter(outputName));
        bw.write(doc.name);
        for(String term: terms){
            bw.write("," + term + " " + termCounts.get(term));
        }
        bw.close();

        outputName = "terms/"+docName+".txt";
        bw = new BufferedWriter(new FileWriter(outputName));
        for(String term: terms){
            bw.write(term+"\n");
        }
        bw.close();
    }
    public void generateCountFile(String termDirectory, String docFrequencyDirectory) throws IOException {
        HashSet<String> globalTerms = new HashSet<String>();
        HashMap<String, Integer> termIdDict = new HashMap<String, Integer>();
        HashMap<String, Integer> globalTermCounts = new HashMap<String, Integer>();
        int loc = 0;
        int docCount = 0;
        ArrayList<Path> thesePaths = new ArrayList<Path>();

        //Files.walk(Paths.get(termDirectory)).forEach(filePath -> {
        //    if (Files.isRegularFile(filePath)) {
        //        //System.out.println("File Name: " + filePath.getFileName());
        //        thesePaths.add(filePath);
        //    }
        //});
        File directory = new File(termDirectory);
        File[] directoryList = directory.list();
        for(Path filePath: thesePaths){
                List<String> lines = null;
                try {
                    lines = Files.readAllLines(filePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                for(String line: lines){
                    String term = line.trim();
                    if(!isStopWord(term)){
                        if(!globalTerms.contains(term)){
                            globalTerms.add(term);
                            globalTermCounts.put(term,1);
                            termIdDict.put(term,loc);
                            loc = loc + 1;
                        }
                        else globalTermCounts.put(term,globalTermCounts.get(term)+1);
                    }
                }
                docCount = docCount + 1;
        }
        thesePaths.clear();
        Files.walk(Paths.get(docFrequencyDirectory)).forEach(filePath -> {
            if (Files.isRegularFile(filePath)) {
                //System.out.println("File Name: " + filePath.getFileName());
                thesePaths.add(filePath);
            }
        });
        for(Path filePath: thesePaths){
            //System.out.println(filePath.toString());
            ArrayList<String> terms = new ArrayList<String>();
            List<String> lines = null;
            try {
                lines = Files.readAllLines(filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String[] elements = lines.get(0).trim().split(",");
            //System.out.println(lines.get(0));
            for(int i = 1; i < elements.length; i++){
                String element = elements[i];
                //System.out.println(element);
                if(!element.isEmpty()) {
                    String[] e = element.split(" ");
                    //System.out.println(e[0]);
                    //System.out.println(e[1]);
                    //System.out.println(Integer.parseInt(e[1]));
                    //System.out.println(globalTermCounts.get(e[0]));
                    //System.out.println(Math.log10(docCount / globalTermCounts.get(e[0])));
                    if(!isStopWord(e[0])){
                        double d = Integer.parseInt(e[1]) * Math.log10(docCount / globalTermCounts.get(e[0]));
                        //System.out.println(d);
                        terms.add(String.valueOf(termIdDict.get(e[0])) + " " + String.valueOf(d));
                    }
                }
            }
            String docName = filePath.getFileName().toString().replace(".txt","");
            String outputName = "tf-idf/"+docName+".txt";
            //if the name is not new, then append rather than start a new file
            BufferedWriter bw = new BufferedWriter(new FileWriter(outputName));
            bw.write(docName);
            for(String term: terms){
                bw.write(" " + term);
            }
            bw.write("\n");
            bw.close();
        }
            BufferedWriter bw = new BufferedWriter(new FileWriter("termDictionary"));
            for(String term: globalTerms){
                bw.write(String.valueOf(termIdDict.get(term)) + " " + term + "\n");
            }
            bw.close();

    }
    public boolean isStopWord(String term){
        HashSet<String> stopWords = new HashSet<String>();
        stopWords.add("the");
        stopWords.add("a");
        stopWords.add("is");
        stopWords.add("not");
        stopWords.add("an");
        stopWords.add("at");
        stopWords.add("or");
        stopWords.add("and");
        if(stopWords.contains(term.toLowerCase())) return true;
        else return false;    
    }
    public static void main(String[] args) throws Exception {
        String termDirectory = args[0];
        String docFrequencyDirectory = args[1];
        TermCounter tc = new TermCounter("output/");
        tc.generateCountFile(termDirectory, docFrequencyDirectory);
    }
}
