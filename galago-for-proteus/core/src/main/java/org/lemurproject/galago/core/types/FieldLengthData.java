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


public class FieldLengthData implements Type<FieldLengthData> {
    public byte[] field;
    public int document;
    public int length; 
    
    public FieldLengthData() {}
    public FieldLengthData(byte[] field, int document, int length) {
        this.field = field;
        this.document = document;
        this.length = length;
    }  
    
    public String toString() {
        try {
            return String.format("%s,%d,%d",
                                   new String(field, "UTF-8"), document, length);
        } catch(UnsupportedEncodingException e) {
            throw new RuntimeException("Couldn't convert string to UTF-8.");
        }
    } 

    public Order<FieldLengthData> getOrder(String... spec) {
        if (Arrays.equals(spec, new String[] { "+field", "+document" })) {
            return new FieldDocumentOrder();
        }
        return null;
    } 
      
    public interface Processor extends Step, org.lemurproject.galago.tupleflow.Processor<FieldLengthData> {
        public void process(FieldLengthData object) throws IOException;
        public void close() throws IOException;
    }                        
    public interface Source extends Step {
    }
    public static class FieldDocumentOrder implements Order<FieldLengthData> {
        public int hash(FieldLengthData object) {
            int h = 0;
            h += Utility.hash(object.field);
            h += Utility.hash(object.document);
            return h;
        } 
        public Comparator<FieldLengthData> greaterThan() {
            return new Comparator<FieldLengthData>() {
                public int compare(FieldLengthData one, FieldLengthData two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.field, two.field);
                        if(result != 0) break;
                        result = + Utility.compare(one.document, two.document);
                        if(result != 0) break;
                    } while (false);
                    return -result;
                }
            };
        }     
        public Comparator<FieldLengthData> lessThan() {
            return new Comparator<FieldLengthData>() {
                public int compare(FieldLengthData one, FieldLengthData two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.field, two.field);
                        if(result != 0) break;
                        result = + Utility.compare(one.document, two.document);
                        if(result != 0) break;
                    } while (false);
                    return result;
                }
            };
        }     
        public TypeReader<FieldLengthData> orderedReader(ArrayInput _input) {
            return new ShreddedReader(_input);
        }    

        public TypeReader<FieldLengthData> orderedReader(ArrayInput _input, int bufferSize) {
            return new ShreddedReader(_input, bufferSize);
        }    
        public OrderedWriter<FieldLengthData> orderedWriter(ArrayOutput _output) {
            ShreddedWriter w = new ShreddedWriter(_output);
            return new OrderedWriterClass(w); 
        }                                    
        public static class OrderedWriterClass extends OrderedWriter< FieldLengthData > {
            FieldLengthData last = null;
            ShreddedWriter shreddedWriter = null; 
            
            public OrderedWriterClass(ShreddedWriter s) {
                this.shreddedWriter = s;
            }
            
            public void process(FieldLengthData object) throws IOException {
               boolean processAll = false;
               if (processAll || last == null || 0 != Utility.compare(object.field, last.field)) { processAll = true; shreddedWriter.processField(object.field); }
               if (processAll || last == null || 0 != Utility.compare(object.document, last.document)) { processAll = true; shreddedWriter.processDocument(object.document); }
               shreddedWriter.processTuple(object.length);
               last = object;
            }           
                 
            public void close() throws IOException {
                shreddedWriter.close();
            }
            
            public Class<FieldLengthData> getInputClass() {
                return FieldLengthData.class;
            }
        } 
        public ReaderSource<FieldLengthData> orderedCombiner(Collection<TypeReader<FieldLengthData>> readers, boolean closeOnExit) {
            ArrayList<ShreddedReader> shreddedReaders = new ArrayList();
            
            for (TypeReader<FieldLengthData> reader : readers) {
                shreddedReaders.add((ShreddedReader)reader);
            }
            
            return new ShreddedCombiner(shreddedReaders, closeOnExit);
        }                  
        public FieldLengthData clone(FieldLengthData object) {
            FieldLengthData result = new FieldLengthData();
            if (object == null) return result;
            result.field = object.field; 
            result.document = object.document; 
            result.length = object.length; 
            return result;
        }                 
        public Class<FieldLengthData> getOrderedClass() {
            return FieldLengthData.class;
        }                           
        public String[] getOrderSpec() {
            return new String[] {"+field", "+document"};
        }

        public static String[] getSpec() {
            return new String[] {"+field", "+document"};
        }
        public static String getSpecString() {
            return "+field +document";
        }
                           
        public interface ShreddedProcessor extends Step {
            public void processField(byte[] field) throws IOException;
            public void processDocument(int document) throws IOException;
            public void processTuple(int length) throws IOException;
            public void close() throws IOException;
        }    
        public interface ShreddedSource extends Step {
        }                                              
        
        public static class ShreddedWriter implements ShreddedProcessor {
            ArrayOutput output;
            ShreddedBuffer buffer = new ShreddedBuffer();
            byte[] lastField;
            int lastDocument;
            boolean lastFlush = false;
            
            public ShreddedWriter(ArrayOutput output) {
                this.output = output;
            }                        
            
            public void close() throws IOException {
                flush();
            }
            
            public void processField(byte[] field) {
                lastField = field;
                buffer.processField(field);
            }
            public void processDocument(int document) {
                lastDocument = document;
                buffer.processDocument(document);
            }
            public final void processTuple(int length) throws IOException {
                if (lastFlush) {
                    if(buffer.fields.size() == 0) buffer.processField(lastField);
                    if(buffer.documents.size() == 0) buffer.processDocument(lastDocument);
                    lastFlush = false;
                }
                buffer.processTuple(length);
                if (buffer.isFull())
                    flush();
            }
            public final void flushTuples(int pauseIndex) throws IOException {
                
                while (buffer.getReadIndex() < pauseIndex) {
                           
                    output.writeInt(buffer.getLength());
                    buffer.incrementTuple();
                }
            }  
            public final void flushField(int pauseIndex) throws IOException {
                while (buffer.getReadIndex() < pauseIndex) {
                    int nextPause = buffer.getFieldEndIndex();
                    int count = nextPause - buffer.getReadIndex();
                    
                    output.writeBytes(buffer.getField());
                    output.writeInt(count);
                    buffer.incrementField();
                      
                    flushDocument(nextPause);
                    assert nextPause == buffer.getReadIndex();
                }
            }
            public final void flushDocument(int pauseIndex) throws IOException {
                while (buffer.getReadIndex() < pauseIndex) {
                    int nextPause = buffer.getDocumentEndIndex();
                    int count = nextPause - buffer.getReadIndex();
                    
                    output.writeInt(buffer.getDocument());
                    output.writeInt(count);
                    buffer.incrementDocument();
                      
                    flushTuples(nextPause);
                    assert nextPause == buffer.getReadIndex();
                }
            }
            public void flush() throws IOException { 
                flushField(buffer.getWriteIndex());
                buffer.reset(); 
                lastFlush = true;
            }                           
        }
        public static class ShreddedBuffer {
            ArrayList<byte[]> fields = new ArrayList();
            TIntArrayList documents = new TIntArrayList();
            TIntArrayList fieldTupleIdx = new TIntArrayList();
            TIntArrayList documentTupleIdx = new TIntArrayList();
            int fieldReadIdx = 0;
            int documentReadIdx = 0;
                            
            int[] lengths;
            int writeTupleIndex = 0;
            int readTupleIndex = 0;
            int batchSize;

            public ShreddedBuffer(int batchSize) {
                this.batchSize = batchSize;

                lengths = new int[batchSize];
            }                              

            public ShreddedBuffer() {    
                this(10000);
            }                                                                                                                    
            
            public void processField(byte[] field) {
                fields.add(field);
                fieldTupleIdx.add(writeTupleIndex);
            }                                      
            public void processDocument(int document) {
                documents.add(document);
                documentTupleIdx.add(writeTupleIndex);
            }                                      
            public void processTuple(int length) {
                assert fields.size() > 0;
                assert documents.size() > 0;
                lengths[writeTupleIndex] = length;
                writeTupleIndex++;
            }
            public void resetData() {
                fields.clear();
                documents.clear();
                fieldTupleIdx.clear();
                documentTupleIdx.clear();
                writeTupleIndex = 0;
            }                  
                                 
            public void resetRead() {
                readTupleIndex = 0;
                fieldReadIdx = 0;
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
            public void incrementField() {
                fieldReadIdx++;  
            }                                                                                              

            public void autoIncrementField() {
                while (readTupleIndex >= getFieldEndIndex() && readTupleIndex < writeTupleIndex)
                    fieldReadIdx++;
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
            public int getFieldEndIndex() {
                if ((fieldReadIdx+1) >= fieldTupleIdx.size())
                    return writeTupleIndex;
                return fieldTupleIdx.get(fieldReadIdx+1);
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
            public byte[] getField() {
                assert readTupleIndex < writeTupleIndex;
                assert fieldReadIdx < fields.size();
                
                return fields.get(fieldReadIdx);
            }
            public int getDocument() {
                assert readTupleIndex < writeTupleIndex;
                assert documentReadIdx < documents.size();
                
                return documents.get(documentReadIdx);
            }
            public int getLength() {
                assert readTupleIndex < writeTupleIndex;
                return lengths[readTupleIndex];
            }                                         
            public void copyTuples(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                   output.processTuple(getLength());
                   incrementTuple();
                }
            }                                                                           
            public void copyUntilIndexField(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                    output.processField(getField());
                    assert getFieldEndIndex() <= endIndex;
                    copyUntilIndexDocument(getFieldEndIndex(), output);
                    incrementField();
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
            public void copyUntilField(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                while (!isAtEnd()) {
                    if (other != null) {   
                        assert !other.isAtEnd();
                        int c = + Utility.compare(getField(), other.getField());
                    
                        if (c > 0) {
                            break;   
                        }
                        
                        output.processField(getField());
                                      
                        if (c < 0) {
                            copyUntilIndexDocument(getFieldEndIndex(), output);
                        } else if (c == 0) {
                            copyUntilDocument(other, output);
                            autoIncrementField();
                            break;
                        }
                    } else {
                        output.processField(getField());
                        copyUntilIndexDocument(getFieldEndIndex(), output);
                    }
                    incrementField();  
                    
               
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
                    
                    if (getFieldEndIndex() <= readTupleIndex)
                        break;   
                }
            }
            public void copyUntil(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                copyUntilField(other, output);
            }
            
        }                         
        public static class ShreddedCombiner implements ReaderSource<FieldLengthData>, ShreddedSource {   
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
                } else if (processor instanceof FieldLengthData.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((FieldLengthData.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<FieldLengthData>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<FieldLengthData> getOutputClass() {
                return FieldLengthData.class;
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

            public FieldLengthData read() throws IOException {
                if (uninitialized)
                    initialize();

                FieldLengthData result = null;

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
        public static class ShreddedReader implements Step, Comparable<ShreddedReader>, TypeReader<FieldLengthData>, ShreddedSource {      
            public ShreddedProcessor processor;
            ShreddedBuffer buffer;
            FieldLengthData last = new FieldLengthData();         
            long updateFieldCount = -1;
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
                    result = + Utility.compare(buffer.getField(), otherBuffer.getField());
                    if(result != 0) break;
                    result = + Utility.compare(buffer.getDocument(), otherBuffer.getDocument());
                    if(result != 0) break;
                } while (false);                                             
                
                return result;
            }
            
            public final ShreddedBuffer getBuffer() {
                return buffer;
            }                
            
            public final FieldLengthData read() throws IOException {
                if (buffer.isAtEnd()) {
                    fill();             
                
                    if (buffer.isAtEnd()) {
                        return null;
                    }
                }
                      
                assert !buffer.isAtEnd();
                FieldLengthData result = new FieldLengthData();
                
                result.field = buffer.getField();
                result.document = buffer.getDocument();
                result.length = buffer.getLength();
                
                buffer.incrementTuple();
                buffer.autoIncrementField();
                buffer.autoIncrementDocument();
                
                return result;
            }           
            
            public final void fill() throws IOException {
                try {   
                    buffer.reset();
                    
                    if (tupleCount != 0) {
                                                      
                        if(updateFieldCount - tupleCount > 0) {
                            buffer.fields.add(last.field);
                            buffer.fieldTupleIdx.add((int) (updateFieldCount - tupleCount));
                        }                              
                        if(updateDocumentCount - tupleCount > 0) {
                            buffer.documents.add(last.document);
                            buffer.documentTupleIdx.add((int) (updateDocumentCount - tupleCount));
                        }
                        bufferStartCount = tupleCount;
                    }
                    
                    while (!buffer.isFull()) {
                        updateDocument();
                        buffer.processTuple(input.readInt());
                        tupleCount++;
                    }
                } catch(EOFException e) {}
            }

            public final void updateField() throws IOException {
                if (updateFieldCount > tupleCount)
                    return;
                     
                last.field = input.readBytes();
                updateFieldCount = tupleCount + input.readInt();
                                      
                buffer.processField(last.field);
            }
            public final void updateDocument() throws IOException {
                if (updateDocumentCount > tupleCount)
                    return;
                     
                updateField();
                last.document = input.readInt();
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
                } else if (processor instanceof FieldLengthData.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((FieldLengthData.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<FieldLengthData>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<FieldLengthData> getOutputClass() {
                return FieldLengthData.class;
            }                
        }
        
        public static class DuplicateEliminator implements ShreddedProcessor {
            public ShreddedProcessor processor;
            FieldLengthData last = new FieldLengthData();
            boolean fieldProcess = true;
            boolean documentProcess = true;
                                           
            public DuplicateEliminator() {}
            public DuplicateEliminator(ShreddedProcessor processor) {
                this.processor = processor;
            }
            
            public void setShreddedProcessor(ShreddedProcessor processor) {
                this.processor = processor;
            }

            public void processField(byte[] field) throws IOException {  
                if (fieldProcess || Utility.compare(field, last.field) != 0) {
                    last.field = field;
                    processor.processField(field);
            resetDocument();
                    fieldProcess = false;
                }
            }
            public void processDocument(int document) throws IOException {  
                if (documentProcess || Utility.compare(document, last.document) != 0) {
                    last.document = document;
                    processor.processDocument(document);
                    documentProcess = false;
                }
            }  
            
            public void resetField() {
                 fieldProcess = true;
            resetDocument();
            }                                                
            public void resetDocument() {
                 documentProcess = true;
            }                                                
                               
            public void processTuple(int length) throws IOException {
                processor.processTuple(length);
            } 
            
            public void close() throws IOException {
                processor.close();
            }                    
        }
        public static class TupleUnshredder implements ShreddedProcessor {
            FieldLengthData last = new FieldLengthData();
            public org.lemurproject.galago.tupleflow.Processor<FieldLengthData> processor;                               
            
            public TupleUnshredder(FieldLengthData.Processor processor) {
                this.processor = processor;
            }         
            
            public TupleUnshredder(org.lemurproject.galago.tupleflow.Processor<FieldLengthData> processor) {
                this.processor = processor;
            }
            
            public FieldLengthData clone(FieldLengthData object) {
                FieldLengthData result = new FieldLengthData();
                if (object == null) return result;
                result.field = object.field; 
                result.document = object.document; 
                result.length = object.length; 
                return result;
            }                 
            
            public void processField(byte[] field) throws IOException {
                last.field = field;
            }   
                
            public void processDocument(int document) throws IOException {
                last.document = document;
            }   
                
            
            public void processTuple(int length) throws IOException {
                last.length = length;
                processor.process(clone(last));
            }               
            
            public void close() throws IOException {
                processor.close();
            }
        }     
        public static class TupleShredder implements Processor {
            FieldLengthData last = null;
            public ShreddedProcessor processor;
            
            public TupleShredder(ShreddedProcessor processor) {
                this.processor = processor;
            }                              
            
            public FieldLengthData clone(FieldLengthData object) {
                FieldLengthData result = new FieldLengthData();
                if (object == null) return result;
                result.field = object.field; 
                result.document = object.document; 
                result.length = object.length; 
                return result;
            }                 
            
            public void process(FieldLengthData object) throws IOException {                                                                                                                                                   
                boolean processAll = false;
                if(last == null || Utility.compare(last.field, object.field) != 0 || processAll) { processor.processField(object.field); processAll = true; }
                if(last == null || Utility.compare(last.document, object.document) != 0 || processAll) { processor.processDocument(object.document); processAll = true; }
                processor.processTuple(object.length);                                         
                last = object;
            }
                          
            public Class<FieldLengthData> getInputClass() {
                return FieldLengthData.class;
            }
            
            public void close() throws IOException {
                processor.close();
            }                     
        }
    } 
}    