// BSD License (http://lemurproject.org/galago-license)
package ciir.proteus.index;

import ciir.proteus.index.PseudoDocument.PseudoDocumentComponents;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lemurproject.galago.core.index.BTreeReader;
import org.lemurproject.galago.core.index.KeyToListIterator;
import org.lemurproject.galago.core.index.KeyValueReader;
import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.parse.TagTokenizer;
import org.lemurproject.galago.core.retrieval.iterator.BaseIterator;
import org.lemurproject.galago.core.retrieval.iterator.DataIterator;
import org.lemurproject.galago.core.retrieval.query.AnnotatedNode;
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.core.retrieval.query.NodeType;
import org.lemurproject.galago.tupleflow.FakeParameters;
import org.lemurproject.galago.tupleflow.Parameters;
import org.lemurproject.galago.tupleflow.Utility;

/**
 * Extracted Corpus for Proteus' PseudoDocuments
 *
 * @author sjh, jfoley
 */
public class PseudoCorpusReader extends KeyValueReader {

  TagTokenizer tokenizer;
  boolean psuedoDocs;
  
  public PseudoCorpusReader(String fileName) throws FileNotFoundException, IOException {
    super(fileName);
    init();
  }

  public PseudoCorpusReader(BTreeReader r) {
    super(r);
    init();
  }

  public void init(){
    Parameters manifest = getManifest();
    psuedoDocs = manifest.get("psuedo", false);
    if(manifest.containsKey("tokenizer")){
      tokenizer = new TagTokenizer(new FakeParameters(getManifest().getMap("tokenizer")));
    } else {
      tokenizer = new TagTokenizer(new FakeParameters(new Parameters()));
    }
  }
  
  @Override
  public PseudoCorpusReader.KeyIterator getIterator() throws IOException {
    return new PseudoCorpusReader.KeyIterator(reader);
  }

  public PseudoDocument getDocument(byte[] key, PseudoDocumentComponents p) throws IOException {
    PseudoCorpusReader.KeyIterator i = new PseudoCorpusReader.KeyIterator(reader);
    if (i.findKey(key)) {
      return i.getDocument(p);
    } else {
      return null;
    }
  }


  public PseudoDocument getDocument(int key, PseudoDocumentComponents p) throws IOException {
    PseudoCorpusReader.KeyIterator i = new PseudoCorpusReader.KeyIterator(reader);
    byte[] k = Utility.fromInt(key);
    if (i.findKey(k)) {
      return i.getDocument(p);
    } else {
      return null;
    }
  }

  @Override
  public Map<String, NodeType> getNodeTypes() {
    HashMap<String, NodeType> types = new HashMap<String, NodeType>();
    types.put("corpus", new NodeType(PseudoCorpusReader.CorpusIterator.class));
    return types;
  }

  @Override
  public BaseIterator getIterator(Node node) throws IOException {
    if (node.getOperator().equals("corpus")) {
      return new PseudoCorpusReader.CorpusIterator(new PseudoCorpusReader.KeyIterator(reader));
    } else {
      throw new UnsupportedOperationException(
              "Index doesn't support operator: " + node.getOperator());
    }
  }

  public class KeyIterator extends KeyValueReader.KeyValueIterator {

    public KeyIterator(BTreeReader reader) throws IOException {
      super(reader);
    }

    @Override
    public String getKeyString() {
      return Integer.toString(Utility.toInt(getKey()));
    }

    public PseudoDocument getDocument(PseudoDocumentComponents p) throws IOException {
      return PseudoDocument.deserialize(iterator.getValueBytes(), p);
    }

    @Override
    public String getValueString() throws IOException {
      try {
        return getDocument(new PseudoDocumentComponents(true, false, false, false)).toString();
      } catch (IOException ex) {
        throw new RuntimeException(ex);
      }
    }

    @Override
    public BaseIterator getValueIterator() throws IOException {
      return new PseudoCorpusReader.CorpusIterator(this);
    }
  }

  public class CorpusIterator extends KeyToListIterator implements DataIterator<Document> {

    PseudoDocumentComponents docParams;

    public CorpusIterator(PseudoCorpusReader.KeyIterator ki) {
      super(ki);
      docParams = new PseudoDocumentComponents(true,false,false,false);
    }

    @Override
    public String getValueString() throws IOException {
      return ((PseudoCorpusReader.KeyIterator) iterator).getValueString();
    }

    @Override
    public long totalEntries() {
      return reader.getManifest().getLong("keyCount");
    }

    @Override
    public Document getData() {
      if (context.document != this.currentCandidate()) {
        try {
          return ((PseudoCorpusReader.KeyIterator) iterator).getDocument(docParams);
        } catch (IOException ioe) {
          throw new RuntimeException(ioe);
        }
      } else {
        return null;
      }
    }

    @Override
    public boolean hasAllCandidates() {
      return true;
    }

    @Override
    public String getKeyString() {
      return "corpus";
    }

    @Override
    public AnnotatedNode getAnnotatedNode() {
      String type = "corpus";
      String className = this.getClass().getSimpleName();
      String parameters = "";
      int document = currentCandidate();
      boolean atCandidate = hasMatch(this.context.document);
      String returnValue = getData().name;
      String extraInfo = getData().toString();
      List<AnnotatedNode> children = Collections.EMPTY_LIST;

      return new AnnotatedNode(type, className, parameters, document, atCandidate, returnValue, extraInfo, children);
    }
  }
}