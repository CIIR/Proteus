package ciir.proteus.users.error;

/**
 * Created by michaelz on 5/18/2015.
 */
public class DuplicateSubCorpus extends DBError {
 
  public DuplicateSubCorpus(){
    super("Label names must be unique.");
  }
}
