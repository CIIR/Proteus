package ciir.proteus.users.http;

import ciir.proteus.server.TestEnvironment;
import ciir.proteus.users.Credentials;
import ciir.proteus.users.error.DBError;
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

/**
 *
 * @author michaelz
 */
public class GetResourcesForLabelsTest {

    public static TestEnvironment env;
    private static boolean PARANOID = false; // set to true to verify existing functionalidy

    @BeforeClass
    public static void setup() throws IOException, WebServerException, NoTuplesAffected {
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
        String token = env.proteus.userdb.login(user);
        assertNotNull(token);

        Credentials cred = new Credentials(user, token);

        // test one label
        env.proteus.userdb.addTag(cred, "res1", "type1:value1");
        // assuming existing functionality works.  
        if (PARANOID) {
            List<String> res1tags = env.proteus.userdb.getTags(cred, "res1");
            Collections.sort(res1tags); // don't depend on db order
            assertArrayEquals(new String[]{"type1:value1"}, res1tags.toArray());
        }

        List<String> labels = new ArrayList<String>();
        labels.add("type1:value1");
        List<String> resources = env.proteus.userdb.getResourcesForLabels(user, labels);
        assertArrayEquals(new String[]{"res1"}, resources.toArray());

        // add new label to search for - but doesn't have a resource assocaiated
        // (should never happen, but just for fun)
        labels.add("type1:value2");
        resources = env.proteus.userdb.getResourcesForLabels(user, labels);
        assertArrayEquals(new String[]{"res1"}, resources.toArray());

        // now add that label
        env.proteus.userdb.addTag(cred, "res1", "type1:value2");
        // assuming existing functionality works.  
        if (PARANOID) {
            List<String> res1tags = env.proteus.userdb.getTags(cred, "res1");
            Collections.sort(res1tags); // don't depend on db order
            assertArrayEquals(new String[]{"type1:value1", "type1:value2"}, res1tags.toArray());
        }
        // adding a duplicate label - should only return one resource
        labels.add("type1:value2");
        resources = env.proteus.userdb.getResourcesForLabels(user, labels);
        assertArrayEquals(new String[]{"res1"}, resources.toArray());

        // check that a 2nd user will not get our resources
        String user2 = "new_user";
        env.proteus.userdb.register(user2);
        String token2 = env.proteus.userdb.login(user2);
        assertNotNull(token2);

        resources = env.proteus.userdb.getResourcesForLabels(user2, labels);
        assertEquals(0, resources.size());

        // add some new resources that should NOT get returned for the labels passed in
        env.proteus.userdb.addTag(cred, "res2", "type1:don't look for me");
        if (PARANOID) {
            List<String> res1tags = env.proteus.userdb.getTags(cred, "res2");
            Collections.sort(res1tags); // don't depend on db order
            assertArrayEquals(new String[]{"type1:don't look for me"}, res1tags.toArray());
        }

        resources = env.proteus.userdb.getResourcesForLabels(user, labels);
        assertArrayEquals(new String[]{"res1"}, resources.toArray());

        // now label another resource with an existing tag
        env.proteus.userdb.addTag(cred, "res3", "type1:value1");
        // assuming existing functionality works.  
        if (PARANOID) {
            List<String> res1tags = env.proteus.userdb.getTags(cred, "res3");
            Collections.sort(res1tags); // don't depend on db order
            assertArrayEquals(new String[]{"type1:value1"}, res1tags.toArray());
        }

        resources = env.proteus.userdb.getResourcesForLabels(user, labels);
        assertArrayEquals(new String[]{"res1", "res3"}, resources.toArray());

    }

}
