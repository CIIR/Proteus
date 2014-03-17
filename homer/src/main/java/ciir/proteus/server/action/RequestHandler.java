package ciir.proteus.server.action;

import org.lemurproject.galago.tupleflow.Parameters;

public interface RequestHandler {
  public abstract Parameters handle(Parameters reqp);
}
