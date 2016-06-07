package ciir.proteus.parse;

import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.parse.DocumentStreamParser;
import org.lemurproject.galago.core.types.DocumentSplit;
import org.lemurproject.galago.utility.Parameters;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse one JSON record per line.
 * 
 * @author David Smith
 */
public class JSONLineParser extends DocumentStreamParser {
	
    BufferedReader reader;
	
    public JSONLineParser(DocumentSplit split, Parameters p) throws FileNotFoundException, IOException {
	super(split, p);
	this.reader = getBufferedReader(split);
    }
	
    public Document nextDocument() throws IOException {
	String line;
	if ( reader == null || (line = reader.readLine()) == null ) {
	    return null;
	}

	Parameters f = Parameters.parseString(line);

	Document doc = new Document();

	doc.name = f.get("name", "");
	doc.text = f.get("text", "");

	for (String key: f.getKeys()) {
	    if ( !key.equals("name") && !key.equals("text") ) {
		doc.metadata.put(key, f.getAsString(key));
	    }
	}

	return doc;
    }
    
    public void close() throws IOException {
	if (this.reader != null) {
	    this.reader.close();
	    this.reader = null;
	}
    }
}
