/*
 *  BSD License (http://lemurproject.org/galago-license)
 */
package org.lemurproject.galago.core.parse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.lemurproject.galago.tupleflow.Parameters;
import org.lemurproject.galago.tupleflow.TypeReader;
import org.lemurproject.galago.tupleflow.types.SerializedParameters;

/**
 *
 * Used to accumulate parameters that contain statistics data
 *  - written to combine the documentCounter output data.
 *
 * @author sjh
 */
public class NumericParameterAccumulator {

  public static Parameters accumulateParameters(TypeReader<SerializedParameters> statsReader) throws IOException {
    SerializedParameters serial;
    List<Parameters> params = new ArrayList();
    while ((serial = statsReader.read()) != null) {
      Parameters fragment = Parameters.parse(serial.parameters);
      params.add(fragment);
    }
    return accumulateParameters(params);
  }

  public static Parameters accumulateParameters(List<Parameters> params) {
    Parameters accumulation = new Parameters();
    for(Parameters p : params){
      for(String key : p.getKeys()){
        if(p.isLong(key)){
          accumulation.set(key, accumulation.get(key, 0L) + p.getLong(key));
        } else if (p.isDouble(key)){
          accumulation.set(key, accumulation.get(key, 0D) + p.getDouble(key));
        } else if (p.isMap(key)){
          Parameters[] mapPair = new Parameters[2];
          mapPair[0] = p.getMap(key);
          mapPair[1] = (accumulation.isMap(key)) ? accumulation.getMap(key) : new Parameters();
          accumulation.set(key, accumulateParameters(Arrays.asList(mapPair)));
        } else {
          throw new RuntimeException("Failed to accumulate parameters: key " + key + " is not numeric, nor a Map object");
        }
      }      
    }
    return accumulation;
  }
}
