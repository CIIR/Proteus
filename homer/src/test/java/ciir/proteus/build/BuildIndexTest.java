package ciir.proteus.build;

import ciir.proteus.parse.MBTEIBookParser;
import ciir.proteus.parse.MBTEIPageParser;
import org.junit.Assert;
import org.junit.Test;
import org.lemurproject.galago.core.retrieval.Retrieval;
import org.lemurproject.galago.core.retrieval.RetrievalFactory;
import org.lemurproject.galago.core.retrieval.ScoredDocument;
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.core.retrieval.query.StructuredQuery;
import org.lemurproject.galago.core.tools.apps.BuildIndex;
import org.lemurproject.galago.tupleflow.FileUtility;
import org.lemurproject.galago.tupleflow.Parameters;
import org.lemurproject.galago.tupleflow.Utility;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * @author jfoley.
 */
public class BuildIndexTest {

  public static Parameters makeExternalParsersConfig(String parserClass) {
    Parameters tokteiParser = new Parameters();
    tokteiParser.set("filetype", "toktei");
    tokteiParser.set("class", parserClass);

    Parameters mbteiParser = new Parameters();
    mbteiParser.set("filetype", "mbtei");
    mbteiParser.set("class", parserClass);

    Parameters extP = new Parameters();
    extP.set("externalParsers", Arrays.asList(tokteiParser, mbteiParser));

    return extP;
  }

  public static List<ScoredDocument> runQuery(Retrieval r, String input) throws Exception {
    Parameters qp = new Parameters();
    Node parsed = StructuredQuery.parse(input);
    Node ready = r.transformQuery(parsed, qp);
    return r.executeQuery(ready, qp).scoredDocuments;
  }

  @Test
  public void buildTokteiPagesIndex() throws Exception {
    File tmpIndex = FileUtility.createTemporaryDirectory();

    Parameters buildP = new Parameters();
    buildP.set("server", false);
    buildP.set("corpus", true);
    buildP.set("indexPath", tmpIndex.getAbsolutePath());
    buildP.set("inputPath", "src/test/resources/toktei/");
    buildP.set("parser", makeExternalParsersConfig(MBTEIPageParser.class.getCanonicalName()));

    BuildIndex buildFn = new BuildIndex();
    buildFn.run(buildP, System.out);

    Assert.assertTrue(tmpIndex.exists());

    Parameters runP = new Parameters();
    runP.set("index", tmpIndex.getAbsolutePath());
    Retrieval ret = RetrievalFactory.instance(runP);
    List<ScoredDocument> results = runQuery(ret, "romeo");

    Assert.assertFalse(results.isEmpty());

    List<ScoredDocument> emptyResults = runQuery(ret, "thisisaridiculoustermthatwillnotbefound");
    Assert.assertTrue(emptyResults.isEmpty());


    Utility.deleteDirectory(tmpIndex);
  }

  @Test
  public void buildTokteiBooksIndex() throws Exception {
    File tmpIndex = FileUtility.createTemporaryDirectory();

    Parameters buildP = new Parameters();
    buildP.set("server", false);
    buildP.set("corpus", true);
    buildP.set("indexPath", tmpIndex.getAbsolutePath());
    buildP.set("inputPath", "src/test/resources/toktei/");
    buildP.set("parser", makeExternalParsersConfig(MBTEIBookParser.class.getCanonicalName()));

    BuildIndex buildFn = new BuildIndex();
    buildFn.run(buildP, System.out);

    Assert.assertTrue(tmpIndex.exists());

    Parameters runP = new Parameters();
    runP.set("index", tmpIndex.getAbsolutePath());
    Retrieval ret = RetrievalFactory.instance(runP);
    List<ScoredDocument> results = runQuery(ret, "romeo");

    Assert.assertFalse(results.isEmpty());

    Utility.deleteDirectory(tmpIndex);
  }
}
