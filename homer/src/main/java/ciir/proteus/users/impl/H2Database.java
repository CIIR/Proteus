package ciir.proteus.users.impl;

import ciir.proteus.users.Credentials;
import ciir.proteus.users.UserDatabase;
import ciir.proteus.users.Users;
import ciir.proteus.users.error.BadSessionException;
import ciir.proteus.users.error.BadUserException;
import ciir.proteus.users.error.DBError;
import ciir.proteus.users.error.DuplicateUser;
import ciir.proteus.users.error.NoTuplesAffected;
import org.lemurproject.galago.utility.Parameters;

import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * @author jfoley.
 */
public class H2Database implements UserDatabase {

    private static final Logger log = Logger.getLogger(H2Database.class.getName());
    private Connection conn;
    private PreparedStatement getAllTagsSQL = null;
    private PreparedStatement getResourcesForLabelsAndUserSQL = null;
    private PreparedStatement getResourcesForLabelsAndUserWithLimitSQL = null;

    public H2Database(Parameters conf) {
        try {
            // prime JDBC driver
            Class.forName("org.h2.Driver");
            // configuration
            String path = conf.getString("path");
            String dbuser = conf.getString("user");
            String dbpass = conf.get("pass", "");
            // setting AUTO_SERVER to TRUE allows multiple processes to 
            // connect to the DB - useful for debugging with a DB viewer
            String autoServer = conf.get("auto_server", "FALSE");
            // open a connection
            conn = DriverManager.getConnection("jdbc:h2:" + path + ";AUTO_SERVER=" + autoServer, dbuser, dbpass);

            // create tables if needed
            initDB();

            // prepare the SQL just once
            getAllTagsSQL = conn.prepareStatement("SELECT user_id, label_type || ':' || label_value AS tag FROM tags WHERE resource LIKE ? GROUP BY user_id, tag ORDER BY user_id, tag");

            // perpared statements don't like "in(...)" clauses, hence the cryptic SQL to do this:
            //  SELECT DISTINCT resource FROM tags WHERE user LIKE ? AND tag IN (?) 
            // we need to ORDER BY to ensure the result sets will always be in the same order.
            getResourcesForLabelsAndUserWithLimitSQL = conn.prepareStatement("SELECT DISTINCT resource FROM table(x VARCHAR=?) t INNER JOIN tags ON t.x=tags.label_type || ':' || tags.label_value AND tags.user_id = ? ORDER BY resource LIMIT ? OFFSET ?");
            getResourcesForLabelsAndUserSQL = conn.prepareStatement("SELECT DISTINCT resource FROM table(x VARCHAR=?) t INNER JOIN tags ON t.x=tags.label_type || ':' || tags.label_value AND tags.user_id = ? ORDER BY resource");

        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void initDB() {
        try {
            /*
             conn.prepareStatement("create table IF NOT EXISTS users ("
             + "user varchar(" + Users.UserMaxLength + ")"
             + ")").execute();
             conn.prepareStatement("create table IF NOT EXISTS sessions ("
             + "user varchar(" + Users.UserMaxLength + "), "
             + "session char(" + Users.SessionIdLength + "), "
             + "foreign key (user) references users(user)"
             + ")").execute();
             conn.prepareStatement("create table IF NOT EXISTS tags ("
             + "user varchar(" + Users.UserMaxLength + "), "
             + "resource varchar(256), "
             + "tag varchar(256), "
             + "foreign key (user) references users(user)"
             + ")").execute();
             */
            conn.prepareStatement("create table IF NOT EXISTS users ("
                    + "ID BIGINT NOT NULL IDENTITY, EMAIL VARCHAR(" + Users.UserEmailMaxLength + ") NOT NULL, PRIMARY KEY (ID)"
                    + ")").execute();
            conn.prepareStatement("create unique index IF NOT EXISTS user_uniq_email_idx on users(email)").execute();

            conn.prepareStatement("create table IF NOT EXISTS sessions ("
                    + "user_id bigint, "
                    + "session char(" + Users.SessionIdLength + "), "
                    + "foreign key (user_id) references users(id)"
                    + ")").execute();

            conn.prepareStatement("create table IF NOT EXISTS tags ("
                    + "USER_ID BIGINT,  "
                    + "resource varchar(256), "
                    + "LABEL_TYPE VARCHAR_IGNORECASE(256), LABEL_VALUE VARCHAR_IGNORECASE(256), "
                    + "foreign key (user_id) references users(id)"
                    + ")").execute();

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void register(String username) throws NoTuplesAffected, DuplicateUser {
        try {

            PreparedStatement stmt = conn.prepareStatement("insert into users (email) values (LOWER(?))");
            stmt.setString(1, username);
            int numRows = stmt.executeUpdate();
            if (numRows == 0) {
                throw new NoTuplesAffected();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DuplicateUser();
        }
    }

    @Override
    public Parameters login(String username) {
        if (!validUser(username)) {
            return null;
        }
        Parameters ret = Parameters.instance();
        ret.put("user", username);
        Integer userid = -1;

        try {
            // get the user id
            PreparedStatement user_id_stmt = conn.prepareStatement("select id from users where email=LOWER(?)");
            user_id_stmt.setString(1, username);
            ResultSet results = user_id_stmt.executeQuery();

            if (results.next()) {
                userid = results.getInt(1);
                ret.put("userid", userid);
            }
            results.close();

            String session = Users.generateSessionId();

            PreparedStatement stmt = conn.prepareStatement("insert into sessions (user_id,session) values (?, ?)");

            stmt.setInt(1, userid);
            stmt.setString(2, session);

            // should create 1 row
            if (1 == stmt.executeUpdate()) {
                ret.put("token", session);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ret;
    }

    @Override
    public void logout(Credentials creds) throws NoTuplesAffected {
        if (!validSession(creds)) {
            return;
        }

        try {
            PreparedStatement stmt = conn.prepareStatement("delete from sessions where user_id=? and session=?");
            stmt.setInt(1, creds.userid);
            stmt.setString(2, creds.token);

            int numRows = stmt.executeUpdate();
            if (numRows == 0) {
                // since validSession was true,
                // this probably should have worked, barring race conditions
                throw new NoTuplesAffected();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean validUser(String username) {
        if (username.length() > 64) {
            return false;
        }

        boolean found = false;
        try {

            PreparedStatement stmt = conn.prepareStatement("select count(*) from users where email=LOWER(?)");
            stmt.setString(1, username);
            ResultSet results = stmt.executeQuery();

            if (results.next()) {
                int numUsers = results.getInt(1);
                found = (numUsers == 1);
            }
            results.close();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return found;
    }

    @Override
    public boolean validSession(Credentials creds
    ) {
        boolean found = false;

        try {
            PreparedStatement stmt = conn.prepareStatement("select (user_id,session) from sessions where user_id=? and session=?");
            stmt.setInt(1, creds.userid);
            stmt.setString(2, creds.token);

            ResultSet results = stmt.executeQuery();
            if (results.next()) {
                found = true;
            }
            results.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return found;
    }

    @Override
    public void checkSession(Credentials creds) throws DBError {
        if (!validUser(creds.user)) {
            throw new BadUserException(creds.user);
        }
        if (!validSession(creds)) {
            throw new BadSessionException(creds.user, creds.token);
        }
    }

    @Override
    public List<String> getTags(Credentials creds, String resource) throws DBError {
        Map<String, List<String>> results = getTags(creds, Arrays.asList(resource));
        return results.get(resource);
    }

    public Map<String, List<String>> getTags(Credentials creds, List<String> resources) throws DBError {
        checkSession(creds);

        Map<String, List<String>> results = new HashMap<String, List<String>>();

        try {
            PreparedStatement stmt = conn.prepareStatement("select label_type || ':' || label_value from tags where user_id=? and resource=?");
            stmt.setInt(1, creds.userid);

            for (String resource : resources) {
                List<String> tags = new ArrayList<String>();
                stmt.setString(2, resource);

                ResultSet tuples = stmt.executeQuery();
                while (tuples.next()) {
                    String tag = tuples.getString(1);
                    tags.add(tag);
                }
                tuples.close();
                results.put(resource, tags);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return results;
    }

    // returns resources and a list of users and their tags.
    @Override
    public Map<Integer, List<String>> getAllTags(String resource) throws DBError {
        Map<String, Map<Integer, List<String>>> results = getAllTags(Arrays.asList(resource));
        return results.get(resource);
    }

    @Override
    public Map<String, Map<Integer, List<String>>> getAllTags(List<String> resources) throws DBError {

        Map<String, Map<Integer, List<String>>> results = new HashMap<String, Map<Integer, List<String>>>();

        try {

            for (String resource : resources) {
                Map<Integer, List<String>> userTags = new HashMap<Integer, List<String>>();
                getAllTagsSQL.setString(1, resource);

                ResultSet tuples = getAllTagsSQL.executeQuery();

                Integer currentUser = -1;
                List<String> tags = new ArrayList<String>();

                while (tuples.next()) {

                    Integer user = tuples.getInt(1);

                    if (currentUser == -1) {   // first time through
                        currentUser = user;
                    }

                    String tag = tuples.getString(2);

                    if (currentUser != user) {
                        userTags.put(currentUser, tags);
                        results.put(resource, userTags); // put user/tags in results for the resource

                        currentUser = user;
                        tags = new ArrayList<String>();
                    }

                    tags.add(tag);

                } // end while we have rows

                // put in the last user - if there is one
                if (currentUser != -1) {
                    userTags.put(currentUser, tags);
                }
                results.put(resource, userTags);

                tuples.close();

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return results;
    }

    @Override
    public void deleteTag(Credentials creds, String resource, String tag) throws DBError {
        checkSession(creds);
        String labelParts[] = tag.split(":");
        try {
            PreparedStatement stmt = conn.prepareStatement("delete from tags where user_id=? and resource=? and label_type=? and label_value=? ");
            stmt.setInt(1, creds.userid);
            stmt.setString(2, resource);
            // if there is only 1 part to the tag, use the "wildcard" for the label type
            if (labelParts.length == 1) {
                stmt.setString(3, "*");
                stmt.setString(4, labelParts[0]);
            } else {
                stmt.setString(3, labelParts[0]);
                stmt.setString(4, labelParts[1]);
            }

            int numRows = stmt.executeUpdate();

            if (numRows == 0) {
                log.info("user: '" + creds.user + "', resource: '" + resource + "', tag: '" + tag + "'");
                throw new NoTuplesAffected();
            }
        } catch (SQLException e) {
            log.info("user: '" + creds.user + "', resource: '" + resource + "', tag: '" + tag + "'");
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addTag(Credentials creds, String resource, String tag) throws DBError {
        checkSession(creds);

        String labelParts[] = tag.split(":");
        try {
            // TODO - move this prepere to constructor/init code.
            PreparedStatement stmt = conn.prepareStatement("insert into tags (user_id,resource,label_type, label_value) values (?,?,?,?)");
            stmt.setInt(1, creds.userid);
            stmt.setString(2, resource);

            // if there is only 1 part to the tag, use the "wildcard" for the label type
            if (labelParts.length == 1) {
                stmt.setString(3, "*");
                stmt.setString(4, labelParts[0]);
            } else {
                stmt.setString(3, labelParts[0]);
                stmt.setString(4, labelParts[1]);
            }

            int numRows = stmt.executeUpdate();
            assert (numRows == 1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> getResourcesForLabels(Integer userid, List<String> labels) throws DBError {
        return getResourcesForLabels(userid, labels, -1, -1);
    }

    @Override
    public List<String> getResourcesForLabels(Integer userid, List<String> labels, Integer numResults, Integer startIndex) throws DBError {

        try {
            List<String> resources = new ArrayList<String>();
            Object[] objLabels = new Object[labels.size()];
            int i = 0;
            for (String label : labels) {
                objLabels[i++] = label;
            }
            ResultSet results = null;

            if (numResults == -1) {
                getResourcesForLabelsAndUserSQL.setObject(1, objLabels);
                getResourcesForLabelsAndUserSQL.setInt(2, userid);
                results = getResourcesForLabelsAndUserSQL.executeQuery();
            } else {
                getResourcesForLabelsAndUserWithLimitSQL.setObject(1, objLabels);
                getResourcesForLabelsAndUserWithLimitSQL.setInt(2, userid);
                getResourcesForLabelsAndUserWithLimitSQL.setInt(3, numResults);
                getResourcesForLabelsAndUserWithLimitSQL.setInt(4, startIndex);
                results = getResourcesForLabelsAndUserWithLimitSQL.executeQuery();
            }
            while (results.next()) {
                String res = results.getString(1);
                resources.add(res);
            }
            results.close();

            return resources;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
