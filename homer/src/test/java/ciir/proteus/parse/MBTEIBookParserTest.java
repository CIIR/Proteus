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
            + "	<pb n=\"2\"/>" // empty page
            + "	<pb n=\"3\"/>"
            + "	<w  form=\"Max\" >Max</w>"
            + "	<w  form=\"is\" >is</w>"
            + "	<w  form=\"a\" >a</w>"
            + "	<w  form=\"cat\" >cat</w>"
            + "</text>"
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
    } catch (Exception e){
      // should get exception
    }

    // doc w/o NER
    parser = new MBTEIBookParser(split, Parameters.create());

    {
      Document doc = parser.nextDocument();
      Assert.assertNotNull(doc);
      Assert.assertEquals("Alice chased the rabbit Max is a cat", doc.text.trim());
    }

    // test with NER
    p.put("ner-model", "src/main/resources/ner-classifiers/english.all.3class.distsim.crf.ser.gz");
    parser = new MBTEIBookParser(split, p);

    {
      Document doc = parser.nextDocument();
      Assert.assertNotNull(doc);
      Assert.assertEquals("<PERSON>Alice</PERSON> chased the rabbit <PERSON>Max</PERSON> is a cat", doc.text.trim());
    }

    Assert.assertNull(parser.nextDocument());
  }

}
