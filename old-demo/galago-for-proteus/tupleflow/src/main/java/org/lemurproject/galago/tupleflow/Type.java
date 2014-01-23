// BSD License (http://lemurproject.org)

package org.lemurproject.galago.tupleflow;

/**
 *
 * @author trevor
 */
public interface Type<T> {
    public Order<T> getOrder(String... fields);
}
