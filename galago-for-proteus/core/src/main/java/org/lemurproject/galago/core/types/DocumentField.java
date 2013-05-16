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


public class DocumentField implements Type<DocumentField> {
    public String fieldName;
    public String identifier;
    public byte[] content; 
    
    public DocumentField() {}
    public DocumentField(String fieldName, String identifier, byte[] content) {
        this.fieldName = fieldName;
        this.identifier = identifier;
        this.content = content;
    }  
    
    public String toString() {
        try {
            return String.format("%s,%s,%s",
                                   fieldName, identifier, new String(content, "UTF-8"));
        } catch(UnsupportedEncodingException e) {
            throw new RuntimeException("Couldn't convert string to UTF-8.");
        }
    } 

    public Order<DocumentField> getOrder(String... spec) {
        if (Arrays.equals(spec, new String[] { "+identifier" })) {
            return new IdentifierOrder();
        }
        return null;
    } 
      
    public interface Processor extends Step, org.lemurproject.galago.tupleflow.Processor<DocumentField> {
        public void process(DocumentField object) throws IOException;
        public void close() throws IOException;
    }                        
    public interface Source extends Step {
    }
    public static class IdentifierOrder implements Order<DocumentField> {
        public int hash(DocumentField object) {
            int h = 0;
            h += Utility.hash(object.identifier);
            return h;
        } 
        public Comparator<DocumentField> greaterThan() {
            return new Comparator<DocumentField>() {
                public int compare(DocumentField one, DocumentField two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.identifier, two.identifier);
                        if(result != 0) break;
                    } while (false);
                    return -result;
                }
            };
        }     
        public Comparator<DocumentField> lessThan() {
            return new Comparator<DocumentField>() {
                public int compare(DocumentField one, DocumentField two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.identifier, two.identifier);
                        if(result != 0) break;
                    } while (false);
                    return result;
                }
            };
        }     
        public TypeReader<DocumentField> orderedReader(ArrayInput _input) {
            return new ShreddedReader(_input);
        }    

        public TypeReader<DocumentField> orderedReader(ArrayInput _input, int bufferSize) {
            return new ShreddedReader(_input, bufferSize);
        }    
        public OrderedWriter<DocumentField> orderedWriter(ArrayOutput _output) {
            ShreddedWriter w = new ShreddedWriter(_output);
            return new OrderedWriterClass(w); 
        }                                    
        public static class OrderedWriterClass extends OrderedWriter< DocumentField > {
            DocumentField last = null;
            ShreddedWriter shreddedWriter = null; 
            
            public OrderedWriterClass(ShreddedWriter s) {
                this.shreddedWriter = s;
            }
            
            public void process(DocumentField object) throws IOException {
               boolean processAll = false;
               if (processAll || last == null || 0 != Utility.compare(object.identifier, last.identifier)) { processAll = true; shreddedWriter.processIdentifier(object.identifier); }
               shreddedWriter.processTuple(object.fieldName, object.content);
               last = object;
            }           
                 
            public void close() throws IOException {
                shreddedWriter.close();
            }
            
            public Class<DocumentField> getInputClass() {
                return DocumentField.class;
            }
        } 
        public ReaderSource<DocumentField> orderedCombiner(Collection<TypeReader<DocumentField>> readers, boolean closeOnExit) {
            ArrayList<ShreddedReader> shreddedReaders = new ArrayList();
            
            for (TypeReader<DocumentField> reader : readers) {
                shreddedReaders.add((ShreddedReader)reader);
            }
            
            return new ShreddedCombiner(shreddedReaders, closeOnExit);
        }                  
        public DocumentField clone(DocumentField object) {
            DocumentField result = new DocumentField();
            if (object == null) return result;
            result.fieldName = object.fieldName; 
            result.identifier = object.identifier; 
            result.content = object.content; 
            return result;
        }                 
        public Class<DocumentField> getOrderedClass() {
            return DocumentField.class;
        }                           
        public String[] getOrderSpec() {
            return new String[] {"+identifier"};
        }

        public static String[] getSpec() {
            return new String[] {"+identifier"};
        }
        public static String getSpecString() {
            return "+identifier";
        }
                           
        public interface ShreddedProcessor extends Step {
            public void processIdentifier(String identifier) throws IOException;
            public void processTuple(String fieldName, byte[] content) throws IOException;
            public void close() throws IOException;
        }    
        public interface ShreddedSource extends Step {
        }                                              
        
        public static class ShreddedWriter implements ShreddedProcessor {
            ArrayOutput output;
            ShreddedBuffer buffer = new ShreddedBuffer();
            String lastIdentifier;
            boolean lastFlush = false;
            
            public ShreddedWriter(ArrayOutput output) {
                this.output = output;
            }                        
            
            public void close() throws IOException {
                flush();
            }
            
            public void processIdentifier(String identifier) {
                lastIdentifier = identifier;
                buffer.processIdentifier(identifier);
            }
            public final void processTuple(String fieldName, byte[] content) throws IOException {
                if (lastFlush) {
                    if(buffer.identifiers.size() == 0) buffer.processIdentifier(lastIdentifier);
                    lastFlush = false;
                }
                buffer.processTuple(fieldName, content);
                if (buffer.isFull())
                    flush();
            }
            public final void flushTuples(int pauseIndex) throws IOException {
                
                while (buffer.getReadIndex() < pauseIndex) {
                           
                    output.writeString(buffer.getFieldName());
                    output.writeBytes(buffer.getContent());
                    buffer.incrementTuple();
                }
            }  
            public final void flushIdentifier(int pauseIndex) throws IOException {
                while (buffer.getReadIndex() < pauseIndex) {
                    int nextPause = buffer.getIdentifierEndIndex();
                    int count = nextPause - buffer.getReadIndex();
                    
                    output.writeString(buffer.getIdentifier());
                    output.writeInt(count);
                    buffer.incrementIdentifier();
                      
                    flushTuples(nextPause);
                    assert nextPause == buffer.getReadIndex();
                }
            }
            public void flush() throws IOException { 
                flushIdentifier(buffer.getWriteIndex());
                buffer.reset(); 
                lastFlush = true;
            }                           
        }
        public static class ShreddedBuffer {
            ArrayList<String> identifiers = new ArrayList();
            TIntArrayList identifierTupleIdx = new TIntArrayList();
            int identifierReadIdx = 0;
                            
            String[] fieldNames;
            byte[][] contents;
            int writeTupleIndex = 0;
            int readTupleIndex = 0;
            int batchSize;

            public ShreddedBuffer(int batchSize) {
                this.batchSize = batchSize;

                fieldNames = new String[batchSize];
                contents = new byte[batchSize][];
            }                              

            public ShreddedBuffer() {    
                this(10000);
            }                                                                                                                    
            
            public void processIdentifier(String identifier) {
                identifiers.add(identifier);
                identifierTupleIdx.add(writeTupleIndex);
            }                                      
            public void processTuple(String fieldName, byte[] content) {
                assert identifiers.size() > 0;
                fieldNames[writeTupleIndex] = fieldName;
                contents[writeTupleIndex] = content;
                writeTupleIndex++;
            }
            public void resetData() {
                identifiers.clear();
                identifierTupleIdx.clear();
                writeTupleIndex = 0;
            }                  
                                 
            public void resetRead() {
                readTupleIndex = 0;
                identifierReadIdx = 0;
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
            public void incrementIdentifier() {
                identifierReadIdx++;  
            }                                                                                              

            public void autoIncrementIdentifier() {
                while (readTupleIndex >= getIdentifierEndIndex() && readTupleIndex < writeTupleIndex)
                    identifierReadIdx++;
            }                 
            public void incrementTuple() {
                readTupleIndex++;
            }                    
            public int getIdentifierEndIndex() {
                if ((identifierReadIdx+1) >= identifierTupleIdx.size())
                    return writeTupleIndex;
                return identifierTupleIdx.get(identifierReadIdx+1);
            }
            public int getReadIndex() {
                return readTupleIndex;
            }   

            public int getWriteIndex() {
                return writeTupleIndex;
            } 
            public String getIdentifier() {
                assert readTupleIndex < writeTupleIndex;
                assert identifierReadIdx < identifiers.size();
                
                return identifiers.get(identifierReadIdx);
            }
            public String getFieldName() {
                assert readTupleIndex < writeTupleIndex;
                return fieldNames[readTupleIndex];
            }                                         
            public byte[] getContent() {
                assert readTupleIndex < writeTupleIndex;
                return contents[readTupleIndex];
            }                                         
            public void copyTuples(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                   output.processTuple(getFieldName(), getContent());
                   incrementTuple();
                }
            }                                                                           
            public void copyUntilIndexIdentifier(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                    output.processIdentifier(getIdentifier());
                    assert getIdentifierEndIndex() <= endIndex;
                    copyTuples(getIdentifierEndIndex(), output);
                    incrementIdentifier();
                }
            }  
            public void copyUntilIdentifier(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                while (!isAtEnd()) {
                    if (other != null) {   
                        assert !other.isAtEnd();
                        int c = + Utility.compare(getIdentifier(), other.getIdentifier());
                    
                        if (c > 0) {
                            break;   
                        }
                        
                        output.processIdentifier(getIdentifier());
                                      
                        copyTuples(getIdentifierEndIndex(), output);
                    } else {
                        output.processIdentifier(getIdentifier());
                        copyTuples(getIdentifierEndIndex(), output);
                    }
                    incrementIdentifier();  
                    
               
                }
            }
            public void copyUntil(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                copyUntilIdentifier(other, output);
            }
            
        }                         
        public static class ShreddedCombiner implements ReaderSource<DocumentField>, ShreddedSource {   
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
                } else if (processor instanceof DocumentField.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((DocumentField.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<DocumentField>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<DocumentField> getOutputClass() {
                return DocumentField.class;
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

            public DocumentField read() throws IOException {
                if (uninitialized)
                    initialize();

                DocumentField result = null;

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
        public static class ShreddedReader implements Step, Comparable<ShreddedReader>, TypeReader<DocumentField>, ShreddedSource {      
            public ShreddedProcessor processor;
            ShreddedBuffer buffer;
            DocumentField last = new DocumentField();         
            long updateIdentifierCount = -1;
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
                    result = + Utility.compare(buffer.getIdentifier(), otherBuffer.getIdentifier());
                    if(result != 0) break;
                } while (false);                                             
                
                return result;
            }
            
            public final ShreddedBuffer getBuffer() {
                return buffer;
            }                
            
            public final DocumentField read() throws IOException {
                if (buffer.isAtEnd()) {
                    fill();             
                
                    if (buffer.isAtEnd()) {
                        return null;
                    }
                }
                      
                assert !buffer.isAtEnd();
                DocumentField result = new DocumentField();
                
                result.identifier = buffer.getIdentifier();
                result.fieldName = buffer.getFieldName();
                result.content = buffer.getContent();
                
                buffer.incrementTuple();
                buffer.autoIncrementIdentifier();
                
                return result;
            }           
            
            public final void fill() throws IOException {
                try {   
                    buffer.reset();
                    
                    if (tupleCount != 0) {
                                                      
                        if(updateIdentifierCount - tupleCount > 0) {
                            buffer.identifiers.add(last.identifier);
                            buffer.identifierTupleIdx.add((int) (updateIdentifierCount - tupleCount));
                        }
                        bufferStartCount = tupleCount;
                    }
                    
                    while (!buffer.isFull()) {
                        updateIdentifier();
                        buffer.processTuple(input.readString(), input.readBytes());
                        tupleCount++;
                    }
                } catch(EOFException e) {}
            }

            public final void updateIdentifier() throws IOException {
                if (updateIdentifierCount > tupleCount)
                    return;
                     
                last.identifier = input.readString();
                updateIdentifierCount = tupleCount + input.readInt();
                                      
                buffer.processIdentifier(last.identifier);
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
                } else if (processor instanceof DocumentField.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((DocumentField.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<DocumentField>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<DocumentField> getOutputClass() {
                return DocumentField.class;
            }                
        }
        
        public static class DuplicateEliminator implements ShreddedProcessor {
            public ShreddedProcessor processor;
            DocumentField last = new DocumentField();
            boolean identifierProcess = true;
                                           
            public DuplicateEliminator() {}
            public DuplicateEliminator(ShreddedProcessor processor) {
                this.processor = processor;
            }
            
            public void setShreddedProcessor(ShreddedProcessor processor) {
                this.processor = processor;
            }

            public void processIdentifier(String identifier) throws IOException {  
                if (identifierProcess || Utility.compare(identifier, last.identifier) != 0) {
                    last.identifier = identifier;
                    processor.processIdentifier(identifier);
                    identifierProcess = false;
                }
            }  
            
            public void resetIdentifier() {
                 identifierProcess = true;
            }                                                
                               
            public void processTuple(String fieldName, byte[] content) throws IOException {
                processor.processTuple(fieldName, content);
            } 
            
            public void close() throws IOException {
                processor.close();
            }                    
        }
        public static class TupleUnshredder implements ShreddedProcessor {
            DocumentField last = new DocumentField();
            public org.lemurproject.galago.tupleflow.Processor<DocumentField> processor;                               
            
            public TupleUnshredder(DocumentField.Processor processor) {
                this.processor = processor;
            }         
            
            public TupleUnshredder(org.lemurproject.galago.tupleflow.Processor<DocumentField> processor) {
                this.processor = processor;
            }
            
            public DocumentField clone(DocumentField object) {
                DocumentField result = new DocumentField();
                if (object == null) return result;
                result.fieldName = object.fieldName; 
                result.identifier = object.identifier; 
                result.content = object.content; 
                return result;
            }                 
            
            public void processIdentifier(String identifier) throws IOException {
                last.identifier = identifier;
            }   
                
            
            public void processTuple(String fieldName, byte[] content) throws IOException {
                last.fieldName = fieldName;
                last.content = content;
                processor.process(clone(last));
            }               
            
            public void close() throws IOException {
                processor.close();
            }
        }     
        public static class TupleShredder implements Processor {
            DocumentField last = null;
            public ShreddedProcessor processor;
            
            public TupleShredder(ShreddedProcessor processor) {
                this.processor = processor;
            }                              
            
            public DocumentField clone(DocumentField object) {
                DocumentField result = new DocumentField();
                if (object == null) return result;
                result.fieldName = object.fieldName; 
                result.identifier = object.identifier; 
                result.content = object.content; 
                return result;
            }                 
            
            public void process(DocumentField object) throws IOException {                                                                                                                                                   
                boolean processAll = false;
                if(last == null || Utility.compare(last.identifier, object.identifier) != 0 || processAll) { processor.processIdentifier(object.identifier); processAll = true; }
                processor.processTuple(object.fieldName, object.content);                                         
                last = object;
            }
                          
            public Class<DocumentField> getInputClass() {
                return DocumentField.class;
            }
            
            public void close() throws IOException {
                processor.close();
            }                     
        }
    } 
}    