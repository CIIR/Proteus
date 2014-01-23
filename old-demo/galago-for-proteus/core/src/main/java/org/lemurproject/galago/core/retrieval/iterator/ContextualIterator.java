// BSD License (http://lemurproject.org/galago-license)

package org.lemurproject.galago.core.retrieval.iterator;

import java.io.IOException;
import org.lemurproject.galago.core.retrieval.processing.ScoringContext;

/**
 *
 * @author irmarc
 */
public interface ContextualIterator {
  public void setContext(ScoringContext context);
  public ScoringContext getContext();
}
