package ciir.proteus.util.logging;

/**
 * Created by michaelz on 7/8/2015.
 */
public class DeleteNoteLogData extends AddNoteLogData {

  public DeleteNoteLogData(String id, String user) {
    super(id, user);
  }

  @Override
  String getAction() {
    return "DEL-NOTE";
  }
 
}
