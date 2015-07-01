/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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

import static org.junit.Assert.fail;

/**
 *
 * @author michaelz
 */
public class MBTEIBookParserTest {

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
    public void testNER() throws Exception {
      String data = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
              + "<TEI>\n"
              + "	<metadata>\n"
              + "		<title>Test Document</title>\n"
              + "		<identifier>mcztest</identifier>\n"
              + "	</metadata>\n"
                + "<text lang=\"eng\">\n"
                + "	<pb n=\"1\"/>\n"
                + "	<w form=\"Alice\" >Alice</w>\n"
                + "	<w form=\"chased\" >chased</w>\n"
                + "	<w form=\"the\" >the</w>\n"
                + "	<w form=\"rabbit\" >rabbit</w>\n"
                + "	<pb n=\"2\"/>\n" // empty page
                + "	<pb n=\"3\"/>\n"
                + "	<w  form=\"Max\" >Max</w>\n"
                + "	<w  form=\"is\" >is</w>\n"
                + "	<w  form=\"a\" >a</w>\n"
                + "	<w  form=\"cat\" >cat</w>\n"
                + "</text>\n"
                + "</TEI>";

        MBTEIBookParser parser;
        File tmp = FileUtility.createTemporary();
        StreamUtil.copyStringToFile(data, tmp);

        DocumentSplit split = DocumentSplitFactory.file(tmp, "mbtei");
        Parameters p = Parameters.create();
        // invalid NER model
        p.put("ner-model", "i-do-not-exist");
        try {
            parser = new MBTEIBookParser(split, p);
            fail("Should throw exception");
        } catch (Exception e) {
            // should get exception
        }

        // doc w/o NER
        parser = new MBTEIBookParser(split, Parameters.create());

        {
            Document doc = parser.nextDocument();
            Assert.assertNotNull(doc);
            Assert.assertEquals("<div class=\"page-break\" page=\"0\">Alice chased the rabbit <br></div><div class=\"page-break\" page=\"2\">Max is a cat <br></div>", doc.text.trim());
        }

        // test with NER
        p.put("ner-model", "src/main/resources/ner-classifiers/english.all.3class.distsim.crf.ser.gz");
        parser = new MBTEIBookParser(split, p);

        {
            Document doc = parser.nextDocument();
            Assert.assertNotNull(doc);
            Assert.assertEquals("<div class=\"page-break\" page=\"0\"><PERSON>Alice</PERSON> chased the rabbit <br></div><div class=\"page-break\" page=\"2\"><PERSON>Max</PERSON> is a cat <br></div>", doc.text.trim());
        }

        Assert.assertNull(parser.nextDocument());
        tmp.delete();
    }

    @Test
    // test that we ignore tokens the DjVu to TOKTEI split. They are indicated
    // by a "+" in the "coords" attribute
    public void testCoordsAttribute() throws Exception {
        String data = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<TEI>\n"
                + "	<metadata>\n"
                + "		<title>Test Document</title>\n"
                + "		<identifier>mcztest</identifier>\n"
                + "	</metadata>\n"
                + "	<text lang=\"eng\">\n"
                + "		<pb n=\"1\"/>\n"
                + "		<p>\n"
                + "			<s>\n"
                + "				<w form=\"EDITED\" coords=\"1285,2158,1586,2093\">EDITED,</w><w form=\",\" coords=\"1285,2158,1586,2093+1\"/>\n"
                + "				<lb/>\n"
                + "			</s>\n"
                + "		</p>\n"
                + "	</text>\n"
                + "</TEI>";

        MBTEIBookParser parser;
        File tmp = FileUtility.createTemporary();
        StreamUtil.copyStringToFile(data, tmp);

        DocumentSplit split = DocumentSplitFactory.file(tmp, "mbtei");

        // doc w/o NER
        parser = new MBTEIBookParser(split, Parameters.create());

        {
            Document doc = parser.nextDocument();
            Assert.assertNotNull(doc);
            Assert.assertEquals("<div class=\"page-break\" page=\"0\">EDITED, <br><br></div>", doc.text.trim());
        }
    }

    @Test
    // test that we rearrange the "fw" tags correctly
    public void testTOKTEIOrder() throws Exception {
        String data = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<TEI>\n"
                + "	<metadata>\n"
                + "		<title>Test Document</title>\n"
                + "		<identifier>mcztest</identifier>\n"
                + "	</metadata>\n"
                + "	<text lang=\"eng\">\n"
                + "		<pb n=\"1\"/>\n"
                + "		<p>\n"
                + "			<s>\n"
                + "				<w form=\"firstLine\" coords=\"1078,915,1359,880\">firstLine</w>\n"
                + "				<lb/>\n"
                + "			</s>\n"
                + "		</p>\n"
                + "		<fw place=\"top\">\n"
                + "			<w form=\"header\" coords=\"672,736,832,658\">header</w>\n"
                + "			<lb/>\n"
                + "		</fw>\n"
                + "		<fw place=\"bottom\">\n"
                + "			<w form=\"footer\" coords=\"305,3471,488,3434\">footer</w>\n"
                + "		</fw>\n"
                + "		<p>\n"
                + "			<s>\n"
                + "				<w form=\"secondLine\" coords=\"1078,915,1359,880\">secondLine</w>\n"
                + "				<lb/>\n"
                + "			</s>\n"
                + "		</p>\n"
                + "	</text>\n"
                + "</TEI>";

        MBTEIBookParser parser;
        File tmp = FileUtility.createTemporary();
        StreamUtil.copyStringToFile(data, tmp);

        DocumentSplit split = DocumentSplitFactory.file(tmp, "mbtei");

        // doc w/o NER
        parser = new MBTEIBookParser(split, Parameters.create());

        {
            Document doc = parser.nextDocument();
            Assert.assertNotNull(doc);
            Assert.assertEquals("<div class=\"page-break\" page=\"0\">header <br>firstLine <br>secondLine <br>footer <br></div>", doc.text.trim());
        }
    }
}
