package ciir.proteus.parse;

import org.lemurproject.galago.core.index.stats.NodeStatistics;
import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.parse.TagTokenizer;
import org.lemurproject.galago.core.retrieval.LocalRetrieval;
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.core.util.WordLists;
import org.lemurproject.galago.utility.Parameters;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.nio.charset.Charset;

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
    public void generateCountFile(String termFrequencyDirectory, String index,String termDictionary, String suffix) throws Exception {

        ArrayList<Path> thesePaths = new ArrayList<Path>();
        HashMap<String, Integer> termIdDict = new HashMap<String, Integer>();

        BufferedReader br = Files.newBufferedReader(Paths.get(termDictionary),Charset.forName("UTF-8"));
        String line = null;
        line = br.readLine();
        int counter = 1;
        while (line != null) {
            String[] elements = line.trim().split(" ");
            if(elements.length == 2) termIdDict.put(elements[1],Integer.valueOf(elements[0]));
            else System.out.println(counter);
            line = br.readLine();
            counter++;
        }
        br.close();

        LocalRetrieval ret = new LocalRetrieval(index);
        Set<String> stopWords = WordLists.getWordList("inquery");


        int docCount = (int)ret.getCollectionStatistics(new Node("lengths")).documentCount;

        Files.walk(Paths.get(termFrequencyDirectory)).forEach(filePath -> {
            if (Files.isRegularFile(filePath)) {
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
                    //if(!stopWords.contains(e[0])){
                        //try {
                        //    NodeStatistics textStats = ret.getNodeStatistics(new Node("text", e[0]));
                        //    double d = Integer.parseInt(e[1]) * Math.log10(docCount / textStats.nodeDocumentCount);
                        //    //System.out.println(d);
                        //    terms.add(String.valueOf(termIdDict.get(e[0])) + " " + String.valueOf(d));
                        //}
                        //catch(java.lang.IllegalArgumentException iae){
                        //    System.out.println("java.lang.IllegalArgumentException for term: " + e[0]);
                        //}
                        try {
                            Node n = new Node("text", e[0]);
                            n.getNodeParameters().set("part", "postings");
                            n.getNodeParameters().set("queryType", "count");
                            n = ret.transformQuery(n, Parameters.create());
                            NodeStatistics textStats = ret.getNodeStatistics(n);
                            if(textStats.nodeDocumentCount > 0){
                                double d = Integer.parseInt(e[1]) * Math.log10(docCount / textStats.nodeDocumentCount);
                                //System.out.println(d);
                                terms.add(String.valueOf(termIdDict.get(e[0])) + " " + String.valueOf(d));
                            }
                            else System.out.println("WARNING: " + e[0] + " does not occur in any documents!");
                        } catch(java.lang.IllegalArgumentException iae){
                            System.out.println("java.lang.IllegalArgumentException for term: " + e[0]);
                        }
                    //}
                }
            }
            String docName = filePath.getFileName().toString().replace(".txt","");
            String outputName = "tf-idf/"+suffix+"/"+docName+".txt";
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
        String index = args[1];
        String termDictionary = args[2];
        String suffix = args[3];
        TermCounter tc = new TermCounter("output/");
        tc.generateCountFile(termFrequencyDirectory, index,termDictionary,suffix);
    }
}
