package ciir.proteus.util;

import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.core.retrieval.traversal.SetFieldTraversal;
import org.lemurproject.galago.core.retrieval.traversal.Traversal;
import org.lemurproject.galago.tupleflow.Utility;
import org.lemurproject.galago.utility.Parameters;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jfoley, michaelz
 *         Oct-18-2016  MCZ Modified so query terms are returned in order.
 */
public class QueryUtil {
  /**
   * Quick, inline traversal to return all the text nodes in a Galago Query.
   *
   * @param query parsed or transformed Galago query
   * @return a set of terms as a list
   */
  public static List<String> queryTerms(Node query) {
    ArrayList<String> terms = new ArrayList<>();

    walk(query, Parameters.create(), new QueryTermTraversal(terms));

    return terms;
  }

  /**
   * Walk over a query and set the preferred field accordingly.
   *
   * @param inputQuery  a pre-transformed query
   * @param targetField the name of the field to send the query against
   */
  public static void setField(Node inputQuery, final String targetField) {
    walk(inputQuery, Parameters.parseArray("field", targetField), new SetFieldTraversal());
  }

  /**
   * Some heuristics for finding "text" nodes.
   *
   * @param node a Galago Node
   * @return true if node is terminal and operator matches
   */
  public static boolean isTextNode(Node node) {
    return node.isText();
  }

  /**
   * Hide exception catching and Galago traversal API into this function.
   *
   * @param node      the query
   * @param qp        any parameters you want to pass
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
   */
  public static class QueryTermTraversal extends Traversal {
    boolean ordered = false;
    boolean extents = false;
    ArrayList<String> terms = new ArrayList<>();
    ArrayList<String> orderedWindowTerms = new ArrayList<>();

    QueryTermTraversal(ArrayList<String> terms) {
      this.terms = terms;
    }

    @Override
    public void beforeNode(Node original, Parameters queryParameters) throws Exception {

    }

    @Override
    public Node afterNode(Node original, Parameters queryParameters) throws Exception {

      // check if we're at a leaf node
      if (original.numChildren() > 0) {
        // add any ordered window terms
        if (orderedWindowTerms.size() > 0) {
          terms.add(Utility.join(orderedWindowTerms, " "));
          orderedWindowTerms = new ArrayList<>();
        }
        return original;
      }

      ordered = original.getParent().getOperator().equals("ordered");
      extents = original.getNodeParameters().get("part", "??").equals("extents");

      if (!isTextNode(original) || extents) {
        return original;
      }

      if (ordered) {
        orderedWindowTerms.add(original.getNodeParameters().getString("default"));
      } else {
        terms.add(original.getNodeParameters().getString("default"));
      }

      return original;
    }

  }
}
