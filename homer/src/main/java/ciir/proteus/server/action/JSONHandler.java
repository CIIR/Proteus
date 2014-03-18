package ciir.proteus.server.action;

import org.lemurproject.galago.tupleflow.Parameters;

public interface JSONHandler {
  public abstract Parameters handle(String method, String path, Parameters reqp);
}
