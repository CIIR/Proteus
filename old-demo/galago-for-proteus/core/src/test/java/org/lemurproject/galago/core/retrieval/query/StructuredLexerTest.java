// BSD License (http://lemurproject.org/galago-license)

package org.lemurproject.galago.core.retrieval.query;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import junit.framework.TestCase;
import org.lemurproject.galago.core.retrieval.query.StructuredLexer.Token;

/**
 *
 * @author trevor
 */
public class StructuredLexerTest extends TestCase {
    
    public StructuredLexerTest(String testName) {
        super(testName);
    }

    public void testTokens() throws Exception {
        StructuredLexer lexer = new StructuredLexer();
        List<Token> tokens = lexer.tokens("#op:this=that:a( b c d ).e");
        Iterator<Token> iterator = tokens.iterator();

        Token t = iterator.next();
        assertEquals("#", t.text);
        assertEquals(0, t.position);

        t = iterator.next();
        assertEquals("op", t.text);
        assertEquals(1, t.position);

        t = iterator.next();
        assertEquals(":", t.text);
        assertEquals(3, t.position);

        t = iterator.next();
        assertEquals("this", t.text);
        assertEquals(4, t.position);

        t = iterator.next();
        assertEquals("=", t.text);
        assertEquals(8, t.position);

        t = iterator.next();
        assertEquals("that", t.text);
        assertEquals(9, t.position);

        t = iterator.next();
        assertEquals(":", t.text);
        assertEquals(13, t.position);

        t = iterator.next();
        assertEquals("a", t.text);
        assertEquals(14, t.position);

        t = iterator.next();
        assertEquals("(", t.text);
        assertEquals(15, t.position);

        t = iterator.next();
        assertEquals("b", t.text);
        assertEquals(17, t.position);

        t = iterator.next();
        assertEquals("c", t.text);
        assertEquals(19, t.position);

        t = iterator.next();
        assertEquals("d", t.text);
        assertEquals(21, t.position);

        t = iterator.next();
        assertEquals(")", t.text);
        assertEquals(23, t.position);

        t = iterator.next();
        assertEquals(".", t.text);
        assertEquals(24, t.position);

        t = iterator.next();
        assertEquals("e", t.text);
        assertEquals(25, t.position);

        assertFalse(iterator.hasNext());
    }

    public void testQuotes() throws IOException {
        StructuredLexer lexer = new StructuredLexer();
        List<Token> tokens = lexer.tokens("\"b  cf d  \"");
        Iterator<Token> iterator = tokens.iterator();

        Token t = iterator.next();
        assertEquals("\"", t.text);
        assertEquals(0, t.position);

        t = iterator.next();
        assertEquals("b", t.text);
        assertEquals(1, t.position);

        t = iterator.next();
        assertEquals("cf", t.text);
        assertEquals(4, t.position);

        t = iterator.next();
        assertEquals("d", t.text);
        assertEquals(7, t.position);

        t = iterator.next();
        assertEquals("\"", t.text);
        assertEquals(10, t.position);
    }

    public void testEscapes() throws IOException {
        StructuredLexer lexer = new StructuredLexer();
        List<Token> tokens = lexer.tokens("@/b c d/");
        Iterator<Token> iterator = tokens.iterator();

        Token t = iterator.next();
        assertEquals("b c d", t.text);
        assertEquals(0, t.position);
    }
}
