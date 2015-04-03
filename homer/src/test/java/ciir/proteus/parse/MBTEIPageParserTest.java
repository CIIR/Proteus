package ciir.proteus.parse;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.types.DocumentSplit;
import org.lemurproject.galago.core.util.DocumentSplitFactory;
import org.lemurproject.galago.tupleflow.FileUtility;
import org.lemurproject.galago.utility.Parameters;
import org.lemurproject.galago.utility.StreamUtil;

import java.io.File;
import java.lang.reflect.Field;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class MBTEIPageParserTest {

    @Before
    // from: http://blog.davidehringer.com/testing/test-driven-development/unit-testing-singletons/
    public void resetSingleton() throws NoSuchFieldException, IllegalAccessException {
      Field classifier = NamedEntityRecognizer.class.getDeclaredField("classifier");
      classifier.setAccessible(true);
      classifier.set(null, null);
      Field instance = NamedEntityRecognizer.class.getDeclaredField("instance");
      instance.setAccessible(true);
      instance.set(null, null);
    }

    @Test
    public void testPage() throws Exception {
        String data = "<TEI>"
                + "<identifier>foo</identifier>"
                + "<text>"
                + "<pb n=\"0\" />"
                + "<w form=\"first\">firstWord</w>"
                + "<pb n=\"1\" />"
                + "<w form=\"second\">huh</w>"
                + "</text>"
                + "</TEI>";

        File tmp = FileUtility.createTemporary();
        StreamUtil.copyStringToFile(data, tmp);

        DocumentSplit split = DocumentSplitFactory.file(tmp, "mbtei");
        MBTEIPageParser parser = new MBTEIPageParser(split, Parameters.create());

        Document page1 = parser.nextDocument();
        Assert.assertNotNull(page1);
        Assert.assertEquals("0", page1.metadata.get("pageNumber"));
        Assert.assertEquals("first", page1.text.trim());

        Document page2 = parser.nextDocument();
        Assert.assertNotNull(page2);
        Assert.assertEquals("1", page2.metadata.get("pageNumber"));
        Assert.assertEquals("second", page2.text.trim());

        Assert.assertNull(parser.nextDocument());
        tmp.delete();
     }

    @Test
    public void testPage2() throws Exception {
        String data = "<TEI>"
                + "<metadata>"
                + "	<identifier>test</identifier>"
                + "	<title>test title</title>"
                + "</metadata>"
                + "<text lang=\"eng\">"
                + "	<pb n=\"1\"/>"
                + "	<w form=\"page one\" >page one</w>"
                + "	<pb n=\"2\"/>"
                + "	<w  form=\"page two\" >page two</w>"
                + "	<pb n=\"3\"/>"
                + "	<pb n=\"4\"/>"
                + "	<pb n=\"5\"/>"
                + "	<pb n=\"6\"/>"
                + "	<pb n=\"7\"/>"
                + "	<w  form=\"page seven\" >page seven</w>"
                + "</text>"
                + "</TEI>";

        File tmp = FileUtility.createTemporary();
        StreamUtil.copyStringToFile(data, tmp);

        DocumentSplit split = DocumentSplitFactory.file(tmp, "mbtei");
        MBTEIPageParser parser = new MBTEIPageParser(split, Parameters.create());
        {
            Document page = parser.nextDocument();
            Assert.assertNotNull(page);
            Assert.assertEquals("0", page.metadata.get("pageNumber"));
            Assert.assertEquals("page one", page.text.trim());
        }
        {
            Document page = parser.nextDocument();
            Assert.assertNotNull(page);
            Assert.assertEquals("1", page.metadata.get("pageNumber"));
            Assert.assertEquals("page two", page.text.trim());
        }

        {
            Document page = parser.nextDocument();
            Assert.assertNotNull(page);
            Assert.assertEquals("6", page.metadata.get("pageNumber"));
            Assert.assertEquals("page seven", page.text.trim());
        }
        Assert.assertNull(parser.nextDocument());
      tmp.delete();
    }

    @Test
    public void testParseCrapDocument() throws Exception {
        DocumentSplit input = DocumentSplitFactory.file("src/test/resources/EmptyFile");
        MBTEIPageParser pages = new MBTEIPageParser(input, Parameters.create());
        Assert.assertNotNull(pages);
        Assert.assertNull(pages.nextDocument());
        pages.close();

        input.fileName = ".gitignore";
        pages = new MBTEIPageParser(input, Parameters.create());
        Assert.assertNotNull(pages);
        Assert.assertNull(pages.nextDocument());
        pages.close();

        input.fileName = "src/test/resources/nearly-empty.mbtei.gz";
        pages = new MBTEIPageParser(input, Parameters.create());
        Assert.assertNotNull(pages);
        Assert.assertNull(pages.nextDocument());
        pages.close();
    }

  @Test
  public void testNER() throws Exception {
    String data = "<TEI>"
            + "<metadata>"
            + "	<identifier>test</identifier>"
            + "	<title>test title</title>"
            + "</metadata>"
            + "<text lang=\"eng\">"
            + "	<pb n=\"1\"/>"
            + "	<w form=\"Alice\" >Alice</w>"
            + "	<w form=\"chased\" >chased</w>"
            + "	<w form=\"the\" >the</w>"
            + "	<w form=\"rabbit\" >rabbit</w>"
            + "	<pb n=\"2\"/>"
            + "	<pb n=\"3\"/>"
            + "	<w  form=\"Max\" >Max</w>"
            + "	<w  form=\"is\" >is</w>"
            + "	<w  form=\"a\" >a</w>"
            + "	<w  form=\"cat\" >cat</w>"
            + "</text>"
            + "</TEI>";

    MBTEIPageParser parser;
    File tmp = FileUtility.createTemporary();
    StreamUtil.copyStringToFile(data, tmp);

    DocumentSplit split = DocumentSplitFactory.file(tmp, "mbtei");
    Parameters p = Parameters.create();
    // invalid NER model
    p.put("ner-model", "i-do-not-exist");
    try {
       parser = new MBTEIPageParser(split, p);
      fail("Should throw exception");
    } catch (Exception e){
      // should get exception
    }

    p.put("ner-model", "src/main/resources/ner-classifiers/english.all.3class.distsim.crf.ser.gz");
    parser = new MBTEIPageParser(split, p);

    {
      Document page = parser.nextDocument();
      Assert.assertNotNull(page);
      Assert.assertEquals("0", page.metadata.get("pageNumber"));
      Assert.assertEquals("<PERSON>Alice</PERSON> chased the rabbit", page.text.trim());
    }

    {
      Document page = parser.nextDocument();
      Assert.assertNotNull(page);
      Assert.assertEquals("2", page.metadata.get("pageNumber"));
      Assert.assertEquals("<PERSON>Max</PERSON> is a cat", page.text.trim());
    }
    Assert.assertNull(parser.nextDocument());
    tmp.delete();
  }


}
