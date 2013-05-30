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


public class NumberedValuedExtent implements Type<NumberedValuedExtent> {
    public byte[] extentName;
    public long number;
    public int begin;
    public int end;
    public long value; 
    
    public NumberedValuedExtent() {}
    public NumberedValuedExtent(byte[] extentName, long number, int begin, int end, long value) {
        this.extentName = extentName;
        this.number = number;
        this.begin = begin;
        this.end = end;
        this.value = value;
    }  
    
    public String toString() {
        try {
            return String.format("%s,%d,%d,%d,%d",
                                   new String(extentName, "UTF-8"), number, begin, end, value);
        } catch(UnsupportedEncodingException e) {
            throw new RuntimeException("Couldn't convert string to UTF-8.");
        }
    } 

    public Order<NumberedValuedExtent> getOrder(String... spec) {
        if (Arrays.equals(spec, new String[] { "+extentName", "+number", "+begin" })) {
            return new ExtentNameNumberBeginOrder();
        }
        return null;
    } 
      
    public interface Processor extends Step, org.lemurproject.galago.tupleflow.Processor<NumberedValuedExtent> {
        public void process(NumberedValuedExtent object) throws IOException;
        public void close() throws IOException;
    }                        
    public interface Source extends Step {
    }
    public static class ExtentNameNumberBeginOrder implements Order<NumberedValuedExtent> {
        public int hash(NumberedValuedExtent object) {
            int h = 0;
            h += Utility.hash(object.extentName);
            h += Utility.hash(object.number);
            h += Utility.hash(object.begin);
            return h;
        } 
        public Comparator<NumberedValuedExtent> greaterThan() {
            return new Comparator<NumberedValuedExtent>() {
                public int compare(NumberedValuedExtent one, NumberedValuedExtent two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.extentName, two.extentName);
                        if(result != 0) break;
                        result = + Utility.compare(one.number, two.number);
                        if(result != 0) break;
                        result = + Utility.compare(one.begin, two.begin);
                        if(result != 0) break;
                    } while (false);
                    return -result;
                }
            };
        }     
        public Comparator<NumberedValuedExtent> lessThan() {
            return new Comparator<NumberedValuedExtent>() {
                public int compare(NumberedValuedExtent one, NumberedValuedExtent two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.extentName, two.extentName);
                        if(result != 0) break;
                        result = + Utility.compare(one.number, two.number);
                        if(result != 0) break;
                        result = + Utility.compare(one.begin, two.begin);
                        if(result != 0) break;
                    } while (false);
                    return result;
                }
            };
        }     
        public TypeReader<NumberedValuedExtent> orderedReader(ArrayInput _input) {
            return new ShreddedReader(_input);
        }    

        public TypeReader<NumberedValuedExtent> orderedReader(ArrayInput _input, int bufferSize) {
            return new ShreddedReader(_input, bufferSize);
        }    
        public OrderedWriter<NumberedValuedExtent> orderedWriter(ArrayOutput _output) {
            ShreddedWriter w = new ShreddedWriter(_output);
            return new OrderedWriterClass(w); 
        }                                    
        public static class OrderedWriterClass extends OrderedWriter< NumberedValuedExtent > {
            NumberedValuedExtent last = null;
            ShreddedWriter shreddedWriter = null; 
            
            public OrderedWriterClass(ShreddedWriter s) {
                this.shreddedWriter = s;
            }
            
            public void process(NumberedValuedExtent object) throws IOException {
               boolean processAll = false;
               if (processAll || last == null || 0 != Utility.compare(object.extentName, last.extentName)) { processAll = true; shreddedWriter.processExtentName(object.extentName); }
               if (processAll || last == null || 0 != Utility.compare(object.number, last.number)) { processAll = true; shreddedWriter.processNumber(object.number); }
               if (processAll || last == null || 0 != Utility.compare(object.begin, last.begin)) { processAll = true; shreddedWriter.processBegin(object.begin); }
               shreddedWriter.processTuple(object.end, object.value);
               last = object;
            }           
                 
            public void close() throws IOException {
                shreddedWriter.close();
            }
            
            public Class<NumberedValuedExtent> getInputClass() {
                return NumberedValuedExtent.class;
            }
        } 
        public ReaderSource<NumberedValuedExtent> orderedCombiner(Collection<TypeReader<NumberedValuedExtent>> readers, boolean closeOnExit) {
            ArrayList<ShreddedReader> shreddedReaders = new ArrayList();
            
            for (TypeReader<NumberedValuedExtent> reader : readers) {
                shreddedReaders.add((ShreddedReader)reader);
            }
            
            return new ShreddedCombiner(shreddedReaders, closeOnExit);
        }                  
        public NumberedValuedExtent clone(NumberedValuedExtent object) {
            NumberedValuedExtent result = new NumberedValuedExtent();
            if (object == null) return result;
            result.extentName = object.extentName; 
            result.number = object.number; 
            result.begin = object.begin; 
            result.end = object.end; 
            result.value = object.value; 
            return result;
        }                 
        public Class<NumberedValuedExtent> getOrderedClass() {
            return NumberedValuedExtent.class;
        }                           
        public String[] getOrderSpec() {
            return new String[] {"+extentName", "+number", "+begin"};
        }

        public static String[] getSpec() {
            return new String[] {"+extentName", "+number", "+begin"};
        }
        public static String getSpecString() {
            return "+extentName +number +begin";
        }
                           
        public interface ShreddedProcessor extends Step {
            public void processExtentName(byte[] extentName) throws IOException;
            public void processNumber(long number) throws IOException;
            public void processBegin(int begin) throws IOException;
            public void processTuple(int end, long value) throws IOException;
            public void close() throws IOException;
        }    
        public interface ShreddedSource extends Step {
        }                                              
        
        public static class ShreddedWriter implements ShreddedProcessor {
            ArrayOutput output;
            ShreddedBuffer buffer = new ShreddedBuffer();
            byte[] lastExtentName;
            long lastNumber;
            int lastBegin;
            boolean lastFlush = false;
            
            public ShreddedWriter(ArrayOutput output) {
                this.output = output;
            }                        
            
            public void close() throws IOException {
                flush();
            }
            
            public void processExtentName(byte[] extentName) {
                lastExtentName = extentName;
                buffer.processExtentName(extentName);
            }
            public void processNumber(long number) {
                lastNumber = number;
                buffer.processNumber(number);
            }
            public void processBegin(int begin) {
                lastBegin = begin;
                buffer.processBegin(begin);
            }
            public final void processTuple(int end, long value) throws IOException {
                if (lastFlush) {
                    if(buffer.extentNames.size() == 0) buffer.processExtentName(lastExtentName);
                    if(buffer.numbers.size() == 0) buffer.processNumber(lastNumber);
                    if(buffer.begins.size() == 0) buffer.processBegin(lastBegin);
                    lastFlush = false;
                }
                buffer.processTuple(end, value);
                if (buffer.isFull())
                    flush();
            }
            public final void flushTuples(int pauseIndex) throws IOException {
                
                while (buffer.getReadIndex() < pauseIndex) {
                           
                    output.writeInt(buffer.getEnd());
                    output.writeLong(buffer.getValue());
                    buffer.incrementTuple();
                }
            }  
            public final void flushExtentName(int pauseIndex) throws IOException {
                while (buffer.getReadIndex() < pauseIndex) {
                    int nextPause = buffer.getExtentNameEndIndex();
                    int count = nextPause - buffer.getReadIndex();
                    
                    output.writeBytes(buffer.getExtentName());
                    output.writeInt(count);
                    buffer.incrementExtentName();
                      
                    flushNumber(nextPause);
                    assert nextPause == buffer.getReadIndex();
                }
            }
            public final void flushNumber(int pauseIndex) throws IOException {
                while (buffer.getReadIndex() < pauseIndex) {
                    int nextPause = buffer.getNumberEndIndex();
                    int count = nextPause - buffer.getReadIndex();
                    
                    output.writeLong(buffer.getNumber());
                    output.writeInt(count);
                    buffer.incrementNumber();
                      
                    flushBegin(nextPause);
                    assert nextPause == buffer.getReadIndex();
                }
            }
            public final void flushBegin(int pauseIndex) throws IOException {
                while (buffer.getReadIndex() < pauseIndex) {
                    int nextPause = buffer.getBeginEndIndex();
                    int count = nextPause - buffer.getReadIndex();
                    
                    output.writeInt(buffer.getBegin());
                    output.writeInt(count);
                    buffer.incrementBegin();
                      
                    flushTuples(nextPause);
                    assert nextPause == buffer.getReadIndex();
                }
            }
            public void flush() throws IOException { 
                flushExtentName(buffer.getWriteIndex());
                buffer.reset(); 
                lastFlush = true;
            }                           
        }
        public static class ShreddedBuffer {
            ArrayList<byte[]> extentNames = new ArrayList();
            TLongArrayList numbers = new TLongArrayList();
            TIntArrayList begins = new TIntArrayList();
            TIntArrayList extentNameTupleIdx = new TIntArrayList();
            TIntArrayList numberTupleIdx = new TIntArrayList();
            TIntArrayList beginTupleIdx = new TIntArrayList();
            int extentNameReadIdx = 0;
            int numberReadIdx = 0;
            int beginReadIdx = 0;
                            
            int[] ends;
            long[] values;
            int writeTupleIndex = 0;
            int readTupleIndex = 0;
            int batchSize;

            public ShreddedBuffer(int batchSize) {
                this.batchSize = batchSize;

                ends = new int[batchSize];
                values = new long[batchSize];
            }                              

            public ShreddedBuffer() {    
                this(10000);
            }                                                                                                                    
            
            public void processExtentName(byte[] extentName) {
                extentNames.add(extentName);
                extentNameTupleIdx.add(writeTupleIndex);
            }                                      
            public void processNumber(long number) {
                numbers.add(number);
                numberTupleIdx.add(writeTupleIndex);
            }                                      
            public void processBegin(int begin) {
                begins.add(begin);
                beginTupleIdx.add(writeTupleIndex);
            }                                      
            public void processTuple(int end, long value) {
                assert extentNames.size() > 0;
                assert numbers.size() > 0;
                assert begins.size() > 0;
                ends[writeTupleIndex] = end;
                values[writeTupleIndex] = value;
                writeTupleIndex++;
            }
            public void resetData() {
                extentNames.clear();
                numbers.clear();
                begins.clear();
                extentNameTupleIdx.clear();
                numberTupleIdx.clear();
                beginTupleIdx.clear();
                writeTupleIndex = 0;
            }                  
                                 
            public void resetRead() {
                readTupleIndex = 0;
                extentNameReadIdx = 0;
                numberReadIdx = 0;
                beginReadIdx = 0;
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
            public void incrementExtentName() {
                extentNameReadIdx++;  
            }                                                                                              

            public void autoIncrementExtentName() {
                while (readTupleIndex >= getExtentNameEndIndex() && readTupleIndex < writeTupleIndex)
                    extentNameReadIdx++;
            }                 
            public void incrementNumber() {
                numberReadIdx++;  
            }                                                                                              

            public void autoIncrementNumber() {
                while (readTupleIndex >= getNumberEndIndex() && readTupleIndex < writeTupleIndex)
                    numberReadIdx++;
            }                 
            public void incrementBegin() {
                beginReadIdx++;  
            }                                                                                              

            public void autoIncrementBegin() {
                while (readTupleIndex >= getBeginEndIndex() && readTupleIndex < writeTupleIndex)
                    beginReadIdx++;
            }                 
            public void incrementTuple() {
                readTupleIndex++;
            }                    
            public int getExtentNameEndIndex() {
                if ((extentNameReadIdx+1) >= extentNameTupleIdx.size())
                    return writeTupleIndex;
                return extentNameTupleIdx.get(extentNameReadIdx+1);
            }

            public int getNumberEndIndex() {
                if ((numberReadIdx+1) >= numberTupleIdx.size())
                    return writeTupleIndex;
                return numberTupleIdx.get(numberReadIdx+1);
            }

            public int getBeginEndIndex() {
                if ((beginReadIdx+1) >= beginTupleIdx.size())
                    return writeTupleIndex;
                return beginTupleIdx.get(beginReadIdx+1);
            }
            public int getReadIndex() {
                return readTupleIndex;
            }   

            public int getWriteIndex() {
                return writeTupleIndex;
            } 
            public byte[] getExtentName() {
                assert readTupleIndex < writeTupleIndex;
                assert extentNameReadIdx < extentNames.size();
                
                return extentNames.get(extentNameReadIdx);
            }
            public long getNumber() {
                assert readTupleIndex < writeTupleIndex;
                assert numberReadIdx < numbers.size();
                
                return numbers.get(numberReadIdx);
            }
            public int getBegin() {
                assert readTupleIndex < writeTupleIndex;
                assert beginReadIdx < begins.size();
                
                return begins.get(beginReadIdx);
            }
            public int getEnd() {
                assert readTupleIndex < writeTupleIndex;
                return ends[readTupleIndex];
            }                                         
            public long getValue() {
                assert readTupleIndex < writeTupleIndex;
                return values[readTupleIndex];
            }                                         
            public void copyTuples(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                   output.processTuple(getEnd(), getValue());
                   incrementTuple();
                }
            }                                                                           
            public void copyUntilIndexExtentName(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                    output.processExtentName(getExtentName());
                    assert getExtentNameEndIndex() <= endIndex;
                    copyUntilIndexNumber(getExtentNameEndIndex(), output);
                    incrementExtentName();
                }
            } 
            public void copyUntilIndexNumber(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                    output.processNumber(getNumber());
                    assert getNumberEndIndex() <= endIndex;
                    copyUntilIndexBegin(getNumberEndIndex(), output);
                    incrementNumber();
                }
            } 
            public void copyUntilIndexBegin(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                    output.processBegin(getBegin());
                    assert getBeginEndIndex() <= endIndex;
                    copyTuples(getBeginEndIndex(), output);
                    incrementBegin();
                }
            }  
            public void copyUntilExtentName(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                while (!isAtEnd()) {
                    if (other != null) {   
                        assert !other.isAtEnd();
                        int c = + Utility.compare(getExtentName(), other.getExtentName());
                    
                        if (c > 0) {
                            break;   
                        }
                        
                        output.processExtentName(getExtentName());
                                      
                        if (c < 0) {
                            copyUntilIndexNumber(getExtentNameEndIndex(), output);
                        } else if (c == 0) {
                            copyUntilNumber(other, output);
                            autoIncrementExtentName();
                            break;
                        }
                    } else {
                        output.processExtentName(getExtentName());
                        copyUntilIndexNumber(getExtentNameEndIndex(), output);
                    }
                    incrementExtentName();  
                    
               
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
                                      
                        if (c < 0) {
                            copyUntilIndexBegin(getNumberEndIndex(), output);
                        } else if (c == 0) {
                            copyUntilBegin(other, output);
                            autoIncrementNumber();
                            break;
                        }
                    } else {
                        output.processNumber(getNumber());
                        copyUntilIndexBegin(getNumberEndIndex(), output);
                    }
                    incrementNumber();  
                    
                    if (getExtentNameEndIndex() <= readTupleIndex)
                        break;   
                }
            }
            public void copyUntilBegin(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                while (!isAtEnd()) {
                    if (other != null) {   
                        assert !other.isAtEnd();
                        int c = + Utility.compare(getBegin(), other.getBegin());
                    
                        if (c > 0) {
                            break;   
                        }
                        
                        output.processBegin(getBegin());
                                      
                        copyTuples(getBeginEndIndex(), output);
                    } else {
                        output.processBegin(getBegin());
                        copyTuples(getBeginEndIndex(), output);
                    }
                    incrementBegin();  
                    
                    if (getNumberEndIndex() <= readTupleIndex)
                        break;   
                }
            }
            public void copyUntil(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                copyUntilExtentName(other, output);
            }
            
        }                         
        public static class ShreddedCombiner implements ReaderSource<NumberedValuedExtent>, ShreddedSource {   
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
                } else if (processor instanceof NumberedValuedExtent.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((NumberedValuedExtent.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<NumberedValuedExtent>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<NumberedValuedExtent> getOutputClass() {
                return NumberedValuedExtent.class;
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

            public NumberedValuedExtent read() throws IOException {
                if (uninitialized)
                    initialize();

                NumberedValuedExtent result = null;

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
        public static class ShreddedReader implements Step, Comparable<ShreddedReader>, TypeReader<NumberedValuedExtent>, ShreddedSource {      
            public ShreddedProcessor processor;
            ShreddedBuffer buffer;
            NumberedValuedExtent last = new NumberedValuedExtent();         
            long updateExtentNameCount = -1;
            long updateNumberCount = -1;
            long updateBeginCount = -1;
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
                    result = + Utility.compare(buffer.getExtentName(), otherBuffer.getExtentName());
                    if(result != 0) break;
                    result = + Utility.compare(buffer.getNumber(), otherBuffer.getNumber());
                    if(result != 0) break;
                    result = + Utility.compare(buffer.getBegin(), otherBuffer.getBegin());
                    if(result != 0) break;
                } while (false);                                             
                
                return result;
            }
            
            public final ShreddedBuffer getBuffer() {
                return buffer;
            }                
            
            public final NumberedValuedExtent read() throws IOException {
                if (buffer.isAtEnd()) {
                    fill();             
                
                    if (buffer.isAtEnd()) {
                        return null;
                    }
                }
                      
                assert !buffer.isAtEnd();
                NumberedValuedExtent result = new NumberedValuedExtent();
                
                result.extentName = buffer.getExtentName();
                result.number = buffer.getNumber();
                result.begin = buffer.getBegin();
                result.end = buffer.getEnd();
                result.value = buffer.getValue();
                
                buffer.incrementTuple();
                buffer.autoIncrementExtentName();
                buffer.autoIncrementNumber();
                buffer.autoIncrementBegin();
                
                return result;
            }           
            
            public final void fill() throws IOException {
                try {   
                    buffer.reset();
                    
                    if (tupleCount != 0) {
                                                      
                        if(updateExtentNameCount - tupleCount > 0) {
                            buffer.extentNames.add(last.extentName);
                            buffer.extentNameTupleIdx.add((int) (updateExtentNameCount - tupleCount));
                        }                              
                        if(updateNumberCount - tupleCount > 0) {
                            buffer.numbers.add(last.number);
                            buffer.numberTupleIdx.add((int) (updateNumberCount - tupleCount));
                        }                              
                        if(updateBeginCount - tupleCount > 0) {
                            buffer.begins.add(last.begin);
                            buffer.beginTupleIdx.add((int) (updateBeginCount - tupleCount));
                        }
                        bufferStartCount = tupleCount;
                    }
                    
                    while (!buffer.isFull()) {
                        updateBegin();
                        buffer.processTuple(input.readInt(), input.readLong());
                        tupleCount++;
                    }
                } catch(EOFException e) {}
            }

            public final void updateExtentName() throws IOException {
                if (updateExtentNameCount > tupleCount)
                    return;
                     
                last.extentName = input.readBytes();
                updateExtentNameCount = tupleCount + input.readInt();
                                      
                buffer.processExtentName(last.extentName);
            }
            public final void updateNumber() throws IOException {
                if (updateNumberCount > tupleCount)
                    return;
                     
                updateExtentName();
                last.number = input.readLong();
                updateNumberCount = tupleCount + input.readInt();
                                      
                buffer.processNumber(last.number);
            }
            public final void updateBegin() throws IOException {
                if (updateBeginCount > tupleCount)
                    return;
                     
                updateNumber();
                last.begin = input.readInt();
                updateBeginCount = tupleCount + input.readInt();
                                      
                buffer.processBegin(last.begin);
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
                } else if (processor instanceof NumberedValuedExtent.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((NumberedValuedExtent.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<NumberedValuedExtent>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<NumberedValuedExtent> getOutputClass() {
                return NumberedValuedExtent.class;
            }                
        }
        
        public static class DuplicateEliminator implements ShreddedProcessor {
            public ShreddedProcessor processor;
            NumberedValuedExtent last = new NumberedValuedExtent();
            boolean extentNameProcess = true;
            boolean numberProcess = true;
            boolean beginProcess = true;
                                           
            public DuplicateEliminator() {}
            public DuplicateEliminator(ShreddedProcessor processor) {
                this.processor = processor;
            }
            
            public void setShreddedProcessor(ShreddedProcessor processor) {
                this.processor = processor;
            }

            public void processExtentName(byte[] extentName) throws IOException {  
                if (extentNameProcess || Utility.compare(extentName, last.extentName) != 0) {
                    last.extentName = extentName;
                    processor.processExtentName(extentName);
            resetNumber();
                    extentNameProcess = false;
                }
            }
            public void processNumber(long number) throws IOException {  
                if (numberProcess || Utility.compare(number, last.number) != 0) {
                    last.number = number;
                    processor.processNumber(number);
            resetBegin();
                    numberProcess = false;
                }
            }
            public void processBegin(int begin) throws IOException {  
                if (beginProcess || Utility.compare(begin, last.begin) != 0) {
                    last.begin = begin;
                    processor.processBegin(begin);
                    beginProcess = false;
                }
            }  
            
            public void resetExtentName() {
                 extentNameProcess = true;
            resetNumber();
            }                                                
            public void resetNumber() {
                 numberProcess = true;
            resetBegin();
            }                                                
            public void resetBegin() {
                 beginProcess = true;
            }                                                
                               
            public void processTuple(int end, long value) throws IOException {
                processor.processTuple(end, value);
            } 
            
            public void close() throws IOException {
                processor.close();
            }                    
        }
        public static class TupleUnshredder implements ShreddedProcessor {
            NumberedValuedExtent last = new NumberedValuedExtent();
            public org.lemurproject.galago.tupleflow.Processor<NumberedValuedExtent> processor;                               
            
            public TupleUnshredder(NumberedValuedExtent.Processor processor) {
                this.processor = processor;
            }         
            
            public TupleUnshredder(org.lemurproject.galago.tupleflow.Processor<NumberedValuedExtent> processor) {
                this.processor = processor;
            }
            
            public NumberedValuedExtent clone(NumberedValuedExtent object) {
                NumberedValuedExtent result = new NumberedValuedExtent();
                if (object == null) return result;
                result.extentName = object.extentName; 
                result.number = object.number; 
                result.begin = object.begin; 
                result.end = object.end; 
                result.value = object.value; 
                return result;
            }                 
            
            public void processExtentName(byte[] extentName) throws IOException {
                last.extentName = extentName;
            }   
                
            public void processNumber(long number) throws IOException {
                last.number = number;
            }   
                
            public void processBegin(int begin) throws IOException {
                last.begin = begin;
            }   
                
            
            public void processTuple(int end, long value) throws IOException {
                last.end = end;
                last.value = value;
                processor.process(clone(last));
            }               
            
            public void close() throws IOException {
                processor.close();
            }
        }     
        public static class TupleShredder implements Processor {
            NumberedValuedExtent last = null;
            public ShreddedProcessor processor;
            
            public TupleShredder(ShreddedProcessor processor) {
                this.processor = processor;
            }                              
            
            public NumberedValuedExtent clone(NumberedValuedExtent object) {
                NumberedValuedExtent result = new NumberedValuedExtent();
                if (object == null) return result;
                result.extentName = object.extentName; 
                result.number = object.number; 
                result.begin = object.begin; 
                result.end = object.end; 
                result.value = object.value; 
                return result;
            }                 
            
            public void process(NumberedValuedExtent object) throws IOException {                                                                                                                                                   
                boolean processAll = false;
                if(last == null || Utility.compare(last.extentName, object.extentName) != 0 || processAll) { processor.processExtentName(object.extentName); processAll = true; }
                if(last == null || Utility.compare(last.number, object.number) != 0 || processAll) { processor.processNumber(object.number); processAll = true; }
                if(last == null || Utility.compare(last.begin, object.begin) != 0 || processAll) { processor.processBegin(object.begin); processAll = true; }
                processor.processTuple(object.end, object.value);                                         
                last = object;
            }
                          
            public Class<NumberedValuedExtent> getInputClass() {
                return NumberedValuedExtent.class;
            }
            
            public void close() throws IOException {
                processor.close();
            }                     
        }
    } 
}    