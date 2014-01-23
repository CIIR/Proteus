// This file was automatically generated with the command: 
//     java org.lemurproject.galago.tupleflow.typebuilder.TypeBuilderMojo ...
package org.lemurproject.galago.tupleflow.types;

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


public class SerializedParameters implements Type<SerializedParameters> {
    public String parameters; 
    
    public SerializedParameters() {}
    public SerializedParameters(String parameters) {
        this.parameters = parameters;
    }  
    
    public String toString() {
            return String.format("%s",
                                   parameters);
    } 

    public Order<SerializedParameters> getOrder(String... spec) {
        if (Arrays.equals(spec, new String[] { "+parameters" })) {
            return new ParametersOrder();
        }
        return null;
    } 
      
    public interface Processor extends Step, org.lemurproject.galago.tupleflow.Processor<SerializedParameters> {
        public void process(SerializedParameters object) throws IOException;
        public void close() throws IOException;
    }                        
    public interface Source extends Step {
    }
    public static class ParametersOrder implements Order<SerializedParameters> {
        public int hash(SerializedParameters object) {
            int h = 0;
            h += Utility.hash(object.parameters);
            return h;
        } 
        public Comparator<SerializedParameters> greaterThan() {
            return new Comparator<SerializedParameters>() {
                public int compare(SerializedParameters one, SerializedParameters two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.parameters, two.parameters);
                        if(result != 0) break;
                    } while (false);
                    return -result;
                }
            };
        }     
        public Comparator<SerializedParameters> lessThan() {
            return new Comparator<SerializedParameters>() {
                public int compare(SerializedParameters one, SerializedParameters two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.parameters, two.parameters);
                        if(result != 0) break;
                    } while (false);
                    return result;
                }
            };
        }     
        public TypeReader<SerializedParameters> orderedReader(ArrayInput _input) {
            return new ShreddedReader(_input);
        }    

        public TypeReader<SerializedParameters> orderedReader(ArrayInput _input, int bufferSize) {
            return new ShreddedReader(_input, bufferSize);
        }    
        public OrderedWriter<SerializedParameters> orderedWriter(ArrayOutput _output) {
            ShreddedWriter w = new ShreddedWriter(_output);
            return new OrderedWriterClass(w); 
        }                                    
        public static class OrderedWriterClass extends OrderedWriter< SerializedParameters > {
            SerializedParameters last = null;
            ShreddedWriter shreddedWriter = null; 
            
            public OrderedWriterClass(ShreddedWriter s) {
                this.shreddedWriter = s;
            }
            
            public void process(SerializedParameters object) throws IOException {
               boolean processAll = false;
               if (processAll || last == null || 0 != Utility.compare(object.parameters, last.parameters)) { processAll = true; shreddedWriter.processParameters(object.parameters); }
               shreddedWriter.processTuple();
               last = object;
            }           
                 
            public void close() throws IOException {
                shreddedWriter.close();
            }
            
            public Class<SerializedParameters> getInputClass() {
                return SerializedParameters.class;
            }
        } 
        public ReaderSource<SerializedParameters> orderedCombiner(Collection<TypeReader<SerializedParameters>> readers, boolean closeOnExit) {
            ArrayList<ShreddedReader> shreddedReaders = new ArrayList();
            
            for (TypeReader<SerializedParameters> reader : readers) {
                shreddedReaders.add((ShreddedReader)reader);
            }
            
            return new ShreddedCombiner(shreddedReaders, closeOnExit);
        }                  
        public SerializedParameters clone(SerializedParameters object) {
            SerializedParameters result = new SerializedParameters();
            if (object == null) return result;
            result.parameters = object.parameters; 
            return result;
        }                 
        public Class<SerializedParameters> getOrderedClass() {
            return SerializedParameters.class;
        }                           
        public String[] getOrderSpec() {
            return new String[] {"+parameters"};
        }

        public static String[] getSpec() {
            return new String[] {"+parameters"};
        }
        public static String getSpecString() {
            return "+parameters";
        }
                           
        public interface ShreddedProcessor extends Step {
            public void processParameters(String parameters) throws IOException;
            public void processTuple() throws IOException;
            public void close() throws IOException;
        }    
        public interface ShreddedSource extends Step {
        }                                              
        
        public static class ShreddedWriter implements ShreddedProcessor {
            ArrayOutput output;
            ShreddedBuffer buffer = new ShreddedBuffer();
            String lastParameters;
            boolean lastFlush = false;
            
            public ShreddedWriter(ArrayOutput output) {
                this.output = output;
            }                        
            
            public void close() throws IOException {
                flush();
            }
            
            public void processParameters(String parameters) {
                lastParameters = parameters;
                buffer.processParameters(parameters);
            }
            public final void processTuple() throws IOException {
                if (lastFlush) {
                    if(buffer.parameterss.size() == 0) buffer.processParameters(lastParameters);
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
            public final void flushParameters(int pauseIndex) throws IOException {
                while (buffer.getReadIndex() < pauseIndex) {
                    int nextPause = buffer.getParametersEndIndex();
                    int count = nextPause - buffer.getReadIndex();
                    
                    output.writeString(buffer.getParameters());
                    output.writeInt(count);
                    buffer.incrementParameters();
                      
                    flushTuples(nextPause);
                    assert nextPause == buffer.getReadIndex();
                }
            }
            public void flush() throws IOException { 
                flushParameters(buffer.getWriteIndex());
                buffer.reset(); 
                lastFlush = true;
            }                           
        }
        public static class ShreddedBuffer {
            ArrayList<String> parameterss = new ArrayList();
            TIntArrayList parametersTupleIdx = new TIntArrayList();
            int parametersReadIdx = 0;
                            
            int writeTupleIndex = 0;
            int readTupleIndex = 0;
            int batchSize;

            public ShreddedBuffer(int batchSize) {
                this.batchSize = batchSize;

            }                              

            public ShreddedBuffer() {    
                this(10000);
            }                                                                                                                    
            
            public void processParameters(String parameters) {
                parameterss.add(parameters);
                parametersTupleIdx.add(writeTupleIndex);
            }                                      
            public void processTuple() {
                assert parameterss.size() > 0;
                writeTupleIndex++;
            }
            public void resetData() {
                parameterss.clear();
                parametersTupleIdx.clear();
                writeTupleIndex = 0;
            }                  
                                 
            public void resetRead() {
                readTupleIndex = 0;
                parametersReadIdx = 0;
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
            public void incrementParameters() {
                parametersReadIdx++;  
            }                                                                                              

            public void autoIncrementParameters() {
                while (readTupleIndex >= getParametersEndIndex() && readTupleIndex < writeTupleIndex)
                    parametersReadIdx++;
            }                 
            public void incrementTuple() {
                readTupleIndex++;
            }                    
            public int getParametersEndIndex() {
                if ((parametersReadIdx+1) >= parametersTupleIdx.size())
                    return writeTupleIndex;
                return parametersTupleIdx.get(parametersReadIdx+1);
            }
            public int getReadIndex() {
                return readTupleIndex;
            }   

            public int getWriteIndex() {
                return writeTupleIndex;
            } 
            public String getParameters() {
                assert readTupleIndex < writeTupleIndex;
                assert parametersReadIdx < parameterss.size();
                
                return parameterss.get(parametersReadIdx);
            }

            public void copyTuples(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                   output.processTuple();
                   incrementTuple();
                }
            }                                                                           
            public void copyUntilIndexParameters(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                    output.processParameters(getParameters());
                    assert getParametersEndIndex() <= endIndex;
                    copyTuples(getParametersEndIndex(), output);
                    incrementParameters();
                }
            }  
            public void copyUntilParameters(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                while (!isAtEnd()) {
                    if (other != null) {   
                        assert !other.isAtEnd();
                        int c = + Utility.compare(getParameters(), other.getParameters());
                    
                        if (c > 0) {
                            break;   
                        }
                        
                        output.processParameters(getParameters());
                                      
                        copyTuples(getParametersEndIndex(), output);
                    } else {
                        output.processParameters(getParameters());
                        copyTuples(getParametersEndIndex(), output);
                    }
                    incrementParameters();  
                    
               
                }
            }
            public void copyUntil(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                copyUntilParameters(other, output);
            }
            
        }                         
        public static class ShreddedCombiner implements ReaderSource<SerializedParameters>, ShreddedSource {   
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
                } else if (processor instanceof SerializedParameters.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((SerializedParameters.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<SerializedParameters>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<SerializedParameters> getOutputClass() {
                return SerializedParameters.class;
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

            public SerializedParameters read() throws IOException {
                if (uninitialized)
                    initialize();

                SerializedParameters result = null;

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
        public static class ShreddedReader implements Step, Comparable<ShreddedReader>, TypeReader<SerializedParameters>, ShreddedSource {      
            public ShreddedProcessor processor;
            ShreddedBuffer buffer;
            SerializedParameters last = new SerializedParameters();         
            long updateParametersCount = -1;
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
                    result = + Utility.compare(buffer.getParameters(), otherBuffer.getParameters());
                    if(result != 0) break;
                } while (false);                                             
                
                return result;
            }
            
            public final ShreddedBuffer getBuffer() {
                return buffer;
            }                
            
            public final SerializedParameters read() throws IOException {
                if (buffer.isAtEnd()) {
                    fill();             
                
                    if (buffer.isAtEnd()) {
                        return null;
                    }
                }
                      
                assert !buffer.isAtEnd();
                SerializedParameters result = new SerializedParameters();
                
                result.parameters = buffer.getParameters();
                
                buffer.incrementTuple();
                buffer.autoIncrementParameters();
                
                return result;
            }           
            
            public final void fill() throws IOException {
                try {   
                    buffer.reset();
                    
                    if (tupleCount != 0) {
                                                      
                        if(updateParametersCount - tupleCount > 0) {
                            buffer.parameterss.add(last.parameters);
                            buffer.parametersTupleIdx.add((int) (updateParametersCount - tupleCount));
                        }
                        bufferStartCount = tupleCount;
                    }
                    
                    while (!buffer.isFull()) {
                        updateParameters();
                        buffer.processTuple();
                        tupleCount++;
                    }
                } catch(EOFException e) {}
            }

            public final void updateParameters() throws IOException {
                if (updateParametersCount > tupleCount)
                    return;
                     
                last.parameters = input.readString();
                updateParametersCount = tupleCount + input.readInt();
                                      
                buffer.processParameters(last.parameters);
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
                } else if (processor instanceof SerializedParameters.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((SerializedParameters.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<SerializedParameters>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<SerializedParameters> getOutputClass() {
                return SerializedParameters.class;
            }                
        }
        
        public static class DuplicateEliminator implements ShreddedProcessor {
            public ShreddedProcessor processor;
            SerializedParameters last = new SerializedParameters();
            boolean parametersProcess = true;
                                           
            public DuplicateEliminator() {}
            public DuplicateEliminator(ShreddedProcessor processor) {
                this.processor = processor;
            }
            
            public void setShreddedProcessor(ShreddedProcessor processor) {
                this.processor = processor;
            }

            public void processParameters(String parameters) throws IOException {  
                if (parametersProcess || Utility.compare(parameters, last.parameters) != 0) {
                    last.parameters = parameters;
                    processor.processParameters(parameters);
                    parametersProcess = false;
                }
            }  
            
            public void resetParameters() {
                 parametersProcess = true;
            }                                                
                               
            public void processTuple() throws IOException {
                processor.processTuple();
            } 
            
            public void close() throws IOException {
                processor.close();
            }                    
        }
        public static class TupleUnshredder implements ShreddedProcessor {
            SerializedParameters last = new SerializedParameters();
            public org.lemurproject.galago.tupleflow.Processor<SerializedParameters> processor;                               
            
            public TupleUnshredder(SerializedParameters.Processor processor) {
                this.processor = processor;
            }         
            
            public TupleUnshredder(org.lemurproject.galago.tupleflow.Processor<SerializedParameters> processor) {
                this.processor = processor;
            }
            
            public SerializedParameters clone(SerializedParameters object) {
                SerializedParameters result = new SerializedParameters();
                if (object == null) return result;
                result.parameters = object.parameters; 
                return result;
            }                 
            
            public void processParameters(String parameters) throws IOException {
                last.parameters = parameters;
            }   
                
            
            public void processTuple() throws IOException {
                processor.process(clone(last));
            }               
            
            public void close() throws IOException {
                processor.close();
            }
        }     
        public static class TupleShredder implements Processor {
            SerializedParameters last = null;
            public ShreddedProcessor processor;
            
            public TupleShredder(ShreddedProcessor processor) {
                this.processor = processor;
            }                              
            
            public SerializedParameters clone(SerializedParameters object) {
                SerializedParameters result = new SerializedParameters();
                if (object == null) return result;
                result.parameters = object.parameters; 
                return result;
            }                 
            
            public void process(SerializedParameters object) throws IOException {                                                                                                                                                   
                boolean processAll = false;
                if(last == null || Utility.compare(last.parameters, object.parameters) != 0 || processAll) { processor.processParameters(object.parameters); processAll = true; }
                processor.processTuple();                                         
                last = object;
            }
                          
            public Class<SerializedParameters> getInputClass() {
                return SerializedParameters.class;
            }
            
            public void close() throws IOException {
                processor.close();
            }                     
        }
    } 
}    