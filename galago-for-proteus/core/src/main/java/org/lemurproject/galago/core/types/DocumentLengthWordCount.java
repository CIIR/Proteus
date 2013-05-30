// This file was automatically generated with the command: 
//     java org.lemurproject.galago.tupleflow.typebuilder.TypeBuilderMojo ...
package org.lemurproject.galago.core.types;

import org.lemurproject.galago.tupleflow.Utility;
import org.lemurproject.galago.tupleflow.ArrayInput;
import org.lemurproject.galago.tupleflow.ArrayOutput;
import org.lemurproject.galago.tupleflow.Order;   
import org.lemurproject.galago.tupleflow.OrderedWriter;
import org.lemurproject.galago.tupleflow.Type; 
import org.lemurproject.galago.tupleflow.TypeReader;
import org.lemurproject.galago.tupleflow.Step; 
import org.lemurproject.galago.tupleflow.IncompatibleProcessorException;
import org.lemurproject.galago.tupleflow.ReaderSource;
import java.io.IOException;             
import java.io.EOFException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;   
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Collection;
import gnu.trove.list.array.TByteArrayList;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.list.array.TShortArrayList;


public class DocumentLengthWordCount implements Type<DocumentLengthWordCount> {
    public String document;
    public int length;
    public String word;
    public int count; 
    
    public DocumentLengthWordCount() {}
    public DocumentLengthWordCount(String document, int length, String word, int count) {
        this.document = document;
        this.length = length;
        this.word = word;
        this.count = count;
    }  
    
    public String toString() {
            return String.format("%s,%d,%s,%d",
                                   document, length, word, count);
    } 

    public Order<DocumentLengthWordCount> getOrder(String... spec) {
        if (Arrays.equals(spec, new String[] { "+document", "+length" })) {
            return new DocumentLengthOrder();
        }
        if (Arrays.equals(spec, new String[] { "+document", "+word" })) {
            return new DocumentWordOrder();
        }
        if (Arrays.equals(spec, new String[] { "+word", "+document" })) {
            return new WordDocumentOrder();
        }
        if (Arrays.equals(spec, new String[] { "+document" })) {
            return new DocumentOrder();
        }
        return null;
    } 
      
    public interface Processor extends Step, org.lemurproject.galago.tupleflow.Processor<DocumentLengthWordCount> {
        public void process(DocumentLengthWordCount object) throws IOException;
        public void close() throws IOException;
    }                        
    public interface Source extends Step {
    }
    public static class DocumentLengthOrder implements Order<DocumentLengthWordCount> {
        public int hash(DocumentLengthWordCount object) {
            int h = 0;
            h += Utility.hash(object.document);
            h += Utility.hash(object.length);
            return h;
        } 
        public Comparator<DocumentLengthWordCount> greaterThan() {
            return new Comparator<DocumentLengthWordCount>() {
                public int compare(DocumentLengthWordCount one, DocumentLengthWordCount two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.document, two.document);
                        if(result != 0) break;
                        result = + Utility.compare(one.length, two.length);
                        if(result != 0) break;
                    } while (false);
                    return -result;
                }
            };
        }     
        public Comparator<DocumentLengthWordCount> lessThan() {
            return new Comparator<DocumentLengthWordCount>() {
                public int compare(DocumentLengthWordCount one, DocumentLengthWordCount two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.document, two.document);
                        if(result != 0) break;
                        result = + Utility.compare(one.length, two.length);
                        if(result != 0) break;
                    } while (false);
                    return result;
                }
            };
        }     
        public TypeReader<DocumentLengthWordCount> orderedReader(ArrayInput _input) {
            return new ShreddedReader(_input);
        }    

        public TypeReader<DocumentLengthWordCount> orderedReader(ArrayInput _input, int bufferSize) {
            return new ShreddedReader(_input, bufferSize);
        }    
        public OrderedWriter<DocumentLengthWordCount> orderedWriter(ArrayOutput _output) {
            ShreddedWriter w = new ShreddedWriter(_output);
            return new OrderedWriterClass(w); 
        }                                    
        public static class OrderedWriterClass extends OrderedWriter< DocumentLengthWordCount > {
            DocumentLengthWordCount last = null;
            ShreddedWriter shreddedWriter = null; 
            
            public OrderedWriterClass(ShreddedWriter s) {
                this.shreddedWriter = s;
            }
            
            public void process(DocumentLengthWordCount object) throws IOException {
               boolean processAll = false;
               if (processAll || last == null || 0 != Utility.compare(object.document, last.document)) { processAll = true; shreddedWriter.processDocument(object.document); }
               if (processAll || last == null || 0 != Utility.compare(object.length, last.length)) { processAll = true; shreddedWriter.processLength(object.length); }
               shreddedWriter.processTuple(object.word, object.count);
               last = object;
            }           
                 
            public void close() throws IOException {
                shreddedWriter.close();
            }
            
            public Class<DocumentLengthWordCount> getInputClass() {
                return DocumentLengthWordCount.class;
            }
        } 
        public ReaderSource<DocumentLengthWordCount> orderedCombiner(Collection<TypeReader<DocumentLengthWordCount>> readers, boolean closeOnExit) {
            ArrayList<ShreddedReader> shreddedReaders = new ArrayList();
            
            for (TypeReader<DocumentLengthWordCount> reader : readers) {
                shreddedReaders.add((ShreddedReader)reader);
            }
            
            return new ShreddedCombiner(shreddedReaders, closeOnExit);
        }                  
        public DocumentLengthWordCount clone(DocumentLengthWordCount object) {
            DocumentLengthWordCount result = new DocumentLengthWordCount();
            if (object == null) return result;
            result.document = object.document; 
            result.length = object.length; 
            result.word = object.word; 
            result.count = object.count; 
            return result;
        }                 
        public Class<DocumentLengthWordCount> getOrderedClass() {
            return DocumentLengthWordCount.class;
        }                           
        public String[] getOrderSpec() {
            return new String[] {"+document", "+length"};
        }

        public static String[] getSpec() {
            return new String[] {"+document", "+length"};
        }
        public static String getSpecString() {
            return "+document +length";
        }
                           
        public interface ShreddedProcessor extends Step {
            public void processDocument(String document) throws IOException;
            public void processLength(int length) throws IOException;
            public void processTuple(String word, int count) throws IOException;
            public void close() throws IOException;
        }    
        public interface ShreddedSource extends Step {
        }                                              
        
        public static class ShreddedWriter implements ShreddedProcessor {
            ArrayOutput output;
            ShreddedBuffer buffer = new ShreddedBuffer();
            String lastDocument;
            int lastLength;
            boolean lastFlush = false;
            
            public ShreddedWriter(ArrayOutput output) {
                this.output = output;
            }                        
            
            public void close() throws IOException {
                flush();
            }
            
            public void processDocument(String document) {
                lastDocument = document;
                buffer.processDocument(document);
            }
            public void processLength(int length) {
                lastLength = length;
                buffer.processLength(length);
            }
            public final void processTuple(String word, int count) throws IOException {
                if (lastFlush) {
                    if(buffer.documents.size() == 0) buffer.processDocument(lastDocument);
                    if(buffer.lengths.size() == 0) buffer.processLength(lastLength);
                    lastFlush = false;
                }
                buffer.processTuple(word, count);
                if (buffer.isFull())
                    flush();
            }
            public final void flushTuples(int pauseIndex) throws IOException {
                
                while (buffer.getReadIndex() < pauseIndex) {
                           
                    output.writeString(buffer.getWord());
                    output.writeInt(buffer.getCount());
                    buffer.incrementTuple();
                }
            }  
            public final void flushDocument(int pauseIndex) throws IOException {
                while (buffer.getReadIndex() < pauseIndex) {
                    int nextPause = buffer.getDocumentEndIndex();
                    int count = nextPause - buffer.getReadIndex();
                    
                    output.writeString(buffer.getDocument());
                    output.writeInt(count);
                    buffer.incrementDocument();
                      
                    flushLength(nextPause);
                    assert nextPause == buffer.getReadIndex();
                }
            }
            public final void flushLength(int pauseIndex) throws IOException {
                while (buffer.getReadIndex() < pauseIndex) {
                    int nextPause = buffer.getLengthEndIndex();
                    int count = nextPause - buffer.getReadIndex();
                    
                    output.writeInt(buffer.getLength());
                    output.writeInt(count);
                    buffer.incrementLength();
                      
                    flushTuples(nextPause);
                    assert nextPause == buffer.getReadIndex();
                }
            }
            public void flush() throws IOException { 
                flushDocument(buffer.getWriteIndex());
                buffer.reset(); 
                lastFlush = true;
            }                           
        }
        public static class ShreddedBuffer {
            ArrayList<String> documents = new ArrayList();
            TIntArrayList lengths = new TIntArrayList();
            TIntArrayList documentTupleIdx = new TIntArrayList();
            TIntArrayList lengthTupleIdx = new TIntArrayList();
            int documentReadIdx = 0;
            int lengthReadIdx = 0;
                            
            String[] words;
            int[] counts;
            int writeTupleIndex = 0;
            int readTupleIndex = 0;
            int batchSize;

            public ShreddedBuffer(int batchSize) {
                this.batchSize = batchSize;

                words = new String[batchSize];
                counts = new int[batchSize];
            }                              

            public ShreddedBuffer() {    
                this(10000);
            }                                                                                                                    
            
            public void processDocument(String document) {
                documents.add(document);
                documentTupleIdx.add(writeTupleIndex);
            }                                      
            public void processLength(int length) {
                lengths.add(length);
                lengthTupleIdx.add(writeTupleIndex);
            }                                      
            public void processTuple(String word, int count) {
                assert documents.size() > 0;
                assert lengths.size() > 0;
                words[writeTupleIndex] = word;
                counts[writeTupleIndex] = count;
                writeTupleIndex++;
            }
            public void resetData() {
                documents.clear();
                lengths.clear();
                documentTupleIdx.clear();
                lengthTupleIdx.clear();
                writeTupleIndex = 0;
            }                  
                                 
            public void resetRead() {
                readTupleIndex = 0;
                documentReadIdx = 0;
                lengthReadIdx = 0;
            } 

            public void reset() {
                resetData();
                resetRead();
            } 
            public boolean isFull() {
                return writeTupleIndex >= batchSize;
            }

            public boolean isEmpty() {
                return writeTupleIndex == 0;
            }                          

            public boolean isAtEnd() {
                return readTupleIndex >= writeTupleIndex;
            }           
            public void incrementDocument() {
                documentReadIdx++;  
            }                                                                                              

            public void autoIncrementDocument() {
                while (readTupleIndex >= getDocumentEndIndex() && readTupleIndex < writeTupleIndex)
                    documentReadIdx++;
            }                 
            public void incrementLength() {
                lengthReadIdx++;  
            }                                                                                              

            public void autoIncrementLength() {
                while (readTupleIndex >= getLengthEndIndex() && readTupleIndex < writeTupleIndex)
                    lengthReadIdx++;
            }                 
            public void incrementTuple() {
                readTupleIndex++;
            }                    
            public int getDocumentEndIndex() {
                if ((documentReadIdx+1) >= documentTupleIdx.size())
                    return writeTupleIndex;
                return documentTupleIdx.get(documentReadIdx+1);
            }

            public int getLengthEndIndex() {
                if ((lengthReadIdx+1) >= lengthTupleIdx.size())
                    return writeTupleIndex;
                return lengthTupleIdx.get(lengthReadIdx+1);
            }
            public int getReadIndex() {
                return readTupleIndex;
            }   

            public int getWriteIndex() {
                return writeTupleIndex;
            } 
            public String getDocument() {
                assert readTupleIndex < writeTupleIndex;
                assert documentReadIdx < documents.size();
                
                return documents.get(documentReadIdx);
            }
            public int getLength() {
                assert readTupleIndex < writeTupleIndex;
                assert lengthReadIdx < lengths.size();
                
                return lengths.get(lengthReadIdx);
            }
            public String getWord() {
                assert readTupleIndex < writeTupleIndex;
                return words[readTupleIndex];
            }                                         
            public int getCount() {
                assert readTupleIndex < writeTupleIndex;
                return counts[readTupleIndex];
            }                                         
            public void copyTuples(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                   output.processTuple(getWord(), getCount());
                   incrementTuple();
                }
            }                                                                           
            public void copyUntilIndexDocument(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                    output.processDocument(getDocument());
                    assert getDocumentEndIndex() <= endIndex;
                    copyUntilIndexLength(getDocumentEndIndex(), output);
                    incrementDocument();
                }
            } 
            public void copyUntilIndexLength(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                    output.processLength(getLength());
                    assert getLengthEndIndex() <= endIndex;
                    copyTuples(getLengthEndIndex(), output);
                    incrementLength();
                }
            }  
            public void copyUntilDocument(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                while (!isAtEnd()) {
                    if (other != null) {   
                        assert !other.isAtEnd();
                        int c = + Utility.compare(getDocument(), other.getDocument());
                    
                        if (c > 0) {
                            break;   
                        }
                        
                        output.processDocument(getDocument());
                                      
                        if (c < 0) {
                            copyUntilIndexLength(getDocumentEndIndex(), output);
                        } else if (c == 0) {
                            copyUntilLength(other, output);
                            autoIncrementDocument();
                            break;
                        }
                    } else {
                        output.processDocument(getDocument());
                        copyUntilIndexLength(getDocumentEndIndex(), output);
                    }
                    incrementDocument();  
                    
               
                }
            }
            public void copyUntilLength(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                while (!isAtEnd()) {
                    if (other != null) {   
                        assert !other.isAtEnd();
                        int c = + Utility.compare(getLength(), other.getLength());
                    
                        if (c > 0) {
                            break;   
                        }
                        
                        output.processLength(getLength());
                                      
                        copyTuples(getLengthEndIndex(), output);
                    } else {
                        output.processLength(getLength());
                        copyTuples(getLengthEndIndex(), output);
                    }
                    incrementLength();  
                    
                    if (getDocumentEndIndex() <= readTupleIndex)
                        break;   
                }
            }
            public void copyUntil(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                copyUntilDocument(other, output);
            }
            
        }                         
        public static class ShreddedCombiner implements ReaderSource<DocumentLengthWordCount>, ShreddedSource {   
            public ShreddedProcessor processor;
            Collection<ShreddedReader> readers;       
            boolean closeOnExit = false;
            boolean uninitialized = true;
            PriorityQueue<ShreddedReader> queue = new PriorityQueue<ShreddedReader>();
            
            public ShreddedCombiner(Collection<ShreddedReader> readers, boolean closeOnExit) {
                this.readers = readers;                                                       
                this.closeOnExit = closeOnExit;
            }
                                  
            public void setProcessor(Step processor) throws IncompatibleProcessorException {  
                if (processor instanceof ShreddedProcessor) {
                    this.processor = new DuplicateEliminator((ShreddedProcessor) processor);
                } else if (processor instanceof DocumentLengthWordCount.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((DocumentLengthWordCount.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<DocumentLengthWordCount>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<DocumentLengthWordCount> getOutputClass() {
                return DocumentLengthWordCount.class;
            }
            
            public void initialize() throws IOException {
                for (ShreddedReader reader : readers) {
                    reader.fill();                                        
                    
                    if (!reader.getBuffer().isAtEnd())
                        queue.add(reader);
                }   

                uninitialized = false;
            }

            public void run() throws IOException {
                initialize();
               
                while (queue.size() > 0) {
                    ShreddedReader top = queue.poll();
                    ShreddedReader next = null;
                    ShreddedBuffer nextBuffer = null; 
                    
                    assert !top.getBuffer().isAtEnd();
                                                  
                    if (queue.size() > 0) {
                        next = queue.peek();
                        nextBuffer = next.getBuffer();
                        assert !nextBuffer.isAtEnd();
                    }
                    
                    top.getBuffer().copyUntil(nextBuffer, processor);
                    if (top.getBuffer().isAtEnd())
                        top.fill();                 
                        
                    if (!top.getBuffer().isAtEnd())
                        queue.add(top);
                }              
                
                if (closeOnExit)
                    processor.close();
            }

            public DocumentLengthWordCount read() throws IOException {
                if (uninitialized)
                    initialize();

                DocumentLengthWordCount result = null;

                while (queue.size() > 0) {
                    ShreddedReader top = queue.poll();
                    result = top.read();

                    if (result != null) {
                        if (top.getBuffer().isAtEnd())
                            top.fill();

                        queue.offer(top);
                        break;
                    } 
                }

                return result;
            }
        } 
        public static class ShreddedReader implements Step, Comparable<ShreddedReader>, TypeReader<DocumentLengthWordCount>, ShreddedSource {      
            public ShreddedProcessor processor;
            ShreddedBuffer buffer;
            DocumentLengthWordCount last = new DocumentLengthWordCount();         
            long updateDocumentCount = -1;
            long updateLengthCount = -1;
            long tupleCount = 0;
            long bufferStartCount = 0;  
            ArrayInput input;
            
            public ShreddedReader(ArrayInput input) {
                this.input = input; 
                this.buffer = new ShreddedBuffer();
            }                               
            
            public ShreddedReader(ArrayInput input, int bufferSize) { 
                this.input = input;
                this.buffer = new ShreddedBuffer(bufferSize);
            }
                 
            public final int compareTo(ShreddedReader other) {
                ShreddedBuffer otherBuffer = other.getBuffer();
                
                if (buffer.isAtEnd() && otherBuffer.isAtEnd()) {
                    return 0;                 
                } else if (buffer.isAtEnd()) {
                    return -1;
                } else if (otherBuffer.isAtEnd()) {
                    return 1;
                }
                                   
                int result = 0;
                do {
                    result = + Utility.compare(buffer.getDocument(), otherBuffer.getDocument());
                    if(result != 0) break;
                    result = + Utility.compare(buffer.getLength(), otherBuffer.getLength());
                    if(result != 0) break;
                } while (false);                                             
                
                return result;
            }
            
            public final ShreddedBuffer getBuffer() {
                return buffer;
            }                
            
            public final DocumentLengthWordCount read() throws IOException {
                if (buffer.isAtEnd()) {
                    fill();             
                
                    if (buffer.isAtEnd()) {
                        return null;
                    }
                }
                      
                assert !buffer.isAtEnd();
                DocumentLengthWordCount result = new DocumentLengthWordCount();
                
                result.document = buffer.getDocument();
                result.length = buffer.getLength();
                result.word = buffer.getWord();
                result.count = buffer.getCount();
                
                buffer.incrementTuple();
                buffer.autoIncrementDocument();
                buffer.autoIncrementLength();
                
                return result;
            }           
            
            public final void fill() throws IOException {
                try {   
                    buffer.reset();
                    
                    if (tupleCount != 0) {
                                                      
                        if(updateDocumentCount - tupleCount > 0) {
                            buffer.documents.add(last.document);
                            buffer.documentTupleIdx.add((int) (updateDocumentCount - tupleCount));
                        }                              
                        if(updateLengthCount - tupleCount > 0) {
                            buffer.lengths.add(last.length);
                            buffer.lengthTupleIdx.add((int) (updateLengthCount - tupleCount));
                        }
                        bufferStartCount = tupleCount;
                    }
                    
                    while (!buffer.isFull()) {
                        updateLength();
                        buffer.processTuple(input.readString(), input.readInt());
                        tupleCount++;
                    }
                } catch(EOFException e) {}
            }

            public final void updateDocument() throws IOException {
                if (updateDocumentCount > tupleCount)
                    return;
                     
                last.document = input.readString();
                updateDocumentCount = tupleCount + input.readInt();
                                      
                buffer.processDocument(last.document);
            }
            public final void updateLength() throws IOException {
                if (updateLengthCount > tupleCount)
                    return;
                     
                updateDocument();
                last.length = input.readInt();
                updateLengthCount = tupleCount + input.readInt();
                                      
                buffer.processLength(last.length);
            }

            public void run() throws IOException {
                while (true) {
                    fill();
                    
                    if (buffer.isAtEnd())
                        break;
                    
                    buffer.copyUntil(null, processor);
                }      
                processor.close();
            }
            
            public void setProcessor(Step processor) throws IncompatibleProcessorException {  
                if (processor instanceof ShreddedProcessor) {
                    this.processor = new DuplicateEliminator((ShreddedProcessor) processor);
                } else if (processor instanceof DocumentLengthWordCount.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((DocumentLengthWordCount.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<DocumentLengthWordCount>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<DocumentLengthWordCount> getOutputClass() {
                return DocumentLengthWordCount.class;
            }                
        }
        
        public static class DuplicateEliminator implements ShreddedProcessor {
            public ShreddedProcessor processor;
            DocumentLengthWordCount last = new DocumentLengthWordCount();
            boolean documentProcess = true;
            boolean lengthProcess = true;
                                           
            public DuplicateEliminator() {}
            public DuplicateEliminator(ShreddedProcessor processor) {
                this.processor = processor;
            }
            
            public void setShreddedProcessor(ShreddedProcessor processor) {
                this.processor = processor;
            }

            public void processDocument(String document) throws IOException {  
                if (documentProcess || Utility.compare(document, last.document) != 0) {
                    last.document = document;
                    processor.processDocument(document);
            resetLength();
                    documentProcess = false;
                }
            }
            public void processLength(int length) throws IOException {  
                if (lengthProcess || Utility.compare(length, last.length) != 0) {
                    last.length = length;
                    processor.processLength(length);
                    lengthProcess = false;
                }
            }  
            
            public void resetDocument() {
                 documentProcess = true;
            resetLength();
            }                                                
            public void resetLength() {
                 lengthProcess = true;
            }                                                
                               
            public void processTuple(String word, int count) throws IOException {
                processor.processTuple(word, count);
            } 
            
            public void close() throws IOException {
                processor.close();
            }                    
        }
        public static class TupleUnshredder implements ShreddedProcessor {
            DocumentLengthWordCount last = new DocumentLengthWordCount();
            public org.lemurproject.galago.tupleflow.Processor<DocumentLengthWordCount> processor;                               
            
            public TupleUnshredder(DocumentLengthWordCount.Processor processor) {
                this.processor = processor;
            }         
            
            public TupleUnshredder(org.lemurproject.galago.tupleflow.Processor<DocumentLengthWordCount> processor) {
                this.processor = processor;
            }
            
            public DocumentLengthWordCount clone(DocumentLengthWordCount object) {
                DocumentLengthWordCount result = new DocumentLengthWordCount();
                if (object == null) return result;
                result.document = object.document; 
                result.length = object.length; 
                result.word = object.word; 
                result.count = object.count; 
                return result;
            }                 
            
            public void processDocument(String document) throws IOException {
                last.document = document;
            }   
                
            public void processLength(int length) throws IOException {
                last.length = length;
            }   
                
            
            public void processTuple(String word, int count) throws IOException {
                last.word = word;
                last.count = count;
                processor.process(clone(last));
            }               
            
            public void close() throws IOException {
                processor.close();
            }
        }     
        public static class TupleShredder implements Processor {
            DocumentLengthWordCount last = null;
            public ShreddedProcessor processor;
            
            public TupleShredder(ShreddedProcessor processor) {
                this.processor = processor;
            }                              
            
            public DocumentLengthWordCount clone(DocumentLengthWordCount object) {
                DocumentLengthWordCount result = new DocumentLengthWordCount();
                if (object == null) return result;
                result.document = object.document; 
                result.length = object.length; 
                result.word = object.word; 
                result.count = object.count; 
                return result;
            }                 
            
            public void process(DocumentLengthWordCount object) throws IOException {                                                                                                                                                   
                boolean processAll = false;
                if(last == null || Utility.compare(last.document, object.document) != 0 || processAll) { processor.processDocument(object.document); processAll = true; }
                if(last == null || Utility.compare(last.length, object.length) != 0 || processAll) { processor.processLength(object.length); processAll = true; }
                processor.processTuple(object.word, object.count);                                         
                last = object;
            }
                          
            public Class<DocumentLengthWordCount> getInputClass() {
                return DocumentLengthWordCount.class;
            }
            
            public void close() throws IOException {
                processor.close();
            }                     
        }
    } 
    public static class DocumentWordOrder implements Order<DocumentLengthWordCount> {
        public int hash(DocumentLengthWordCount object) {
            int h = 0;
            h += Utility.hash(object.document);
            h += Utility.hash(object.word);
            return h;
        } 
        public Comparator<DocumentLengthWordCount> greaterThan() {
            return new Comparator<DocumentLengthWordCount>() {
                public int compare(DocumentLengthWordCount one, DocumentLengthWordCount two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.document, two.document);
                        if(result != 0) break;
                        result = + Utility.compare(one.word, two.word);
                        if(result != 0) break;
                    } while (false);
                    return -result;
                }
            };
        }     
        public Comparator<DocumentLengthWordCount> lessThan() {
            return new Comparator<DocumentLengthWordCount>() {
                public int compare(DocumentLengthWordCount one, DocumentLengthWordCount two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.document, two.document);
                        if(result != 0) break;
                        result = + Utility.compare(one.word, two.word);
                        if(result != 0) break;
                    } while (false);
                    return result;
                }
            };
        }     
        public TypeReader<DocumentLengthWordCount> orderedReader(ArrayInput _input) {
            return new ShreddedReader(_input);
        }    

        public TypeReader<DocumentLengthWordCount> orderedReader(ArrayInput _input, int bufferSize) {
            return new ShreddedReader(_input, bufferSize);
        }    
        public OrderedWriter<DocumentLengthWordCount> orderedWriter(ArrayOutput _output) {
            ShreddedWriter w = new ShreddedWriter(_output);
            return new OrderedWriterClass(w); 
        }                                    
        public static class OrderedWriterClass extends OrderedWriter< DocumentLengthWordCount > {
            DocumentLengthWordCount last = null;
            ShreddedWriter shreddedWriter = null; 
            
            public OrderedWriterClass(ShreddedWriter s) {
                this.shreddedWriter = s;
            }
            
            public void process(DocumentLengthWordCount object) throws IOException {
               boolean processAll = false;
               if (processAll || last == null || 0 != Utility.compare(object.document, last.document)) { processAll = true; shreddedWriter.processDocument(object.document); }
               if (processAll || last == null || 0 != Utility.compare(object.word, last.word)) { processAll = true; shreddedWriter.processWord(object.word); }
               shreddedWriter.processTuple(object.length, object.count);
               last = object;
            }           
                 
            public void close() throws IOException {
                shreddedWriter.close();
            }
            
            public Class<DocumentLengthWordCount> getInputClass() {
                return DocumentLengthWordCount.class;
            }
        } 
        public ReaderSource<DocumentLengthWordCount> orderedCombiner(Collection<TypeReader<DocumentLengthWordCount>> readers, boolean closeOnExit) {
            ArrayList<ShreddedReader> shreddedReaders = new ArrayList();
            
            for (TypeReader<DocumentLengthWordCount> reader : readers) {
                shreddedReaders.add((ShreddedReader)reader);
            }
            
            return new ShreddedCombiner(shreddedReaders, closeOnExit);
        }                  
        public DocumentLengthWordCount clone(DocumentLengthWordCount object) {
            DocumentLengthWordCount result = new DocumentLengthWordCount();
            if (object == null) return result;
            result.document = object.document; 
            result.length = object.length; 
            result.word = object.word; 
            result.count = object.count; 
            return result;
        }                 
        public Class<DocumentLengthWordCount> getOrderedClass() {
            return DocumentLengthWordCount.class;
        }                           
        public String[] getOrderSpec() {
            return new String[] {"+document", "+word"};
        }

        public static String[] getSpec() {
            return new String[] {"+document", "+word"};
        }
        public static String getSpecString() {
            return "+document +word";
        }
                           
        public interface ShreddedProcessor extends Step {
            public void processDocument(String document) throws IOException;
            public void processWord(String word) throws IOException;
            public void processTuple(int length, int count) throws IOException;
            public void close() throws IOException;
        }    
        public interface ShreddedSource extends Step {
        }                                              
        
        public static class ShreddedWriter implements ShreddedProcessor {
            ArrayOutput output;
            ShreddedBuffer buffer = new ShreddedBuffer();
            String lastDocument;
            String lastWord;
            boolean lastFlush = false;
            
            public ShreddedWriter(ArrayOutput output) {
                this.output = output;
            }                        
            
            public void close() throws IOException {
                flush();
            }
            
            public void processDocument(String document) {
                lastDocument = document;
                buffer.processDocument(document);
            }
            public void processWord(String word) {
                lastWord = word;
                buffer.processWord(word);
            }
            public final void processTuple(int length, int count) throws IOException {
                if (lastFlush) {
                    if(buffer.documents.size() == 0) buffer.processDocument(lastDocument);
                    if(buffer.words.size() == 0) buffer.processWord(lastWord);
                    lastFlush = false;
                }
                buffer.processTuple(length, count);
                if (buffer.isFull())
                    flush();
            }
            public final void flushTuples(int pauseIndex) throws IOException {
                
                while (buffer.getReadIndex() < pauseIndex) {
                           
                    output.writeInt(buffer.getLength());
                    output.writeInt(buffer.getCount());
                    buffer.incrementTuple();
                }
            }  
            public final void flushDocument(int pauseIndex) throws IOException {
                while (buffer.getReadIndex() < pauseIndex) {
                    int nextPause = buffer.getDocumentEndIndex();
                    int count = nextPause - buffer.getReadIndex();
                    
                    output.writeString(buffer.getDocument());
                    output.writeInt(count);
                    buffer.incrementDocument();
                      
                    flushWord(nextPause);
                    assert nextPause == buffer.getReadIndex();
                }
            }
            public final void flushWord(int pauseIndex) throws IOException {
                while (buffer.getReadIndex() < pauseIndex) {
                    int nextPause = buffer.getWordEndIndex();
                    int count = nextPause - buffer.getReadIndex();
                    
                    output.writeString(buffer.getWord());
                    output.writeInt(count);
                    buffer.incrementWord();
                      
                    flushTuples(nextPause);
                    assert nextPause == buffer.getReadIndex();
                }
            }
            public void flush() throws IOException { 
                flushDocument(buffer.getWriteIndex());
                buffer.reset(); 
                lastFlush = true;
            }                           
        }
        public static class ShreddedBuffer {
            ArrayList<String> documents = new ArrayList();
            ArrayList<String> words = new ArrayList();
            TIntArrayList documentTupleIdx = new TIntArrayList();
            TIntArrayList wordTupleIdx = new TIntArrayList();
            int documentReadIdx = 0;
            int wordReadIdx = 0;
                            
            int[] lengths;
            int[] counts;
            int writeTupleIndex = 0;
            int readTupleIndex = 0;
            int batchSize;

            public ShreddedBuffer(int batchSize) {
                this.batchSize = batchSize;

                lengths = new int[batchSize];
                counts = new int[batchSize];
            }                              

            public ShreddedBuffer() {    
                this(10000);
            }                                                                                                                    
            
            public void processDocument(String document) {
                documents.add(document);
                documentTupleIdx.add(writeTupleIndex);
            }                                      
            public void processWord(String word) {
                words.add(word);
                wordTupleIdx.add(writeTupleIndex);
            }                                      
            public void processTuple(int length, int count) {
                assert documents.size() > 0;
                assert words.size() > 0;
                lengths[writeTupleIndex] = length;
                counts[writeTupleIndex] = count;
                writeTupleIndex++;
            }
            public void resetData() {
                documents.clear();
                words.clear();
                documentTupleIdx.clear();
                wordTupleIdx.clear();
                writeTupleIndex = 0;
            }                  
                                 
            public void resetRead() {
                readTupleIndex = 0;
                documentReadIdx = 0;
                wordReadIdx = 0;
            } 

            public void reset() {
                resetData();
                resetRead();
            } 
            public boolean isFull() {
                return writeTupleIndex >= batchSize;
            }

            public boolean isEmpty() {
                return writeTupleIndex == 0;
            }                          

            public boolean isAtEnd() {
                return readTupleIndex >= writeTupleIndex;
            }           
            public void incrementDocument() {
                documentReadIdx++;  
            }                                                                                              

            public void autoIncrementDocument() {
                while (readTupleIndex >= getDocumentEndIndex() && readTupleIndex < writeTupleIndex)
                    documentReadIdx++;
            }                 
            public void incrementWord() {
                wordReadIdx++;  
            }                                                                                              

            public void autoIncrementWord() {
                while (readTupleIndex >= getWordEndIndex() && readTupleIndex < writeTupleIndex)
                    wordReadIdx++;
            }                 
            public void incrementTuple() {
                readTupleIndex++;
            }                    
            public int getDocumentEndIndex() {
                if ((documentReadIdx+1) >= documentTupleIdx.size())
                    return writeTupleIndex;
                return documentTupleIdx.get(documentReadIdx+1);
            }

            public int getWordEndIndex() {
                if ((wordReadIdx+1) >= wordTupleIdx.size())
                    return writeTupleIndex;
                return wordTupleIdx.get(wordReadIdx+1);
            }
            public int getReadIndex() {
                return readTupleIndex;
            }   

            public int getWriteIndex() {
                return writeTupleIndex;
            } 
            public String getDocument() {
                assert readTupleIndex < writeTupleIndex;
                assert documentReadIdx < documents.size();
                
                return documents.get(documentReadIdx);
            }
            public String getWord() {
                assert readTupleIndex < writeTupleIndex;
                assert wordReadIdx < words.size();
                
                return words.get(wordReadIdx);
            }
            public int getLength() {
                assert readTupleIndex < writeTupleIndex;
                return lengths[readTupleIndex];
            }                                         
            public int getCount() {
                assert readTupleIndex < writeTupleIndex;
                return counts[readTupleIndex];
            }                                         
            public void copyTuples(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                   output.processTuple(getLength(), getCount());
                   incrementTuple();
                }
            }                                                                           
            public void copyUntilIndexDocument(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                    output.processDocument(getDocument());
                    assert getDocumentEndIndex() <= endIndex;
                    copyUntilIndexWord(getDocumentEndIndex(), output);
                    incrementDocument();
                }
            } 
            public void copyUntilIndexWord(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                    output.processWord(getWord());
                    assert getWordEndIndex() <= endIndex;
                    copyTuples(getWordEndIndex(), output);
                    incrementWord();
                }
            }  
            public void copyUntilDocument(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                while (!isAtEnd()) {
                    if (other != null) {   
                        assert !other.isAtEnd();
                        int c = + Utility.compare(getDocument(), other.getDocument());
                    
                        if (c > 0) {
                            break;   
                        }
                        
                        output.processDocument(getDocument());
                                      
                        if (c < 0) {
                            copyUntilIndexWord(getDocumentEndIndex(), output);
                        } else if (c == 0) {
                            copyUntilWord(other, output);
                            autoIncrementDocument();
                            break;
                        }
                    } else {
                        output.processDocument(getDocument());
                        copyUntilIndexWord(getDocumentEndIndex(), output);
                    }
                    incrementDocument();  
                    
               
                }
            }
            public void copyUntilWord(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                while (!isAtEnd()) {
                    if (other != null) {   
                        assert !other.isAtEnd();
                        int c = + Utility.compare(getWord(), other.getWord());
                    
                        if (c > 0) {
                            break;   
                        }
                        
                        output.processWord(getWord());
                                      
                        copyTuples(getWordEndIndex(), output);
                    } else {
                        output.processWord(getWord());
                        copyTuples(getWordEndIndex(), output);
                    }
                    incrementWord();  
                    
                    if (getDocumentEndIndex() <= readTupleIndex)
                        break;   
                }
            }
            public void copyUntil(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                copyUntilDocument(other, output);
            }
            
        }                         
        public static class ShreddedCombiner implements ReaderSource<DocumentLengthWordCount>, ShreddedSource {   
            public ShreddedProcessor processor;
            Collection<ShreddedReader> readers;       
            boolean closeOnExit = false;
            boolean uninitialized = true;
            PriorityQueue<ShreddedReader> queue = new PriorityQueue<ShreddedReader>();
            
            public ShreddedCombiner(Collection<ShreddedReader> readers, boolean closeOnExit) {
                this.readers = readers;                                                       
                this.closeOnExit = closeOnExit;
            }
                                  
            public void setProcessor(Step processor) throws IncompatibleProcessorException {  
                if (processor instanceof ShreddedProcessor) {
                    this.processor = new DuplicateEliminator((ShreddedProcessor) processor);
                } else if (processor instanceof DocumentLengthWordCount.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((DocumentLengthWordCount.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<DocumentLengthWordCount>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<DocumentLengthWordCount> getOutputClass() {
                return DocumentLengthWordCount.class;
            }
            
            public void initialize() throws IOException {
                for (ShreddedReader reader : readers) {
                    reader.fill();                                        
                    
                    if (!reader.getBuffer().isAtEnd())
                        queue.add(reader);
                }   

                uninitialized = false;
            }

            public void run() throws IOException {
                initialize();
               
                while (queue.size() > 0) {
                    ShreddedReader top = queue.poll();
                    ShreddedReader next = null;
                    ShreddedBuffer nextBuffer = null; 
                    
                    assert !top.getBuffer().isAtEnd();
                                                  
                    if (queue.size() > 0) {
                        next = queue.peek();
                        nextBuffer = next.getBuffer();
                        assert !nextBuffer.isAtEnd();
                    }
                    
                    top.getBuffer().copyUntil(nextBuffer, processor);
                    if (top.getBuffer().isAtEnd())
                        top.fill();                 
                        
                    if (!top.getBuffer().isAtEnd())
                        queue.add(top);
                }              
                
                if (closeOnExit)
                    processor.close();
            }

            public DocumentLengthWordCount read() throws IOException {
                if (uninitialized)
                    initialize();

                DocumentLengthWordCount result = null;

                while (queue.size() > 0) {
                    ShreddedReader top = queue.poll();
                    result = top.read();

                    if (result != null) {
                        if (top.getBuffer().isAtEnd())
                            top.fill();

                        queue.offer(top);
                        break;
                    } 
                }

                return result;
            }
        } 
        public static class ShreddedReader implements Step, Comparable<ShreddedReader>, TypeReader<DocumentLengthWordCount>, ShreddedSource {      
            public ShreddedProcessor processor;
            ShreddedBuffer buffer;
            DocumentLengthWordCount last = new DocumentLengthWordCount();         
            long updateDocumentCount = -1;
            long updateWordCount = -1;
            long tupleCount = 0;
            long bufferStartCount = 0;  
            ArrayInput input;
            
            public ShreddedReader(ArrayInput input) {
                this.input = input; 
                this.buffer = new ShreddedBuffer();
            }                               
            
            public ShreddedReader(ArrayInput input, int bufferSize) { 
                this.input = input;
                this.buffer = new ShreddedBuffer(bufferSize);
            }
                 
            public final int compareTo(ShreddedReader other) {
                ShreddedBuffer otherBuffer = other.getBuffer();
                
                if (buffer.isAtEnd() && otherBuffer.isAtEnd()) {
                    return 0;                 
                } else if (buffer.isAtEnd()) {
                    return -1;
                } else if (otherBuffer.isAtEnd()) {
                    return 1;
                }
                                   
                int result = 0;
                do {
                    result = + Utility.compare(buffer.getDocument(), otherBuffer.getDocument());
                    if(result != 0) break;
                    result = + Utility.compare(buffer.getWord(), otherBuffer.getWord());
                    if(result != 0) break;
                } while (false);                                             
                
                return result;
            }
            
            public final ShreddedBuffer getBuffer() {
                return buffer;
            }                
            
            public final DocumentLengthWordCount read() throws IOException {
                if (buffer.isAtEnd()) {
                    fill();             
                
                    if (buffer.isAtEnd()) {
                        return null;
                    }
                }
                      
                assert !buffer.isAtEnd();
                DocumentLengthWordCount result = new DocumentLengthWordCount();
                
                result.document = buffer.getDocument();
                result.word = buffer.getWord();
                result.length = buffer.getLength();
                result.count = buffer.getCount();
                
                buffer.incrementTuple();
                buffer.autoIncrementDocument();
                buffer.autoIncrementWord();
                
                return result;
            }           
            
            public final void fill() throws IOException {
                try {   
                    buffer.reset();
                    
                    if (tupleCount != 0) {
                                                      
                        if(updateDocumentCount - tupleCount > 0) {
                            buffer.documents.add(last.document);
                            buffer.documentTupleIdx.add((int) (updateDocumentCount - tupleCount));
                        }                              
                        if(updateWordCount - tupleCount > 0) {
                            buffer.words.add(last.word);
                            buffer.wordTupleIdx.add((int) (updateWordCount - tupleCount));
                        }
                        bufferStartCount = tupleCount;
                    }
                    
                    while (!buffer.isFull()) {
                        updateWord();
                        buffer.processTuple(input.readInt(), input.readInt());
                        tupleCount++;
                    }
                } catch(EOFException e) {}
            }

            public final void updateDocument() throws IOException {
                if (updateDocumentCount > tupleCount)
                    return;
                     
                last.document = input.readString();
                updateDocumentCount = tupleCount + input.readInt();
                                      
                buffer.processDocument(last.document);
            }
            public final void updateWord() throws IOException {
                if (updateWordCount > tupleCount)
                    return;
                     
                updateDocument();
                last.word = input.readString();
                updateWordCount = tupleCount + input.readInt();
                                      
                buffer.processWord(last.word);
            }

            public void run() throws IOException {
                while (true) {
                    fill();
                    
                    if (buffer.isAtEnd())
                        break;
                    
                    buffer.copyUntil(null, processor);
                }      
                processor.close();
            }
            
            public void setProcessor(Step processor) throws IncompatibleProcessorException {  
                if (processor instanceof ShreddedProcessor) {
                    this.processor = new DuplicateEliminator((ShreddedProcessor) processor);
                } else if (processor instanceof DocumentLengthWordCount.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((DocumentLengthWordCount.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<DocumentLengthWordCount>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<DocumentLengthWordCount> getOutputClass() {
                return DocumentLengthWordCount.class;
            }                
        }
        
        public static class DuplicateEliminator implements ShreddedProcessor {
            public ShreddedProcessor processor;
            DocumentLengthWordCount last = new DocumentLengthWordCount();
            boolean documentProcess = true;
            boolean wordProcess = true;
                                           
            public DuplicateEliminator() {}
            public DuplicateEliminator(ShreddedProcessor processor) {
                this.processor = processor;
            }
            
            public void setShreddedProcessor(ShreddedProcessor processor) {
                this.processor = processor;
            }

            public void processDocument(String document) throws IOException {  
                if (documentProcess || Utility.compare(document, last.document) != 0) {
                    last.document = document;
                    processor.processDocument(document);
            resetWord();
                    documentProcess = false;
                }
            }
            public void processWord(String word) throws IOException {  
                if (wordProcess || Utility.compare(word, last.word) != 0) {
                    last.word = word;
                    processor.processWord(word);
                    wordProcess = false;
                }
            }  
            
            public void resetDocument() {
                 documentProcess = true;
            resetWord();
            }                                                
            public void resetWord() {
                 wordProcess = true;
            }                                                
                               
            public void processTuple(int length, int count) throws IOException {
                processor.processTuple(length, count);
            } 
            
            public void close() throws IOException {
                processor.close();
            }                    
        }
        public static class TupleUnshredder implements ShreddedProcessor {
            DocumentLengthWordCount last = new DocumentLengthWordCount();
            public org.lemurproject.galago.tupleflow.Processor<DocumentLengthWordCount> processor;                               
            
            public TupleUnshredder(DocumentLengthWordCount.Processor processor) {
                this.processor = processor;
            }         
            
            public TupleUnshredder(org.lemurproject.galago.tupleflow.Processor<DocumentLengthWordCount> processor) {
                this.processor = processor;
            }
            
            public DocumentLengthWordCount clone(DocumentLengthWordCount object) {
                DocumentLengthWordCount result = new DocumentLengthWordCount();
                if (object == null) return result;
                result.document = object.document; 
                result.length = object.length; 
                result.word = object.word; 
                result.count = object.count; 
                return result;
            }                 
            
            public void processDocument(String document) throws IOException {
                last.document = document;
            }   
                
            public void processWord(String word) throws IOException {
                last.word = word;
            }   
                
            
            public void processTuple(int length, int count) throws IOException {
                last.length = length;
                last.count = count;
                processor.process(clone(last));
            }               
            
            public void close() throws IOException {
                processor.close();
            }
        }     
        public static class TupleShredder implements Processor {
            DocumentLengthWordCount last = null;
            public ShreddedProcessor processor;
            
            public TupleShredder(ShreddedProcessor processor) {
                this.processor = processor;
            }                              
            
            public DocumentLengthWordCount clone(DocumentLengthWordCount object) {
                DocumentLengthWordCount result = new DocumentLengthWordCount();
                if (object == null) return result;
                result.document = object.document; 
                result.length = object.length; 
                result.word = object.word; 
                result.count = object.count; 
                return result;
            }                 
            
            public void process(DocumentLengthWordCount object) throws IOException {                                                                                                                                                   
                boolean processAll = false;
                if(last == null || Utility.compare(last.document, object.document) != 0 || processAll) { processor.processDocument(object.document); processAll = true; }
                if(last == null || Utility.compare(last.word, object.word) != 0 || processAll) { processor.processWord(object.word); processAll = true; }
                processor.processTuple(object.length, object.count);                                         
                last = object;
            }
                          
            public Class<DocumentLengthWordCount> getInputClass() {
                return DocumentLengthWordCount.class;
            }
            
            public void close() throws IOException {
                processor.close();
            }                     
        }
    } 
    public static class WordDocumentOrder implements Order<DocumentLengthWordCount> {
        public int hash(DocumentLengthWordCount object) {
            int h = 0;
            h += Utility.hash(object.word);
            h += Utility.hash(object.document);
            return h;
        } 
        public Comparator<DocumentLengthWordCount> greaterThan() {
            return new Comparator<DocumentLengthWordCount>() {
                public int compare(DocumentLengthWordCount one, DocumentLengthWordCount two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.word, two.word);
                        if(result != 0) break;
                        result = + Utility.compare(one.document, two.document);
                        if(result != 0) break;
                    } while (false);
                    return -result;
                }
            };
        }     
        public Comparator<DocumentLengthWordCount> lessThan() {
            return new Comparator<DocumentLengthWordCount>() {
                public int compare(DocumentLengthWordCount one, DocumentLengthWordCount two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.word, two.word);
                        if(result != 0) break;
                        result = + Utility.compare(one.document, two.document);
                        if(result != 0) break;
                    } while (false);
                    return result;
                }
            };
        }     
        public TypeReader<DocumentLengthWordCount> orderedReader(ArrayInput _input) {
            return new ShreddedReader(_input);
        }    

        public TypeReader<DocumentLengthWordCount> orderedReader(ArrayInput _input, int bufferSize) {
            return new ShreddedReader(_input, bufferSize);
        }    
        public OrderedWriter<DocumentLengthWordCount> orderedWriter(ArrayOutput _output) {
            ShreddedWriter w = new ShreddedWriter(_output);
            return new OrderedWriterClass(w); 
        }                                    
        public static class OrderedWriterClass extends OrderedWriter< DocumentLengthWordCount > {
            DocumentLengthWordCount last = null;
            ShreddedWriter shreddedWriter = null; 
            
            public OrderedWriterClass(ShreddedWriter s) {
                this.shreddedWriter = s;
            }
            
            public void process(DocumentLengthWordCount object) throws IOException {
               boolean processAll = false;
               if (processAll || last == null || 0 != Utility.compare(object.word, last.word)) { processAll = true; shreddedWriter.processWord(object.word); }
               if (processAll || last == null || 0 != Utility.compare(object.document, last.document)) { processAll = true; shreddedWriter.processDocument(object.document); }
               shreddedWriter.processTuple(object.length, object.count);
               last = object;
            }           
                 
            public void close() throws IOException {
                shreddedWriter.close();
            }
            
            public Class<DocumentLengthWordCount> getInputClass() {
                return DocumentLengthWordCount.class;
            }
        } 
        public ReaderSource<DocumentLengthWordCount> orderedCombiner(Collection<TypeReader<DocumentLengthWordCount>> readers, boolean closeOnExit) {
            ArrayList<ShreddedReader> shreddedReaders = new ArrayList();
            
            for (TypeReader<DocumentLengthWordCount> reader : readers) {
                shreddedReaders.add((ShreddedReader)reader);
            }
            
            return new ShreddedCombiner(shreddedReaders, closeOnExit);
        }                  
        public DocumentLengthWordCount clone(DocumentLengthWordCount object) {
            DocumentLengthWordCount result = new DocumentLengthWordCount();
            if (object == null) return result;
            result.document = object.document; 
            result.length = object.length; 
            result.word = object.word; 
            result.count = object.count; 
            return result;
        }                 
        public Class<DocumentLengthWordCount> getOrderedClass() {
            return DocumentLengthWordCount.class;
        }                           
        public String[] getOrderSpec() {
            return new String[] {"+word", "+document"};
        }

        public static String[] getSpec() {
            return new String[] {"+word", "+document"};
        }
        public static String getSpecString() {
            return "+word +document";
        }
                           
        public interface ShreddedProcessor extends Step {
            public void processWord(String word) throws IOException;
            public void processDocument(String document) throws IOException;
            public void processTuple(int length, int count) throws IOException;
            public void close() throws IOException;
        }    
        public interface ShreddedSource extends Step {
        }                                              
        
        public static class ShreddedWriter implements ShreddedProcessor {
            ArrayOutput output;
            ShreddedBuffer buffer = new ShreddedBuffer();
            String lastWord;
            String lastDocument;
            boolean lastFlush = false;
            
            public ShreddedWriter(ArrayOutput output) {
                this.output = output;
            }                        
            
            public void close() throws IOException {
                flush();
            }
            
            public void processWord(String word) {
                lastWord = word;
                buffer.processWord(word);
            }
            public void processDocument(String document) {
                lastDocument = document;
                buffer.processDocument(document);
            }
            public final void processTuple(int length, int count) throws IOException {
                if (lastFlush) {
                    if(buffer.words.size() == 0) buffer.processWord(lastWord);
                    if(buffer.documents.size() == 0) buffer.processDocument(lastDocument);
                    lastFlush = false;
                }
                buffer.processTuple(length, count);
                if (buffer.isFull())
                    flush();
            }
            public final void flushTuples(int pauseIndex) throws IOException {
                
                while (buffer.getReadIndex() < pauseIndex) {
                           
                    output.writeInt(buffer.getLength());
                    output.writeInt(buffer.getCount());
                    buffer.incrementTuple();
                }
            }  
            public final void flushWord(int pauseIndex) throws IOException {
                while (buffer.getReadIndex() < pauseIndex) {
                    int nextPause = buffer.getWordEndIndex();
                    int count = nextPause - buffer.getReadIndex();
                    
                    output.writeString(buffer.getWord());
                    output.writeInt(count);
                    buffer.incrementWord();
                      
                    flushDocument(nextPause);
                    assert nextPause == buffer.getReadIndex();
                }
            }
            public final void flushDocument(int pauseIndex) throws IOException {
                while (buffer.getReadIndex() < pauseIndex) {
                    int nextPause = buffer.getDocumentEndIndex();
                    int count = nextPause - buffer.getReadIndex();
                    
                    output.writeString(buffer.getDocument());
                    output.writeInt(count);
                    buffer.incrementDocument();
                      
                    flushTuples(nextPause);
                    assert nextPause == buffer.getReadIndex();
                }
            }
            public void flush() throws IOException { 
                flushWord(buffer.getWriteIndex());
                buffer.reset(); 
                lastFlush = true;
            }                           
        }
        public static class ShreddedBuffer {
            ArrayList<String> words = new ArrayList();
            ArrayList<String> documents = new ArrayList();
            TIntArrayList wordTupleIdx = new TIntArrayList();
            TIntArrayList documentTupleIdx = new TIntArrayList();
            int wordReadIdx = 0;
            int documentReadIdx = 0;
                            
            int[] lengths;
            int[] counts;
            int writeTupleIndex = 0;
            int readTupleIndex = 0;
            int batchSize;

            public ShreddedBuffer(int batchSize) {
                this.batchSize = batchSize;

                lengths = new int[batchSize];
                counts = new int[batchSize];
            }                              

            public ShreddedBuffer() {    
                this(10000);
            }                                                                                                                    
            
            public void processWord(String word) {
                words.add(word);
                wordTupleIdx.add(writeTupleIndex);
            }                                      
            public void processDocument(String document) {
                documents.add(document);
                documentTupleIdx.add(writeTupleIndex);
            }                                      
            public void processTuple(int length, int count) {
                assert words.size() > 0;
                assert documents.size() > 0;
                lengths[writeTupleIndex] = length;
                counts[writeTupleIndex] = count;
                writeTupleIndex++;
            }
            public void resetData() {
                words.clear();
                documents.clear();
                wordTupleIdx.clear();
                documentTupleIdx.clear();
                writeTupleIndex = 0;
            }                  
                                 
            public void resetRead() {
                readTupleIndex = 0;
                wordReadIdx = 0;
                documentReadIdx = 0;
            } 

            public void reset() {
                resetData();
                resetRead();
            } 
            public boolean isFull() {
                return writeTupleIndex >= batchSize;
            }

            public boolean isEmpty() {
                return writeTupleIndex == 0;
            }                          

            public boolean isAtEnd() {
                return readTupleIndex >= writeTupleIndex;
            }           
            public void incrementWord() {
                wordReadIdx++;  
            }                                                                                              

            public void autoIncrementWord() {
                while (readTupleIndex >= getWordEndIndex() && readTupleIndex < writeTupleIndex)
                    wordReadIdx++;
            }                 
            public void incrementDocument() {
                documentReadIdx++;  
            }                                                                                              

            public void autoIncrementDocument() {
                while (readTupleIndex >= getDocumentEndIndex() && readTupleIndex < writeTupleIndex)
                    documentReadIdx++;
            }                 
            public void incrementTuple() {
                readTupleIndex++;
            }                    
            public int getWordEndIndex() {
                if ((wordReadIdx+1) >= wordTupleIdx.size())
                    return writeTupleIndex;
                return wordTupleIdx.get(wordReadIdx+1);
            }

            public int getDocumentEndIndex() {
                if ((documentReadIdx+1) >= documentTupleIdx.size())
                    return writeTupleIndex;
                return documentTupleIdx.get(documentReadIdx+1);
            }
            public int getReadIndex() {
                return readTupleIndex;
            }   

            public int getWriteIndex() {
                return writeTupleIndex;
            } 
            public String getWord() {
                assert readTupleIndex < writeTupleIndex;
                assert wordReadIdx < words.size();
                
                return words.get(wordReadIdx);
            }
            public String getDocument() {
                assert readTupleIndex < writeTupleIndex;
                assert documentReadIdx < documents.size();
                
                return documents.get(documentReadIdx);
            }
            public int getLength() {
                assert readTupleIndex < writeTupleIndex;
                return lengths[readTupleIndex];
            }                                         
            public int getCount() {
                assert readTupleIndex < writeTupleIndex;
                return counts[readTupleIndex];
            }                                         
            public void copyTuples(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                   output.processTuple(getLength(), getCount());
                   incrementTuple();
                }
            }                                                                           
            public void copyUntilIndexWord(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                    output.processWord(getWord());
                    assert getWordEndIndex() <= endIndex;
                    copyUntilIndexDocument(getWordEndIndex(), output);
                    incrementWord();
                }
            } 
            public void copyUntilIndexDocument(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                    output.processDocument(getDocument());
                    assert getDocumentEndIndex() <= endIndex;
                    copyTuples(getDocumentEndIndex(), output);
                    incrementDocument();
                }
            }  
            public void copyUntilWord(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                while (!isAtEnd()) {
                    if (other != null) {   
                        assert !other.isAtEnd();
                        int c = + Utility.compare(getWord(), other.getWord());
                    
                        if (c > 0) {
                            break;   
                        }
                        
                        output.processWord(getWord());
                                      
                        if (c < 0) {
                            copyUntilIndexDocument(getWordEndIndex(), output);
                        } else if (c == 0) {
                            copyUntilDocument(other, output);
                            autoIncrementWord();
                            break;
                        }
                    } else {
                        output.processWord(getWord());
                        copyUntilIndexDocument(getWordEndIndex(), output);
                    }
                    incrementWord();  
                    
               
                }
            }
            public void copyUntilDocument(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                while (!isAtEnd()) {
                    if (other != null) {   
                        assert !other.isAtEnd();
                        int c = + Utility.compare(getDocument(), other.getDocument());
                    
                        if (c > 0) {
                            break;   
                        }
                        
                        output.processDocument(getDocument());
                                      
                        copyTuples(getDocumentEndIndex(), output);
                    } else {
                        output.processDocument(getDocument());
                        copyTuples(getDocumentEndIndex(), output);
                    }
                    incrementDocument();  
                    
                    if (getWordEndIndex() <= readTupleIndex)
                        break;   
                }
            }
            public void copyUntil(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                copyUntilWord(other, output);
            }
            
        }                         
        public static class ShreddedCombiner implements ReaderSource<DocumentLengthWordCount>, ShreddedSource {   
            public ShreddedProcessor processor;
            Collection<ShreddedReader> readers;       
            boolean closeOnExit = false;
            boolean uninitialized = true;
            PriorityQueue<ShreddedReader> queue = new PriorityQueue<ShreddedReader>();
            
            public ShreddedCombiner(Collection<ShreddedReader> readers, boolean closeOnExit) {
                this.readers = readers;                                                       
                this.closeOnExit = closeOnExit;
            }
                                  
            public void setProcessor(Step processor) throws IncompatibleProcessorException {  
                if (processor instanceof ShreddedProcessor) {
                    this.processor = new DuplicateEliminator((ShreddedProcessor) processor);
                } else if (processor instanceof DocumentLengthWordCount.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((DocumentLengthWordCount.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<DocumentLengthWordCount>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<DocumentLengthWordCount> getOutputClass() {
                return DocumentLengthWordCount.class;
            }
            
            public void initialize() throws IOException {
                for (ShreddedReader reader : readers) {
                    reader.fill();                                        
                    
                    if (!reader.getBuffer().isAtEnd())
                        queue.add(reader);
                }   

                uninitialized = false;
            }

            public void run() throws IOException {
                initialize();
               
                while (queue.size() > 0) {
                    ShreddedReader top = queue.poll();
                    ShreddedReader next = null;
                    ShreddedBuffer nextBuffer = null; 
                    
                    assert !top.getBuffer().isAtEnd();
                                                  
                    if (queue.size() > 0) {
                        next = queue.peek();
                        nextBuffer = next.getBuffer();
                        assert !nextBuffer.isAtEnd();
                    }
                    
                    top.getBuffer().copyUntil(nextBuffer, processor);
                    if (top.getBuffer().isAtEnd())
                        top.fill();                 
                        
                    if (!top.getBuffer().isAtEnd())
                        queue.add(top);
                }              
                
                if (closeOnExit)
                    processor.close();
            }

            public DocumentLengthWordCount read() throws IOException {
                if (uninitialized)
                    initialize();

                DocumentLengthWordCount result = null;

                while (queue.size() > 0) {
                    ShreddedReader top = queue.poll();
                    result = top.read();

                    if (result != null) {
                        if (top.getBuffer().isAtEnd())
                            top.fill();

                        queue.offer(top);
                        break;
                    } 
                }

                return result;
            }
        } 
        public static class ShreddedReader implements Step, Comparable<ShreddedReader>, TypeReader<DocumentLengthWordCount>, ShreddedSource {      
            public ShreddedProcessor processor;
            ShreddedBuffer buffer;
            DocumentLengthWordCount last = new DocumentLengthWordCount();         
            long updateWordCount = -1;
            long updateDocumentCount = -1;
            long tupleCount = 0;
            long bufferStartCount = 0;  
            ArrayInput input;
            
            public ShreddedReader(ArrayInput input) {
                this.input = input; 
                this.buffer = new ShreddedBuffer();
            }                               
            
            public ShreddedReader(ArrayInput input, int bufferSize) { 
                this.input = input;
                this.buffer = new ShreddedBuffer(bufferSize);
            }
                 
            public final int compareTo(ShreddedReader other) {
                ShreddedBuffer otherBuffer = other.getBuffer();
                
                if (buffer.isAtEnd() && otherBuffer.isAtEnd()) {
                    return 0;                 
                } else if (buffer.isAtEnd()) {
                    return -1;
                } else if (otherBuffer.isAtEnd()) {
                    return 1;
                }
                                   
                int result = 0;
                do {
                    result = + Utility.compare(buffer.getWord(), otherBuffer.getWord());
                    if(result != 0) break;
                    result = + Utility.compare(buffer.getDocument(), otherBuffer.getDocument());
                    if(result != 0) break;
                } while (false);                                             
                
                return result;
            }
            
            public final ShreddedBuffer getBuffer() {
                return buffer;
            }                
            
            public final DocumentLengthWordCount read() throws IOException {
                if (buffer.isAtEnd()) {
                    fill();             
                
                    if (buffer.isAtEnd()) {
                        return null;
                    }
                }
                      
                assert !buffer.isAtEnd();
                DocumentLengthWordCount result = new DocumentLengthWordCount();
                
                result.word = buffer.getWord();
                result.document = buffer.getDocument();
                result.length = buffer.getLength();
                result.count = buffer.getCount();
                
                buffer.incrementTuple();
                buffer.autoIncrementWord();
                buffer.autoIncrementDocument();
                
                return result;
            }           
            
            public final void fill() throws IOException {
                try {   
                    buffer.reset();
                    
                    if (tupleCount != 0) {
                                                      
                        if(updateWordCount - tupleCount > 0) {
                            buffer.words.add(last.word);
                            buffer.wordTupleIdx.add((int) (updateWordCount - tupleCount));
                        }                              
                        if(updateDocumentCount - tupleCount > 0) {
                            buffer.documents.add(last.document);
                            buffer.documentTupleIdx.add((int) (updateDocumentCount - tupleCount));
                        }
                        bufferStartCount = tupleCount;
                    }
                    
                    while (!buffer.isFull()) {
                        updateDocument();
                        buffer.processTuple(input.readInt(), input.readInt());
                        tupleCount++;
                    }
                } catch(EOFException e) {}
            }

            public final void updateWord() throws IOException {
                if (updateWordCount > tupleCount)
                    return;
                     
                last.word = input.readString();
                updateWordCount = tupleCount + input.readInt();
                                      
                buffer.processWord(last.word);
            }
            public final void updateDocument() throws IOException {
                if (updateDocumentCount > tupleCount)
                    return;
                     
                updateWord();
                last.document = input.readString();
                updateDocumentCount = tupleCount + input.readInt();
                                      
                buffer.processDocument(last.document);
            }

            public void run() throws IOException {
                while (true) {
                    fill();
                    
                    if (buffer.isAtEnd())
                        break;
                    
                    buffer.copyUntil(null, processor);
                }      
                processor.close();
            }
            
            public void setProcessor(Step processor) throws IncompatibleProcessorException {  
                if (processor instanceof ShreddedProcessor) {
                    this.processor = new DuplicateEliminator((ShreddedProcessor) processor);
                } else if (processor instanceof DocumentLengthWordCount.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((DocumentLengthWordCount.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<DocumentLengthWordCount>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<DocumentLengthWordCount> getOutputClass() {
                return DocumentLengthWordCount.class;
            }                
        }
        
        public static class DuplicateEliminator implements ShreddedProcessor {
            public ShreddedProcessor processor;
            DocumentLengthWordCount last = new DocumentLengthWordCount();
            boolean wordProcess = true;
            boolean documentProcess = true;
                                           
            public DuplicateEliminator() {}
            public DuplicateEliminator(ShreddedProcessor processor) {
                this.processor = processor;
            }
            
            public void setShreddedProcessor(ShreddedProcessor processor) {
                this.processor = processor;
            }

            public void processWord(String word) throws IOException {  
                if (wordProcess || Utility.compare(word, last.word) != 0) {
                    last.word = word;
                    processor.processWord(word);
            resetDocument();
                    wordProcess = false;
                }
            }
            public void processDocument(String document) throws IOException {  
                if (documentProcess || Utility.compare(document, last.document) != 0) {
                    last.document = document;
                    processor.processDocument(document);
                    documentProcess = false;
                }
            }  
            
            public void resetWord() {
                 wordProcess = true;
            resetDocument();
            }                                                
            public void resetDocument() {
                 documentProcess = true;
            }                                                
                               
            public void processTuple(int length, int count) throws IOException {
                processor.processTuple(length, count);
            } 
            
            public void close() throws IOException {
                processor.close();
            }                    
        }
        public static class TupleUnshredder implements ShreddedProcessor {
            DocumentLengthWordCount last = new DocumentLengthWordCount();
            public org.lemurproject.galago.tupleflow.Processor<DocumentLengthWordCount> processor;                               
            
            public TupleUnshredder(DocumentLengthWordCount.Processor processor) {
                this.processor = processor;
            }         
            
            public TupleUnshredder(org.lemurproject.galago.tupleflow.Processor<DocumentLengthWordCount> processor) {
                this.processor = processor;
            }
            
            public DocumentLengthWordCount clone(DocumentLengthWordCount object) {
                DocumentLengthWordCount result = new DocumentLengthWordCount();
                if (object == null) return result;
                result.document = object.document; 
                result.length = object.length; 
                result.word = object.word; 
                result.count = object.count; 
                return result;
            }                 
            
            public void processWord(String word) throws IOException {
                last.word = word;
            }   
                
            public void processDocument(String document) throws IOException {
                last.document = document;
            }   
                
            
            public void processTuple(int length, int count) throws IOException {
                last.length = length;
                last.count = count;
                processor.process(clone(last));
            }               
            
            public void close() throws IOException {
                processor.close();
            }
        }     
        public static class TupleShredder implements Processor {
            DocumentLengthWordCount last = null;
            public ShreddedProcessor processor;
            
            public TupleShredder(ShreddedProcessor processor) {
                this.processor = processor;
            }                              
            
            public DocumentLengthWordCount clone(DocumentLengthWordCount object) {
                DocumentLengthWordCount result = new DocumentLengthWordCount();
                if (object == null) return result;
                result.document = object.document; 
                result.length = object.length; 
                result.word = object.word; 
                result.count = object.count; 
                return result;
            }                 
            
            public void process(DocumentLengthWordCount object) throws IOException {                                                                                                                                                   
                boolean processAll = false;
                if(last == null || Utility.compare(last.word, object.word) != 0 || processAll) { processor.processWord(object.word); processAll = true; }
                if(last == null || Utility.compare(last.document, object.document) != 0 || processAll) { processor.processDocument(object.document); processAll = true; }
                processor.processTuple(object.length, object.count);                                         
                last = object;
            }
                          
            public Class<DocumentLengthWordCount> getInputClass() {
                return DocumentLengthWordCount.class;
            }
            
            public void close() throws IOException {
                processor.close();
            }                     
        }
    } 
    public static class DocumentOrder implements Order<DocumentLengthWordCount> {
        public int hash(DocumentLengthWordCount object) {
            int h = 0;
            h += Utility.hash(object.document);
            return h;
        } 
        public Comparator<DocumentLengthWordCount> greaterThan() {
            return new Comparator<DocumentLengthWordCount>() {
                public int compare(DocumentLengthWordCount one, DocumentLengthWordCount two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.document, two.document);
                        if(result != 0) break;
                    } while (false);
                    return -result;
                }
            };
        }     
        public Comparator<DocumentLengthWordCount> lessThan() {
            return new Comparator<DocumentLengthWordCount>() {
                public int compare(DocumentLengthWordCount one, DocumentLengthWordCount two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.document, two.document);
                        if(result != 0) break;
                    } while (false);
                    return result;
                }
            };
        }     
        public TypeReader<DocumentLengthWordCount> orderedReader(ArrayInput _input) {
            return new ShreddedReader(_input);
        }    

        public TypeReader<DocumentLengthWordCount> orderedReader(ArrayInput _input, int bufferSize) {
            return new ShreddedReader(_input, bufferSize);
        }    
        public OrderedWriter<DocumentLengthWordCount> orderedWriter(ArrayOutput _output) {
            ShreddedWriter w = new ShreddedWriter(_output);
            return new OrderedWriterClass(w); 
        }                                    
        public static class OrderedWriterClass extends OrderedWriter< DocumentLengthWordCount > {
            DocumentLengthWordCount last = null;
            ShreddedWriter shreddedWriter = null; 
            
            public OrderedWriterClass(ShreddedWriter s) {
                this.shreddedWriter = s;
            }
            
            public void process(DocumentLengthWordCount object) throws IOException {
               boolean processAll = false;
               if (processAll || last == null || 0 != Utility.compare(object.document, last.document)) { processAll = true; shreddedWriter.processDocument(object.document); }
               shreddedWriter.processTuple(object.length, object.word, object.count);
               last = object;
            }           
                 
            public void close() throws IOException {
                shreddedWriter.close();
            }
            
            public Class<DocumentLengthWordCount> getInputClass() {
                return DocumentLengthWordCount.class;
            }
        } 
        public ReaderSource<DocumentLengthWordCount> orderedCombiner(Collection<TypeReader<DocumentLengthWordCount>> readers, boolean closeOnExit) {
            ArrayList<ShreddedReader> shreddedReaders = new ArrayList();
            
            for (TypeReader<DocumentLengthWordCount> reader : readers) {
                shreddedReaders.add((ShreddedReader)reader);
            }
            
            return new ShreddedCombiner(shreddedReaders, closeOnExit);
        }                  
        public DocumentLengthWordCount clone(DocumentLengthWordCount object) {
            DocumentLengthWordCount result = new DocumentLengthWordCount();
            if (object == null) return result;
            result.document = object.document; 
            result.length = object.length; 
            result.word = object.word; 
            result.count = object.count; 
            return result;
        }                 
        public Class<DocumentLengthWordCount> getOrderedClass() {
            return DocumentLengthWordCount.class;
        }                           
        public String[] getOrderSpec() {
            return new String[] {"+document"};
        }

        public static String[] getSpec() {
            return new String[] {"+document"};
        }
        public static String getSpecString() {
            return "+document";
        }
                           
        public interface ShreddedProcessor extends Step {
            public void processDocument(String document) throws IOException;
            public void processTuple(int length, String word, int count) throws IOException;
            public void close() throws IOException;
        }    
        public interface ShreddedSource extends Step {
        }                                              
        
        public static class ShreddedWriter implements ShreddedProcessor {
            ArrayOutput output;
            ShreddedBuffer buffer = new ShreddedBuffer();
            String lastDocument;
            boolean lastFlush = false;
            
            public ShreddedWriter(ArrayOutput output) {
                this.output = output;
            }                        
            
            public void close() throws IOException {
                flush();
            }
            
            public void processDocument(String document) {
                lastDocument = document;
                buffer.processDocument(document);
            }
            public final void processTuple(int length, String word, int count) throws IOException {
                if (lastFlush) {
                    if(buffer.documents.size() == 0) buffer.processDocument(lastDocument);
                    lastFlush = false;
                }
                buffer.processTuple(length, word, count);
                if (buffer.isFull())
                    flush();
            }
            public final void flushTuples(int pauseIndex) throws IOException {
                
                while (buffer.getReadIndex() < pauseIndex) {
                           
                    output.writeInt(buffer.getLength());
                    output.writeString(buffer.getWord());
                    output.writeInt(buffer.getCount());
                    buffer.incrementTuple();
                }
            }  
            public final void flushDocument(int pauseIndex) throws IOException {
                while (buffer.getReadIndex() < pauseIndex) {
                    int nextPause = buffer.getDocumentEndIndex();
                    int count = nextPause - buffer.getReadIndex();
                    
                    output.writeString(buffer.getDocument());
                    output.writeInt(count);
                    buffer.incrementDocument();
                      
                    flushTuples(nextPause);
                    assert nextPause == buffer.getReadIndex();
                }
            }
            public void flush() throws IOException { 
                flushDocument(buffer.getWriteIndex());
                buffer.reset(); 
                lastFlush = true;
            }                           
        }
        public static class ShreddedBuffer {
            ArrayList<String> documents = new ArrayList();
            TIntArrayList documentTupleIdx = new TIntArrayList();
            int documentReadIdx = 0;
                            
            int[] lengths;
            String[] words;
            int[] counts;
            int writeTupleIndex = 0;
            int readTupleIndex = 0;
            int batchSize;

            public ShreddedBuffer(int batchSize) {
                this.batchSize = batchSize;

                lengths = new int[batchSize];
                words = new String[batchSize];
                counts = new int[batchSize];
            }                              

            public ShreddedBuffer() {    
                this(10000);
            }                                                                                                                    
            
            public void processDocument(String document) {
                documents.add(document);
                documentTupleIdx.add(writeTupleIndex);
            }                                      
            public void processTuple(int length, String word, int count) {
                assert documents.size() > 0;
                lengths[writeTupleIndex] = length;
                words[writeTupleIndex] = word;
                counts[writeTupleIndex] = count;
                writeTupleIndex++;
            }
            public void resetData() {
                documents.clear();
                documentTupleIdx.clear();
                writeTupleIndex = 0;
            }                  
                                 
            public void resetRead() {
                readTupleIndex = 0;
                documentReadIdx = 0;
            } 

            public void reset() {
                resetData();
                resetRead();
            } 
            public boolean isFull() {
                return writeTupleIndex >= batchSize;
            }

            public boolean isEmpty() {
                return writeTupleIndex == 0;
            }                          

            public boolean isAtEnd() {
                return readTupleIndex >= writeTupleIndex;
            }           
            public void incrementDocument() {
                documentReadIdx++;  
            }                                                                                              

            public void autoIncrementDocument() {
                while (readTupleIndex >= getDocumentEndIndex() && readTupleIndex < writeTupleIndex)
                    documentReadIdx++;
            }                 
            public void incrementTuple() {
                readTupleIndex++;
            }                    
            public int getDocumentEndIndex() {
                if ((documentReadIdx+1) >= documentTupleIdx.size())
                    return writeTupleIndex;
                return documentTupleIdx.get(documentReadIdx+1);
            }
            public int getReadIndex() {
                return readTupleIndex;
            }   

            public int getWriteIndex() {
                return writeTupleIndex;
            } 
            public String getDocument() {
                assert readTupleIndex < writeTupleIndex;
                assert documentReadIdx < documents.size();
                
                return documents.get(documentReadIdx);
            }
            public int getLength() {
                assert readTupleIndex < writeTupleIndex;
                return lengths[readTupleIndex];
            }                                         
            public String getWord() {
                assert readTupleIndex < writeTupleIndex;
                return words[readTupleIndex];
            }                                         
            public int getCount() {
                assert readTupleIndex < writeTupleIndex;
                return counts[readTupleIndex];
            }                                         
            public void copyTuples(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                   output.processTuple(getLength(), getWord(), getCount());
                   incrementTuple();
                }
            }                                                                           
            public void copyUntilIndexDocument(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                    output.processDocument(getDocument());
                    assert getDocumentEndIndex() <= endIndex;
                    copyTuples(getDocumentEndIndex(), output);
                    incrementDocument();
                }
            }  
            public void copyUntilDocument(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                while (!isAtEnd()) {
                    if (other != null) {   
                        assert !other.isAtEnd();
                        int c = + Utility.compare(getDocument(), other.getDocument());
                    
                        if (c > 0) {
                            break;   
                        }
                        
                        output.processDocument(getDocument());
                                      
                        copyTuples(getDocumentEndIndex(), output);
                    } else {
                        output.processDocument(getDocument());
                        copyTuples(getDocumentEndIndex(), output);
                    }
                    incrementDocument();  
                    
               
                }
            }
            public void copyUntil(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                copyUntilDocument(other, output);
            }
            
        }                         
        public static class ShreddedCombiner implements ReaderSource<DocumentLengthWordCount>, ShreddedSource {   
            public ShreddedProcessor processor;
            Collection<ShreddedReader> readers;       
            boolean closeOnExit = false;
            boolean uninitialized = true;
            PriorityQueue<ShreddedReader> queue = new PriorityQueue<ShreddedReader>();
            
            public ShreddedCombiner(Collection<ShreddedReader> readers, boolean closeOnExit) {
                this.readers = readers;                                                       
                this.closeOnExit = closeOnExit;
            }
                                  
            public void setProcessor(Step processor) throws IncompatibleProcessorException {  
                if (processor instanceof ShreddedProcessor) {
                    this.processor = new DuplicateEliminator((ShreddedProcessor) processor);
                } else if (processor instanceof DocumentLengthWordCount.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((DocumentLengthWordCount.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<DocumentLengthWordCount>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<DocumentLengthWordCount> getOutputClass() {
                return DocumentLengthWordCount.class;
            }
            
            public void initialize() throws IOException {
                for (ShreddedReader reader : readers) {
                    reader.fill();                                        
                    
                    if (!reader.getBuffer().isAtEnd())
                        queue.add(reader);
                }   

                uninitialized = false;
            }

            public void run() throws IOException {
                initialize();
               
                while (queue.size() > 0) {
                    ShreddedReader top = queue.poll();
                    ShreddedReader next = null;
                    ShreddedBuffer nextBuffer = null; 
                    
                    assert !top.getBuffer().isAtEnd();
                                                  
                    if (queue.size() > 0) {
                        next = queue.peek();
                        nextBuffer = next.getBuffer();
                        assert !nextBuffer.isAtEnd();
                    }
                    
                    top.getBuffer().copyUntil(nextBuffer, processor);
                    if (top.getBuffer().isAtEnd())
                        top.fill();                 
                        
                    if (!top.getBuffer().isAtEnd())
                        queue.add(top);
                }              
                
                if (closeOnExit)
                    processor.close();
            }

            public DocumentLengthWordCount read() throws IOException {
                if (uninitialized)
                    initialize();

                DocumentLengthWordCount result = null;

                while (queue.size() > 0) {
                    ShreddedReader top = queue.poll();
                    result = top.read();

                    if (result != null) {
                        if (top.getBuffer().isAtEnd())
                            top.fill();

                        queue.offer(top);
                        break;
                    } 
                }

                return result;
            }
        } 
        public static class ShreddedReader implements Step, Comparable<ShreddedReader>, TypeReader<DocumentLengthWordCount>, ShreddedSource {      
            public ShreddedProcessor processor;
            ShreddedBuffer buffer;
            DocumentLengthWordCount last = new DocumentLengthWordCount();         
            long updateDocumentCount = -1;
            long tupleCount = 0;
            long bufferStartCount = 0;  
            ArrayInput input;
            
            public ShreddedReader(ArrayInput input) {
                this.input = input; 
                this.buffer = new ShreddedBuffer();
            }                               
            
            public ShreddedReader(ArrayInput input, int bufferSize) { 
                this.input = input;
                this.buffer = new ShreddedBuffer(bufferSize);
            }
                 
            public final int compareTo(ShreddedReader other) {
                ShreddedBuffer otherBuffer = other.getBuffer();
                
                if (buffer.isAtEnd() && otherBuffer.isAtEnd()) {
                    return 0;                 
                } else if (buffer.isAtEnd()) {
                    return -1;
                } else if (otherBuffer.isAtEnd()) {
                    return 1;
                }
                                   
                int result = 0;
                do {
                    result = + Utility.compare(buffer.getDocument(), otherBuffer.getDocument());
                    if(result != 0) break;
                } while (false);                                             
                
                return result;
            }
            
            public final ShreddedBuffer getBuffer() {
                return buffer;
            }                
            
            public final DocumentLengthWordCount read() throws IOException {
                if (buffer.isAtEnd()) {
                    fill();             
                
                    if (buffer.isAtEnd()) {
                        return null;
                    }
                }
                      
                assert !buffer.isAtEnd();
                DocumentLengthWordCount result = new DocumentLengthWordCount();
                
                result.document = buffer.getDocument();
                result.length = buffer.getLength();
                result.word = buffer.getWord();
                result.count = buffer.getCount();
                
                buffer.incrementTuple();
                buffer.autoIncrementDocument();
                
                return result;
            }           
            
            public final void fill() throws IOException {
                try {   
                    buffer.reset();
                    
                    if (tupleCount != 0) {
                                                      
                        if(updateDocumentCount - tupleCount > 0) {
                            buffer.documents.add(last.document);
                            buffer.documentTupleIdx.add((int) (updateDocumentCount - tupleCount));
                        }
                        bufferStartCount = tupleCount;
                    }
                    
                    while (!buffer.isFull()) {
                        updateDocument();
                        buffer.processTuple(input.readInt(), input.readString(), input.readInt());
                        tupleCount++;
                    }
                } catch(EOFException e) {}
            }

            public final void updateDocument() throws IOException {
                if (updateDocumentCount > tupleCount)
                    return;
                     
                last.document = input.readString();
                updateDocumentCount = tupleCount + input.readInt();
                                      
                buffer.processDocument(last.document);
            }

            public void run() throws IOException {
                while (true) {
                    fill();
                    
                    if (buffer.isAtEnd())
                        break;
                    
                    buffer.copyUntil(null, processor);
                }      
                processor.close();
            }
            
            public void setProcessor(Step processor) throws IncompatibleProcessorException {  
                if (processor instanceof ShreddedProcessor) {
                    this.processor = new DuplicateEliminator((ShreddedProcessor) processor);
                } else if (processor instanceof DocumentLengthWordCount.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((DocumentLengthWordCount.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<DocumentLengthWordCount>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<DocumentLengthWordCount> getOutputClass() {
                return DocumentLengthWordCount.class;
            }                
        }
        
        public static class DuplicateEliminator implements ShreddedProcessor {
            public ShreddedProcessor processor;
            DocumentLengthWordCount last = new DocumentLengthWordCount();
            boolean documentProcess = true;
                                           
            public DuplicateEliminator() {}
            public DuplicateEliminator(ShreddedProcessor processor) {
                this.processor = processor;
            }
            
            public void setShreddedProcessor(ShreddedProcessor processor) {
                this.processor = processor;
            }

            public void processDocument(String document) throws IOException {  
                if (documentProcess || Utility.compare(document, last.document) != 0) {
                    last.document = document;
                    processor.processDocument(document);
                    documentProcess = false;
                }
            }  
            
            public void resetDocument() {
                 documentProcess = true;
            }                                                
                               
            public void processTuple(int length, String word, int count) throws IOException {
                processor.processTuple(length, word, count);
            } 
            
            public void close() throws IOException {
                processor.close();
            }                    
        }
        public static class TupleUnshredder implements ShreddedProcessor {
            DocumentLengthWordCount last = new DocumentLengthWordCount();
            public org.lemurproject.galago.tupleflow.Processor<DocumentLengthWordCount> processor;                               
            
            public TupleUnshredder(DocumentLengthWordCount.Processor processor) {
                this.processor = processor;
            }         
            
            public TupleUnshredder(org.lemurproject.galago.tupleflow.Processor<DocumentLengthWordCount> processor) {
                this.processor = processor;
            }
            
            public DocumentLengthWordCount clone(DocumentLengthWordCount object) {
                DocumentLengthWordCount result = new DocumentLengthWordCount();
                if (object == null) return result;
                result.document = object.document; 
                result.length = object.length; 
                result.word = object.word; 
                result.count = object.count; 
                return result;
            }                 
            
            public void processDocument(String document) throws IOException {
                last.document = document;
            }   
                
            
            public void processTuple(int length, String word, int count) throws IOException {
                last.length = length;
                last.word = word;
                last.count = count;
                processor.process(clone(last));
            }               
            
            public void close() throws IOException {
                processor.close();
            }
        }     
        public static class TupleShredder implements Processor {
            DocumentLengthWordCount last = null;
            public ShreddedProcessor processor;
            
            public TupleShredder(ShreddedProcessor processor) {
                this.processor = processor;
            }                              
            
            public DocumentLengthWordCount clone(DocumentLengthWordCount object) {
                DocumentLengthWordCount result = new DocumentLengthWordCount();
                if (object == null) return result;
                result.document = object.document; 
                result.length = object.length; 
                result.word = object.word; 
                result.count = object.count; 
                return result;
            }                 
            
            public void process(DocumentLengthWordCount object) throws IOException {                                                                                                                                                   
                boolean processAll = false;
                if(last == null || Utility.compare(last.document, object.document) != 0 || processAll) { processor.processDocument(object.document); processAll = true; }
                processor.processTuple(object.length, object.word, object.count);                                         
                last = object;
            }
                          
            public Class<DocumentLengthWordCount> getInputClass() {
                return DocumentLengthWordCount.class;
            }
            
            public void close() throws IOException {
                processor.close();
            }                     
        }
    } 
}    