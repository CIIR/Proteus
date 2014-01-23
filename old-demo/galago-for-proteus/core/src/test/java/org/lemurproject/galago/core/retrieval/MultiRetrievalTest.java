/*
 *  BSD License (http://lemurproject.org/galago-license)
 */
package org.lemurproject.galago.core.retrieval;

import org.lemurproject.galago.core.retrieval.MultiRetrieval;
import org.lemurproject.galago.core.retrieval.ScoredDocument;
import org.lemurproject.galago.core.retrieval.RetrievalFactory;
import java.io.File;
import java.util.Arrays;
import junit.framework.TestCase;
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.core.retrieval.query.StructuredQuery;
import org.lemurproject.galago.core.tools.App;
import org.lemurproject.galago.core.tools.AppTest;
import org.lemurproject.galago.tupleflow.Parameters;
import org.lemurproject.galago.tupleflow.Utility;

/**
 *
 * @author sjh
 */
public class MultiRetrievalTest extends TestCase {

  public MultiRetrievalTest(String name) {
    super(name);
  }

  public void testMultiRetrieval() throws Exception {
    File trecCorpusFile1 = null;
    File trecCorpusFile2 = null;
    File index1 = null;
    File index2 = null;

    try {
      // create index 1
      String trecCorpus = AppTest.trecDocument("i1-55", "This is a sample document")
              + AppTest.trecDocument("i1-59", "sample document two");

      trecCorpusFile1 = Utility.createTemporary();
      Utility.copyStringToFile(trecCorpus, trecCorpusFile1);

      index1 = Utility.createTemporaryDirectory();
      App.main(new String[]{"build", "--indexPath=" + index1.getAbsolutePath(),
                "--inputPath=" + trecCorpusFile1.getAbsolutePath()});

      // create index 2
      trecCorpus = AppTest.trecDocument("i2-55", "This is a sample also a document")
              + AppTest.trecDocument("i2-59", "sample document four long");

      trecCorpusFile2 = Utility.createTemporary();
      Utility.copyStringToFile(trecCorpus, trecCorpusFile2);

      index2 = Utility.createTemporaryDirectory();
      App.main(new String[]{"build", "--indexPath=" + index2.getAbsolutePath(),
                "--inputPath=" + trecCorpusFile2.getAbsolutePath()});

      Parameters params = new Parameters();
      String[] indexes = {index1.getAbsolutePath(), index2.getAbsolutePath()};
      params.set("index", Arrays.asList(indexes));
      MultiRetrieval mr = (MultiRetrieval) RetrievalFactory.instance(params);      
      String query = "#combine( sample document )";
      Node parsedQuery = StructuredQuery.parse(query);
      Parameters qp = new Parameters();
      Node queryTree = mr.transformQuery(parsedQuery, qp);

      assertEquals(queryTree.toString(), "#combine( #feature:dirichlet:collectionProbability=0.21052631578947367( #counts:sample:part=postings.porter() ) #feature:dirichlet:collectionProbability=0.21052631578947367( #counts:document:part=postings.porter() ) )");

      ScoredDocument[] res = mr.runQuery(queryTree, qp);

      String[] expected = {"i1-59	1	-1.5569809573716442",
        "i2-59	1	-1.5576460721284549",
        "i1-55	2	-1.5583107448016458",
        "i2-55	2	-1.5596387662451652"
      };

      for (int i = 0; i < res.length; i++) {
        String r = res[i].documentName + "\t" + res[i].rank + "\t" + res[i].score;
        assertEquals(r, expected[i]);
      }

    } finally {

      if (trecCorpusFile1 != null) {
        trecCorpusFile1.delete();
      }
      if (trecCorpusFile2 != null) {
        trecCorpusFile2.delete();
      }
      if (index1 != null) {
        Utility.deleteDirectory(index1);
      }
      if (index2 != null) {
        Utility.deleteDirectory(index2);
      }
    }
  }
}
