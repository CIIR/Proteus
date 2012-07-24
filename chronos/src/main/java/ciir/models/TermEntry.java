package ciir.models;

public class TermEntry implements Comparable<TermEntry> {

	private String m_term;

	private long m_termFrequency;

	private long m_docFrequency;

	private int m_numTokens;

	private double m_probability;

    private double m_weight;

	public TermEntry(String term, long termFrequency, int numTokens) {
		m_term = term;
		m_termFrequency = termFrequency;
		m_docFrequency = 0;
		m_numTokens = numTokens;
		m_probability = 0.0;
		m_weight = 0.0;
	}
	
	public TermEntry(String term, long termFrequency, int numTokens, long documentFrequency) {
		m_term = term;
		m_termFrequency = termFrequency;
		m_docFrequency = documentFrequency;
		m_numTokens = numTokens;
		m_probability = 0.0;
		m_weight = 0.0;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof TermEntry) {
			TermEntry te = (TermEntry) o;
			return ((te.getTerm() == m_term) && 
					(te.getFrequency() == m_termFrequency));
		}
		return false;
	}

    public double getWeight() { 
	return m_weight; 
    }

    public void setWeight(double weight) {
	m_weight = weight;
    }
    

	public String getTerm() { 
		return m_term; 
	}

	public long getFrequency() { 
		return m_termFrequency; 
	}

	public long getDocumentFrequency() { 
		return m_docFrequency; 
	}

	public double getProbability() {
		return m_probability;
	}

	public void setProbability(double p) {
		m_probability=p;
	}

	@Override
	public int hashCode() {
		String hashStr = m_term + m_termFrequency;
		return (hashStr.hashCode());
	}

	public void incrementTermFrequency() {
		m_termFrequency++;
	}
	
	public void addTermFrequency(long frequency) {
		m_termFrequency += frequency;
	}

	public void incrementDocFrequency() {
		m_docFrequency++;
	}
	
	public void addDocFrequency(long docFreq) {
		m_docFrequency += docFreq;
	}


	public int getNumTokens() {
		return m_numTokens;
	}
	
	@Override
	public String toString() {
		return m_term + ":" + m_termFrequency;
	}

	/**
	 * The comparison follows two sort keys:
	 * The entry with higher frequency is "first" - so, it's actually "less than"
	 * in the comparison sense
	 * If frequencies match, we go by standard lexical order in the comparison sense.
	 * 
	 * @param termEntry o
	 */
	public int compareTo(TermEntry o) {

		// First check frequencies
		if (m_termFrequency < o.getFrequency()) {
			return 1;
		} else if (m_termFrequency > o.getFrequency()) {
			return -1;
		}

		// Frequencies are equal, just use whatever the lexographical ordering is
		return (m_term.compareTo(o.getTerm()));
	}


}