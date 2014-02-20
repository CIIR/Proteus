package ciir.proteus.server.action;

import ciir.proteus.system.SearchSystem;
import org.lemurproject.galago.tupleflow.Parameters;

public interface RequestHandler {
  public abstract Parameters handle(Parameters reqp);
}
