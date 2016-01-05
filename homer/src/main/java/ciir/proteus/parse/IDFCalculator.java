package ciir.proteus.parse;

import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.parse.TagTokenizer;

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

public class IDFCalculator {

    String writeOutLocation;
    int offset;

    public IDFCalculator(String possibleWriteOutLocation) {

        this.writeOutLocation = possibleWriteOutLocation;
        offset = 10;

    }

    public void generateIdfFile(String docFrequencyDirectory) throws IOException {
        HashSet<String> globalTerms = new HashSet<String>();
        HashMap<String, Integer> termIdDict = new HashMap<String, Integer>();
        HashMap<String, Integer> globalTermCounts = new HashMap<String, Integer>();
        int loc = 0;
        int docCount = 0;
        ArrayList<Path> thesePaths = new ArrayList<Path>();
        ArrayList<String> idfs = new ArrayList<String>();

        Files.walk(Paths.get(docFrequencyDirectory)).forEach(filePath -> {
            if (Files.isRegularFile(filePath)) {
                //System.out.println("File Name: " + filePath.getFileName());
                thesePaths.add(filePath);
            }
        });
        //File directory = new File(termDirectory);
        //File[] directoryList = directory.list();
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
        for(String term: globalTerms){
            if(!isStopWord(term)) {
                double d = Math.log10(docCount / globalTermCounts.get(term));
                //System.out.println(d);
                idfs.add(String.valueOf(termIdDict.get(term)) + " " + String.valueOf(d));
            }
        }
        BufferedWriter bw = new BufferedWriter(new FileWriter("termDictionary"));
        for(String term: globalTerms){
            bw.write(String.valueOf(termIdDict.get(term)) + " " + term + "\n");
        }
        bw.close();
        bw = new BufferedWriter(new FileWriter("idf.list"));
        for(String line: idfs){
            bw.write(line + "\n");
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
        String docFrequencyDirectory = args[0];
        IDFCalculator ic = new IDFCalculator("idf.list");
        ic.generateIdfFile(docFrequencyDirectory);
    }
}
