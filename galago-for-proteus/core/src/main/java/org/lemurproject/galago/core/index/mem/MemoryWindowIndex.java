// BSD License (http://lemurproject.org/galago-license)
package org.lemurproject.galago.core.index.mem;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.lemurproject.galago.core.index.AggregateReader;
import org.lemurproject.galago.core.index.KeyIterator;
import org.lemurproject.galago.core.index.AggregateReader.AggregateIterator;
import org.lemurproject.galago.core.index.CompressedByteBuffer;
import org.lemurproject.galago.core.index.ValueIterator;
import org.lemurproject.galago.core.index.disk.WindowIndexWriter;
import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.parse.Tag;
import org.lemurproject.galago.core.parse.stem.Stemmer;
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.core.retrieval.query.NodeType;
import org.lemurproject.galago.core.retrieval.iterator.ExtentArrayIterator;
import org.lemurproject.galago.core.retrieval.iterator.MovableExtentIterator;
import org.lemurproject.galago.core.retrieval.iterator.MovableCountIterator;
import org.lemurproject.galago.core.retrieval.iterator.MovableIterator;
import org.lemurproject.galago.core.retrieval.query.AnnotatedNode;
import org.lemurproject.galago.core.util.ExtentArray;
import org.lemurproject.galago.tupleflow.FakeParameters;
import org.lemurproject.galago.tupleflow.Parameters;
import org.lemurproject.galago.tupleflow.Utility;
import org.lemurproject.galago.tupleflow.Utility.ByteArrComparator;
import org.lemurproject.galago.tupleflow.VByteInput;


/*
 * author sjh
 *
 * In-memory window posting index
 *
 */
public class MemoryWindowIndex implements MemoryIndexPart, AggregateReader {

  // this could be a bit big -- but we need random access here
  // perhaps we should use a trie (but java doesn't have one?)
  protected TreeMap<byte[], WindowPostingList> postings = new TreeMap(new ByteArrComparator());
  protected Parameters parameters;
  protected long collectionDocumentCount = 0;
  protected long collectionPostingsCount = 0;
  protected Stemmer stemmer = null;
  
  public MemoryWindowIndex(Parameters parameters) throws Exception {
    this.parameters = parameters;
    
    if (parameters.containsKey("stemmer")) {
      stemmer = (Stemmer) Class.forName(parameters.getString("stemmer")).newInstance();
    }

    // if the parameters specify a collection length use them.
    collectionPostingsCount = parameters.get("statistics/collectionLength", 0);
    collectionDocumentCount = parameters.get("statistics/documentCount", 0);
  }
  
  @Override
  public void addDocument(Document doc) throws IOException {
    collectionDocumentCount += 1;
    collectionPostingsCount += doc.terms.size();
    
    int prevBegin = -1;
    for (Tag tag : doc.tags) {
      assert tag.begin >= prevBegin;
      prevBegin = tag.begin;
      addExtent(Utility.fromString(tag.name), doc.identifier, tag.begin, tag.end);
    }
  }
  
  @Override
  public void addIteratorData(byte[] key, MovableIterator iterator) throws IOException {
    if (postings.containsKey(key)) {
      // do nothing - we have already cached this data
      return;
    }
    
    WindowPostingList postingList = new WindowPostingList(key);
    MovableExtentIterator mi = (MovableExtentIterator) iterator;
    while (!mi.isDone()) {
      int document = mi.currentCandidate();
      ExtentArrayIterator ei = new ExtentArrayIterator(mi.extents());
      while (!ei.isDone()) {
        postingList.add(document, ei.currentBegin(), ei.currentEnd());
        ei.next();
      }
      mi.movePast(document);
    }
    
    postings.put(key, postingList);
  }
  
  @Override
  public void removeIteratorData(byte[] key) throws IOException {
    postings.remove(key);
  }
  
  protected void addExtent(byte[] byteWord, int document, int begin, int end) {
    if (!postings.containsKey(byteWord)) {
      WindowPostingList postingList = new WindowPostingList(byteWord);
      postings.put(byteWord, postingList);
    }
    
    WindowPostingList postingList = postings.get(byteWord);
    postingList.add(document, begin, end);
  }

  // Posting List Reader functions
  @Override
  public KeyIterator getIterator() throws IOException {
    return new KIterator();
  }
  
  @Override
  public ValueIterator getIterator(Node node) throws IOException {
    String term = stemAsRequired(node.getDefaultParameter());
    byte[] byteWord = Utility.fromString(term);
    return getTermExtents(byteWord);
  }
  
  @Override
  public ValueIterator getIterator(byte[] key) throws IOException {
    return getTermExtents(key);
  }
  
  @Override
  public NodeStatistics getTermStatistics(String term) throws IOException {
    term = stemAsRequired(term);
    return getTermStatistics(Utility.fromString(term));
  }
  
  @Override
  public NodeStatistics getTermStatistics(byte[] term) throws IOException {
    WindowPostingList postingList = postings.get(term);
    if (postingList != null) {
      ExtentIterator counts = new ExtentIterator(postingList);
      return counts.getStatistics();
    }
    NodeStatistics stats = new NodeStatistics();
    stats.node = Utility.toString(term);
    return stats;
  }
  
  private ExtentIterator getTermExtents(byte[] term) throws IOException {
    WindowPostingList postingList = postings.get(term);
    if (postingList != null) {
      return new ExtentIterator(postingList);
    }
    return null;
  }

  // try to free up memory.
  @Override
  public void close() throws IOException {
    postings = null;
  }
  
  @Override
  public Map<String, NodeType> getNodeTypes() {
    HashMap<String, NodeType> types = new HashMap<String, NodeType>();
    types.put("extents", new NodeType(ExtentIterator.class));
    return types;
  }
  
  @Override
  public String getDefaultOperator() {
    return "extents";
  }
  
  @Override
  public Parameters getManifest() {
    return parameters;
  }
  
  @Override
  public long getDocumentCount() {
    return collectionDocumentCount;
  }
  
  @Override
  public long getCollectionLength() {
    return collectionPostingsCount;
  }
  
  @Override
  public long getKeyCount() {
    return postings.size();
  }
  
  @Override
  public void flushToDisk(String path) throws IOException {
    Parameters p = getManifest();
    p.set("filename", path);
    p.set("statistics/documentCount", this.getDocumentCount());
    p.set("statistics/collectionLength", this.getCollectionLength());
    p.set("statistics/vocabCount", this.getKeyCount());
    WindowIndexWriter writer = new WindowIndexWriter(new FakeParameters(p));
    
    KIterator kiterator = new KIterator();
    ExtentIterator viterator;
    ExtentArray extents;
    while (!kiterator.isDone()) {
      viterator = (ExtentIterator) kiterator.getValueIterator();
      writer.processExtentName(kiterator.getKey());
      
      while (!viterator.isDone()) {
        writer.processNumber(viterator.currentCandidate());
        extents = viterator.extents();
        for (int i = 0; i < extents.size(); i++) {
          writer.processBegin(extents.begin(i));
          writer.processTuple(extents.end(i));
        }
        viterator.movePast(viterator.currentCandidate());
      }
      kiterator.nextKey();
    }
    writer.close();
  }

  // private functions
  private String stemAsRequired(String term) {
    if (stemmer != null) {
      return stemmer.stem(term);
    }
    return term;
  }

  // sub classes:
  public class WindowPostingList {
    
    byte[] key;
    CompressedByteBuffer documents_cbb = new CompressedByteBuffer();
    CompressedByteBuffer counts_cbb = new CompressedByteBuffer();
    CompressedByteBuffer begins_cbb = new CompressedByteBuffer();
    CompressedByteBuffer ends_cbb = new CompressedByteBuffer();
    //IntArray documents = new IntArray();
    //IntArray termFreqCounts = new IntArray();
    //IntArray termPositions = new IntArray();
    int termDocumentCount = 0;
    int termWindowCount = 0;
    int lastDocument = 0;
    int lastCount = 0;
    
    public WindowPostingList(byte[] key) {
      this.key = key;
    }
    
    public void add(int document, int begin, int end) {
      if (termDocumentCount == 0) {
        // first instance of term
        lastDocument = document;
        lastCount = 1;
        termDocumentCount += 1;
        documents_cbb.add(document);
      } else if (lastDocument == document) {
        // additional instance of term in document
        lastCount += 1;
      } else {
        // new document
        assert lastDocument == 0 || document > lastDocument;
        documents_cbb.add(document - lastDocument);
        lastDocument = document;
        counts_cbb.add(lastCount);
        lastCount = 1;
        termDocumentCount += 1;
      }
      begins_cbb.add(begin);
      ends_cbb.add(end);
      termWindowCount += 1;
    }
  }
  // iterator allows for query processing and for streaming posting list data
  // public class Iterator extends ExtentIterator implements IndexIterator {

  public class KIterator implements KeyIterator {
    
    Iterator<byte[]> iterator;
    byte[] currKey;
    boolean done = false;
    
    public KIterator() throws IOException {
      iterator = postings.keySet().iterator();
      this.nextKey();
    }
    
    @Override
    public void reset() throws IOException {
      iterator = postings.keySet().iterator();
    }
    
    @Override
    public String getKeyString() throws IOException {
      return Utility.toString(currKey);
    }
    
    @Override
    public byte[] getKey() {
      return currKey;
    }
    
    @Override
    public boolean nextKey() throws IOException {
      if (iterator.hasNext()) {
        currKey = iterator.next();
        return true;
      } else {
        currKey = null;
        done = true;
        return false;
      }
    }
    
    @Override
    public boolean skipToKey(byte[] key) throws IOException {
      iterator = postings.tailMap(key).keySet().iterator();
      return nextKey();
    }
    
    @Override
    public boolean findKey(byte[] key) throws IOException {
      iterator = postings.tailMap(key).keySet().iterator();
      return nextKey();
    }
    
    @Override
    public String getValueString() throws IOException {
      long count = -1;
      ExtentIterator it = new ExtentIterator(postings.get(currKey));
      count = it.count();
      StringBuilder sb = new StringBuilder();
      sb.append(Utility.toString(getKey())).append(",");
      sb.append("list of size: ");
      if (count > 0) {
        sb.append(count);
      } else {
        sb.append("Unknown");
      }
      return sb.toString();
    }
    
    @Override
    public byte[] getValueBytes() throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public boolean isDone() {
      return done;
    }
    
    @Override
    public int compareTo(KeyIterator t) {
      try {
        return Utility.compare(this.getKey(), t.getKey());
      } catch (IOException ex) {
        throw new RuntimeException(ex);
      }
    }
    
    @Override
    public ValueIterator getValueIterator() throws IOException {
      if (currKey != null) {
        return new ExtentIterator(postings.get(currKey));
      } else {
        return null;
      }
    }
  }
  
  public class ExtentIterator extends ValueIterator implements AggregateIterator, MovableCountIterator, MovableExtentIterator {
    
    WindowPostingList postings;
    VByteInput documents_reader;
    VByteInput counts_reader;
    VByteInput begins_reader;
    VByteInput ends_reader;
    int iteratedDocs;
    int currDocument;
    int currCount;
    ExtentArray extents;
    boolean done;
    Map<String, Object> modifiers;
    
    private ExtentIterator(WindowPostingList postings) throws IOException {
      this.postings = postings;
      reset();
    }
    
    @Override
    public void reset() throws IOException {
      documents_reader = new VByteInput(
              new DataInputStream(
              new ByteArrayInputStream(postings.documents_cbb.getBytes())));
      counts_reader = new VByteInput(
              new DataInputStream(
              new ByteArrayInputStream(postings.counts_cbb.getBytes())));
      begins_reader = new VByteInput(
              new DataInputStream(
              new ByteArrayInputStream(postings.begins_cbb.getBytes())));
      ends_reader = new VByteInput(
              new DataInputStream(
              new ByteArrayInputStream(postings.ends_cbb.getBytes())));
      
      iteratedDocs = 0;
      currDocument = 0;
      currCount = 0;
      extents = new ExtentArray();
      
      read();
    }
    
    @Override
    public int count() {
      return currCount;
    }
    
    @Override
    public int maximumCount() {
      return Integer.MAX_VALUE;
    }
    
    @Override
    public ExtentArray extents() {
      return extents;
    }
    
    @Override
    public ExtentArray getData() {
      return extents;
    }
    
    @Override
    public boolean isDone() {
      return done;
    }
    
    @Override
    public int currentCandidate() {
      return currDocument;
    }
    
    @Override
    public boolean hasMatch(int identifier) {
      return (!isDone() && identifier == currDocument);
    }
    
    @Override
    public boolean hasAllCandidates() {
      return false;
    }
    
    private void read() throws IOException {
      if (iteratedDocs >= postings.termDocumentCount) {
        done = true;
        return;
      } else if (iteratedDocs == postings.termDocumentCount - 1) {
        currDocument = postings.lastDocument;
        currCount = postings.lastCount;
      } else {
        currDocument += documents_reader.readInt();
        currCount = counts_reader.readInt();
      }
      loadExtents();
      
      iteratedDocs++;
    }
    
    public void loadExtents() throws IOException {
      extents.reset();
      extents.setDocument(currDocument);
      int begin = 0;
      int end;
      for (int i = 0; i < currCount; i++) {
        begin += begins_reader.readInt();
        end = ends_reader.readInt();
        extents.add(begin, end);
      }
    }
    
    @Override
    public void moveTo(int identifier) throws IOException {
      // TODO implement skip lists
      
      while (!isDone() && (currDocument < identifier)) {
        read();
      }
    }
    
    @Override
    public void movePast(int identifier) throws IOException {

      while (!isDone() && (currDocument <= identifier)) {
        read();
      }
    }
    
    @Override
    public String getEntry() throws IOException {
      StringBuilder builder = new StringBuilder();
      
      builder.append(Utility.toString(postings.key));
      builder.append(",");
      builder.append(currDocument);
      for (int i = 0; i < extents.size(); ++i) {
        builder.append(",");
        builder.append(extents.begin(i));
      }
      
      return builder.toString();
    }
    
    @Override
    public long totalEntries() {
      return postings.termDocumentCount;
    }
    
    @Override
    public NodeStatistics getStatistics() {
      NodeStatistics stats = new NodeStatistics();
      stats.node = Utility.toString(postings.key);
      stats.nodeFrequency = postings.termWindowCount;
      stats.nodeDocumentCount = postings.termDocumentCount;
      stats.collectionLength = collectionPostingsCount;
      stats.documentCount = collectionDocumentCount;
      return stats;
    }
    
    @Override
    public int compareTo(MovableIterator other) {
      if (isDone() && !other.isDone()) {
        return 1;
      }
      if (other.isDone() && !isDone()) {
        return -1;
      }
      if (isDone() && other.isDone()) {
        return 0;
      }
      return currentCandidate() - other.currentCandidate();
    }
    
    @Override
    public String getKeyString() throws IOException {
      return Utility.toString(postings.key);
    }
    
    @Override
    public byte[] getKeyBytes() throws IOException {
      return postings.key;
    }
    
    @Override
    public AnnotatedNode getAnnotatedNode() throws IOException {
      String type = "extents";
      String className = this.getClass().getSimpleName();
      String parameters = this.getKeyString();
      int document = currentCandidate();
      boolean atCandidate = hasMatch(this.context.document);
      String returnValue = extents().toString();
      List<AnnotatedNode> children = Collections.EMPTY_LIST;
      
      return new AnnotatedNode(type, className, parameters, document, atCandidate, returnValue, children);
    }
  }
}
