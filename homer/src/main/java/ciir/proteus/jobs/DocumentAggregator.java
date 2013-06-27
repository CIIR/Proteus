// BSD License (http://lemurproject.org/galago-license)
package ciir.proteus.jobs;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.types.KeyValuePair;
import org.lemurproject.galago.tupleflow.Counter;
import org.lemurproject.galago.tupleflow.IncompatibleProcessorException;
import org.lemurproject.galago.tupleflow.InputClass;
import org.lemurproject.galago.tupleflow.Linkage;
import org.lemurproject.galago.tupleflow.NullProcessor;
import org.lemurproject.galago.tupleflow.OutputClass;
import org.lemurproject.galago.tupleflow.Parameters;
import org.lemurproject.galago.tupleflow.Processor;
import org.lemurproject.galago.tupleflow.Source;
import org.lemurproject.galago.tupleflow.Step;
import org.lemurproject.galago.tupleflow.TupleFlowParameters;
import org.lemurproject.galago.tupleflow.Utility;
import org.lemurproject.galago.tupleflow.execution.Verified;
import org.xerial.snappy.SnappyInputStream;

@Verified
@InputClass(className = "org.lemurproject.galago.core.types.KeyValuePair", order = {"+key"})
@OutputClass(className = "org.lemurproject.galago.core.parse.Document")
public class DocumentAggregator implements KeyValuePair.KeyOrder.ShreddedProcessor, Source<Document> {
  
  public final class MergedDocument {
    private long identifier;
    private Map<String,String> metadata;
    private StringBuilder sb;
    private int numSamples = 0;
    private String name;

    private MergedDocument(Document d) {
      identifier = -1;
      metadata = new HashMap<String,String>();
      sb = new StringBuilder();
      name = d.name;
      
      addSample(d);
    }
    
    private void addAttribute(String key, String value) {
      sb.append(key).append("=\"").append(Utility.escape(value)).append("\" ");
    }

    private void addSample(Document d) {
      numSamples++;
      
      sb.append("<sample ");
      addAttribute("title", d.metadata.get("title"));
      addAttribute("src", d.metadata.get("src"));
      addAttribute("page", d.metadata.get("page"));
      addAttribute("pos", d.metadata.get("pos"));
      if(d.metadata.containsKey("link")) {
        String externalLink = d.metadata.get("link");
        addAttribute("link", externalLink);
        
        // if we don't have a link in the top metadata, take the first one
        if(!metadata.containsKey("link")) {
          metadata.put("link", externalLink);
        }
      }
      sb.append(">");
      
      for(String term : d.terms) {
        sb.append(term).append(' ');
      }
      
      sb.append("</sample>");
    }

    private Document result(long id) {
      metadata.put("numSamples", Integer.toString(numSamples));
      
      Document d = new Document();
      d.name = name;
      d.identifier = id;
      d.text = sb.toString();
      d.metadata = metadata;
      
      return d;
    }
 }
  
  // this gets overriden with the correct thing in Linkage.link
  public Processor<Document> processor = new NullProcessor(Document.class);
  
  @Override
  public void setProcessor(Step processor) throws IncompatibleProcessorException {
    Linkage.link(this, processor);
  }
  
  Counter docsIn, docsOut;
  long documentNumber = 0;
  byte[] lastIdentifier = null;
  Map<String, MergedDocument> bufferedDocuments;

  public DocumentAggregator(TupleFlowParameters parameters) throws IOException, FileNotFoundException {
    docsIn = parameters.getCounter("Documents in");
    docsOut = parameters.getCounter("Documents out");
    bufferedDocuments = new HashMap<String, MergedDocument>();
  }

  /**
   * If this key is new and we have a key already, we need to write the previous document.
   * @param key
   * @throws IOException 
   */
  @Override
  public void processKey(byte[] key) throws IOException {
    if(key == null) { return; }
    if (lastIdentifier != null && (Utility.compare(key, lastIdentifier) != 0)) {
      write();
    }
    lastIdentifier = key;
  }

  @Override
  public void processTuple(byte[] value) throws IOException {
    ByteArrayInputStream stream = new ByteArrayInputStream(value);
    Document document;
    try {
      ObjectInputStream input = new ObjectInputStream(new SnappyInputStream(stream));
      document = (Document) input.readObject();
      addToBuffer(document);
      if (docsIn != null) {
        docsIn.increment();
      }
    } catch (ClassNotFoundException cnfe) {
      throw new RuntimeException(cnfe);
    }
  }

  private void addToBuffer(Document d) {
    if (!bufferedDocuments.containsKey(d.name)) {
      bufferedDocuments.put(d.name, new MergedDocument(d));
    } else {
      bufferedDocuments.get(d.name).addSample(d);
    }
  }
  private Parameters emptyParameters = new Parameters();

  private void write() throws IOException {
    for (String nameKey : bufferedDocuments.keySet()) {
      ByteArrayOutputStream array = new ByteArrayOutputStream();
      
      MergedDocument pd = bufferedDocuments.get(nameKey);
      pd.identifier = documentNumber;
      Document doc = pd.result(documentNumber);
      processor.process(doc);
      
      documentNumber++;
      if (docsOut != null) {
        docsOut.increment();
      }
    }
    bufferedDocuments.clear();
  }

  @Override
  public void close() throws IOException {
    write();
    processor.close();
  }
}
