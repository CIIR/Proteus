package ciir.proteus.users.http;

import ciir.proteus.server.TestEnvironment;
import ciir.proteus.users.Credentials;
import ciir.proteus.users.error.DBError;
import ciir.proteus.users.error.DuplicateUser;
import ciir.proteus.users.error.NoTuplesAffected;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.lemurproject.galago.tupleflow.web.WebServerException;
import org.lemurproject.galago.utility.Parameters;

/**
 *
 * @author michaelz
 */
public class GetResourcesForLabelsTest {

    public static TestEnvironment env;
    private static boolean PARANOID = true; // set to true to verify existing functionalidy

    @BeforeClass
    public static void setup() throws IOException, WebServerException, NoTuplesAffected, DuplicateUser, Exception {
        env = new TestEnvironment();
    }

    @AfterClass
    public static void tearDown() throws IOException, WebServerException {
        env.close();
    }

    @Test
    public void getResourcesTest() throws DBError {
        String user = "maxdcat";
        env.proteus.userdb.register(user);
        Parameters p = env.proteus.userdb.login(user);

        Credentials cred = new Credentials(p);
        int userid = cred.userid;

        // test one label
        env.proteus.userdb.addTag(cred, "res1", "type1:value1", 5);
        // assuming existing functionality works.  
        if (PARANOID) {
            List<String> res1tags = env.proteus.userdb.getTags(cred, "res1");
            Collections.sort(res1tags); // don't depend on db order
            assertArrayEquals(new String[]{"type1:value1"}, res1tags.toArray());
        }

        List<String> labels = new ArrayList<>();
        labels.add("type1:value1");
        List<String> resources = env.proteus.userdb.getResourcesForLabels(userid, labels, 10, 0);
        assertArrayEquals(new String[]{"res1"}, resources.toArray());

        // add new label to search for - but doesn't have a resource assocaiated
        // (should never happen, but just for fun)
        labels.add("type1:value2");
        resources = env.proteus.userdb.getResourcesForLabels(userid, labels, 10, 0);
        assertArrayEquals(new String[]{"res1"}, resources.toArray());

        // now add that label
        env.proteus.userdb.addTag(cred, "res1", "type1:value2", 5);
        // assuming existing functionality works.  
        if (PARANOID) {
            List<String> res1tags = env.proteus.userdb.getTags(cred, "res1");
            Collections.sort(res1tags); // don't depend on db order
            assertArrayEquals(new String[]{"type1:value1", "type1:value2"}, res1tags.toArray());
        }
        // adding a duplicate label - should only return one resource
        labels.add("type1:value2");
        resources = env.proteus.userdb.getResourcesForLabels(userid, labels, 10, 0);
        assertArrayEquals(new String[]{"res1"}, resources.toArray());

        // check that a 2nd user will not get our resources
        String user2 = "new_user";
        env.proteus.userdb.register(user2);
        Parameters p2 = env.proteus.userdb.login(user2);

        resources = env.proteus.userdb.getResourcesForLabels((Integer) p2.get("userid"), labels, 10, 0);
        assertEquals(0, resources.size());

        // add some new resources that should NOT get returned for the labels passed in
        env.proteus.userdb.addTag(cred, "res2", "type1:don't look for me", 0);
        if (PARANOID) {
            List<String> res1tags = env.proteus.userdb.getTags(cred, "res2");
            Collections.sort(res1tags); // don't depend on db order
            assertArrayEquals(new String[]{"type1:don't look for me"}, res1tags.toArray());
        }

        resources = env.proteus.userdb.getResourcesForLabels(userid, labels, 10, 0);
        assertArrayEquals(new String[]{"res1"}, resources.toArray());

        // now label another resource with an existing tag
        env.proteus.userdb.addTag(cred, "res3", "type1:value1", 5);
        // assuming existing functionality works.  
        if (PARANOID) {
            List<String> res1tags = env.proteus.userdb.getTags(cred, "res3");
            Collections.sort(res1tags); // don't depend on db order
            assertArrayEquals(new String[]{"type1:value1"}, res1tags.toArray());
        }

        resources = env.proteus.userdb.getResourcesForLabels(userid, labels, 10, 0);
        assertArrayEquals(new String[]{"res1", "res3"}, resources.toArray());

        // test number of results and start index - results are sorted by RESOURCE 
        for (int i = 1; i <= 5; i++) {
            // insert new resources for a single label "0"
            String tmp = Integer.toString(i);
            env.proteus.userdb.addTag(cred, "0-" + tmp, "type:0", 5);
            // assuming existing functionality works.  
            if (PARANOID) {
                List<String> res1tags = env.proteus.userdb.getTags(cred, "0-" + tmp);
                Collections.sort(res1tags); // don't depend on db order
                assertArrayEquals(new String[]{"type:0"}, res1tags.toArray());
            }
        }

        labels.clear();
        labels.add("type:0");
        resources = env.proteus.userdb.getResourcesForLabels(userid, labels, 1, 0);
        assertArrayEquals(new String[]{"0-1"}, resources.toArray());

        resources = env.proteus.userdb.getResourcesForLabels(userid, labels, 1, 1);
        assertArrayEquals(new String[]{"0-2"}, resources.toArray());

        resources = env.proteus.userdb.getResourcesForLabels(userid, labels, 2, 3);
        assertArrayEquals(new String[]{"0-4", "0-5"}, resources.toArray());

        // add uniq resources for another label ("1")
        for (int i = 1; i <= 5; i++) {

            String tmp = Integer.toString(i);
            env.proteus.userdb.addTag(cred, "1-" + tmp, "type:1", i);
            // assuming existing functionality works.  
            if (PARANOID) {
                List<String> res1tags = env.proteus.userdb.getTags(cred, "1-" + tmp);
                Collections.sort(res1tags); // don't depend on db order
                assertArrayEquals(new String[]{"type:1"}, res1tags.toArray());
            }
        }

        labels.add("type:1");
        resources = env.proteus.userdb.getResourcesForLabels(userid, labels, 1, 0);
        assertArrayEquals(new String[]{"0-1"}, resources.toArray());

        resources = env.proteus.userdb.getResourcesForLabels(userid, labels, 2, 4);
        assertArrayEquals(new String[]{"0-5", "1-1"}, resources.toArray());

        // only 10 entries but we're asking for 20 starting at index 9
        resources = env.proteus.userdb.getResourcesForLabels(userid, labels, 20, 9);
        assertArrayEquals(new String[]{"1-5"}, resources.toArray());

        // test version where we return everything
        resources = env.proteus.userdb.getResourcesForLabels(userid, labels);
        assertEquals(resources.size(), 10);

    }

}
