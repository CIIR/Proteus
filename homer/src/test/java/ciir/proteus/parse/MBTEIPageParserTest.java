package ciir.proteus.parse;

import org.junit.Assert;
import org.junit.Test;
import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.types.DocumentSplit;
import org.lemurproject.galago.tupleflow.FileUtility;
import org.lemurproject.galago.tupleflow.Parameters;
import org.lemurproject.galago.tupleflow.Utility;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;

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
    Assert.assertNotNull(page1);
    Assert.assertEquals("0", page1.metadata.get("pageNumber"));
    Assert.assertEquals("first", page1.text.trim());

    Document page2 = parser.nextDocument();
    Assert.assertNotNull(page2);
    Assert.assertEquals("1", page2.metadata.get("pageNumber"));
    Assert.assertEquals("second", page2.text.trim());

    Assert.assertNull(parser.nextDocument());
  }

  @Test
  public void testParseCrapDocument() throws IOException {
    DocumentSplit input = new DocumentSplit();
    input.fileName = "src/test/resources/EmptyFile";
    MBTEIPageParser pages = new MBTEIPageParser(input, new Parameters());
    Assert.assertNotNull(pages);
    Assert.assertNull(pages.nextDocument());
    pages.close();

    input.fileName = ".gitignore";
    pages = new MBTEIPageParser(input, new Parameters());
    Assert.assertNotNull(pages);
    Assert.assertNull(pages.nextDocument());
    pages.close();

    input.fileName = "src/test/resources/nearly-empty.mbtei.gz";
    pages = new MBTEIPageParser(input, new Parameters());
    Assert.assertNotNull(pages);
    Assert.assertNull(pages.nextDocument());
    pages.close();
  }

}