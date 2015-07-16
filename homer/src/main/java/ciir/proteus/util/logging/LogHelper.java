package ciir.proteus.util.logging;

import ciir.proteus.system.BroadcastMsg;
import ciir.proteus.system.ProteusSystem;
import org.apache.logging.log4j.LogManager;

/**
 * Created by michaelz on 7/7/2015.
 */
public class LogHelper {


  private static final org.apache.logging.log4j.Logger proteusLog = LogManager.getLogger("Proteus");
  private static final org.apache.logging.log4j.Logger activityLog = LogManager.getLogger("ProteusActvity");

//  public static void setProteus(ProteusSystem proteus){
//    system = proteus;
//  }
  public static void log(LogData info, ProteusSystem proteus) {
    proteusLog.info(info.toTSV());
    String json = info.toJSON();
    if (json != null) {
      activityLog.info(json);
      BroadcastMsg msg = new BroadcastMsg(info.getAction(), "dummy-time-stamp\t" +  json);
      proteus.broadcastMsg(msg);
    }
  }

}

