// BSD License (http://lemurproject.org/galago-license)
/*
 * PositionIndexReaderTest.java
 * JUnit based test
 *
 * Created on October 5, 2007, 4:38 PM
 */
package org.lemurproject.galago.core.retrieval;

import org.lemurproject.galago.core.index.disk.PositionIndexReader;
import org.lemurproject.galago.tupleflow.Utility;
import org.lemurproject.galago.core.index.disk.PositionIndexWriter;
import org.lemurproject.galago.core.retrieval.iterator.ExtentArrayIterator;
import org.lemurproject.galago.tupleflow.Parameters;
import org.lemurproject.galago.core.util.ExtentArray;
import java.io.File;
import java.io.IOException;
import junit.framework.TestCase;
import org.lemurproject.galago.core.index.AggregateReader;
import org.lemurproject.galago.core.retrieval.iterator.MovableExtentIterator;

/**
 *
 * @author trevor
 */
public class PositionIndexReaderTest extends TestCase {

    File tempPath;
    File skipPath = null;
    static int[][] dataA = {
        {5, 7, 9},
        {19, 27, 300}
    };
    static int[][] dataB = {
        {149, 15500, 30319},
        {555555, 2}
    };

    public PositionIndexReaderTest(String testName) {
        super(testName);
    }

    @Override
    public void setUp() throws Exception {
        // make a spot for the index
        tempPath = Utility.createTemporary();
        tempPath.delete();

        skipPath = Utility.createTemporary();
        skipPath.delete();

        Parameters p = new Parameters();
        p.set("filename", tempPath.toString());
        p.set("estimateDocumentCount", true);

        PositionIndexWriter writer =
                new PositionIndexWriter(new org.lemurproject.galago.tupleflow.FakeParameters(p));

        writer.processWord(Utility.fromString("a"));

        for (int[] doc : dataA) {
            writer.processDocument(doc[0]);

            for (int i = 1; i < doc.length; i++) {
                writer.processPosition(doc[i]);
            }
        }

        writer.processWord(Utility.fromString("b"));

        for (int[] doc : dataB) {
            writer.processDocument(doc[0]);

            for (int i = 1; i < doc.length; i++) {
                writer.processPosition(doc[i]);
            }
        }

        writer.close();
    }

    @Override
    public void tearDown() throws Exception {
        tempPath.delete();
        if (skipPath != null) skipPath.delete();
    }

    public void internalTestIterator(
            MovableExtentIterator termExtents,
            int[][] data) throws IOException {
        assertNotNull(termExtents);
        assertFalse(termExtents.isDone());
        assertEquals(data.length, termExtents.totalEntries());
        int totalPositions = 0;
        for (int[] doc : data) {
            assertFalse(termExtents.isDone());
            ExtentArray e = termExtents.extents();
            ExtentArrayIterator iter = new ExtentArrayIterator(e);
            totalPositions += (doc.length - 1); // first entry in doc array is docid
            for (int i = 1; i < doc.length; i++) {
                assertFalse(iter.isDone());
                assertEquals(doc[i], iter.currentBegin());
                assertEquals(doc[i] + 1, iter.currentEnd());
                iter.next();
            }
            assertTrue(iter.isDone());
            termExtents.movePast(termExtents.currentCandidate());
        }

        assertEquals(((AggregateReader.AggregateIterator) termExtents).getStatistics().nodeFrequency, totalPositions);
        assertTrue(termExtents.isDone());
    }

    public void testA() throws Exception {
        PositionIndexReader reader = new PositionIndexReader(tempPath.toString());
        MovableExtentIterator termExtents = reader.getTermExtents("a");

        internalTestIterator(termExtents, dataA);

        assertEquals(2, reader.getTermStatistics("a").nodeDocumentCount);
        assertEquals(4, reader.getTermStatistics("a").nodeFrequency);
        assertEquals(7, reader.getTermStatistics("a").collectionLength);
        assertEquals(2, reader.getTermStatistics("a").documentCount);
        reader.close();
    }

    public void testB() throws Exception {
        PositionIndexReader reader = new PositionIndexReader(tempPath.toString());
        MovableExtentIterator termExtents = reader.getTermExtents("b");

        internalTestIterator(termExtents, dataB);
        assertEquals(2, reader.getTermStatistics("b").nodeDocumentCount);
        assertEquals(3, reader.getTermStatistics("b").nodeFrequency);
        reader.close();
    }

    public void testSkipLists() throws Exception {
        // internally fill the skip file
        Parameters p = new Parameters();
        p.set("filename", skipPath.toString());
        p.set("skipping", true);
        p.set("skipDistance", 20);
        p.set("skipResetDistance", 5);
        p.set("estimateDocumentCount", true);

        PositionIndexWriter writer =
                new PositionIndexWriter(new org.lemurproject.galago.tupleflow.FakeParameters(p));

        writer.processWord(Utility.fromString("a"));
        for (int docid = 1; docid < 5000; docid += 3) {
            writer.processDocument(docid);
            for (int pos = 1; pos < ((docid/50)+2); pos++) {
                writer.processPosition(pos);
            }
        }
        writer.close();

        // Now read it
        PositionIndexReader reader = new PositionIndexReader(skipPath.toString());
        PositionIndexReader.TermExtentIterator termExtents = reader.getTermExtents("a");
        assertEquals("a", termExtents.getKeyString());

        // Read first identifier
        assertEquals(1, termExtents.currentCandidate());
        assertEquals(1, termExtents.count());

        termExtents.moveTo(7);
        assertTrue(termExtents.hasMatch(7));

        // Now move to a doc, but not one we have
        termExtents.moveTo(90);
        assertFalse(termExtents.hasMatch(90));

        // Now move forward one
        termExtents.movePast(93);
        assertEquals(94, termExtents.currentCandidate());
        assertEquals(2, termExtents.count());

        // One more time, then we read extents
        termExtents.movePast(2543);
        assertEquals(2545, termExtents.currentCandidate());
        assertEquals(51, termExtents.count());
        ExtentArray ea = termExtents.extents();
        assertEquals(2545, ea.getDocument());
        assertEquals(51, ea.size());
        for (int i = 0; i < ea.size(); i++) {
            assertEquals(i+1, ea.begin(i));
        }
        termExtents.moveTo(10005);
        assertFalse(termExtents.hasMatch(10005));
        assertTrue(termExtents.isDone());

        skipPath.delete();
        skipPath = null;
    }

    public void testCountIterator() throws Exception {
        PositionIndexReader reader = new PositionIndexReader(tempPath.toString());
        PositionIndexReader.TermCountIterator termCounts = reader.getTermCounts("b");

        assertEquals(dataB[0][0], termCounts.currentCandidate());
        assertEquals(dataB[0].length-1, termCounts.count());
        termCounts.movePast(dataB[0][0]);

        assertEquals(dataB[1][0], termCounts.currentCandidate());
        assertEquals(dataB[1].length-1, termCounts.count());

        assertEquals(2, reader.getTermStatistics("b").nodeDocumentCount);
        assertEquals(3, reader.getTermStatistics("b").nodeFrequency);

        reader.close();
    }
}
