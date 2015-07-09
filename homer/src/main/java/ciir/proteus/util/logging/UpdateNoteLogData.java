package ciir.proteus.util.logging;

/**
 * Created by michaelz on 7/8/2015.
 */
public class UpdateNoteLogData extends AddNoteLogData {

  public UpdateNoteLogData(String id, String user) {
    super(id, user);
  }

  @Override
  String getAction() {
    return "UPD-NOTE";
  }

}
