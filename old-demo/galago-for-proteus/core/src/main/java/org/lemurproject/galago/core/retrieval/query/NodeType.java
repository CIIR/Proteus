// BSD License (http://lemurproject.org/galago-license)
package org.lemurproject.galago.core.retrieval.query;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import org.lemurproject.galago.core.retrieval.iterator.StructuredIterator;
import org.lemurproject.galago.tupleflow.Parameters;

/**
 * <p>A NodeType describes the class type and input types of an iterator.</p>
 * 
 * <p>Traversals that modify a tree may want to know what type of iterator will be generated
 * when a Node is converted into a StructuredIterator.  For instance, a Node with a
 * "counts" operator will turn into a TermCountIterator.  This is important to know because
 * a ScoreCombinationIterator can't take a TermCountIterator as an argument; it needs an
 * iterator between them to convert extents into scores.  A Traversal can check the types
 * of "counts" and "combine", notice the type mismatch, and add a "#feature" node between
 * them so that the types match. This is important because raw counts have to be transformed into
 * a score space, i.e. (# term occurrences in doc D) -->  (#term occurrences) / (|D|) </p>
 *
 *
 * @author trevor, irmarc
 */
public class NodeType implements Serializable {

  private Class<? extends StructuredIterator> nodeClass;

  public NodeType(Class<? extends StructuredIterator> nodeClass) {
    this.nodeClass = nodeClass;
  }

  public Class<? extends StructuredIterator> getIteratorClass() {
    return nodeClass;
  }

  public Class[] getInputs() throws Exception {
    Constructor constructor = null;
    try {
      constructor = getConstructor();
    } catch (Exception e) {
      return new Class[0];
    }
    return constructor.getParameterTypes();
  }

  public Class[] getParameterTypes(int length) throws Exception {
    Class[] inputs = getInputs();
    if (inputs == null) {
      return new Class[0];
    }
    if (inputs.length == 0) {
      return new Class[0];
    }

    int paramCount = 0;
    if(inputs[paramCount].isAssignableFrom(Parameters.class)){
      paramCount++;
    }
    if(inputs[paramCount].isAssignableFrom(NodeParameters.class)){
      paramCount++;
    }
    if(paramCount > 0){
      Class[] newInputs = new Class[inputs.length - paramCount];
      System.arraycopy(inputs, paramCount, newInputs, 0, newInputs.length);
      inputs = newInputs;
    }
    
    if (inputs[inputs.length - 1].isArray()) {
      if (length < inputs.length - 1) {
        // Not enough parameters.
        return null;
      } else {
        Class[] result = new Class[length];
        // Copy in classes for the first few parameters.
        for (int i = 0; i < inputs.length - 1; ++i) {
          result[i] = inputs[i];
        }
        // Apply the array class type to the remaining slots.
        for (int i = inputs.length - 1; i < result.length; ++i) {
          result[i] = inputs[inputs.length - 1].getComponentType();
        }
        return result;
      }
    } else {
      if (length != inputs.length) {
        return null;
      } else {
        return inputs;
      }
    }
  }

  public boolean isStructuredIteratorOrArray(Class c) {
    if (c.isArray() && StructuredIterator.class.isAssignableFrom(c.getComponentType())) {
      return true;
    }
    if (StructuredIterator.class.isAssignableFrom(c)) {
      return true;
    }
    return false;
  }

  public Constructor getConstructor() throws Exception {
    for (Constructor constructor : nodeClass.getConstructors()) {
      Class[] types = constructor.getParameterTypes();

      // The constructor needs at least one parameter.
      if (types.length < 1) {
        continue;
      }
      
      int pointer = 0;
      // The first class may be Params
      if(Parameters.class.isAssignableFrom(types[pointer])){
        pointer++;
      }
      // The first/second class needs to be Parameters.
      if (!NodeParameters.class.isAssignableFrom(types[pointer])) {
        continue;
      }
      pointer++;
      // Check arguments for valid argument types.
      boolean validTypes = true;
      for (int i = pointer; i < types.length; ++i) {
        if (!isStructuredIteratorOrArray(types[i])) {
          validTypes = false;
          break;
        }
      }
      // If everything looks good, return this constructor.
      if (validTypes) {
        return constructor;
      }
    }

    throw new Exception("No reasonable constructors were found for " + nodeClass.toString());
  }

  public String toString() {
    return nodeClass.getName();
  }
}
