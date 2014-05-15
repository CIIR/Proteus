package ciir.proteus.build;

import ciir.proteus.parse.MBTEIPageParser;
import org.junit.Test;
import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.types.DocumentSplit;
import org.lemurproject.galago.tupleflow.FileUtility;
import org.lemurproject.galago.tupleflow.Parameters;
import org.lemurproject.galago.tupleflow.Utility;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author jfoley.
 */
public class MBTEIPageParserTest {

  @Test
  public void testPage() throws IOException, XMLStreamException {
    String data = "<TEI>" +
      "<identifier>foo</identifier>"+
      "<text>"+
      "<w form=\"first\">firstWord</w>"+
      "<pb n=\"0\" />"+
      "<w form=\"second\">huh</w>"+
      "<pb n=\"1\" />"+
      "</text>"+
      "</TEI>";

    File tmp = FileUtility.createTemporary();
    Utility.copyStringToFile(data, tmp);

    DocumentSplit split = new DocumentSplit();
    split.fileName = tmp.getAbsolutePath();
    split.fileType = "mbtei";
    MBTEIPageParser parser = new MBTEIPageParser(split, new Parameters());

    Document page1 = parser.nextDocument();
    assertNotNull(page1);
    assertEquals("0", page1.metadata.get("pageNumber"));
    assertEquals("first", page1.text.trim());

    Document page2 = parser.nextDocument();
    assertNotNull(page2);
    assertEquals("1", page2.metadata.get("pageNumber"));
    assertEquals("second", page2.text.trim());

    assertNull(parser.nextDocument());
  }
}
