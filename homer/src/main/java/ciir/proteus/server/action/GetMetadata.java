package ciir.proteus.server.action;

import ciir.proteus.system.ProteusSystem;
import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.tupleflow.Parameters;

import java.io.IOException;
import java.util.Map;

public class GetMetadata implements JSONHandler {
  private final ProteusSystem system;

  public GetMetadata(ProteusSystem system, Parameters argp) {
    this.system = system;
  }

  @Override
  public Parameters handle(String method, String path, Parameters reqp) {
    String requestId = reqp.getString("id");
    String kind = reqp.get("kind", "books");

    Parameters metadata = new Parameters();
    try {
      Map<String,String> docMeta = system.getRetrieval(kind).getDocument(requestId, new Document.DocumentComponents(false, true, false)).metadata;

      for(Map.Entry<String,String> kv : docMeta.entrySet()) {
        metadata.put(kv.getKey(), kv.getValue());
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return metadata;
  }
}
