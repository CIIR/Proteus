package ciir.proteus.users;

import org.lemurproject.galago.utility.Parameters;

/**
 * @author jfoley.
 */
public class Credentials {

    public String user;
    public String token;
    public Integer userid;

    public Credentials(Parameters loginParam) {
        this.user = loginParam.getAsString("user");
        this.token = loginParam.getAsString("token");
        String tmp = loginParam.getAsString("userid");
        if (tmp.length() == 0) {
            this.userid = null;
        } else {
            this.userid = Integer.parseInt(tmp);
        }
    }

    public static Credentials login(String user, UserDatabase db) {
        return new Credentials(db.login(user));
    }

    public static Credentials fromJSON(Parameters p) {
        return new Credentials(p);
    }

    @Override
    public String toString() {
        return toJSON().toString();
    }

    public Parameters toJSON() {
        Parameters p = Parameters.create();
        p.put("user", user);
        p.put("userid", userid);
        p.put("token", token);
        return p;
    }
}
