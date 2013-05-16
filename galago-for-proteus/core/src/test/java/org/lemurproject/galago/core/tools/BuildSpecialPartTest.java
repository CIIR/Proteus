// BSD License (http://lemurproject.org/galago-license)
package org.lemurproject.galago.core.tools;

import org.lemurproject.galago.core.tools.App;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.HashMap;
import junit.framework.TestCase;
import org.lemurproject.galago.core.index.disk.DocumentIndicatorReader;
import org.lemurproject.galago.core.index.disk.DocumentPriorReader;
import org.lemurproject.galago.tupleflow.Utility;

/**
 *
 * @author trevor
 */
public class BuildSpecialPartTest extends TestCase {

  public BuildSpecialPartTest(String testName) {
    super(testName);
  }

  public static String trecDocument(String docno, String text) {
    return "<DOC>\n<DOCNO>d" + docno + "</DOCNO>\n"
            + "<TEXT>\n" + text + "</TEXT>\n</DOC>\n";
  }

  public void testIndicators() throws Exception {
    File trecCorpusFile = null;
    File indicatorFile = null;
    File indexFile = null;
    File queryFile = null;

    try {
      // create a simple doc file, trec format:
      String trecCorpus = trecDocument("10", "sample document four")
              + trecDocument("11", "sample document five")
              + trecDocument("55", "This is a sample document")
              + trecDocument("59", "sample document two")
              + trecDocument("73", "sample document three");
      trecCorpusFile = Utility.createTemporary();
      Utility.copyStringToFile(trecCorpus, trecCorpusFile);

      String indicators = "d1\n"
              + "d5\n"
              + "d55\ttrue\n"
              + "d59\tfalse\n"
              + "d10\n";

      indicatorFile = Utility.createTemporary();
      Utility.copyStringToFile(indicators, indicatorFile);


      // now, try to build an index from that
      indexFile = Utility.createTemporaryDirectory();
      App.main(new String[]{"build", "--indexPath=" + indexFile.getAbsolutePath(),
                "--inputPath=" + trecCorpusFile.getAbsolutePath()});

      App.main(new String[]{"build-special", "--indexPath=" + indexFile.getAbsolutePath(),
                "--inputPath=" + indicatorFile.getAbsolutePath(), "--type=indicator",
                "--partName=testingIndicators"});

      DocumentIndicatorReader reader = new DocumentIndicatorReader(indexFile.getAbsolutePath() + File.separator + "testingIndicators");

      String output = "0	true\n"
                    + "2	true\n"
                    + "3	false\n";

      DocumentIndicatorReader.KeyIterator iterator = reader.getIterator();
      StringBuilder correct = new StringBuilder();
      do {
        correct.append(iterator.getCurrentDocument()).append("\t").append(iterator.getCurrentIndicator()).append("\n");
      } while (iterator.nextKey());

      assert output.equals(correct.toString());

      // now test a query:
      String queries =
              "{ \"queries\" : [\n"
              + "{ \"number\" : \"1\", \"text\" : \"sample\" },\n"
              + "{ \"number\" : \"2\", \"text\" : \"#require( #indicator:part=testingIndicators() #combine( sample ) )\" }\n"
              + "]}\n";
      queryFile = Utility.createTemporary();
      Utility.copyStringToFile(queries, queryFile);

      // test with batch search
      ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
      PrintStream printStream = new PrintStream(byteArrayStream);

      new App().run(new String[]{"batch-search",
                "--index=" + indexFile.getAbsolutePath(),
                queryFile.getAbsolutePath()}, printStream);

      // Now, verify that some stuff exists
      String out = byteArrayStream.toString();

      String expected = "1 Q0 d10 1 -1.22350933 galago\n"
              + "1 Q0 d11 2 -1.22350933 galago\n"
              + "1 Q0 d59 3 -1.22350933 galago\n"
              + "1 Q0 d73 4 -1.22350933 galago\n"
              + "1 Q0 d55 5 -1.22483912 galago\n"
              + "2 Q0 d10 1 -1.22350933 galago\n"
              + "2 Q0 d55 2 -1.22483912 galago\n";

      assertEquals(expected, out);

    } finally {
      if (trecCorpusFile != null) {
        trecCorpusFile.delete();
      }
      if (indicatorFile != null) {
        indicatorFile.delete();
      }
      if (queryFile != null) {
        queryFile.delete();
      }
      if (indexFile != null) {
        Utility.deleteDirectory(indexFile);
      }
    }
  }

 
  public void testPriors() throws Exception {
    File trecCorpusFile = null;
    File priorFile = null;
    File indexFile = null;
    File queryFile = null;

    try {
      // create a simple doc file, trec format:
      String trecCorpus = trecDocument("10", "sample document four")
              + trecDocument("11", "sample document five")
              + trecDocument("55", "This is a sample document")
              + trecDocument("59", "sample document two")
              + trecDocument("73", "sample document three");
      trecCorpusFile = Utility.createTemporary();
      Utility.copyStringToFile(trecCorpus, trecCorpusFile);

      String priors = 
                "d10\t-23.0259\n"
              + "d11\t-1e-10\n"
              + "d59\t-7.0\n"
              + "d73\t-6.0\n";

      priorFile = Utility.createTemporary();
      Utility.copyStringToFile(priors, priorFile);


      // now, try to build an index from that
      indexFile = Utility.createTemporary();
      indexFile.delete();
      App.main(new String[]{"build", "--indexPath=" + indexFile.getAbsolutePath(),
                "--inputPath=" + trecCorpusFile.getAbsolutePath()});

      App.main(new String[]{"build-special", "--indexPath=" + indexFile.getAbsolutePath(),
                "--inputPath=" + priorFile.getAbsolutePath(), "--type=prior",
                "--partName=testingPriors"});

      DocumentPriorReader reader = new DocumentPriorReader(indexFile.getAbsolutePath() + File.separator + "testingPriors");

      HashMap<Integer, Double> priorData = new HashMap();
      priorData.put(0, -23.0259);
      priorData.put(1, -1e-10);
      priorData.put(3, -7.0);
      priorData.put(4, -6.0);

      DocumentPriorReader.KeyIterator iterator = reader.getIterator();
      do {
        int doc = iterator.getCurrentDocument();
        double score = iterator.getCurrentScore();
        assert (priorData.get(doc) == score);
      } while (iterator.nextKey());

      // now test a query:
      String queries =
              "{ \"queries\" : [\n"
              + "{ \"number\" : \"1\", \"text\" : \"sample\" },\n"
              + "{ \"number\" : \"2\", \"text\" : \"#combine( #prior:part=testingPriors() sample )\" }\n"
              + "]}\n";
      queryFile = Utility.createTemporary();
      Utility.copyStringToFile(queries, queryFile);

      // test with batch search
      ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
      PrintStream printStream = new PrintStream(byteArrayStream);

      new App().run(new String[]{"batch-search",
                "--index=" + indexFile.getAbsolutePath(),
                queryFile.getAbsolutePath()}, printStream);

      // Now, verify that some stuff exists
      String out = byteArrayStream.toString();

      String expected = "1 Q0 d10 1 -1.22350933 galago\n"
              + "1 Q0 d11 2 -1.22350933 galago\n"
              + "1 Q0 d59 3 -1.22350933 galago\n"
              + "1 Q0 d73 4 -1.22350933 galago\n"
              + "1 Q0 d55 5 -1.22483912 galago\n"
              + "2 Q0 d11 1 -0.61175467 galago\n"
              + "2 Q0 d73 2 -3.61175467 galago\n"
              + "2 Q0 d59 3 -4.11175467 galago\n"
              + "2 Q0 d10 4 -12.12470467 galago\n"
              + "2 Q0 d55 5 -12.12534503 galago\n";

      assertEquals(expected, out);

    } finally {
      if (trecCorpusFile != null) {
        trecCorpusFile.delete();
      }
      if (priorFile != null) {
        priorFile.delete();
      }
      if (queryFile != null) {
        queryFile.delete();
      }
      if (indexFile != null) {
        Utility.deleteDirectory(indexFile);
      }
    }
  }
}
