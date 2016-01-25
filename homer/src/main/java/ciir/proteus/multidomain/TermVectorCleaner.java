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
 * Created by david on 1/22/16.
 */
public class TermVectorCleaner {

    //for each vector, delete it if it is a duplicate, delete any terms with freq < 1, re-encode the ids for the remainder.

    //read through a vector file
    //format: <book> <

    public static void generateCountFile(String termVectorFile, String oldTermDictionaryFile, String newTermDictionaryFile, String suffix, String pagesToDeleteFile) throws Exception {

        ArrayList<Path> thesePaths = new ArrayList<Path>();

        //create a dictionary from terms to new ids
        HashMap<String, String> newTermDictionary = new HashMap<String, String>();
        BufferedReader br = Files.newBufferedReader(Paths.get(newTermDictionaryFile), Charset.forName("UTF-8"));
        String line = null;
        line = br.readLine();
        int counter = 1;
        while (line != null) {
            String[] elements = line.trim().split(" ");
            if(elements.length == 2) newTermDictionary.put(elements[1],elements[0]);
            else System.out.println(counter);
            line = br.readLine();
            counter++;
        }
        br.close();

        //create a dictionary from old ids to terms
        HashMap<Integer, String> oldTermDictionary = new HashMap<Integer, String>();
        br = Files.newBufferedReader(Paths.get(oldTermDictionaryFile), Charset.forName("UTF-8"));
        line = br.readLine();
        counter = 1;
        while (line != null) {
            String[] elements = line.trim().split(" ");
            if(elements.length == 2) oldTermDictionary.put(Integer.valueOf(elements[0]),elements[1]);
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

        //go through the vector list
        //format: <docid> <termid> <score> <termid> <score>
        ArrayList<String> output = new ArrayList<String>();
        br = Files.newBufferedReader(Paths.get(termVectorFile), Charset.forName("UTF-8"));
        line = br.readLine();
        while (line != null) {
            String[] elements = line.trim().split(" ");
            String docid = elements[0];
            //delete vectors if the docid is in the delete list
            if(!pagesToDelete.contains(docid)) {
                StringBuilder newline = new StringBuilder();
                newline.append(docid);
                int i = 1;
                while(i < elements.length) {
                    String term = oldTermDictionary.get(Integer.parseInt(elements[i]));
                    i++; //pointing at score
                    if (newTermDictionary.containsKey(term)) {
                        String newID = newTermDictionary.get(term);
                        String score = elements[i];
                        newline.append(" " + newID + " " + score);
                    }
                    i++; //pointing at term
                }
            output.add(newline.toString());
            }
            line = br.readLine();
        }
        br.close();

        //write out new file
        String outputName = "new-tf-idf/"+suffix+".txt";
        BufferedWriter bw = new BufferedWriter(new FileWriter(outputName));
        for(String l: output){
            bw.write(l+"\n");
        }
        bw.close();

    }

    public static void main(String[] args) throws Exception {
        String termVectorFile = args[0];
        String oldTermDictionaryFile = args[1];
        String newTermDictionaryFile = args[2];
        String suffix = args[3];
        String pagesToDeleteFile = args[4];
        generateCountFile(termVectorFile, oldTermDictionaryFile,newTermDictionaryFile,suffix,pagesToDeleteFile);
    }

}
