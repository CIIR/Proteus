// BSD License (http://lemurproject.org/galago-license)
/*
 * FieldIndexReaderTest.java
 * JUnit based test
 *
 * Created on August 21, 2011
 */
package org.lemurproject.galago.core.retrieval;

import java.io.ByteArrayOutputStream;
import org.lemurproject.galago.core.index.disk.DiskBTreeReader;
import org.lemurproject.galago.core.index.disk.FieldIndexReader;
import org.lemurproject.galago.tupleflow.Utility;
import java.io.File;
import java.text.DateFormat;
import java.util.Arrays;
import junit.framework.TestCase;
import org.lemurproject.galago.core.index.disk.FieldIndexWriter;
import org.lemurproject.galago.core.retrieval.iterator.EqualityIterator;
import org.lemurproject.galago.core.retrieval.iterator.GreaterThanIterator;
import org.lemurproject.galago.core.retrieval.iterator.InBetweenIterator;
import org.lemurproject.galago.core.retrieval.iterator.LessThanIterator;
import org.lemurproject.galago.core.retrieval.query.NodeParameters;
import org.lemurproject.galago.tupleflow.Parameters;

/**
 *
 * @author irmarc
 */
public class FieldIndexReaderTest extends TestCase {

  File tempPath;

  public FieldIndexReaderTest(String testName) {
    super(testName);
  }

  @Override
  public void setUp() throws Exception {
    // make a spot for the index
    tempPath = Utility.createTemporary();

    Parameters tokenizer = new Parameters();
    Parameters formats = new Parameters();
    formats.set("title", "string");
    formats.set("date", "date");
    formats.set("version", "int");
    tokenizer.set("formats", formats);
    String[] fields = {"title", "date", "version"};
    tokenizer.set("fields", Arrays.asList(fields));

    Parameters params = new Parameters();
    params.set("filename", tempPath.toString());
    params.set("tokenizer", tokenizer);

    FieldIndexWriter writer =
            new FieldIndexWriter(new org.lemurproject.galago.tupleflow.FakeParameters(params));

    // Dates are... dates
    writer.processFieldName(Utility.fromString("date"));
    DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);

    writer.processNumber(15);
    writer.processTuple(Utility.fromLong(df.parse("6/12/1980").getTime()));

    writer.processNumber(25);
    writer.processTuple(Utility.fromLong(df.parse("6/12/1949").getTime()));


    writer.processNumber(47);
    writer.processTuple(Utility.fromLong(df.parse("1/1/1663").getTime()));

    // Titles are strings
    writer.processFieldName(Utility.fromString("title"));

    writer.processNumber(1);
    writer.processTuple(convertStringToBytes("doc1"));

    writer.processNumber(9);
    writer.processTuple(convertStringToBytes("doc2"));

    writer.processNumber(34);
    writer.processTuple(convertStringToBytes("doc9"));

    // We consider versions to be ints
    writer.processFieldName(Utility.fromString("version"));

    writer.processNumber(1);
    writer.processTuple(Utility.compressInt(1));

    writer.processNumber(2);
    writer.processTuple(Utility.compressInt(12));

    writer.processNumber(3);
    writer.processTuple(Utility.compressInt(4));

    writer.close();
  }

  @Override
  public void tearDown() throws Exception {
    tempPath.delete();
  }

  protected byte[] convertStringToBytes(String s) throws Exception {
    byte[] strBytes = Utility.fromString(s);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    baos.write(Utility.compressInt(strBytes.length));
    baos.write(strBytes);
    return (baos.toByteArray());
  }

  public void testReadTitle() throws Exception {
    FieldIndexReader reader = new FieldIndexReader(new DiskBTreeReader(tempPath.toString()));
    FieldIndexReader.ListIterator fields = reader.getField("title");

    assertFalse(fields.isDone());
    assertEquals(fields.currentCandidate(), 1);
    assertEquals(fields.stringValue(), "doc1");

    fields.movePast(fields.currentCandidate());

    assertFalse(fields.isDone());
    assertEquals(fields.currentCandidate(), 9);
    assertEquals(fields.stringValue(), "doc2");

    fields.movePast(fields.currentCandidate());

    assertFalse(fields.isDone());
    assertEquals(fields.currentCandidate(), 34);
    assertEquals(fields.stringValue(), "doc9");

    fields.movePast(fields.currentCandidate());
    assertTrue(fields.isDone());
    reader.close();
  }

  public void testReadDate() throws Exception {
    FieldIndexReader reader = new FieldIndexReader(new DiskBTreeReader(tempPath.toString()));
    FieldIndexReader.ListIterator fields = reader.getField("date");

    DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);

    assertFalse(fields.isDone());
    assertEquals(fields.currentCandidate(), 15);
    assertEquals(fields.dateValue(), df.parse("6/12/1980").getTime());

    fields.movePast(fields.currentCandidate());

    assertFalse(fields.isDone());
    assertEquals(fields.currentCandidate(), 25);
    assertEquals(fields.dateValue(), df.parse("6/12/1949").getTime());

    fields.movePast(fields.currentCandidate());

    assertFalse(fields.isDone());
    assertEquals(fields.currentCandidate(), 47);
    assertEquals(fields.dateValue(), df.parse("1/1/1663").getTime());

    fields.movePast(fields.currentCandidate());
    assertTrue(fields.isDone());
    reader.close();
  }

  public void testReadVersion() throws Exception {
    FieldIndexReader reader = new FieldIndexReader(new DiskBTreeReader(tempPath.toString()));
    FieldIndexReader.ListIterator fields = reader.getField("version");

    assertFalse(fields.isDone());
    assertEquals(fields.currentCandidate(), 1);
    assertEquals(fields.intValue(), 1);

    fields.movePast(fields.currentCandidate());

    assertFalse(fields.isDone());
    assertEquals(fields.currentCandidate(), 2);
    assertEquals(fields.intValue(), 12);

    fields.movePast(fields.currentCandidate());

    assertFalse(fields.isDone());
    assertEquals(fields.currentCandidate(), 3);
    assertEquals(fields.intValue(), 4);

    fields.movePast(fields.currentCandidate());
    assertTrue(fields.isDone());
    reader.close();
  }

  public void testGreaterThan() throws Exception {
    FieldIndexReader reader = new FieldIndexReader(new DiskBTreeReader(tempPath.toString()));
    FieldIndexReader.ListIterator fields = reader.getField("version");

    NodeParameters p = new NodeParameters();
    p.set("0","5");
    GreaterThanIterator gti = new GreaterThanIterator(p, fields);

    assertFalse(gti.isDone());
    assertEquals(gti.currentCandidate(), 1);
    assertTrue(gti.hasMatch(gti.currentCandidate()));
    assertFalse(gti.indicator(gti.currentCandidate()));
    gti.movePast(gti.currentCandidate());

    assertFalse(gti.isDone());
    assertEquals(gti.currentCandidate(), 2);
    assertTrue(gti.hasMatch(gti.currentCandidate()));
    assertTrue(gti.indicator(gti.currentCandidate()));
    gti.movePast(gti.currentCandidate());

    assertFalse(gti.isDone());
    assertEquals(gti.currentCandidate(), 3);
    assertTrue(gti.hasMatch(gti.currentCandidate()));
    assertFalse(gti.indicator(gti.currentCandidate()));
    gti.movePast(gti.currentCandidate());

    assertTrue(gti.isDone());
    reader.close();
  }

  public void testLessThan() throws Exception {
    FieldIndexReader reader = new FieldIndexReader(new DiskBTreeReader(tempPath.toString()));
    FieldIndexReader.ListIterator fields = reader.getField("version");

    NodeParameters p = new NodeParameters();
    p.set("0", "5");
    LessThanIterator lti = new LessThanIterator(p, fields);

    assertFalse(lti.isDone());
    assertEquals(lti.currentCandidate(), 1);
    assertTrue(lti.hasMatch(lti.currentCandidate()));
    assertTrue(lti.indicator(lti.currentCandidate()));
    lti.movePast(lti.currentCandidate());

    assertFalse(lti.isDone());
    assertEquals(lti.currentCandidate(), 2);
    assertTrue(lti.hasMatch(lti.currentCandidate()));
    assertFalse(lti.indicator(lti.currentCandidate()));
    lti.movePast(lti.currentCandidate());

    assertFalse(lti.isDone());
    assertEquals(lti.currentCandidate(), 3);
    assertTrue(lti.hasMatch(lti.currentCandidate()));
    assertTrue(lti.indicator(lti.currentCandidate()));
    lti.movePast(lti.currentCandidate());

    assertTrue(lti.isDone());
    reader.close();
  }

  public void testInBetween() throws Exception {
    FieldIndexReader reader = new FieldIndexReader(new DiskBTreeReader(tempPath.toString()));
    FieldIndexReader.ListIterator fields = reader.getField("date");

    NodeParameters p = new NodeParameters();
    p.set("0", "12/25/1939");
    p.set("1", "4/12/1984");
    InBetweenIterator ibi = new InBetweenIterator(p, fields);

    assertFalse(ibi.isDone());
    assertEquals(ibi.currentCandidate(), 15);
    assertTrue(ibi.hasMatch(ibi.currentCandidate()));
    assertTrue(ibi.indicator(ibi.currentCandidate()));
    ibi.movePast(ibi.currentCandidate());

    assertFalse(ibi.isDone());
    assertEquals(ibi.currentCandidate(), 25);
    assertTrue(ibi.hasMatch(ibi.currentCandidate()));
    assertTrue(ibi.indicator(ibi.currentCandidate()));
    ibi.movePast(ibi.currentCandidate());

    assertFalse(ibi.isDone());
    assertEquals(ibi.currentCandidate(), 47);
    assertTrue(ibi.hasMatch(ibi.currentCandidate()));
    assertFalse(ibi.indicator(ibi.currentCandidate()));
    ibi.movePast(ibi.currentCandidate());

    assertTrue(ibi.isDone());
    reader.close();
  }

  public void testEquality() throws Exception {
    FieldIndexReader reader = new FieldIndexReader(new DiskBTreeReader(tempPath.toString()));
    FieldIndexReader.ListIterator fields = reader.getField("title");

    NodeParameters p = new NodeParameters();
    p.set("0", "doc9");
    EqualityIterator ei = new EqualityIterator(p, fields);

    assertFalse(ei.isDone());
    assertEquals(ei.currentCandidate(), 1);
    assertTrue(ei.hasMatch(ei.currentCandidate()));
    assertFalse(ei.indicator(ei.currentCandidate()));
    ei.movePast(ei.currentCandidate());

    assertFalse(ei.isDone());
    assertEquals(ei.currentCandidate(), 9);
    assertTrue(ei.hasMatch(ei.currentCandidate()));
    assertFalse(ei.indicator(ei.currentCandidate()));
    ei.movePast(ei.currentCandidate());

    assertFalse(ei.isDone());
    assertEquals(ei.currentCandidate(), 34);
    assertTrue(ei.hasMatch(ei.currentCandidate()));
    assertTrue(ei.indicator(ei.currentCandidate()));
    ei.movePast(ei.currentCandidate());

    assertTrue(ei.isDone());
    reader.close();
  }
}
