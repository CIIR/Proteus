package ciir.proteus.parse;

import org.junit.Assert;
import org.junit.Test;
import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.types.DocumentSplit;
import org.lemurproject.galago.core.util.DocumentSplitFactory;
import org.lemurproject.galago.tupleflow.FileUtility;
import org.lemurproject.galago.utility.Parameters;
import org.lemurproject.galago.tupleflow.Utility;
import org.lemurproject.galago.utility.StreamUtil;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;

public class MBTEIPageParserTest {

    @Test
    public void testPage() throws IOException, XMLStreamException {
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
    }

    @Test
    public void testPage2() throws IOException, XMLStreamException {
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
    }

    @Test
    public void testParseCrapDocument() throws IOException {
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

}
