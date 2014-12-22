package ciir.proteus.util;

import java.util.ArrayList;
import java.util.List;

public class ListUtil {
  /**
   * Clone list after skipping a number of values...
   * @param input the list to clone
   * @param amt the number of values to skip
   * @param <T> the type of the list
   * @return the new list
   */
  public static <T> List<T> drop(List<T> input, int amt) {
    ArrayList<T> res = new ArrayList<>();
    for(int i=amt; i<input.size(); i++) {
      res.add(input.get(i));
    }
    return res;
  }

  /**
   * Clone list of length amt...
   * @param input the list to clone
   * @param amt the number of values to take
   * @param <T> the type of the list
   * @return the new list
   */
  public static <T> List<T> take(List<T> input, int amt) {
    ArrayList<T> res = new ArrayList<>();
    for(int i=0; i<input.size()&&i<amt; i++) {
      res.add(input.get(i));
    }
    return res;
  }

  public static <T> List<T> slice(List<T> input, int start, int end) {
    ArrayList<T> res = new ArrayList<>();
    for(int i=start; i<input.size()&&i<end; i++) {
      res.add(input.get(i));
    }
    return res;
  }
}
