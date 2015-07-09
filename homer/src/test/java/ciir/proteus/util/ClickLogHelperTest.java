/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ciir.proteus.util;

import java.util.*;

import ciir.proteus.util.logging.ClickLogHelper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.lemurproject.galago.core.retrieval.ScoredDocument;
import org.lemurproject.galago.utility.Parameters;

/**
 *
 * @author michaelz
 */
public class ClickLogHelperTest {

    public ClickLogHelperTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getID method, of class ClickLogHelper.
     */
    @Test
    public void testGetID() {

        Parameters reqp = Parameters.create();

        // both input null
        String result = ClickLogHelper.getID(null, null);
        assertEquals(null, result);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest();

        // null parameters, have IP
        result = ClickLogHelper.getID(null, mockRequest);
        assertEquals("1.2.3.4", result);

        reqp.set("dummy", "value");

        // request null, no IP or token
        result = ClickLogHelper.getID(reqp, null);
        assertEquals(null, result);

        // non-null parameters, no token, have IP
        result = ClickLogHelper.getID(reqp, mockRequest);
        assertEquals("1.2.3.4", result);

        // have token, no IP
        reqp.set("token", "abc123");
        result = ClickLogHelper.getID(reqp, null);
        assertEquals("abc123", result);

        // have both token and IP
        result = ClickLogHelper.getID(reqp, mockRequest);
        assertEquals("abc123", result);

    }

    /**
     * Test of extractDocID method, of class ClickLogHelper.
     */
    @Test
    public void testExtractDocID() {
        System.out.println("extractDocID");
        List<Object> list = null;
        List<String> expResult = null;

        // null input
        List<String> result = ClickLogHelper.extractDocID(null);
        assertEquals(Collections.emptyList(), result);

        // list of wrong object type
        List<String> strings = Arrays.asList("foo", "bar", "baz");

        try {
            result = ClickLogHelper.extractDocID(strings);
            fail("Should throw exception");
        } catch (Exception e) {

        }

        List<ScoredDocument> docs = new ArrayList<>();
        docs.add(new ScoredDocument("doc1", 1, 0.0));
        docs.add(new ScoredDocument("doc2", 2, 0.0));
        docs.add(new ScoredDocument("doc3", 3, 0.0));
        result = ClickLogHelper.extractDocID(docs);
        assertArrayEquals(new String[]{"doc1", "doc2", "doc3"}, result.toArray());

        List<Parameters> pDocs = new ArrayList<>();
        Parameters p1 = Parameters.create();
        p1.set("key", "value");
        pDocs.add(p1);
        Parameters p2 = Parameters.create();
        p2.set("name", "doc1");
        p2.set("dummy", "value");
        pDocs.add(p2);
        Parameters p3 = Parameters.create();
        p3.set("a", "123");
        p3.set("name", "doc2");
        pDocs.add(p3);
        result = ClickLogHelper.extractDocID(pDocs);
        assertArrayEquals(new String[]{"doc1", "doc2"}, result.toArray());

    }

}
