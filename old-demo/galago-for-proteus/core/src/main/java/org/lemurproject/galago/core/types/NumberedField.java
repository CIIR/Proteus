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


public class NumberedField implements Type<NumberedField> {
    public byte[] fieldName;
    public long number;
    public byte[] content; 
    
    public NumberedField() {}
    public NumberedField(byte[] fieldName, long number, byte[] content) {
        this.fieldName = fieldName;
        this.number = number;
        this.content = content;
    }  
    
    public String toString() {
        try {
            return String.format("%s,%d,%s",
                                   new String(fieldName, "UTF-8"), number, new String(content, "UTF-8"));
        } catch(UnsupportedEncodingException e) {
            throw new RuntimeException("Couldn't convert string to UTF-8.");
        }
    } 

    public Order<NumberedField> getOrder(String... spec) {
        if (Arrays.equals(spec, new String[] { "+fieldName", "+number" })) {
            return new FieldNameNumberOrder();
        }
        if (Arrays.equals(spec, new String[] { "+fieldName" })) {
            return new FieldNameOrder();
        }
        return null;
    } 
      
    public interface Processor extends Step, org.lemurproject.galago.tupleflow.Processor<NumberedField> {
        public void process(NumberedField object) throws IOException;
        public void close() throws IOException;
    }                        
    public interface Source extends Step {
    }
    public static class FieldNameNumberOrder implements Order<NumberedField> {
        public int hash(NumberedField object) {
            int h = 0;
            h += Utility.hash(object.fieldName);
            h += Utility.hash(object.number);
            return h;
        } 
        public Comparator<NumberedField> greaterThan() {
            return new Comparator<NumberedField>() {
                public int compare(NumberedField one, NumberedField two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.fieldName, two.fieldName);
                        if(result != 0) break;
                        result = + Utility.compare(one.number, two.number);
                        if(result != 0) break;
                    } while (false);
                    return -result;
                }
            };
        }     
        public Comparator<NumberedField> lessThan() {
            return new Comparator<NumberedField>() {
                public int compare(NumberedField one, NumberedField two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.fieldName, two.fieldName);
                        if(result != 0) break;
                        result = + Utility.compare(one.number, two.number);
                        if(result != 0) break;
                    } while (false);
                    return result;
                }
            };
        }     
        public TypeReader<NumberedField> orderedReader(ArrayInput _input) {
            return new ShreddedReader(_input);
        }    

        public TypeReader<NumberedField> orderedReader(ArrayInput _input, int bufferSize) {
            return new ShreddedReader(_input, bufferSize);
        }    
        public OrderedWriter<NumberedField> orderedWriter(ArrayOutput _output) {
            ShreddedWriter w = new ShreddedWriter(_output);
            return new OrderedWriterClass(w); 
        }                                    
        public static class OrderedWriterClass extends OrderedWriter< NumberedField > {
            NumberedField last = null;
            ShreddedWriter shreddedWriter = null; 
            
            public OrderedWriterClass(ShreddedWriter s) {
                this.shreddedWriter = s;
            }
            
            public void process(NumberedField object) throws IOException {
               boolean processAll = false;
               if (processAll || last == null || 0 != Utility.compare(object.fieldName, last.fieldName)) { processAll = true; shreddedWriter.processFieldName(object.fieldName); }
               if (processAll || last == null || 0 != Utility.compare(object.number, last.number)) { processAll = true; shreddedWriter.processNumber(object.number); }
               shreddedWriter.processTuple(object.content);
               last = object;
            }           
                 
            public void close() throws IOException {
                shreddedWriter.close();
            }
            
            public Class<NumberedField> getInputClass() {
                return NumberedField.class;
            }
        } 
        public ReaderSource<NumberedField> orderedCombiner(Collection<TypeReader<NumberedField>> readers, boolean closeOnExit) {
            ArrayList<ShreddedReader> shreddedReaders = new ArrayList();
            
            for (TypeReader<NumberedField> reader : readers) {
                shreddedReaders.add((ShreddedReader)reader);
            }
            
            return new ShreddedCombiner(shreddedReaders, closeOnExit);
        }                  
        public NumberedField clone(NumberedField object) {
            NumberedField result = new NumberedField();
            if (object == null) return result;
            result.fieldName = object.fieldName; 
            result.number = object.number; 
            result.content = object.content; 
            return result;
        }                 
        public Class<NumberedField> getOrderedClass() {
            return NumberedField.class;
        }                           
        public String[] getOrderSpec() {
            return new String[] {"+fieldName", "+number"};
        }

        public static String[] getSpec() {
            return new String[] {"+fieldName", "+number"};
        }
        public static String getSpecString() {
            return "+fieldName +number";
        }
                           
        public interface ShreddedProcessor extends Step {
            public void processFieldName(byte[] fieldName) throws IOException;
            public void processNumber(long number) throws IOException;
            public void processTuple(byte[] content) throws IOException;
            public void close() throws IOException;
        }    
        public interface ShreddedSource extends Step {
        }                                              
        
        public static class ShreddedWriter implements ShreddedProcessor {
            ArrayOutput output;
            ShreddedBuffer buffer = new ShreddedBuffer();
            byte[] lastFieldName;
            long lastNumber;
            boolean lastFlush = false;
            
            public ShreddedWriter(ArrayOutput output) {
                this.output = output;
            }                        
            
            public void close() throws IOException {
                flush();
            }
            
            public void processFieldName(byte[] fieldName) {
                lastFieldName = fieldName;
                buffer.processFieldName(fieldName);
            }
            public void processNumber(long number) {
                lastNumber = number;
                buffer.processNumber(number);
            }
            public final void processTuple(byte[] content) throws IOException {
                if (lastFlush) {
                    if(buffer.fieldNames.size() == 0) buffer.processFieldName(lastFieldName);
                    if(buffer.numbers.size() == 0) buffer.processNumber(lastNumber);
                    lastFlush = false;
                }
                buffer.processTuple(content);
                if (buffer.isFull())
                    flush();
            }
            public final void flushTuples(int pauseIndex) throws IOException {
                
                while (buffer.getReadIndex() < pauseIndex) {
                           
                    output.writeBytes(buffer.getContent());
                    buffer.incrementTuple();
                }
            }  
            public final void flushFieldName(int pauseIndex) throws IOException {
                while (buffer.getReadIndex() < pauseIndex) {
                    int nextPause = buffer.getFieldNameEndIndex();
                    int count = nextPause - buffer.getReadIndex();
                    
                    output.writeBytes(buffer.getFieldName());
                    output.writeInt(count);
                    buffer.incrementFieldName();
                      
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
                      
                    flushTuples(nextPause);
                    assert nextPause == buffer.getReadIndex();
                }
            }
            public void flush() throws IOException { 
                flushFieldName(buffer.getWriteIndex());
                buffer.reset(); 
                lastFlush = true;
            }                           
        }
        public static class ShreddedBuffer {
            ArrayList<byte[]> fieldNames = new ArrayList();
            TLongArrayList numbers = new TLongArrayList();
            TIntArrayList fieldNameTupleIdx = new TIntArrayList();
            TIntArrayList numberTupleIdx = new TIntArrayList();
            int fieldNameReadIdx = 0;
            int numberReadIdx = 0;
                            
            byte[][] contents;
            int writeTupleIndex = 0;
            int readTupleIndex = 0;
            int batchSize;

            public ShreddedBuffer(int batchSize) {
                this.batchSize = batchSize;

                contents = new byte[batchSize][];
            }                              

            public ShreddedBuffer() {    
                this(10000);
            }                                                                                                                    
            
            public void processFieldName(byte[] fieldName) {
                fieldNames.add(fieldName);
                fieldNameTupleIdx.add(writeTupleIndex);
            }                                      
            public void processNumber(long number) {
                numbers.add(number);
                numberTupleIdx.add(writeTupleIndex);
            }                                      
            public void processTuple(byte[] content) {
                assert fieldNames.size() > 0;
                assert numbers.size() > 0;
                contents[writeTupleIndex] = content;
                writeTupleIndex++;
            }
            public void resetData() {
                fieldNames.clear();
                numbers.clear();
                fieldNameTupleIdx.clear();
                numberTupleIdx.clear();
                writeTupleIndex = 0;
            }                  
                                 
            public void resetRead() {
                readTupleIndex = 0;
                fieldNameReadIdx = 0;
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
            public void incrementFieldName() {
                fieldNameReadIdx++;  
            }                                                                                              

            public void autoIncrementFieldName() {
                while (readTupleIndex >= getFieldNameEndIndex() && readTupleIndex < writeTupleIndex)
                    fieldNameReadIdx++;
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
            public int getFieldNameEndIndex() {
                if ((fieldNameReadIdx+1) >= fieldNameTupleIdx.size())
                    return writeTupleIndex;
                return fieldNameTupleIdx.get(fieldNameReadIdx+1);
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
            public byte[] getFieldName() {
                assert readTupleIndex < writeTupleIndex;
                assert fieldNameReadIdx < fieldNames.size();
                
                return fieldNames.get(fieldNameReadIdx);
            }
            public long getNumber() {
                assert readTupleIndex < writeTupleIndex;
                assert numberReadIdx < numbers.size();
                
                return numbers.get(numberReadIdx);
            }
            public byte[] getContent() {
                assert readTupleIndex < writeTupleIndex;
                return contents[readTupleIndex];
            }                                         
            public void copyTuples(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                   output.processTuple(getContent());
                   incrementTuple();
                }
            }                                                                           
            public void copyUntilIndexFieldName(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                    output.processFieldName(getFieldName());
                    assert getFieldNameEndIndex() <= endIndex;
                    copyUntilIndexNumber(getFieldNameEndIndex(), output);
                    incrementFieldName();
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
            public void copyUntilFieldName(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                while (!isAtEnd()) {
                    if (other != null) {   
                        assert !other.isAtEnd();
                        int c = + Utility.compare(getFieldName(), other.getFieldName());
                    
                        if (c > 0) {
                            break;   
                        }
                        
                        output.processFieldName(getFieldName());
                                      
                        if (c < 0) {
                            copyUntilIndexNumber(getFieldNameEndIndex(), output);
                        } else if (c == 0) {
                            copyUntilNumber(other, output);
                            autoIncrementFieldName();
                            break;
                        }
                    } else {
                        output.processFieldName(getFieldName());
                        copyUntilIndexNumber(getFieldNameEndIndex(), output);
                    }
                    incrementFieldName();  
                    
               
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
                    
                    if (getFieldNameEndIndex() <= readTupleIndex)
                        break;   
                }
            }
            public void copyUntil(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                copyUntilFieldName(other, output);
            }
            
        }                         
        public static class ShreddedCombiner implements ReaderSource<NumberedField>, ShreddedSource {   
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
                } else if (processor instanceof NumberedField.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((NumberedField.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<NumberedField>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<NumberedField> getOutputClass() {
                return NumberedField.class;
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

            public NumberedField read() throws IOException {
                if (uninitialized)
                    initialize();

                NumberedField result = null;

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
        public static class ShreddedReader implements Step, Comparable<ShreddedReader>, TypeReader<NumberedField>, ShreddedSource {      
            public ShreddedProcessor processor;
            ShreddedBuffer buffer;
            NumberedField last = new NumberedField();         
            long updateFieldNameCount = -1;
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
                    result = + Utility.compare(buffer.getFieldName(), otherBuffer.getFieldName());
                    if(result != 0) break;
                    result = + Utility.compare(buffer.getNumber(), otherBuffer.getNumber());
                    if(result != 0) break;
                } while (false);                                             
                
                return result;
            }
            
            public final ShreddedBuffer getBuffer() {
                return buffer;
            }                
            
            public final NumberedField read() throws IOException {
                if (buffer.isAtEnd()) {
                    fill();             
                
                    if (buffer.isAtEnd()) {
                        return null;
                    }
                }
                      
                assert !buffer.isAtEnd();
                NumberedField result = new NumberedField();
                
                result.fieldName = buffer.getFieldName();
                result.number = buffer.getNumber();
                result.content = buffer.getContent();
                
                buffer.incrementTuple();
                buffer.autoIncrementFieldName();
                buffer.autoIncrementNumber();
                
                return result;
            }           
            
            public final void fill() throws IOException {
                try {   
                    buffer.reset();
                    
                    if (tupleCount != 0) {
                                                      
                        if(updateFieldNameCount - tupleCount > 0) {
                            buffer.fieldNames.add(last.fieldName);
                            buffer.fieldNameTupleIdx.add((int) (updateFieldNameCount - tupleCount));
                        }                              
                        if(updateNumberCount - tupleCount > 0) {
                            buffer.numbers.add(last.number);
                            buffer.numberTupleIdx.add((int) (updateNumberCount - tupleCount));
                        }
                        bufferStartCount = tupleCount;
                    }
                    
                    while (!buffer.isFull()) {
                        updateNumber();
                        buffer.processTuple(input.readBytes());
                        tupleCount++;
                    }
                } catch(EOFException e) {}
            }

            public final void updateFieldName() throws IOException {
                if (updateFieldNameCount > tupleCount)
                    return;
                     
                last.fieldName = input.readBytes();
                updateFieldNameCount = tupleCount + input.readInt();
                                      
                buffer.processFieldName(last.fieldName);
            }
            public final void updateNumber() throws IOException {
                if (updateNumberCount > tupleCount)
                    return;
                     
                updateFieldName();
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
                } else if (processor instanceof NumberedField.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((NumberedField.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<NumberedField>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<NumberedField> getOutputClass() {
                return NumberedField.class;
            }                
        }
        
        public static class DuplicateEliminator implements ShreddedProcessor {
            public ShreddedProcessor processor;
            NumberedField last = new NumberedField();
            boolean fieldNameProcess = true;
            boolean numberProcess = true;
                                           
            public DuplicateEliminator() {}
            public DuplicateEliminator(ShreddedProcessor processor) {
                this.processor = processor;
            }
            
            public void setShreddedProcessor(ShreddedProcessor processor) {
                this.processor = processor;
            }

            public void processFieldName(byte[] fieldName) throws IOException {  
                if (fieldNameProcess || Utility.compare(fieldName, last.fieldName) != 0) {
                    last.fieldName = fieldName;
                    processor.processFieldName(fieldName);
            resetNumber();
                    fieldNameProcess = false;
                }
            }
            public void processNumber(long number) throws IOException {  
                if (numberProcess || Utility.compare(number, last.number) != 0) {
                    last.number = number;
                    processor.processNumber(number);
                    numberProcess = false;
                }
            }  
            
            public void resetFieldName() {
                 fieldNameProcess = true;
            resetNumber();
            }                                                
            public void resetNumber() {
                 numberProcess = true;
            }                                                
                               
            public void processTuple(byte[] content) throws IOException {
                processor.processTuple(content);
            } 
            
            public void close() throws IOException {
                processor.close();
            }                    
        }
        public static class TupleUnshredder implements ShreddedProcessor {
            NumberedField last = new NumberedField();
            public org.lemurproject.galago.tupleflow.Processor<NumberedField> processor;                               
            
            public TupleUnshredder(NumberedField.Processor processor) {
                this.processor = processor;
            }         
            
            public TupleUnshredder(org.lemurproject.galago.tupleflow.Processor<NumberedField> processor) {
                this.processor = processor;
            }
            
            public NumberedField clone(NumberedField object) {
                NumberedField result = new NumberedField();
                if (object == null) return result;
                result.fieldName = object.fieldName; 
                result.number = object.number; 
                result.content = object.content; 
                return result;
            }                 
            
            public void processFieldName(byte[] fieldName) throws IOException {
                last.fieldName = fieldName;
            }   
                
            public void processNumber(long number) throws IOException {
                last.number = number;
            }   
                
            
            public void processTuple(byte[] content) throws IOException {
                last.content = content;
                processor.process(clone(last));
            }               
            
            public void close() throws IOException {
                processor.close();
            }
        }     
        public static class TupleShredder implements Processor {
            NumberedField last = null;
            public ShreddedProcessor processor;
            
            public TupleShredder(ShreddedProcessor processor) {
                this.processor = processor;
            }                              
            
            public NumberedField clone(NumberedField object) {
                NumberedField result = new NumberedField();
                if (object == null) return result;
                result.fieldName = object.fieldName; 
                result.number = object.number; 
                result.content = object.content; 
                return result;
            }                 
            
            public void process(NumberedField object) throws IOException {                                                                                                                                                   
                boolean processAll = false;
                if(last == null || Utility.compare(last.fieldName, object.fieldName) != 0 || processAll) { processor.processFieldName(object.fieldName); processAll = true; }
                if(last == null || Utility.compare(last.number, object.number) != 0 || processAll) { processor.processNumber(object.number); processAll = true; }
                processor.processTuple(object.content);                                         
                last = object;
            }
                          
            public Class<NumberedField> getInputClass() {
                return NumberedField.class;
            }
            
            public void close() throws IOException {
                processor.close();
            }                     
        }
    } 
    public static class FieldNameOrder implements Order<NumberedField> {
        public int hash(NumberedField object) {
            int h = 0;
            h += Utility.hash(object.fieldName);
            return h;
        } 
        public Comparator<NumberedField> greaterThan() {
            return new Comparator<NumberedField>() {
                public int compare(NumberedField one, NumberedField two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.fieldName, two.fieldName);
                        if(result != 0) break;
                    } while (false);
                    return -result;
                }
            };
        }     
        public Comparator<NumberedField> lessThan() {
            return new Comparator<NumberedField>() {
                public int compare(NumberedField one, NumberedField two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.fieldName, two.fieldName);
                        if(result != 0) break;
                    } while (false);
                    return result;
                }
            };
        }     
        public TypeReader<NumberedField> orderedReader(ArrayInput _input) {
            return new ShreddedReader(_input);
        }    

        public TypeReader<NumberedField> orderedReader(ArrayInput _input, int bufferSize) {
            return new ShreddedReader(_input, bufferSize);
        }    
        public OrderedWriter<NumberedField> orderedWriter(ArrayOutput _output) {
            ShreddedWriter w = new ShreddedWriter(_output);
            return new OrderedWriterClass(w); 
        }                                    
        public static class OrderedWriterClass extends OrderedWriter< NumberedField > {
            NumberedField last = null;
            ShreddedWriter shreddedWriter = null; 
            
            public OrderedWriterClass(ShreddedWriter s) {
                this.shreddedWriter = s;
            }
            
            public void process(NumberedField object) throws IOException {
               boolean processAll = false;
               if (processAll || last == null || 0 != Utility.compare(object.fieldName, last.fieldName)) { processAll = true; shreddedWriter.processFieldName(object.fieldName); }
               shreddedWriter.processTuple(object.number, object.content);
               last = object;
            }           
                 
            public void close() throws IOException {
                shreddedWriter.close();
            }
            
            public Class<NumberedField> getInputClass() {
                return NumberedField.class;
            }
        } 
        public ReaderSource<NumberedField> orderedCombiner(Collection<TypeReader<NumberedField>> readers, boolean closeOnExit) {
            ArrayList<ShreddedReader> shreddedReaders = new ArrayList();
            
            for (TypeReader<NumberedField> reader : readers) {
                shreddedReaders.add((ShreddedReader)reader);
            }
            
            return new ShreddedCombiner(shreddedReaders, closeOnExit);
        }                  
        public NumberedField clone(NumberedField object) {
            NumberedField result = new NumberedField();
            if (object == null) return result;
            result.fieldName = object.fieldName; 
            result.number = object.number; 
            result.content = object.content; 
            return result;
        }                 
        public Class<NumberedField> getOrderedClass() {
            return NumberedField.class;
        }                           
        public String[] getOrderSpec() {
            return new String[] {"+fieldName"};
        }

        public static String[] getSpec() {
            return new String[] {"+fieldName"};
        }
        public static String getSpecString() {
            return "+fieldName";
        }
                           
        public interface ShreddedProcessor extends Step {
            public void processFieldName(byte[] fieldName) throws IOException;
            public void processTuple(long number, byte[] content) throws IOException;
            public void close() throws IOException;
        }    
        public interface ShreddedSource extends Step {
        }                                              
        
        public static class ShreddedWriter implements ShreddedProcessor {
            ArrayOutput output;
            ShreddedBuffer buffer = new ShreddedBuffer();
            byte[] lastFieldName;
            boolean lastFlush = false;
            
            public ShreddedWriter(ArrayOutput output) {
                this.output = output;
            }                        
            
            public void close() throws IOException {
                flush();
            }
            
            public void processFieldName(byte[] fieldName) {
                lastFieldName = fieldName;
                buffer.processFieldName(fieldName);
            }
            public final void processTuple(long number, byte[] content) throws IOException {
                if (lastFlush) {
                    if(buffer.fieldNames.size() == 0) buffer.processFieldName(lastFieldName);
                    lastFlush = false;
                }
                buffer.processTuple(number, content);
                if (buffer.isFull())
                    flush();
            }
            public final void flushTuples(int pauseIndex) throws IOException {
                
                while (buffer.getReadIndex() < pauseIndex) {
                           
                    output.writeLong(buffer.getNumber());
                    output.writeBytes(buffer.getContent());
                    buffer.incrementTuple();
                }
            }  
            public final void flushFieldName(int pauseIndex) throws IOException {
                while (buffer.getReadIndex() < pauseIndex) {
                    int nextPause = buffer.getFieldNameEndIndex();
                    int count = nextPause - buffer.getReadIndex();
                    
                    output.writeBytes(buffer.getFieldName());
                    output.writeInt(count);
                    buffer.incrementFieldName();
                      
                    flushTuples(nextPause);
                    assert nextPause == buffer.getReadIndex();
                }
            }
            public void flush() throws IOException { 
                flushFieldName(buffer.getWriteIndex());
                buffer.reset(); 
                lastFlush = true;
            }                           
        }
        public static class ShreddedBuffer {
            ArrayList<byte[]> fieldNames = new ArrayList();
            TIntArrayList fieldNameTupleIdx = new TIntArrayList();
            int fieldNameReadIdx = 0;
                            
            long[] numbers;
            byte[][] contents;
            int writeTupleIndex = 0;
            int readTupleIndex = 0;
            int batchSize;

            public ShreddedBuffer(int batchSize) {
                this.batchSize = batchSize;

                numbers = new long[batchSize];
                contents = new byte[batchSize][];
            }                              

            public ShreddedBuffer() {    
                this(10000);
            }                                                                                                                    
            
            public void processFieldName(byte[] fieldName) {
                fieldNames.add(fieldName);
                fieldNameTupleIdx.add(writeTupleIndex);
            }                                      
            public void processTuple(long number, byte[] content) {
                assert fieldNames.size() > 0;
                numbers[writeTupleIndex] = number;
                contents[writeTupleIndex] = content;
                writeTupleIndex++;
            }
            public void resetData() {
                fieldNames.clear();
                fieldNameTupleIdx.clear();
                writeTupleIndex = 0;
            }                  
                                 
            public void resetRead() {
                readTupleIndex = 0;
                fieldNameReadIdx = 0;
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
            public void incrementFieldName() {
                fieldNameReadIdx++;  
            }                                                                                              

            public void autoIncrementFieldName() {
                while (readTupleIndex >= getFieldNameEndIndex() && readTupleIndex < writeTupleIndex)
                    fieldNameReadIdx++;
            }                 
            public void incrementTuple() {
                readTupleIndex++;
            }                    
            public int getFieldNameEndIndex() {
                if ((fieldNameReadIdx+1) >= fieldNameTupleIdx.size())
                    return writeTupleIndex;
                return fieldNameTupleIdx.get(fieldNameReadIdx+1);
            }
            public int getReadIndex() {
                return readTupleIndex;
            }   

            public int getWriteIndex() {
                return writeTupleIndex;
            } 
            public byte[] getFieldName() {
                assert readTupleIndex < writeTupleIndex;
                assert fieldNameReadIdx < fieldNames.size();
                
                return fieldNames.get(fieldNameReadIdx);
            }
            public long getNumber() {
                assert readTupleIndex < writeTupleIndex;
                return numbers[readTupleIndex];
            }                                         
            public byte[] getContent() {
                assert readTupleIndex < writeTupleIndex;
                return contents[readTupleIndex];
            }                                         
            public void copyTuples(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                   output.processTuple(getNumber(), getContent());
                   incrementTuple();
                }
            }                                                                           
            public void copyUntilIndexFieldName(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                    output.processFieldName(getFieldName());
                    assert getFieldNameEndIndex() <= endIndex;
                    copyTuples(getFieldNameEndIndex(), output);
                    incrementFieldName();
                }
            }  
            public void copyUntilFieldName(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                while (!isAtEnd()) {
                    if (other != null) {   
                        assert !other.isAtEnd();
                        int c = + Utility.compare(getFieldName(), other.getFieldName());
                    
                        if (c > 0) {
                            break;   
                        }
                        
                        output.processFieldName(getFieldName());
                                      
                        copyTuples(getFieldNameEndIndex(), output);
                    } else {
                        output.processFieldName(getFieldName());
                        copyTuples(getFieldNameEndIndex(), output);
                    }
                    incrementFieldName();  
                    
               
                }
            }
            public void copyUntil(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                copyUntilFieldName(other, output);
            }
            
        }                         
        public static class ShreddedCombiner implements ReaderSource<NumberedField>, ShreddedSource {   
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
                } else if (processor instanceof NumberedField.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((NumberedField.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<NumberedField>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<NumberedField> getOutputClass() {
                return NumberedField.class;
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

            public NumberedField read() throws IOException {
                if (uninitialized)
                    initialize();

                NumberedField result = null;

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
        public static class ShreddedReader implements Step, Comparable<ShreddedReader>, TypeReader<NumberedField>, ShreddedSource {      
            public ShreddedProcessor processor;
            ShreddedBuffer buffer;
            NumberedField last = new NumberedField();         
            long updateFieldNameCount = -1;
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
                    result = + Utility.compare(buffer.getFieldName(), otherBuffer.getFieldName());
                    if(result != 0) break;
                } while (false);                                             
                
                return result;
            }
            
            public final ShreddedBuffer getBuffer() {
                return buffer;
            }                
            
            public final NumberedField read() throws IOException {
                if (buffer.isAtEnd()) {
                    fill();             
                
                    if (buffer.isAtEnd()) {
                        return null;
                    }
                }
                      
                assert !buffer.isAtEnd();
                NumberedField result = new NumberedField();
                
                result.fieldName = buffer.getFieldName();
                result.number = buffer.getNumber();
                result.content = buffer.getContent();
                
                buffer.incrementTuple();
                buffer.autoIncrementFieldName();
                
                return result;
            }           
            
            public final void fill() throws IOException {
                try {   
                    buffer.reset();
                    
                    if (tupleCount != 0) {
                                                      
                        if(updateFieldNameCount - tupleCount > 0) {
                            buffer.fieldNames.add(last.fieldName);
                            buffer.fieldNameTupleIdx.add((int) (updateFieldNameCount - tupleCount));
                        }
                        bufferStartCount = tupleCount;
                    }
                    
                    while (!buffer.isFull()) {
                        updateFieldName();
                        buffer.processTuple(input.readLong(), input.readBytes());
                        tupleCount++;
                    }
                } catch(EOFException e) {}
            }

            public final void updateFieldName() throws IOException {
                if (updateFieldNameCount > tupleCount)
                    return;
                     
                last.fieldName = input.readBytes();
                updateFieldNameCount = tupleCount + input.readInt();
                                      
                buffer.processFieldName(last.fieldName);
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
                } else if (processor instanceof NumberedField.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((NumberedField.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<NumberedField>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<NumberedField> getOutputClass() {
                return NumberedField.class;
            }                
        }
        
        public static class DuplicateEliminator implements ShreddedProcessor {
            public ShreddedProcessor processor;
            NumberedField last = new NumberedField();
            boolean fieldNameProcess = true;
                                           
            public DuplicateEliminator() {}
            public DuplicateEliminator(ShreddedProcessor processor) {
                this.processor = processor;
            }
            
            public void setShreddedProcessor(ShreddedProcessor processor) {
                this.processor = processor;
            }

            public void processFieldName(byte[] fieldName) throws IOException {  
                if (fieldNameProcess || Utility.compare(fieldName, last.fieldName) != 0) {
                    last.fieldName = fieldName;
                    processor.processFieldName(fieldName);
                    fieldNameProcess = false;
                }
            }  
            
            public void resetFieldName() {
                 fieldNameProcess = true;
            }                                                
                               
            public void processTuple(long number, byte[] content) throws IOException {
                processor.processTuple(number, content);
            } 
            
            public void close() throws IOException {
                processor.close();
            }                    
        }
        public static class TupleUnshredder implements ShreddedProcessor {
            NumberedField last = new NumberedField();
            public org.lemurproject.galago.tupleflow.Processor<NumberedField> processor;                               
            
            public TupleUnshredder(NumberedField.Processor processor) {
                this.processor = processor;
            }         
            
            public TupleUnshredder(org.lemurproject.galago.tupleflow.Processor<NumberedField> processor) {
                this.processor = processor;
            }
            
            public NumberedField clone(NumberedField object) {
                NumberedField result = new NumberedField();
                if (object == null) return result;
                result.fieldName = object.fieldName; 
                result.number = object.number; 
                result.content = object.content; 
                return result;
            }                 
            
            public void processFieldName(byte[] fieldName) throws IOException {
                last.fieldName = fieldName;
            }   
                
            
            public void processTuple(long number, byte[] content) throws IOException {
                last.number = number;
                last.content = content;
                processor.process(clone(last));
            }               
            
            public void close() throws IOException {
                processor.close();
            }
        }     
        public static class TupleShredder implements Processor {
            NumberedField last = null;
            public ShreddedProcessor processor;
            
            public TupleShredder(ShreddedProcessor processor) {
                this.processor = processor;
            }                              
            
            public NumberedField clone(NumberedField object) {
                NumberedField result = new NumberedField();
                if (object == null) return result;
                result.fieldName = object.fieldName; 
                result.number = object.number; 
                result.content = object.content; 
                return result;
            }                 
            
            public void process(NumberedField object) throws IOException {                                                                                                                                                   
                boolean processAll = false;
                if(last == null || Utility.compare(last.fieldName, object.fieldName) != 0 || processAll) { processor.processFieldName(object.fieldName); processAll = true; }
                processor.processTuple(object.number, object.content);                                         
                last = object;
            }
                          
            public Class<NumberedField> getInputClass() {
                return NumberedField.class;
            }
            
            public void close() throws IOException {
                processor.close();
            }                     
        }
    } 
}    