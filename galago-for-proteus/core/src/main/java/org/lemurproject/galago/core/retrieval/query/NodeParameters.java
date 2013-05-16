/*
 *  BSD License (http://lemurproject.org/galago-license)
 */
package org.lemurproject.galago.core.retrieval.query;

import gnu.trove.map.hash.TObjectByteHashMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import gnu.trove.map.hash.TObjectLongHashMap;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import org.lemurproject.galago.tupleflow.Parameters.Type;

/**
 * Currently the parameters that are attached to query Nodes are not quite the same
 * implementation as the generic Parameters object. We intend to fold these classes together
 * at some point in the future, most likely making this one a subclass of the Parameters object.
 * 
 * For now, however, they are separate.
 *
 *
 * @author sjh
 */
public class NodeParameters implements Serializable {

  private HashMap<String, Type> keyMapping = new HashMap();
  private HashMap<String, String> stringMap = null;
  private TObjectByteHashMap<String> boolMap = null;
  private TObjectLongHashMap<String> longMap = null;
  private TObjectDoubleHashMap<String> doubleMap = null;

  public NodeParameters() {
    // do nothing
  }

  public NodeParameters(boolean def) {
    set("default", def);
  }

  public NodeParameters(long def) {
    set("default", def);
  }

  public NodeParameters(double def) {
    set("default", def);
  }

  public NodeParameters(String def) {
    set("default", def);
  }

  public Set<String> getKeySet() {
    return keyMapping.keySet();
  }

  public Type getKeyType(String key) {
    return this.keyMapping.get(key);
  }

  public boolean containsKey(String key) {
    return this.keyMapping.containsKey(key);
  }

  public void set(String key, boolean value) {
    ensureKeyType(key, Type.BOOLEAN);
    if (boolMap == null) {
      boolMap = new TObjectByteHashMap();
    }
    boolMap.put(key, (value ? (byte) 1 : (byte) 0));
  }

  public void set(String key, long value) {
    ensureKeyType(key, Type.LONG);
    if (longMap == null) {
      longMap = new TObjectLongHashMap();
    }
    longMap.put(key, value);
  }

  public void set(String key, double value) {
    ensureKeyType(key, Type.DOUBLE);
    if (doubleMap == null) {
      doubleMap = new TObjectDoubleHashMap();
    }
    doubleMap.put(key, value);
  }

  public void set(String key, String value) {
    ensureKeyType(key, Type.STRING);
    if (stringMap == null) {
      stringMap = new HashMap();
    }
    stringMap.put(key, value);
  }

  public boolean getBoolean(String key) {
    checkKeyType(key, Type.BOOLEAN);
    return (boolMap.get(key) != 0);
  }

  public long getLong(String key) {
    checkKeyType(key, Type.LONG);
    return longMap.get(key);
  }

  public double getDouble(String key) {
    // special case - allow longs to be cast to doubles.
    if (keyMapping.containsKey(key)) {
      if (keyMapping.get(key).equals(Type.LONG)) {
        return getLong(key);
      }
    }
    checkKeyType(key, Type.DOUBLE);
    return doubleMap.get(key);
  }

  public String getString(String key) {
    checkKeyType(key, Type.STRING);
    return stringMap.get(key);
  }

  public boolean get(String key, boolean def) {
    if (keyMapping.containsKey(key)) {
      return getBoolean(key);
    } else {
      return def;
    }
  }

  public long get(String key, long def) {
    if (keyMapping.containsKey(key)) {
      return getLong(key);
    } else {
      return def;
    }
  }

  public double get(String key, double def) {
    if (keyMapping.containsKey(key)) {
      return getDouble(key);
    } else {
      return def;
    }
  }

  public String get(String key, String def) {
    if (keyMapping.containsKey(key)) {
      return getString(key);
    } else {
      return def;
    }
  }

  /**
   * Special get function - does not throw exceptions for missing keys.
   */
  public String getAsString(String key) {
    // assert keyMapping.containsKey(key) : "Key " + key + " not found in NodeParameters.";
    if (keyMapping.containsKey(key)) {
      switch (keyMapping.get(key)) {
        case BOOLEAN:
          return Boolean.toString(boolMap.get(key) != 0);
        case LONG:
          return Long.toString(longMap.get(key));
        case DOUBLE:
          return Double.toString(doubleMap.get(key));
        case STRING:
          return stringMap.get(key);
      }
    }
    return null;
  }

  public void remove(String key) {
    if (keyMapping.containsKey(key)) {
      switch (keyMapping.get(key)) {
        case BOOLEAN:
          boolMap.remove(key);
          break;
        case LONG:
          longMap.remove(key);
          break;
        case DOUBLE:
          doubleMap.remove(key);
          break;
        case STRING:
          stringMap.remove(key);
          break;
        default:
          throw new IllegalArgumentException("Key somehow has an illegal type: " + keyMapping.get(key));
      }
      keyMapping.remove(key);
    }
  }

  @Override
  public NodeParameters clone() {
    NodeParameters duplicate = new NodeParameters();
    if (keyMapping != null) {
      duplicate.keyMapping = (HashMap<String, Type>) this.keyMapping.clone();
    }
    if (boolMap != null) {
      duplicate.boolMap = new TObjectByteHashMap(boolMap);
    }
    if (longMap != null) {
      duplicate.longMap = new TObjectLongHashMap(longMap);
    }
    if (doubleMap != null) {
      duplicate.doubleMap = new TObjectDoubleHashMap(doubleMap);
    }
    if (stringMap != null) {
      duplicate.stringMap = (HashMap<String, String>) this.stringMap.clone();
    }
    return duplicate;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    // ensure default is the first value
    if (keyMapping.containsKey("default")) {
      String value = getAsString("default");
      value = escapeAsNecessary(value, keyMapping.get("default") == Type.STRING);
      sb.append(":").append(value);
    }

    // sort remaining keys alphabetically.
    ArrayList<String> keys = new ArrayList(keyMapping.keySet());
    Collections.sort(keys);
    for (String key : keys) {
      // need to ensure "default" is not double written.
      if (!key.equals("default")) {
        String value = getAsString(key);
        value = escapeAsNecessary(value, keyMapping.get(key) == Type.STRING);
        key = escapeAsNecessary(key, false);
        sb.append(":").append(key).append("=").append(value);
      }
    }
    return sb.toString();
  }

  public void parseSet(String key, String value) {

    // decode value:
    // boolean: true | false
    if (value.equals("true") || value.equals("false")) {
      boolean b = Boolean.parseBoolean(value);
      this.set(key, b);
    } else {
      try {
        // if the value contains a . -- could be double
        if (value.contains(".")) {
          double d = Double.parseDouble(value);
          this.set(key, d);
        } else {
          // otherwise could be a long
          long l = Long.parseLong(value);
          this.set(key, l);
        }
      } catch (NumberFormatException e) {
        // if it's not numeric or boolean - it's a string
        this.set(key, value);
      }
    }
  }

  private void ensureKeyType(String key, Type t) {
    checkKeyType(key, t);
    keyMapping.put(key, t);
  }

  private void checkKeyType(String key, Type t) {
    if (keyMapping.containsKey(key)) {
      if (keyMapping.get(key) != t) {
        throw new IllegalArgumentException("Key " + key + " exists as a " + keyMapping.get(key).name());
      }
    }
  }

  // escaping functions - used in toString()
  public boolean needsToBeEscaped(String text, boolean typeProtection) {
    // if the text is a string -- we may need to quote it:
    if (typeProtection) {
      if (text.equals(Boolean.toString(true))
              || text.equals(Boolean.toString(false))) {
        return true;
      }
      try {
        Double.parseDouble(text);
        return true;
      } catch (Exception e) {
      }
    }
    // A parameter value needs to be escaped if it contains: ':' '=' '('
    return (text.contains(":") || text.contains("=")
            || text.contains("(") || text.contains("@")
            || text.contains("\"") || text.contains("'"));
  }

  public String escapeAsNecessary(String text, boolean typeProtection) {
    if (!needsToBeEscaped(text, typeProtection)) {
      return text;
    } else {
      String[] preferredDelimiters = {"/", "|", "\\", "#", "!", "%", "~", "&", "^", "%"};

      for (String delimiter : preferredDelimiters) {
        if (!text.contains(delimiter)) {
          return "@" + delimiter + text + delimiter;
        }
      }

      // give up
      return text;
    }
  }

  public boolean isString(String key) {
    return this.keyMapping.containsKey(key) && this.keyMapping.get(key) == Type.STRING;
  }

  public boolean isBoolean(String key) {
    return this.keyMapping.containsKey(key) && this.keyMapping.get(key) == Type.BOOLEAN;
  }

  public boolean isDouble(String key) {
    return this.keyMapping.containsKey(key) && this.keyMapping.get(key) == Type.DOUBLE;
  }

  public boolean isLong(String key) {
    return this.keyMapping.containsKey(key) && this.keyMapping.get(key) == Type.LONG;
  }
}
