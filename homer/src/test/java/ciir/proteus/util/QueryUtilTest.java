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
    assertEquals(3, terms.size());
    assertTrue(terms.contains("max"));
    assertTrue(terms.contains("d"));
    assertTrue(terms.contains("cat"));

    // test phrase
    // "pine tree shilling"
    query = StructuredQuery.parse("#combine( #ordered:1( #text:pine() #text:tree() #text:shilling() ) )");
    terms = QueryUtil.queryTerms(query);
    assertEquals(1, terms.size());
    assertTrue(terms.contains("pine tree shilling"));

    // currently, we assume all phrases are ordered window 1
    query = StructuredQuery.parse("#combine( #ordered:3( #text:pine() #text:tree() #text:shilling() ) )");
    terms = QueryUtil.queryTerms(query);
    assertEquals(1, terms.size());
    assertTrue(terms.contains("pine tree shilling"));

    // treat unordered as a regular query
    query = StructuredQuery.parse("#combine( #unordered:2( #text:pine() #text:tree() #text:shilling() ) )");
    terms = QueryUtil.queryTerms(query);
    assertEquals(3, terms.size());
    assertTrue(terms.contains("pine"));
    assertTrue(terms.contains("tree"));
    assertTrue(terms.contains("shilling"));

    // test field search
    // organization:"pan american union"
    query = StructuredQuery.parse("#combine( #inside( #ordered:1( #text:pan() #text:american() #text:union() ) #field:organization() ) )");
    terms = QueryUtil.queryTerms(query);
    assertEquals(1, terms.size());
    assertTrue(terms.contains("pan american union"));

    // organization:"pan"
    query = StructuredQuery.parse("#combine( #inside( #text:pan() #field:organization() ) )");
    terms = QueryUtil.queryTerms(query);
    assertEquals(1, terms.size());
    assertTrue(terms.contains("pan"));

    // organization:pan (no double quotes)
    query = StructuredQuery.parse("#combine( #inside( #text:pan() #field:organization() ) )");
    terms = QueryUtil.queryTerms(query);
    assertEquals(1, terms.size());
    assertTrue(terms.contains("pan"));

    // person:"charles" location:"new england"
    query = StructuredQuery.parse("#combine( #inside( #text:charles() #field:person() ) #inside( #ordered:1( #text:new() #text:england() ) #field:location() ) )");
    terms = QueryUtil.queryTerms(query);
    assertEquals(2, terms.size());
    assertTrue(terms.contains("charles"));
    assertTrue(terms.contains("new england"));

    query = StructuredQuery.parse("#combine:w=1.0( #extents:charles:part=field.krovetz.person() )");
    terms = QueryUtil.queryTerms(query);
    assertEquals(1, terms.size());
    assertTrue(terms.contains("charles"));

    query = StructuredQuery.parse("#combine:w=1.0( #inside( #ordered:1( #extents:new:part=postings.krovetz() #extents:england:part=postings.krovetz() ) #extents:location:part=extents() ) )");
    terms = QueryUtil.queryTerms(query);
    assertEquals(1, terms.size());
    assertTrue(terms.contains("new england"));

    query = StructuredQuery.parse("#combine:w=1.0( #extents:charles:part=field.krovetz.person() #inside( #ordered:1( #extents:new:part=postings.krovetz() #extents:england:part=postings.krovetz() ) #extents:location:part=extents() ) )");
    terms = QueryUtil.queryTerms(query);
    assertEquals(2, terms.size());
    assertTrue(terms.contains("charles"));
    assertTrue(terms.contains("new england"));

    // field search using extents - single term. Prior versions were returning two terms: "location" and "england"
    query = StructuredQuery.parse("#combine:w=1.0( #inside( #extents:england:part=postings.krovetz() #extents:location:part=extents() ) )");
    terms = QueryUtil.queryTerms(query);
    assertEquals(1, terms.size());
    assertTrue(terms.contains("england"));

  }
}