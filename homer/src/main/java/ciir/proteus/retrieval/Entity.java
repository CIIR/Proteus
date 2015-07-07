package ciir.proteus.retrieval;

import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by wem on 6/23/15.
 */
public class Entity {

    String name;
    Set<String> aliases;
    Set<String> locations;
    String languageModel;
    double score;

    public Entity(String name){
        this.name = name;
        this.aliases = new HashSet<String>();
        this.locations = new HashSet<String>();
        this.languageModel = "";
        this.score = 0;
    }

    public String getName() {
        return name;
    }

    public Set<String> getAliases(){
        return aliases;
    }

    public void addAlias(String name){
        aliases.add(name);
    }

    public Set<String> getLocations(){
        return locations;
    }

    public void addLocation(String identifier){
        locations.add(identifier);
    }

    public String getLanguageModel(){
        return languageModel;
    }

    public void extendLanguageModel(String newText){
        languageModel = languageModel + newText;
    }

    public void addScore(double score){
        this.score = this.score + score;
    }

    // compare entities on the basis of the string edit distance of their aliases
    public int compareAliases(String s) {
        int minDistance = Integer.MAX_VALUE;
        for(String alias: aliases){
            int newDistance = StringUtils.getLevenshteinDistance(alias, s);
            minDistance = (newDistance < minDistance) ? newDistance : minDistance;
        }
        System.out.println("Comparison: " + name);
        System.out.println("Score: " + minDistance);
        return minDistance;
    }

    //TODO finish this code
    // compare entities on the basis of the string edit distance of their aliases
    // minDistance returned is for the min distance between to segments
    // thus "David Johnson" and "David Jonson" would have a distance of 0
    public int compareAliasesBySegment(String s) {
        String[] sComponents = s.split(" ");
        int minDistance = Integer.MAX_VALUE;
        for(String alias: aliases){
            for(String aliasComponent: alias.split(" ")){
                for(String sComponent: sComponents) {
                    System.out.println("  Comparing " + aliasComponent + " against " + sComponent);
                    int newDistance = StringUtils.getLevenshteinDistance(aliasComponent, sComponent);
                    minDistance = (newDistance < minDistance) ? newDistance : minDistance;
                    System.out.println("   Distance = " + newDistance);
                }
            }

        }
        return minDistance;
    }

    // compare entities on the basis of the string edit distance of their aliases
    public int compare(Entity e) {
        int minDistance = Integer.MAX_VALUE;
        for(String alias: e.aliases){
            int newDistance = compareAliases(alias);
            minDistance = (newDistance < minDistance) ? newDistance : minDistance;
        }
        return minDistance;
    }

    //add the data from an entity to this entity
    public void addEntity(Entity e){
        aliases.addAll(e.aliases);
        locations.addAll(e.locations);
        extendLanguageModel(e.languageModel);
    }

}
