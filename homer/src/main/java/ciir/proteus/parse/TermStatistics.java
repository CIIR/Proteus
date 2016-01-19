package ciir.proteus.parse;

import org.lemurproject.galago.core.index.IndexPartReader;
import org.lemurproject.galago.core.index.KeyIterator;
import org.lemurproject.galago.core.index.disk.DiskIndex;
import org.lemurproject.galago.core.index.stats.NodeStatistics;
import org.lemurproject.galago.core.retrieval.LocalRetrieval;
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.utility.Parameters;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

/**
 * @author David Wemhoener
 */

public class TermStatistics {

    String writeOutLocation;
    int offset;

    public TermStatistics(String possibleWriteOutLocation) {

        this.writeOutLocation = possibleWriteOutLocation;
        offset = 10;

    }

    public static void main(String[] args) throws Exception {
        String index = args[0];

        ArrayList<String> output = new ArrayList<String>();
        int counter = 0;
        int[] frequencies = new int[3];
        frequencies[0] = 0;
        frequencies[1] = 0;
        frequencies[2] = 0;

        IndexPartReader reader = DiskIndex.openIndexPart(index);
        LocalRetrieval ret = new LocalRetrieval(index);

        KeyIterator iterator = reader.getIterator();
        while (!iterator.isDone()) {
            String s = iterator.getKeyString();
            try {
                Node n = new Node("text", s);
                n.getNodeParameters().set("part", "postings");
                n.getNodeParameters().set("queryType", "count");
                n = ret.transformQuery(n, Parameters.create());
                NodeStatistics textStats = ret.getNodeStatistics(n);
                if(textStats.nodeDocumentCount > 0){
                    output.add(s + " " + String.valueOf(textStats.nodeDocumentCount));
                    frequencies[(int)textStats.nodeDocumentCount-1]++;
                }
                else System.out.println("WARNING: " + s + " does not occur in any documents!");
            } catch(java.lang.IllegalArgumentException iae){
                System.out.println("java.lang.IllegalArgumentException for term: " + s);
            }
            iterator.nextKey();
            counter++;
        }
        reader.close();

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("termDictionary"),"UTF-8"));
        for(String entry: output){
            bw.write(entry + "\n");
        }
        bw.close();

        System.out.println("Number of Terms that occur once: " + String.valueOf(frequencies[0]));
        System.out.println("Number of Terms that occur twice: " + String.valueOf(frequencies[1]));
        System.out.println("Number of Terms that occur thrice: " + String.valueOf(frequencies[2]));
        System.out.println("Remaining Terms: " + String.valueOf(counter));

    }
}
