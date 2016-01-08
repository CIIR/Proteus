package ciir.proteus.parse;

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

public class TermEncoder {

    String writeOutLocation;
    int offset;

    public TermEncoder(String possibleWriteOutLocation) {

        this.writeOutLocation = possibleWriteOutLocation;
        offset = 10;

    }

    public static void main(String[] args) throws Exception {
        String termFile = args[0];

        ArrayList<String> output = new ArrayList<String>();
        int counter = 0;


        BufferedReader br = Files.newBufferedReader(Paths.get(termFile));
        String line = null;
        line = br.readLine();
        while (line != null) {
            output.add(Integer.toString(counter) + " " + line.trim());
            counter++;
            line = br.readLine();
        }
        br.close();

        BufferedWriter bw = new BufferedWriter(new FileWriter("termDictionary"));
        for(String entry: output){
            bw.write(entry + "\n");
        }
        bw.close();

    }
}
