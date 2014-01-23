package ciir.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.lemurproject.galago.core.parse.Document;
//import org.galagosearch.core.parse.Document;

/**
 * Represents a model of words in a collection of documents.
 * 
 * @author Marc Cartright
 * @author Jeff Dalton
 * @author Elif Aktoga
 *
 */
public class LanguageModel implements Cloneable {
 
	/**
	 * Stores a map of words to information about it
	 * in the document
	 */
    private HashMap<String, TermEntry> m_entries;
    
	/**
	 * Stores a map of term entries keyed on their suffix term. 
	 */
    private HashMap<String, ArrayList<TermEntry>> m_suffixList;
    
    /**
     * Stores the set of the distinct terms encountered in the last
     * document.  This is used 
     */
    private HashSet<String> m_distinctTermsInLastDoc;
    
    /**
     * The default size of n-grams in words
     */
    private static final int DEFAULT_WINDOW_SIZE = 3;
    
    /**
     * The maximum phrase length to compute for the language model
     */
    private int m_phraseWindowSize;
    
    /**
     * A window used to create n-grams of terms in the document
     */
    private List<String> m_phraseWindow = new LinkedList<String>();

    /**
     * The total number of terms in the model, i.e. the sum
     * of all term frequencies for each word.
     * 
     * Note: This may include counts for terms that were ignored
     * by the language model (such as stop words).
     */
    private long m_collectionFrequency;
    
    /**
     * The minimum length terms to keep.  This is useful for removing
     * unigrams from the model.
     */
    private int m_minNgramLength = 1;
	
	/**
     * Constructor with default window size for n-grams
     */
    public LanguageModel() {    
    	this(DEFAULT_WINDOW_SIZE);
    }

    /**
     * Constructor
     * 
     * @param windowSize length of n-grams to create 
     */
    public LanguageModel(int windowSize) {
    	m_entries = new HashMap<String, TermEntry>();
    	m_suffixList = new HashMap<String, ArrayList<TermEntry>>();
    	m_distinctTermsInLastDoc = new HashSet<String>();
    	m_collectionFrequency = 0;
    	m_phraseWindowSize = windowSize;
    }

    public long getCollectionFrequency() {
    	return m_collectionFrequency;
    }

    /**
     * Add a document to the language model.
     * 
     * @param Document to add
     * @param filterStopWords, if true then stop words are not used in the model
     */
    public void addDocument(Document d, boolean filterStopWords) 
    throws Exception {
    	m_distinctTermsInLastDoc.clear();
    	m_phraseWindow.clear();
    	Iterator<String> iter = d.terms.iterator();
    	
    	while (iter.hasNext()) {
    		String term = iter.next();
    		
    		//System.out.println(term);
    		
//    		// skip terms less than three characters long
//    		if (term.length() < 3) {
//    			continue;
//    		}
    		
    		if (filterStopWords && StopWordList.isStopWord(term)) {
    			continue;
    		}
    		generateTerms(term);
    		
    	}
    	finishGeneratingTerms();

    	// update document frequencies
    	for (String term : m_distinctTermsInLastDoc) {
    		TermEntry t = getTermEntry(term);
    		t.incrementDocFrequency();
    	}
    } 
    
    /**
     * Add a document to the language model.
     * 
     * @param Document to add
     * @param filterStopWords, if true then stop words are not used in the model
     */
    public void addDocument(Iterable<String> terms, boolean filterStopWords) 
    throws Exception {
    	m_distinctTermsInLastDoc.clear();
    	m_phraseWindow.clear();

    	for (String term : terms) {
    		//System.out.println(term);
    		
    		// skip terms less than three characters long
    		if (term.length() < 1) {
    			continue;
    		}
    		
    		if (filterStopWords && StopWordList.isStopWord(term)) {
    			continue;
    		}
    		generateTerms(term);
    		
    	}
    	finishGeneratingTerms();

    	// update document frequencies
    	for (String term : m_distinctTermsInLastDoc) {
    		TermEntry t = getTermEntry(term);
    		t.incrementDocFrequency();
    	}
    }
    
    /**
     * Add a document to the language model.
     * 
     * @param Document to add
     * @param filterStopWords, if true then stop words are not used in the model
     */
    public void addDocument(String[] terms, boolean filterStopWords) 
    throws Exception {
    	m_distinctTermsInLastDoc.clear();
    	m_phraseWindow.clear();

    	for (String term : terms) {
    		//System.out.println(term);
    		
    		// skip terms that are empty
    		if (term.length() < 1) {
    			continue;
    		}
    		
    		if (filterStopWords && StopWordList.isStopWord(term)) {
    			continue;
    		}
    		generateTerms(term);
    		
    	}
    	finishGeneratingTerms();

    	// update document frequencies
    	for (String term : m_distinctTermsInLastDoc) {
    		TermEntry t = getTermEntry(term);
    		t.incrementDocFrequency();
    	}
    }

    /**
     * Returns all terms in the language model. Note this includes whatever-sized
     * n-grams you may have told it to track
     */
    public Collection<TermEntry> getEntries() {
    	return m_entries.values();
    }

    /**
     * Add a term to the language model, including the term
     * itself and possible n-grams terminating in the current
     * term.
     * 
     * @param term
     */
    private void generateTerms(String term) {
    	if (m_phraseWindow.size() >= m_phraseWindowSize) {
    		m_phraseWindow.remove(0);
    	}
    	m_phraseWindow.add(term);
    	if (m_minNgramLength == 1) {
    		addEntry(term, 1);
    	}
    	//System.out.println("New term: " + term);
    	StringBuilder sb = new StringBuilder();
    	for (int i=0; i < m_phraseWindow.size(); i++) {
    		 sb.append(m_phraseWindow.get(i));
    		 sb.append(" ");
    		 if (i > 0 && m_phraseWindow.size() == m_phraseWindowSize) {
    			 // generate phrases.
    			 String phrase = sb.toString().trim();
    			 int termLength = i+1;
    			 if (termLength >= m_minNgramLength) {
    				 addEntry(phrase, termLength);
    			 }
    			// System.out.println("phrase: " + phrase);
    		 }
    	}
    	
    }
    
    /**
     * Flush n-grams still in the pipeline.
     */
    private void finishGeneratingTerms() {
    	while (m_phraseWindow.size() > 1) {
    		m_phraseWindow.remove(0);
    		StringBuilder sb = new StringBuilder();
    		for (int i=0; i < m_phraseWindow.size(); i++) {
    			sb.append(m_phraseWindow.get(i));
    			sb.append(" ");
    			if (i > 0) {
    				// generate phrases.
    				String phrase = sb.toString().trim();
    				 int termLength = i+1;
    				 if (termLength >= m_minNgramLength) {
    					 addEntry(phrase, termLength);
    				 }
    			//	System.out.println("phrase: " + phrase);
    			}
    		}
    	}
    }
    
    /**
     * Add a term to the language model
     * 
     * @param term
     * @param numTokens the number of words in the term
     */
    private void addEntry(String term, int numTokens) {
    	TermEntry entry = m_entries.get(term);
    	if (entry == null) {
    		entry = new TermEntry(term, 1, numTokens);
    		m_entries.put(term, entry);
    	} else {
    		entry.incrementTermFrequency();
    	}
    	
    	// NOTE: We only care about being able to look up distinct
    	// tri-grams.  These will only have one entry since the term
    	// at the end is distinct.
    	// THIS IS A HACK!
    	if (numTokens > 2 && entry.getFrequency() == 1) {
    		String[] terms = entry.getTerm().split(" ");
    		ArrayList<TermEntry> suffixTerms = m_suffixList.get(terms[2]);
    		if (suffixTerms == null) {
    			suffixTerms = new ArrayList<TermEntry>();
    			suffixTerms.add(entry);
    			m_suffixList.put(terms[2], suffixTerms);
//    		} else {
//    			boolean found = false;
//    			for (TermEntry te : suffixTerms) {
//    				if (te.equals(entry)) {
//    					found = true;
//    				}
//    			}
//    			if (!found) {
//    				suffixTerms.add(entry);
//    			}
    		}
    	}
    	m_collectionFrequency++;
    	m_distinctTermsInLastDoc.add(term);
    }
    
    public ArrayList<TermEntry> lookupTermsBySuffix(String suffix) {
    	return m_suffixList.get(suffix);
    }
    
    /**
     * Add a term entry to the language model
     * 
     * @param term
     * @param numTokens the number of words in the term
     */
    private void addEntry(TermEntry te) {
    	String term = te.getTerm();
    	TermEntry entry = m_entries.get(term);
    	if (entry == null) {
    		entry = new TermEntry(term, te.getFrequency(), te.getNumTokens(), te.getDocumentFrequency());
    		m_entries.put(term, entry);
    	} else {
    		entry.addTermFrequency(te.getFrequency());
    		entry.addDocFrequency(te.getDocumentFrequency());
    	}
    	m_collectionFrequency += te.getFrequency();
    	// we do not need to update the distinct terms found since this should only be used for merging language
    	// models that have already been distinctified.
    }
    
    /**
     * Return the most frequent terms from the language model
     * 
     * @param k the number of most frequent terms to return
     * @param filterStopWords whether or not to filter stop words
     * @param minTokenLength the minimum length in tokens to return the phrase
     * @return
     */
    public ArrayList<TermEntry> getKTopFrequencies(int k, boolean filterStopWords, int minTokenLength) 
    throws Exception {
    	if (k < 1 || minTokenLength < 1) {
    		throw new IllegalArgumentException("Minimum num tokens is 1 and min token length is 1.");
    	}
    	
    	TermEntry[] terms = m_entries.values().toArray(new TermEntry[0]);
    	Arrays.sort(terms);
    	ArrayList<TermEntry> chosen = new ArrayList<TermEntry>();
    	for (TermEntry term :  terms) {
    		if (chosen.size() >= k) {
    			break;
    		}
    		String word = term.getTerm();
    		
    		if (term.getNumTokens() < minTokenLength) {
    			// skip all tokens under the minimum token length
    			continue;
    		}
    		if (filterStopWords) {
    			
    			if (term.getNumTokens() == 1) {
    				if (!StopWordList.isStopWord(word) && word.length() > 3) {
            			chosen.add(term);
            		}
    			} else {
    				// Filter out terms that start with stop words or end in stop words
        			if (term.getNumTokens() > 1) {
        				String[] splitTerm = word.split(" ");
        				if (!StopWordList.isStopWord(splitTerm[0]) && !StopWordList.isStopWord(splitTerm[splitTerm.length-1])) {
        					chosen.add(term);
        				}
        			}
    			}
    		} else {
    			chosen.add(term);
    		}
    		
    	}
    	return chosen;
    }
    
    public void calculateProbabilities() {
    	Collection<TermEntry> terms = m_entries.values();
    	for (TermEntry t : terms)
    		t.setProbability(((double) t.getFrequency())/m_collectionFrequency);
    }

    /**
     *  Merges two language models.  The two original models are unchanged.
     *  
     *  @return the merged language model.
     **/
    public static LanguageModel unionModel(LanguageModel lm1, LanguageModel lm2) {
    	
    	// Only keep terms that are longer than the shortest min ngram length
    	int minLangModelLen = Math.min(lm1.getMinNgramLength(), lm2.getMinNgramLength());
    	LanguageModel newModel = new LanguageModel();
    	
    	// Add the terms from LM1
    	Collection<TermEntry> lm1Entries = lm1.m_entries.values();
    	for (TermEntry te : lm1Entries) {
    		if (te.getNumTokens() >= minLangModelLen) {
    			newModel.addEntry(te);
    		}
    	}
    	
    	// Add the terms from LM2
    	Collection<TermEntry> lm2Entries = lm2.m_entries.values();
    	for (TermEntry te : lm2Entries) {
    		if (te.getNumTokens() >= minLangModelLen) {
    			newModel.addEntry(te);
    		}
       	}

    	newModel.calculateProbabilities();
    	return newModel;

    }

    /**
     * Subtracts the probabilities of LM2 - LM1, creating a new language model
     * with only the terms where the probability is greater than 0.
     * 
     * BIG HAIRY WARNING, use the resultin model WITH EXTREME CARE.
     * 
     * The resulting language model is only half baked!  Not all members
     * can be set in a consistent way!  
     * 
     * @param lm1 to subtract 
     * @param lm2 to use as the basis for subtraction
     * @return a new language model with lm2-lm1 word probabilities
     */
    public static LanguageModel subtractModel(LanguageModel lm1, LanguageModel lm2) {
    	// copy this LM to create the subtracted model
    	LanguageModel newModel = new LanguageModel();

    	// Add the terms from LM2
    	Collection<TermEntry> lm2Entries = lm2.m_entries.values();
    	for (TermEntry te : lm2Entries) {
    		newModel.addEntry(te);
    	}
    	
    	newModel.calculateProbabilities();
   	
    	// do subtraction
    	Collection<TermEntry> lm1Entries = lm1.m_entries.values();
    	for (TermEntry t : lm1Entries) {
    		String term = t.getTerm();
    		TermEntry thisLMTermEntry = newModel.getTermEntry(term);
    		if (thisLMTermEntry!=null) {
    			double newProbability = thisLMTermEntry.getProbability() - t.getProbability();
    			if (newProbability<=0.0) {
    				newModel.remove(term);
    			} else {
    				thisLMTermEntry.setProbability(newProbability);
    			}
    		}
    	}
    	return newModel;
    }

    /** 
     * Performs LM2 - LM1 in terms of terms instead of probabilities.  
     * 
     * Unlike subtractModel, where the probabilities of the given model are subtracted from this model, 
     * here we exclude the terms occurring in the given model from this model.
     * 
     * Note: This only removes exact terms.  If there are phrases where the n-grams differ, these will
     * not be removed, i.e. even if you remove the unigrams from l2, all the terms where non-unigrams will
     * remain.
     * */
    public static LanguageModel excludeModel(LanguageModel lm1, LanguageModel lm2) {
    	// copy this LM to create the subtracted model
    	LanguageModel newModel = new LanguageModel();

    	// Add the terms from LM2
    	Collection<TermEntry> lm2Entries = lm2.m_entries.values();
    	for (TermEntry te : lm2Entries) {
    		newModel.addEntry(te);
    	}
    	
    	// do exclusion
    	Collection<TermEntry> entries = lm1.m_entries.values();
    	for (TermEntry t : entries) {
    		newModel.remove(t.getTerm());
    	}

    	newModel.calculateProbabilities();
    	return newModel;
    }
        
    /**
     * Remove a term from the language model.
     * This can be used to remove stopwords or undesired
     * words.
     * 
     * @param term to remove from the language model
     */
	public void remove(String term) {
		TermEntry removedTerm = m_entries.remove(term);
		if (removedTerm !=null) {
			m_collectionFrequency -= removedTerm.getFrequency();
		}
	}
	
	public TermEntry[] getSortedTermEntries() {
		TermEntry[] terms = m_entries.values().toArray(new TermEntry[0]);
    	Arrays.sort(terms);
    	return terms;
	}

    /**
     * Lookup a term in the language model.
     * 
     * @param term to lookup in the language model
     * @return the term entry, or null if the term does not exist
     */
    public TermEntry getTermEntry(String term) { 
    	return m_entries.get(term); 
    }
    
    /**
     * Return all the terms in the language model
     * 
     * @return
     */
    public Collection<TermEntry> getVocabulary() {
    	return (m_entries.values());
    }
    
    public int getMinNgramLength() {
		return m_minNgramLength;
	}

	public void setMinNgramLength(int minNgramLength) {
		m_minNgramLength = minNgramLength;
	}
	
    @Override
	public String toString() {
    	StringBuilder sb = new StringBuilder();
    	TermEntry[] terms = getSortedTermEntries();
    	for (TermEntry term : terms) {
    		sb.append(term.toString() + "\n");
    	}
    	return sb.toString();
    }

    /**
     * Filters words with document frequencies below the specified threshold.
     * 
     * @param document frequency threshold
     * @return the list of terms removed because they were below the threshold
     */
    public ArrayList<String> filterWordsWithDocumentFrequencyLessThan(int threshold) {
    	// build the list to be removed
    	ArrayList<String> toBeRemoved = new ArrayList<String>();
    	for (TermEntry t : m_entries.values()) {
    		if (t.getDocumentFrequency() < threshold) {
    			toBeRemoved.add(t.getTerm());	
    		}
    	}   
    	
    	// remove them from the model
    	for (String t : toBeRemoved) {
    		remove(t);
    	}

    	calculateProbabilities();
    	return toBeRemoved;
    }
    
}