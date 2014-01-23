// BSD License (http://lemurproject.org/galago-license)
package org.lemurproject.galago.core.index.corpus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import junit.framework.TestCase;
import org.lemurproject.galago.core.index.GenericElement;
import org.lemurproject.galago.tupleflow.FakeParameters;
import org.lemurproject.galago.tupleflow.IncompatibleProcessorException;
import org.lemurproject.galago.tupleflow.Parameters;
import org.lemurproject.galago.tupleflow.Utility;

/**
 *
 * @author trevor
 */
public class SplitIndexWriterTest extends TestCase {

  public SplitIndexWriterTest(String testName) {
    super(testName);
  }

  public void testSingleKeyValue() throws IOException, IncompatibleProcessorException {
    File temporary = null;

    try {
      temporary = Utility.createTemporary();
      temporary.delete();
      temporary.mkdir();

      Parameters parameters = new Parameters();
      parameters.set("blockSize", 64);
      parameters.set("filename", temporary.getAbsolutePath());
      parameters.set("parallel", true);

      SplitBTreeValueWriter writer = new SplitBTreeValueWriter(new FakeParameters(parameters));
      writer.setProcessor(new SplitBTreeKeyWriter(new FakeParameters(parameters)));
      writer.add(new GenericElement("key", "value"));
      writer.close();

      assertTrue(SplitBTreeReader.isBTree(temporary));
      SplitBTreeReader reader = new SplitBTreeReader(temporary);

      assertEquals("value", reader.getValueString(Utility.fromString("key")));
      reader.close();
    } finally {
      if (temporary != null) {
        Utility.deleteDirectory(temporary);
      }
    }
  }

  public void testSeek() throws IOException, IncompatibleProcessorException {
    File temporary = null;

    try {
      temporary = Utility.createTemporary();
      temporary.delete();
      temporary.mkdir();

      Parameters parameters = new Parameters();
      parameters.set("blockSize", 64);
      parameters.set("filename", temporary.getAbsolutePath());
      parameters.set("parallel", "true");

      SplitBTreeValueWriter writer = new SplitBTreeValueWriter(new FakeParameters(parameters));
      writer.setProcessor(new SplitBTreeKeyWriter(new FakeParameters(parameters)));

      writer.add(new GenericElement("key", "value"));
      writer.add(new GenericElement("more", "value2"));
      writer.close();

      assertTrue(SplitBTreeReader.isBTree(temporary));
      SplitBTreeReader reader = new SplitBTreeReader(temporary);
      SplitBTreeReader.Iterator iterator = reader.getIterator();

      // Skip to 'more'
      iterator.skipTo(new byte[]{(byte) 'm'});
      assertFalse(iterator.isDone());
      assertEquals("more", Utility.toString(iterator.getKey()));
      assertEquals("value2", iterator.getValueString());
      assertFalse(iterator.nextKey());

      // Start at the beginning
      iterator = reader.getIterator();
      assertFalse(iterator.isDone());
      assertEquals("key", Utility.toString(iterator.getKey()));
      assertTrue(iterator.nextKey());
      assertEquals("more", Utility.toString(iterator.getKey()));
      assertFalse(iterator.nextKey());

      // Start after all keys
      iterator = reader.getIterator();
      assertFalse(iterator.isDone());
      iterator.skipTo(new byte[]{(byte) 'z'});
      assertTrue(iterator.isDone());

      reader.close();
    } finally {
      if (temporary != null) {
        Utility.deleteDirectory(temporary);
      }
    }
  }

  public void testSimpleWrite() throws FileNotFoundException, IOException, IncompatibleProcessorException {
    File temporary = null;

    try {
      temporary = Utility.createTemporary();
      temporary.delete();
      temporary.mkdir();

      Parameters parameters = new Parameters();
      parameters.set("blockSize", 128);
      parameters.set("filename", temporary.getAbsolutePath());
      parameters.set("parallel", true);

      SplitBTreeValueWriter writer = new SplitBTreeValueWriter(new FakeParameters(parameters));
      writer.setProcessor(new SplitBTreeKeyWriter(new FakeParameters(parameters)));

      for (int i = 0; i < 1000; ++i) {
        String key = String.format("%05d", i);
        String value = String.format("value%05d", i);
        writer.add(new GenericElement(key, value));
      }
      writer.close();

      assertTrue(SplitBTreeReader.isBTree(temporary));
      SplitBTreeReader reader = new SplitBTreeReader(temporary);


      for (int i = 1000 - 1; i >= 0; i--) {
        String key = String.format("%05d", i);
        String value = String.format("value%05d", i);

        assertEquals(value, reader.getValueString(Utility.fromString(key)));
      }
      reader.close();

    } finally {
      if (temporary != null) {
        Utility.deleteDirectory(temporary);
      }
    }
  }
}
