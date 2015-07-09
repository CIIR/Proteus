package ciir.proteus.util.logging;

import ciir.proteus.system.ChatObject;
import org.apache.logging.log4j.LogManager;

/**
 * Created by michaelz on 7/7/2015.
 */
public class LogHelper {

  private static final org.apache.logging.log4j.Logger proteusLog = LogManager.getLogger("Proteus");
  private static final org.apache.logging.log4j.Logger activityLog = LogManager.getLogger("ProteusActvity");

  public static void log(LogData info){
    proteusLog.info(info.toTSV());
  }
  public static void log(String action, String str) { // TODO should be variable params

    proteusLog.info("INFO level log");
    activityLog.info("ACTIVITY log");
//    ChatObject msg = new ChatObject("", query);
//    system.broadcastMsg("SEARCH", msg);

  }
}

