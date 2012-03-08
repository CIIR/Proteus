
//package semsearch;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.zip.GZIPInputStream;
import java.net.URLDecoder;

import org.apache.commons.lang3.StringEscapeUtils;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.NxParser;

public class RdfToIndriConverter {
	
	int m_curDoc = 0;
	private File m_curFile;
	private final File m_outputDir;
	private PrintWriter m_outputWriter;
	
	private HashSet<String> m_fields = new HashSet<String>();
	private HashSet<String> m_ontologies = new HashSet<String>();
	private PrintWriter m_fieldWriter;
	private PrintWriter m_ontologyWriter;
	private PrintWriter m_docNoToUrlWriter;

	private Pattern base_pattern = Pattern.compile("http://(dbpedia.org|rdf.freebase.com).*");

	public RdfToIndriConverter(File outputDir) throws Exception {
		m_outputDir = outputDir;
		m_ontologyWriter = new PrintWriter(new File(m_outputDir + "/ontologies"));
	}
	
	public void parse(File file) 
	throws Exception {
		m_curFile = file;
		
		String outputXmlPath = m_outputDir + "/" + m_curFile.getName();
		outputXmlPath = outputXmlPath.replace(".gz", "");
		outputXmlPath = outputXmlPath + "";
		
		String fieldPath = m_outputDir + "/" + m_curFile.getName();
		fieldPath = fieldPath.replace(".gz", "");
		fieldPath = fieldPath + "_fields";
		
		m_outputWriter = new PrintWriter(outputXmlPath);
		m_fieldWriter = new PrintWriter(new File(fieldPath));
		
		String docNoToUrlPath = fieldPath.replaceAll("fields", "docNoToUrl");
		m_docNoToUrlWriter = new PrintWriter(new File(docNoToUrlPath + ".name"));
		
		System.out.println("Now processing file:" + m_curFile.getName());
		InputStream is = new FileInputStream(file);
		try {
			NxParser nxp = new NxParser(new GZIPInputStream(is,10*1024*1024),false);
			Node subj = null;
			HashMap<Node,Node> attrs = new HashMap<Node,Node>();
			
			// max number of records per input file to process.  
			int limit = 100000000;
			
			int numSubjects = 0;
			
			while (nxp.hasNext() && numSubjects < limit) {
				
				Node[] ns = nxp.next();
				
				Node curSubject = ns[0];
				Node predicate = ns[1];
				Node object = ns[2];

				// Exclude subjects which aren't useful (non-dbpedia/freebase, etc.)				
				if (!filterSubject(clean(curSubject.toN3())))
					continue;

				if (subj !=null && !subj.equals(curSubject) ) {
					if (numSubjects % 1000 ==0 && numSubjects > 0 ) {
						System.out.println("Parsing tuple:" + numSubjects);
					}
					attributesToXml(subj, attrs);
					numSubjects++;
					attrs = new HashMap<Node,Node>();
				}
				
				m_ontologies.add(stripAnchor(clean(predicate.toN3())));
				String field = toIndriSafeFieldString(clean(predicate.toN3()));
				m_fields.add(field);
				
				subj = curSubject;
				attrs.put(predicate, object);
			}

			// be sure to convert the last subject.Personennamendatei
			if (subj != null)
			    attributesToXml(subj, attrs);
			
			writeFields();
			//writeOntologies();
		} finally {
			is.close();
			m_fieldWriter.close();
			m_ontologyWriter.close();
			m_outputWriter.close();
			m_docNoToUrlWriter.close();
		}
		
	}
	
	private String stripAnchor(String predicateUrl) {
		int anchorIdx = predicateUrl.indexOf("#");
		if (anchorIdx > -1) {
			return predicateUrl.substring(0,anchorIdx);
		} else {
			return predicateUrl;
		}
	}
	
	private void writeFields() throws Exception {
		for (String field : m_fields) {
			m_fieldWriter.println("<field><name>" + field + "</name></field>");
		}
		m_fields.clear();
	}
	
	private void writeOntologies() throws Exception {
		
		String[] sortedOntologies = m_ontologies.toArray(new String[0]);
		Arrays.sort(sortedOntologies);
		for (String ontology : sortedOntologies) {
			m_ontologyWriter.println(ontology);
		}
		
		m_ontologies.clear();
	}

	/**
	 * Write a subject as XML with all of its predicates and objects.
	 * 
	 * @param subject
	 * @param attributeMap
	 * @throws Exception
	 */
	public void attributesToXml(Node subject, HashMap<Node,Node> attributeMap) 
	throws Exception {
			startDoc();
			String url = clean(subject.toN3());
			writeDocNo(url);
			writeUrl(url);
			
			if (url.startsWith("http://dbpedia.org")) {
				// grab the end of it as the title.
				int lastSegment = url.lastIndexOf('/');
				String value = url.substring(lastSegment+1, url.length()).toLowerCase();
				addField("dpedia-title", value);
			}
			
			//System.out.println("\ndocid = "+ clean(subject.toN3()) + " ; num attributes:" + attributeMap.size());

			boolean hasType = false;
			
			String title = null;
			String name = null;
			String synonym = null;
			
	
			StringBuilder text = new StringBuilder();
			for (Node key : attributeMap.keySet()) {
				String field = toIndriSafeFieldString(clean(key.toN3()));
				String value = clean(attributeMap.get(key).toN3());
				//System.out.println(field + ":" + value);

				String fieldLower = field.toLowerCase();
				if (fieldLower.endsWith("type")) {
					hasType = true;
					addField("objectType", extractType(value));
				}
				if (fieldLower.endsWith("title") && title == null) {
					if (value.toLowerCase().startsWith("http")) {
						int lastSegment = url.lastIndexOf('/');
						title = url.substring(lastSegment+1, url.length()).toLowerCase();
					} else {
						title = value.toLowerCase();
					}
					addField("object-title", title);
				}
				
				if (fieldLower.endsWith("name") && name == null) {
					if (value.toLowerCase().startsWith("http")) {
						int lastSegment = url.lastIndexOf('/');
						name = url.substring(lastSegment+1, url.length()).toLowerCase();
					} else {
						name = value.toLowerCase();
					}
					addField("object-name", name);
				}

				// Field for the textual synonyms from objects
				if (fieldLower.endsWith("wikipagedisambiguates") || fieldLower.endsWith("wikipageredirects")) {
					int lastSegment = value.lastIndexOf('/');		
					// Decoding the URL, and removing posessives and periods
					synonym = URLDecoder.decode(value.substring(lastSegment+1, value.length()), "UTF-8").replaceAll("'s|\\.", "");
					// Replacing remaining puntuation with spaces
					synonym = Pattern.compile("[,;=)(#_:^'`?!.//\\**-]").matcher(synonym).replaceAll(" ");
					// Removing excess whitespace
					synonym = Pattern.compile(" +").matcher(synonym).replaceAll(" ").trim();
					addField("object-synonym", synonym);
				} //else if () // add field for synonym and alternate name fields
				
				// Field for all the links within the database
				if (filterSubject(value)) {
					addField("internal-link", extractURLName(value));
				}

				// Field for all the links outside the database
				if (fieldLower.endsWith("externallink") || fieldLower.endsWith("homepage")) {
					addField("external-link", value);
				}

				// Put all text based fields into the text-value
				if (!value.toLowerCase().startsWith("http") && value.indexOf(' ') > -1) {
					text.append(value);
				}
				
				addField(field, value);
			}
			
			addField("text-value", text.toString());
			
			if (!hasType) {
				// lump it as a miscellaneous object type
				addField("objectType", "object-other");
			}
			endDoc();
			m_curDoc++;
	}
	 
	
	private String extractType(String url) {
		// the last segment of the URL is the type.
		int lastSegment = url.lastIndexOf('/');
		String value = url.substring(lastSegment+1, url.length()).toLowerCase();
		int anchorIdx = value.indexOf('#');
		if ( anchorIdx > -1) {
			value = value.substring(anchorIdx+1, value.length());
		}
		
		value = value.replace('"', ' ').trim();
		
		return value;
	}

	private String extractURLName(String url) {
	  // the last segment of the URL (for the resource) is the canonical name
	  int lastSegment = url.lastIndexOf('/');
		String value = url.substring(lastSegment+1, url.length());
		Matcher matcher = base_pattern.matcher(url);
		if(matcher.find()) {
			String database = matcher.group(1);
			if(database.endsWith("dbpedia.org"))
				value = "dbp:" + value;
			else if(database.endsWith("freebase.com"))
				value = "fb:" + value;
		}
		return value.substring(0, java.lang.Math.min(255, value.length()));
  }

	// Filter out certain subjects
	private boolean filterSubject(String subject) {
		return base_pattern.matcher(subject.toLowerCase()).matches();
	}
	
	private void writeDocNo(String url) {
		
		String docNo = extractURLName(url);//m_curFile.getName().replace("-urified.gz", "");
		//docNo = docNo + "_" + m_curDoc;
		m_docNoToUrlWriter.println(docNo + "\t" + url);
		m_outputWriter.print("<DOCNO>");
		m_outputWriter.print(docNo);
		m_outputWriter.print("</DOCNO>\n<DOCHDR>\n" + url + "\n</DOCHDR>\n");
		
	}

	private String clean(String str) {
		String newString = str.replace("<", "");
		newString = newString.replace(">", "");
		return newString;
	}
	
	private String toIndriSafeFieldString(String str) {
		
		String value = str;
		Pattern pattern = Pattern.compile("[^a-zA-Z]");
		value = pattern.matcher(value).replaceAll("");
		return value;
	}
	
	private String toIndriSafeTextString(String str) {
		
		String value = str;
		Pattern pattern = Pattern.compile("[^a-zA-Z]");
		value = pattern.matcher(value).replaceAll(" ");
		return value;
	}
	
	private void startDoc() {
		m_outputWriter.println("\n<DOC>");
	}
	
	private void endDoc() {
		m_outputWriter.println("\n</DOC>");
	}
	
	private void writeUrl(String url) {
		m_outputWriter.println("<url>");
		m_outputWriter.print(StringEscapeUtils.escapeXml(url));
		m_outputWriter.print("</url>");
	}
	
	private void addField(String field, String value) {
		m_outputWriter.println("<" + field + ">");
		m_outputWriter.print(StringEscapeUtils.escapeXml(value));
		m_outputWriter.println("\n</" + field + ">");
	}
	

	
	
	public static void main(String[] args)
	throws Exception {
		System.out.println("arguments: " + args[0] + " " + args[1]);
		File inputFile = new File(args[0]);
		File outputDir = new File(args[1]);
		RdfToIndriConverter reader = new RdfToIndriConverter(outputDir);

		if (inputFile.isDirectory()) {
			cleanDirectory(outputDir);
			
			FilenameFilter filter = new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.endsWith(".gz");
				}
			}; // End of anonymous inner class

			File[] xmlFiles = inputFile.listFiles(filter);
			for (File xmlFile : xmlFiles) {
				reader.parse(xmlFile);	
			}
				
		} else if (inputFile.getName().endsWith(".gz")) {
			reader.parse(inputFile);	
		}


	}
	
	private static void cleanDirectory(File dir) throws Exception {
		if (dir.exists()) {
			File[] xmlFiles = dir.listFiles();
			for (File file : xmlFiles) {
				file.delete();
			}
		} else {
			dir.mkdir();
			if (!dir.exists()) {
				throw new Exception("Unable to create output directory.");
			}
		}

	}
}

