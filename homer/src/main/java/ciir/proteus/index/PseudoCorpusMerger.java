// BSD License (http://lemurproject.org/galago-license)
package ciir.proteus.index;

import ciir.proteus.index.PseudoDocument.PseudoDocumentComponents;
import org.lemurproject.galago.core.index.merge.GenericIndexMerger;
import org.lemurproject.galago.core.index.merge.KeyIteratorWrapper;

import java.io.IOException;
import java.util.List;

import org.lemurproject.galago.core.index.corpus.CorpusFileWriter;
import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.tupleflow.Processor;
import org.lemurproject.galago.tupleflow.TupleFlowParameters;

/**
 *
 * @author sjh
 */
public class PseudoCorpusMerger extends GenericIndexMerger<Document> {

  public PseudoCorpusMerger(TupleFlowParameters p) throws Exception {
    super(p);
  }

  @Override
  public boolean mappingKeys() {
    return true;
  }

  @Override
  public Processor<Document> createIndexWriter(TupleFlowParameters parameters) throws Exception {
    return new CorpusFileWriter(parameters);
  }

  @Override
  public void performValueMerge(byte[] key, List<KeyIteratorWrapper> keyIterators) throws IOException {
    assert (keyIterators.size() == 1) : "Found two identical keys when merging names. Documents can never be combined.";
    PseudoCorpusReader.KeyIterator iter = (PseudoCorpusReader.KeyIterator) (keyIterators.get(0).getIterator());
    PseudoDocument d = iter.getDocument(new PseudoDocumentComponents(true, true, true, true)) ;
    this.writer.process(d);
  }
}
