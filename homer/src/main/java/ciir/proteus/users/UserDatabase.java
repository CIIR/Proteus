package ciir.proteus.users;

import ciir.proteus.users.error.DBError;
import ciir.proteus.users.error.DuplicateUser;
import ciir.proteus.users.error.NoTuplesAffected;
import org.lemurproject.galago.utility.Parameters;

import java.util.List;
import java.util.Map;

/**
 * @author jfoley.
 */
public interface UserDatabase {

    /**
     * shut down connection
     */
    public void close();

    /**
     * Add a user to the database
     *
     */
    public void register(String username) throws NoTuplesAffected, DuplicateUser;

    /**
     * Logs in a
     *
     * @param username the user to login
     * @return a session token for that user and the user id MCZ 7/9/2014 -
     * updated to return parameters rather than just the session id since the DB
     * structure changed.
     */
    public Parameters login(String username);

    /**
     * Logs out a user/session combo
     */
    public void logout(Credentials creds) throws NoTuplesAffected;

    /**
     * Returns true if user has registered
     *
     * @param user a username
     * @return registered?
     */
    public boolean validUser(String user);

    /**
     * Returns a user record for a given session
     *
     * @return user object for the logged in user or null
     */
    public boolean validSession(Credentials creds);

    public void checkSession(Credentials creds) throws DBError;

    public List<String> getTags(Credentials creds, String resource) throws DBError;

    public Map<String, List<String>> getTags(Credentials creds, List<String> resources) throws DBError;

    public Map<Integer, List<String>> getAllTags(String resource) throws DBError;

    public Map<String, Map<Integer, List<String>>> getAllTags(List<String> resources) throws DBError;

    public void deleteTag(Credentials creds, String resource, String tag) throws DBError;

    public void addTag(Credentials creds, String resource, String tag, Integer rating) throws DBError;

    public List<String> getResourcesForLabels(Integer userid, List<String> labels) throws DBError;

    public List<String> getResourcesForLabels(Integer userid, List<String> labels, Integer numResults, Integer startIndex) throws DBError;
}
