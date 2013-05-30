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


public class HalfNumberedLink implements Type<HalfNumberedLink> {
    public String src;
    public int dest; 
    
    public HalfNumberedLink() {}
    public HalfNumberedLink(String src, int dest) {
        this.src = src;
        this.dest = dest;
    }  
    
    public String toString() {
            return String.format("%s,%d",
                                   src, dest);
    } 

    public Order<HalfNumberedLink> getOrder(String... spec) {
        if (Arrays.equals(spec, new String[] { "+dest" })) {
            return new DestOrder();
        }
        if (Arrays.equals(spec, new String[] { "+src" })) {
            return new SrcOrder();
        }
        return null;
    } 
      
    public interface Processor extends Step, org.lemurproject.galago.tupleflow.Processor<HalfNumberedLink> {
        public void process(HalfNumberedLink object) throws IOException;
        public void close() throws IOException;
    }                        
    public interface Source extends Step {
    }
    public static class DestOrder implements Order<HalfNumberedLink> {
        public int hash(HalfNumberedLink object) {
            int h = 0;
            h += Utility.hash(object.dest);
            return h;
        } 
        public Comparator<HalfNumberedLink> greaterThan() {
            return new Comparator<HalfNumberedLink>() {
                public int compare(HalfNumberedLink one, HalfNumberedLink two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.dest, two.dest);
                        if(result != 0) break;
                    } while (false);
                    return -result;
                }
            };
        }     
        public Comparator<HalfNumberedLink> lessThan() {
            return new Comparator<HalfNumberedLink>() {
                public int compare(HalfNumberedLink one, HalfNumberedLink two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.dest, two.dest);
                        if(result != 0) break;
                    } while (false);
                    return result;
                }
            };
        }     
        public TypeReader<HalfNumberedLink> orderedReader(ArrayInput _input) {
            return new ShreddedReader(_input);
        }    

        public TypeReader<HalfNumberedLink> orderedReader(ArrayInput _input, int bufferSize) {
            return new ShreddedReader(_input, bufferSize);
        }    
        public OrderedWriter<HalfNumberedLink> orderedWriter(ArrayOutput _output) {
            ShreddedWriter w = new ShreddedWriter(_output);
            return new OrderedWriterClass(w); 
        }                                    
        public static class OrderedWriterClass extends OrderedWriter< HalfNumberedLink > {
            HalfNumberedLink last = null;
            ShreddedWriter shreddedWriter = null; 
            
            public OrderedWriterClass(ShreddedWriter s) {
                this.shreddedWriter = s;
            }
            
            public void process(HalfNumberedLink object) throws IOException {
               boolean processAll = false;
               if (processAll || last == null || 0 != Utility.compare(object.dest, last.dest)) { processAll = true; shreddedWriter.processDest(object.dest); }
               shreddedWriter.processTuple(object.src);
               last = object;
            }           
                 
            public void close() throws IOException {
                shreddedWriter.close();
            }
            
            public Class<HalfNumberedLink> getInputClass() {
                return HalfNumberedLink.class;
            }
        } 
        public ReaderSource<HalfNumberedLink> orderedCombiner(Collection<TypeReader<HalfNumberedLink>> readers, boolean closeOnExit) {
            ArrayList<ShreddedReader> shreddedReaders = new ArrayList();
            
            for (TypeReader<HalfNumberedLink> reader : readers) {
                shreddedReaders.add((ShreddedReader)reader);
            }
            
            return new ShreddedCombiner(shreddedReaders, closeOnExit);
        }                  
        public HalfNumberedLink clone(HalfNumberedLink object) {
            HalfNumberedLink result = new HalfNumberedLink();
            if (object == null) return result;
            result.src = object.src; 
            result.dest = object.dest; 
            return result;
        }                 
        public Class<HalfNumberedLink> getOrderedClass() {
            return HalfNumberedLink.class;
        }                           
        public String[] getOrderSpec() {
            return new String[] {"+dest"};
        }

        public static String[] getSpec() {
            return new String[] {"+dest"};
        }
        public static String getSpecString() {
            return "+dest";
        }
                           
        public interface ShreddedProcessor extends Step {
            public void processDest(int dest) throws IOException;
            public void processTuple(String src) throws IOException;
            public void close() throws IOException;
        }    
        public interface ShreddedSource extends Step {
        }                                              
        
        public static class ShreddedWriter implements ShreddedProcessor {
            ArrayOutput output;
            ShreddedBuffer buffer = new ShreddedBuffer();
            int lastDest;
            boolean lastFlush = false;
            
            public ShreddedWriter(ArrayOutput output) {
                this.output = output;
            }                        
            
            public void close() throws IOException {
                flush();
            }
            
            public void processDest(int dest) {
                lastDest = dest;
                buffer.processDest(dest);
            }
            public final void processTuple(String src) throws IOException {
                if (lastFlush) {
                    if(buffer.dests.size() == 0) buffer.processDest(lastDest);
                    lastFlush = false;
                }
                buffer.processTuple(src);
                if (buffer.isFull())
                    flush();
            }
            public final void flushTuples(int pauseIndex) throws IOException {
                
                while (buffer.getReadIndex() < pauseIndex) {
                           
                    output.writeString(buffer.getSrc());
                    buffer.incrementTuple();
                }
            }  
            public final void flushDest(int pauseIndex) throws IOException {
                while (buffer.getReadIndex() < pauseIndex) {
                    int nextPause = buffer.getDestEndIndex();
                    int count = nextPause - buffer.getReadIndex();
                    
                    output.writeInt(buffer.getDest());
                    output.writeInt(count);
                    buffer.incrementDest();
                      
                    flushTuples(nextPause);
                    assert nextPause == buffer.getReadIndex();
                }
            }
            public void flush() throws IOException { 
                flushDest(buffer.getWriteIndex());
                buffer.reset(); 
                lastFlush = true;
            }                           
        }
        public static class ShreddedBuffer {
            TIntArrayList dests = new TIntArrayList();
            TIntArrayList destTupleIdx = new TIntArrayList();
            int destReadIdx = 0;
                            
            String[] srcs;
            int writeTupleIndex = 0;
            int readTupleIndex = 0;
            int batchSize;

            public ShreddedBuffer(int batchSize) {
                this.batchSize = batchSize;

                srcs = new String[batchSize];
            }                              

            public ShreddedBuffer() {    
                this(10000);
            }                                                                                                                    
            
            public void processDest(int dest) {
                dests.add(dest);
                destTupleIdx.add(writeTupleIndex);
            }                                      
            public void processTuple(String src) {
                assert dests.size() > 0;
                srcs[writeTupleIndex] = src;
                writeTupleIndex++;
            }
            public void resetData() {
                dests.clear();
                destTupleIdx.clear();
                writeTupleIndex = 0;
            }                  
                                 
            public void resetRead() {
                readTupleIndex = 0;
                destReadIdx = 0;
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
            public void incrementDest() {
                destReadIdx++;  
            }                                                                                              

            public void autoIncrementDest() {
                while (readTupleIndex >= getDestEndIndex() && readTupleIndex < writeTupleIndex)
                    destReadIdx++;
            }                 
            public void incrementTuple() {
                readTupleIndex++;
            }                    
            public int getDestEndIndex() {
                if ((destReadIdx+1) >= destTupleIdx.size())
                    return writeTupleIndex;
                return destTupleIdx.get(destReadIdx+1);
            }
            public int getReadIndex() {
                return readTupleIndex;
            }   

            public int getWriteIndex() {
                return writeTupleIndex;
            } 
            public int getDest() {
                assert readTupleIndex < writeTupleIndex;
                assert destReadIdx < dests.size();
                
                return dests.get(destReadIdx);
            }
            public String getSrc() {
                assert readTupleIndex < writeTupleIndex;
                return srcs[readTupleIndex];
            }                                         
            public void copyTuples(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                   output.processTuple(getSrc());
                   incrementTuple();
                }
            }                                                                           
            public void copyUntilIndexDest(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                    output.processDest(getDest());
                    assert getDestEndIndex() <= endIndex;
                    copyTuples(getDestEndIndex(), output);
                    incrementDest();
                }
            }  
            public void copyUntilDest(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                while (!isAtEnd()) {
                    if (other != null) {   
                        assert !other.isAtEnd();
                        int c = + Utility.compare(getDest(), other.getDest());
                    
                        if (c > 0) {
                            break;   
                        }
                        
                        output.processDest(getDest());
                                      
                        copyTuples(getDestEndIndex(), output);
                    } else {
                        output.processDest(getDest());
                        copyTuples(getDestEndIndex(), output);
                    }
                    incrementDest();  
                    
               
                }
            }
            public void copyUntil(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                copyUntilDest(other, output);
            }
            
        }                         
        public static class ShreddedCombiner implements ReaderSource<HalfNumberedLink>, ShreddedSource {   
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
                } else if (processor instanceof HalfNumberedLink.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((HalfNumberedLink.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<HalfNumberedLink>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<HalfNumberedLink> getOutputClass() {
                return HalfNumberedLink.class;
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

            public HalfNumberedLink read() throws IOException {
                if (uninitialized)
                    initialize();

                HalfNumberedLink result = null;

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
        public static class ShreddedReader implements Step, Comparable<ShreddedReader>, TypeReader<HalfNumberedLink>, ShreddedSource {      
            public ShreddedProcessor processor;
            ShreddedBuffer buffer;
            HalfNumberedLink last = new HalfNumberedLink();         
            long updateDestCount = -1;
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
                    result = + Utility.compare(buffer.getDest(), otherBuffer.getDest());
                    if(result != 0) break;
                } while (false);                                             
                
                return result;
            }
            
            public final ShreddedBuffer getBuffer() {
                return buffer;
            }                
            
            public final HalfNumberedLink read() throws IOException {
                if (buffer.isAtEnd()) {
                    fill();             
                
                    if (buffer.isAtEnd()) {
                        return null;
                    }
                }
                      
                assert !buffer.isAtEnd();
                HalfNumberedLink result = new HalfNumberedLink();
                
                result.dest = buffer.getDest();
                result.src = buffer.getSrc();
                
                buffer.incrementTuple();
                buffer.autoIncrementDest();
                
                return result;
            }           
            
            public final void fill() throws IOException {
                try {   
                    buffer.reset();
                    
                    if (tupleCount != 0) {
                                                      
                        if(updateDestCount - tupleCount > 0) {
                            buffer.dests.add(last.dest);
                            buffer.destTupleIdx.add((int) (updateDestCount - tupleCount));
                        }
                        bufferStartCount = tupleCount;
                    }
                    
                    while (!buffer.isFull()) {
                        updateDest();
                        buffer.processTuple(input.readString());
                        tupleCount++;
                    }
                } catch(EOFException e) {}
            }

            public final void updateDest() throws IOException {
                if (updateDestCount > tupleCount)
                    return;
                     
                last.dest = input.readInt();
                updateDestCount = tupleCount + input.readInt();
                                      
                buffer.processDest(last.dest);
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
                } else if (processor instanceof HalfNumberedLink.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((HalfNumberedLink.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<HalfNumberedLink>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<HalfNumberedLink> getOutputClass() {
                return HalfNumberedLink.class;
            }                
        }
        
        public static class DuplicateEliminator implements ShreddedProcessor {
            public ShreddedProcessor processor;
            HalfNumberedLink last = new HalfNumberedLink();
            boolean destProcess = true;
                                           
            public DuplicateEliminator() {}
            public DuplicateEliminator(ShreddedProcessor processor) {
                this.processor = processor;
            }
            
            public void setShreddedProcessor(ShreddedProcessor processor) {
                this.processor = processor;
            }

            public void processDest(int dest) throws IOException {  
                if (destProcess || Utility.compare(dest, last.dest) != 0) {
                    last.dest = dest;
                    processor.processDest(dest);
                    destProcess = false;
                }
            }  
            
            public void resetDest() {
                 destProcess = true;
            }                                                
                               
            public void processTuple(String src) throws IOException {
                processor.processTuple(src);
            } 
            
            public void close() throws IOException {
                processor.close();
            }                    
        }
        public static class TupleUnshredder implements ShreddedProcessor {
            HalfNumberedLink last = new HalfNumberedLink();
            public org.lemurproject.galago.tupleflow.Processor<HalfNumberedLink> processor;                               
            
            public TupleUnshredder(HalfNumberedLink.Processor processor) {
                this.processor = processor;
            }         
            
            public TupleUnshredder(org.lemurproject.galago.tupleflow.Processor<HalfNumberedLink> processor) {
                this.processor = processor;
            }
            
            public HalfNumberedLink clone(HalfNumberedLink object) {
                HalfNumberedLink result = new HalfNumberedLink();
                if (object == null) return result;
                result.src = object.src; 
                result.dest = object.dest; 
                return result;
            }                 
            
            public void processDest(int dest) throws IOException {
                last.dest = dest;
            }   
                
            
            public void processTuple(String src) throws IOException {
                last.src = src;
                processor.process(clone(last));
            }               
            
            public void close() throws IOException {
                processor.close();
            }
        }     
        public static class TupleShredder implements Processor {
            HalfNumberedLink last = null;
            public ShreddedProcessor processor;
            
            public TupleShredder(ShreddedProcessor processor) {
                this.processor = processor;
            }                              
            
            public HalfNumberedLink clone(HalfNumberedLink object) {
                HalfNumberedLink result = new HalfNumberedLink();
                if (object == null) return result;
                result.src = object.src; 
                result.dest = object.dest; 
                return result;
            }                 
            
            public void process(HalfNumberedLink object) throws IOException {                                                                                                                                                   
                boolean processAll = false;
                if(last == null || Utility.compare(last.dest, object.dest) != 0 || processAll) { processor.processDest(object.dest); processAll = true; }
                processor.processTuple(object.src);                                         
                last = object;
            }
                          
            public Class<HalfNumberedLink> getInputClass() {
                return HalfNumberedLink.class;
            }
            
            public void close() throws IOException {
                processor.close();
            }                     
        }
    } 
    public static class SrcOrder implements Order<HalfNumberedLink> {
        public int hash(HalfNumberedLink object) {
            int h = 0;
            h += Utility.hash(object.src);
            return h;
        } 
        public Comparator<HalfNumberedLink> greaterThan() {
            return new Comparator<HalfNumberedLink>() {
                public int compare(HalfNumberedLink one, HalfNumberedLink two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.src, two.src);
                        if(result != 0) break;
                    } while (false);
                    return -result;
                }
            };
        }     
        public Comparator<HalfNumberedLink> lessThan() {
            return new Comparator<HalfNumberedLink>() {
                public int compare(HalfNumberedLink one, HalfNumberedLink two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.src, two.src);
                        if(result != 0) break;
                    } while (false);
                    return result;
                }
            };
        }     
        public TypeReader<HalfNumberedLink> orderedReader(ArrayInput _input) {
            return new ShreddedReader(_input);
        }    

        public TypeReader<HalfNumberedLink> orderedReader(ArrayInput _input, int bufferSize) {
            return new ShreddedReader(_input, bufferSize);
        }    
        public OrderedWriter<HalfNumberedLink> orderedWriter(ArrayOutput _output) {
            ShreddedWriter w = new ShreddedWriter(_output);
            return new OrderedWriterClass(w); 
        }                                    
        public static class OrderedWriterClass extends OrderedWriter< HalfNumberedLink > {
            HalfNumberedLink last = null;
            ShreddedWriter shreddedWriter = null; 
            
            public OrderedWriterClass(ShreddedWriter s) {
                this.shreddedWriter = s;
            }
            
            public void process(HalfNumberedLink object) throws IOException {
               boolean processAll = false;
               if (processAll || last == null || 0 != Utility.compare(object.src, last.src)) { processAll = true; shreddedWriter.processSrc(object.src); }
               shreddedWriter.processTuple(object.dest);
               last = object;
            }           
                 
            public void close() throws IOException {
                shreddedWriter.close();
            }
            
            public Class<HalfNumberedLink> getInputClass() {
                return HalfNumberedLink.class;
            }
        } 
        public ReaderSource<HalfNumberedLink> orderedCombiner(Collection<TypeReader<HalfNumberedLink>> readers, boolean closeOnExit) {
            ArrayList<ShreddedReader> shreddedReaders = new ArrayList();
            
            for (TypeReader<HalfNumberedLink> reader : readers) {
                shreddedReaders.add((ShreddedReader)reader);
            }
            
            return new ShreddedCombiner(shreddedReaders, closeOnExit);
        }                  
        public HalfNumberedLink clone(HalfNumberedLink object) {
            HalfNumberedLink result = new HalfNumberedLink();
            if (object == null) return result;
            result.src = object.src; 
            result.dest = object.dest; 
            return result;
        }                 
        public Class<HalfNumberedLink> getOrderedClass() {
            return HalfNumberedLink.class;
        }                           
        public String[] getOrderSpec() {
            return new String[] {"+src"};
        }

        public static String[] getSpec() {
            return new String[] {"+src"};
        }
        public static String getSpecString() {
            return "+src";
        }
                           
        public interface ShreddedProcessor extends Step {
            public void processSrc(String src) throws IOException;
            public void processTuple(int dest) throws IOException;
            public void close() throws IOException;
        }    
        public interface ShreddedSource extends Step {
        }                                              
        
        public static class ShreddedWriter implements ShreddedProcessor {
            ArrayOutput output;
            ShreddedBuffer buffer = new ShreddedBuffer();
            String lastSrc;
            boolean lastFlush = false;
            
            public ShreddedWriter(ArrayOutput output) {
                this.output = output;
            }                        
            
            public void close() throws IOException {
                flush();
            }
            
            public void processSrc(String src) {
                lastSrc = src;
                buffer.processSrc(src);
            }
            public final void processTuple(int dest) throws IOException {
                if (lastFlush) {
                    if(buffer.srcs.size() == 0) buffer.processSrc(lastSrc);
                    lastFlush = false;
                }
                buffer.processTuple(dest);
                if (buffer.isFull())
                    flush();
            }
            public final void flushTuples(int pauseIndex) throws IOException {
                
                while (buffer.getReadIndex() < pauseIndex) {
                           
                    output.writeInt(buffer.getDest());
                    buffer.incrementTuple();
                }
            }  
            public final void flushSrc(int pauseIndex) throws IOException {
                while (buffer.getReadIndex() < pauseIndex) {
                    int nextPause = buffer.getSrcEndIndex();
                    int count = nextPause - buffer.getReadIndex();
                    
                    output.writeString(buffer.getSrc());
                    output.writeInt(count);
                    buffer.incrementSrc();
                      
                    flushTuples(nextPause);
                    assert nextPause == buffer.getReadIndex();
                }
            }
            public void flush() throws IOException { 
                flushSrc(buffer.getWriteIndex());
                buffer.reset(); 
                lastFlush = true;
            }                           
        }
        public static class ShreddedBuffer {
            ArrayList<String> srcs = new ArrayList();
            TIntArrayList srcTupleIdx = new TIntArrayList();
            int srcReadIdx = 0;
                            
            int[] dests;
            int writeTupleIndex = 0;
            int readTupleIndex = 0;
            int batchSize;

            public ShreddedBuffer(int batchSize) {
                this.batchSize = batchSize;

                dests = new int[batchSize];
            }                              

            public ShreddedBuffer() {    
                this(10000);
            }                                                                                                                    
            
            public void processSrc(String src) {
                srcs.add(src);
                srcTupleIdx.add(writeTupleIndex);
            }                                      
            public void processTuple(int dest) {
                assert srcs.size() > 0;
                dests[writeTupleIndex] = dest;
                writeTupleIndex++;
            }
            public void resetData() {
                srcs.clear();
                srcTupleIdx.clear();
                writeTupleIndex = 0;
            }                  
                                 
            public void resetRead() {
                readTupleIndex = 0;
                srcReadIdx = 0;
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
            public void incrementSrc() {
                srcReadIdx++;  
            }                                                                                              

            public void autoIncrementSrc() {
                while (readTupleIndex >= getSrcEndIndex() && readTupleIndex < writeTupleIndex)
                    srcReadIdx++;
            }                 
            public void incrementTuple() {
                readTupleIndex++;
            }                    
            public int getSrcEndIndex() {
                if ((srcReadIdx+1) >= srcTupleIdx.size())
                    return writeTupleIndex;
                return srcTupleIdx.get(srcReadIdx+1);
            }
            public int getReadIndex() {
                return readTupleIndex;
            }   

            public int getWriteIndex() {
                return writeTupleIndex;
            } 
            public String getSrc() {
                assert readTupleIndex < writeTupleIndex;
                assert srcReadIdx < srcs.size();
                
                return srcs.get(srcReadIdx);
            }
            public int getDest() {
                assert readTupleIndex < writeTupleIndex;
                return dests[readTupleIndex];
            }                                         
            public void copyTuples(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                   output.processTuple(getDest());
                   incrementTuple();
                }
            }                                                                           
            public void copyUntilIndexSrc(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                    output.processSrc(getSrc());
                    assert getSrcEndIndex() <= endIndex;
                    copyTuples(getSrcEndIndex(), output);
                    incrementSrc();
                }
            }  
            public void copyUntilSrc(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                while (!isAtEnd()) {
                    if (other != null) {   
                        assert !other.isAtEnd();
                        int c = + Utility.compare(getSrc(), other.getSrc());
                    
                        if (c > 0) {
                            break;   
                        }
                        
                        output.processSrc(getSrc());
                                      
                        copyTuples(getSrcEndIndex(), output);
                    } else {
                        output.processSrc(getSrc());
                        copyTuples(getSrcEndIndex(), output);
                    }
                    incrementSrc();  
                    
               
                }
            }
            public void copyUntil(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                copyUntilSrc(other, output);
            }
            
        }                         
        public static class ShreddedCombiner implements ReaderSource<HalfNumberedLink>, ShreddedSource {   
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
                } else if (processor instanceof HalfNumberedLink.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((HalfNumberedLink.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<HalfNumberedLink>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<HalfNumberedLink> getOutputClass() {
                return HalfNumberedLink.class;
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

            public HalfNumberedLink read() throws IOException {
                if (uninitialized)
                    initialize();

                HalfNumberedLink result = null;

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
        public static class ShreddedReader implements Step, Comparable<ShreddedReader>, TypeReader<HalfNumberedLink>, ShreddedSource {      
            public ShreddedProcessor processor;
            ShreddedBuffer buffer;
            HalfNumberedLink last = new HalfNumberedLink();         
            long updateSrcCount = -1;
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
                    result = + Utility.compare(buffer.getSrc(), otherBuffer.getSrc());
                    if(result != 0) break;
                } while (false);                                             
                
                return result;
            }
            
            public final ShreddedBuffer getBuffer() {
                return buffer;
            }                
            
            public final HalfNumberedLink read() throws IOException {
                if (buffer.isAtEnd()) {
                    fill();             
                
                    if (buffer.isAtEnd()) {
                        return null;
                    }
                }
                      
                assert !buffer.isAtEnd();
                HalfNumberedLink result = new HalfNumberedLink();
                
                result.src = buffer.getSrc();
                result.dest = buffer.getDest();
                
                buffer.incrementTuple();
                buffer.autoIncrementSrc();
                
                return result;
            }           
            
            public final void fill() throws IOException {
                try {   
                    buffer.reset();
                    
                    if (tupleCount != 0) {
                                                      
                        if(updateSrcCount - tupleCount > 0) {
                            buffer.srcs.add(last.src);
                            buffer.srcTupleIdx.add((int) (updateSrcCount - tupleCount));
                        }
                        bufferStartCount = tupleCount;
                    }
                    
                    while (!buffer.isFull()) {
                        updateSrc();
                        buffer.processTuple(input.readInt());
                        tupleCount++;
                    }
                } catch(EOFException e) {}
            }

            public final void updateSrc() throws IOException {
                if (updateSrcCount > tupleCount)
                    return;
                     
                last.src = input.readString();
                updateSrcCount = tupleCount + input.readInt();
                                      
                buffer.processSrc(last.src);
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
                } else if (processor instanceof HalfNumberedLink.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((HalfNumberedLink.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<HalfNumberedLink>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<HalfNumberedLink> getOutputClass() {
                return HalfNumberedLink.class;
            }                
        }
        
        public static class DuplicateEliminator implements ShreddedProcessor {
            public ShreddedProcessor processor;
            HalfNumberedLink last = new HalfNumberedLink();
            boolean srcProcess = true;
                                           
            public DuplicateEliminator() {}
            public DuplicateEliminator(ShreddedProcessor processor) {
                this.processor = processor;
            }
            
            public void setShreddedProcessor(ShreddedProcessor processor) {
                this.processor = processor;
            }

            public void processSrc(String src) throws IOException {  
                if (srcProcess || Utility.compare(src, last.src) != 0) {
                    last.src = src;
                    processor.processSrc(src);
                    srcProcess = false;
                }
            }  
            
            public void resetSrc() {
                 srcProcess = true;
            }                                                
                               
            public void processTuple(int dest) throws IOException {
                processor.processTuple(dest);
            } 
            
            public void close() throws IOException {
                processor.close();
            }                    
        }
        public static class TupleUnshredder implements ShreddedProcessor {
            HalfNumberedLink last = new HalfNumberedLink();
            public org.lemurproject.galago.tupleflow.Processor<HalfNumberedLink> processor;                               
            
            public TupleUnshredder(HalfNumberedLink.Processor processor) {
                this.processor = processor;
            }         
            
            public TupleUnshredder(org.lemurproject.galago.tupleflow.Processor<HalfNumberedLink> processor) {
                this.processor = processor;
            }
            
            public HalfNumberedLink clone(HalfNumberedLink object) {
                HalfNumberedLink result = new HalfNumberedLink();
                if (object == null) return result;
                result.src = object.src; 
                result.dest = object.dest; 
                return result;
            }                 
            
            public void processSrc(String src) throws IOException {
                last.src = src;
            }   
                
            
            public void processTuple(int dest) throws IOException {
                last.dest = dest;
                processor.process(clone(last));
            }               
            
            public void close() throws IOException {
                processor.close();
            }
        }     
        public static class TupleShredder implements Processor {
            HalfNumberedLink last = null;
            public ShreddedProcessor processor;
            
            public TupleShredder(ShreddedProcessor processor) {
                this.processor = processor;
            }                              
            
            public HalfNumberedLink clone(HalfNumberedLink object) {
                HalfNumberedLink result = new HalfNumberedLink();
                if (object == null) return result;
                result.src = object.src; 
                result.dest = object.dest; 
                return result;
            }                 
            
            public void process(HalfNumberedLink object) throws IOException {                                                                                                                                                   
                boolean processAll = false;
                if(last == null || Utility.compare(last.src, object.src) != 0 || processAll) { processor.processSrc(object.src); processAll = true; }
                processor.processTuple(object.dest);                                         
                last = object;
            }
                          
            public Class<HalfNumberedLink> getInputClass() {
                return HalfNumberedLink.class;
            }
            
            public void close() throws IOException {
                processor.close();
            }                     
        }
    } 
}    