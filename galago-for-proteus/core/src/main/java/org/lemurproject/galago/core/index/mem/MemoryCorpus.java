// BSD License (http://lemurproject.org/galago-license)
package org.lemurproject.galago.core.index.mem;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import org.lemurproject.galago.core.index.KeyIterator;
import org.lemurproject.galago.core.index.ValueIterator;
import org.lemurproject.galago.core.index.corpus.CorpusFileWriter;
import org.lemurproject.galago.core.index.corpus.DocumentReader;
import org.lemurproject.galago.core.index.corpus.DocumentReader.DocumentIterator;
import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.retrieval.iterator.DataIterator;
import org.lemurproject.galago.core.retrieval.iterator.MovableIterator;
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.core.retrieval.query.NodeType;
import org.lemurproject.galago.tupleflow.DataStream;
import org.lemurproject.galago.tupleflow.FakeParameters;
import org.lemurproject.galago.tupleflow.Parameters;
import org.lemurproject.galago.tupleflow.Utility;
import org.lemurproject.galago.tupleflow.Utility.ByteArrComparator;

public class MemoryCorpus implements DocumentReader, MemoryIndexPart {

  private TreeMap<byte[], Document> corpusData;
  private Parameters params;
  private long docCount;
  private long termCount;

  public MemoryCorpus(Parameters params) throws IOException {
    this.params = params;
    this.corpusData = new TreeMap(new ByteArrComparator());
  }

  public void addDocument(Document doc) {

    docCount += 1;
    termCount += doc.terms.size();

    // save a subset of the document 
    // - to match the output of themake-corpus function.
    corpusData.put(Utility.fromInt(doc.identifier), doc);
  }

  // this is likely to waste all of your memory...
  @Override
  public void addIteratorData(byte[] key, MovableIterator iterator) throws IOException {
    while (!iterator.isDone()) {
      Document doc = ((DataIterator<Document>) iterator).getData();
      // if the document already exists - no harm done.
      addDocument(doc);
      iterator.movePast(iterator.currentCandidate());
    }
  }

  @Override
  public void removeIteratorData(byte[] key) throws IOException {
    throw new IOException("Can not remove Document Names iterator data");
  }

  @Override
  public void close() throws IOException {
    // clean up data.
    corpusData = null;
  }

  @Override
  public KeyIterator getIterator() throws IOException {
    return new MemDocIterator(corpusData.keySet().iterator());
  }

  @Override
  public Document getDocument(int key, Parameters p) throws IOException {
    return corpusData.get(Utility.fromInt(key));
  }

  @Override
  public Parameters getManifest() {
    return params;
  }

  @Override
  public long getDocumentCount() {
    return docCount;
  }

  @Override
  public long getCollectionLength() {
    return termCount;
  }

  @Override
  public long getKeyCount() {
    return this.corpusData.size();
  }

  @Override
  public void flushToDisk(String path) throws IOException {
    Parameters p = getManifest();
    p.set("filename", path);
    CorpusFileWriter writer = new CorpusFileWriter(new FakeParameters(p));
    DocumentIterator iterator = (DocumentIterator) getIterator();
    while (!iterator.isDone()) {
      writer.process(iterator.getDocument(new Parameters()));
      iterator.nextKey();
    }
    writer.close();
  }

  // unsupported functions - perhaps soon they will be supported.
  @Override
  public ValueIterator getIterator(Node node) throws IOException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public ValueIterator getIterator(byte[] nodeString) throws IOException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public String getDefaultOperator() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Map<String, NodeType> getNodeTypes() {
    return new HashMap();
  }

  // document iterator
  private class MemDocIterator implements DocumentIterator {

    private Iterator<byte[]> keyIterator;
    private byte[] currKey;

    public MemDocIterator(Iterator<byte[]> iterator) throws IOException {
      this.keyIterator = iterator;
      nextKey();
    }

    public boolean skipToKey(byte[] key) throws IOException {
      keyIterator = corpusData.tailMap(key).keySet().iterator();
      nextKey();
      return (Utility.compare(key, currKey) == 0);
    }

    public boolean findKey(byte[] key) throws IOException {
      keyIterator = corpusData.tailMap(key).keySet().iterator();
      nextKey();
      return (Utility.compare(key, currKey) == 0);
    }

    public String getKeyString() {
      return Integer.toString(Utility.toInt(currKey));
    }

    public byte[] getKey() {
      return currKey;
    }

    public boolean isDone() {
      return currKey == null;
    }

    public Document getDocument(Parameters p) throws IOException {
      return corpusData.get(currKey);
    }

    public boolean nextKey() throws IOException {
      if (keyIterator.hasNext()) {
        currKey = keyIterator.next();
        return true;
      } else {
        currKey = null;
        return false;
      }
    }

    public String getValueString() throws IOException {
      return getDocument(new Parameters()).toString();
    }

    public void reset() throws IOException {
      keyIterator = corpusData.keySet().iterator();
      nextKey();
    }

    public int compareTo(KeyIterator t) {
      try {
        return Utility.compare(this.getKey(), t.getKey());
      } catch (IOException ex) {
        throw new RuntimeException("Failed to compare mem-corpus keys");
      }
    }

    // unsupported functions:
    public ValueIterator getValueIterator() throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    public byte[] getValueBytes() throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    public DataStream getValueStream() throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
    }
  }
}
