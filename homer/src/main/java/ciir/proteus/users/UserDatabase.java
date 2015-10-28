package ciir.proteus.users;

import ciir.proteus.users.error.DBError;
import ciir.proteus.users.error.DuplicateCorpus;
import ciir.proteus.users.error.DuplicateUser;
import ciir.proteus.users.error.NoTuplesAffected;
import org.lemurproject.galago.utility.Parameters;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
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
    public Parameters login(String username) ;

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

    public Connection getConnection();

    public void checkSession(Credentials creds) throws DBError;

    public List<String> getTags(Credentials creds, String resource) throws DBError;

    public Map<String, List<String>> getTags(Credentials creds, List<String> resources) throws DBError;

    public Map<Integer, Map<String, String>> getAllTags(String resource) throws DBError;

    public Map<String, Map<Integer, Map<String, String>>> getAllTags(List<String> resources) throws DBError;

    public Map<String, Map<String, List<String>>> getAllTagsForResources(List<String> resources) throws DBError;

    public void deleteTag(Credentials creds, String resource, String tag) throws DBError;

    public void addTag(Credentials creds, String resource, String tag, Integer rating, String comment) throws DBError;

    public void updateTag(Credentials creds, String resource, String tag, Integer rating, String comment) throws DBError;

    public List<String> getResourcesForLabels(Integer userid, List<String> labels) throws DBError;

    public List<String> getResourcesForLabels(Integer userid, List<String> labels, Integer numResults, Integer startIndex) throws DBError;

    public Map<String, String> getUsers() throws DBError;

    public Integer createCorpus(String corpus, String username) throws NoTuplesAffected, DuplicateCorpus, SQLException;

    public Parameters upsertSubCorpus(Parameters subcorpus) throws DBError, SQLException;

    public Parameters getAllCorpora() throws DBError;

    public Parameters getAllSubCorpora() throws DBError;

    public void updateUserSettings(Credentials creds, String settings) throws DBError;

    public Parameters getResourceRatings(String resource, Integer corpusID);
    public Parameters getResourceRatings2(String resource, Integer corpusID);

    public void upsertResourceRating(Credentials creds, String resource, Integer userID, Integer corpusID, Integer rating, Integer queryid) throws DBError;

    public List<String> getResourcesForCorpus(Integer userid, Integer corpusID, Integer numResults, Integer startIndex) throws DBError;

    public Parameters getResourcesForCorpusByQuery(Integer corpusID) throws DBError;

    public List<String> getAllResourcesForCorpus(Integer userid, Integer corpusID) throws DBError;

    public void updateNote(Credentials creds, Integer id, Integer corpusID, String data) throws DBError;

    public Parameters getNotesForResource(String resource, Integer corpusID) throws DBError, IOException;

    public Integer insertNote(Credentials creds, Integer corpusID, String resource, String data) throws DBError;

    public void deleteNote(Credentials creds, Integer id, Integer corpusID) throws DBError;

    public Parameters getNotesForCorpus(Integer corpusID) throws DBError, IOException;

    public Integer insertQuery(Credentials creds, Integer corpusID, String query, String kind) throws DBError;

    public void insertQueryResourceXref(Credentials creds, String resource, Integer corpusID, Integer queryid) throws DBError;

    public void addVoteForResource(Credentials creds, String resource, Integer corpusID, Integer subcorpusID, Integer queryid) throws DBError;
    public void removeVoteForResource(Credentials creds, String resource, Integer corpusID, Integer subcorpusID) throws DBError;


}
