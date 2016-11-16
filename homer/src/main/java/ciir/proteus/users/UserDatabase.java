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
    void close();

    /**
     * Add a user to the database
     *
     */
    void register(String username) throws NoTuplesAffected, DuplicateUser;

    /**
     * Logs in a
     *
     * @param username the user to login
     * @return a session token for that user and the user id MCZ 7/9/2014 -
     * updated to return parameters rather than just the session id since the DB
     * structure changed.
     */
    Parameters login(String username) ;

    /**
     * Logs out a user/session combo
     */
    void logout(Credentials creds) throws NoTuplesAffected;

    /**
     * Returns true if user has registered
     *
     * @param user a username
     * @return registered?
     */
    boolean validUser(String user);

    /**
     * Returns a user record for a given session
     *
     * @return user object for the logged in user or null
     */
    boolean validSession(Credentials creds);

    Connection getConnection();

    void checkSession(Credentials creds) throws DBError;

    Map<String, String> getUsers() throws DBError;

    Integer createCorpus(String corpus, String username) throws NoTuplesAffected, DuplicateCorpus, SQLException;

    Parameters upsertSubCorpus(Parameters subcorpus) throws DBError, SQLException;

    Parameters getAllCorpora() throws DBError;

    Parameters getAllSubCorpora() throws DBError;

    void updateUserSettings(Credentials creds, String settings) throws DBError;

    Parameters getResourceLabels(String resource, Integer corpusID);

    List<String> getResourcesForCorpus(Integer userid, Integer corpusID, Integer numResults, Integer startIndex) throws DBError;

    Parameters getResourcesForCorpusByQuery(Integer corpusID) throws DBError;

    List<String> getAllResourcesForCorpus(Integer userid, Integer corpusID) throws DBError;

    void updateNote(Credentials creds, Integer id, Integer corpusID, String data) throws DBError;

    Parameters getNotesForResource(String resource, Integer corpusID) throws DBError, IOException;

    Integer insertNote(Credentials creds, Integer corpusID, String resource, String data) throws DBError;

    void deleteNote(Credentials creds, Integer id, Integer corpusID) throws DBError;

    Parameters getNotesForCorpus(Integer corpusID) throws DBError, IOException;

    Parameters getNotesForBook(String bookID, Integer corpusID) throws DBError, IOException;

    String getQuery(Credentials creds, Integer queryID) throws DBError;

    Integer insertQuery(Credentials creds, Integer corpusID, String query, String kind) throws DBError;

    Parameters getQueriesForResource(String resource, Integer corpusID) throws DBError, IOException;

    void insertQueryResourceXref(Credentials creds, String resource, Integer corpusID, Integer queryid) throws DBError;

    void addVoteForResource(Credentials creds, String resource, Integer corpusID, Integer subcorpusID, Integer queryid) throws DBError;
    void removeVoteForResource(Credentials creds, String resource, Integer corpusID, Integer subcorpusID) throws DBError;

    List<String> getResourcesForSubcorpora(Integer userid, Integer corpus, List<Long> subcorpora) throws DBError;

    List<String> getResourcesForSubcorpora(Integer userid, Integer corpus, List<Long> subcorpora, Integer numResults, Integer startIndex) throws DBError;


}
