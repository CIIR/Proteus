/*
 *  BSD License (http://lemurproject.org/galago-license)
 */
package org.lemurproject.galago.core.index.merge;

import java.io.IOException;
import java.util.List;
import java.util.PriorityQueue;
import org.lemurproject.galago.core.retrieval.iterator.MovableExtentIterator;
import org.lemurproject.galago.core.util.ExtentArray;
import org.lemurproject.galago.tupleflow.TupleFlowParameters;
import org.lemurproject.galago.tupleflow.Utility;

/**
 *
 * @author sjh
 */
public abstract class GenericExtentValueIndexMerger<S> extends GenericIndexMerger<S> {

  // wrapper class for ExtentValueIterators
  private class ExtentValueIteratorWrapper implements Comparable<ExtentValueIteratorWrapper> {

    int indexId;
    MovableExtentIterator iterator;
    int currentDocument;
    ExtentArray currentExtentArray;
    DocumentMappingReader mapping;
    
    private ExtentValueIteratorWrapper(int indexId, MovableExtentIterator extentIterator, DocumentMappingReader mapping) {
      this.indexId = indexId;
      this.iterator = extentIterator;
      this.mapping = mapping;

      // initialization
      load();
    }
    
    public void next() throws IOException {
      iterator.movePast(iterator.currentCandidate());
      if (!iterator.isDone()) {
        load();
      }
    }

    // changes the document numbers in the extent array
    private void load() {
      this.currentExtentArray = iterator.extents();
      this.currentDocument = mapping.map(indexId, currentExtentArray.getDocument());
      currentExtentArray.setDocument(this.currentDocument);
    }
    
    public boolean isDone() {
      return iterator.isDone();
    }
    
    public int compareTo(ExtentValueIteratorWrapper other) {
      return Utility.compare(currentDocument, other.currentDocument);
    }
  }

  // overridden functions
  public GenericExtentValueIndexMerger(TupleFlowParameters parameters) throws Exception {
    super(parameters);
  }
  
  @Override
  public void performValueMerge(byte[] key, List<KeyIteratorWrapper> keyIterators) throws IOException {
    PriorityQueue<ExtentValueIteratorWrapper> extentQueue = new PriorityQueue();
    for (KeyIteratorWrapper w : keyIterators) {
      MovableExtentIterator extentIterator = (MovableExtentIterator) w.iterator.getValueIterator();
      extentQueue.add(new ExtentValueIteratorWrapper(this.partIds.get(w), extentIterator, this.mappingReader));
    }
    
    while (!extentQueue.isEmpty()) {
      ExtentValueIteratorWrapper head = extentQueue.poll();
      transformExtentArray(key, head.currentExtentArray);
      head.next();
      if (!head.isDone()) {
        extentQueue.offer(head);
      }
    }
  }
  
  public abstract void transformExtentArray(byte[] key, ExtentArray extentArray) throws IOException;
}
