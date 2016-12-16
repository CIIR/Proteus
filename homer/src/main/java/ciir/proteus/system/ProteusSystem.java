package ciir.proteus.system;

import ciir.proteus.tools.apps.IndexType;
import ciir.proteus.users.UserDatabase;
import ciir.proteus.users.UserDatabaseFactory;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import org.lemurproject.galago.utility.Parameters;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProteusSystem {

  public final String defaultKind;
  private final Parameters config;

  public UserDatabase userdb;
  final private SocketIOServer broadcastServer;

  private IndexType index;

  public ProteusSystem(Parameters argp) throws Exception {
    this.config = argp;
    this.defaultKind = argp.getString("defaultKind");

    ClassLoader classLoader = ProteusSystem.class.getClassLoader();

    try {
      Class klazz = classLoader.loadClass(argp.get("indexType", "ciir.proteus.tools.apps.Galago"));
      Constructor<?> ctor = klazz.getConstructor(Parameters.class);
      index = (IndexType) ctor.newInstance(new Object[]{argp});
      index.whoAmI();
      System.out.println("Loaded index type: " + klazz.getName());
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }

    this.userdb = UserDatabaseFactory.instance(argp.getMap("userdb"));

    loadNoteIndex();

    // only configure if we need to
    if (argp.containsKey("broadcast")) {
      Parameters broadcastParams = argp.getMap("broadcast");
      Configuration config = new Configuration();
      config.setHostname(broadcastParams.get("url", "localhost"));
      config.setPort(broadcastParams.getInt("port"));
      broadcastServer = new SocketIOServer(config);
      broadcastServer.start();
    } else {
      broadcastServer = null;
    }

  }

  public IndexType getIndex() {
    return index;
  }

  public List<ProteusDocument> doSearch(String kind, String query, Parameters qp) throws IOException {
    return index.doSearch(kind, query, qp);
  }

  public void close() throws IOException {

    index.close();

    userdb.close();
    if (broadcastServer != null)
      broadcastServer.stop();
  }

  public Map<String, ProteusDocument> getDocs(String kind, List<String> names, boolean metadata, boolean text) {
    return index.getDocs(kind, names, metadata, text);
  }

  public Set<String> getKinds() {
    return index.getKinds();
  }

  public Parameters getConfig() {
    return config;
  }

  public void broadcastMsg(BroadcastMsg msg) {

    if (broadcastServer == null)
      return;

    broadcastServer.getBroadcastOperations().sendEvent("ProteusEvent", msg);

  }

  public void loadNoteIndex() throws Exception {
    // TODO : get corpus number
    Parameters notes = this.userdb.getNotesForCorpus(1);
    index.loadNoteIndex(notes);
  }

  public List<String> getWorkingSetDocNames(String kind, String archiveid) throws IOException {
    return index.getWorkingSetDocNames(kind, archiveid);
  }
  public Boolean needPassage(){
    return index.needPassage();
  }

}
