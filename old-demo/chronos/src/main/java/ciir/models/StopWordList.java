package ciir.models;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

public class StopWordList {

	private static final String[] STOP_WORDS = {
		"a", "about", "and", "are", "an", "also", "are", "as", "at", "be", "but", "by", "com",
		"for", "from", "he", "how", "his", "has", "have", "if", "i", "in", "into", "is", "it",
		"its", "may", "no", "not", "of", "on", "or", "s", "she", "such", "should",
		"t", "that", "the", "this", "their", "then", "there", "these",
		"they", "this", "to", "was", "were", "what", "when", "where", "who", "will", "with",
		"you"
		}; 
	
	private static Set<String> m_stopSet = null; 
	
	public static boolean isStopWord(String word) 
	throws Exception {
		if (m_stopSet == null) {
			m_stopSet = makeStopSet(STOP_WORDS, true);
		}
		return m_stopSet.contains(word.toLowerCase());
	} 
	
	 /**
	* @param stopWords
	* @param ignoreCase If true, all words are lower cased first.
	* @return a Set containing the words

	*/
	private static final Set<String> makeStopSet(String[] stopWords, boolean ignoreCase) {
		HashSet<String> stopTable = new HashSet<String>(stopWords.length);
		for (int i = 0; i < stopWords.length; i++)
			stopTable.add(ignoreCase ? stopWords[i].toLowerCase() : stopWords[i]);
		return stopTable;
	} 
	
	/**
	 * Reads stop words from a file, one per line.  
	 * 
	 * It ignores lines commented out with //
	 * 
	 * @param stopwordFile
	 */
	public static final void reloadStopWordFromInputStream(DataInputStream inputStream, boolean ignoreCase) 
	throws Exception {
		HashSet<String> stopTable = new HashSet<String>();
		BufferedReader br  = new BufferedReader(new InputStreamReader(inputStream));
		try {
			int numLoaded = 0;
			while (br.ready()) {
				String line = br.readLine();
				if (line.trim().length() > 0 && !line.startsWith("//")) {
					stopTable.add(ignoreCase ? line.toLowerCase() : line);
					numLoaded++;
				}
			}
			System.out.println("Loaded: " + numLoaded + " stopwords.");
		} finally {
			br.close();
		}
		m_stopSet = stopTable;
	}
}