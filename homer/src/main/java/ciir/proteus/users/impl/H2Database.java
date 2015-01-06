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
import com.mchange.v2.c3p0.*;
import java.beans.PropertyVetoException;

/**
 * @author jfoley. Updates: MichaelZ
 */
public class H2Database implements UserDatabase {

    private static final Logger log = Logger.getLogger(H2Database.class.getName());

    private ComboPooledDataSource cpds = null;

    public H2Database(Parameters conf) throws SQLException {

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
            // create the connection pool
            cpds = new ComboPooledDataSource();
            cpds.setDriverClass("org.h2.Driver"); //loads the jdbc driver            
            cpds.setJdbcUrl("jdbc:h2:" + path + ";AUTO_SERVER=" + autoServer);
            cpds.setUser(dbuser);
            cpds.setPassword(dbpass);
            cpds.setAutoCommitOnClose(true); // without this - connections don't close unless you explicitly commit
            cpds.setMaxPoolSize((int) conf.get("max_pool_size", 1000));
            cpds.setInitialPoolSize((int) conf.get("initial_pool_size", 50));
            cpds.setMaxStatementsPerConnection((int) conf.get("max_prepared_statements_per_connection", 10));  // allow for prepared statements

            // create tables if needed
            initDB();

        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        } catch (PropertyVetoException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void initDB() {
        Connection conn = null;
        try {
            conn = cpds.getConnection();
            // NOTE: H2 will automatically create an index on any foreign keys.
            conn.createStatement().executeUpdate("create table IF NOT EXISTS users ("
                    + "ID BIGINT NOT NULL IDENTITY, EMAIL VARCHAR(" + Users.UserEmailMaxLength + ") NOT NULL, PRIMARY KEY (ID)"
                    + ")");
            conn.createStatement().executeUpdate("create unique index IF NOT EXISTS user_uniq_email_idx on users(email)");

            conn.createStatement().executeUpdate("create table IF NOT EXISTS sessions ("
                    + "user_id bigint NOT NULL, "
                    + "session char(" + Users.SessionIdLength + "), "
                    + "foreign key (user_id) references users(id)"
                    + ")");

            conn.createStatement().executeUpdate("create table IF NOT EXISTS tags ("
                    + "USER_ID BIGINT NOT NULL,  "
                    + "resource varchar(256) NOT NULL, "
                    + "LABEL_TYPE VARCHAR_IGNORECASE(256) NOT NULL, LABEL_VALUE VARCHAR_IGNORECASE(256) NOT NULL, "
                    + "rating int NOT NULL default 0, "
                    + "foreign key (user_id) references users(id)"
                    + ")");
            conn.createStatement().executeUpdate("create index IF NOT EXISTS label_resource_idx on tags(resource)");

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            attemptClose(conn);
        }
    }

    @Override
    public void close() {
        try {
            DataSources.destroy(cpds);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void register(String username) throws NoTuplesAffected, DuplicateUser {
        Connection conn = null;
        try {
            conn = cpds.getConnection();
            int numRows = conn.createStatement().executeUpdate("insert into users (email) values (LOWER('" + username + "'))");

            if (numRows == 0) {
                throw new NoTuplesAffected();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new DuplicateUser();
        } finally {
            attemptClose(conn);
        }
    }

    @Override
    public Parameters login(String username) {
        if (!validUser(username)) {
            return null;
        }

        System.out.println("Logging in user " + username);
        Parameters ret = Parameters.create();
        ret.put("user", username);
        Integer userid = -1;
        Connection conn = null;
        try {
            conn = cpds.getConnection();
            // get the user id
            ResultSet results = conn.createStatement().executeQuery("select id from users where email=LOWER('" + username + "')");

            if (results.next()) {
                userid = results.getInt(1);
                ret.put("userid", userid);
            }

            results.close();

            String session = Users.generateSessionId();

            int numRows = conn.createStatement().executeUpdate("insert into sessions (user_id,session) values (" + userid + ",'" + session + "')");

            // should create 1 row
            if (1 == numRows) {
                ret.put("token", session);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            attemptClose(conn);
        }

        return ret;
    }

    @Override
    public void logout(Credentials creds) throws NoTuplesAffected {
        if (!validSession(creds)) {
            return;
        }
        Connection conn = null;
        try {
            conn = cpds.getConnection();
            int numRows = conn.createStatement().executeUpdate("delete from sessions where user_id=" + creds.userid + " and session='" + creds.token + "'");

            if (numRows == 0) {
                // since validSession was true,
                // this probably should have worked, barring race conditions
                throw new NoTuplesAffected();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            attemptClose(conn);
        }
    }

    @Override
    public boolean validUser(String username) {
        if (username.length() > 64) {
            return false;
        }

        boolean found = false;
        Connection conn = null;
        try {
            conn = cpds.getConnection();
            ResultSet results = conn.createStatement().executeQuery("select count(*) from users where email=LOWER('" + username + "')");

            if (results.next()) {
                int numUsers = results.getInt(1);
                found = (numUsers == 1);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            attemptClose(conn);
        }
        return found;
    }

    @Override
    public boolean validSession(Credentials creds) {
        boolean found = false;
        Connection conn = null;
        try {
            conn = cpds.getConnection();
            ResultSet results = conn.createStatement().executeQuery("select (user_id,session) from sessions where user_id=" + creds.userid + " and session='" + creds.token + "'");

            if (results.next()) {
                found = true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            attemptClose(conn);
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

        Map<String, List<String>> results = new HashMap<>();

        Connection conn = null;
        try {
            conn = cpds.getConnection();

            PreparedStatement sql = conn.prepareStatement("select label_type || ':' || label_value from tags where user_id=? and resource=?");

            sql.setInt(1, creds.userid);

            for (String resource : resources) {
                List<String> tags = new ArrayList<>();
                sql.setString(2, resource);

                ResultSet tuples = sql.executeQuery();
                while (tuples.next()) {
                    String tag = tuples.getString(1);
                    tags.add(tag);
                }
                tuples.close();
                results.put(resource, tags);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            attemptClose(conn);
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

        Map<String, Map<Integer, List<String>>> results = new HashMap<>();

        Connection conn = null;
        try {
            conn = cpds.getConnection();

            PreparedStatement sql = conn.prepareStatement("SELECT user_id, label_type || ':' || label_value || '@' || tags.rating AS tag FROM tags WHERE resource LIKE ? GROUP BY user_id, tag ORDER BY user_id, tag");
            for (String resource : resources) {
                Map<Integer, List<String>> userTags = new HashMap<>();
                sql.setString(1, resource);

                ResultSet tuples = sql.executeQuery();

                Integer currentUser = -1;
                List<String> tags = new ArrayList<>();

                while (tuples.next()) {

                    Integer user = tuples.getInt(1);

                    if (currentUser == -1) {   // first time through
                        currentUser = user;
                    }

                    String tag = tuples.getString(2);

                    if (!Objects.equals(currentUser, user)) {
                        userTags.put(currentUser, tags);
                        results.put(resource, userTags); // put user/tags in results for the resource

                        currentUser = user;
                        tags = new ArrayList<>();
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
            //     getAllTagsSQL.close();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            attemptClose(conn);
        }

        return results;
    }

    @Override
    public void deleteTag(Credentials creds, String resource, String tag) throws DBError {
        checkSession(creds);
        String labelParts[] = tag.split(":");
        String labelType = null;
        String labelValue = null;
        if (labelParts.length == 1) {
            labelType = "*";
            labelValue = labelParts[0];
        } else {
            labelType = labelParts[0];
            labelValue = labelParts[1];
        }
        Connection conn = null;
        try {
            conn = cpds.getConnection();
            int numRows = conn.createStatement().executeUpdate("delete from tags where user_id= " + creds.userid
                    + " and resource='" + resource + "' and label_type='" + labelType + "' and label_value='" + labelValue + "'");

            if (numRows == 0) {
                log.info("user: '" + creds.user + "', resource: '" + resource + "', tag: '" + tag + "'");
                throw new NoTuplesAffected();
            }

        } catch (SQLException e) {
            log.info("user: '" + creds.user + "', resource: '" + resource + "', tag: '" + tag + "'");
            throw new RuntimeException(e);
        } finally {
            attemptClose(conn);
        }
    }

    @Override
    public void addTag(Credentials creds, String resource, String tag, Integer rating) throws DBError {
        checkSession(creds);

        String labelParts[] = tag.split(":");
        Connection conn = null;
        try {
            conn = cpds.getConnection();
            PreparedStatement sql = conn.prepareStatement("insert into tags (user_id,resource,label_type, label_value, rating) values (?,?,?,?,?)");

            sql.setInt(1, creds.userid);
            sql.setString(2, resource);
            sql.setInt(5, rating);

            // if there is only 1 part to the tag, use the "wildcard" for the label type
            if (labelParts.length == 1) {
                sql.setString(3, "*");
                sql.setString(4, labelParts[0]);
            } else {
                sql.setString(3, labelParts[0]);
                sql.setString(4, labelParts[1]);
            }

            int numRows = sql.executeUpdate();

            assert (numRows == 1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            attemptClose(conn);
        }
    }

    @Override
    public List<String> getResourcesForLabels(Integer userid, List<String> labels) throws DBError {
        return getResourcesForLabels(userid, labels, -1, -1);
    }

    @Override
    public List<String> getResourcesForLabels(Integer userid, List<String> labels, Integer numResults, Integer startIndex) throws DBError {

        Connection conn = null;
        try {
            conn = cpds.getConnection();

            List<String> resources = new ArrayList<>();
            Object[] objLabels = new Object[labels.size()];
            int i = 0;
            for (String label : labels) {
                objLabels[i++] = label;
            }
            ResultSet results = null;

            if (numResults == -1) {
                PreparedStatement sql = conn.prepareStatement("SELECT DISTINCT resource FROM table(x VARCHAR=?) t INNER JOIN tags ON t.x=tags.label_type || ':' || tags.label_value AND tags.user_id = ? ORDER BY resource");
                sql.setObject(1, objLabels);
                sql.setInt(2, userid);
                results = sql.executeQuery();
            } else {
                // perpared statements don't like "in(...)" clauses, hence the cryptic SQL to do this:
                //  SELECT DISTINCT resource FROM tags WHERE user LIKE ? AND tag IN (?) 
                // we need to ORDER BY to ensure the result sets will always be in the same order.
                PreparedStatement sql = conn.prepareStatement("SELECT DISTINCT resource FROM table(x VARCHAR=?) t INNER JOIN tags ON t.x=tags.label_type || ':' || tags.label_value AND tags.user_id = ? ORDER BY resource LIMIT ? OFFSET ?");

                sql.setObject(1, objLabels);
                sql.setInt(2, userid);
                sql.setInt(3, numResults);
                sql.setInt(4, startIndex);
                results = sql.executeQuery();
            }
            while (results.next()) {
                String res = results.getString(1);
                resources.add(res);
            }

            results.close();

            return resources;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            attemptClose(conn);
        }
    }

    // "borrowed" from the C3P0 examples: http://sourceforge.net/projects/c3p0/files/c3p0-src/c3p0-0.9.2.1/
    static void attemptClose(Connection o) {
        try {
            if (o != null) {
                o.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
