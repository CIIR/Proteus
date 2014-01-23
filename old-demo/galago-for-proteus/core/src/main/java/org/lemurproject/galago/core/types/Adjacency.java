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


public class Adjacency implements Type<Adjacency> {
    public byte[] source;
    public byte[] destination;
    public double weight; 
    
    public Adjacency() {}
    public Adjacency(byte[] source, byte[] destination, double weight) {
        this.source = source;
        this.destination = destination;
        this.weight = weight;
    }  
    
    public String toString() {
        try {
            return String.format("%s,%s,%f",
                                   new String(source, "UTF-8"), new String(destination, "UTF-8"), weight);
        } catch(UnsupportedEncodingException e) {
            throw new RuntimeException("Couldn't convert string to UTF-8.");
        }
    } 

    public Order<Adjacency> getOrder(String... spec) {
        if (Arrays.equals(spec, new String[] { "+source" })) {
            return new SourceOrder();
        }
        if (Arrays.equals(spec, new String[] { "+source", "+destination" })) {
            return new SourceDestinationOrder();
        }
        if (Arrays.equals(spec, new String[] { "+source", "+weight" })) {
            return new SourceWeightOrder();
        }
        if (Arrays.equals(spec, new String[] { "+source", "-weight" })) {
            return new SourceDescWeightOrder();
        }
        return null;
    } 
      
    public interface Processor extends Step, org.lemurproject.galago.tupleflow.Processor<Adjacency> {
        public void process(Adjacency object) throws IOException;
        public void close() throws IOException;
    }                        
    public interface Source extends Step {
    }
    public static class SourceOrder implements Order<Adjacency> {
        public int hash(Adjacency object) {
            int h = 0;
            h += Utility.hash(object.source);
            return h;
        } 
        public Comparator<Adjacency> greaterThan() {
            return new Comparator<Adjacency>() {
                public int compare(Adjacency one, Adjacency two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.source, two.source);
                        if(result != 0) break;
                    } while (false);
                    return -result;
                }
            };
        }     
        public Comparator<Adjacency> lessThan() {
            return new Comparator<Adjacency>() {
                public int compare(Adjacency one, Adjacency two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.source, two.source);
                        if(result != 0) break;
                    } while (false);
                    return result;
                }
            };
        }     
        public TypeReader<Adjacency> orderedReader(ArrayInput _input) {
            return new ShreddedReader(_input);
        }    

        public TypeReader<Adjacency> orderedReader(ArrayInput _input, int bufferSize) {
            return new ShreddedReader(_input, bufferSize);
        }    
        public OrderedWriter<Adjacency> orderedWriter(ArrayOutput _output) {
            ShreddedWriter w = new ShreddedWriter(_output);
            return new OrderedWriterClass(w); 
        }                                    
        public static class OrderedWriterClass extends OrderedWriter< Adjacency > {
            Adjacency last = null;
            ShreddedWriter shreddedWriter = null; 
            
            public OrderedWriterClass(ShreddedWriter s) {
                this.shreddedWriter = s;
            }
            
            public void process(Adjacency object) throws IOException {
               boolean processAll = false;
               if (processAll || last == null || 0 != Utility.compare(object.source, last.source)) { processAll = true; shreddedWriter.processSource(object.source); }
               shreddedWriter.processTuple(object.destination, object.weight);
               last = object;
            }           
                 
            public void close() throws IOException {
                shreddedWriter.close();
            }
            
            public Class<Adjacency> getInputClass() {
                return Adjacency.class;
            }
        } 
        public ReaderSource<Adjacency> orderedCombiner(Collection<TypeReader<Adjacency>> readers, boolean closeOnExit) {
            ArrayList<ShreddedReader> shreddedReaders = new ArrayList();
            
            for (TypeReader<Adjacency> reader : readers) {
                shreddedReaders.add((ShreddedReader)reader);
            }
            
            return new ShreddedCombiner(shreddedReaders, closeOnExit);
        }                  
        public Adjacency clone(Adjacency object) {
            Adjacency result = new Adjacency();
            if (object == null) return result;
            result.source = object.source; 
            result.destination = object.destination; 
            result.weight = object.weight; 
            return result;
        }                 
        public Class<Adjacency> getOrderedClass() {
            return Adjacency.class;
        }                           
        public String[] getOrderSpec() {
            return new String[] {"+source"};
        }

        public static String[] getSpec() {
            return new String[] {"+source"};
        }
        public static String getSpecString() {
            return "+source";
        }
                           
        public interface ShreddedProcessor extends Step {
            public void processSource(byte[] source) throws IOException;
            public void processTuple(byte[] destination, double weight) throws IOException;
            public void close() throws IOException;
        }    
        public interface ShreddedSource extends Step {
        }                                              
        
        public static class ShreddedWriter implements ShreddedProcessor {
            ArrayOutput output;
            ShreddedBuffer buffer = new ShreddedBuffer();
            byte[] lastSource;
            boolean lastFlush = false;
            
            public ShreddedWriter(ArrayOutput output) {
                this.output = output;
            }                        
            
            public void close() throws IOException {
                flush();
            }
            
            public void processSource(byte[] source) {
                lastSource = source;
                buffer.processSource(source);
            }
            public final void processTuple(byte[] destination, double weight) throws IOException {
                if (lastFlush) {
                    if(buffer.sources.size() == 0) buffer.processSource(lastSource);
                    lastFlush = false;
                }
                buffer.processTuple(destination, weight);
                if (buffer.isFull())
                    flush();
            }
            public final void flushTuples(int pauseIndex) throws IOException {
                
                while (buffer.getReadIndex() < pauseIndex) {
                           
                    output.writeBytes(buffer.getDestination());
                    output.writeDouble(buffer.getWeight());
                    buffer.incrementTuple();
                }
            }  
            public final void flushSource(int pauseIndex) throws IOException {
                while (buffer.getReadIndex() < pauseIndex) {
                    int nextPause = buffer.getSourceEndIndex();
                    int count = nextPause - buffer.getReadIndex();
                    
                    output.writeBytes(buffer.getSource());
                    output.writeInt(count);
                    buffer.incrementSource();
                      
                    flushTuples(nextPause);
                    assert nextPause == buffer.getReadIndex();
                }
            }
            public void flush() throws IOException { 
                flushSource(buffer.getWriteIndex());
                buffer.reset(); 
                lastFlush = true;
            }                           
        }
        public static class ShreddedBuffer {
            ArrayList<byte[]> sources = new ArrayList();
            TIntArrayList sourceTupleIdx = new TIntArrayList();
            int sourceReadIdx = 0;
                            
            byte[][] destinations;
            double[] weights;
            int writeTupleIndex = 0;
            int readTupleIndex = 0;
            int batchSize;

            public ShreddedBuffer(int batchSize) {
                this.batchSize = batchSize;

                destinations = new byte[batchSize][];
                weights = new double[batchSize];
            }                              

            public ShreddedBuffer() {    
                this(10000);
            }                                                                                                                    
            
            public void processSource(byte[] source) {
                sources.add(source);
                sourceTupleIdx.add(writeTupleIndex);
            }                                      
            public void processTuple(byte[] destination, double weight) {
                assert sources.size() > 0;
                destinations[writeTupleIndex] = destination;
                weights[writeTupleIndex] = weight;
                writeTupleIndex++;
            }
            public void resetData() {
                sources.clear();
                sourceTupleIdx.clear();
                writeTupleIndex = 0;
            }                  
                                 
            public void resetRead() {
                readTupleIndex = 0;
                sourceReadIdx = 0;
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
            public void incrementSource() {
                sourceReadIdx++;  
            }                                                                                              

            public void autoIncrementSource() {
                while (readTupleIndex >= getSourceEndIndex() && readTupleIndex < writeTupleIndex)
                    sourceReadIdx++;
            }                 
            public void incrementTuple() {
                readTupleIndex++;
            }                    
            public int getSourceEndIndex() {
                if ((sourceReadIdx+1) >= sourceTupleIdx.size())
                    return writeTupleIndex;
                return sourceTupleIdx.get(sourceReadIdx+1);
            }
            public int getReadIndex() {
                return readTupleIndex;
            }   

            public int getWriteIndex() {
                return writeTupleIndex;
            } 
            public byte[] getSource() {
                assert readTupleIndex < writeTupleIndex;
                assert sourceReadIdx < sources.size();
                
                return sources.get(sourceReadIdx);
            }
            public byte[] getDestination() {
                assert readTupleIndex < writeTupleIndex;
                return destinations[readTupleIndex];
            }                                         
            public double getWeight() {
                assert readTupleIndex < writeTupleIndex;
                return weights[readTupleIndex];
            }                                         
            public void copyTuples(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                   output.processTuple(getDestination(), getWeight());
                   incrementTuple();
                }
            }                                                                           
            public void copyUntilIndexSource(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                    output.processSource(getSource());
                    assert getSourceEndIndex() <= endIndex;
                    copyTuples(getSourceEndIndex(), output);
                    incrementSource();
                }
            }  
            public void copyUntilSource(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                while (!isAtEnd()) {
                    if (other != null) {   
                        assert !other.isAtEnd();
                        int c = + Utility.compare(getSource(), other.getSource());
                    
                        if (c > 0) {
                            break;   
                        }
                        
                        output.processSource(getSource());
                                      
                        copyTuples(getSourceEndIndex(), output);
                    } else {
                        output.processSource(getSource());
                        copyTuples(getSourceEndIndex(), output);
                    }
                    incrementSource();  
                    
               
                }
            }
            public void copyUntil(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                copyUntilSource(other, output);
            }
            
        }                         
        public static class ShreddedCombiner implements ReaderSource<Adjacency>, ShreddedSource {   
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
                } else if (processor instanceof Adjacency.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((Adjacency.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<Adjacency>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<Adjacency> getOutputClass() {
                return Adjacency.class;
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

            public Adjacency read() throws IOException {
                if (uninitialized)
                    initialize();

                Adjacency result = null;

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
        public static class ShreddedReader implements Step, Comparable<ShreddedReader>, TypeReader<Adjacency>, ShreddedSource {      
            public ShreddedProcessor processor;
            ShreddedBuffer buffer;
            Adjacency last = new Adjacency();         
            long updateSourceCount = -1;
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
                    result = + Utility.compare(buffer.getSource(), otherBuffer.getSource());
                    if(result != 0) break;
                } while (false);                                             
                
                return result;
            }
            
            public final ShreddedBuffer getBuffer() {
                return buffer;
            }                
            
            public final Adjacency read() throws IOException {
                if (buffer.isAtEnd()) {
                    fill();             
                
                    if (buffer.isAtEnd()) {
                        return null;
                    }
                }
                      
                assert !buffer.isAtEnd();
                Adjacency result = new Adjacency();
                
                result.source = buffer.getSource();
                result.destination = buffer.getDestination();
                result.weight = buffer.getWeight();
                
                buffer.incrementTuple();
                buffer.autoIncrementSource();
                
                return result;
            }           
            
            public final void fill() throws IOException {
                try {   
                    buffer.reset();
                    
                    if (tupleCount != 0) {
                                                      
                        if(updateSourceCount - tupleCount > 0) {
                            buffer.sources.add(last.source);
                            buffer.sourceTupleIdx.add((int) (updateSourceCount - tupleCount));
                        }
                        bufferStartCount = tupleCount;
                    }
                    
                    while (!buffer.isFull()) {
                        updateSource();
                        buffer.processTuple(input.readBytes(), input.readDouble());
                        tupleCount++;
                    }
                } catch(EOFException e) {}
            }

            public final void updateSource() throws IOException {
                if (updateSourceCount > tupleCount)
                    return;
                     
                last.source = input.readBytes();
                updateSourceCount = tupleCount + input.readInt();
                                      
                buffer.processSource(last.source);
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
                } else if (processor instanceof Adjacency.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((Adjacency.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<Adjacency>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<Adjacency> getOutputClass() {
                return Adjacency.class;
            }                
        }
        
        public static class DuplicateEliminator implements ShreddedProcessor {
            public ShreddedProcessor processor;
            Adjacency last = new Adjacency();
            boolean sourceProcess = true;
                                           
            public DuplicateEliminator() {}
            public DuplicateEliminator(ShreddedProcessor processor) {
                this.processor = processor;
            }
            
            public void setShreddedProcessor(ShreddedProcessor processor) {
                this.processor = processor;
            }

            public void processSource(byte[] source) throws IOException {  
                if (sourceProcess || Utility.compare(source, last.source) != 0) {
                    last.source = source;
                    processor.processSource(source);
                    sourceProcess = false;
                }
            }  
            
            public void resetSource() {
                 sourceProcess = true;
            }                                                
                               
            public void processTuple(byte[] destination, double weight) throws IOException {
                processor.processTuple(destination, weight);
            } 
            
            public void close() throws IOException {
                processor.close();
            }                    
        }
        public static class TupleUnshredder implements ShreddedProcessor {
            Adjacency last = new Adjacency();
            public org.lemurproject.galago.tupleflow.Processor<Adjacency> processor;                               
            
            public TupleUnshredder(Adjacency.Processor processor) {
                this.processor = processor;
            }         
            
            public TupleUnshredder(org.lemurproject.galago.tupleflow.Processor<Adjacency> processor) {
                this.processor = processor;
            }
            
            public Adjacency clone(Adjacency object) {
                Adjacency result = new Adjacency();
                if (object == null) return result;
                result.source = object.source; 
                result.destination = object.destination; 
                result.weight = object.weight; 
                return result;
            }                 
            
            public void processSource(byte[] source) throws IOException {
                last.source = source;
            }   
                
            
            public void processTuple(byte[] destination, double weight) throws IOException {
                last.destination = destination;
                last.weight = weight;
                processor.process(clone(last));
            }               
            
            public void close() throws IOException {
                processor.close();
            }
        }     
        public static class TupleShredder implements Processor {
            Adjacency last = null;
            public ShreddedProcessor processor;
            
            public TupleShredder(ShreddedProcessor processor) {
                this.processor = processor;
            }                              
            
            public Adjacency clone(Adjacency object) {
                Adjacency result = new Adjacency();
                if (object == null) return result;
                result.source = object.source; 
                result.destination = object.destination; 
                result.weight = object.weight; 
                return result;
            }                 
            
            public void process(Adjacency object) throws IOException {                                                                                                                                                   
                boolean processAll = false;
                if(last == null || Utility.compare(last.source, object.source) != 0 || processAll) { processor.processSource(object.source); processAll = true; }
                processor.processTuple(object.destination, object.weight);                                         
                last = object;
            }
                          
            public Class<Adjacency> getInputClass() {
                return Adjacency.class;
            }
            
            public void close() throws IOException {
                processor.close();
            }                     
        }
    } 
    public static class SourceDestinationOrder implements Order<Adjacency> {
        public int hash(Adjacency object) {
            int h = 0;
            h += Utility.hash(object.source);
            h += Utility.hash(object.destination);
            return h;
        } 
        public Comparator<Adjacency> greaterThan() {
            return new Comparator<Adjacency>() {
                public int compare(Adjacency one, Adjacency two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.source, two.source);
                        if(result != 0) break;
                        result = + Utility.compare(one.destination, two.destination);
                        if(result != 0) break;
                    } while (false);
                    return -result;
                }
            };
        }     
        public Comparator<Adjacency> lessThan() {
            return new Comparator<Adjacency>() {
                public int compare(Adjacency one, Adjacency two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.source, two.source);
                        if(result != 0) break;
                        result = + Utility.compare(one.destination, two.destination);
                        if(result != 0) break;
                    } while (false);
                    return result;
                }
            };
        }     
        public TypeReader<Adjacency> orderedReader(ArrayInput _input) {
            return new ShreddedReader(_input);
        }    

        public TypeReader<Adjacency> orderedReader(ArrayInput _input, int bufferSize) {
            return new ShreddedReader(_input, bufferSize);
        }    
        public OrderedWriter<Adjacency> orderedWriter(ArrayOutput _output) {
            ShreddedWriter w = new ShreddedWriter(_output);
            return new OrderedWriterClass(w); 
        }                                    
        public static class OrderedWriterClass extends OrderedWriter< Adjacency > {
            Adjacency last = null;
            ShreddedWriter shreddedWriter = null; 
            
            public OrderedWriterClass(ShreddedWriter s) {
                this.shreddedWriter = s;
            }
            
            public void process(Adjacency object) throws IOException {
               boolean processAll = false;
               if (processAll || last == null || 0 != Utility.compare(object.source, last.source)) { processAll = true; shreddedWriter.processSource(object.source); }
               if (processAll || last == null || 0 != Utility.compare(object.destination, last.destination)) { processAll = true; shreddedWriter.processDestination(object.destination); }
               shreddedWriter.processTuple(object.weight);
               last = object;
            }           
                 
            public void close() throws IOException {
                shreddedWriter.close();
            }
            
            public Class<Adjacency> getInputClass() {
                return Adjacency.class;
            }
        } 
        public ReaderSource<Adjacency> orderedCombiner(Collection<TypeReader<Adjacency>> readers, boolean closeOnExit) {
            ArrayList<ShreddedReader> shreddedReaders = new ArrayList();
            
            for (TypeReader<Adjacency> reader : readers) {
                shreddedReaders.add((ShreddedReader)reader);
            }
            
            return new ShreddedCombiner(shreddedReaders, closeOnExit);
        }                  
        public Adjacency clone(Adjacency object) {
            Adjacency result = new Adjacency();
            if (object == null) return result;
            result.source = object.source; 
            result.destination = object.destination; 
            result.weight = object.weight; 
            return result;
        }                 
        public Class<Adjacency> getOrderedClass() {
            return Adjacency.class;
        }                           
        public String[] getOrderSpec() {
            return new String[] {"+source", "+destination"};
        }

        public static String[] getSpec() {
            return new String[] {"+source", "+destination"};
        }
        public static String getSpecString() {
            return "+source +destination";
        }
                           
        public interface ShreddedProcessor extends Step {
            public void processSource(byte[] source) throws IOException;
            public void processDestination(byte[] destination) throws IOException;
            public void processTuple(double weight) throws IOException;
            public void close() throws IOException;
        }    
        public interface ShreddedSource extends Step {
        }                                              
        
        public static class ShreddedWriter implements ShreddedProcessor {
            ArrayOutput output;
            ShreddedBuffer buffer = new ShreddedBuffer();
            byte[] lastSource;
            byte[] lastDestination;
            boolean lastFlush = false;
            
            public ShreddedWriter(ArrayOutput output) {
                this.output = output;
            }                        
            
            public void close() throws IOException {
                flush();
            }
            
            public void processSource(byte[] source) {
                lastSource = source;
                buffer.processSource(source);
            }
            public void processDestination(byte[] destination) {
                lastDestination = destination;
                buffer.processDestination(destination);
            }
            public final void processTuple(double weight) throws IOException {
                if (lastFlush) {
                    if(buffer.sources.size() == 0) buffer.processSource(lastSource);
                    if(buffer.destinations.size() == 0) buffer.processDestination(lastDestination);
                    lastFlush = false;
                }
                buffer.processTuple(weight);
                if (buffer.isFull())
                    flush();
            }
            public final void flushTuples(int pauseIndex) throws IOException {
                
                while (buffer.getReadIndex() < pauseIndex) {
                           
                    output.writeDouble(buffer.getWeight());
                    buffer.incrementTuple();
                }
            }  
            public final void flushSource(int pauseIndex) throws IOException {
                while (buffer.getReadIndex() < pauseIndex) {
                    int nextPause = buffer.getSourceEndIndex();
                    int count = nextPause - buffer.getReadIndex();
                    
                    output.writeBytes(buffer.getSource());
                    output.writeInt(count);
                    buffer.incrementSource();
                      
                    flushDestination(nextPause);
                    assert nextPause == buffer.getReadIndex();
                }
            }
            public final void flushDestination(int pauseIndex) throws IOException {
                while (buffer.getReadIndex() < pauseIndex) {
                    int nextPause = buffer.getDestinationEndIndex();
                    int count = nextPause - buffer.getReadIndex();
                    
                    output.writeBytes(buffer.getDestination());
                    output.writeInt(count);
                    buffer.incrementDestination();
                      
                    flushTuples(nextPause);
                    assert nextPause == buffer.getReadIndex();
                }
            }
            public void flush() throws IOException { 
                flushSource(buffer.getWriteIndex());
                buffer.reset(); 
                lastFlush = true;
            }                           
        }
        public static class ShreddedBuffer {
            ArrayList<byte[]> sources = new ArrayList();
            ArrayList<byte[]> destinations = new ArrayList();
            TIntArrayList sourceTupleIdx = new TIntArrayList();
            TIntArrayList destinationTupleIdx = new TIntArrayList();
            int sourceReadIdx = 0;
            int destinationReadIdx = 0;
                            
            double[] weights;
            int writeTupleIndex = 0;
            int readTupleIndex = 0;
            int batchSize;

            public ShreddedBuffer(int batchSize) {
                this.batchSize = batchSize;

                weights = new double[batchSize];
            }                              

            public ShreddedBuffer() {    
                this(10000);
            }                                                                                                                    
            
            public void processSource(byte[] source) {
                sources.add(source);
                sourceTupleIdx.add(writeTupleIndex);
            }                                      
            public void processDestination(byte[] destination) {
                destinations.add(destination);
                destinationTupleIdx.add(writeTupleIndex);
            }                                      
            public void processTuple(double weight) {
                assert sources.size() > 0;
                assert destinations.size() > 0;
                weights[writeTupleIndex] = weight;
                writeTupleIndex++;
            }
            public void resetData() {
                sources.clear();
                destinations.clear();
                sourceTupleIdx.clear();
                destinationTupleIdx.clear();
                writeTupleIndex = 0;
            }                  
                                 
            public void resetRead() {
                readTupleIndex = 0;
                sourceReadIdx = 0;
                destinationReadIdx = 0;
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
            public void incrementSource() {
                sourceReadIdx++;  
            }                                                                                              

            public void autoIncrementSource() {
                while (readTupleIndex >= getSourceEndIndex() && readTupleIndex < writeTupleIndex)
                    sourceReadIdx++;
            }                 
            public void incrementDestination() {
                destinationReadIdx++;  
            }                                                                                              

            public void autoIncrementDestination() {
                while (readTupleIndex >= getDestinationEndIndex() && readTupleIndex < writeTupleIndex)
                    destinationReadIdx++;
            }                 
            public void incrementTuple() {
                readTupleIndex++;
            }                    
            public int getSourceEndIndex() {
                if ((sourceReadIdx+1) >= sourceTupleIdx.size())
                    return writeTupleIndex;
                return sourceTupleIdx.get(sourceReadIdx+1);
            }

            public int getDestinationEndIndex() {
                if ((destinationReadIdx+1) >= destinationTupleIdx.size())
                    return writeTupleIndex;
                return destinationTupleIdx.get(destinationReadIdx+1);
            }
            public int getReadIndex() {
                return readTupleIndex;
            }   

            public int getWriteIndex() {
                return writeTupleIndex;
            } 
            public byte[] getSource() {
                assert readTupleIndex < writeTupleIndex;
                assert sourceReadIdx < sources.size();
                
                return sources.get(sourceReadIdx);
            }
            public byte[] getDestination() {
                assert readTupleIndex < writeTupleIndex;
                assert destinationReadIdx < destinations.size();
                
                return destinations.get(destinationReadIdx);
            }
            public double getWeight() {
                assert readTupleIndex < writeTupleIndex;
                return weights[readTupleIndex];
            }                                         
            public void copyTuples(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                   output.processTuple(getWeight());
                   incrementTuple();
                }
            }                                                                           
            public void copyUntilIndexSource(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                    output.processSource(getSource());
                    assert getSourceEndIndex() <= endIndex;
                    copyUntilIndexDestination(getSourceEndIndex(), output);
                    incrementSource();
                }
            } 
            public void copyUntilIndexDestination(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                    output.processDestination(getDestination());
                    assert getDestinationEndIndex() <= endIndex;
                    copyTuples(getDestinationEndIndex(), output);
                    incrementDestination();
                }
            }  
            public void copyUntilSource(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                while (!isAtEnd()) {
                    if (other != null) {   
                        assert !other.isAtEnd();
                        int c = + Utility.compare(getSource(), other.getSource());
                    
                        if (c > 0) {
                            break;   
                        }
                        
                        output.processSource(getSource());
                                      
                        if (c < 0) {
                            copyUntilIndexDestination(getSourceEndIndex(), output);
                        } else if (c == 0) {
                            copyUntilDestination(other, output);
                            autoIncrementSource();
                            break;
                        }
                    } else {
                        output.processSource(getSource());
                        copyUntilIndexDestination(getSourceEndIndex(), output);
                    }
                    incrementSource();  
                    
               
                }
            }
            public void copyUntilDestination(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                while (!isAtEnd()) {
                    if (other != null) {   
                        assert !other.isAtEnd();
                        int c = + Utility.compare(getDestination(), other.getDestination());
                    
                        if (c > 0) {
                            break;   
                        }
                        
                        output.processDestination(getDestination());
                                      
                        copyTuples(getDestinationEndIndex(), output);
                    } else {
                        output.processDestination(getDestination());
                        copyTuples(getDestinationEndIndex(), output);
                    }
                    incrementDestination();  
                    
                    if (getSourceEndIndex() <= readTupleIndex)
                        break;   
                }
            }
            public void copyUntil(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                copyUntilSource(other, output);
            }
            
        }                         
        public static class ShreddedCombiner implements ReaderSource<Adjacency>, ShreddedSource {   
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
                } else if (processor instanceof Adjacency.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((Adjacency.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<Adjacency>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<Adjacency> getOutputClass() {
                return Adjacency.class;
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

            public Adjacency read() throws IOException {
                if (uninitialized)
                    initialize();

                Adjacency result = null;

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
        public static class ShreddedReader implements Step, Comparable<ShreddedReader>, TypeReader<Adjacency>, ShreddedSource {      
            public ShreddedProcessor processor;
            ShreddedBuffer buffer;
            Adjacency last = new Adjacency();         
            long updateSourceCount = -1;
            long updateDestinationCount = -1;
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
                    result = + Utility.compare(buffer.getSource(), otherBuffer.getSource());
                    if(result != 0) break;
                    result = + Utility.compare(buffer.getDestination(), otherBuffer.getDestination());
                    if(result != 0) break;
                } while (false);                                             
                
                return result;
            }
            
            public final ShreddedBuffer getBuffer() {
                return buffer;
            }                
            
            public final Adjacency read() throws IOException {
                if (buffer.isAtEnd()) {
                    fill();             
                
                    if (buffer.isAtEnd()) {
                        return null;
                    }
                }
                      
                assert !buffer.isAtEnd();
                Adjacency result = new Adjacency();
                
                result.source = buffer.getSource();
                result.destination = buffer.getDestination();
                result.weight = buffer.getWeight();
                
                buffer.incrementTuple();
                buffer.autoIncrementSource();
                buffer.autoIncrementDestination();
                
                return result;
            }           
            
            public final void fill() throws IOException {
                try {   
                    buffer.reset();
                    
                    if (tupleCount != 0) {
                                                      
                        if(updateSourceCount - tupleCount > 0) {
                            buffer.sources.add(last.source);
                            buffer.sourceTupleIdx.add((int) (updateSourceCount - tupleCount));
                        }                              
                        if(updateDestinationCount - tupleCount > 0) {
                            buffer.destinations.add(last.destination);
                            buffer.destinationTupleIdx.add((int) (updateDestinationCount - tupleCount));
                        }
                        bufferStartCount = tupleCount;
                    }
                    
                    while (!buffer.isFull()) {
                        updateDestination();
                        buffer.processTuple(input.readDouble());
                        tupleCount++;
                    }
                } catch(EOFException e) {}
            }

            public final void updateSource() throws IOException {
                if (updateSourceCount > tupleCount)
                    return;
                     
                last.source = input.readBytes();
                updateSourceCount = tupleCount + input.readInt();
                                      
                buffer.processSource(last.source);
            }
            public final void updateDestination() throws IOException {
                if (updateDestinationCount > tupleCount)
                    return;
                     
                updateSource();
                last.destination = input.readBytes();
                updateDestinationCount = tupleCount + input.readInt();
                                      
                buffer.processDestination(last.destination);
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
                } else if (processor instanceof Adjacency.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((Adjacency.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<Adjacency>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<Adjacency> getOutputClass() {
                return Adjacency.class;
            }                
        }
        
        public static class DuplicateEliminator implements ShreddedProcessor {
            public ShreddedProcessor processor;
            Adjacency last = new Adjacency();
            boolean sourceProcess = true;
            boolean destinationProcess = true;
                                           
            public DuplicateEliminator() {}
            public DuplicateEliminator(ShreddedProcessor processor) {
                this.processor = processor;
            }
            
            public void setShreddedProcessor(ShreddedProcessor processor) {
                this.processor = processor;
            }

            public void processSource(byte[] source) throws IOException {  
                if (sourceProcess || Utility.compare(source, last.source) != 0) {
                    last.source = source;
                    processor.processSource(source);
            resetDestination();
                    sourceProcess = false;
                }
            }
            public void processDestination(byte[] destination) throws IOException {  
                if (destinationProcess || Utility.compare(destination, last.destination) != 0) {
                    last.destination = destination;
                    processor.processDestination(destination);
                    destinationProcess = false;
                }
            }  
            
            public void resetSource() {
                 sourceProcess = true;
            resetDestination();
            }                                                
            public void resetDestination() {
                 destinationProcess = true;
            }                                                
                               
            public void processTuple(double weight) throws IOException {
                processor.processTuple(weight);
            } 
            
            public void close() throws IOException {
                processor.close();
            }                    
        }
        public static class TupleUnshredder implements ShreddedProcessor {
            Adjacency last = new Adjacency();
            public org.lemurproject.galago.tupleflow.Processor<Adjacency> processor;                               
            
            public TupleUnshredder(Adjacency.Processor processor) {
                this.processor = processor;
            }         
            
            public TupleUnshredder(org.lemurproject.galago.tupleflow.Processor<Adjacency> processor) {
                this.processor = processor;
            }
            
            public Adjacency clone(Adjacency object) {
                Adjacency result = new Adjacency();
                if (object == null) return result;
                result.source = object.source; 
                result.destination = object.destination; 
                result.weight = object.weight; 
                return result;
            }                 
            
            public void processSource(byte[] source) throws IOException {
                last.source = source;
            }   
                
            public void processDestination(byte[] destination) throws IOException {
                last.destination = destination;
            }   
                
            
            public void processTuple(double weight) throws IOException {
                last.weight = weight;
                processor.process(clone(last));
            }               
            
            public void close() throws IOException {
                processor.close();
            }
        }     
        public static class TupleShredder implements Processor {
            Adjacency last = null;
            public ShreddedProcessor processor;
            
            public TupleShredder(ShreddedProcessor processor) {
                this.processor = processor;
            }                              
            
            public Adjacency clone(Adjacency object) {
                Adjacency result = new Adjacency();
                if (object == null) return result;
                result.source = object.source; 
                result.destination = object.destination; 
                result.weight = object.weight; 
                return result;
            }                 
            
            public void process(Adjacency object) throws IOException {                                                                                                                                                   
                boolean processAll = false;
                if(last == null || Utility.compare(last.source, object.source) != 0 || processAll) { processor.processSource(object.source); processAll = true; }
                if(last == null || Utility.compare(last.destination, object.destination) != 0 || processAll) { processor.processDestination(object.destination); processAll = true; }
                processor.processTuple(object.weight);                                         
                last = object;
            }
                          
            public Class<Adjacency> getInputClass() {
                return Adjacency.class;
            }
            
            public void close() throws IOException {
                processor.close();
            }                     
        }
    } 
    public static class SourceWeightOrder implements Order<Adjacency> {
        public int hash(Adjacency object) {
            int h = 0;
            h += Utility.hash(object.source);
            h += Utility.hash(object.weight);
            return h;
        } 
        public Comparator<Adjacency> greaterThan() {
            return new Comparator<Adjacency>() {
                public int compare(Adjacency one, Adjacency two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.source, two.source);
                        if(result != 0) break;
                        result = + Utility.compare(one.weight, two.weight);
                        if(result != 0) break;
                    } while (false);
                    return -result;
                }
            };
        }     
        public Comparator<Adjacency> lessThan() {
            return new Comparator<Adjacency>() {
                public int compare(Adjacency one, Adjacency two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.source, two.source);
                        if(result != 0) break;
                        result = + Utility.compare(one.weight, two.weight);
                        if(result != 0) break;
                    } while (false);
                    return result;
                }
            };
        }     
        public TypeReader<Adjacency> orderedReader(ArrayInput _input) {
            return new ShreddedReader(_input);
        }    

        public TypeReader<Adjacency> orderedReader(ArrayInput _input, int bufferSize) {
            return new ShreddedReader(_input, bufferSize);
        }    
        public OrderedWriter<Adjacency> orderedWriter(ArrayOutput _output) {
            ShreddedWriter w = new ShreddedWriter(_output);
            return new OrderedWriterClass(w); 
        }                                    
        public static class OrderedWriterClass extends OrderedWriter< Adjacency > {
            Adjacency last = null;
            ShreddedWriter shreddedWriter = null; 
            
            public OrderedWriterClass(ShreddedWriter s) {
                this.shreddedWriter = s;
            }
            
            public void process(Adjacency object) throws IOException {
               boolean processAll = false;
               if (processAll || last == null || 0 != Utility.compare(object.source, last.source)) { processAll = true; shreddedWriter.processSource(object.source); }
               if (processAll || last == null || 0 != Utility.compare(object.weight, last.weight)) { processAll = true; shreddedWriter.processWeight(object.weight); }
               shreddedWriter.processTuple(object.destination);
               last = object;
            }           
                 
            public void close() throws IOException {
                shreddedWriter.close();
            }
            
            public Class<Adjacency> getInputClass() {
                return Adjacency.class;
            }
        } 
        public ReaderSource<Adjacency> orderedCombiner(Collection<TypeReader<Adjacency>> readers, boolean closeOnExit) {
            ArrayList<ShreddedReader> shreddedReaders = new ArrayList();
            
            for (TypeReader<Adjacency> reader : readers) {
                shreddedReaders.add((ShreddedReader)reader);
            }
            
            return new ShreddedCombiner(shreddedReaders, closeOnExit);
        }                  
        public Adjacency clone(Adjacency object) {
            Adjacency result = new Adjacency();
            if (object == null) return result;
            result.source = object.source; 
            result.destination = object.destination; 
            result.weight = object.weight; 
            return result;
        }                 
        public Class<Adjacency> getOrderedClass() {
            return Adjacency.class;
        }                           
        public String[] getOrderSpec() {
            return new String[] {"+source", "+weight"};
        }

        public static String[] getSpec() {
            return new String[] {"+source", "+weight"};
        }
        public static String getSpecString() {
            return "+source +weight";
        }
                           
        public interface ShreddedProcessor extends Step {
            public void processSource(byte[] source) throws IOException;
            public void processWeight(double weight) throws IOException;
            public void processTuple(byte[] destination) throws IOException;
            public void close() throws IOException;
        }    
        public interface ShreddedSource extends Step {
        }                                              
        
        public static class ShreddedWriter implements ShreddedProcessor {
            ArrayOutput output;
            ShreddedBuffer buffer = new ShreddedBuffer();
            byte[] lastSource;
            double lastWeight;
            boolean lastFlush = false;
            
            public ShreddedWriter(ArrayOutput output) {
                this.output = output;
            }                        
            
            public void close() throws IOException {
                flush();
            }
            
            public void processSource(byte[] source) {
                lastSource = source;
                buffer.processSource(source);
            }
            public void processWeight(double weight) {
                lastWeight = weight;
                buffer.processWeight(weight);
            }
            public final void processTuple(byte[] destination) throws IOException {
                if (lastFlush) {
                    if(buffer.sources.size() == 0) buffer.processSource(lastSource);
                    if(buffer.weights.size() == 0) buffer.processWeight(lastWeight);
                    lastFlush = false;
                }
                buffer.processTuple(destination);
                if (buffer.isFull())
                    flush();
            }
            public final void flushTuples(int pauseIndex) throws IOException {
                
                while (buffer.getReadIndex() < pauseIndex) {
                           
                    output.writeBytes(buffer.getDestination());
                    buffer.incrementTuple();
                }
            }  
            public final void flushSource(int pauseIndex) throws IOException {
                while (buffer.getReadIndex() < pauseIndex) {
                    int nextPause = buffer.getSourceEndIndex();
                    int count = nextPause - buffer.getReadIndex();
                    
                    output.writeBytes(buffer.getSource());
                    output.writeInt(count);
                    buffer.incrementSource();
                      
                    flushWeight(nextPause);
                    assert nextPause == buffer.getReadIndex();
                }
            }
            public final void flushWeight(int pauseIndex) throws IOException {
                while (buffer.getReadIndex() < pauseIndex) {
                    int nextPause = buffer.getWeightEndIndex();
                    int count = nextPause - buffer.getReadIndex();
                    
                    output.writeDouble(buffer.getWeight());
                    output.writeInt(count);
                    buffer.incrementWeight();
                      
                    flushTuples(nextPause);
                    assert nextPause == buffer.getReadIndex();
                }
            }
            public void flush() throws IOException { 
                flushSource(buffer.getWriteIndex());
                buffer.reset(); 
                lastFlush = true;
            }                           
        }
        public static class ShreddedBuffer {
            ArrayList<byte[]> sources = new ArrayList();
            TDoubleArrayList weights = new TDoubleArrayList();
            TIntArrayList sourceTupleIdx = new TIntArrayList();
            TIntArrayList weightTupleIdx = new TIntArrayList();
            int sourceReadIdx = 0;
            int weightReadIdx = 0;
                            
            byte[][] destinations;
            int writeTupleIndex = 0;
            int readTupleIndex = 0;
            int batchSize;

            public ShreddedBuffer(int batchSize) {
                this.batchSize = batchSize;

                destinations = new byte[batchSize][];
            }                              

            public ShreddedBuffer() {    
                this(10000);
            }                                                                                                                    
            
            public void processSource(byte[] source) {
                sources.add(source);
                sourceTupleIdx.add(writeTupleIndex);
            }                                      
            public void processWeight(double weight) {
                weights.add(weight);
                weightTupleIdx.add(writeTupleIndex);
            }                                      
            public void processTuple(byte[] destination) {
                assert sources.size() > 0;
                assert weights.size() > 0;
                destinations[writeTupleIndex] = destination;
                writeTupleIndex++;
            }
            public void resetData() {
                sources.clear();
                weights.clear();
                sourceTupleIdx.clear();
                weightTupleIdx.clear();
                writeTupleIndex = 0;
            }                  
                                 
            public void resetRead() {
                readTupleIndex = 0;
                sourceReadIdx = 0;
                weightReadIdx = 0;
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
            public void incrementSource() {
                sourceReadIdx++;  
            }                                                                                              

            public void autoIncrementSource() {
                while (readTupleIndex >= getSourceEndIndex() && readTupleIndex < writeTupleIndex)
                    sourceReadIdx++;
            }                 
            public void incrementWeight() {
                weightReadIdx++;  
            }                                                                                              

            public void autoIncrementWeight() {
                while (readTupleIndex >= getWeightEndIndex() && readTupleIndex < writeTupleIndex)
                    weightReadIdx++;
            }                 
            public void incrementTuple() {
                readTupleIndex++;
            }                    
            public int getSourceEndIndex() {
                if ((sourceReadIdx+1) >= sourceTupleIdx.size())
                    return writeTupleIndex;
                return sourceTupleIdx.get(sourceReadIdx+1);
            }

            public int getWeightEndIndex() {
                if ((weightReadIdx+1) >= weightTupleIdx.size())
                    return writeTupleIndex;
                return weightTupleIdx.get(weightReadIdx+1);
            }
            public int getReadIndex() {
                return readTupleIndex;
            }   

            public int getWriteIndex() {
                return writeTupleIndex;
            } 
            public byte[] getSource() {
                assert readTupleIndex < writeTupleIndex;
                assert sourceReadIdx < sources.size();
                
                return sources.get(sourceReadIdx);
            }
            public double getWeight() {
                assert readTupleIndex < writeTupleIndex;
                assert weightReadIdx < weights.size();
                
                return weights.get(weightReadIdx);
            }
            public byte[] getDestination() {
                assert readTupleIndex < writeTupleIndex;
                return destinations[readTupleIndex];
            }                                         
            public void copyTuples(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                   output.processTuple(getDestination());
                   incrementTuple();
                }
            }                                                                           
            public void copyUntilIndexSource(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                    output.processSource(getSource());
                    assert getSourceEndIndex() <= endIndex;
                    copyUntilIndexWeight(getSourceEndIndex(), output);
                    incrementSource();
                }
            } 
            public void copyUntilIndexWeight(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                    output.processWeight(getWeight());
                    assert getWeightEndIndex() <= endIndex;
                    copyTuples(getWeightEndIndex(), output);
                    incrementWeight();
                }
            }  
            public void copyUntilSource(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                while (!isAtEnd()) {
                    if (other != null) {   
                        assert !other.isAtEnd();
                        int c = + Utility.compare(getSource(), other.getSource());
                    
                        if (c > 0) {
                            break;   
                        }
                        
                        output.processSource(getSource());
                                      
                        if (c < 0) {
                            copyUntilIndexWeight(getSourceEndIndex(), output);
                        } else if (c == 0) {
                            copyUntilWeight(other, output);
                            autoIncrementSource();
                            break;
                        }
                    } else {
                        output.processSource(getSource());
                        copyUntilIndexWeight(getSourceEndIndex(), output);
                    }
                    incrementSource();  
                    
               
                }
            }
            public void copyUntilWeight(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                while (!isAtEnd()) {
                    if (other != null) {   
                        assert !other.isAtEnd();
                        int c = + Utility.compare(getWeight(), other.getWeight());
                    
                        if (c > 0) {
                            break;   
                        }
                        
                        output.processWeight(getWeight());
                                      
                        copyTuples(getWeightEndIndex(), output);
                    } else {
                        output.processWeight(getWeight());
                        copyTuples(getWeightEndIndex(), output);
                    }
                    incrementWeight();  
                    
                    if (getSourceEndIndex() <= readTupleIndex)
                        break;   
                }
            }
            public void copyUntil(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                copyUntilSource(other, output);
            }
            
        }                         
        public static class ShreddedCombiner implements ReaderSource<Adjacency>, ShreddedSource {   
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
                } else if (processor instanceof Adjacency.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((Adjacency.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<Adjacency>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<Adjacency> getOutputClass() {
                return Adjacency.class;
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

            public Adjacency read() throws IOException {
                if (uninitialized)
                    initialize();

                Adjacency result = null;

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
        public static class ShreddedReader implements Step, Comparable<ShreddedReader>, TypeReader<Adjacency>, ShreddedSource {      
            public ShreddedProcessor processor;
            ShreddedBuffer buffer;
            Adjacency last = new Adjacency();         
            long updateSourceCount = -1;
            long updateWeightCount = -1;
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
                    result = + Utility.compare(buffer.getSource(), otherBuffer.getSource());
                    if(result != 0) break;
                    result = + Utility.compare(buffer.getWeight(), otherBuffer.getWeight());
                    if(result != 0) break;
                } while (false);                                             
                
                return result;
            }
            
            public final ShreddedBuffer getBuffer() {
                return buffer;
            }                
            
            public final Adjacency read() throws IOException {
                if (buffer.isAtEnd()) {
                    fill();             
                
                    if (buffer.isAtEnd()) {
                        return null;
                    }
                }
                      
                assert !buffer.isAtEnd();
                Adjacency result = new Adjacency();
                
                result.source = buffer.getSource();
                result.weight = buffer.getWeight();
                result.destination = buffer.getDestination();
                
                buffer.incrementTuple();
                buffer.autoIncrementSource();
                buffer.autoIncrementWeight();
                
                return result;
            }           
            
            public final void fill() throws IOException {
                try {   
                    buffer.reset();
                    
                    if (tupleCount != 0) {
                                                      
                        if(updateSourceCount - tupleCount > 0) {
                            buffer.sources.add(last.source);
                            buffer.sourceTupleIdx.add((int) (updateSourceCount - tupleCount));
                        }                              
                        if(updateWeightCount - tupleCount > 0) {
                            buffer.weights.add(last.weight);
                            buffer.weightTupleIdx.add((int) (updateWeightCount - tupleCount));
                        }
                        bufferStartCount = tupleCount;
                    }
                    
                    while (!buffer.isFull()) {
                        updateWeight();
                        buffer.processTuple(input.readBytes());
                        tupleCount++;
                    }
                } catch(EOFException e) {}
            }

            public final void updateSource() throws IOException {
                if (updateSourceCount > tupleCount)
                    return;
                     
                last.source = input.readBytes();
                updateSourceCount = tupleCount + input.readInt();
                                      
                buffer.processSource(last.source);
            }
            public final void updateWeight() throws IOException {
                if (updateWeightCount > tupleCount)
                    return;
                     
                updateSource();
                last.weight = input.readDouble();
                updateWeightCount = tupleCount + input.readInt();
                                      
                buffer.processWeight(last.weight);
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
                } else if (processor instanceof Adjacency.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((Adjacency.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<Adjacency>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<Adjacency> getOutputClass() {
                return Adjacency.class;
            }                
        }
        
        public static class DuplicateEliminator implements ShreddedProcessor {
            public ShreddedProcessor processor;
            Adjacency last = new Adjacency();
            boolean sourceProcess = true;
            boolean weightProcess = true;
                                           
            public DuplicateEliminator() {}
            public DuplicateEliminator(ShreddedProcessor processor) {
                this.processor = processor;
            }
            
            public void setShreddedProcessor(ShreddedProcessor processor) {
                this.processor = processor;
            }

            public void processSource(byte[] source) throws IOException {  
                if (sourceProcess || Utility.compare(source, last.source) != 0) {
                    last.source = source;
                    processor.processSource(source);
            resetWeight();
                    sourceProcess = false;
                }
            }
            public void processWeight(double weight) throws IOException {  
                if (weightProcess || Utility.compare(weight, last.weight) != 0) {
                    last.weight = weight;
                    processor.processWeight(weight);
                    weightProcess = false;
                }
            }  
            
            public void resetSource() {
                 sourceProcess = true;
            resetWeight();
            }                                                
            public void resetWeight() {
                 weightProcess = true;
            }                                                
                               
            public void processTuple(byte[] destination) throws IOException {
                processor.processTuple(destination);
            } 
            
            public void close() throws IOException {
                processor.close();
            }                    
        }
        public static class TupleUnshredder implements ShreddedProcessor {
            Adjacency last = new Adjacency();
            public org.lemurproject.galago.tupleflow.Processor<Adjacency> processor;                               
            
            public TupleUnshredder(Adjacency.Processor processor) {
                this.processor = processor;
            }         
            
            public TupleUnshredder(org.lemurproject.galago.tupleflow.Processor<Adjacency> processor) {
                this.processor = processor;
            }
            
            public Adjacency clone(Adjacency object) {
                Adjacency result = new Adjacency();
                if (object == null) return result;
                result.source = object.source; 
                result.destination = object.destination; 
                result.weight = object.weight; 
                return result;
            }                 
            
            public void processSource(byte[] source) throws IOException {
                last.source = source;
            }   
                
            public void processWeight(double weight) throws IOException {
                last.weight = weight;
            }   
                
            
            public void processTuple(byte[] destination) throws IOException {
                last.destination = destination;
                processor.process(clone(last));
            }               
            
            public void close() throws IOException {
                processor.close();
            }
        }     
        public static class TupleShredder implements Processor {
            Adjacency last = null;
            public ShreddedProcessor processor;
            
            public TupleShredder(ShreddedProcessor processor) {
                this.processor = processor;
            }                              
            
            public Adjacency clone(Adjacency object) {
                Adjacency result = new Adjacency();
                if (object == null) return result;
                result.source = object.source; 
                result.destination = object.destination; 
                result.weight = object.weight; 
                return result;
            }                 
            
            public void process(Adjacency object) throws IOException {                                                                                                                                                   
                boolean processAll = false;
                if(last == null || Utility.compare(last.source, object.source) != 0 || processAll) { processor.processSource(object.source); processAll = true; }
                if(last == null || Utility.compare(last.weight, object.weight) != 0 || processAll) { processor.processWeight(object.weight); processAll = true; }
                processor.processTuple(object.destination);                                         
                last = object;
            }
                          
            public Class<Adjacency> getInputClass() {
                return Adjacency.class;
            }
            
            public void close() throws IOException {
                processor.close();
            }                     
        }
    } 
    public static class SourceDescWeightOrder implements Order<Adjacency> {
        public int hash(Adjacency object) {
            int h = 0;
            h += Utility.hash(object.source);
            h += Utility.hash(object.weight);
            return h;
        } 
        public Comparator<Adjacency> greaterThan() {
            return new Comparator<Adjacency>() {
                public int compare(Adjacency one, Adjacency two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.source, two.source);
                        if(result != 0) break;
                        result = - Utility.compare(one.weight, two.weight);
                        if(result != 0) break;
                    } while (false);
                    return -result;
                }
            };
        }     
        public Comparator<Adjacency> lessThan() {
            return new Comparator<Adjacency>() {
                public int compare(Adjacency one, Adjacency two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.source, two.source);
                        if(result != 0) break;
                        result = - Utility.compare(one.weight, two.weight);
                        if(result != 0) break;
                    } while (false);
                    return result;
                }
            };
        }     
        public TypeReader<Adjacency> orderedReader(ArrayInput _input) {
            return new ShreddedReader(_input);
        }    

        public TypeReader<Adjacency> orderedReader(ArrayInput _input, int bufferSize) {
            return new ShreddedReader(_input, bufferSize);
        }    
        public OrderedWriter<Adjacency> orderedWriter(ArrayOutput _output) {
            ShreddedWriter w = new ShreddedWriter(_output);
            return new OrderedWriterClass(w); 
        }                                    
        public static class OrderedWriterClass extends OrderedWriter< Adjacency > {
            Adjacency last = null;
            ShreddedWriter shreddedWriter = null; 
            
            public OrderedWriterClass(ShreddedWriter s) {
                this.shreddedWriter = s;
            }
            
            public void process(Adjacency object) throws IOException {
               boolean processAll = false;
               if (processAll || last == null || 0 != Utility.compare(object.source, last.source)) { processAll = true; shreddedWriter.processSource(object.source); }
               if (processAll || last == null || 0 != Utility.compare(object.weight, last.weight)) { processAll = true; shreddedWriter.processWeight(object.weight); }
               shreddedWriter.processTuple(object.destination);
               last = object;
            }           
                 
            public void close() throws IOException {
                shreddedWriter.close();
            }
            
            public Class<Adjacency> getInputClass() {
                return Adjacency.class;
            }
        } 
        public ReaderSource<Adjacency> orderedCombiner(Collection<TypeReader<Adjacency>> readers, boolean closeOnExit) {
            ArrayList<ShreddedReader> shreddedReaders = new ArrayList();
            
            for (TypeReader<Adjacency> reader : readers) {
                shreddedReaders.add((ShreddedReader)reader);
            }
            
            return new ShreddedCombiner(shreddedReaders, closeOnExit);
        }                  
        public Adjacency clone(Adjacency object) {
            Adjacency result = new Adjacency();
            if (object == null) return result;
            result.source = object.source; 
            result.destination = object.destination; 
            result.weight = object.weight; 
            return result;
        }                 
        public Class<Adjacency> getOrderedClass() {
            return Adjacency.class;
        }                           
        public String[] getOrderSpec() {
            return new String[] {"+source", "-weight"};
        }

        public static String[] getSpec() {
            return new String[] {"+source", "-weight"};
        }
        public static String getSpecString() {
            return "+source -weight";
        }
                           
        public interface ShreddedProcessor extends Step {
            public void processSource(byte[] source) throws IOException;
            public void processWeight(double weight) throws IOException;
            public void processTuple(byte[] destination) throws IOException;
            public void close() throws IOException;
        }    
        public interface ShreddedSource extends Step {
        }                                              
        
        public static class ShreddedWriter implements ShreddedProcessor {
            ArrayOutput output;
            ShreddedBuffer buffer = new ShreddedBuffer();
            byte[] lastSource;
            double lastWeight;
            boolean lastFlush = false;
            
            public ShreddedWriter(ArrayOutput output) {
                this.output = output;
            }                        
            
            public void close() throws IOException {
                flush();
            }
            
            public void processSource(byte[] source) {
                lastSource = source;
                buffer.processSource(source);
            }
            public void processWeight(double weight) {
                lastWeight = weight;
                buffer.processWeight(weight);
            }
            public final void processTuple(byte[] destination) throws IOException {
                if (lastFlush) {
                    if(buffer.sources.size() == 0) buffer.processSource(lastSource);
                    if(buffer.weights.size() == 0) buffer.processWeight(lastWeight);
                    lastFlush = false;
                }
                buffer.processTuple(destination);
                if (buffer.isFull())
                    flush();
            }
            public final void flushTuples(int pauseIndex) throws IOException {
                
                while (buffer.getReadIndex() < pauseIndex) {
                           
                    output.writeBytes(buffer.getDestination());
                    buffer.incrementTuple();
                }
            }  
            public final void flushSource(int pauseIndex) throws IOException {
                while (buffer.getReadIndex() < pauseIndex) {
                    int nextPause = buffer.getSourceEndIndex();
                    int count = nextPause - buffer.getReadIndex();
                    
                    output.writeBytes(buffer.getSource());
                    output.writeInt(count);
                    buffer.incrementSource();
                      
                    flushWeight(nextPause);
                    assert nextPause == buffer.getReadIndex();
                }
            }
            public final void flushWeight(int pauseIndex) throws IOException {
                while (buffer.getReadIndex() < pauseIndex) {
                    int nextPause = buffer.getWeightEndIndex();
                    int count = nextPause - buffer.getReadIndex();
                    
                    output.writeDouble(buffer.getWeight());
                    output.writeInt(count);
                    buffer.incrementWeight();
                      
                    flushTuples(nextPause);
                    assert nextPause == buffer.getReadIndex();
                }
            }
            public void flush() throws IOException { 
                flushSource(buffer.getWriteIndex());
                buffer.reset(); 
                lastFlush = true;
            }                           
        }
        public static class ShreddedBuffer {
            ArrayList<byte[]> sources = new ArrayList();
            TDoubleArrayList weights = new TDoubleArrayList();
            TIntArrayList sourceTupleIdx = new TIntArrayList();
            TIntArrayList weightTupleIdx = new TIntArrayList();
            int sourceReadIdx = 0;
            int weightReadIdx = 0;
                            
            byte[][] destinations;
            int writeTupleIndex = 0;
            int readTupleIndex = 0;
            int batchSize;

            public ShreddedBuffer(int batchSize) {
                this.batchSize = batchSize;

                destinations = new byte[batchSize][];
            }                              

            public ShreddedBuffer() {    
                this(10000);
            }                                                                                                                    
            
            public void processSource(byte[] source) {
                sources.add(source);
                sourceTupleIdx.add(writeTupleIndex);
            }                                      
            public void processWeight(double weight) {
                weights.add(weight);
                weightTupleIdx.add(writeTupleIndex);
            }                                      
            public void processTuple(byte[] destination) {
                assert sources.size() > 0;
                assert weights.size() > 0;
                destinations[writeTupleIndex] = destination;
                writeTupleIndex++;
            }
            public void resetData() {
                sources.clear();
                weights.clear();
                sourceTupleIdx.clear();
                weightTupleIdx.clear();
                writeTupleIndex = 0;
            }                  
                                 
            public void resetRead() {
                readTupleIndex = 0;
                sourceReadIdx = 0;
                weightReadIdx = 0;
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
            public void incrementSource() {
                sourceReadIdx++;  
            }                                                                                              

            public void autoIncrementSource() {
                while (readTupleIndex >= getSourceEndIndex() && readTupleIndex < writeTupleIndex)
                    sourceReadIdx++;
            }                 
            public void incrementWeight() {
                weightReadIdx++;  
            }                                                                                              

            public void autoIncrementWeight() {
                while (readTupleIndex >= getWeightEndIndex() && readTupleIndex < writeTupleIndex)
                    weightReadIdx++;
            }                 
            public void incrementTuple() {
                readTupleIndex++;
            }                    
            public int getSourceEndIndex() {
                if ((sourceReadIdx+1) >= sourceTupleIdx.size())
                    return writeTupleIndex;
                return sourceTupleIdx.get(sourceReadIdx+1);
            }

            public int getWeightEndIndex() {
                if ((weightReadIdx+1) >= weightTupleIdx.size())
                    return writeTupleIndex;
                return weightTupleIdx.get(weightReadIdx+1);
            }
            public int getReadIndex() {
                return readTupleIndex;
            }   

            public int getWriteIndex() {
                return writeTupleIndex;
            } 
            public byte[] getSource() {
                assert readTupleIndex < writeTupleIndex;
                assert sourceReadIdx < sources.size();
                
                return sources.get(sourceReadIdx);
            }
            public double getWeight() {
                assert readTupleIndex < writeTupleIndex;
                assert weightReadIdx < weights.size();
                
                return weights.get(weightReadIdx);
            }
            public byte[] getDestination() {
                assert readTupleIndex < writeTupleIndex;
                return destinations[readTupleIndex];
            }                                         
            public void copyTuples(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                   output.processTuple(getDestination());
                   incrementTuple();
                }
            }                                                                           
            public void copyUntilIndexSource(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                    output.processSource(getSource());
                    assert getSourceEndIndex() <= endIndex;
                    copyUntilIndexWeight(getSourceEndIndex(), output);
                    incrementSource();
                }
            } 
            public void copyUntilIndexWeight(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                    output.processWeight(getWeight());
                    assert getWeightEndIndex() <= endIndex;
                    copyTuples(getWeightEndIndex(), output);
                    incrementWeight();
                }
            }  
            public void copyUntilSource(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                while (!isAtEnd()) {
                    if (other != null) {   
                        assert !other.isAtEnd();
                        int c = + Utility.compare(getSource(), other.getSource());
                    
                        if (c > 0) {
                            break;   
                        }
                        
                        output.processSource(getSource());
                                      
                        if (c < 0) {
                            copyUntilIndexWeight(getSourceEndIndex(), output);
                        } else if (c == 0) {
                            copyUntilWeight(other, output);
                            autoIncrementSource();
                            break;
                        }
                    } else {
                        output.processSource(getSource());
                        copyUntilIndexWeight(getSourceEndIndex(), output);
                    }
                    incrementSource();  
                    
               
                }
            }
            public void copyUntilWeight(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                while (!isAtEnd()) {
                    if (other != null) {   
                        assert !other.isAtEnd();
                        int c = - Utility.compare(getWeight(), other.getWeight());
                    
                        if (c > 0) {
                            break;   
                        }
                        
                        output.processWeight(getWeight());
                                      
                        copyTuples(getWeightEndIndex(), output);
                    } else {
                        output.processWeight(getWeight());
                        copyTuples(getWeightEndIndex(), output);
                    }
                    incrementWeight();  
                    
                    if (getSourceEndIndex() <= readTupleIndex)
                        break;   
                }
            }
            public void copyUntil(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                copyUntilSource(other, output);
            }
            
        }                         
        public static class ShreddedCombiner implements ReaderSource<Adjacency>, ShreddedSource {   
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
                } else if (processor instanceof Adjacency.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((Adjacency.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<Adjacency>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<Adjacency> getOutputClass() {
                return Adjacency.class;
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

            public Adjacency read() throws IOException {
                if (uninitialized)
                    initialize();

                Adjacency result = null;

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
        public static class ShreddedReader implements Step, Comparable<ShreddedReader>, TypeReader<Adjacency>, ShreddedSource {      
            public ShreddedProcessor processor;
            ShreddedBuffer buffer;
            Adjacency last = new Adjacency();         
            long updateSourceCount = -1;
            long updateWeightCount = -1;
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
                    result = + Utility.compare(buffer.getSource(), otherBuffer.getSource());
                    if(result != 0) break;
                    result = - Utility.compare(buffer.getWeight(), otherBuffer.getWeight());
                    if(result != 0) break;
                } while (false);                                             
                
                return result;
            }
            
            public final ShreddedBuffer getBuffer() {
                return buffer;
            }                
            
            public final Adjacency read() throws IOException {
                if (buffer.isAtEnd()) {
                    fill();             
                
                    if (buffer.isAtEnd()) {
                        return null;
                    }
                }
                      
                assert !buffer.isAtEnd();
                Adjacency result = new Adjacency();
                
                result.source = buffer.getSource();
                result.weight = buffer.getWeight();
                result.destination = buffer.getDestination();
                
                buffer.incrementTuple();
                buffer.autoIncrementSource();
                buffer.autoIncrementWeight();
                
                return result;
            }           
            
            public final void fill() throws IOException {
                try {   
                    buffer.reset();
                    
                    if (tupleCount != 0) {
                                                      
                        if(updateSourceCount - tupleCount > 0) {
                            buffer.sources.add(last.source);
                            buffer.sourceTupleIdx.add((int) (updateSourceCount - tupleCount));
                        }                              
                        if(updateWeightCount - tupleCount > 0) {
                            buffer.weights.add(last.weight);
                            buffer.weightTupleIdx.add((int) (updateWeightCount - tupleCount));
                        }
                        bufferStartCount = tupleCount;
                    }
                    
                    while (!buffer.isFull()) {
                        updateWeight();
                        buffer.processTuple(input.readBytes());
                        tupleCount++;
                    }
                } catch(EOFException e) {}
            }

            public final void updateSource() throws IOException {
                if (updateSourceCount > tupleCount)
                    return;
                     
                last.source = input.readBytes();
                updateSourceCount = tupleCount + input.readInt();
                                      
                buffer.processSource(last.source);
            }
            public final void updateWeight() throws IOException {
                if (updateWeightCount > tupleCount)
                    return;
                     
                updateSource();
                last.weight = input.readDouble();
                updateWeightCount = tupleCount + input.readInt();
                                      
                buffer.processWeight(last.weight);
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
                } else if (processor instanceof Adjacency.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((Adjacency.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<Adjacency>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<Adjacency> getOutputClass() {
                return Adjacency.class;
            }                
        }
        
        public static class DuplicateEliminator implements ShreddedProcessor {
            public ShreddedProcessor processor;
            Adjacency last = new Adjacency();
            boolean sourceProcess = true;
            boolean weightProcess = true;
                                           
            public DuplicateEliminator() {}
            public DuplicateEliminator(ShreddedProcessor processor) {
                this.processor = processor;
            }
            
            public void setShreddedProcessor(ShreddedProcessor processor) {
                this.processor = processor;
            }

            public void processSource(byte[] source) throws IOException {  
                if (sourceProcess || Utility.compare(source, last.source) != 0) {
                    last.source = source;
                    processor.processSource(source);
            resetWeight();
                    sourceProcess = false;
                }
            }
            public void processWeight(double weight) throws IOException {  
                if (weightProcess || Utility.compare(weight, last.weight) != 0) {
                    last.weight = weight;
                    processor.processWeight(weight);
                    weightProcess = false;
                }
            }  
            
            public void resetSource() {
                 sourceProcess = true;
            resetWeight();
            }                                                
            public void resetWeight() {
                 weightProcess = true;
            }                                                
                               
            public void processTuple(byte[] destination) throws IOException {
                processor.processTuple(destination);
            } 
            
            public void close() throws IOException {
                processor.close();
            }                    
        }
        public static class TupleUnshredder implements ShreddedProcessor {
            Adjacency last = new Adjacency();
            public org.lemurproject.galago.tupleflow.Processor<Adjacency> processor;                               
            
            public TupleUnshredder(Adjacency.Processor processor) {
                this.processor = processor;
            }         
            
            public TupleUnshredder(org.lemurproject.galago.tupleflow.Processor<Adjacency> processor) {
                this.processor = processor;
            }
            
            public Adjacency clone(Adjacency object) {
                Adjacency result = new Adjacency();
                if (object == null) return result;
                result.source = object.source; 
                result.destination = object.destination; 
                result.weight = object.weight; 
                return result;
            }                 
            
            public void processSource(byte[] source) throws IOException {
                last.source = source;
            }   
                
            public void processWeight(double weight) throws IOException {
                last.weight = weight;
            }   
                
            
            public void processTuple(byte[] destination) throws IOException {
                last.destination = destination;
                processor.process(clone(last));
            }               
            
            public void close() throws IOException {
                processor.close();
            }
        }     
        public static class TupleShredder implements Processor {
            Adjacency last = null;
            public ShreddedProcessor processor;
            
            public TupleShredder(ShreddedProcessor processor) {
                this.processor = processor;
            }                              
            
            public Adjacency clone(Adjacency object) {
                Adjacency result = new Adjacency();
                if (object == null) return result;
                result.source = object.source; 
                result.destination = object.destination; 
                result.weight = object.weight; 
                return result;
            }                 
            
            public void process(Adjacency object) throws IOException {                                                                                                                                                   
                boolean processAll = false;
                if(last == null || Utility.compare(last.source, object.source) != 0 || processAll) { processor.processSource(object.source); processAll = true; }
                if(last == null || Utility.compare(last.weight, object.weight) != 0 || processAll) { processor.processWeight(object.weight); processAll = true; }
                processor.processTuple(object.destination);                                         
                last = object;
            }
                          
            public Class<Adjacency> getInputClass() {
                return Adjacency.class;
            }
            
            public void close() throws IOException {
                processor.close();
            }                     
        }
    } 
}    