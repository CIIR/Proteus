/*
 *  BSD License (http://lemurproject.org/galago-license)
 */
package org.lemurproject.galago.core.index;

import org.lemurproject.galago.core.index.disk.VocabularyWriter;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.Arrays;
import junit.framework.TestCase;
import org.lemurproject.galago.tupleflow.Utility;

/**
 *
 * @author trevor
 */
public class VocabularyWriterTest extends TestCase {
    public VocabularyWriterTest(String testName) {
        super(testName);
    }

    public void testWriter() throws Exception {
        VocabularyWriter writer = new VocabularyWriter();

        byte[] first = "first".getBytes("UTF-8");
        byte[] second = "second".getBytes("UTF-8");
        writer.add(first, 0, (short) 10);
        writer.add(second, 256, (short) 10);

        byte[] output = writer.data();
        DataInputStream input = new DataInputStream(new ByteArrayInputStream(output));

        byte[] buffer = new byte[5];
        assertEquals(5, Utility.uncompressInt(input));
        input.read(buffer);
        assertTrue(Arrays.equals(first, buffer));
        assertEquals(0, Utility.uncompressLong(input));
        assertEquals(10, Utility.uncompressInt(input));

        buffer = new byte[6];
        assertEquals(6, Utility.uncompressInt(input));
        input.read(buffer);
        assertTrue(Arrays.equals(second, buffer));
        assertEquals(256, Utility.uncompressLong(input));
        assertEquals(10, Utility.uncompressInt(input));

    }
}
