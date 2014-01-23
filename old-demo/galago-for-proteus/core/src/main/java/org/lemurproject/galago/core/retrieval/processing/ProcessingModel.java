// BSD License (http://lemurproject.org/galago-license)
package org.lemurproject.galago.core.retrieval.processing;

import java.io.IOException;
import org.lemurproject.galago.core.index.Index;
import org.lemurproject.galago.core.retrieval.ScoredDocument;
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.tupleflow.Parameters;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import org.lemurproject.galago.core.index.LengthsReader;
import org.lemurproject.galago.core.index.disk.FieldLengthsReader;
import org.lemurproject.galago.core.index.disk.WindowIndexReader;
import org.lemurproject.galago.core.retrieval.LocalRetrieval;
import org.lemurproject.galago.core.retrieval.query.NodeParameters;
import org.lemurproject.galago.core.retrieval.query.QueryType;

/**
 * An interface that defines the contract for processing a query.
 * There's one method : execute, which takes a fully annotated query
 * tree, and somehow produces a result list.
 *
 *
 * @author irmarc
 */
public abstract class ProcessingModel {

  public abstract ScoredDocument[] execute(Node queryTree, Parameters queryParams) throws Exception;

  public abstract void defineWorkingSet(int[] docs);

  public static <T extends ScoredDocument> T[] toReversedArray(PriorityQueue<T> queue) {
    if (queue.size() == 0) {
      return null;
    }

    T[] items = (T[]) Array.newInstance(queue.peek().getClass(), queue.size());
    for (int i = queue.size() - 1; queue.isEmpty() == false; i--) {
      items[i] = queue.poll();
    }
    return items;
  }

  public static void initializeLengths(LocalRetrieval r, ScoringContext ctx) throws IOException {

    Parameters global = r.getGlobalParameters();
    List<String> fields;
    if (global.containsKey("fields")) {
      fields = global.getAsList("fields");
    } else {
      fields = new ArrayList<String>();
    }

    Node docLengths = new Node("lengths", new NodeParameters());
    docLengths.getNodeParameters().set("default", "document");
    docLengths.getNodeParameters().set("mode", global.get("lenMode", "memory"));
    
    Index index = r.getIndex();
    LengthsReader.Iterator documentLengths = (LengthsReader.Iterator) index.getIterator(docLengths);
    
    //LengthsReader.Iterator documentLengths = index.getLengthsIterator();
    
    
    ctx.addLength("", documentLengths);
    if (index.containsPart("extents") && !fields.isEmpty()) {
      WindowIndexReader wir = (WindowIndexReader) index.getIndexPart("extents");
      FieldLengthsReader flr = new FieldLengthsReader(wir);
      Parameters parts = r.getAvailableParts();
      for (String field : fields) {
        String partName = "field." + field;
        if (!parts.containsKey(partName)) {
          continue;
        }
        LengthsReader.Iterator it = flr.getLengthsIterator(field);
        ctx.addLength(field, it);
      }
    }
  }

public static ProcessingModel instance(LocalRetrieval r, Node root, Parameters p)
    throws Exception {
    QueryType qt = r.getQueryType(root);
    if (qt == QueryType.BOOLEAN) {
    } else if (qt == QueryType.RANKED) {
      if (p.containsKey("passageSize") || p.containsKey("passageShift")) {
        return new RankedPassageModel(r);
      } else {
        if (p.get("deltaReady", false)) {
          return new DeltaScoreDocumentModel(r);
        } else {
          return new RankedDocumentModel(r);
        }
      }
    }
    throw new RuntimeException(String.format("Unable to determine processing model for %s",
            root.toString()));
  }
}
