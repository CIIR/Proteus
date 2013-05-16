// BSD License (http://lemurproject.org/galago-license)
package org.lemurproject.galago.core.retrieval.iterator;

/**
 * 2/24/2010 (irmarc): Refactored to represent anything that
 * iterates and produces scores.
 *
 * 12/19/2010 (irmarc): This should've been done before - methods add to return
 * estimates of the maximum and minimum scores the iterator. If it doesn't know,
 * the estimates are awful (Double.MAX and Double.MIN), otherwise they're useful.
 *
 *
 * @author trevor, irmarc
 */
public interface ScoreIterator {

  /**
   * Produce a score for the iterator's current candidate given the implicit
   * context.
   * @return
   */
  public double score();

  /**
   * Estimate the maximum possible score to be produced by this iterator.
   * If a useful estimate cannot be formed, returns Double.MAX_VALUE
   */
  public double maximumScore();

  /**
   * Estimate the minimum possible score to be produced by this iterator.
   * If useful estimate cannot be formed, returns Double.MIN_VALUE
   *
   */
  public double minimumScore();
}
