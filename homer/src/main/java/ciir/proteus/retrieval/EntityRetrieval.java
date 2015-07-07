package ciir.proteus.retrieval;

import ciir.proteus.util.QueryUtil;
import org.apache.commons.lang3.StringUtils;
import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.parse.Tag;
import org.lemurproject.galago.core.parse.TagTokenizer;
import org.lemurproject.galago.core.retrieval.LocalRetrieval;
import org.lemurproject.galago.core.retrieval.Retrieval;
import org.lemurproject.galago.core.retrieval.ScoredDocument;
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.core.retrieval.query.StructuredQuery;
import org.lemurproject.galago.utility.Parameters;

import java.io.IOException;
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

    public List<Entity> buildSortedEntityList() throws IOException {
        ArrayList<Entity> entities= new ArrayList<Entity>();

        for(ScoredDocument sDoc: resultsList){
            System.out.println(sDoc.documentName);
            String name = sDoc.documentName;
            Entity e = new Entity(name);
            TagTokenizer tok = new TagTokenizer();
            tok.addField("alias");
            tok.addField("location");
            Document doc = ret.getDocument(sDoc.documentName, new Document.DocumentComponents(true, true, true));
            tok.tokenize(doc);
            for(Tag tag: doc.tags){
                StringBuilder sb = new StringBuilder();
                for (int i = tag.begin; i < tag.end; i++) {
                    sb.append(doc.terms.get(i));
                }
                if(tag.name.equals("alias")) e.addAlias(sb.toString());
                if(tag.name.equals("location")) e.addLocation(sb.toString());
            }
            e.addScore(sDoc.getScore());
            e.extendLanguageModel(sDoc.toString());
            entities.add(e);

        }

        return entities;
    }

    //create a new entity from the data of two other entities
    //the entity takes the name of the first entity
    public Entity mergeEntities(Entity a, Entity b){
        Entity merged = new Entity(a.name);
        merged.aliases.addAll(a.aliases);
        merged.aliases.addAll(b.aliases);
        merged.locations.addAll(a.locations);
        merged.locations.addAll(b.locations);
        merged.extendLanguageModel(a.languageModel);
        merged.extendLanguageModel(b.languageModel);
        return merged;
    }

    public List<Entity> search(String rawQuery, int count) throws IOException {
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
