package ciir.proteus.util;

import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.core.retrieval.traversal.SetFieldTraversal;
import org.lemurproject.galago.core.retrieval.traversal.Traversal;
import org.lemurproject.galago.utility.Parameters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * @author jfoley.
 */
public class QueryUtil {
  /**
   * Quick, inline traversal to return all the text nodes in a Galago Query.
   * @param query parsed or transformed Galago query
   * @return a set of terms as a list
   */
  public static List<String> queryTerms(Node query) {
    final HashSet<String> terms = new HashSet<>();

    walk(query, Parameters.create(), new BeforeTraversal() {
      @Override
      public void beforeNode(Node original, Parameters queryParameters) throws Exception {
        if(isTextNode(original)) {
          terms.add(original.getNodeParameters().getString("default"));
        }
      }
    });

    return new ArrayList<>(terms);
  }

  /**
   * Walk over a query and set the preferred field accordingly.
   * @param inputQuery a pre-transformed query
   * @param targetField the name of the field to send the query against
   */
  public static void setField(Node inputQuery, final String targetField) {
    walk(inputQuery, Parameters.parseArray("field", targetField), new SetFieldTraversal());
  }

  /**
   * Some heuristics for finding "text" nodes.
   * @param node a Galago Node
   * @return true if node is terminal and operator matches
   */
  public static boolean isTextNode(Node node) {
    return node.isText();
  }

  /**
   * Hide exception catching and Galago traversal API into this function.
   * @param node the query
   * @param qp any parameters you want to pass
   * @param traversal the traversal object / function
   * @return the modified node, if that's how you want to roll
   */
  public static Node walk(Node node, Parameters qp, Traversal traversal) {
    try {
      return traversal.traverse(node, qp);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Neat abstract class to make inline syntax more fun; default behavior for afterNode implemented.
   *
   * TODO: move this into Galago core someday.
   */
  public static abstract class BeforeTraversal extends Traversal {
    @Override
    public Node afterNode(Node original, Parameters queryParameters) throws Exception { return original; }
  }
}
