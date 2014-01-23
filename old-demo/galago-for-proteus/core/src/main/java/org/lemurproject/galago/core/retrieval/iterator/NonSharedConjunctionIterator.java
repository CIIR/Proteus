/*
 * BSD License (http://www.galagosearch.org/license)
 */
package org.lemurproject.galago.core.retrieval.iterator;

import java.io.IOException;
import org.lemurproject.galago.core.retrieval.processing.ScoringContext;
import org.lemurproject.galago.core.retrieval.query.NodeParameters;

/**
 * Similar to the ConjunctionIterator
 *  - however assumes that all descendents are not shared with any other iterators
 *  - this allows more aggressive movement.
 * 
 * @author sjh
 */
public abstract class NonSharedConjunctionIterator implements MovableIterator {

  protected MovableIterator[] iterators;
  protected MovableIterator[] drivingIterators;
  protected boolean hasAllCandidates;
  protected ScoringContext context;

  public NonSharedConjunctionIterator(NodeParameters parameters, MovableIterator[] queryIterators) {
    this.iterators = queryIterators;

    // count the number of iterators that dont have
    // a non-default data for all candidates
    int drivingIteratorCount = 0;
    for (MovableIterator iterator : this.iterators) {
      if (!iterator.hasAllCandidates()) {
        drivingIteratorCount++;
      }
    }

    if (drivingIteratorCount <= 0) {
      // if all iterators will report matches for all documents
      // make sure this information is communicated up.
      hasAllCandidates = true;
      drivingIterators = iterators;

    } else {
      // otherwise this disjunction is discriminative
      // and will not report matches for all documents
      //
      // the driving iterators will ensure this iterator
      //   does not stop at ALL documents
      hasAllCandidates = false;
      drivingIterators = new MovableIterator[drivingIteratorCount];
      int i = 0;
      for (MovableIterator iterator : this.iterators) {
        if (!iterator.hasAllCandidates()) {
          drivingIterators[i] = iterator;
          i++;
        }
      }
    }
  }

  @Override
  public void moveTo(int candidate) throws IOException {
    for (MovableIterator iterator : iterators) {
      iterator.moveTo(candidate);
    }

    int currCandidate = currentCandidate();
    while (!isDone()) {
      for (MovableIterator iterator : iterators) {
        iterator.moveTo(currCandidate);

        // if we skip too far:
        //   don't bother to move the other children
        //   we will need to pick a different candidate
        if (!iterator.hasMatch(currCandidate)) {
          break;
        }
      }

      if (hasMatch(currCandidate)) {
        return;
      }
      currCandidate = Math.max(currCandidate + 1, currentCandidate());
    }
  }

  @Override
  public void movePast(int candidate) throws IOException {
    this.moveTo(candidate + 1);
  }

  @Override
  public int currentCandidate() {
    int candidateMax = Integer.MIN_VALUE;
    int candidateMin = Integer.MAX_VALUE;
    for (MovableIterator iterator : drivingIterators) {
      if (iterator.isDone()) {
        return Integer.MAX_VALUE;
      }
      candidateMax = Math.max(candidateMax, iterator.currentCandidate());
      candidateMin = Math.min(candidateMin, iterator.currentCandidate());
    }
    if (candidateMax == candidateMin) {
      return candidateMax;
    } else {
      return candidateMax - 1;
    }
  }

  @Override
  public boolean hasMatch(int candidate) {
    for (MovableIterator iterator : drivingIterators) {
      if (iterator.isDone() || !iterator.hasMatch(candidate)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean isDone() {
    for (MovableIterator iterator : drivingIterators) {
      if (iterator.isDone()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void reset() throws IOException {
    for (MovableIterator iterator : iterators) {
      iterator.reset();
    }
  }

  @Override
  public boolean hasAllCandidates() {
    return hasAllCandidates;
  }

  @Override
  public long totalEntries() {
    long min = Integer.MAX_VALUE;
    for (MovableIterator iterator : iterators) {
      min = Math.min(min, iterator.totalEntries());
    }
    return min;
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
    return this.currentCandidate() - other.currentCandidate();
  }

  @Override
  public void setContext(ScoringContext context) {
    this.context = context;
  }

  @Override
  public ScoringContext getContext() {
    return context;
  }
}
