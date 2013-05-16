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


public class NumberKeyValue implements Type<NumberKeyValue> {
    public long number;
    public byte[] key;
    public byte[] value; 
    
    public NumberKeyValue() {}
    public NumberKeyValue(long number, byte[] key, byte[] value) {
        this.number = number;
        this.key = key;
        this.value = value;
    }  
    
    public String toString() {
        try {
            return String.format("%d,%s,%s",
                                   number, new String(key, "UTF-8"), new String(value, "UTF-8"));
        } catch(UnsupportedEncodingException e) {
            throw new RuntimeException("Couldn't convert string to UTF-8.");
        }
    } 

    public Order<NumberKeyValue> getOrder(String... spec) {
        if (Arrays.equals(spec, new String[] { "+number" })) {
            return new NumberOrder();
        }
        return null;
    } 
      
    public interface Processor extends Step, org.lemurproject.galago.tupleflow.Processor<NumberKeyValue> {
        public void process(NumberKeyValue object) throws IOException;
        public void close() throws IOException;
    }                        
    public interface Source extends Step {
    }
    public static class NumberOrder implements Order<NumberKeyValue> {
        public int hash(NumberKeyValue object) {
            int h = 0;
            h += Utility.hash(object.number);
            return h;
        } 
        public Comparator<NumberKeyValue> greaterThan() {
            return new Comparator<NumberKeyValue>() {
                public int compare(NumberKeyValue one, NumberKeyValue two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.number, two.number);
                        if(result != 0) break;
                    } while (false);
                    return -result;
                }
            };
        }     
        public Comparator<NumberKeyValue> lessThan() {
            return new Comparator<NumberKeyValue>() {
                public int compare(NumberKeyValue one, NumberKeyValue two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.number, two.number);
                        if(result != 0) break;
                    } while (false);
                    return result;
                }
            };
        }     
        public TypeReader<NumberKeyValue> orderedReader(ArrayInput _input) {
            return new ShreddedReader(_input);
        }    

        public TypeReader<NumberKeyValue> orderedReader(ArrayInput _input, int bufferSize) {
            return new ShreddedReader(_input, bufferSize);
        }    
        public OrderedWriter<NumberKeyValue> orderedWriter(ArrayOutput _output) {
            ShreddedWriter w = new ShreddedWriter(_output);
            return new OrderedWriterClass(w); 
        }                                    
        public static class OrderedWriterClass extends OrderedWriter< NumberKeyValue > {
            NumberKeyValue last = null;
            ShreddedWriter shreddedWriter = null; 
            
            public OrderedWriterClass(ShreddedWriter s) {
                this.shreddedWriter = s;
            }
            
            public void process(NumberKeyValue object) throws IOException {
               boolean processAll = false;
               if (processAll || last == null || 0 != Utility.compare(object.number, last.number)) { processAll = true; shreddedWriter.processNumber(object.number); }
               shreddedWriter.processTuple(object.key, object.value);
               last = object;
            }           
                 
            public void close() throws IOException {
                shreddedWriter.close();
            }
            
            public Class<NumberKeyValue> getInputClass() {
                return NumberKeyValue.class;
            }
        } 
        public ReaderSource<NumberKeyValue> orderedCombiner(Collection<TypeReader<NumberKeyValue>> readers, boolean closeOnExit) {
            ArrayList<ShreddedReader> shreddedReaders = new ArrayList();
            
            for (TypeReader<NumberKeyValue> reader : readers) {
                shreddedReaders.add((ShreddedReader)reader);
            }
            
            return new ShreddedCombiner(shreddedReaders, closeOnExit);
        }                  
        public NumberKeyValue clone(NumberKeyValue object) {
            NumberKeyValue result = new NumberKeyValue();
            if (object == null) return result;
            result.number = object.number; 
            result.key = object.key; 
            result.value = object.value; 
            return result;
        }                 
        public Class<NumberKeyValue> getOrderedClass() {
            return NumberKeyValue.class;
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
            public void processNumber(long number) throws IOException;
            public void processTuple(byte[] key, byte[] value) throws IOException;
            public void close() throws IOException;
        }    
        public interface ShreddedSource extends Step {
        }                                              
        
        public static class ShreddedWriter implements ShreddedProcessor {
            ArrayOutput output;
            ShreddedBuffer buffer = new ShreddedBuffer();
            long lastNumber;
            boolean lastFlush = false;
            
            public ShreddedWriter(ArrayOutput output) {
                this.output = output;
            }                        
            
            public void close() throws IOException {
                flush();
            }
            
            public void processNumber(long number) {
                lastNumber = number;
                buffer.processNumber(number);
            }
            public final void processTuple(byte[] key, byte[] value) throws IOException {
                if (lastFlush) {
                    if(buffer.numbers.size() == 0) buffer.processNumber(lastNumber);
                    lastFlush = false;
                }
                buffer.processTuple(key, value);
                if (buffer.isFull())
                    flush();
            }
            public final void flushTuples(int pauseIndex) throws IOException {
                
                while (buffer.getReadIndex() < pauseIndex) {
                           
                    output.writeBytes(buffer.getKey());
                    output.writeBytes(buffer.getValue());
                    buffer.incrementTuple();
                }
            }  
            public final void flushNumber(int pauseIndex) throws IOException {
                while (buffer.getReadIndex() < pauseIndex) {
                    int nextPause = buffer.getNumberEndIndex();
                    int count = nextPause - buffer.getReadIndex();
                    
                    output.writeLong(buffer.getNumber());
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
            TLongArrayList numbers = new TLongArrayList();
            TIntArrayList numberTupleIdx = new TIntArrayList();
            int numberReadIdx = 0;
                            
            byte[][] keys;
            byte[][] values;
            int writeTupleIndex = 0;
            int readTupleIndex = 0;
            int batchSize;

            public ShreddedBuffer(int batchSize) {
                this.batchSize = batchSize;

                keys = new byte[batchSize][];
                values = new byte[batchSize][];
            }                              

            public ShreddedBuffer() {    
                this(10000);
            }                                                                                                                    
            
            public void processNumber(long number) {
                numbers.add(number);
                numberTupleIdx.add(writeTupleIndex);
            }                                      
            public void processTuple(byte[] key, byte[] value) {
                assert numbers.size() > 0;
                keys[writeTupleIndex] = key;
                values[writeTupleIndex] = value;
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
            public long getNumber() {
                assert readTupleIndex < writeTupleIndex;
                assert numberReadIdx < numbers.size();
                
                return numbers.get(numberReadIdx);
            }
            public byte[] getKey() {
                assert readTupleIndex < writeTupleIndex;
                return keys[readTupleIndex];
            }                                         
            public byte[] getValue() {
                assert readTupleIndex < writeTupleIndex;
                return values[readTupleIndex];
            }                                         
            public void copyTuples(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                   output.processTuple(getKey(), getValue());
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
        public static class ShreddedCombiner implements ReaderSource<NumberKeyValue>, ShreddedSource {   
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
                } else if (processor instanceof NumberKeyValue.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((NumberKeyValue.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<NumberKeyValue>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<NumberKeyValue> getOutputClass() {
                return NumberKeyValue.class;
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

            public NumberKeyValue read() throws IOException {
                if (uninitialized)
                    initialize();

                NumberKeyValue result = null;

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
        public static class ShreddedReader implements Step, Comparable<ShreddedReader>, TypeReader<NumberKeyValue>, ShreddedSource {      
            public ShreddedProcessor processor;
            ShreddedBuffer buffer;
            NumberKeyValue last = new NumberKeyValue();         
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
            
            public final NumberKeyValue read() throws IOException {
                if (buffer.isAtEnd()) {
                    fill();             
                
                    if (buffer.isAtEnd()) {
                        return null;
                    }
                }
                      
                assert !buffer.isAtEnd();
                NumberKeyValue result = new NumberKeyValue();
                
                result.number = buffer.getNumber();
                result.key = buffer.getKey();
                result.value = buffer.getValue();
                
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
                        buffer.processTuple(input.readBytes(), input.readBytes());
                        tupleCount++;
                    }
                } catch(EOFException e) {}
            }

            public final void updateNumber() throws IOException {
                if (updateNumberCount > tupleCount)
                    return;
                     
                last.number = input.readLong();
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
                } else if (processor instanceof NumberKeyValue.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((NumberKeyValue.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<NumberKeyValue>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<NumberKeyValue> getOutputClass() {
                return NumberKeyValue.class;
            }                
        }
        
        public static class DuplicateEliminator implements ShreddedProcessor {
            public ShreddedProcessor processor;
            NumberKeyValue last = new NumberKeyValue();
            boolean numberProcess = true;
                                           
            public DuplicateEliminator() {}
            public DuplicateEliminator(ShreddedProcessor processor) {
                this.processor = processor;
            }
            
            public void setShreddedProcessor(ShreddedProcessor processor) {
                this.processor = processor;
            }

            public void processNumber(long number) throws IOException {  
                if (numberProcess || Utility.compare(number, last.number) != 0) {
                    last.number = number;
                    processor.processNumber(number);
                    numberProcess = false;
                }
            }  
            
            public void resetNumber() {
                 numberProcess = true;
            }                                                
                               
            public void processTuple(byte[] key, byte[] value) throws IOException {
                processor.processTuple(key, value);
            } 
            
            public void close() throws IOException {
                processor.close();
            }                    
        }
        public static class TupleUnshredder implements ShreddedProcessor {
            NumberKeyValue last = new NumberKeyValue();
            public org.lemurproject.galago.tupleflow.Processor<NumberKeyValue> processor;                               
            
            public TupleUnshredder(NumberKeyValue.Processor processor) {
                this.processor = processor;
            }         
            
            public TupleUnshredder(org.lemurproject.galago.tupleflow.Processor<NumberKeyValue> processor) {
                this.processor = processor;
            }
            
            public NumberKeyValue clone(NumberKeyValue object) {
                NumberKeyValue result = new NumberKeyValue();
                if (object == null) return result;
                result.number = object.number; 
                result.key = object.key; 
                result.value = object.value; 
                return result;
            }                 
            
            public void processNumber(long number) throws IOException {
                last.number = number;
            }   
                
            
            public void processTuple(byte[] key, byte[] value) throws IOException {
                last.key = key;
                last.value = value;
                processor.process(clone(last));
            }               
            
            public void close() throws IOException {
                processor.close();
            }
        }     
        public static class TupleShredder implements Processor {
            NumberKeyValue last = null;
            public ShreddedProcessor processor;
            
            public TupleShredder(ShreddedProcessor processor) {
                this.processor = processor;
            }                              
            
            public NumberKeyValue clone(NumberKeyValue object) {
                NumberKeyValue result = new NumberKeyValue();
                if (object == null) return result;
                result.number = object.number; 
                result.key = object.key; 
                result.value = object.value; 
                return result;
            }                 
            
            public void process(NumberKeyValue object) throws IOException {                                                                                                                                                   
                boolean processAll = false;
                if(last == null || Utility.compare(last.number, object.number) != 0 || processAll) { processor.processNumber(object.number); processAll = true; }
                processor.processTuple(object.key, object.value);                                         
                last = object;
            }
                          
            public Class<NumberKeyValue> getInputClass() {
                return NumberKeyValue.class;
            }
            
            public void close() throws IOException {
                processor.close();
            }                     
        }
    } 
}    