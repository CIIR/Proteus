// BSD License (http://lemurproject.org/galago-license)
package ciir.proteus.index;

import ciir.proteus.index.PseudoDocument.PseudoDocumentComponents;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.lemurproject.galago.core.parse.DocumentStreamParser;
import org.lemurproject.galago.core.types.DocumentSplit;
import org.lemurproject.galago.tupleflow.Parameters;
import org.lemurproject.galago.tupleflow.Utility;

/**
 * Reads Document data from an index file. Typically you'd use this parser by
 * including UniversalParser in a TupleFlow Job.
 *
 * @author trevor, sjh
 */
public class PseudoCorpusSplitParser extends DocumentStreamParser {

  PseudoCorpusReader reader;
  PseudoCorpusReader.KeyIterator iterator;
  DocumentSplit split;
  PseudoDocumentComponents extractionParameters;

  public PseudoCorpusSplitParser(DocumentSplit split) throws FileNotFoundException, IOException {
    this(split, new Parameters());
  }

  public PseudoCorpusSplitParser(DocumentSplit split, Parameters p) throws FileNotFoundException, IOException {
    super(split, p);
    reader = new PseudoCorpusReader(split.fileName);
    iterator = reader.getIterator();
    iterator.skipToKey(split.startKey);
    this.split = split;
    extractionParameters = new PseudoDocumentComponents(false, true, false, true);    
  }

  @Override
  public PseudoDocument nextDocument() throws IOException {
    if (reader != null && iterator.isDone()) {
      return null;
    }

    byte[] keyBytes = iterator.getKey();

    // Don't go past the end of the split.
    if (split.endKey.length > 0 && Utility.compare(keyBytes, split.endKey) >= 0) {
      return null;
    }

    PseudoDocument document = iterator.getDocument(extractionParameters);
    iterator.nextKey();
    return document;
  }

  @Override
  public void close() throws IOException {
    if (reader != null) {
      reader.close();
      reader = null;
      iterator = null;
    }
  }
}
