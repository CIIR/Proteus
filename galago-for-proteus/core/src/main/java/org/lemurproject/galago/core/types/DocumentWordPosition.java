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


public class DocumentWordPosition implements Type<DocumentWordPosition> {
    public String document;
    public byte[] word;
    public int position; 
    
    public DocumentWordPosition() {}
    public DocumentWordPosition(String document, byte[] word, int position) {
        this.document = document;
        this.word = word;
        this.position = position;
    }  
    
    public String toString() {
        try {
            return String.format("%s,%s,%d",
                                   document, new String(word, "UTF-8"), position);
        } catch(UnsupportedEncodingException e) {
            throw new RuntimeException("Couldn't convert string to UTF-8.");
        }
    } 

    public Order<DocumentWordPosition> getOrder(String... spec) {
        if (Arrays.equals(spec, new String[] { "+document" })) {
            return new DocumentOrder();
        }
        if (Arrays.equals(spec, new String[] { "+document", "+word", "+position" })) {
            return new DocumentWordPositionOrder();
        }
        return null;
    } 
      
    public interface Processor extends Step, org.lemurproject.galago.tupleflow.Processor<DocumentWordPosition> {
        public void process(DocumentWordPosition object) throws IOException;
        public void close() throws IOException;
    }                        
    public interface Source extends Step {
    }
    public static class DocumentOrder implements Order<DocumentWordPosition> {
        public int hash(DocumentWordPosition object) {
            int h = 0;
            h += Utility.hash(object.document);
            return h;
        } 
        public Comparator<DocumentWordPosition> greaterThan() {
            return new Comparator<DocumentWordPosition>() {
                public int compare(DocumentWordPosition one, DocumentWordPosition two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.document, two.document);
                        if(result != 0) break;
                    } while (false);
                    return -result;
                }
            };
        }     
        public Comparator<DocumentWordPosition> lessThan() {
            return new Comparator<DocumentWordPosition>() {
                public int compare(DocumentWordPosition one, DocumentWordPosition two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.document, two.document);
                        if(result != 0) break;
                    } while (false);
                    return result;
                }
            };
        }     
        public TypeReader<DocumentWordPosition> orderedReader(ArrayInput _input) {
            return new ShreddedReader(_input);
        }    

        public TypeReader<DocumentWordPosition> orderedReader(ArrayInput _input, int bufferSize) {
            return new ShreddedReader(_input, bufferSize);
        }    
        public OrderedWriter<DocumentWordPosition> orderedWriter(ArrayOutput _output) {
            ShreddedWriter w = new ShreddedWriter(_output);
            return new OrderedWriterClass(w); 
        }                                    
        public static class OrderedWriterClass extends OrderedWriter< DocumentWordPosition > {
            DocumentWordPosition last = null;
            ShreddedWriter shreddedWriter = null; 
            
            public OrderedWriterClass(ShreddedWriter s) {
                this.shreddedWriter = s;
            }
            
            public void process(DocumentWordPosition object) throws IOException {
               boolean processAll = false;
               if (processAll || last == null || 0 != Utility.compare(object.document, last.document)) { processAll = true; shreddedWriter.processDocument(object.document); }
               shreddedWriter.processTuple(object.word, object.position);
               last = object;
            }           
                 
            public void close() throws IOException {
                shreddedWriter.close();
            }
            
            public Class<DocumentWordPosition> getInputClass() {
                return DocumentWordPosition.class;
            }
        } 
        public ReaderSource<DocumentWordPosition> orderedCombiner(Collection<TypeReader<DocumentWordPosition>> readers, boolean closeOnExit) {
            ArrayList<ShreddedReader> shreddedReaders = new ArrayList();
            
            for (TypeReader<DocumentWordPosition> reader : readers) {
                shreddedReaders.add((ShreddedReader)reader);
            }
            
            return new ShreddedCombiner(shreddedReaders, closeOnExit);
        }                  
        public DocumentWordPosition clone(DocumentWordPosition object) {
            DocumentWordPosition result = new DocumentWordPosition();
            if (object == null) return result;
            result.document = object.document; 
            result.word = object.word; 
            result.position = object.position; 
            return result;
        }                 
        public Class<DocumentWordPosition> getOrderedClass() {
            return DocumentWordPosition.class;
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
            public void processTuple(byte[] word, int position) throws IOException;
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
            public final void processTuple(byte[] word, int position) throws IOException {
                if (lastFlush) {
                    if(buffer.documents.size() == 0) buffer.processDocument(lastDocument);
                    lastFlush = false;
                }
                buffer.processTuple(word, position);
                if (buffer.isFull())
                    flush();
            }
            public final void flushTuples(int pauseIndex) throws IOException {
                
                while (buffer.getReadIndex() < pauseIndex) {
                           
                    output.writeBytes(buffer.getWord());
                    output.writeInt(buffer.getPosition());
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
                            
            byte[][] words;
            int[] positions;
            int writeTupleIndex = 0;
            int readTupleIndex = 0;
            int batchSize;

            public ShreddedBuffer(int batchSize) {
                this.batchSize = batchSize;

                words = new byte[batchSize][];
                positions = new int[batchSize];
            }                              

            public ShreddedBuffer() {    
                this(10000);
            }                                                                                                                    
            
            public void processDocument(String document) {
                documents.add(document);
                documentTupleIdx.add(writeTupleIndex);
            }                                      
            public void processTuple(byte[] word, int position) {
                assert documents.size() > 0;
                words[writeTupleIndex] = word;
                positions[writeTupleIndex] = position;
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
            public byte[] getWord() {
                assert readTupleIndex < writeTupleIndex;
                return words[readTupleIndex];
            }                                         
            public int getPosition() {
                assert readTupleIndex < writeTupleIndex;
                return positions[readTupleIndex];
            }                                         
            public void copyTuples(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                   output.processTuple(getWord(), getPosition());
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
        public static class ShreddedCombiner implements ReaderSource<DocumentWordPosition>, ShreddedSource {   
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
                } else if (processor instanceof DocumentWordPosition.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((DocumentWordPosition.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<DocumentWordPosition>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<DocumentWordPosition> getOutputClass() {
                return DocumentWordPosition.class;
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

            public DocumentWordPosition read() throws IOException {
                if (uninitialized)
                    initialize();

                DocumentWordPosition result = null;

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
        public static class ShreddedReader implements Step, Comparable<ShreddedReader>, TypeReader<DocumentWordPosition>, ShreddedSource {      
            public ShreddedProcessor processor;
            ShreddedBuffer buffer;
            DocumentWordPosition last = new DocumentWordPosition();         
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
            
            public final DocumentWordPosition read() throws IOException {
                if (buffer.isAtEnd()) {
                    fill();             
                
                    if (buffer.isAtEnd()) {
                        return null;
                    }
                }
                      
                assert !buffer.isAtEnd();
                DocumentWordPosition result = new DocumentWordPosition();
                
                result.document = buffer.getDocument();
                result.word = buffer.getWord();
                result.position = buffer.getPosition();
                
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
                        buffer.processTuple(input.readBytes(), input.readInt());
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
                } else if (processor instanceof DocumentWordPosition.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((DocumentWordPosition.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<DocumentWordPosition>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<DocumentWordPosition> getOutputClass() {
                return DocumentWordPosition.class;
            }                
        }
        
        public static class DuplicateEliminator implements ShreddedProcessor {
            public ShreddedProcessor processor;
            DocumentWordPosition last = new DocumentWordPosition();
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
                               
            public void processTuple(byte[] word, int position) throws IOException {
                processor.processTuple(word, position);
            } 
            
            public void close() throws IOException {
                processor.close();
            }                    
        }
        public static class TupleUnshredder implements ShreddedProcessor {
            DocumentWordPosition last = new DocumentWordPosition();
            public org.lemurproject.galago.tupleflow.Processor<DocumentWordPosition> processor;                               
            
            public TupleUnshredder(DocumentWordPosition.Processor processor) {
                this.processor = processor;
            }         
            
            public TupleUnshredder(org.lemurproject.galago.tupleflow.Processor<DocumentWordPosition> processor) {
                this.processor = processor;
            }
            
            public DocumentWordPosition clone(DocumentWordPosition object) {
                DocumentWordPosition result = new DocumentWordPosition();
                if (object == null) return result;
                result.document = object.document; 
                result.word = object.word; 
                result.position = object.position; 
                return result;
            }                 
            
            public void processDocument(String document) throws IOException {
                last.document = document;
            }   
                
            
            public void processTuple(byte[] word, int position) throws IOException {
                last.word = word;
                last.position = position;
                processor.process(clone(last));
            }               
            
            public void close() throws IOException {
                processor.close();
            }
        }     
        public static class TupleShredder implements Processor {
            DocumentWordPosition last = null;
            public ShreddedProcessor processor;
            
            public TupleShredder(ShreddedProcessor processor) {
                this.processor = processor;
            }                              
            
            public DocumentWordPosition clone(DocumentWordPosition object) {
                DocumentWordPosition result = new DocumentWordPosition();
                if (object == null) return result;
                result.document = object.document; 
                result.word = object.word; 
                result.position = object.position; 
                return result;
            }                 
            
            public void process(DocumentWordPosition object) throws IOException {                                                                                                                                                   
                boolean processAll = false;
                if(last == null || Utility.compare(last.document, object.document) != 0 || processAll) { processor.processDocument(object.document); processAll = true; }
                processor.processTuple(object.word, object.position);                                         
                last = object;
            }
                          
            public Class<DocumentWordPosition> getInputClass() {
                return DocumentWordPosition.class;
            }
            
            public void close() throws IOException {
                processor.close();
            }                     
        }
    } 
    public static class DocumentWordPositionOrder implements Order<DocumentWordPosition> {
        public int hash(DocumentWordPosition object) {
            int h = 0;
            h += Utility.hash(object.document);
            h += Utility.hash(object.word);
            h += Utility.hash(object.position);
            return h;
        } 
        public Comparator<DocumentWordPosition> greaterThan() {
            return new Comparator<DocumentWordPosition>() {
                public int compare(DocumentWordPosition one, DocumentWordPosition two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.document, two.document);
                        if(result != 0) break;
                        result = + Utility.compare(one.word, two.word);
                        if(result != 0) break;
                        result = + Utility.compare(one.position, two.position);
                        if(result != 0) break;
                    } while (false);
                    return -result;
                }
            };
        }     
        public Comparator<DocumentWordPosition> lessThan() {
            return new Comparator<DocumentWordPosition>() {
                public int compare(DocumentWordPosition one, DocumentWordPosition two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.document, two.document);
                        if(result != 0) break;
                        result = + Utility.compare(one.word, two.word);
                        if(result != 0) break;
                        result = + Utility.compare(one.position, two.position);
                        if(result != 0) break;
                    } while (false);
                    return result;
                }
            };
        }     
        public TypeReader<DocumentWordPosition> orderedReader(ArrayInput _input) {
            return new ShreddedReader(_input);
        }    

        public TypeReader<DocumentWordPosition> orderedReader(ArrayInput _input, int bufferSize) {
            return new ShreddedReader(_input, bufferSize);
        }    
        public OrderedWriter<DocumentWordPosition> orderedWriter(ArrayOutput _output) {
            ShreddedWriter w = new ShreddedWriter(_output);
            return new OrderedWriterClass(w); 
        }                                    
        public static class OrderedWriterClass extends OrderedWriter< DocumentWordPosition > {
            DocumentWordPosition last = null;
            ShreddedWriter shreddedWriter = null; 
            
            public OrderedWriterClass(ShreddedWriter s) {
                this.shreddedWriter = s;
            }
            
            public void process(DocumentWordPosition object) throws IOException {
               boolean processAll = false;
               if (processAll || last == null || 0 != Utility.compare(object.document, last.document)) { processAll = true; shreddedWriter.processDocument(object.document); }
               if (processAll || last == null || 0 != Utility.compare(object.word, last.word)) { processAll = true; shreddedWriter.processWord(object.word); }
               if (processAll || last == null || 0 != Utility.compare(object.position, last.position)) { processAll = true; shreddedWriter.processPosition(object.position); }
               shreddedWriter.processTuple();
               last = object;
            }           
                 
            public void close() throws IOException {
                shreddedWriter.close();
            }
            
            public Class<DocumentWordPosition> getInputClass() {
                return DocumentWordPosition.class;
            }
        } 
        public ReaderSource<DocumentWordPosition> orderedCombiner(Collection<TypeReader<DocumentWordPosition>> readers, boolean closeOnExit) {
            ArrayList<ShreddedReader> shreddedReaders = new ArrayList();
            
            for (TypeReader<DocumentWordPosition> reader : readers) {
                shreddedReaders.add((ShreddedReader)reader);
            }
            
            return new ShreddedCombiner(shreddedReaders, closeOnExit);
        }                  
        public DocumentWordPosition clone(DocumentWordPosition object) {
            DocumentWordPosition result = new DocumentWordPosition();
            if (object == null) return result;
            result.document = object.document; 
            result.word = object.word; 
            result.position = object.position; 
            return result;
        }                 
        public Class<DocumentWordPosition> getOrderedClass() {
            return DocumentWordPosition.class;
        }                           
        public String[] getOrderSpec() {
            return new String[] {"+document", "+word", "+position"};
        }

        public static String[] getSpec() {
            return new String[] {"+document", "+word", "+position"};
        }
        public static String getSpecString() {
            return "+document +word +position";
        }
                           
        public interface ShreddedProcessor extends Step {
            public void processDocument(String document) throws IOException;
            public void processWord(byte[] word) throws IOException;
            public void processPosition(int position) throws IOException;
            public void processTuple() throws IOException;
            public void close() throws IOException;
        }    
        public interface ShreddedSource extends Step {
        }                                              
        
        public static class ShreddedWriter implements ShreddedProcessor {
            ArrayOutput output;
            ShreddedBuffer buffer = new ShreddedBuffer();
            String lastDocument;
            byte[] lastWord;
            int lastPosition;
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
            public void processWord(byte[] word) {
                lastWord = word;
                buffer.processWord(word);
            }
            public void processPosition(int position) {
                lastPosition = position;
                buffer.processPosition(position);
            }
            public final void processTuple() throws IOException {
                if (lastFlush) {
                    if(buffer.documents.size() == 0) buffer.processDocument(lastDocument);
                    if(buffer.words.size() == 0) buffer.processWord(lastWord);
                    if(buffer.positions.size() == 0) buffer.processPosition(lastPosition);
                    lastFlush = false;
                }
                buffer.processTuple();
                if (buffer.isFull())
                    flush();
            }
            public final void flushTuples(int pauseIndex) throws IOException {
                
                while (buffer.getReadIndex() < pauseIndex) {
                           
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
                    
                    output.writeBytes(buffer.getWord());
                    output.writeInt(count);
                    buffer.incrementWord();
                      
                    flushPosition(nextPause);
                    assert nextPause == buffer.getReadIndex();
                }
            }
            public final void flushPosition(int pauseIndex) throws IOException {
                while (buffer.getReadIndex() < pauseIndex) {
                    int nextPause = buffer.getPositionEndIndex();
                    int count = nextPause - buffer.getReadIndex();
                    
                    output.writeInt(buffer.getPosition());
                    output.writeInt(count);
                    buffer.incrementPosition();
                      
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
            ArrayList<byte[]> words = new ArrayList();
            TIntArrayList positions = new TIntArrayList();
            TIntArrayList documentTupleIdx = new TIntArrayList();
            TIntArrayList wordTupleIdx = new TIntArrayList();
            TIntArrayList positionTupleIdx = new TIntArrayList();
            int documentReadIdx = 0;
            int wordReadIdx = 0;
            int positionReadIdx = 0;
                            
            int writeTupleIndex = 0;
            int readTupleIndex = 0;
            int batchSize;

            public ShreddedBuffer(int batchSize) {
                this.batchSize = batchSize;

            }                              

            public ShreddedBuffer() {    
                this(10000);
            }                                                                                                                    
            
            public void processDocument(String document) {
                documents.add(document);
                documentTupleIdx.add(writeTupleIndex);
            }                                      
            public void processWord(byte[] word) {
                words.add(word);
                wordTupleIdx.add(writeTupleIndex);
            }                                      
            public void processPosition(int position) {
                positions.add(position);
                positionTupleIdx.add(writeTupleIndex);
            }                                      
            public void processTuple() {
                assert documents.size() > 0;
                assert words.size() > 0;
                assert positions.size() > 0;
                writeTupleIndex++;
            }
            public void resetData() {
                documents.clear();
                words.clear();
                positions.clear();
                documentTupleIdx.clear();
                wordTupleIdx.clear();
                positionTupleIdx.clear();
                writeTupleIndex = 0;
            }                  
                                 
            public void resetRead() {
                readTupleIndex = 0;
                documentReadIdx = 0;
                wordReadIdx = 0;
                positionReadIdx = 0;
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
            public void incrementPosition() {
                positionReadIdx++;  
            }                                                                                              

            public void autoIncrementPosition() {
                while (readTupleIndex >= getPositionEndIndex() && readTupleIndex < writeTupleIndex)
                    positionReadIdx++;
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

            public int getPositionEndIndex() {
                if ((positionReadIdx+1) >= positionTupleIdx.size())
                    return writeTupleIndex;
                return positionTupleIdx.get(positionReadIdx+1);
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
            public byte[] getWord() {
                assert readTupleIndex < writeTupleIndex;
                assert wordReadIdx < words.size();
                
                return words.get(wordReadIdx);
            }
            public int getPosition() {
                assert readTupleIndex < writeTupleIndex;
                assert positionReadIdx < positions.size();
                
                return positions.get(positionReadIdx);
            }

            public void copyTuples(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                   output.processTuple();
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
                    copyUntilIndexPosition(getWordEndIndex(), output);
                    incrementWord();
                }
            } 
            public void copyUntilIndexPosition(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                    output.processPosition(getPosition());
                    assert getPositionEndIndex() <= endIndex;
                    copyTuples(getPositionEndIndex(), output);
                    incrementPosition();
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
                                      
                        if (c < 0) {
                            copyUntilIndexPosition(getWordEndIndex(), output);
                        } else if (c == 0) {
                            copyUntilPosition(other, output);
                            autoIncrementWord();
                            break;
                        }
                    } else {
                        output.processWord(getWord());
                        copyUntilIndexPosition(getWordEndIndex(), output);
                    }
                    incrementWord();  
                    
                    if (getDocumentEndIndex() <= readTupleIndex)
                        break;   
                }
            }
            public void copyUntilPosition(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                while (!isAtEnd()) {
                    if (other != null) {   
                        assert !other.isAtEnd();
                        int c = + Utility.compare(getPosition(), other.getPosition());
                    
                        if (c > 0) {
                            break;   
                        }
                        
                        output.processPosition(getPosition());
                                      
                        copyTuples(getPositionEndIndex(), output);
                    } else {
                        output.processPosition(getPosition());
                        copyTuples(getPositionEndIndex(), output);
                    }
                    incrementPosition();  
                    
                    if (getWordEndIndex() <= readTupleIndex)
                        break;   
                }
            }
            public void copyUntil(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                copyUntilDocument(other, output);
            }
            
        }                         
        public static class ShreddedCombiner implements ReaderSource<DocumentWordPosition>, ShreddedSource {   
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
                } else if (processor instanceof DocumentWordPosition.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((DocumentWordPosition.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<DocumentWordPosition>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<DocumentWordPosition> getOutputClass() {
                return DocumentWordPosition.class;
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

            public DocumentWordPosition read() throws IOException {
                if (uninitialized)
                    initialize();

                DocumentWordPosition result = null;

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
        public static class ShreddedReader implements Step, Comparable<ShreddedReader>, TypeReader<DocumentWordPosition>, ShreddedSource {      
            public ShreddedProcessor processor;
            ShreddedBuffer buffer;
            DocumentWordPosition last = new DocumentWordPosition();         
            long updateDocumentCount = -1;
            long updateWordCount = -1;
            long updatePositionCount = -1;
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
                    result = + Utility.compare(buffer.getPosition(), otherBuffer.getPosition());
                    if(result != 0) break;
                } while (false);                                             
                
                return result;
            }
            
            public final ShreddedBuffer getBuffer() {
                return buffer;
            }                
            
            public final DocumentWordPosition read() throws IOException {
                if (buffer.isAtEnd()) {
                    fill();             
                
                    if (buffer.isAtEnd()) {
                        return null;
                    }
                }
                      
                assert !buffer.isAtEnd();
                DocumentWordPosition result = new DocumentWordPosition();
                
                result.document = buffer.getDocument();
                result.word = buffer.getWord();
                result.position = buffer.getPosition();
                
                buffer.incrementTuple();
                buffer.autoIncrementDocument();
                buffer.autoIncrementWord();
                buffer.autoIncrementPosition();
                
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
                        if(updatePositionCount - tupleCount > 0) {
                            buffer.positions.add(last.position);
                            buffer.positionTupleIdx.add((int) (updatePositionCount - tupleCount));
                        }
                        bufferStartCount = tupleCount;
                    }
                    
                    while (!buffer.isFull()) {
                        updatePosition();
                        buffer.processTuple();
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
                last.word = input.readBytes();
                updateWordCount = tupleCount + input.readInt();
                                      
                buffer.processWord(last.word);
            }
            public final void updatePosition() throws IOException {
                if (updatePositionCount > tupleCount)
                    return;
                     
                updateWord();
                last.position = input.readInt();
                updatePositionCount = tupleCount + input.readInt();
                                      
                buffer.processPosition(last.position);
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
                } else if (processor instanceof DocumentWordPosition.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((DocumentWordPosition.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<DocumentWordPosition>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<DocumentWordPosition> getOutputClass() {
                return DocumentWordPosition.class;
            }                
        }
        
        public static class DuplicateEliminator implements ShreddedProcessor {
            public ShreddedProcessor processor;
            DocumentWordPosition last = new DocumentWordPosition();
            boolean documentProcess = true;
            boolean wordProcess = true;
            boolean positionProcess = true;
                                           
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
            public void processWord(byte[] word) throws IOException {  
                if (wordProcess || Utility.compare(word, last.word) != 0) {
                    last.word = word;
                    processor.processWord(word);
            resetPosition();
                    wordProcess = false;
                }
            }
            public void processPosition(int position) throws IOException {  
                if (positionProcess || Utility.compare(position, last.position) != 0) {
                    last.position = position;
                    processor.processPosition(position);
                    positionProcess = false;
                }
            }  
            
            public void resetDocument() {
                 documentProcess = true;
            resetWord();
            }                                                
            public void resetWord() {
                 wordProcess = true;
            resetPosition();
            }                                                
            public void resetPosition() {
                 positionProcess = true;
            }                                                
                               
            public void processTuple() throws IOException {
                processor.processTuple();
            } 
            
            public void close() throws IOException {
                processor.close();
            }                    
        }
        public static class TupleUnshredder implements ShreddedProcessor {
            DocumentWordPosition last = new DocumentWordPosition();
            public org.lemurproject.galago.tupleflow.Processor<DocumentWordPosition> processor;                               
            
            public TupleUnshredder(DocumentWordPosition.Processor processor) {
                this.processor = processor;
            }         
            
            public TupleUnshredder(org.lemurproject.galago.tupleflow.Processor<DocumentWordPosition> processor) {
                this.processor = processor;
            }
            
            public DocumentWordPosition clone(DocumentWordPosition object) {
                DocumentWordPosition result = new DocumentWordPosition();
                if (object == null) return result;
                result.document = object.document; 
                result.word = object.word; 
                result.position = object.position; 
                return result;
            }                 
            
            public void processDocument(String document) throws IOException {
                last.document = document;
            }   
                
            public void processWord(byte[] word) throws IOException {
                last.word = word;
            }   
                
            public void processPosition(int position) throws IOException {
                last.position = position;
            }   
                
            
            public void processTuple() throws IOException {
                processor.process(clone(last));
            }               
            
            public void close() throws IOException {
                processor.close();
            }
        }     
        public static class TupleShredder implements Processor {
            DocumentWordPosition last = null;
            public ShreddedProcessor processor;
            
            public TupleShredder(ShreddedProcessor processor) {
                this.processor = processor;
            }                              
            
            public DocumentWordPosition clone(DocumentWordPosition object) {
                DocumentWordPosition result = new DocumentWordPosition();
                if (object == null) return result;
                result.document = object.document; 
                result.word = object.word; 
                result.position = object.position; 
                return result;
            }                 
            
            public void process(DocumentWordPosition object) throws IOException {                                                                                                                                                   
                boolean processAll = false;
                if(last == null || Utility.compare(last.document, object.document) != 0 || processAll) { processor.processDocument(object.document); processAll = true; }
                if(last == null || Utility.compare(last.word, object.word) != 0 || processAll) { processor.processWord(object.word); processAll = true; }
                if(last == null || Utility.compare(last.position, object.position) != 0 || processAll) { processor.processPosition(object.position); processAll = true; }
                processor.processTuple();                                         
                last = object;
            }
                          
            public Class<DocumentWordPosition> getInputClass() {
                return DocumentWordPosition.class;
            }
            
            public void close() throws IOException {
                processor.close();
            }                     
        }
    } 
}    