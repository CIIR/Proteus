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


public class NumberedDocumentData implements Type<NumberedDocumentData> {
    public String identifier;
    public String fieldList;
    public String url;
    public int number;
    public int textLength; 
    
    public NumberedDocumentData() {}
    public NumberedDocumentData(String identifier, String fieldList, String url, int number, int textLength) {
        this.identifier = identifier;
        this.fieldList = fieldList;
        this.url = url;
        this.number = number;
        this.textLength = textLength;
    }  
    
    public String toString() {
            return String.format("%s,%s,%s,%d,%d",
                                   identifier, fieldList, url, number, textLength);
    } 

    public Order<NumberedDocumentData> getOrder(String... spec) {
        if (Arrays.equals(spec, new String[] { "+number" })) {
            return new NumberOrder();
        }
        if (Arrays.equals(spec, new String[] { "+identifier" })) {
            return new IdentifierOrder();
        }
        if (Arrays.equals(spec, new String[] { "+url" })) {
            return new UrlOrder();
        }
        return null;
    } 
      
    public interface Processor extends Step, org.lemurproject.galago.tupleflow.Processor<NumberedDocumentData> {
        public void process(NumberedDocumentData object) throws IOException;
        public void close() throws IOException;
    }                        
    public interface Source extends Step {
    }
    public static class NumberOrder implements Order<NumberedDocumentData> {
        public int hash(NumberedDocumentData object) {
            int h = 0;
            h += Utility.hash(object.number);
            return h;
        } 
        public Comparator<NumberedDocumentData> greaterThan() {
            return new Comparator<NumberedDocumentData>() {
                public int compare(NumberedDocumentData one, NumberedDocumentData two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.number, two.number);
                        if(result != 0) break;
                    } while (false);
                    return -result;
                }
            };
        }     
        public Comparator<NumberedDocumentData> lessThan() {
            return new Comparator<NumberedDocumentData>() {
                public int compare(NumberedDocumentData one, NumberedDocumentData two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.number, two.number);
                        if(result != 0) break;
                    } while (false);
                    return result;
                }
            };
        }     
        public TypeReader<NumberedDocumentData> orderedReader(ArrayInput _input) {
            return new ShreddedReader(_input);
        }    

        public TypeReader<NumberedDocumentData> orderedReader(ArrayInput _input, int bufferSize) {
            return new ShreddedReader(_input, bufferSize);
        }    
        public OrderedWriter<NumberedDocumentData> orderedWriter(ArrayOutput _output) {
            ShreddedWriter w = new ShreddedWriter(_output);
            return new OrderedWriterClass(w); 
        }                                    
        public static class OrderedWriterClass extends OrderedWriter< NumberedDocumentData > {
            NumberedDocumentData last = null;
            ShreddedWriter shreddedWriter = null; 
            
            public OrderedWriterClass(ShreddedWriter s) {
                this.shreddedWriter = s;
            }
            
            public void process(NumberedDocumentData object) throws IOException {
               boolean processAll = false;
               if (processAll || last == null || 0 != Utility.compare(object.number, last.number)) { processAll = true; shreddedWriter.processNumber(object.number); }
               shreddedWriter.processTuple(object.identifier, object.fieldList, object.url, object.textLength);
               last = object;
            }           
                 
            public void close() throws IOException {
                shreddedWriter.close();
            }
            
            public Class<NumberedDocumentData> getInputClass() {
                return NumberedDocumentData.class;
            }
        } 
        public ReaderSource<NumberedDocumentData> orderedCombiner(Collection<TypeReader<NumberedDocumentData>> readers, boolean closeOnExit) {
            ArrayList<ShreddedReader> shreddedReaders = new ArrayList();
            
            for (TypeReader<NumberedDocumentData> reader : readers) {
                shreddedReaders.add((ShreddedReader)reader);
            }
            
            return new ShreddedCombiner(shreddedReaders, closeOnExit);
        }                  
        public NumberedDocumentData clone(NumberedDocumentData object) {
            NumberedDocumentData result = new NumberedDocumentData();
            if (object == null) return result;
            result.identifier = object.identifier; 
            result.fieldList = object.fieldList; 
            result.url = object.url; 
            result.number = object.number; 
            result.textLength = object.textLength; 
            return result;
        }                 
        public Class<NumberedDocumentData> getOrderedClass() {
            return NumberedDocumentData.class;
        }                           
        public String[] getOrderSpec() {
            return new String[] {"+number"};
        }

        public static String[] getSpec() {
            return new String[] {"+number"};
        }
        public static String getSpecString() {
            return "+number";
        }
                           
        public interface ShreddedProcessor extends Step {
            public void processNumber(int number) throws IOException;
            public void processTuple(String identifier, String fieldList, String url, int textLength) throws IOException;
            public void close() throws IOException;
        }    
        public interface ShreddedSource extends Step {
        }                                              
        
        public static class ShreddedWriter implements ShreddedProcessor {
            ArrayOutput output;
            ShreddedBuffer buffer = new ShreddedBuffer();
            int lastNumber;
            boolean lastFlush = false;
            
            public ShreddedWriter(ArrayOutput output) {
                this.output = output;
            }                        
            
            public void close() throws IOException {
                flush();
            }
            
            public void processNumber(int number) {
                lastNumber = number;
                buffer.processNumber(number);
            }
            public final void processTuple(String identifier, String fieldList, String url, int textLength) throws IOException {
                if (lastFlush) {
                    if(buffer.numbers.size() == 0) buffer.processNumber(lastNumber);
                    lastFlush = false;
                }
                buffer.processTuple(identifier, fieldList, url, textLength);
                if (buffer.isFull())
                    flush();
            }
            public final void flushTuples(int pauseIndex) throws IOException {
                
                while (buffer.getReadIndex() < pauseIndex) {
                           
                    output.writeString(buffer.getIdentifier());
                    output.writeString(buffer.getFieldList());
                    output.writeString(buffer.getUrl());
                    output.writeInt(buffer.getTextLength());
                    buffer.incrementTuple();
                }
            }  
            public final void flushNumber(int pauseIndex) throws IOException {
                while (buffer.getReadIndex() < pauseIndex) {
                    int nextPause = buffer.getNumberEndIndex();
                    int count = nextPause - buffer.getReadIndex();
                    
                    output.writeInt(buffer.getNumber());
                    output.writeInt(count);
                    buffer.incrementNumber();
                      
                    flushTuples(nextPause);
                    assert nextPause == buffer.getReadIndex();
                }
            }
            public void flush() throws IOException { 
                flushNumber(buffer.getWriteIndex());
                buffer.reset(); 
                lastFlush = true;
            }                           
        }
        public static class ShreddedBuffer {
            TIntArrayList numbers = new TIntArrayList();
            TIntArrayList numberTupleIdx = new TIntArrayList();
            int numberReadIdx = 0;
                            
            String[] identifiers;
            String[] fieldLists;
            String[] urls;
            int[] textLengths;
            int writeTupleIndex = 0;
            int readTupleIndex = 0;
            int batchSize;

            public ShreddedBuffer(int batchSize) {
                this.batchSize = batchSize;

                identifiers = new String[batchSize];
                fieldLists = new String[batchSize];
                urls = new String[batchSize];
                textLengths = new int[batchSize];
            }                              

            public ShreddedBuffer() {    
                this(10000);
            }                                                                                                                    
            
            public void processNumber(int number) {
                numbers.add(number);
                numberTupleIdx.add(writeTupleIndex);
            }                                      
            public void processTuple(String identifier, String fieldList, String url, int textLength) {
                assert numbers.size() > 0;
                identifiers[writeTupleIndex] = identifier;
                fieldLists[writeTupleIndex] = fieldList;
                urls[writeTupleIndex] = url;
                textLengths[writeTupleIndex] = textLength;
                writeTupleIndex++;
            }
            public void resetData() {
                numbers.clear();
                numberTupleIdx.clear();
                writeTupleIndex = 0;
            }                  
                                 
            public void resetRead() {
                readTupleIndex = 0;
                numberReadIdx = 0;
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
            public void incrementNumber() {
                numberReadIdx++;  
            }                                                                                              

            public void autoIncrementNumber() {
                while (readTupleIndex >= getNumberEndIndex() && readTupleIndex < writeTupleIndex)
                    numberReadIdx++;
            }                 
            public void incrementTuple() {
                readTupleIndex++;
            }                    
            public int getNumberEndIndex() {
                if ((numberReadIdx+1) >= numberTupleIdx.size())
                    return writeTupleIndex;
                return numberTupleIdx.get(numberReadIdx+1);
            }
            public int getReadIndex() {
                return readTupleIndex;
            }   

            public int getWriteIndex() {
                return writeTupleIndex;
            } 
            public int getNumber() {
                assert readTupleIndex < writeTupleIndex;
                assert numberReadIdx < numbers.size();
                
                return numbers.get(numberReadIdx);
            }
            public String getIdentifier() {
                assert readTupleIndex < writeTupleIndex;
                return identifiers[readTupleIndex];
            }                                         
            public String getFieldList() {
                assert readTupleIndex < writeTupleIndex;
                return fieldLists[readTupleIndex];
            }                                         
            public String getUrl() {
                assert readTupleIndex < writeTupleIndex;
                return urls[readTupleIndex];
            }                                         
            public int getTextLength() {
                assert readTupleIndex < writeTupleIndex;
                return textLengths[readTupleIndex];
            }                                         
            public void copyTuples(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                   output.processTuple(getIdentifier(), getFieldList(), getUrl(), getTextLength());
                   incrementTuple();
                }
            }                                                                           
            public void copyUntilIndexNumber(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                    output.processNumber(getNumber());
                    assert getNumberEndIndex() <= endIndex;
                    copyTuples(getNumberEndIndex(), output);
                    incrementNumber();
                }
            }  
            public void copyUntilNumber(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                while (!isAtEnd()) {
                    if (other != null) {   
                        assert !other.isAtEnd();
                        int c = + Utility.compare(getNumber(), other.getNumber());
                    
                        if (c > 0) {
                            break;   
                        }
                        
                        output.processNumber(getNumber());
                                      
                        copyTuples(getNumberEndIndex(), output);
                    } else {
                        output.processNumber(getNumber());
                        copyTuples(getNumberEndIndex(), output);
                    }
                    incrementNumber();  
                    
               
                }
            }
            public void copyUntil(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                copyUntilNumber(other, output);
            }
            
        }                         
        public static class ShreddedCombiner implements ReaderSource<NumberedDocumentData>, ShreddedSource {   
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
                } else if (processor instanceof NumberedDocumentData.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((NumberedDocumentData.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<NumberedDocumentData>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<NumberedDocumentData> getOutputClass() {
                return NumberedDocumentData.class;
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

            public NumberedDocumentData read() throws IOException {
                if (uninitialized)
                    initialize();

                NumberedDocumentData result = null;

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
        public static class ShreddedReader implements Step, Comparable<ShreddedReader>, TypeReader<NumberedDocumentData>, ShreddedSource {      
            public ShreddedProcessor processor;
            ShreddedBuffer buffer;
            NumberedDocumentData last = new NumberedDocumentData();         
            long updateNumberCount = -1;
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
                    result = + Utility.compare(buffer.getNumber(), otherBuffer.getNumber());
                    if(result != 0) break;
                } while (false);                                             
                
                return result;
            }
            
            public final ShreddedBuffer getBuffer() {
                return buffer;
            }                
            
            public final NumberedDocumentData read() throws IOException {
                if (buffer.isAtEnd()) {
                    fill();             
                
                    if (buffer.isAtEnd()) {
                        return null;
                    }
                }
                      
                assert !buffer.isAtEnd();
                NumberedDocumentData result = new NumberedDocumentData();
                
                result.number = buffer.getNumber();
                result.identifier = buffer.getIdentifier();
                result.fieldList = buffer.getFieldList();
                result.url = buffer.getUrl();
                result.textLength = buffer.getTextLength();
                
                buffer.incrementTuple();
                buffer.autoIncrementNumber();
                
                return result;
            }           
            
            public final void fill() throws IOException {
                try {   
                    buffer.reset();
                    
                    if (tupleCount != 0) {
                                                      
                        if(updateNumberCount - tupleCount > 0) {
                            buffer.numbers.add(last.number);
                            buffer.numberTupleIdx.add((int) (updateNumberCount - tupleCount));
                        }
                        bufferStartCount = tupleCount;
                    }
                    
                    while (!buffer.isFull()) {
                        updateNumber();
                        buffer.processTuple(input.readString(), input.readString(), input.readString(), input.readInt());
                        tupleCount++;
                    }
                } catch(EOFException e) {}
            }

            public final void updateNumber() throws IOException {
                if (updateNumberCount > tupleCount)
                    return;
                     
                last.number = input.readInt();
                updateNumberCount = tupleCount + input.readInt();
                                      
                buffer.processNumber(last.number);
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
                } else if (processor instanceof NumberedDocumentData.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((NumberedDocumentData.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<NumberedDocumentData>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<NumberedDocumentData> getOutputClass() {
                return NumberedDocumentData.class;
            }                
        }
        
        public static class DuplicateEliminator implements ShreddedProcessor {
            public ShreddedProcessor processor;
            NumberedDocumentData last = new NumberedDocumentData();
            boolean numberProcess = true;
                                           
            public DuplicateEliminator() {}
            public DuplicateEliminator(ShreddedProcessor processor) {
                this.processor = processor;
            }
            
            public void setShreddedProcessor(ShreddedProcessor processor) {
                this.processor = processor;
            }

            public void processNumber(int number) throws IOException {  
                if (numberProcess || Utility.compare(number, last.number) != 0) {
                    last.number = number;
                    processor.processNumber(number);
                    numberProcess = false;
                }
            }  
            
            public void resetNumber() {
                 numberProcess = true;
            }                                                
                               
            public void processTuple(String identifier, String fieldList, String url, int textLength) throws IOException {
                processor.processTuple(identifier, fieldList, url, textLength);
            } 
            
            public void close() throws IOException {
                processor.close();
            }                    
        }
        public static class TupleUnshredder implements ShreddedProcessor {
            NumberedDocumentData last = new NumberedDocumentData();
            public org.lemurproject.galago.tupleflow.Processor<NumberedDocumentData> processor;                               
            
            public TupleUnshredder(NumberedDocumentData.Processor processor) {
                this.processor = processor;
            }         
            
            public TupleUnshredder(org.lemurproject.galago.tupleflow.Processor<NumberedDocumentData> processor) {
                this.processor = processor;
            }
            
            public NumberedDocumentData clone(NumberedDocumentData object) {
                NumberedDocumentData result = new NumberedDocumentData();
                if (object == null) return result;
                result.identifier = object.identifier; 
                result.fieldList = object.fieldList; 
                result.url = object.url; 
                result.number = object.number; 
                result.textLength = object.textLength; 
                return result;
            }                 
            
            public void processNumber(int number) throws IOException {
                last.number = number;
            }   
                
            
            public void processTuple(String identifier, String fieldList, String url, int textLength) throws IOException {
                last.identifier = identifier;
                last.fieldList = fieldList;
                last.url = url;
                last.textLength = textLength;
                processor.process(clone(last));
            }               
            
            public void close() throws IOException {
                processor.close();
            }
        }     
        public static class TupleShredder implements Processor {
            NumberedDocumentData last = null;
            public ShreddedProcessor processor;
            
            public TupleShredder(ShreddedProcessor processor) {
                this.processor = processor;
            }                              
            
            public NumberedDocumentData clone(NumberedDocumentData object) {
                NumberedDocumentData result = new NumberedDocumentData();
                if (object == null) return result;
                result.identifier = object.identifier; 
                result.fieldList = object.fieldList; 
                result.url = object.url; 
                result.number = object.number; 
                result.textLength = object.textLength; 
                return result;
            }                 
            
            public void process(NumberedDocumentData object) throws IOException {                                                                                                                                                   
                boolean processAll = false;
                if(last == null || Utility.compare(last.number, object.number) != 0 || processAll) { processor.processNumber(object.number); processAll = true; }
                processor.processTuple(object.identifier, object.fieldList, object.url, object.textLength);                                         
                last = object;
            }
                          
            public Class<NumberedDocumentData> getInputClass() {
                return NumberedDocumentData.class;
            }
            
            public void close() throws IOException {
                processor.close();
            }                     
        }
    } 
    public static class IdentifierOrder implements Order<NumberedDocumentData> {
        public int hash(NumberedDocumentData object) {
            int h = 0;
            h += Utility.hash(object.identifier);
            return h;
        } 
        public Comparator<NumberedDocumentData> greaterThan() {
            return new Comparator<NumberedDocumentData>() {
                public int compare(NumberedDocumentData one, NumberedDocumentData two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.identifier, two.identifier);
                        if(result != 0) break;
                    } while (false);
                    return -result;
                }
            };
        }     
        public Comparator<NumberedDocumentData> lessThan() {
            return new Comparator<NumberedDocumentData>() {
                public int compare(NumberedDocumentData one, NumberedDocumentData two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.identifier, two.identifier);
                        if(result != 0) break;
                    } while (false);
                    return result;
                }
            };
        }     
        public TypeReader<NumberedDocumentData> orderedReader(ArrayInput _input) {
            return new ShreddedReader(_input);
        }    

        public TypeReader<NumberedDocumentData> orderedReader(ArrayInput _input, int bufferSize) {
            return new ShreddedReader(_input, bufferSize);
        }    
        public OrderedWriter<NumberedDocumentData> orderedWriter(ArrayOutput _output) {
            ShreddedWriter w = new ShreddedWriter(_output);
            return new OrderedWriterClass(w); 
        }                                    
        public static class OrderedWriterClass extends OrderedWriter< NumberedDocumentData > {
            NumberedDocumentData last = null;
            ShreddedWriter shreddedWriter = null; 
            
            public OrderedWriterClass(ShreddedWriter s) {
                this.shreddedWriter = s;
            }
            
            public void process(NumberedDocumentData object) throws IOException {
               boolean processAll = false;
               if (processAll || last == null || 0 != Utility.compare(object.identifier, last.identifier)) { processAll = true; shreddedWriter.processIdentifier(object.identifier); }
               shreddedWriter.processTuple(object.fieldList, object.url, object.number, object.textLength);
               last = object;
            }           
                 
            public void close() throws IOException {
                shreddedWriter.close();
            }
            
            public Class<NumberedDocumentData> getInputClass() {
                return NumberedDocumentData.class;
            }
        } 
        public ReaderSource<NumberedDocumentData> orderedCombiner(Collection<TypeReader<NumberedDocumentData>> readers, boolean closeOnExit) {
            ArrayList<ShreddedReader> shreddedReaders = new ArrayList();
            
            for (TypeReader<NumberedDocumentData> reader : readers) {
                shreddedReaders.add((ShreddedReader)reader);
            }
            
            return new ShreddedCombiner(shreddedReaders, closeOnExit);
        }                  
        public NumberedDocumentData clone(NumberedDocumentData object) {
            NumberedDocumentData result = new NumberedDocumentData();
            if (object == null) return result;
            result.identifier = object.identifier; 
            result.fieldList = object.fieldList; 
            result.url = object.url; 
            result.number = object.number; 
            result.textLength = object.textLength; 
            return result;
        }                 
        public Class<NumberedDocumentData> getOrderedClass() {
            return NumberedDocumentData.class;
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
            public void processTuple(String fieldList, String url, int number, int textLength) throws IOException;
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
            public final void processTuple(String fieldList, String url, int number, int textLength) throws IOException {
                if (lastFlush) {
                    if(buffer.identifiers.size() == 0) buffer.processIdentifier(lastIdentifier);
                    lastFlush = false;
                }
                buffer.processTuple(fieldList, url, number, textLength);
                if (buffer.isFull())
                    flush();
            }
            public final void flushTuples(int pauseIndex) throws IOException {
                
                while (buffer.getReadIndex() < pauseIndex) {
                           
                    output.writeString(buffer.getFieldList());
                    output.writeString(buffer.getUrl());
                    output.writeInt(buffer.getNumber());
                    output.writeInt(buffer.getTextLength());
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
                            
            String[] fieldLists;
            String[] urls;
            int[] numbers;
            int[] textLengths;
            int writeTupleIndex = 0;
            int readTupleIndex = 0;
            int batchSize;

            public ShreddedBuffer(int batchSize) {
                this.batchSize = batchSize;

                fieldLists = new String[batchSize];
                urls = new String[batchSize];
                numbers = new int[batchSize];
                textLengths = new int[batchSize];
            }                              

            public ShreddedBuffer() {    
                this(10000);
            }                                                                                                                    
            
            public void processIdentifier(String identifier) {
                identifiers.add(identifier);
                identifierTupleIdx.add(writeTupleIndex);
            }                                      
            public void processTuple(String fieldList, String url, int number, int textLength) {
                assert identifiers.size() > 0;
                fieldLists[writeTupleIndex] = fieldList;
                urls[writeTupleIndex] = url;
                numbers[writeTupleIndex] = number;
                textLengths[writeTupleIndex] = textLength;
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
            public String getFieldList() {
                assert readTupleIndex < writeTupleIndex;
                return fieldLists[readTupleIndex];
            }                                         
            public String getUrl() {
                assert readTupleIndex < writeTupleIndex;
                return urls[readTupleIndex];
            }                                         
            public int getNumber() {
                assert readTupleIndex < writeTupleIndex;
                return numbers[readTupleIndex];
            }                                         
            public int getTextLength() {
                assert readTupleIndex < writeTupleIndex;
                return textLengths[readTupleIndex];
            }                                         
            public void copyTuples(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                   output.processTuple(getFieldList(), getUrl(), getNumber(), getTextLength());
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
        public static class ShreddedCombiner implements ReaderSource<NumberedDocumentData>, ShreddedSource {   
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
                } else if (processor instanceof NumberedDocumentData.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((NumberedDocumentData.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<NumberedDocumentData>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<NumberedDocumentData> getOutputClass() {
                return NumberedDocumentData.class;
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

            public NumberedDocumentData read() throws IOException {
                if (uninitialized)
                    initialize();

                NumberedDocumentData result = null;

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
        public static class ShreddedReader implements Step, Comparable<ShreddedReader>, TypeReader<NumberedDocumentData>, ShreddedSource {      
            public ShreddedProcessor processor;
            ShreddedBuffer buffer;
            NumberedDocumentData last = new NumberedDocumentData();         
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
            
            public final NumberedDocumentData read() throws IOException {
                if (buffer.isAtEnd()) {
                    fill();             
                
                    if (buffer.isAtEnd()) {
                        return null;
                    }
                }
                      
                assert !buffer.isAtEnd();
                NumberedDocumentData result = new NumberedDocumentData();
                
                result.identifier = buffer.getIdentifier();
                result.fieldList = buffer.getFieldList();
                result.url = buffer.getUrl();
                result.number = buffer.getNumber();
                result.textLength = buffer.getTextLength();
                
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
                        buffer.processTuple(input.readString(), input.readString(), input.readInt(), input.readInt());
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
                } else if (processor instanceof NumberedDocumentData.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((NumberedDocumentData.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<NumberedDocumentData>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<NumberedDocumentData> getOutputClass() {
                return NumberedDocumentData.class;
            }                
        }
        
        public static class DuplicateEliminator implements ShreddedProcessor {
            public ShreddedProcessor processor;
            NumberedDocumentData last = new NumberedDocumentData();
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
                               
            public void processTuple(String fieldList, String url, int number, int textLength) throws IOException {
                processor.processTuple(fieldList, url, number, textLength);
            } 
            
            public void close() throws IOException {
                processor.close();
            }                    
        }
        public static class TupleUnshredder implements ShreddedProcessor {
            NumberedDocumentData last = new NumberedDocumentData();
            public org.lemurproject.galago.tupleflow.Processor<NumberedDocumentData> processor;                               
            
            public TupleUnshredder(NumberedDocumentData.Processor processor) {
                this.processor = processor;
            }         
            
            public TupleUnshredder(org.lemurproject.galago.tupleflow.Processor<NumberedDocumentData> processor) {
                this.processor = processor;
            }
            
            public NumberedDocumentData clone(NumberedDocumentData object) {
                NumberedDocumentData result = new NumberedDocumentData();
                if (object == null) return result;
                result.identifier = object.identifier; 
                result.fieldList = object.fieldList; 
                result.url = object.url; 
                result.number = object.number; 
                result.textLength = object.textLength; 
                return result;
            }                 
            
            public void processIdentifier(String identifier) throws IOException {
                last.identifier = identifier;
            }   
                
            
            public void processTuple(String fieldList, String url, int number, int textLength) throws IOException {
                last.fieldList = fieldList;
                last.url = url;
                last.number = number;
                last.textLength = textLength;
                processor.process(clone(last));
            }               
            
            public void close() throws IOException {
                processor.close();
            }
        }     
        public static class TupleShredder implements Processor {
            NumberedDocumentData last = null;
            public ShreddedProcessor processor;
            
            public TupleShredder(ShreddedProcessor processor) {
                this.processor = processor;
            }                              
            
            public NumberedDocumentData clone(NumberedDocumentData object) {
                NumberedDocumentData result = new NumberedDocumentData();
                if (object == null) return result;
                result.identifier = object.identifier; 
                result.fieldList = object.fieldList; 
                result.url = object.url; 
                result.number = object.number; 
                result.textLength = object.textLength; 
                return result;
            }                 
            
            public void process(NumberedDocumentData object) throws IOException {                                                                                                                                                   
                boolean processAll = false;
                if(last == null || Utility.compare(last.identifier, object.identifier) != 0 || processAll) { processor.processIdentifier(object.identifier); processAll = true; }
                processor.processTuple(object.fieldList, object.url, object.number, object.textLength);                                         
                last = object;
            }
                          
            public Class<NumberedDocumentData> getInputClass() {
                return NumberedDocumentData.class;
            }
            
            public void close() throws IOException {
                processor.close();
            }                     
        }
    } 
    public static class UrlOrder implements Order<NumberedDocumentData> {
        public int hash(NumberedDocumentData object) {
            int h = 0;
            h += Utility.hash(object.url);
            return h;
        } 
        public Comparator<NumberedDocumentData> greaterThan() {
            return new Comparator<NumberedDocumentData>() {
                public int compare(NumberedDocumentData one, NumberedDocumentData two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.url, two.url);
                        if(result != 0) break;
                    } while (false);
                    return -result;
                }
            };
        }     
        public Comparator<NumberedDocumentData> lessThan() {
            return new Comparator<NumberedDocumentData>() {
                public int compare(NumberedDocumentData one, NumberedDocumentData two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.url, two.url);
                        if(result != 0) break;
                    } while (false);
                    return result;
                }
            };
        }     
        public TypeReader<NumberedDocumentData> orderedReader(ArrayInput _input) {
            return new ShreddedReader(_input);
        }    

        public TypeReader<NumberedDocumentData> orderedReader(ArrayInput _input, int bufferSize) {
            return new ShreddedReader(_input, bufferSize);
        }    
        public OrderedWriter<NumberedDocumentData> orderedWriter(ArrayOutput _output) {
            ShreddedWriter w = new ShreddedWriter(_output);
            return new OrderedWriterClass(w); 
        }                                    
        public static class OrderedWriterClass extends OrderedWriter< NumberedDocumentData > {
            NumberedDocumentData last = null;
            ShreddedWriter shreddedWriter = null; 
            
            public OrderedWriterClass(ShreddedWriter s) {
                this.shreddedWriter = s;
            }
            
            public void process(NumberedDocumentData object) throws IOException {
               boolean processAll = false;
               if (processAll || last == null || 0 != Utility.compare(object.url, last.url)) { processAll = true; shreddedWriter.processUrl(object.url); }
               shreddedWriter.processTuple(object.identifier, object.fieldList, object.number, object.textLength);
               last = object;
            }           
                 
            public void close() throws IOException {
                shreddedWriter.close();
            }
            
            public Class<NumberedDocumentData> getInputClass() {
                return NumberedDocumentData.class;
            }
        } 
        public ReaderSource<NumberedDocumentData> orderedCombiner(Collection<TypeReader<NumberedDocumentData>> readers, boolean closeOnExit) {
            ArrayList<ShreddedReader> shreddedReaders = new ArrayList();
            
            for (TypeReader<NumberedDocumentData> reader : readers) {
                shreddedReaders.add((ShreddedReader)reader);
            }
            
            return new ShreddedCombiner(shreddedReaders, closeOnExit);
        }                  
        public NumberedDocumentData clone(NumberedDocumentData object) {
            NumberedDocumentData result = new NumberedDocumentData();
            if (object == null) return result;
            result.identifier = object.identifier; 
            result.fieldList = object.fieldList; 
            result.url = object.url; 
            result.number = object.number; 
            result.textLength = object.textLength; 
            return result;
        }                 
        public Class<NumberedDocumentData> getOrderedClass() {
            return NumberedDocumentData.class;
        }                           
        public String[] getOrderSpec() {
            return new String[] {"+url"};
        }

        public static String[] getSpec() {
            return new String[] {"+url"};
        }
        public static String getSpecString() {
            return "+url";
        }
                           
        public interface ShreddedProcessor extends Step {
            public void processUrl(String url) throws IOException;
            public void processTuple(String identifier, String fieldList, int number, int textLength) throws IOException;
            public void close() throws IOException;
        }    
        public interface ShreddedSource extends Step {
        }                                              
        
        public static class ShreddedWriter implements ShreddedProcessor {
            ArrayOutput output;
            ShreddedBuffer buffer = new ShreddedBuffer();
            String lastUrl;
            boolean lastFlush = false;
            
            public ShreddedWriter(ArrayOutput output) {
                this.output = output;
            }                        
            
            public void close() throws IOException {
                flush();
            }
            
            public void processUrl(String url) {
                lastUrl = url;
                buffer.processUrl(url);
            }
            public final void processTuple(String identifier, String fieldList, int number, int textLength) throws IOException {
                if (lastFlush) {
                    if(buffer.urls.size() == 0) buffer.processUrl(lastUrl);
                    lastFlush = false;
                }
                buffer.processTuple(identifier, fieldList, number, textLength);
                if (buffer.isFull())
                    flush();
            }
            public final void flushTuples(int pauseIndex) throws IOException {
                
                while (buffer.getReadIndex() < pauseIndex) {
                           
                    output.writeString(buffer.getIdentifier());
                    output.writeString(buffer.getFieldList());
                    output.writeInt(buffer.getNumber());
                    output.writeInt(buffer.getTextLength());
                    buffer.incrementTuple();
                }
            }  
            public final void flushUrl(int pauseIndex) throws IOException {
                while (buffer.getReadIndex() < pauseIndex) {
                    int nextPause = buffer.getUrlEndIndex();
                    int count = nextPause - buffer.getReadIndex();
                    
                    output.writeString(buffer.getUrl());
                    output.writeInt(count);
                    buffer.incrementUrl();
                      
                    flushTuples(nextPause);
                    assert nextPause == buffer.getReadIndex();
                }
            }
            public void flush() throws IOException { 
                flushUrl(buffer.getWriteIndex());
                buffer.reset(); 
                lastFlush = true;
            }                           
        }
        public static class ShreddedBuffer {
            ArrayList<String> urls = new ArrayList();
            TIntArrayList urlTupleIdx = new TIntArrayList();
            int urlReadIdx = 0;
                            
            String[] identifiers;
            String[] fieldLists;
            int[] numbers;
            int[] textLengths;
            int writeTupleIndex = 0;
            int readTupleIndex = 0;
            int batchSize;

            public ShreddedBuffer(int batchSize) {
                this.batchSize = batchSize;

                identifiers = new String[batchSize];
                fieldLists = new String[batchSize];
                numbers = new int[batchSize];
                textLengths = new int[batchSize];
            }                              

            public ShreddedBuffer() {    
                this(10000);
            }                                                                                                                    
            
            public void processUrl(String url) {
                urls.add(url);
                urlTupleIdx.add(writeTupleIndex);
            }                                      
            public void processTuple(String identifier, String fieldList, int number, int textLength) {
                assert urls.size() > 0;
                identifiers[writeTupleIndex] = identifier;
                fieldLists[writeTupleIndex] = fieldList;
                numbers[writeTupleIndex] = number;
                textLengths[writeTupleIndex] = textLength;
                writeTupleIndex++;
            }
            public void resetData() {
                urls.clear();
                urlTupleIdx.clear();
                writeTupleIndex = 0;
            }                  
                                 
            public void resetRead() {
                readTupleIndex = 0;
                urlReadIdx = 0;
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
            public void incrementUrl() {
                urlReadIdx++;  
            }                                                                                              

            public void autoIncrementUrl() {
                while (readTupleIndex >= getUrlEndIndex() && readTupleIndex < writeTupleIndex)
                    urlReadIdx++;
            }                 
            public void incrementTuple() {
                readTupleIndex++;
            }                    
            public int getUrlEndIndex() {
                if ((urlReadIdx+1) >= urlTupleIdx.size())
                    return writeTupleIndex;
                return urlTupleIdx.get(urlReadIdx+1);
            }
            public int getReadIndex() {
                return readTupleIndex;
            }   

            public int getWriteIndex() {
                return writeTupleIndex;
            } 
            public String getUrl() {
                assert readTupleIndex < writeTupleIndex;
                assert urlReadIdx < urls.size();
                
                return urls.get(urlReadIdx);
            }
            public String getIdentifier() {
                assert readTupleIndex < writeTupleIndex;
                return identifiers[readTupleIndex];
            }                                         
            public String getFieldList() {
                assert readTupleIndex < writeTupleIndex;
                return fieldLists[readTupleIndex];
            }                                         
            public int getNumber() {
                assert readTupleIndex < writeTupleIndex;
                return numbers[readTupleIndex];
            }                                         
            public int getTextLength() {
                assert readTupleIndex < writeTupleIndex;
                return textLengths[readTupleIndex];
            }                                         
            public void copyTuples(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                   output.processTuple(getIdentifier(), getFieldList(), getNumber(), getTextLength());
                   incrementTuple();
                }
            }                                                                           
            public void copyUntilIndexUrl(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                    output.processUrl(getUrl());
                    assert getUrlEndIndex() <= endIndex;
                    copyTuples(getUrlEndIndex(), output);
                    incrementUrl();
                }
            }  
            public void copyUntilUrl(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                while (!isAtEnd()) {
                    if (other != null) {   
                        assert !other.isAtEnd();
                        int c = + Utility.compare(getUrl(), other.getUrl());
                    
                        if (c > 0) {
                            break;   
                        }
                        
                        output.processUrl(getUrl());
                                      
                        copyTuples(getUrlEndIndex(), output);
                    } else {
                        output.processUrl(getUrl());
                        copyTuples(getUrlEndIndex(), output);
                    }
                    incrementUrl();  
                    
               
                }
            }
            public void copyUntil(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                copyUntilUrl(other, output);
            }
            
        }                         
        public static class ShreddedCombiner implements ReaderSource<NumberedDocumentData>, ShreddedSource {   
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
                } else if (processor instanceof NumberedDocumentData.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((NumberedDocumentData.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<NumberedDocumentData>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<NumberedDocumentData> getOutputClass() {
                return NumberedDocumentData.class;
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

            public NumberedDocumentData read() throws IOException {
                if (uninitialized)
                    initialize();

                NumberedDocumentData result = null;

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
        public static class ShreddedReader implements Step, Comparable<ShreddedReader>, TypeReader<NumberedDocumentData>, ShreddedSource {      
            public ShreddedProcessor processor;
            ShreddedBuffer buffer;
            NumberedDocumentData last = new NumberedDocumentData();         
            long updateUrlCount = -1;
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
                    result = + Utility.compare(buffer.getUrl(), otherBuffer.getUrl());
                    if(result != 0) break;
                } while (false);                                             
                
                return result;
            }
            
            public final ShreddedBuffer getBuffer() {
                return buffer;
            }                
            
            public final NumberedDocumentData read() throws IOException {
                if (buffer.isAtEnd()) {
                    fill();             
                
                    if (buffer.isAtEnd()) {
                        return null;
                    }
                }
                      
                assert !buffer.isAtEnd();
                NumberedDocumentData result = new NumberedDocumentData();
                
                result.url = buffer.getUrl();
                result.identifier = buffer.getIdentifier();
                result.fieldList = buffer.getFieldList();
                result.number = buffer.getNumber();
                result.textLength = buffer.getTextLength();
                
                buffer.incrementTuple();
                buffer.autoIncrementUrl();
                
                return result;
            }           
            
            public final void fill() throws IOException {
                try {   
                    buffer.reset();
                    
                    if (tupleCount != 0) {
                                                      
                        if(updateUrlCount - tupleCount > 0) {
                            buffer.urls.add(last.url);
                            buffer.urlTupleIdx.add((int) (updateUrlCount - tupleCount));
                        }
                        bufferStartCount = tupleCount;
                    }
                    
                    while (!buffer.isFull()) {
                        updateUrl();
                        buffer.processTuple(input.readString(), input.readString(), input.readInt(), input.readInt());
                        tupleCount++;
                    }
                } catch(EOFException e) {}
            }

            public final void updateUrl() throws IOException {
                if (updateUrlCount > tupleCount)
                    return;
                     
                last.url = input.readString();
                updateUrlCount = tupleCount + input.readInt();
                                      
                buffer.processUrl(last.url);
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
                } else if (processor instanceof NumberedDocumentData.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((NumberedDocumentData.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<NumberedDocumentData>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<NumberedDocumentData> getOutputClass() {
                return NumberedDocumentData.class;
            }                
        }
        
        public static class DuplicateEliminator implements ShreddedProcessor {
            public ShreddedProcessor processor;
            NumberedDocumentData last = new NumberedDocumentData();
            boolean urlProcess = true;
                                           
            public DuplicateEliminator() {}
            public DuplicateEliminator(ShreddedProcessor processor) {
                this.processor = processor;
            }
            
            public void setShreddedProcessor(ShreddedProcessor processor) {
                this.processor = processor;
            }

            public void processUrl(String url) throws IOException {  
                if (urlProcess || Utility.compare(url, last.url) != 0) {
                    last.url = url;
                    processor.processUrl(url);
                    urlProcess = false;
                }
            }  
            
            public void resetUrl() {
                 urlProcess = true;
            }                                                
                               
            public void processTuple(String identifier, String fieldList, int number, int textLength) throws IOException {
                processor.processTuple(identifier, fieldList, number, textLength);
            } 
            
            public void close() throws IOException {
                processor.close();
            }                    
        }
        public static class TupleUnshredder implements ShreddedProcessor {
            NumberedDocumentData last = new NumberedDocumentData();
            public org.lemurproject.galago.tupleflow.Processor<NumberedDocumentData> processor;                               
            
            public TupleUnshredder(NumberedDocumentData.Processor processor) {
                this.processor = processor;
            }         
            
            public TupleUnshredder(org.lemurproject.galago.tupleflow.Processor<NumberedDocumentData> processor) {
                this.processor = processor;
            }
            
            public NumberedDocumentData clone(NumberedDocumentData object) {
                NumberedDocumentData result = new NumberedDocumentData();
                if (object == null) return result;
                result.identifier = object.identifier; 
                result.fieldList = object.fieldList; 
                result.url = object.url; 
                result.number = object.number; 
                result.textLength = object.textLength; 
                return result;
            }                 
            
            public void processUrl(String url) throws IOException {
                last.url = url;
            }   
                
            
            public void processTuple(String identifier, String fieldList, int number, int textLength) throws IOException {
                last.identifier = identifier;
                last.fieldList = fieldList;
                last.number = number;
                last.textLength = textLength;
                processor.process(clone(last));
            }               
            
            public void close() throws IOException {
                processor.close();
            }
        }     
        public static class TupleShredder implements Processor {
            NumberedDocumentData last = null;
            public ShreddedProcessor processor;
            
            public TupleShredder(ShreddedProcessor processor) {
                this.processor = processor;
            }                              
            
            public NumberedDocumentData clone(NumberedDocumentData object) {
                NumberedDocumentData result = new NumberedDocumentData();
                if (object == null) return result;
                result.identifier = object.identifier; 
                result.fieldList = object.fieldList; 
                result.url = object.url; 
                result.number = object.number; 
                result.textLength = object.textLength; 
                return result;
            }                 
            
            public void process(NumberedDocumentData object) throws IOException {                                                                                                                                                   
                boolean processAll = false;
                if(last == null || Utility.compare(last.url, object.url) != 0 || processAll) { processor.processUrl(object.url); processAll = true; }
                processor.processTuple(object.identifier, object.fieldList, object.number, object.textLength);                                         
                last = object;
            }
                          
            public Class<NumberedDocumentData> getInputClass() {
                return NumberedDocumentData.class;
            }
            
            public void close() throws IOException {
                processor.close();
            }                     
        }
    } 
}    