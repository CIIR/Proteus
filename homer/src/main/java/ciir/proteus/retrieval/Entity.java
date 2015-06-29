package ciir.proteus.retrieval;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wem on 6/23/15.
 */
public class Entity {

    String name;
    List<String> locations;
    String languageModel;
    double score;

    public Entity(String name){
        this.name = name;
        this.locations = new ArrayList<String>();
        this.languageModel = "";
        this.score = 0;
    }

    public void addLocation(String identifier){
        locations.add(identifier);
    }

    public void extendLanguageModel(String newText){
        languageModel = languageModel + newText;
    }

    public void addScore(double score){
        this.score = this.score + score;
    }

}
