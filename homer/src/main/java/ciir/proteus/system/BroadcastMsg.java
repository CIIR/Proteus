package ciir.proteus.system;

/**
 * Created by michaelz on 7/2/2015.
 */
public class BroadcastMsg {

    private String action;
    private String message;

    public BroadcastMsg() {
    }

    public BroadcastMsg(String action, String message) {
        super();
        this.action = action;
        this.message = message;
    }

    public String getAction() {
        return action;
    }
    public void setAction(String action) {
        this.action = action;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

}
