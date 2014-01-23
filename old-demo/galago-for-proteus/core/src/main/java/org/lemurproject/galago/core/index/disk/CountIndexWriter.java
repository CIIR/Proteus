// BSD License (http://lemurproject.org/galago-license)
package org.lemurproject.galago.core.index.disk;

import gnu.trove.set.hash.TIntHashSet;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;
import org.lemurproject.galago.core.index.CompressedByteBuffer;
import org.lemurproject.galago.core.index.CompressedRawByteBuffer;
import org.lemurproject.galago.core.index.BTreeWriter;
import org.lemurproject.galago.core.index.IndexElement;
import org.lemurproject.galago.core.index.KeyListReader;
import org.lemurproject.galago.core.parse.NumericParameterAccumulator;
import org.lemurproject.galago.core.types.NumberWordCount;
import org.lemurproject.galago.tupleflow.IncompatibleProcessorException;
import org.lemurproject.galago.tupleflow.InputClass;
import org.lemurproject.galago.tupleflow.Parameters;
import org.lemurproject.galago.tupleflow.Step;
import org.lemurproject.galago.tupleflow.TupleFlowParameters;
import org.lemurproject.galago.tupleflow.Utility;
import org.lemurproject.galago.tupleflow.execution.ErrorHandler;
import org.lemurproject.galago.tupleflow.execution.Verification;

/**
 * Count Indexes are similar to a Position or Window Index Writer,
 * except that extent positions are not written.
 * 
 *  Structure: 
 *  mapping( term -> list(document-id), list(document-freq) )
 *
 *  Skip lists are supported
 * 
 * @author sjh
 */
@InputClass(className = "org.lemurproject.galago.core.types.NumberWordCount", order = {"+word", "+document"})
public class CountIndexWriter implements
        NumberWordCount.WordDocumentOrder.ShreddedProcessor {

  // writer variables //
  Parameters actualParams;
  BTreeWriter writer;
  CountsList invertedList;
  // statistics //
  byte[] lastWord;
  long vocabCount = 0;
  long collectionLength = 0;
  boolean estimateDocumentCount = false;
  long longestPostingList = 0;
  boolean calculateDocumentCount = false;
  TIntHashSet uniqueDocSet;
  // skipping parameters
  int options = 0;
  int skipDistance;
  int skipResetDistance;

  /**
   * Creates a new instance of CountIndexWriter
   */
  public CountIndexWriter(TupleFlowParameters parameters) throws FileNotFoundException, IOException {
    this.actualParams = parameters.getJSON();
    this.actualParams.set("writerClass", CountIndexWriter.class.getName());
    this.actualParams.set("readerClass", CountIndexReader.class.getName());
    this.actualParams.set("defaultOperator", "counts");

    // vocab and collection length can be calculated - doc count is more complex
    // option 1: predefined doccount
    if (this.actualParams.containsKey("statistics/documentCount")) {
      // great.
      // option 2: there is a doccount pipe
    } else if (this.actualParams.isString("pipename")) {
      Parameters docCounts = NumericParameterAccumulator.accumulateParameters(parameters.getTypeReader(actualParams.getString("pipename")));
      this.actualParams.set("statistics/documentCount", docCounts.getMap("documentCount").getLong("global"));

      // option 3: estimated document count - as longest posting list
    } else if (this.actualParams.isBoolean("estimateDocumentCount")) {
      this.estimateDocumentCount = true;
      this.longestPostingList = 0;

      // option 4: default option - calculate document count (this may be a problem - it requires memory)
    } else {
      this.actualParams.set("calculateDocumentCount", true);
      this.calculateDocumentCount = true;
      this.uniqueDocSet = new TIntHashSet();
    }

    this.writer = new DiskBTreeWriter(parameters);

    // look for skips
    boolean skip = parameters.getJSON().get("skipping", true);
    this.skipDistance = (int) parameters.getJSON().get("skipDistance", 500);
    this.skipResetDistance = (int) parameters.getJSON().get("skipResetDistance", 20);
    this.options |= (skip ? KeyListReader.ListIterator.HAS_SKIPS : 0x0);
    this.options |= KeyListReader.ListIterator.HAS_MAXTF;
    // more options here?
  }

  @Override
  public void processWord(byte[] wordBytes) throws IOException {
    if (invertedList != null) {
      if (estimateDocumentCount) {
        longestPostingList = Math.max(longestPostingList, invertedList.documentCount);
      }
      collectionLength += invertedList.totalWindowCount;
      invertedList.close();
      writer.add(invertedList);

      invertedList = null;
    }

    invertedList = new CountsList();
    invertedList.setWord(wordBytes);
    assert lastWord == null || 0 != Utility.compare(lastWord, wordBytes) : "Duplicate word";
    lastWord = wordBytes;

    vocabCount++;
  }

  @Override
  public void processDocument(int document) throws IOException {
    invertedList.addDocument(document);
    if (calculateDocumentCount) {
      this.uniqueDocSet.add(document);
    }
  }
  int currentBegin;

  @Override
  public void processTuple(int count) throws IOException {
    invertedList.addCount(count);
  }

  @Override
  public void close() throws IOException {
    if (invertedList != null) {
      if (estimateDocumentCount) {
        longestPostingList = Math.max(longestPostingList, invertedList.documentCount);
      }
      collectionLength += invertedList.totalWindowCount;
      invertedList.close();
      writer.add(invertedList);
    }

    // Add stats to the manifest if needed
    Parameters manifest = writer.getManifest();
    if (!manifest.isLong("statistics/collectionLength")) {
      manifest.set("statistics/collectionLength", collectionLength);
    }
    if (!manifest.isLong("statistics/vocabCount")) {
      manifest.set("statistics/vocabCount", vocabCount);
    }
    if (!manifest.isLong("statistics/documentCount")) {
      if (this.estimateDocumentCount) {
        manifest.set("statistics/documentCount", this.longestPostingList);
      } else if (this.calculateDocumentCount) {
        manifest.set("statistics/documentCount", this.uniqueDocSet.size());
        this.uniqueDocSet.clear();
      } else {
        Logger.getLogger(this.getClass().getName()).info("Could NOT find, calculate, or estimate a document count.");
      }
    }

    writer.close();
  }

  public static void verify(TupleFlowParameters parameters, ErrorHandler handler) {
    if (!parameters.getJSON().isString("filename")) {
      handler.addError("CountIndexWriter requires a 'filename' parameter.");
      return;
    }

    String index = parameters.getJSON().getString("filename");
    Verification.requireWriteableFile(index, handler);
  }

  public void setProcessor(Step processor) throws IncompatibleProcessorException {
    writer.setProcessor(processor);
  }

  public class CountsList implements IndexElement {

    public CountsList() {
      documents = new CompressedRawByteBuffer();
      counts = new CompressedRawByteBuffer();
      header = new CompressedByteBuffer();

      if ((options & KeyListReader.ListIterator.HAS_SKIPS) == KeyListReader.ListIterator.HAS_SKIPS) {
        skips = new CompressedRawByteBuffer();
        skipPositions = new CompressedRawByteBuffer();
      } else {
        skips = null;
      }
    }

    public void close() throws IOException {

      if (documents.length() > 0) {
        counts.add(positionCount);
        maximumPositionCount = Math.max(maximumPositionCount, positionCount);
      }

      if (skips != null && skips.length() == 0) {
        // not adding skip information b/c its empty
        options &= (0xffff - KeyListReader.ListIterator.HAS_SKIPS);
        header.add(options);
      } else {
        header.add(options);
      }

      header.add(documentCount);
      header.add(totalWindowCount);
      header.add(maximumPositionCount);
      if (skips != null && skips.length() > 0) {
        header.add(skipDistance);
        header.add(skipResetDistance);
        header.add(numSkips);
      }

      header.add(documents.length());
      header.add(counts.length());
      if (skips != null && skips.length() > 0) {
        header.add(skips.length());
        header.add(skipPositions.length());
      }
    }

    public long dataLength() {
      long listLength = 0;

      listLength += header.length();
      listLength += counts.length();
      listLength += documents.length();
      if (skips != null) {
        listLength += skips.length();
        listLength += skipPositions.length();
      }

      return listLength;
    }

    public void write(final OutputStream output) throws IOException {
      header.write(output);
      header.clear();

      documents.write(output);
      documents.clear();

      counts.write(output);
      counts.clear();

      if (skips != null && skips.length() > 0) {
        skips.write(output);
        skips.clear();
        skipPositions.write(output);
        skipPositions.clear();
      }
    }

    public byte[] key() {
      return word;
    }

    public void setWord(byte[] word) {
      this.word = word;
      this.lastDocument = 0;

      this.totalWindowCount = 0;
      this.positionCount = 0;
      this.maximumPositionCount = 0;
      if (skips != null) {
        this.docsSinceLastSkip = 0;
        this.lastSkipPosition = 0;
        this.lastDocumentSkipped = 0;
        this.lastDocumentSkip = 0;
        this.lastCountSkip = 0;
        this.numSkips = 0;
      }

    }

    public void addDocument(long documentID) throws IOException {
      // add the last document's counts
      if (documents.length() > 0) {
        counts.add(positionCount);
        maximumPositionCount = Math.max(maximumPositionCount, positionCount);

        // if we're skipping check that
        if (skips != null) {
          updateSkipInformation();
        }
      }
      documents.add(documentID - lastDocument);
      lastDocument = documentID;


      positionCount = 0;
      documentCount++;

    }

    public void addCount(int count) throws IOException {
      positionCount += count;
      totalWindowCount += count;
    }

    private void updateSkipInformation() {
      // There are already docs entered and we've gone skipDistance docs -- make a skip
      docsSinceLastSkip = (docsSinceLastSkip + 1) % skipDistance;
      if (documents.length() > 0 && docsSinceLastSkip == 0) {
        skips.add(lastDocument - lastDocumentSkipped);
        skips.add(skipPositions.length() - lastSkipPosition);
        lastDocumentSkipped = lastDocument;
        lastSkipPosition = skipPositions.length();

        // Now we decide whether we're storing an abs. value d-gapped value
        if (numSkips % skipResetDistance == 0) {
          // absolute values
          skipPositions.add(documents.length());
          skipPositions.add(counts.length());
          lastDocumentSkip = documents.length();
          lastCountSkip = counts.length();
        } else {
          // d-gap skip
          skipPositions.add(documents.length() - lastDocumentSkip);
          skipPositions.add(counts.length() - lastCountSkip);
        }
        numSkips++;
      }
    }
    private long lastDocument;
    private int positionCount;
    private int documentCount;
    private int totalWindowCount;
    private int maximumPositionCount;
    public byte[] word;
    public CompressedByteBuffer header;
    public CompressedRawByteBuffer documents;
    public CompressedRawByteBuffer counts;
    // to support skipping
    private long lastDocumentSkipped;
    private long lastSkipPosition;
    private long lastDocumentSkip;
    private long lastCountSkip;
    private long numSkips;
    private int docsSinceLastSkip;
    private CompressedRawByteBuffer skips;
    private CompressedRawByteBuffer skipPositions;
  }
}
