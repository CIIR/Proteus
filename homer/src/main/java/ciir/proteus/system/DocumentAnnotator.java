package ciir.proteus.system;

import ciir.proteus.users.error.DBError;
import ciir.proteus.util.ListUtil;
import ciir.proteus.util.RetrievalUtil;
import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.parse.Tag;
import org.lemurproject.galago.core.retrieval.ScoredDocument;
import org.lemurproject.galago.core.retrieval.ScoredPassage;
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.tupleflow.execution.SSHStageExecutor;
import org.lemurproject.galago.utility.Parameters;
import org.lemurproject.galago.tupleflow.Utility;

import java.sql.ParameterMetaData;
import java.util.*;

/**
 * @author jfoley, michaelz
 */
public class DocumentAnnotator {

  public static List<Parameters> annotate(ProteusSystem system, String kind, List<String> names, Parameters reqp) throws DBError {
    reqp.put("metadata", false);
    List<ScoredDocument> fakeDocs = new ArrayList<>();
    for (String id : names) {
      fakeDocs.add(new ScoredDocument(id, 0, 0.0));
    }
    return annotate(system, kind, fakeDocs, null, reqp);
  }

  public static List<Parameters> annotate(ProteusSystem system, String kind, List<ScoredDocument> results, Node query, Parameters reqp) throws DBError {
    boolean snippets = reqp.get("snippets", true);
    boolean metadata = reqp.get("metadata", true);
    boolean tags = reqp.get("tags", reqp.isString("user"));
    int numEntities = (int) reqp.get("top_k_entities", 0);

    List<String> names = RetrievalUtil.names(results);

    // retrieve snippets if requested
    if (snippets) {
      results = system.findPassages(kind, query, names);
    }

    // if we need to pull the documents:
    Map<String, Document> pulled = Collections.emptyMap();
    if (snippets || metadata) {
      pulled = system.getDocs(kind, names, metadata, snippets);
    }

    // if we need to get tags for these documents:
    Map<String, Map<Integer, Map<String, String>>> docTags = null;
    if (tags) {
      docTags = system.userdb.getAllTags(RetrievalUtil.names(results));
    }

    // result data
    ArrayList<Parameters> resultData = new ArrayList<>(results.size());
    for (ScoredDocument sdoc : results) {
      Document doc = pulled.get(sdoc.documentName);

      if (doc == null) {
        continue;
      }
      Parameters docp = Parameters.create();

      // if we want the "top K" entities returned, get them...
      if (numEntities > 0) {
        ArrayList<Parameters> entList = sortEntities(numEntities, doc);
        docp.put("entities", entList);
      } // end if we want to get entities

      // default annotations
      docp.put("name", sdoc.documentName);
      docp.put("rank", sdoc.rank);
      docp.put("score", sdoc.score);

      // metadata annotation
      if (metadata) {
        docp.put("meta", Parameters.parseMap(doc.metadata));
      }
      // snippet annotation
      if (snippets) {
        ScoredPassage psg = (ScoredPassage) sdoc;
        String snippet
                = (Utility.join(ListUtil.slice(doc.terms, psg.begin, psg.end), " "));

        docp.put("snippet", snippet);
      }

      // tags annotation
      if (docTags != null) {

        // get the tags for this resource
        if (docTags.containsKey(sdoc.documentName)) {
          Parameters tmp = Parameters.create();
          for (Map.Entry<Integer, Map<String, String>> entry : docTags.get(sdoc.documentName).entrySet()) {
            //  tmp.put(entry.getKey().toString(), entry.getValue());
            Parameters userData = Parameters.create();
            for (Map.Entry<String, String> ud : entry.getValue().entrySet()) {
              userData.put(ud.getKey(), ud.getValue());
            }
            tmp.put(entry.getKey().toString(), userData);
          }
          if (tmp.size() == 0) {
            docp.set("tags", new ArrayList<String>()); // empty list of tags
          } else {
            docp.set("tags", tmp);
          }
        } else {
          docp.set("tags", new ArrayList<String>()); // empty list of tags
        }
      } // end if we want tags

      // get any rankings of the document
      // TODO: should have a flag indicating IF we want these
      Parameters ratings = Parameters.create();
      ratings = system.userdb.getResourceRatings(doc.name);
      docp.copyFrom(ratings);

      resultData.add(docp);
    }

    // return annotated data:
    return resultData;
  }

  private static   ArrayList<Parameters> sortEntities(int numEntities, Document doc) {
    // keep a count of each entity <entity type <name, count>>
    Map<String, Map<String, Integer>> entities = new HashMap<String, Map<String, Integer>>();
    for (Tag tag : doc.tags) {

      HashMap<String, Long> tmpEnt = new HashMap<String, Long>();
      StringBuilder name = new StringBuilder();
      for (int i = tag.begin; i < tag.end; i++) {
        if (name.length() > 0)
          name.append(" ");
        name.append(doc.terms.get(i));
      }

      if (name.toString().equalsIgnoreCase("google")){
        continue;
      }
      if (!entities.containsKey(tag.name)) {
        entities.put(tag.name, new HashMap<String, Integer>());
      }
      Integer count = 1;
      if (entities.get(tag.name).containsKey(name.toString())) {
        count = entities.get(tag.name).get(name.toString()) + 1;
      }
      entities.get(tag.name).put(name.toString(), count);
    } // end loop through tags

    class Ent {
      String name;
      Integer count;
      Ent(String name, Integer count){
        this.name = name;
        this.count = count;
      }
    }

    ArrayList<Parameters> entList = new ArrayList<>();

    // loop through each entity type
    for (String entType : entities.keySet()) {

      // Make a Priority Queue so we have them sorted
      PriorityQueue<Ent> PQ = new PriorityQueue<Ent>(entities.get(entType).entrySet().size(),
              new Comparator<Ent>() {
                public int compare(Ent p, Ent q) {
                  return (q.count - p.count);
                }
              });

      try {

        for (Map.Entry<String, Integer> ent : entities.get(entType).entrySet()) {
          PQ.add(new Ent(ent.getKey(), ent.getValue()));
        }

        ArrayList<Parameters> parr = new ArrayList<>();
        int imax = Math.min(numEntities, PQ.size());

        for (int i = 0; i < imax; i++) {
          Parameters tp = Parameters.create();
          Ent x = PQ.poll();
          tp.put("entity", x.name);
          tp.put("count", x.count);
          parr.add(tp);
        }
        Parameters p = Parameters.create();
        p.set(entType, parr);
        entList.add(p);

      } catch (Exception e) {
        System.out.println(e.toString());
      }
    } // end loop through entities

    return entList;

  }
}
