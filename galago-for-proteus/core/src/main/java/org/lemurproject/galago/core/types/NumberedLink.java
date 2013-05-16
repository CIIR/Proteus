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


public class NumberedLink implements Type<NumberedLink> {
    public long source;
    public long destination; 
    
    public NumberedLink() {}
    public NumberedLink(long source, long destination) {
        this.source = source;
        this.destination = destination;
    }  
    
    public String toString() {
            return String.format("%d,%d",
                                   source, destination);
    } 

    public Order<NumberedLink> getOrder(String... spec) {
        if (Arrays.equals(spec, new String[] { "+source" })) {
            return new SourceOrder();
        }
        if (Arrays.equals(spec, new String[] { "+destination" })) {
            return new DestinationOrder();
        }
        return null;
    } 
      
    public interface Processor extends Step, org.lemurproject.galago.tupleflow.Processor<NumberedLink> {
        public void process(NumberedLink object) throws IOException;
        public void close() throws IOException;
    }                        
    public interface Source extends Step {
    }
    public static class SourceOrder implements Order<NumberedLink> {
        public int hash(NumberedLink object) {
            int h = 0;
            h += Utility.hash(object.source);
            return h;
        } 
        public Comparator<NumberedLink> greaterThan() {
            return new Comparator<NumberedLink>() {
                public int compare(NumberedLink one, NumberedLink two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.source, two.source);
                        if(result != 0) break;
                    } while (false);
                    return -result;
                }
            };
        }     
        public Comparator<NumberedLink> lessThan() {
            return new Comparator<NumberedLink>() {
                public int compare(NumberedLink one, NumberedLink two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.source, two.source);
                        if(result != 0) break;
                    } while (false);
                    return result;
                }
            };
        }     
        public TypeReader<NumberedLink> orderedReader(ArrayInput _input) {
            return new ShreddedReader(_input);
        }    

        public TypeReader<NumberedLink> orderedReader(ArrayInput _input, int bufferSize) {
            return new ShreddedReader(_input, bufferSize);
        }    
        public OrderedWriter<NumberedLink> orderedWriter(ArrayOutput _output) {
            ShreddedWriter w = new ShreddedWriter(_output);
            return new OrderedWriterClass(w); 
        }                                    
        public static class OrderedWriterClass extends OrderedWriter< NumberedLink > {
            NumberedLink last = null;
            ShreddedWriter shreddedWriter = null; 
            
            public OrderedWriterClass(ShreddedWriter s) {
                this.shreddedWriter = s;
            }
            
            public void process(NumberedLink object) throws IOException {
               boolean processAll = false;
               if (processAll || last == null || 0 != Utility.compare(object.source, last.source)) { processAll = true; shreddedWriter.processSource(object.source); }
               shreddedWriter.processTuple(object.destination);
               last = object;
            }           
                 
            public void close() throws IOException {
                shreddedWriter.close();
            }
            
            public Class<NumberedLink> getInputClass() {
                return NumberedLink.class;
            }
        } 
        public ReaderSource<NumberedLink> orderedCombiner(Collection<TypeReader<NumberedLink>> readers, boolean closeOnExit) {
            ArrayList<ShreddedReader> shreddedReaders = new ArrayList();
            
            for (TypeReader<NumberedLink> reader : readers) {
                shreddedReaders.add((ShreddedReader)reader);
            }
            
            return new ShreddedCombiner(shreddedReaders, closeOnExit);
        }                  
        public NumberedLink clone(NumberedLink object) {
            NumberedLink result = new NumberedLink();
            if (object == null) return result;
            result.source = object.source; 
            result.destination = object.destination; 
            return result;
        }                 
        public Class<NumberedLink> getOrderedClass() {
            return NumberedLink.class;
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
            public void processSource(long source) throws IOException;
            public void processTuple(long destination) throws IOException;
            public void close() throws IOException;
        }    
        public interface ShreddedSource extends Step {
        }                                              
        
        public static class ShreddedWriter implements ShreddedProcessor {
            ArrayOutput output;
            ShreddedBuffer buffer = new ShreddedBuffer();
            long lastSource;
            boolean lastFlush = false;
            
            public ShreddedWriter(ArrayOutput output) {
                this.output = output;
            }                        
            
            public void close() throws IOException {
                flush();
            }
            
            public void processSource(long source) {
                lastSource = source;
                buffer.processSource(source);
            }
            public final void processTuple(long destination) throws IOException {
                if (lastFlush) {
                    if(buffer.sources.size() == 0) buffer.processSource(lastSource);
                    lastFlush = false;
                }
                buffer.processTuple(destination);
                if (buffer.isFull())
                    flush();
            }
            public final void flushTuples(int pauseIndex) throws IOException {
                
                while (buffer.getReadIndex() < pauseIndex) {
                           
                    output.writeLong(buffer.getDestination());
                    buffer.incrementTuple();
                }
            }  
            public final void flushSource(int pauseIndex) throws IOException {
                while (buffer.getReadIndex() < pauseIndex) {
                    int nextPause = buffer.getSourceEndIndex();
                    int count = nextPause - buffer.getReadIndex();
                    
                    output.writeLong(buffer.getSource());
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
            TLongArrayList sources = new TLongArrayList();
            TIntArrayList sourceTupleIdx = new TIntArrayList();
            int sourceReadIdx = 0;
                            
            long[] destinations;
            int writeTupleIndex = 0;
            int readTupleIndex = 0;
            int batchSize;

            public ShreddedBuffer(int batchSize) {
                this.batchSize = batchSize;

                destinations = new long[batchSize];
            }                              

            public ShreddedBuffer() {    
                this(10000);
            }                                                                                                                    
            
            public void processSource(long source) {
                sources.add(source);
                sourceTupleIdx.add(writeTupleIndex);
            }                                      
            public void processTuple(long destination) {
                assert sources.size() > 0;
                destinations[writeTupleIndex] = destination;
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
            public long getSource() {
                assert readTupleIndex < writeTupleIndex;
                assert sourceReadIdx < sources.size();
                
                return sources.get(sourceReadIdx);
            }
            public long getDestination() {
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
        public static class ShreddedCombiner implements ReaderSource<NumberedLink>, ShreddedSource {   
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
                } else if (processor instanceof NumberedLink.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((NumberedLink.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<NumberedLink>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<NumberedLink> getOutputClass() {
                return NumberedLink.class;
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

            public NumberedLink read() throws IOException {
                if (uninitialized)
                    initialize();

                NumberedLink result = null;

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
        public static class ShreddedReader implements Step, Comparable<ShreddedReader>, TypeReader<NumberedLink>, ShreddedSource {      
            public ShreddedProcessor processor;
            ShreddedBuffer buffer;
            NumberedLink last = new NumberedLink();         
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
            
            public final NumberedLink read() throws IOException {
                if (buffer.isAtEnd()) {
                    fill();             
                
                    if (buffer.isAtEnd()) {
                        return null;
                    }
                }
                      
                assert !buffer.isAtEnd();
                NumberedLink result = new NumberedLink();
                
                result.source = buffer.getSource();
                result.destination = buffer.getDestination();
                
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
                        buffer.processTuple(input.readLong());
                        tupleCount++;
                    }
                } catch(EOFException e) {}
            }

            public final void updateSource() throws IOException {
                if (updateSourceCount > tupleCount)
                    return;
                     
                last.source = input.readLong();
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
                } else if (processor instanceof NumberedLink.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((NumberedLink.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<NumberedLink>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<NumberedLink> getOutputClass() {
                return NumberedLink.class;
            }                
        }
        
        public static class DuplicateEliminator implements ShreddedProcessor {
            public ShreddedProcessor processor;
            NumberedLink last = new NumberedLink();
            boolean sourceProcess = true;
                                           
            public DuplicateEliminator() {}
            public DuplicateEliminator(ShreddedProcessor processor) {
                this.processor = processor;
            }
            
            public void setShreddedProcessor(ShreddedProcessor processor) {
                this.processor = processor;
            }

            public void processSource(long source) throws IOException {  
                if (sourceProcess || Utility.compare(source, last.source) != 0) {
                    last.source = source;
                    processor.processSource(source);
                    sourceProcess = false;
                }
            }  
            
            public void resetSource() {
                 sourceProcess = true;
            }                                                
                               
            public void processTuple(long destination) throws IOException {
                processor.processTuple(destination);
            } 
            
            public void close() throws IOException {
                processor.close();
            }                    
        }
        public static class TupleUnshredder implements ShreddedProcessor {
            NumberedLink last = new NumberedLink();
            public org.lemurproject.galago.tupleflow.Processor<NumberedLink> processor;                               
            
            public TupleUnshredder(NumberedLink.Processor processor) {
                this.processor = processor;
            }         
            
            public TupleUnshredder(org.lemurproject.galago.tupleflow.Processor<NumberedLink> processor) {
                this.processor = processor;
            }
            
            public NumberedLink clone(NumberedLink object) {
                NumberedLink result = new NumberedLink();
                if (object == null) return result;
                result.source = object.source; 
                result.destination = object.destination; 
                return result;
            }                 
            
            public void processSource(long source) throws IOException {
                last.source = source;
            }   
                
            
            public void processTuple(long destination) throws IOException {
                last.destination = destination;
                processor.process(clone(last));
            }               
            
            public void close() throws IOException {
                processor.close();
            }
        }     
        public static class TupleShredder implements Processor {
            NumberedLink last = null;
            public ShreddedProcessor processor;
            
            public TupleShredder(ShreddedProcessor processor) {
                this.processor = processor;
            }                              
            
            public NumberedLink clone(NumberedLink object) {
                NumberedLink result = new NumberedLink();
                if (object == null) return result;
                result.source = object.source; 
                result.destination = object.destination; 
                return result;
            }                 
            
            public void process(NumberedLink object) throws IOException {                                                                                                                                                   
                boolean processAll = false;
                if(last == null || Utility.compare(last.source, object.source) != 0 || processAll) { processor.processSource(object.source); processAll = true; }
                processor.processTuple(object.destination);                                         
                last = object;
            }
                          
            public Class<NumberedLink> getInputClass() {
                return NumberedLink.class;
            }
            
            public void close() throws IOException {
                processor.close();
            }                     
        }
    } 
    public static class DestinationOrder implements Order<NumberedLink> {
        public int hash(NumberedLink object) {
            int h = 0;
            h += Utility.hash(object.destination);
            return h;
        } 
        public Comparator<NumberedLink> greaterThan() {
            return new Comparator<NumberedLink>() {
                public int compare(NumberedLink one, NumberedLink two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.destination, two.destination);
                        if(result != 0) break;
                    } while (false);
                    return -result;
                }
            };
        }     
        public Comparator<NumberedLink> lessThan() {
            return new Comparator<NumberedLink>() {
                public int compare(NumberedLink one, NumberedLink two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.destination, two.destination);
                        if(result != 0) break;
                    } while (false);
                    return result;
                }
            };
        }     
        public TypeReader<NumberedLink> orderedReader(ArrayInput _input) {
            return new ShreddedReader(_input);
        }    

        public TypeReader<NumberedLink> orderedReader(ArrayInput _input, int bufferSize) {
            return new ShreddedReader(_input, bufferSize);
        }    
        public OrderedWriter<NumberedLink> orderedWriter(ArrayOutput _output) {
            ShreddedWriter w = new ShreddedWriter(_output);
            return new OrderedWriterClass(w); 
        }                                    
        public static class OrderedWriterClass extends OrderedWriter< NumberedLink > {
            NumberedLink last = null;
            ShreddedWriter shreddedWriter = null; 
            
            public OrderedWriterClass(ShreddedWriter s) {
                this.shreddedWriter = s;
            }
            
            public void process(NumberedLink object) throws IOException {
               boolean processAll = false;
               if (processAll || last == null || 0 != Utility.compare(object.destination, last.destination)) { processAll = true; shreddedWriter.processDestination(object.destination); }
               shreddedWriter.processTuple(object.source);
               last = object;
            }           
                 
            public void close() throws IOException {
                shreddedWriter.close();
            }
            
            public Class<NumberedLink> getInputClass() {
                return NumberedLink.class;
            }
        } 
        public ReaderSource<NumberedLink> orderedCombiner(Collection<TypeReader<NumberedLink>> readers, boolean closeOnExit) {
            ArrayList<ShreddedReader> shreddedReaders = new ArrayList();
            
            for (TypeReader<NumberedLink> reader : readers) {
                shreddedReaders.add((ShreddedReader)reader);
            }
            
            return new ShreddedCombiner(shreddedReaders, closeOnExit);
        }                  
        public NumberedLink clone(NumberedLink object) {
            NumberedLink result = new NumberedLink();
            if (object == null) return result;
            result.source = object.source; 
            result.destination = object.destination; 
            return result;
        }                 
        public Class<NumberedLink> getOrderedClass() {
            return NumberedLink.class;
        }                           
        public String[] getOrderSpec() {
            return new String[] {"+destination"};
        }

        public static String[] getSpec() {
            return new String[] {"+destination"};
        }
        public static String getSpecString() {
            return "+destination";
        }
                           
        public interface ShreddedProcessor extends Step {
            public void processDestination(long destination) throws IOException;
            public void processTuple(long source) throws IOException;
            public void close() throws IOException;
        }    
        public interface ShreddedSource extends Step {
        }                                              
        
        public static class ShreddedWriter implements ShreddedProcessor {
            ArrayOutput output;
            ShreddedBuffer buffer = new ShreddedBuffer();
            long lastDestination;
            boolean lastFlush = false;
            
            public ShreddedWriter(ArrayOutput output) {
                this.output = output;
            }                        
            
            public void close() throws IOException {
                flush();
            }
            
            public void processDestination(long destination) {
                lastDestination = destination;
                buffer.processDestination(destination);
            }
            public final void processTuple(long source) throws IOException {
                if (lastFlush) {
                    if(buffer.destinations.size() == 0) buffer.processDestination(lastDestination);
                    lastFlush = false;
                }
                buffer.processTuple(source);
                if (buffer.isFull())
                    flush();
            }
            public final void flushTuples(int pauseIndex) throws IOException {
                
                while (buffer.getReadIndex() < pauseIndex) {
                           
                    output.writeLong(buffer.getSource());
                    buffer.incrementTuple();
                }
            }  
            public final void flushDestination(int pauseIndex) throws IOException {
                while (buffer.getReadIndex() < pauseIndex) {
                    int nextPause = buffer.getDestinationEndIndex();
                    int count = nextPause - buffer.getReadIndex();
                    
                    output.writeLong(buffer.getDestination());
                    output.writeInt(count);
                    buffer.incrementDestination();
                      
                    flushTuples(nextPause);
                    assert nextPause == buffer.getReadIndex();
                }
            }
            public void flush() throws IOException { 
                flushDestination(buffer.getWriteIndex());
                buffer.reset(); 
                lastFlush = true;
            }                           
        }
        public static class ShreddedBuffer {
            TLongArrayList destinations = new TLongArrayList();
            TIntArrayList destinationTupleIdx = new TIntArrayList();
            int destinationReadIdx = 0;
                            
            long[] sources;
            int writeTupleIndex = 0;
            int readTupleIndex = 0;
            int batchSize;

            public ShreddedBuffer(int batchSize) {
                this.batchSize = batchSize;

                sources = new long[batchSize];
            }                              

            public ShreddedBuffer() {    
                this(10000);
            }                                                                                                                    
            
            public void processDestination(long destination) {
                destinations.add(destination);
                destinationTupleIdx.add(writeTupleIndex);
            }                                      
            public void processTuple(long source) {
                assert destinations.size() > 0;
                sources[writeTupleIndex] = source;
                writeTupleIndex++;
            }
            public void resetData() {
                destinations.clear();
                destinationTupleIdx.clear();
                writeTupleIndex = 0;
            }                  
                                 
            public void resetRead() {
                readTupleIndex = 0;
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
            public long getDestination() {
                assert readTupleIndex < writeTupleIndex;
                assert destinationReadIdx < destinations.size();
                
                return destinations.get(destinationReadIdx);
            }
            public long getSource() {
                assert readTupleIndex < writeTupleIndex;
                return sources[readTupleIndex];
            }                                         
            public void copyTuples(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                   output.processTuple(getSource());
                   incrementTuple();
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
                    
               
                }
            }
            public void copyUntil(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                copyUntilDestination(other, output);
            }
            
        }                         
        public static class ShreddedCombiner implements ReaderSource<NumberedLink>, ShreddedSource {   
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
                } else if (processor instanceof NumberedLink.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((NumberedLink.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<NumberedLink>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<NumberedLink> getOutputClass() {
                return NumberedLink.class;
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

            public NumberedLink read() throws IOException {
                if (uninitialized)
                    initialize();

                NumberedLink result = null;

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
        public static class ShreddedReader implements Step, Comparable<ShreddedReader>, TypeReader<NumberedLink>, ShreddedSource {      
            public ShreddedProcessor processor;
            ShreddedBuffer buffer;
            NumberedLink last = new NumberedLink();         
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
                    result = + Utility.compare(buffer.getDestination(), otherBuffer.getDestination());
                    if(result != 0) break;
                } while (false);                                             
                
                return result;
            }
            
            public final ShreddedBuffer getBuffer() {
                return buffer;
            }                
            
            public final NumberedLink read() throws IOException {
                if (buffer.isAtEnd()) {
                    fill();             
                
                    if (buffer.isAtEnd()) {
                        return null;
                    }
                }
                      
                assert !buffer.isAtEnd();
                NumberedLink result = new NumberedLink();
                
                result.destination = buffer.getDestination();
                result.source = buffer.getSource();
                
                buffer.incrementTuple();
                buffer.autoIncrementDestination();
                
                return result;
            }           
            
            public final void fill() throws IOException {
                try {   
                    buffer.reset();
                    
                    if (tupleCount != 0) {
                                                      
                        if(updateDestinationCount - tupleCount > 0) {
                            buffer.destinations.add(last.destination);
                            buffer.destinationTupleIdx.add((int) (updateDestinationCount - tupleCount));
                        }
                        bufferStartCount = tupleCount;
                    }
                    
                    while (!buffer.isFull()) {
                        updateDestination();
                        buffer.processTuple(input.readLong());
                        tupleCount++;
                    }
                } catch(EOFException e) {}
            }

            public final void updateDestination() throws IOException {
                if (updateDestinationCount > tupleCount)
                    return;
                     
                last.destination = input.readLong();
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
                } else if (processor instanceof NumberedLink.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((NumberedLink.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<NumberedLink>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<NumberedLink> getOutputClass() {
                return NumberedLink.class;
            }                
        }
        
        public static class DuplicateEliminator implements ShreddedProcessor {
            public ShreddedProcessor processor;
            NumberedLink last = new NumberedLink();
            boolean destinationProcess = true;
                                           
            public DuplicateEliminator() {}
            public DuplicateEliminator(ShreddedProcessor processor) {
                this.processor = processor;
            }
            
            public void setShreddedProcessor(ShreddedProcessor processor) {
                this.processor = processor;
            }

            public void processDestination(long destination) throws IOException {  
                if (destinationProcess || Utility.compare(destination, last.destination) != 0) {
                    last.destination = destination;
                    processor.processDestination(destination);
                    destinationProcess = false;
                }
            }  
            
            public void resetDestination() {
                 destinationProcess = true;
            }                                                
                               
            public void processTuple(long source) throws IOException {
                processor.processTuple(source);
            } 
            
            public void close() throws IOException {
                processor.close();
            }                    
        }
        public static class TupleUnshredder implements ShreddedProcessor {
            NumberedLink last = new NumberedLink();
            public org.lemurproject.galago.tupleflow.Processor<NumberedLink> processor;                               
            
            public TupleUnshredder(NumberedLink.Processor processor) {
                this.processor = processor;
            }         
            
            public TupleUnshredder(org.lemurproject.galago.tupleflow.Processor<NumberedLink> processor) {
                this.processor = processor;
            }
            
            public NumberedLink clone(NumberedLink object) {
                NumberedLink result = new NumberedLink();
                if (object == null) return result;
                result.source = object.source; 
                result.destination = object.destination; 
                return result;
            }                 
            
            public void processDestination(long destination) throws IOException {
                last.destination = destination;
            }   
                
            
            public void processTuple(long source) throws IOException {
                last.source = source;
                processor.process(clone(last));
            }               
            
            public void close() throws IOException {
                processor.close();
            }
        }     
        public static class TupleShredder implements Processor {
            NumberedLink last = null;
            public ShreddedProcessor processor;
            
            public TupleShredder(ShreddedProcessor processor) {
                this.processor = processor;
            }                              
            
            public NumberedLink clone(NumberedLink object) {
                NumberedLink result = new NumberedLink();
                if (object == null) return result;
                result.source = object.source; 
                result.destination = object.destination; 
                return result;
            }                 
            
            public void process(NumberedLink object) throws IOException {                                                                                                                                                   
                boolean processAll = false;
                if(last == null || Utility.compare(last.destination, object.destination) != 0 || processAll) { processor.processDestination(object.destination); processAll = true; }
                processor.processTuple(object.source);                                         
                last = object;
            }
                          
            public Class<NumberedLink> getInputClass() {
                return NumberedLink.class;
            }
            
            public void close() throws IOException {
                processor.close();
            }                     
        }
    } 
}    