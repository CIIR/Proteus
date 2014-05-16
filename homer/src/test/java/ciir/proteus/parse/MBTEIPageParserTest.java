package ciir.proteus.parse;

import org.junit.Assert;
import org.junit.Test;
import org.lemurproject.galago.core.types.DocumentSplit;
import org.lemurproject.galago.tupleflow.Parameters;

import java.io.IOException;

public class MBTEIPageParserTest {
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