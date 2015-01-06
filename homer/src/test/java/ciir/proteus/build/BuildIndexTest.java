package ciir.proteus.build;

import ciir.proteus.parse.MBTEIBookParser;
import ciir.proteus.parse.MBTEIPageParser;
import org.junit.Assert;
import org.junit.Test;
import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.retrieval.Retrieval;
import org.lemurproject.galago.core.retrieval.RetrievalFactory;
import org.lemurproject.galago.core.retrieval.ScoredDocument;
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.core.retrieval.query.StructuredQuery;
import org.lemurproject.galago.core.tools.apps.BuildIndex;
import org.lemurproject.galago.tupleflow.FileUtility;
import org.lemurproject.galago.utility.FSUtil;
import org.lemurproject.galago.utility.Parameters;

import java.io.File;
import java.util.List;

/**
 * @author jfoley.
 */
public class BuildIndexTest {

  public static List<ScoredDocument> runQuery(Retrieval r, String input) throws Exception {
    Parameters qp = Parameters.create();
    Node parsed = StructuredQuery.parse(input);
    Node ready = r.transformQuery(parsed, qp);
    return r.executeQuery(ready, qp).scoredDocuments;
  }

  @Test
  public void buildTokteiPagesIndex() throws Exception {
    File tmpIndex = FileUtility.createTemporaryDirectory();

    Parameters buildP = Parameters.create();
    buildP.set("server", false);
    buildP.set("corpus", true);
    buildP.set("indexPath", tmpIndex.getAbsolutePath());
    buildP.set("inputPath", "src/test/resources/toktei/");
    buildP.set("filetype", MBTEIPageParser.class.getCanonicalName());

    BuildIndex buildFn = new BuildIndex();
    buildFn.run(buildP, System.out);

    Assert.assertTrue(tmpIndex.exists());

    Parameters runP = Parameters.create();
    runP.set("index", tmpIndex.getAbsolutePath());
    Retrieval ret = RetrievalFactory.create(runP);
    List<ScoredDocument> results = runQuery(ret, "romeo");

    String aDocumentName = results.get(0).documentName;

    Assert.assertFalse(results.isEmpty());

    Document doc = ret.getDocument(aDocumentName, new Document.DocumentComponents(true, true, true));
    Assert.assertNotNull(doc);
    Assert.assertNotNull(doc.text);
    Assert.assertNotNull(doc.metadata);
    Assert.assertNotNull(doc.terms);

    List<ScoredDocument> emptyResults = runQuery(ret, "thisisaridiculoustermthatwillnotbefound");
    Assert.assertTrue(emptyResults.isEmpty());

    // assert that our identifier hack is still working
    Assert.assertTrue(aDocumentName.contains("_"));
    String[] parts = aDocumentName.split("_");
    Assert.assertEquals(2, parts.length);
    String bookName = parts[0];
    Assert.assertNotNull(Integer.parseInt(parts[1]));
    Assert.assertEquals(bookName, doc.metadata.get("identifier"));


    FSUtil.deleteDirectory(tmpIndex);
  }

  @Test
  public void buildTokteiBooksIndex() throws Exception {
    File tmpIndex = FileUtility.createTemporaryDirectory();

    Parameters buildP = Parameters.create();
    buildP.set("server", false);
    buildP.set("corpus", true);
    buildP.set("indexPath", tmpIndex.getAbsolutePath());
    buildP.set("inputPath", "src/test/resources/toktei/");
    buildP.set("filetype", MBTEIBookParser.class.getCanonicalName());

    BuildIndex buildFn = new BuildIndex();
    buildFn.run(buildP, System.out);

    Assert.assertTrue(tmpIndex.exists());

    Parameters runP = Parameters.create();
    runP.set("index", tmpIndex.getAbsolutePath());
    Retrieval ret = RetrievalFactory.create(runP);
    List<ScoredDocument> results = runQuery(ret, "romeo");

    Assert.assertFalse(results.isEmpty());

    FSUtil.deleteDirectory(tmpIndex);
  }
}
