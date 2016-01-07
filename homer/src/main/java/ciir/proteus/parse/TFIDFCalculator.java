package ciir.proteus.parse;

import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.parse.TagTokenizer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * @author David Wemhoener
 */

public class TFIDFCalculator {

    String writeOutLocation;
    int offset;

    public TFIDFCalculator(String possibleWriteOutLocation) {

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
    public void generateTfIdfFile(String termFrequencyDirectory,String idfScoresFile, String termDictionary) throws IOException {
        HashSet<String> globalTerms = new HashSet<String>();
        HashMap<String, Integer> termIdDict = new HashMap<String, Integer>();
        HashMap<String, Double> idfScores = new HashMap<String, Double>();
        int loc = 0;
        int docCount = 0;

        BufferedReader br = Files.newBufferedReader(Paths.get(idfScoresFile));
        String line = null;
        while ((line = br.readLine()) != null) {
            idfScores.put(line.split(" ")[0],Double.valueOf(line.split(" ")[1]));
        }
        br.close();

        br = Files.newBufferedReader(Paths.get(termDictionary));
        while ((line = br.readLine()) != null) {
            termIdDict.put(line.split(" ")[1],Integer.valueOf(line.split(" ")[0]));
        }
        br.close();

        ArrayList<Path> thesePaths = new ArrayList<Path>();
        Files.walk(Paths.get(termFrequencyDirectory)).forEach(filePath -> {
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
                    if(!isStopWord(e[0])){
                        double d = Integer.parseInt(e[1]) * idfScores.get(String.valueOf(termIdDict.get(e[0])));
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
        String termFrequencyDirectory = args[0];
        String idfScoresFile = args[1];
        String termDictionary = args[2];
        TFIDFCalculator tc = new TFIDFCalculator("output/");
        tc.generateTfIdfFile(termFrequencyDirectory,idfScoresFile,termDictionary);
    }
}
