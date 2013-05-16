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
import org.lemurproject.galago.core.index.mem.MemoryPositionalIndex;
import org.lemurproject.galago.core.index.merge.PositionIndexMerger;
import org.lemurproject.galago.core.parse.NumericParameterAccumulator;
import org.lemurproject.galago.core.types.KeyValuePair;
import org.lemurproject.galago.core.types.NumberWordPosition;
import org.lemurproject.galago.tupleflow.IncompatibleProcessorException;
import org.lemurproject.galago.tupleflow.InputClass;
import org.lemurproject.galago.tupleflow.Parameters;
import org.lemurproject.galago.tupleflow.OutputClass;
import org.lemurproject.galago.tupleflow.Source;
import org.lemurproject.galago.tupleflow.Step;
import org.lemurproject.galago.tupleflow.TupleFlowParameters;
import org.lemurproject.galago.tupleflow.Utility;
import org.lemurproject.galago.tupleflow.execution.ErrorHandler;
import org.lemurproject.galago.tupleflow.execution.Verification;

/**
 * 12/14/2010 (irmarc): Adding a skip list to this structure. It's pretty basic
 * - we have a predefined skip distance in terms of how many entries to skip. A
 * skip is a two-tier structure:
 *
 * 1st tier: [d-gap doc id, d-gap byte offset to tier 2] 2nd tier: [docs byte
 * pos, counts byte pos, positions byte pos]
 *
 * Documents are d-gapped, but we already have those in tier 1. Counts are not
 * d-gapped b/c they store the # of positions, so they don't monotonically
 * track. Positions are self-contained (reset at a new doc boundary), so we only
 * need the byte information in tier 2.
 *
 * Some variable names: skipDistance: the maximum number of documents we store
 * generating a skip. skipResetDisance: the number of skips we generate before
 * we reset the offset base. Instead of storing the absolute values in the 2nd
 * tier, all entries that are some factor x*skipResetDistance are absolute
 * values, and all values until (x+1)*skipResetDistance entries away are
 * d-gapped off that absolute value so there are a few extra reads (or if you're
 * clever only one extra read), but it keeps the 2nd tier values from ballooning
 * fast, and we don't need to read them all in order to recover the original
 * values.
 *
 * @author trevor, irmarc
 */
@InputClass(className = "org.lemurproject.galago.core.types.NumberWordPosition", order = {"+word", "+document", "+position"})
@OutputClass(className = "org.lemurproject.galago.core.types.KeyValuePair", order = {"+key"})
public class PositionIndexWriter implements
        NumberWordPosition.WordDocumentPositionOrder.ShreddedProcessor {
    static final int MARKER_MINIMUM = 2;

  // writer variables //
  Parameters actualParams;
  BTreeWriter writer;
  PositionsList invertedList;
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
   * Creates a new instance of PositionIndexWriter
   */
  public PositionIndexWriter(TupleFlowParameters parameters) throws FileNotFoundException, IOException {
    actualParams = parameters.getJSON();
    actualParams.set("writerClass", getClass().getName());
    actualParams.set("readerClass", PositionIndexReader.class.getName());
    actualParams.set("mergerClass", PositionIndexMerger.class.getName());
    actualParams.set("memoryClass", MemoryPositionalIndex.class.getName());
    actualParams.set("defaultOperator", "counts");

    // vocab and collection length can be calculated - doc count is more complex
    // option 1: predefined doccount
    if (actualParams.containsKey("statistics/documentCount")) {
      // great.
      // option 2: there is a doccount pipe
    } else if (actualParams.isString("pipename")) {
      Parameters docCounts = NumericParameterAccumulator.accumulateParameters(parameters.getTypeReader(actualParams.getString("pipename")));
      actualParams.set("statistics/documentCount", docCounts.getMap("documentCount").getLong("global"));

      // option 3: estimated document count - as longest posting list
    } else if (actualParams.isBoolean("estimateDocumentCount")) {
      estimateDocumentCount = true;
      longestPostingList = 0;

      // option 4: default option - calculate document count (this may be a problem - it requires memory)
    } else {
      actualParams.set("calculateDocumentCount", true);
      calculateDocumentCount = true;
      uniqueDocSet = new TIntHashSet();
    }

    writer = new DiskBTreeWriter(parameters);

    // look for skips
    boolean skip = parameters.getJSON().get("skipping", true);
    skipDistance = (int) parameters.getJSON().get("skipDistance", 500);
    skipResetDistance = (int) parameters.getJSON().get("skipResetDistance", 20);
    options |= (skip ? KeyListReader.ListIterator.HAS_SKIPS : 0x0);
    options |= KeyListReader.ListIterator.HAS_MAXTF;
    options |= KeyListReader.ListIterator.HAS_INLINING;
  }

  @Override
  public void processWord(byte[] wordBytes) throws IOException {
    if (invertedList != null) {
      if (estimateDocumentCount) {
        longestPostingList = Math.max(longestPostingList, invertedList.documentCount);
      }
      collectionLength += invertedList.totalPositionCount;
      invertedList.close();
      writer.add(invertedList);

      invertedList = null;
    }

    invertedList = new PositionsList();
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

  @Override
  public void processPosition(int position) throws IOException {
    invertedList.addPosition(position);
  }

  @Override
  public void processTuple() {
    // does nothing - this means we ignore duplicate postings.
  }

  @Override
  public void close() throws IOException {
    if (invertedList != null) {
      if (estimateDocumentCount) {
        longestPostingList = Math.max(longestPostingList, invertedList.documentCount);
      }
      collectionLength += invertedList.totalPositionCount;
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
      handler.addError("PositionIndexWriter requires a 'filename' parameter.");
      return;
    }

    String index = parameters.getJSON().getString("filename");
    Verification.requireWriteableFile(index, handler);
  }

  public class PositionsList implements IndexElement {

    private long lastDocument;
    private int lastPosition;
    private int positionCount;
    private int documentCount;
    private int maximumPositionCount;
    private int totalPositionCount;
    public byte[] word;
    public CompressedByteBuffer header;
    public CompressedRawByteBuffer documents;
    public CompressedRawByteBuffer counts;
    public CompressedRawByteBuffer positions;
    public CompressedByteBuffer positionBlock;
    // to support skipping
    private long lastDocumentSkipped;
    private long lastSkipPosition;
    private long lastDocumentSkip;
    private long lastCountSkip;
    private long lastPositionSkip;
    private long numSkips;
    private int docsSinceLastSkip;
    private CompressedRawByteBuffer skips;
    private CompressedRawByteBuffer skipPositions;

    public PositionsList() {
      documents = new CompressedRawByteBuffer();
      counts = new CompressedRawByteBuffer();
      positions = new CompressedRawByteBuffer();
      positionBlock = new CompressedByteBuffer();
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

        // Now conditionally add in the skip marker and the array of position bytes
	if (positionCount > MARKER_MINIMUM) {
	    positions.add(positionBlock.length());
	}
        positions.add(positionBlock);
        maximumPositionCount = Math.max(maximumPositionCount, positionCount);


      }

      if (skips != null && skips.length() == 0) {
        // not adding skip information b/c its empty
        options &= (0xffff - KeyListReader.ListIterator.HAS_SKIPS);
        header.add(options);
      } else {
        header.add(options);
      }

      // Start with the inline length
      header.add(MARKER_MINIMUM);

      header.add(documentCount);
      header.add(totalPositionCount);
      header.add(maximumPositionCount);
      if (skips != null && skips.length() > 0) {
        header.add(skipDistance);
        header.add(skipResetDistance);
        header.add(numSkips);
      }

      header.add(documents.length());
      header.add(counts.length());
      header.add(positions.length());
      if (skips != null && skips.length() > 0) {
        header.add(skips.length());
        header.add(skipPositions.length());
      }
    }

    public long dataLength() {
      long listLength = 0;

      listLength += header.length();
      listLength += counts.length();
      listLength += positions.length();
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

      positions.write(output);
      positions.clear();

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
      this.lastPosition = 0;
      this.totalPositionCount = 0;
      this.maximumPositionCount = 0;
      this.positionCount = 0;
      if (skips != null) {
        this.docsSinceLastSkip = 0;
        this.lastSkipPosition = 0;
        this.lastDocumentSkipped = 0;
        this.lastDocumentSkip = 0;
        this.lastCountSkip = 0;
        this.lastPositionSkip = 0;
        this.numSkips = 0;
      }
    }

    public void addDocument(long documentID) throws IOException {
      // add the last document's counts
      if (documents.length() > 0) {
        counts.add(positionCount);

        // Now add in the skip marker and the array of position bytes
	if (positionCount > MARKER_MINIMUM) {
	    positions.add(positionBlock.length());
	}
        positions.add(positionBlock);
        maximumPositionCount = Math.max(maximumPositionCount, positionCount);

        // if we're skipping check that
        if (skips != null) {
          updateSkipInformation();
        }
      }
      documents.add(documentID - lastDocument);
      lastDocument = documentID;

      lastPosition = 0;
      positionCount = 0;
      positionBlock.clear();
      documentCount++;

    }

    public void addPosition(int position) throws IOException {
      positionCount++;
      totalPositionCount++;
      positionBlock.add(position - lastPosition);
      lastPosition = position;
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
          skipPositions.add(positions.length());
          lastDocumentSkip = documents.length();
          lastCountSkip = counts.length();
          lastPositionSkip = positions.length();
        } else {
          // d-gap skip
          skipPositions.add(documents.length() - lastDocumentSkip);
          skipPositions.add(counts.length() - lastCountSkip);
          skipPositions.add((long) (positions.length() - lastPositionSkip));
        }
        numSkips++;
      }
    }
  }
}
