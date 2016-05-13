package ciir.proteus.util;

import org.junit.Test;
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.core.retrieval.query.SimpleQuery;
import org.lemurproject.galago.core.retrieval.query.StructuredQuery;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by michaelz on 5/10/2016.
 */
public class QueryUtilTest {

  @Test
  public void testQueryTerms() throws Exception {

    List<String> terms = new ArrayList<>();
    // simple query: max d cat
    Node query = StructuredQuery.parse("#combine( #text:max() #text:d() #text:cat() )");
    terms = QueryUtil.queryTerms(query);
    assertEquals(terms.size(), 3);
    assertTrue(terms.contains("max"));
    assertTrue(terms.contains("d"));
    assertTrue(terms.contains("cat"));

    // test phrase
    // "pine tree shilling"
    query = StructuredQuery.parse("#combine( #ordered:1( #text:pine() #text:tree() #text:shilling() ) )");
    terms = QueryUtil.queryTerms(query);
    assertEquals(terms.size(), 1);
    assertTrue(terms.contains("pine tree shilling"));

    // currently, we assume all phrases are ordered window 1
    query = StructuredQuery.parse("#combine( #ordered:3( #text:pine() #text:tree() #text:shilling() ) )");
    terms = QueryUtil.queryTerms(query);
    assertEquals(terms.size(), 1);
    assertTrue(terms.contains("pine tree shilling"));

    // treat unordered as a regular query
    query = StructuredQuery.parse("#combine( #unordered:2( #text:pine() #text:tree() #text:shilling() ) )");
    terms = QueryUtil.queryTerms(query);
    assertEquals(terms.size(), 3);
    assertTrue(terms.contains("pine"));
    assertTrue(terms.contains("tree"));
    assertTrue(terms.contains("shilling"));

    // test field search
    // organization:"pan american union"
    query = StructuredQuery.parse("#combine( #inside( #ordered:1( #text:pan() #text:american() #text:union() ) #field:organization() ) )");
    terms = QueryUtil.queryTerms(query);
    assertEquals(terms.size(), 1);
    assertTrue(terms.contains("pan american union"));

    // person:"charles" location:"new england"
    query = StructuredQuery.parse("#combine( #inside( #text:charles() #field:person() ) #inside( #ordered:1( #text:new() #text:england() ) #field:location() ) )");

    Node tq = SimpleQuery.parseTree("person:\"charles\" location:\"new england\"");
    terms = QueryUtil.queryTerms(query);
    terms = QueryUtil.queryTerms(tq);
    assertEquals(terms.size(), 2);
    assertTrue(terms.contains("charles"));
    assertTrue(terms.contains("new england"));

  }
}