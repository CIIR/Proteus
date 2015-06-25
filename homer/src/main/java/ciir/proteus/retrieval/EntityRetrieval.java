package ciir.proteus.retrieval;

import ciir.proteus.util.QueryUtil;
import org.lemurproject.galago.core.retrieval.LocalRetrieval;
import org.lemurproject.galago.core.retrieval.Retrieval;
import org.lemurproject.galago.core.retrieval.ScoredDocument;
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.core.retrieval.query.StructuredQuery;
import org.lemurproject.galago.utility.Parameters;

import java.util.*;

/**
 * Created by wem on 6/23/15.
 */
public class EntityRetrieval {

    private List<ScoredDocument> resultsList;
    private Retrieval ret;

    public EntityRetrieval(String indexPath) throws Exception {
        resultsList = null;
        ret = new LocalRetrieval(indexPath);
        if(!((LocalRetrieval) ret).getIndex().containsPart("corpus")) {
            System.out.println("Doesn't have a corpus?");
        }
    }

    public List<ScoredDocument> getResults(){
        return resultsList;
    }

    public Retrieval getRetrieval(){
        return ret;
    }

    //retrieves the number of results specified by count
    public void documentSearch(String rawQuery, int count) {
        try {
            Parameters searchParms = Parameters.instance();
            searchParms.set("requested", count);
            Node query = StructuredQuery.parse(rawQuery);
            Node trfmd = ret.transformQuery(query, searchParms);
            resultsList = ret.executeQuery(trfmd, searchParms).scoredDocuments;
            //ret.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Entity> buildSortedEntityList(){
        HashMap <String, Entity> entities= new HashMap<>();

        for(ScoredDocument sDoc: resultsList){
            System.out.println(sDoc.documentName);
            String name = sDoc.documentName.split("\\.")[0];
            String identifier = sDoc.documentName.split("\\.")[1];
            if(!entities.containsKey(name)) {
                Entity e = new Entity(name);
                entities.put(name, e);
            }
            entities.get(name).addLocation(identifier);
            entities.get(name).addScore(sDoc.getScore());
            entities.get(name).extendLanguageModel(sDoc.toString());

        }

        TreeMap<Double, List<Entity>> scoreMap = new TreeMap<Double, List<Entity>>(Collections.reverseOrder());
        for(Entity e: entities.values()) {
            if(!scoreMap.containsKey(e.score)) scoreMap.put(e.score, new ArrayList<Entity>());
            scoreMap.get(e.score).add(e);
        }

        List<Entity> sortedEntities = new ArrayList<Entity>();
        for(List<Entity> el: scoreMap.values()){
            for(Entity e: el) sortedEntities.add(e);
        }
        return sortedEntities;
    }

    public List<Entity> search(String rawQuery, int count){
        documentSearch(rawQuery, count);
        List<Entity> sortedEntities = buildSortedEntityList();

        return sortedEntities;
    }

    public static void main(String[] args) throws Exception {
        String rawQuery = args[1];
        int count = Integer.parseInt(args[2]);
        EntityRetrieval er = new EntityRetrieval(args[0]);
        List<Entity> sortedEntities = er.search(rawQuery, count);
        for(Entity e:sortedEntities){
            StringBuilder locations = new StringBuilder();
            for(String indentifier: e.locations){
                locations.append(" " + indentifier);
            }
            System.out.println(Double.toString(e.score) + " " + e.name + " in books:" + locations);
        }
    }

}
