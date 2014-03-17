package ciir.proteus.util;

import org.lemurproject.galago.tupleflow.Parameters;
import org.lemurproject.galago.tupleflow.json.JSONUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by jfoley on 2/20/14.
 */
public class HTTPUtil {

  public static Parameters fromHTTPRequest(HttpServletRequest req) {
    Parameters reqp = new Parameters();

    Map<String, String[]> asMap = (Map <String,String[]>) req.getParameterMap();

    for(Map.Entry<String,String[]> kv : asMap.entrySet()) {
      String arg = kv.getKey();
      String[] values = kv.getValue();

      if(values.length == 1) {
        reqp.put(arg, JSONUtil.parseString(values[0]));
      } else {
        reqp.set(arg, new ArrayList());
        for(String val : values) {
          reqp.getAsList(val).add(JSONUtil.parseString(val));
        }
      }
    }

    return reqp;
  }
}
