// BSD License (http://lemurproject.org/galago-license)
package org.lemurproject.galago.core.index.geometric;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import junit.framework.TestCase;
import org.lemurproject.galago.core.index.AggregateReader.CollectionStatistics;
import org.lemurproject.galago.core.index.LengthsReader;
import org.lemurproject.galago.core.index.NamesReader;
import org.lemurproject.galago.core.parse.Document;

import org.lemurproject.galago.core.retrieval.LocalRetrieval;
import org.lemurproject.galago.core.retrieval.ScoredDocument;
import org.lemurproject.galago.core.retrieval.iterator.MovableCountIterator;
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.core.retrieval.query.StructuredQuery;
import org.lemurproject.galago.tupleflow.FakeParameters;
import org.lemurproject.galago.tupleflow.Parameters;
import org.lemurproject.galago.tupleflow.Utility;

/**
 *
 * @author sjh
 */
public class GeometricIndexTest extends TestCase {

  public GeometricIndexTest(String testName) {
    super(testName);
  }

  public void testProcessDocuments() throws Exception {
    PrintStream oldErr = System.err;
    PrintStream newErr = new PrintStream(new ByteArrayOutputStream());
    System.setErr(newErr);

    File shards = Utility.createTemporaryDirectory();
    try {
      Parameters p = new Parameters();
      p.set("indexBlockSize", 50);
      p.set("shardDirectory", shards.getAbsolutePath());
      GeometricIndex index = new GeometricIndex(new FakeParameters(p));

      for (int i = 0; i < 255; i++) {
        Document d = new Document();
        d.name = "DOC-" + i;
        d.text = "this is sample document " + i;
        d.terms = Arrays.asList(d.text.split(" "));
        d.tags = new ArrayList();
        d.metadata = new HashMap();

        index.process(d);
      }

      assertTrue(index.globalDocumentCount == 255);
      CollectionStatistics stats = index.getCollectionStatistics("postings");
      assertTrue(stats.collectionLength == 1275);
      assertTrue(stats.documentCount == 255);

      NamesReader.Iterator names = index.getNamesIterator();
      names.moveTo(99);
      assertEquals(names.getCurrentName(), "DOC-" + 99);
      names.movePast(99);
      assertEquals(names.getCurrentName(), "DOC-" + 100);

      LengthsReader.Iterator lengths = index.getLengthsIterator();
      lengths.moveTo(99);
      assertEquals(lengths.getCurrentIdentifier(), 99);
      assertEquals(lengths.getCurrentLength(), 5);
      lengths.movePast(99);
      assertEquals(lengths.getCurrentIdentifier(), 100);
      assertEquals(lengths.getCurrentLength(), 5);

      Node q1 = StructuredQuery.parse("#counts:sample:part=postings()");
      MovableCountIterator ci1 = (MovableCountIterator) index.getIterator(q1);
      assert ci1 != null;
      ci1.moveTo(99);
      assertEquals(ci1.currentCandidate(), 99);
      assertEquals(ci1.count(), 1);
      ci1.movePast(99);
      assertEquals(ci1.currentCandidate(), 100);
      assertEquals(ci1.count(), 1);

      Node q2 = StructuredQuery.parse("#counts:@/101/:part=postings()");
      MovableCountIterator ci2 = (MovableCountIterator) index.getIterator(q2);
      assertEquals(ci2.currentCandidate(), 101);
      assertEquals(ci2.count(), 1);
      ci2.movePast(101);
      assert (ci2.isDone());
      ci2.reset();
      assertEquals(ci2.currentCandidate(), 101);
      assertEquals(ci2.count(), 1);
      ci2.movePast(101);
      assert (ci2.isDone());

      index.close();

    } finally {
      Utility.deleteDirectory(shards);
      System.setErr(oldErr);
    }
  }

  public void testRetrievalFunctions() throws Exception {
    PrintStream oldErr = System.err;
    PrintStream newErr = new PrintStream(new ByteArrayOutputStream());
    System.setErr(newErr);
    File shards = Utility.createTemporaryDirectory();

    Random rnd = new Random();
    try {
      Parameters p = new Parameters();
      p.set("indexBlockSize", 50);
      p.set("shardDirectory", shards.getAbsolutePath());
      p.set("requested", 10);
      GeometricIndex index = new GeometricIndex(new FakeParameters(p));
      LocalRetrieval ret = new LocalRetrieval(index);

      for (int i = 0; i < 255; i++) {

        Document d = new Document();
        d.name = "DOC-" + i;
        d.text = "this is sample document " + i;
        d.terms = Arrays.asList(d.text.split(" "));
        d.tags = new ArrayList();
        d.metadata = new HashMap();

        index.process(d);
        if (i > 0) {
          int j = rnd.nextInt(i);
          Node query = StructuredQuery.parse("sample " + j);
          query = ret.transformQuery(query, p);

          ScoredDocument[] results = ret.runQuery(query, p);
          assert (results[0].documentName.contains(Integer.toString(j)));
        }
      }
    } finally {
      Utility.deleteDirectory(shards);
      System.setErr(oldErr);
    }
  }
}
