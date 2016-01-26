package ciir.proteus.multidomain;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by david on 1/22/16.
 */
public class OldToNewIdGenerator {

    //for each vector, delete it if it is a duplicate, delete any terms with freq < 1, re-encode the ids for the remainder.

    //read through a vector file
    //format: <book> <

    public static void generateCountFile(String oldTermDictionaryFile, String newTermDictionaryFile) throws Exception {

        //create a dictionary from terms to new ids
        HashMap<String, String> newTermDictionary = new HashMap<String, String>();
        BufferedReader br = Files.newBufferedReader(Paths.get(newTermDictionaryFile), Charset.forName("UTF-8"));
        String line = br.readLine();
        int counter = 1;
        while (line != null) {
            String[] elements = line.trim().split(" ");
            if(elements.length == 2) newTermDictionary.put(elements[1],elements[0]);
            else System.out.println(counter);
            line = br.readLine();
            counter++;
        }
        br.close();

        //create a dictionary from old ids to new ids
        ArrayList<String> output = new ArrayList<>();
        br = Files.newBufferedReader(Paths.get(oldTermDictionaryFile), Charset.forName("UTF-8"));
        line = br.readLine();
        counter = 1;
        while (line != null) {
            try {
                System.out.println(line);
                String[] elements = line.trim().split(" ");
                if(newTermDictionary.containsKey(elements[1])) output.add(elements[0] + " " + newTermDictionary.get(elements[1]) + "\n");
                line = br.readLine();
                counter++;
            }
            catch(Exception e){
                System.err.println("Exception at dictionary file line " + counter);
                System.err.println(line);
                throw e;
            }
        }
        br.close();

        //write out new file
        String outputName = "old-new-id-dictionary";
        BufferedWriter bw = new BufferedWriter(new FileWriter(outputName));
        for(String l: output){
            bw.write(l);
        }
        bw.close();

    }

    public static void main(String[] args) throws Exception {
        String oldTermDictionaryFile = args[0];
        String newTermDictionaryFile = args[1];
        generateCountFile(oldTermDictionaryFile,newTermDictionaryFile);
    }

}
