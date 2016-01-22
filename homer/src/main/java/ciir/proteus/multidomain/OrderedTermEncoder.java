package ciir.proteus.multidomain;

import org.lemurproject.galago.core.index.IndexPartReader;
import org.lemurproject.galago.core.index.KeyIterator;
import org.lemurproject.galago.core.index.disk.DiskIndex;
import org.lemurproject.galago.core.index.stats.NodeStatistics;
import org.lemurproject.galago.core.retrieval.LocalRetrieval;
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.utility.Parameters;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author David Wemhoener
 * Create an encoding table for the set of terms in the index, where terms
 * that only occur once are not included and all other terms are given ids
 * ordered by increasing frequency.
 */

public class OrderedTermEncoder {

    String writeOutLocation;
    int offset;

    public OrderedTermEncoder(String possibleWriteOutLocation) {

        this.writeOutLocation = possibleWriteOutLocation;
        offset = 10;

    }

    public static void main(String[] args) throws Exception {
        String frequencies_list = args[0];

        //load the list of <term> <frequency>
        HashMap<String, Integer> termFrequencies = new HashMap<String, Integer>();
        BufferedReader br = Files.newBufferedReader(Paths.get(frequencies_list));
        String line = null;
        while ((line = br.readLine()) != null) {
            //only add those entries whose frequency is greater than 1
            if(Integer.valueOf(line.split(" ")[1]) == 1)
            termFrequencies.put(line.split(" ")[0],Integer.valueOf(line.split(" ")[1]));
        }
        br.close();

        //sort the list
        ArrayList<String> sortedTerms = new ArrayList<String>(termFrequencies.keySet());
        Collections.sort(sortedTerms, new Comparator<String>() {
            @Override
            public int compare(String x, String y) {
                return termFrequencies.get(y) - termFrequencies.get(x);
            }
        });

        //generate the new encoding from the ordered list of terms
        ArrayList<String> output = new ArrayList<String>();
        int counter = 0;

        for(String term: sortedTerms){
            output.add(Integer.toString(counter) + " " + term);
            counter++;
        }

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("termDictionary"),"UTF-8"));
        for(String entry: output){
            bw.write(entry + "\n");
        }
        bw.close();

    }
}
