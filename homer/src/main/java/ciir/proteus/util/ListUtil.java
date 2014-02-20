package ciir.proteus.util;

import java.util.ArrayList;
import java.util.List;

public class ListUtil {
  public static List drop(List input, int amt) {
    ArrayList res = new ArrayList();
    for(int i=amt; i<input.size(); i++) {
      res.add(input.get(i));
    }
    return res;
  }

  public static List take(List input, int amt) {
    ArrayList res = new ArrayList();
    for(int i=0; i<input.size()&&i<amt; i++) {
      res.add(input.get(i));
    }
    return res;
  }
}
