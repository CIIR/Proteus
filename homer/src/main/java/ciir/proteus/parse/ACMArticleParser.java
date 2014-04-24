package ciir.proteus.parse;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.parse.DocumentStreamParser;
import org.lemurproject.galago.core.types.DocumentSplit;
import org.lemurproject.galago.tupleflow.Parameters;

/**
 * Parse trectext format dataset of acm articles. Adding metadata fields to
 * parsed document obect.
 * 
 * @author Jiepu Jiang
 * @version Apr 23, 2014
 */
public class ACMArticleParser extends DocumentStreamParser {
	
	BufferedReader reader;
	
	public ACMArticleParser(DocumentSplit split, Parameters p) throws FileNotFoundException, IOException {
		super(split, p);
		this.reader = getBufferedReader(split);
	}
	
	public String waitFor(String tag) throws IOException {
		String line;

		while ((line = reader.readLine()) != null) {
			if (line.startsWith(tag)) {
				return line;
			}
		}

		return null;
	}
	
	public String parseDocNumber() throws IOException {
		
		String allText = waitFor("<DOCNO>");
		if (allText == null) {
			return null;
		}

		while (allText.contains("</DOCNO>") == false) {
			String line = reader.readLine();
			if (line == null) {
				break;
			}
			allText += line;
		}

		int start = allText.indexOf("<DOCNO>") + 7;
		int end = allText.indexOf("</DOCNO>");

		return new String(allText.substring(start, end).trim());
		
	}
	
	public Document nextDocument() throws IOException {
		
		String line;
		if (reader == null || null == waitFor("<DOC>")) {
			return null;
		}
		String identifier = parseDocNumber();
		if (identifier == null) {
			return null;
		}
		
		// note that it is different from the original loop in TrecTextParser in that after <docno> line, 
		// all the following lines are considered document content
		StringBuilder buffer = new StringBuilder();
		while ((line = reader.readLine()) != null) {
			if (line.startsWith("</DOC>")) {
				break;
			}
			buffer.append(line);
			buffer.append('\n');
		}
		
		Document doc = new Document(identifier, buffer.toString());
		if( doc!=null && doc.text!=null ) {
			String text = doc.text;
			storeMetadata(doc, text, "artid");
			storeMetadata(doc, text, "procid");
			storeMetadata(doc, text, "proc");
			storeMetadata(doc, text, "pubyear");
			storeMetadata(doc, text, "citation");
			storeMetadata(doc, text, "title");
			storeMetadata(doc, text, "author");
			storeMetadata(doc, text, "institution");
			storeMetadata(doc, text, "address");
			storeMetadata(doc, text, "email");
			storeMetadata(doc, text, "keyword");
		}
		return doc;
	}
	
	private void storeMetadata(Document doc, String text, String tag) {
		StringBuilder sb = new StringBuilder();
		Pattern p = Pattern.compile( "<"+tag+">(.+?)</"+tag+">", Pattern.CASE_INSENSITIVE + Pattern.DOTALL + Pattern.MULTILINE );
		Matcher m = p.matcher(text);
		boolean first = true;
		while( m.find() ){
			if(first){
				first = false;
			}else{
				sb.append("; ");
			}
			sb.append(m.group(1));
		}
		doc.metadata.put(tag, sb.toString().trim());
//		System.out.println(" >> putting metadata "+tag+" "+sb.toString().trim());
	}
	
	public void close() throws IOException {
		if (this.reader != null) {
			this.reader.close();
			this.reader = null;
		}
	}
	
}
