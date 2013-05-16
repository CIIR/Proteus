/*
 *  BSD License (http://lemurproject.org/galago-license)
 */
package org.lemurproject.galago.core.index.geometric;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.lemurproject.galago.core.index.NamesReader;
import org.lemurproject.galago.core.retrieval.iterator.MovableIterator;
import org.lemurproject.galago.core.retrieval.query.AnnotatedNode;

/**
 *
 * @author sjh
 */
public class DisjointNamesIterator extends DisjointIndexesIterator implements NamesReader.Iterator {

  public DisjointNamesIterator(Collection<NamesReader.Iterator> iterators) {
    super((Collection) iterators);
  }

  @Override
  public String getCurrentName() throws IOException {
    if (head != null) {
      return ((NamesReader.Iterator) this.head).getCurrentName();
    } else {
      throw new IOException("Names Iterator is done.");
    }
  }

  @Override
  public int getCurrentIdentifier() throws IOException {
    if (head != null) {
      return ((NamesReader.Iterator) this.head).getCurrentIdentifier();
    } else {
      throw new IOException("Names Iterator is done.");
    }
  }

  @Override
  public AnnotatedNode getAnnotatedNode() throws IOException {
    String type = "counts";
    String className = this.getClass().getSimpleName();
    String parameters = this.getKeyString();
    int document = currentCandidate();
    boolean atCandidate = hasMatch(this.context.document);
    String returnValue = getCurrentName();
    List<AnnotatedNode> children = new ArrayList();
    for (MovableIterator child : this.allIterators) {
      children.add(child.getAnnotatedNode());
    }

    return new AnnotatedNode(type, className, parameters, document, atCandidate, returnValue, children);
  }
}
