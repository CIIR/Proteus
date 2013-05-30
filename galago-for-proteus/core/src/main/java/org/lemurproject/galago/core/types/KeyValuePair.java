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


public class KeyValuePair implements Type<KeyValuePair> {
    public byte[] key;
    public byte[] value; 
    
    public KeyValuePair() {}
    public KeyValuePair(byte[] key, byte[] value) {
        this.key = key;
        this.value = value;
    }  
    
    public String toString() {
        try {
            return String.format("%s,%s",
                                   new String(key, "UTF-8"), new String(value, "UTF-8"));
        } catch(UnsupportedEncodingException e) {
            throw new RuntimeException("Couldn't convert string to UTF-8.");
        }
    } 

    public Order<KeyValuePair> getOrder(String... spec) {
        if (Arrays.equals(spec, new String[] { "+key" })) {
            return new KeyOrder();
        }
        if (Arrays.equals(spec, new String[] { "+key", "+value" })) {
            return new KeyValueOrder();
        }
        return null;
    } 
      
    public interface Processor extends Step, org.lemurproject.galago.tupleflow.Processor<KeyValuePair> {
        public void process(KeyValuePair object) throws IOException;
        public void close() throws IOException;
    }                        
    public interface Source extends Step {
    }
    public static class KeyOrder implements Order<KeyValuePair> {
        public int hash(KeyValuePair object) {
            int h = 0;
            h += Utility.hash(object.key);
            return h;
        } 
        public Comparator<KeyValuePair> greaterThan() {
            return new Comparator<KeyValuePair>() {
                public int compare(KeyValuePair one, KeyValuePair two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.key, two.key);
                        if(result != 0) break;
                    } while (false);
                    return -result;
                }
            };
        }     
        public Comparator<KeyValuePair> lessThan() {
            return new Comparator<KeyValuePair>() {
                public int compare(KeyValuePair one, KeyValuePair two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.key, two.key);
                        if(result != 0) break;
                    } while (false);
                    return result;
                }
            };
        }     
        public TypeReader<KeyValuePair> orderedReader(ArrayInput _input) {
            return new ShreddedReader(_input);
        }    

        public TypeReader<KeyValuePair> orderedReader(ArrayInput _input, int bufferSize) {
            return new ShreddedReader(_input, bufferSize);
        }    
        public OrderedWriter<KeyValuePair> orderedWriter(ArrayOutput _output) {
            ShreddedWriter w = new ShreddedWriter(_output);
            return new OrderedWriterClass(w); 
        }                                    
        public static class OrderedWriterClass extends OrderedWriter< KeyValuePair > {
            KeyValuePair last = null;
            ShreddedWriter shreddedWriter = null; 
            
            public OrderedWriterClass(ShreddedWriter s) {
                this.shreddedWriter = s;
            }
            
            public void process(KeyValuePair object) throws IOException {
               boolean processAll = false;
               if (processAll || last == null || 0 != Utility.compare(object.key, last.key)) { processAll = true; shreddedWriter.processKey(object.key); }
               shreddedWriter.processTuple(object.value);
               last = object;
            }           
                 
            public void close() throws IOException {
                shreddedWriter.close();
            }
            
            public Class<KeyValuePair> getInputClass() {
                return KeyValuePair.class;
            }
        } 
        public ReaderSource<KeyValuePair> orderedCombiner(Collection<TypeReader<KeyValuePair>> readers, boolean closeOnExit) {
            ArrayList<ShreddedReader> shreddedReaders = new ArrayList();
            
            for (TypeReader<KeyValuePair> reader : readers) {
                shreddedReaders.add((ShreddedReader)reader);
            }
            
            return new ShreddedCombiner(shreddedReaders, closeOnExit);
        }                  
        public KeyValuePair clone(KeyValuePair object) {
            KeyValuePair result = new KeyValuePair();
            if (object == null) return result;
            result.key = object.key; 
            result.value = object.value; 
            return result;
        }                 
        public Class<KeyValuePair> getOrderedClass() {
            return KeyValuePair.class;
        }                           
        public String[] getOrderSpec() {
            return new String[] {"+key"};
        }

        public static String[] getSpec() {
            return new String[] {"+key"};
        }
        public static String getSpecString() {
            return "+key";
        }
                           
        public interface ShreddedProcessor extends Step {
            public void processKey(byte[] key) throws IOException;
            public void processTuple(byte[] value) throws IOException;
            public void close() throws IOException;
        }    
        public interface ShreddedSource extends Step {
        }                                              
        
        public static class ShreddedWriter implements ShreddedProcessor {
            ArrayOutput output;
            ShreddedBuffer buffer = new ShreddedBuffer();
            byte[] lastKey;
            boolean lastFlush = false;
            
            public ShreddedWriter(ArrayOutput output) {
                this.output = output;
            }                        
            
            public void close() throws IOException {
                flush();
            }
            
            public void processKey(byte[] key) {
                lastKey = key;
                buffer.processKey(key);
            }
            public final void processTuple(byte[] value) throws IOException {
                if (lastFlush) {
                    if(buffer.keys.size() == 0) buffer.processKey(lastKey);
                    lastFlush = false;
                }
                buffer.processTuple(value);
                if (buffer.isFull())
                    flush();
            }
            public final void flushTuples(int pauseIndex) throws IOException {
                
                while (buffer.getReadIndex() < pauseIndex) {
                           
                    output.writeBytes(buffer.getValue());
                    buffer.incrementTuple();
                }
            }  
            public final void flushKey(int pauseIndex) throws IOException {
                while (buffer.getReadIndex() < pauseIndex) {
                    int nextPause = buffer.getKeyEndIndex();
                    int count = nextPause - buffer.getReadIndex();
                    
                    output.writeBytes(buffer.getKey());
                    output.writeInt(count);
                    buffer.incrementKey();
                      
                    flushTuples(nextPause);
                    assert nextPause == buffer.getReadIndex();
                }
            }
            public void flush() throws IOException { 
                flushKey(buffer.getWriteIndex());
                buffer.reset(); 
                lastFlush = true;
            }                           
        }
        public static class ShreddedBuffer {
            ArrayList<byte[]> keys = new ArrayList();
            TIntArrayList keyTupleIdx = new TIntArrayList();
            int keyReadIdx = 0;
                            
            byte[][] values;
            int writeTupleIndex = 0;
            int readTupleIndex = 0;
            int batchSize;

            public ShreddedBuffer(int batchSize) {
                this.batchSize = batchSize;

                values = new byte[batchSize][];
            }                              

            public ShreddedBuffer() {    
                this(10000);
            }                                                                                                                    
            
            public void processKey(byte[] key) {
                keys.add(key);
                keyTupleIdx.add(writeTupleIndex);
            }                                      
            public void processTuple(byte[] value) {
                assert keys.size() > 0;
                values[writeTupleIndex] = value;
                writeTupleIndex++;
            }
            public void resetData() {
                keys.clear();
                keyTupleIdx.clear();
                writeTupleIndex = 0;
            }                  
                                 
            public void resetRead() {
                readTupleIndex = 0;
                keyReadIdx = 0;
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
            public void incrementKey() {
                keyReadIdx++;  
            }                                                                                              

            public void autoIncrementKey() {
                while (readTupleIndex >= getKeyEndIndex() && readTupleIndex < writeTupleIndex)
                    keyReadIdx++;
            }                 
            public void incrementTuple() {
                readTupleIndex++;
            }                    
            public int getKeyEndIndex() {
                if ((keyReadIdx+1) >= keyTupleIdx.size())
                    return writeTupleIndex;
                return keyTupleIdx.get(keyReadIdx+1);
            }
            public int getReadIndex() {
                return readTupleIndex;
            }   

            public int getWriteIndex() {
                return writeTupleIndex;
            } 
            public byte[] getKey() {
                assert readTupleIndex < writeTupleIndex;
                assert keyReadIdx < keys.size();
                
                return keys.get(keyReadIdx);
            }
            public byte[] getValue() {
                assert readTupleIndex < writeTupleIndex;
                return values[readTupleIndex];
            }                                         
            public void copyTuples(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                   output.processTuple(getValue());
                   incrementTuple();
                }
            }                                                                           
            public void copyUntilIndexKey(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                    output.processKey(getKey());
                    assert getKeyEndIndex() <= endIndex;
                    copyTuples(getKeyEndIndex(), output);
                    incrementKey();
                }
            }  
            public void copyUntilKey(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                while (!isAtEnd()) {
                    if (other != null) {   
                        assert !other.isAtEnd();
                        int c = + Utility.compare(getKey(), other.getKey());
                    
                        if (c > 0) {
                            break;   
                        }
                        
                        output.processKey(getKey());
                                      
                        copyTuples(getKeyEndIndex(), output);
                    } else {
                        output.processKey(getKey());
                        copyTuples(getKeyEndIndex(), output);
                    }
                    incrementKey();  
                    
               
                }
            }
            public void copyUntil(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                copyUntilKey(other, output);
            }
            
        }                         
        public static class ShreddedCombiner implements ReaderSource<KeyValuePair>, ShreddedSource {   
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
                } else if (processor instanceof KeyValuePair.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((KeyValuePair.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<KeyValuePair>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<KeyValuePair> getOutputClass() {
                return KeyValuePair.class;
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

            public KeyValuePair read() throws IOException {
                if (uninitialized)
                    initialize();

                KeyValuePair result = null;

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
        public static class ShreddedReader implements Step, Comparable<ShreddedReader>, TypeReader<KeyValuePair>, ShreddedSource {      
            public ShreddedProcessor processor;
            ShreddedBuffer buffer;
            KeyValuePair last = new KeyValuePair();         
            long updateKeyCount = -1;
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
                    result = + Utility.compare(buffer.getKey(), otherBuffer.getKey());
                    if(result != 0) break;
                } while (false);                                             
                
                return result;
            }
            
            public final ShreddedBuffer getBuffer() {
                return buffer;
            }                
            
            public final KeyValuePair read() throws IOException {
                if (buffer.isAtEnd()) {
                    fill();             
                
                    if (buffer.isAtEnd()) {
                        return null;
                    }
                }
                      
                assert !buffer.isAtEnd();
                KeyValuePair result = new KeyValuePair();
                
                result.key = buffer.getKey();
                result.value = buffer.getValue();
                
                buffer.incrementTuple();
                buffer.autoIncrementKey();
                
                return result;
            }           
            
            public final void fill() throws IOException {
                try {   
                    buffer.reset();
                    
                    if (tupleCount != 0) {
                                                      
                        if(updateKeyCount - tupleCount > 0) {
                            buffer.keys.add(last.key);
                            buffer.keyTupleIdx.add((int) (updateKeyCount - tupleCount));
                        }
                        bufferStartCount = tupleCount;
                    }
                    
                    while (!buffer.isFull()) {
                        updateKey();
                        buffer.processTuple(input.readBytes());
                        tupleCount++;
                    }
                } catch(EOFException e) {}
            }

            public final void updateKey() throws IOException {
                if (updateKeyCount > tupleCount)
                    return;
                     
                last.key = input.readBytes();
                updateKeyCount = tupleCount + input.readInt();
                                      
                buffer.processKey(last.key);
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
                } else if (processor instanceof KeyValuePair.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((KeyValuePair.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<KeyValuePair>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<KeyValuePair> getOutputClass() {
                return KeyValuePair.class;
            }                
        }
        
        public static class DuplicateEliminator implements ShreddedProcessor {
            public ShreddedProcessor processor;
            KeyValuePair last = new KeyValuePair();
            boolean keyProcess = true;
                                           
            public DuplicateEliminator() {}
            public DuplicateEliminator(ShreddedProcessor processor) {
                this.processor = processor;
            }
            
            public void setShreddedProcessor(ShreddedProcessor processor) {
                this.processor = processor;
            }

            public void processKey(byte[] key) throws IOException {  
                if (keyProcess || Utility.compare(key, last.key) != 0) {
                    last.key = key;
                    processor.processKey(key);
                    keyProcess = false;
                }
            }  
            
            public void resetKey() {
                 keyProcess = true;
            }                                                
                               
            public void processTuple(byte[] value) throws IOException {
                processor.processTuple(value);
            } 
            
            public void close() throws IOException {
                processor.close();
            }                    
        }
        public static class TupleUnshredder implements ShreddedProcessor {
            KeyValuePair last = new KeyValuePair();
            public org.lemurproject.galago.tupleflow.Processor<KeyValuePair> processor;                               
            
            public TupleUnshredder(KeyValuePair.Processor processor) {
                this.processor = processor;
            }         
            
            public TupleUnshredder(org.lemurproject.galago.tupleflow.Processor<KeyValuePair> processor) {
                this.processor = processor;
            }
            
            public KeyValuePair clone(KeyValuePair object) {
                KeyValuePair result = new KeyValuePair();
                if (object == null) return result;
                result.key = object.key; 
                result.value = object.value; 
                return result;
            }                 
            
            public void processKey(byte[] key) throws IOException {
                last.key = key;
            }   
                
            
            public void processTuple(byte[] value) throws IOException {
                last.value = value;
                processor.process(clone(last));
            }               
            
            public void close() throws IOException {
                processor.close();
            }
        }     
        public static class TupleShredder implements Processor {
            KeyValuePair last = null;
            public ShreddedProcessor processor;
            
            public TupleShredder(ShreddedProcessor processor) {
                this.processor = processor;
            }                              
            
            public KeyValuePair clone(KeyValuePair object) {
                KeyValuePair result = new KeyValuePair();
                if (object == null) return result;
                result.key = object.key; 
                result.value = object.value; 
                return result;
            }                 
            
            public void process(KeyValuePair object) throws IOException {                                                                                                                                                   
                boolean processAll = false;
                if(last == null || Utility.compare(last.key, object.key) != 0 || processAll) { processor.processKey(object.key); processAll = true; }
                processor.processTuple(object.value);                                         
                last = object;
            }
                          
            public Class<KeyValuePair> getInputClass() {
                return KeyValuePair.class;
            }
            
            public void close() throws IOException {
                processor.close();
            }                     
        }
    } 
    public static class KeyValueOrder implements Order<KeyValuePair> {
        public int hash(KeyValuePair object) {
            int h = 0;
            h += Utility.hash(object.key);
            h += Utility.hash(object.value);
            return h;
        } 
        public Comparator<KeyValuePair> greaterThan() {
            return new Comparator<KeyValuePair>() {
                public int compare(KeyValuePair one, KeyValuePair two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.key, two.key);
                        if(result != 0) break;
                        result = + Utility.compare(one.value, two.value);
                        if(result != 0) break;
                    } while (false);
                    return -result;
                }
            };
        }     
        public Comparator<KeyValuePair> lessThan() {
            return new Comparator<KeyValuePair>() {
                public int compare(KeyValuePair one, KeyValuePair two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.key, two.key);
                        if(result != 0) break;
                        result = + Utility.compare(one.value, two.value);
                        if(result != 0) break;
                    } while (false);
                    return result;
                }
            };
        }     
        public TypeReader<KeyValuePair> orderedReader(ArrayInput _input) {
            return new ShreddedReader(_input);
        }    

        public TypeReader<KeyValuePair> orderedReader(ArrayInput _input, int bufferSize) {
            return new ShreddedReader(_input, bufferSize);
        }    
        public OrderedWriter<KeyValuePair> orderedWriter(ArrayOutput _output) {
            ShreddedWriter w = new ShreddedWriter(_output);
            return new OrderedWriterClass(w); 
        }                                    
        public static class OrderedWriterClass extends OrderedWriter< KeyValuePair > {
            KeyValuePair last = null;
            ShreddedWriter shreddedWriter = null; 
            
            public OrderedWriterClass(ShreddedWriter s) {
                this.shreddedWriter = s;
            }
            
            public void process(KeyValuePair object) throws IOException {
               boolean processAll = false;
               if (processAll || last == null || 0 != Utility.compare(object.key, last.key)) { processAll = true; shreddedWriter.processKey(object.key); }
               if (processAll || last == null || 0 != Utility.compare(object.value, last.value)) { processAll = true; shreddedWriter.processValue(object.value); }
               shreddedWriter.processTuple();
               last = object;
            }           
                 
            public void close() throws IOException {
                shreddedWriter.close();
            }
            
            public Class<KeyValuePair> getInputClass() {
                return KeyValuePair.class;
            }
        } 
        public ReaderSource<KeyValuePair> orderedCombiner(Collection<TypeReader<KeyValuePair>> readers, boolean closeOnExit) {
            ArrayList<ShreddedReader> shreddedReaders = new ArrayList();
            
            for (TypeReader<KeyValuePair> reader : readers) {
                shreddedReaders.add((ShreddedReader)reader);
            }
            
            return new ShreddedCombiner(shreddedReaders, closeOnExit);
        }                  
        public KeyValuePair clone(KeyValuePair object) {
            KeyValuePair result = new KeyValuePair();
            if (object == null) return result;
            result.key = object.key; 
            result.value = object.value; 
            return result;
        }                 
        public Class<KeyValuePair> getOrderedClass() {
            return KeyValuePair.class;
        }                           
        public String[] getOrderSpec() {
            return new String[] {"+key", "+value"};
        }

        public static String[] getSpec() {
            return new String[] {"+key", "+value"};
        }
        public static String getSpecString() {
            return "+key +value";
        }
                           
        public interface ShreddedProcessor extends Step {
            public void processKey(byte[] key) throws IOException;
            public void processValue(byte[] value) throws IOException;
            public void processTuple() throws IOException;
            public void close() throws IOException;
        }    
        public interface ShreddedSource extends Step {
        }                                              
        
        public static class ShreddedWriter implements ShreddedProcessor {
            ArrayOutput output;
            ShreddedBuffer buffer = new ShreddedBuffer();
            byte[] lastKey;
            byte[] lastValue;
            boolean lastFlush = false;
            
            public ShreddedWriter(ArrayOutput output) {
                this.output = output;
            }                        
            
            public void close() throws IOException {
                flush();
            }
            
            public void processKey(byte[] key) {
                lastKey = key;
                buffer.processKey(key);
            }
            public void processValue(byte[] value) {
                lastValue = value;
                buffer.processValue(value);
            }
            public final void processTuple() throws IOException {
                if (lastFlush) {
                    if(buffer.keys.size() == 0) buffer.processKey(lastKey);
                    if(buffer.values.size() == 0) buffer.processValue(lastValue);
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
            public final void flushKey(int pauseIndex) throws IOException {
                while (buffer.getReadIndex() < pauseIndex) {
                    int nextPause = buffer.getKeyEndIndex();
                    int count = nextPause - buffer.getReadIndex();
                    
                    output.writeBytes(buffer.getKey());
                    output.writeInt(count);
                    buffer.incrementKey();
                      
                    flushValue(nextPause);
                    assert nextPause == buffer.getReadIndex();
                }
            }
            public final void flushValue(int pauseIndex) throws IOException {
                while (buffer.getReadIndex() < pauseIndex) {
                    int nextPause = buffer.getValueEndIndex();
                    int count = nextPause - buffer.getReadIndex();
                    
                    output.writeBytes(buffer.getValue());
                    output.writeInt(count);
                    buffer.incrementValue();
                      
                    flushTuples(nextPause);
                    assert nextPause == buffer.getReadIndex();
                }
            }
            public void flush() throws IOException { 
                flushKey(buffer.getWriteIndex());
                buffer.reset(); 
                lastFlush = true;
            }                           
        }
        public static class ShreddedBuffer {
            ArrayList<byte[]> keys = new ArrayList();
            ArrayList<byte[]> values = new ArrayList();
            TIntArrayList keyTupleIdx = new TIntArrayList();
            TIntArrayList valueTupleIdx = new TIntArrayList();
            int keyReadIdx = 0;
            int valueReadIdx = 0;
                            
            int writeTupleIndex = 0;
            int readTupleIndex = 0;
            int batchSize;

            public ShreddedBuffer(int batchSize) {
                this.batchSize = batchSize;

            }                              

            public ShreddedBuffer() {    
                this(10000);
            }                                                                                                                    
            
            public void processKey(byte[] key) {
                keys.add(key);
                keyTupleIdx.add(writeTupleIndex);
            }                                      
            public void processValue(byte[] value) {
                values.add(value);
                valueTupleIdx.add(writeTupleIndex);
            }                                      
            public void processTuple() {
                assert keys.size() > 0;
                assert values.size() > 0;
                writeTupleIndex++;
            }
            public void resetData() {
                keys.clear();
                values.clear();
                keyTupleIdx.clear();
                valueTupleIdx.clear();
                writeTupleIndex = 0;
            }                  
                                 
            public void resetRead() {
                readTupleIndex = 0;
                keyReadIdx = 0;
                valueReadIdx = 0;
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
            public void incrementKey() {
                keyReadIdx++;  
            }                                                                                              

            public void autoIncrementKey() {
                while (readTupleIndex >= getKeyEndIndex() && readTupleIndex < writeTupleIndex)
                    keyReadIdx++;
            }                 
            public void incrementValue() {
                valueReadIdx++;  
            }                                                                                              

            public void autoIncrementValue() {
                while (readTupleIndex >= getValueEndIndex() && readTupleIndex < writeTupleIndex)
                    valueReadIdx++;
            }                 
            public void incrementTuple() {
                readTupleIndex++;
            }                    
            public int getKeyEndIndex() {
                if ((keyReadIdx+1) >= keyTupleIdx.size())
                    return writeTupleIndex;
                return keyTupleIdx.get(keyReadIdx+1);
            }

            public int getValueEndIndex() {
                if ((valueReadIdx+1) >= valueTupleIdx.size())
                    return writeTupleIndex;
                return valueTupleIdx.get(valueReadIdx+1);
            }
            public int getReadIndex() {
                return readTupleIndex;
            }   

            public int getWriteIndex() {
                return writeTupleIndex;
            } 
            public byte[] getKey() {
                assert readTupleIndex < writeTupleIndex;
                assert keyReadIdx < keys.size();
                
                return keys.get(keyReadIdx);
            }
            public byte[] getValue() {
                assert readTupleIndex < writeTupleIndex;
                assert valueReadIdx < values.size();
                
                return values.get(valueReadIdx);
            }

            public void copyTuples(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                   output.processTuple();
                   incrementTuple();
                }
            }                                                                           
            public void copyUntilIndexKey(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                    output.processKey(getKey());
                    assert getKeyEndIndex() <= endIndex;
                    copyUntilIndexValue(getKeyEndIndex(), output);
                    incrementKey();
                }
            } 
            public void copyUntilIndexValue(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                    output.processValue(getValue());
                    assert getValueEndIndex() <= endIndex;
                    copyTuples(getValueEndIndex(), output);
                    incrementValue();
                }
            }  
            public void copyUntilKey(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                while (!isAtEnd()) {
                    if (other != null) {   
                        assert !other.isAtEnd();
                        int c = + Utility.compare(getKey(), other.getKey());
                    
                        if (c > 0) {
                            break;   
                        }
                        
                        output.processKey(getKey());
                                      
                        if (c < 0) {
                            copyUntilIndexValue(getKeyEndIndex(), output);
                        } else if (c == 0) {
                            copyUntilValue(other, output);
                            autoIncrementKey();
                            break;
                        }
                    } else {
                        output.processKey(getKey());
                        copyUntilIndexValue(getKeyEndIndex(), output);
                    }
                    incrementKey();  
                    
               
                }
            }
            public void copyUntilValue(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                while (!isAtEnd()) {
                    if (other != null) {   
                        assert !other.isAtEnd();
                        int c = + Utility.compare(getValue(), other.getValue());
                    
                        if (c > 0) {
                            break;   
                        }
                        
                        output.processValue(getValue());
                                      
                        copyTuples(getValueEndIndex(), output);
                    } else {
                        output.processValue(getValue());
                        copyTuples(getValueEndIndex(), output);
                    }
                    incrementValue();  
                    
                    if (getKeyEndIndex() <= readTupleIndex)
                        break;   
                }
            }
            public void copyUntil(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                copyUntilKey(other, output);
            }
            
        }                         
        public static class ShreddedCombiner implements ReaderSource<KeyValuePair>, ShreddedSource {   
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
                } else if (processor instanceof KeyValuePair.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((KeyValuePair.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<KeyValuePair>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<KeyValuePair> getOutputClass() {
                return KeyValuePair.class;
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

            public KeyValuePair read() throws IOException {
                if (uninitialized)
                    initialize();

                KeyValuePair result = null;

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
        public static class ShreddedReader implements Step, Comparable<ShreddedReader>, TypeReader<KeyValuePair>, ShreddedSource {      
            public ShreddedProcessor processor;
            ShreddedBuffer buffer;
            KeyValuePair last = new KeyValuePair();         
            long updateKeyCount = -1;
            long updateValueCount = -1;
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
                    result = + Utility.compare(buffer.getKey(), otherBuffer.getKey());
                    if(result != 0) break;
                    result = + Utility.compare(buffer.getValue(), otherBuffer.getValue());
                    if(result != 0) break;
                } while (false);                                             
                
                return result;
            }
            
            public final ShreddedBuffer getBuffer() {
                return buffer;
            }                
            
            public final KeyValuePair read() throws IOException {
                if (buffer.isAtEnd()) {
                    fill();             
                
                    if (buffer.isAtEnd()) {
                        return null;
                    }
                }
                      
                assert !buffer.isAtEnd();
                KeyValuePair result = new KeyValuePair();
                
                result.key = buffer.getKey();
                result.value = buffer.getValue();
                
                buffer.incrementTuple();
                buffer.autoIncrementKey();
                buffer.autoIncrementValue();
                
                return result;
            }           
            
            public final void fill() throws IOException {
                try {   
                    buffer.reset();
                    
                    if (tupleCount != 0) {
                                                      
                        if(updateKeyCount - tupleCount > 0) {
                            buffer.keys.add(last.key);
                            buffer.keyTupleIdx.add((int) (updateKeyCount - tupleCount));
                        }                              
                        if(updateValueCount - tupleCount > 0) {
                            buffer.values.add(last.value);
                            buffer.valueTupleIdx.add((int) (updateValueCount - tupleCount));
                        }
                        bufferStartCount = tupleCount;
                    }
                    
                    while (!buffer.isFull()) {
                        updateValue();
                        buffer.processTuple();
                        tupleCount++;
                    }
                } catch(EOFException e) {}
            }

            public final void updateKey() throws IOException {
                if (updateKeyCount > tupleCount)
                    return;
                     
                last.key = input.readBytes();
                updateKeyCount = tupleCount + input.readInt();
                                      
                buffer.processKey(last.key);
            }
            public final void updateValue() throws IOException {
                if (updateValueCount > tupleCount)
                    return;
                     
                updateKey();
                last.value = input.readBytes();
                updateValueCount = tupleCount + input.readInt();
                                      
                buffer.processValue(last.value);
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
                } else if (processor instanceof KeyValuePair.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((KeyValuePair.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<KeyValuePair>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<KeyValuePair> getOutputClass() {
                return KeyValuePair.class;
            }                
        }
        
        public static class DuplicateEliminator implements ShreddedProcessor {
            public ShreddedProcessor processor;
            KeyValuePair last = new KeyValuePair();
            boolean keyProcess = true;
            boolean valueProcess = true;
                                           
            public DuplicateEliminator() {}
            public DuplicateEliminator(ShreddedProcessor processor) {
                this.processor = processor;
            }
            
            public void setShreddedProcessor(ShreddedProcessor processor) {
                this.processor = processor;
            }

            public void processKey(byte[] key) throws IOException {  
                if (keyProcess || Utility.compare(key, last.key) != 0) {
                    last.key = key;
                    processor.processKey(key);
            resetValue();
                    keyProcess = false;
                }
            }
            public void processValue(byte[] value) throws IOException {  
                if (valueProcess || Utility.compare(value, last.value) != 0) {
                    last.value = value;
                    processor.processValue(value);
                    valueProcess = false;
                }
            }  
            
            public void resetKey() {
                 keyProcess = true;
            resetValue();
            }                                                
            public void resetValue() {
                 valueProcess = true;
            }                                                
                               
            public void processTuple() throws IOException {
                processor.processTuple();
            } 
            
            public void close() throws IOException {
                processor.close();
            }                    
        }
        public static class TupleUnshredder implements ShreddedProcessor {
            KeyValuePair last = new KeyValuePair();
            public org.lemurproject.galago.tupleflow.Processor<KeyValuePair> processor;                               
            
            public TupleUnshredder(KeyValuePair.Processor processor) {
                this.processor = processor;
            }         
            
            public TupleUnshredder(org.lemurproject.galago.tupleflow.Processor<KeyValuePair> processor) {
                this.processor = processor;
            }
            
            public KeyValuePair clone(KeyValuePair object) {
                KeyValuePair result = new KeyValuePair();
                if (object == null) return result;
                result.key = object.key; 
                result.value = object.value; 
                return result;
            }                 
            
            public void processKey(byte[] key) throws IOException {
                last.key = key;
            }   
                
            public void processValue(byte[] value) throws IOException {
                last.value = value;
            }   
                
            
            public void processTuple() throws IOException {
                processor.process(clone(last));
            }               
            
            public void close() throws IOException {
                processor.close();
            }
        }     
        public static class TupleShredder implements Processor {
            KeyValuePair last = null;
            public ShreddedProcessor processor;
            
            public TupleShredder(ShreddedProcessor processor) {
                this.processor = processor;
            }                              
            
            public KeyValuePair clone(KeyValuePair object) {
                KeyValuePair result = new KeyValuePair();
                if (object == null) return result;
                result.key = object.key; 
                result.value = object.value; 
                return result;
            }                 
            
            public void process(KeyValuePair object) throws IOException {                                                                                                                                                   
                boolean processAll = false;
                if(last == null || Utility.compare(last.key, object.key) != 0 || processAll) { processor.processKey(object.key); processAll = true; }
                if(last == null || Utility.compare(last.value, object.value) != 0 || processAll) { processor.processValue(object.value); processAll = true; }
                processor.processTuple();                                         
                last = object;
            }
                          
            public Class<KeyValuePair> getInputClass() {
                return KeyValuePair.class;
            }
            
            public void close() throws IOException {
                processor.close();
            }                     
        }
    } 
}    