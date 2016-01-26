package ciir.proteus.multidomain;

import org.lemurproject.galago.core.index.stats.NodeStatistics;
import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.parse.TagTokenizer;
import org.lemurproject.galago.core.retrieval.LocalRetrieval;
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.utility.Parameters;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
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

public class TermVectorGenerator {

    String writeOutLocation;
    int offset;

    public TermVectorGenerator(String possibleWriteOutLocation) {

        this.writeOutLocation = possibleWriteOutLocation;
        offset = 10;

    }

    public static void generateCountFile(String termFrequencyDirectory, String index,String termDictionaryFile, String suffix, String pagesToDeleteFile) throws Exception {

        ArrayList<Path> thesePaths = new ArrayList<Path>();
        HashMap<String, Integer> termIdDict = new HashMap<String, Integer>();

        BufferedReader br = Files.newBufferedReader(Paths.get(termDictionaryFile),Charset.forName("UTF-8"));
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

        //create a list of pages to delete
        HashSet<String> pagesToDelete = new HashSet<String>();
        br = Files.newBufferedReader(Paths.get(pagesToDeleteFile), Charset.forName("UTF-8"));
        line = br.readLine();
        while (line != null) {
            pagesToDelete.add(line.trim());
            line = br.readLine();
        }
        br.close();

        LocalRetrieval ret = new LocalRetrieval(index);
        //Set<String> stopWords = WordLists.getWordList("inquery");

        int docCount = (int)ret.getCollectionStatistics(new Node("lengths")).documentCount;

        Files.walk(Paths.get(termFrequencyDirectory)).forEach(filePath -> {
            if (Files.isRegularFile(filePath)) {
                thesePaths.add(filePath);
            }
        });
        for(Path filePath: thesePaths){
            String docid = filePath.getFileName().toString().replace(".txt","");
            //ignore pages in delete list
            if(!pagesToDelete.contains(docid)) {
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
                for (int i = 1; i < elements.length; i++) {
                    String element = elements[i];
                    //System.out.println(element);
                    if (!element.isEmpty()) {
                        String[] e = element.split(" ");
                        //only process terms which are in the id dictionary
                        if (termIdDict.containsKey(e[0])) {
                            try {
                                Node n = new Node("text", e[0]);
                                n.getNodeParameters().set("part", "postings");
                                n.getNodeParameters().set("queryType", "count");
                                n = ret.transformQuery(n, Parameters.create());
                                NodeStatistics textStats = ret.getNodeStatistics(n);
                                if (textStats.nodeDocumentCount > 0) {
                                    double d = Integer.parseInt(e[1]) * Math.log10(docCount / textStats.nodeDocumentCount);
                                    terms.add(String.valueOf(termIdDict.get(e[0])) + " " + String.valueOf(d));
                                } else {
                                    System.out.println("WARNING: " + e[0] + " does not occur in any documents!");
                                    double d = Integer.parseInt(e[1]) * Math.log10(docCount / 1);
                                    terms.add(String.valueOf(termIdDict.get(e[0])) + " " + String.valueOf(d));
                                }
                            } catch (IllegalArgumentException iae) {
                                System.out.println("java.lang.IllegalArgumentException for term: " + e[0]);
                            }
                        }
                    }
                }


            //String outputName = "tf-idf/"+suffix+"/"+docName+".txt";
            String outputName = "new-tf-idf/"+suffix+".txt";
            //if the name is not new, then append rather than start a new file
            BufferedWriter bw = new BufferedWriter(new FileWriter(outputName, true));
            bw.write(docid);
            for(String term: terms){
                bw.write(" " + term);
            }
            bw.write("\n");
            bw.close();
            }
        }

    }

    public static void main(String[] args) throws Exception {
        String termFrequencyDirectory = args[0];
        String index = args[1];
        String termDictionaryFile = args[2];
        String suffix = args[3];
        String pagesToDeleteFile = args[4];
        //TermVectorGenerator tc = new TermVectorGenerator("output/");
        generateCountFile(termFrequencyDirectory, index,termDictionaryFile,suffix,pagesToDeleteFile);
    }
}
