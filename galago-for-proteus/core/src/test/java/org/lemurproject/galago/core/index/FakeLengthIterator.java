/*
 * BSD License (http://www.galagosearch.org/license)

 */
package org.lemurproject.galago.core.index;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.lemurproject.galago.core.retrieval.iterator.MovableIterator;
import org.lemurproject.galago.core.retrieval.processing.ScoringContext;
import org.lemurproject.galago.core.retrieval.query.AnnotatedNode;

/**
 *
 * @author marc
 */
public class FakeLengthIterator implements LengthsReader.Iterator {

  private int[] ids;
  private int[] lengths;
  private int position;
  private ScoringContext context;

  public FakeLengthIterator(int[] i, int[] l) {
    ids = i;
    lengths = l;
    position = 0;
  }

  @Override
  public int getCurrentLength() {
    return lengths[position];
  }

  @Override
  public int getCurrentIdentifier() {
    return ids[position];
  }

  @Override
  public int currentCandidate() {
    return ids[position];
  }

  @Override
  public boolean hasMatch(int identifier) {
    return (ids[position] == identifier);
  }

  @Override
  public boolean hasAllCandidates() {
    return true;
  }

  @Override
  public void movePast(int identifier) throws IOException {
    moveTo(identifier + 1);
  }

  @Override
  public void moveTo(int identifier) throws IOException {
    while (!isDone() && ids[position] < identifier) {
      position++;
    }
  }

  @Override
  public void reset() throws IOException {
    position = 0;
  }

  @Override
  public boolean isDone() {
    return (position >= ids.length);
  }

  @Override
  public String getEntry() throws IOException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public long totalEntries() {
    return ids.length;
  }

  @Override
  public int compareTo(MovableIterator t) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void setContext(ScoringContext context) {
    this.context = context;
  }

  @Override
  public ScoringContext getContext() {
    return this.context;
  }

  @Override
  public AnnotatedNode getAnnotatedNode() {
    String type = "length";
    String className = this.getClass().getSimpleName();
    String parameters = "";
    int document = currentCandidate();
    boolean atCandidate = hasMatch(this.context.document);
    String returnValue = Integer.toString(getCurrentLength());
    List<AnnotatedNode> children = Collections.EMPTY_LIST;

    return new AnnotatedNode(type, className, parameters, document, atCandidate, returnValue, children);
  }
}
