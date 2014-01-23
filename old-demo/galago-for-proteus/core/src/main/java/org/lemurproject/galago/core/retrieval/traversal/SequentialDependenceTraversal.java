// BSD License (http://lemurproject.org/galago-license)
package org.lemurproject.galago.core.retrieval.traversal;

import java.util.ArrayList;
import java.util.List;
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.core.retrieval.query.MalformedQueryException;
import org.lemurproject.galago.core.retrieval.Retrieval;
import org.lemurproject.galago.core.retrieval.query.NodeParameters;
import org.lemurproject.galago.tupleflow.Parameters;

/**
 * Transforms a #sdm operator into a full expansion of the
 * sequential dependence model. That means:
 * 
 * #seqdep( #text:term1() #text:term2() ... termk ) -->
 * 
 * #weight ( 0.8 #combine ( term1 term2 ... termk)
 *           0.15 #combine ( #od(term1 term2) #od(term2 term3) ... #od(termk-1 termk) )
 *           0.05 #combine ( #uw8(term term2) ... #uw8(termk-1 termk) ) )
 *
 *
 *
 * @author irmarc
 */
public class SequentialDependenceTraversal extends Traversal {

  private int defaultWindowLimit;
  private double unigramDefault;
  private double orderedDefault;
  private double unorderedDefault;

  public SequentialDependenceTraversal(Retrieval retrieval, Parameters queryParameters) {
    Parameters parameters = retrieval.getGlobalParameters();
    unigramDefault = parameters.get("uniw", 0.8);
    orderedDefault = parameters.get("odw", 0.15);
    unorderedDefault = parameters.get("uww", 0.05);
    defaultWindowLimit = (int) parameters.get("windowLimit", 2);


    unigramDefault = parameters.get("uniw", unigramDefault);
    orderedDefault = parameters.get("odw", orderedDefault);
    unorderedDefault = parameters.get("uww", unorderedDefault);
    defaultWindowLimit = (int) parameters.get("windowLimit", defaultWindowLimit);

  }

  public static boolean isNeeded(Node root) {
    String op = root.getOperator();
    return (op.equals("sdm") || op.equals("seqdep"));
  }

  @Override
  public void beforeNode(Node original) throws Exception {
  }

  @Override
  public Node afterNode(Node original) throws Exception {
    if (original.getOperator().equals("sdm")
            || original.getOperator().equals("seqdep")) {
      // get to work

      // First check format - should only contain text node children
      List<Node> children = original.getInternalNodes();
      for (Node child : children) {
        if (child.getOperator().equals("text") == false) {
          throw new MalformedQueryException("seqdep operator needs text-only children");
        }
      }

      // formatting is ok - now reassemble
      // unigrams go as-is
      Node unigramNode = new Node("combine", Node.cloneNodeList(children));

      if (children.size() == 1) {
        return unigramNode;
      }

      // ordered and unordered can go at the same time
      ArrayList<Node> ordered = new ArrayList<Node>();
      ArrayList<Node> unordered = new ArrayList<Node>();

      NodeParameters parameters = original.getNodeParameters();
      double windowLimit = parameters.get("windowLimit", defaultWindowLimit);

      for (int n = 2; n <= windowLimit; n++) {
        for (int i = 0; i < (children.size() - n + 1); i++) {
          List<Node> seq = children.subList(i, i + n);
          ordered.add(new Node("ordered", new NodeParameters(1), Node.cloneNodeList(seq)));
          unordered.add(new Node("unordered", new NodeParameters(4 * seq.size()), Node.cloneNodeList(seq)));
        }
      }

      Node orderedWindowNode = new Node("combine", ordered);
      Node unorderedWindowNode = new Node("combine", unordered);

      // now get the weights for each component, and add to immediate children
      double uni = parameters.get("uniw", unigramDefault);
      double odw = parameters.get("odw", orderedDefault);
      double uww = parameters.get("uww", unorderedDefault);

      NodeParameters weights = new NodeParameters();
      if (parameters.containsKey("norm")) {
        weights.set("norm", parameters.getBoolean("norm"));
      }

      ArrayList<Node> immediateChildren = new ArrayList<Node>();

      // unigrams - 0.80
      weights.set("0", uni);
      immediateChildren.add(unigramNode);

      // ordered
      weights.set("1", odw);
      immediateChildren.add(orderedWindowNode);

      // unordered
      weights.set("2", uww);
      immediateChildren.add(unorderedWindowNode);

      // Finally put them all inside a combine node w/ the weights
      Node outerweight = new Node("combine", weights, immediateChildren, original.getPosition());
      return outerweight;
    } else {
      return original;
    }
  }
}
