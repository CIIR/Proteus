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


public class PictureOccurrence implements Type<PictureOccurrence> {
    public byte[] id;
    public int ordinal;
    public int top;
    public int bottom;
    public int left;
    public int right; 
    
    public PictureOccurrence() {}
    public PictureOccurrence(byte[] id, int ordinal, int top, int bottom, int left, int right) {
        this.id = id;
        this.ordinal = ordinal;
        this.top = top;
        this.bottom = bottom;
        this.left = left;
        this.right = right;
    }  
    
    public String toString() {
        try {
            return String.format("%s,%d,%d,%d,%d,%d",
                                   new String(id, "UTF-8"), ordinal, top, bottom, left, right);
        } catch(UnsupportedEncodingException e) {
            throw new RuntimeException("Couldn't convert string to UTF-8.");
        }
    } 

    public Order<PictureOccurrence> getOrder(String... spec) {
        if (Arrays.equals(spec, new String[] { "+id", "+ordinal", "+top", "+left" })) {
            return new IdOrdinalTopLeftOrder();
        }
        if (Arrays.equals(spec, new String[] { "+id" })) {
            return new IdOrder();
        }
        return null;
    } 
      
    public interface Processor extends Step, org.lemurproject.galago.tupleflow.Processor<PictureOccurrence> {
        public void process(PictureOccurrence object) throws IOException;
        public void close() throws IOException;
    }                        
    public interface Source extends Step {
    }
    public static class IdOrdinalTopLeftOrder implements Order<PictureOccurrence> {
        public int hash(PictureOccurrence object) {
            int h = 0;
            h += Utility.hash(object.id);
            h += Utility.hash(object.ordinal);
            h += Utility.hash(object.top);
            h += Utility.hash(object.left);
            return h;
        } 
        public Comparator<PictureOccurrence> greaterThan() {
            return new Comparator<PictureOccurrence>() {
                public int compare(PictureOccurrence one, PictureOccurrence two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.id, two.id);
                        if(result != 0) break;
                        result = + Utility.compare(one.ordinal, two.ordinal);
                        if(result != 0) break;
                        result = + Utility.compare(one.top, two.top);
                        if(result != 0) break;
                        result = + Utility.compare(one.left, two.left);
                        if(result != 0) break;
                    } while (false);
                    return -result;
                }
            };
        }     
        public Comparator<PictureOccurrence> lessThan() {
            return new Comparator<PictureOccurrence>() {
                public int compare(PictureOccurrence one, PictureOccurrence two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.id, two.id);
                        if(result != 0) break;
                        result = + Utility.compare(one.ordinal, two.ordinal);
                        if(result != 0) break;
                        result = + Utility.compare(one.top, two.top);
                        if(result != 0) break;
                        result = + Utility.compare(one.left, two.left);
                        if(result != 0) break;
                    } while (false);
                    return result;
                }
            };
        }     
        public TypeReader<PictureOccurrence> orderedReader(ArrayInput _input) {
            return new ShreddedReader(_input);
        }    

        public TypeReader<PictureOccurrence> orderedReader(ArrayInput _input, int bufferSize) {
            return new ShreddedReader(_input, bufferSize);
        }    
        public OrderedWriter<PictureOccurrence> orderedWriter(ArrayOutput _output) {
            ShreddedWriter w = new ShreddedWriter(_output);
            return new OrderedWriterClass(w); 
        }                                    
        public static class OrderedWriterClass extends OrderedWriter< PictureOccurrence > {
            PictureOccurrence last = null;
            ShreddedWriter shreddedWriter = null; 
            
            public OrderedWriterClass(ShreddedWriter s) {
                this.shreddedWriter = s;
            }
            
            public void process(PictureOccurrence object) throws IOException {
               boolean processAll = false;
               if (processAll || last == null || 0 != Utility.compare(object.id, last.id)) { processAll = true; shreddedWriter.processId(object.id); }
               if (processAll || last == null || 0 != Utility.compare(object.ordinal, last.ordinal)) { processAll = true; shreddedWriter.processOrdinal(object.ordinal); }
               if (processAll || last == null || 0 != Utility.compare(object.top, last.top)) { processAll = true; shreddedWriter.processTop(object.top); }
               if (processAll || last == null || 0 != Utility.compare(object.left, last.left)) { processAll = true; shreddedWriter.processLeft(object.left); }
               shreddedWriter.processTuple(object.bottom, object.right);
               last = object;
            }           
                 
            public void close() throws IOException {
                shreddedWriter.close();
            }
            
            public Class<PictureOccurrence> getInputClass() {
                return PictureOccurrence.class;
            }
        } 
        public ReaderSource<PictureOccurrence> orderedCombiner(Collection<TypeReader<PictureOccurrence>> readers, boolean closeOnExit) {
            ArrayList<ShreddedReader> shreddedReaders = new ArrayList();
            
            for (TypeReader<PictureOccurrence> reader : readers) {
                shreddedReaders.add((ShreddedReader)reader);
            }
            
            return new ShreddedCombiner(shreddedReaders, closeOnExit);
        }                  
        public PictureOccurrence clone(PictureOccurrence object) {
            PictureOccurrence result = new PictureOccurrence();
            if (object == null) return result;
            result.id = object.id; 
            result.ordinal = object.ordinal; 
            result.top = object.top; 
            result.bottom = object.bottom; 
            result.left = object.left; 
            result.right = object.right; 
            return result;
        }                 
        public Class<PictureOccurrence> getOrderedClass() {
            return PictureOccurrence.class;
        }                           
        public String[] getOrderSpec() {
            return new String[] {"+id", "+ordinal", "+top", "+left"};
        }

        public static String[] getSpec() {
            return new String[] {"+id", "+ordinal", "+top", "+left"};
        }
        public static String getSpecString() {
            return "+id +ordinal +top +left";
        }
                           
        public interface ShreddedProcessor extends Step {
            public void processId(byte[] id) throws IOException;
            public void processOrdinal(int ordinal) throws IOException;
            public void processTop(int top) throws IOException;
            public void processLeft(int left) throws IOException;
            public void processTuple(int bottom, int right) throws IOException;
            public void close() throws IOException;
        }    
        public interface ShreddedSource extends Step {
        }                                              
        
        public static class ShreddedWriter implements ShreddedProcessor {
            ArrayOutput output;
            ShreddedBuffer buffer = new ShreddedBuffer();
            byte[] lastId;
            int lastOrdinal;
            int lastTop;
            int lastLeft;
            boolean lastFlush = false;
            
            public ShreddedWriter(ArrayOutput output) {
                this.output = output;
            }                        
            
            public void close() throws IOException {
                flush();
            }
            
            public void processId(byte[] id) {
                lastId = id;
                buffer.processId(id);
            }
            public void processOrdinal(int ordinal) {
                lastOrdinal = ordinal;
                buffer.processOrdinal(ordinal);
            }
            public void processTop(int top) {
                lastTop = top;
                buffer.processTop(top);
            }
            public void processLeft(int left) {
                lastLeft = left;
                buffer.processLeft(left);
            }
            public final void processTuple(int bottom, int right) throws IOException {
                if (lastFlush) {
                    if(buffer.ids.size() == 0) buffer.processId(lastId);
                    if(buffer.ordinals.size() == 0) buffer.processOrdinal(lastOrdinal);
                    if(buffer.tops.size() == 0) buffer.processTop(lastTop);
                    if(buffer.lefts.size() == 0) buffer.processLeft(lastLeft);
                    lastFlush = false;
                }
                buffer.processTuple(bottom, right);
                if (buffer.isFull())
                    flush();
            }
            public final void flushTuples(int pauseIndex) throws IOException {
                
                while (buffer.getReadIndex() < pauseIndex) {
                           
                    output.writeInt(buffer.getBottom());
                    output.writeInt(buffer.getRight());
                    buffer.incrementTuple();
                }
            }  
            public final void flushId(int pauseIndex) throws IOException {
                while (buffer.getReadIndex() < pauseIndex) {
                    int nextPause = buffer.getIdEndIndex();
                    int count = nextPause - buffer.getReadIndex();
                    
                    output.writeBytes(buffer.getId());
                    output.writeInt(count);
                    buffer.incrementId();
                      
                    flushOrdinal(nextPause);
                    assert nextPause == buffer.getReadIndex();
                }
            }
            public final void flushOrdinal(int pauseIndex) throws IOException {
                while (buffer.getReadIndex() < pauseIndex) {
                    int nextPause = buffer.getOrdinalEndIndex();
                    int count = nextPause - buffer.getReadIndex();
                    
                    output.writeInt(buffer.getOrdinal());
                    output.writeInt(count);
                    buffer.incrementOrdinal();
                      
                    flushTop(nextPause);
                    assert nextPause == buffer.getReadIndex();
                }
            }
            public final void flushTop(int pauseIndex) throws IOException {
                while (buffer.getReadIndex() < pauseIndex) {
                    int nextPause = buffer.getTopEndIndex();
                    int count = nextPause - buffer.getReadIndex();
                    
                    output.writeInt(buffer.getTop());
                    output.writeInt(count);
                    buffer.incrementTop();
                      
                    flushLeft(nextPause);
                    assert nextPause == buffer.getReadIndex();
                }
            }
            public final void flushLeft(int pauseIndex) throws IOException {
                while (buffer.getReadIndex() < pauseIndex) {
                    int nextPause = buffer.getLeftEndIndex();
                    int count = nextPause - buffer.getReadIndex();
                    
                    output.writeInt(buffer.getLeft());
                    output.writeInt(count);
                    buffer.incrementLeft();
                      
                    flushTuples(nextPause);
                    assert nextPause == buffer.getReadIndex();
                }
            }
            public void flush() throws IOException { 
                flushId(buffer.getWriteIndex());
                buffer.reset(); 
                lastFlush = true;
            }                           
        }
        public static class ShreddedBuffer {
            ArrayList<byte[]> ids = new ArrayList();
            TIntArrayList ordinals = new TIntArrayList();
            TIntArrayList tops = new TIntArrayList();
            TIntArrayList lefts = new TIntArrayList();
            TIntArrayList idTupleIdx = new TIntArrayList();
            TIntArrayList ordinalTupleIdx = new TIntArrayList();
            TIntArrayList topTupleIdx = new TIntArrayList();
            TIntArrayList leftTupleIdx = new TIntArrayList();
            int idReadIdx = 0;
            int ordinalReadIdx = 0;
            int topReadIdx = 0;
            int leftReadIdx = 0;
                            
            int[] bottoms;
            int[] rights;
            int writeTupleIndex = 0;
            int readTupleIndex = 0;
            int batchSize;

            public ShreddedBuffer(int batchSize) {
                this.batchSize = batchSize;

                bottoms = new int[batchSize];
                rights = new int[batchSize];
            }                              

            public ShreddedBuffer() {    
                this(10000);
            }                                                                                                                    
            
            public void processId(byte[] id) {
                ids.add(id);
                idTupleIdx.add(writeTupleIndex);
            }                                      
            public void processOrdinal(int ordinal) {
                ordinals.add(ordinal);
                ordinalTupleIdx.add(writeTupleIndex);
            }                                      
            public void processTop(int top) {
                tops.add(top);
                topTupleIdx.add(writeTupleIndex);
            }                                      
            public void processLeft(int left) {
                lefts.add(left);
                leftTupleIdx.add(writeTupleIndex);
            }                                      
            public void processTuple(int bottom, int right) {
                assert ids.size() > 0;
                assert ordinals.size() > 0;
                assert tops.size() > 0;
                assert lefts.size() > 0;
                bottoms[writeTupleIndex] = bottom;
                rights[writeTupleIndex] = right;
                writeTupleIndex++;
            }
            public void resetData() {
                ids.clear();
                ordinals.clear();
                tops.clear();
                lefts.clear();
                idTupleIdx.clear();
                ordinalTupleIdx.clear();
                topTupleIdx.clear();
                leftTupleIdx.clear();
                writeTupleIndex = 0;
            }                  
                                 
            public void resetRead() {
                readTupleIndex = 0;
                idReadIdx = 0;
                ordinalReadIdx = 0;
                topReadIdx = 0;
                leftReadIdx = 0;
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
            public void incrementId() {
                idReadIdx++;  
            }                                                                                              

            public void autoIncrementId() {
                while (readTupleIndex >= getIdEndIndex() && readTupleIndex < writeTupleIndex)
                    idReadIdx++;
            }                 
            public void incrementOrdinal() {
                ordinalReadIdx++;  
            }                                                                                              

            public void autoIncrementOrdinal() {
                while (readTupleIndex >= getOrdinalEndIndex() && readTupleIndex < writeTupleIndex)
                    ordinalReadIdx++;
            }                 
            public void incrementTop() {
                topReadIdx++;  
            }                                                                                              

            public void autoIncrementTop() {
                while (readTupleIndex >= getTopEndIndex() && readTupleIndex < writeTupleIndex)
                    topReadIdx++;
            }                 
            public void incrementLeft() {
                leftReadIdx++;  
            }                                                                                              

            public void autoIncrementLeft() {
                while (readTupleIndex >= getLeftEndIndex() && readTupleIndex < writeTupleIndex)
                    leftReadIdx++;
            }                 
            public void incrementTuple() {
                readTupleIndex++;
            }                    
            public int getIdEndIndex() {
                if ((idReadIdx+1) >= idTupleIdx.size())
                    return writeTupleIndex;
                return idTupleIdx.get(idReadIdx+1);
            }

            public int getOrdinalEndIndex() {
                if ((ordinalReadIdx+1) >= ordinalTupleIdx.size())
                    return writeTupleIndex;
                return ordinalTupleIdx.get(ordinalReadIdx+1);
            }

            public int getTopEndIndex() {
                if ((topReadIdx+1) >= topTupleIdx.size())
                    return writeTupleIndex;
                return topTupleIdx.get(topReadIdx+1);
            }

            public int getLeftEndIndex() {
                if ((leftReadIdx+1) >= leftTupleIdx.size())
                    return writeTupleIndex;
                return leftTupleIdx.get(leftReadIdx+1);
            }
            public int getReadIndex() {
                return readTupleIndex;
            }   

            public int getWriteIndex() {
                return writeTupleIndex;
            } 
            public byte[] getId() {
                assert readTupleIndex < writeTupleIndex;
                assert idReadIdx < ids.size();
                
                return ids.get(idReadIdx);
            }
            public int getOrdinal() {
                assert readTupleIndex < writeTupleIndex;
                assert ordinalReadIdx < ordinals.size();
                
                return ordinals.get(ordinalReadIdx);
            }
            public int getTop() {
                assert readTupleIndex < writeTupleIndex;
                assert topReadIdx < tops.size();
                
                return tops.get(topReadIdx);
            }
            public int getLeft() {
                assert readTupleIndex < writeTupleIndex;
                assert leftReadIdx < lefts.size();
                
                return lefts.get(leftReadIdx);
            }
            public int getBottom() {
                assert readTupleIndex < writeTupleIndex;
                return bottoms[readTupleIndex];
            }                                         
            public int getRight() {
                assert readTupleIndex < writeTupleIndex;
                return rights[readTupleIndex];
            }                                         
            public void copyTuples(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                   output.processTuple(getBottom(), getRight());
                   incrementTuple();
                }
            }                                                                           
            public void copyUntilIndexId(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                    output.processId(getId());
                    assert getIdEndIndex() <= endIndex;
                    copyUntilIndexOrdinal(getIdEndIndex(), output);
                    incrementId();
                }
            } 
            public void copyUntilIndexOrdinal(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                    output.processOrdinal(getOrdinal());
                    assert getOrdinalEndIndex() <= endIndex;
                    copyUntilIndexTop(getOrdinalEndIndex(), output);
                    incrementOrdinal();
                }
            } 
            public void copyUntilIndexTop(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                    output.processTop(getTop());
                    assert getTopEndIndex() <= endIndex;
                    copyUntilIndexLeft(getTopEndIndex(), output);
                    incrementTop();
                }
            } 
            public void copyUntilIndexLeft(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                    output.processLeft(getLeft());
                    assert getLeftEndIndex() <= endIndex;
                    copyTuples(getLeftEndIndex(), output);
                    incrementLeft();
                }
            }  
            public void copyUntilId(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                while (!isAtEnd()) {
                    if (other != null) {   
                        assert !other.isAtEnd();
                        int c = + Utility.compare(getId(), other.getId());
                    
                        if (c > 0) {
                            break;   
                        }
                        
                        output.processId(getId());
                                      
                        if (c < 0) {
                            copyUntilIndexOrdinal(getIdEndIndex(), output);
                        } else if (c == 0) {
                            copyUntilOrdinal(other, output);
                            autoIncrementId();
                            break;
                        }
                    } else {
                        output.processId(getId());
                        copyUntilIndexOrdinal(getIdEndIndex(), output);
                    }
                    incrementId();  
                    
               
                }
            }
            public void copyUntilOrdinal(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                while (!isAtEnd()) {
                    if (other != null) {   
                        assert !other.isAtEnd();
                        int c = + Utility.compare(getOrdinal(), other.getOrdinal());
                    
                        if (c > 0) {
                            break;   
                        }
                        
                        output.processOrdinal(getOrdinal());
                                      
                        if (c < 0) {
                            copyUntilIndexTop(getOrdinalEndIndex(), output);
                        } else if (c == 0) {
                            copyUntilTop(other, output);
                            autoIncrementOrdinal();
                            break;
                        }
                    } else {
                        output.processOrdinal(getOrdinal());
                        copyUntilIndexTop(getOrdinalEndIndex(), output);
                    }
                    incrementOrdinal();  
                    
                    if (getIdEndIndex() <= readTupleIndex)
                        break;   
                }
            }
            public void copyUntilTop(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                while (!isAtEnd()) {
                    if (other != null) {   
                        assert !other.isAtEnd();
                        int c = + Utility.compare(getTop(), other.getTop());
                    
                        if (c > 0) {
                            break;   
                        }
                        
                        output.processTop(getTop());
                                      
                        if (c < 0) {
                            copyUntilIndexLeft(getTopEndIndex(), output);
                        } else if (c == 0) {
                            copyUntilLeft(other, output);
                            autoIncrementTop();
                            break;
                        }
                    } else {
                        output.processTop(getTop());
                        copyUntilIndexLeft(getTopEndIndex(), output);
                    }
                    incrementTop();  
                    
                    if (getOrdinalEndIndex() <= readTupleIndex)
                        break;   
                }
            }
            public void copyUntilLeft(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                while (!isAtEnd()) {
                    if (other != null) {   
                        assert !other.isAtEnd();
                        int c = + Utility.compare(getLeft(), other.getLeft());
                    
                        if (c > 0) {
                            break;   
                        }
                        
                        output.processLeft(getLeft());
                                      
                        copyTuples(getLeftEndIndex(), output);
                    } else {
                        output.processLeft(getLeft());
                        copyTuples(getLeftEndIndex(), output);
                    }
                    incrementLeft();  
                    
                    if (getTopEndIndex() <= readTupleIndex)
                        break;   
                }
            }
            public void copyUntil(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                copyUntilId(other, output);
            }
            
        }                         
        public static class ShreddedCombiner implements ReaderSource<PictureOccurrence>, ShreddedSource {   
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
                } else if (processor instanceof PictureOccurrence.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((PictureOccurrence.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<PictureOccurrence>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<PictureOccurrence> getOutputClass() {
                return PictureOccurrence.class;
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

            public PictureOccurrence read() throws IOException {
                if (uninitialized)
                    initialize();

                PictureOccurrence result = null;

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
        public static class ShreddedReader implements Step, Comparable<ShreddedReader>, TypeReader<PictureOccurrence>, ShreddedSource {      
            public ShreddedProcessor processor;
            ShreddedBuffer buffer;
            PictureOccurrence last = new PictureOccurrence();         
            long updateIdCount = -1;
            long updateOrdinalCount = -1;
            long updateTopCount = -1;
            long updateLeftCount = -1;
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
                    result = + Utility.compare(buffer.getId(), otherBuffer.getId());
                    if(result != 0) break;
                    result = + Utility.compare(buffer.getOrdinal(), otherBuffer.getOrdinal());
                    if(result != 0) break;
                    result = + Utility.compare(buffer.getTop(), otherBuffer.getTop());
                    if(result != 0) break;
                    result = + Utility.compare(buffer.getLeft(), otherBuffer.getLeft());
                    if(result != 0) break;
                } while (false);                                             
                
                return result;
            }
            
            public final ShreddedBuffer getBuffer() {
                return buffer;
            }                
            
            public final PictureOccurrence read() throws IOException {
                if (buffer.isAtEnd()) {
                    fill();             
                
                    if (buffer.isAtEnd()) {
                        return null;
                    }
                }
                      
                assert !buffer.isAtEnd();
                PictureOccurrence result = new PictureOccurrence();
                
                result.id = buffer.getId();
                result.ordinal = buffer.getOrdinal();
                result.top = buffer.getTop();
                result.left = buffer.getLeft();
                result.bottom = buffer.getBottom();
                result.right = buffer.getRight();
                
                buffer.incrementTuple();
                buffer.autoIncrementId();
                buffer.autoIncrementOrdinal();
                buffer.autoIncrementTop();
                buffer.autoIncrementLeft();
                
                return result;
            }           
            
            public final void fill() throws IOException {
                try {   
                    buffer.reset();
                    
                    if (tupleCount != 0) {
                                                      
                        if(updateIdCount - tupleCount > 0) {
                            buffer.ids.add(last.id);
                            buffer.idTupleIdx.add((int) (updateIdCount - tupleCount));
                        }                              
                        if(updateOrdinalCount - tupleCount > 0) {
                            buffer.ordinals.add(last.ordinal);
                            buffer.ordinalTupleIdx.add((int) (updateOrdinalCount - tupleCount));
                        }                              
                        if(updateTopCount - tupleCount > 0) {
                            buffer.tops.add(last.top);
                            buffer.topTupleIdx.add((int) (updateTopCount - tupleCount));
                        }                              
                        if(updateLeftCount - tupleCount > 0) {
                            buffer.lefts.add(last.left);
                            buffer.leftTupleIdx.add((int) (updateLeftCount - tupleCount));
                        }
                        bufferStartCount = tupleCount;
                    }
                    
                    while (!buffer.isFull()) {
                        updateLeft();
                        buffer.processTuple(input.readInt(), input.readInt());
                        tupleCount++;
                    }
                } catch(EOFException e) {}
            }

            public final void updateId() throws IOException {
                if (updateIdCount > tupleCount)
                    return;
                     
                last.id = input.readBytes();
                updateIdCount = tupleCount + input.readInt();
                                      
                buffer.processId(last.id);
            }
            public final void updateOrdinal() throws IOException {
                if (updateOrdinalCount > tupleCount)
                    return;
                     
                updateId();
                last.ordinal = input.readInt();
                updateOrdinalCount = tupleCount + input.readInt();
                                      
                buffer.processOrdinal(last.ordinal);
            }
            public final void updateTop() throws IOException {
                if (updateTopCount > tupleCount)
                    return;
                     
                updateOrdinal();
                last.top = input.readInt();
                updateTopCount = tupleCount + input.readInt();
                                      
                buffer.processTop(last.top);
            }
            public final void updateLeft() throws IOException {
                if (updateLeftCount > tupleCount)
                    return;
                     
                updateTop();
                last.left = input.readInt();
                updateLeftCount = tupleCount + input.readInt();
                                      
                buffer.processLeft(last.left);
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
                } else if (processor instanceof PictureOccurrence.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((PictureOccurrence.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<PictureOccurrence>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<PictureOccurrence> getOutputClass() {
                return PictureOccurrence.class;
            }                
        }
        
        public static class DuplicateEliminator implements ShreddedProcessor {
            public ShreddedProcessor processor;
            PictureOccurrence last = new PictureOccurrence();
            boolean idProcess = true;
            boolean ordinalProcess = true;
            boolean topProcess = true;
            boolean leftProcess = true;
                                           
            public DuplicateEliminator() {}
            public DuplicateEliminator(ShreddedProcessor processor) {
                this.processor = processor;
            }
            
            public void setShreddedProcessor(ShreddedProcessor processor) {
                this.processor = processor;
            }

            public void processId(byte[] id) throws IOException {  
                if (idProcess || Utility.compare(id, last.id) != 0) {
                    last.id = id;
                    processor.processId(id);
            resetOrdinal();
                    idProcess = false;
                }
            }
            public void processOrdinal(int ordinal) throws IOException {  
                if (ordinalProcess || Utility.compare(ordinal, last.ordinal) != 0) {
                    last.ordinal = ordinal;
                    processor.processOrdinal(ordinal);
            resetTop();
                    ordinalProcess = false;
                }
            }
            public void processTop(int top) throws IOException {  
                if (topProcess || Utility.compare(top, last.top) != 0) {
                    last.top = top;
                    processor.processTop(top);
            resetLeft();
                    topProcess = false;
                }
            }
            public void processLeft(int left) throws IOException {  
                if (leftProcess || Utility.compare(left, last.left) != 0) {
                    last.left = left;
                    processor.processLeft(left);
                    leftProcess = false;
                }
            }  
            
            public void resetId() {
                 idProcess = true;
            resetOrdinal();
            }                                                
            public void resetOrdinal() {
                 ordinalProcess = true;
            resetTop();
            }                                                
            public void resetTop() {
                 topProcess = true;
            resetLeft();
            }                                                
            public void resetLeft() {
                 leftProcess = true;
            }                                                
                               
            public void processTuple(int bottom, int right) throws IOException {
                processor.processTuple(bottom, right);
            } 
            
            public void close() throws IOException {
                processor.close();
            }                    
        }
        public static class TupleUnshredder implements ShreddedProcessor {
            PictureOccurrence last = new PictureOccurrence();
            public org.lemurproject.galago.tupleflow.Processor<PictureOccurrence> processor;                               
            
            public TupleUnshredder(PictureOccurrence.Processor processor) {
                this.processor = processor;
            }         
            
            public TupleUnshredder(org.lemurproject.galago.tupleflow.Processor<PictureOccurrence> processor) {
                this.processor = processor;
            }
            
            public PictureOccurrence clone(PictureOccurrence object) {
                PictureOccurrence result = new PictureOccurrence();
                if (object == null) return result;
                result.id = object.id; 
                result.ordinal = object.ordinal; 
                result.top = object.top; 
                result.bottom = object.bottom; 
                result.left = object.left; 
                result.right = object.right; 
                return result;
            }                 
            
            public void processId(byte[] id) throws IOException {
                last.id = id;
            }   
                
            public void processOrdinal(int ordinal) throws IOException {
                last.ordinal = ordinal;
            }   
                
            public void processTop(int top) throws IOException {
                last.top = top;
            }   
                
            public void processLeft(int left) throws IOException {
                last.left = left;
            }   
                
            
            public void processTuple(int bottom, int right) throws IOException {
                last.bottom = bottom;
                last.right = right;
                processor.process(clone(last));
            }               
            
            public void close() throws IOException {
                processor.close();
            }
        }     
        public static class TupleShredder implements Processor {
            PictureOccurrence last = null;
            public ShreddedProcessor processor;
            
            public TupleShredder(ShreddedProcessor processor) {
                this.processor = processor;
            }                              
            
            public PictureOccurrence clone(PictureOccurrence object) {
                PictureOccurrence result = new PictureOccurrence();
                if (object == null) return result;
                result.id = object.id; 
                result.ordinal = object.ordinal; 
                result.top = object.top; 
                result.bottom = object.bottom; 
                result.left = object.left; 
                result.right = object.right; 
                return result;
            }                 
            
            public void process(PictureOccurrence object) throws IOException {                                                                                                                                                   
                boolean processAll = false;
                if(last == null || Utility.compare(last.id, object.id) != 0 || processAll) { processor.processId(object.id); processAll = true; }
                if(last == null || Utility.compare(last.ordinal, object.ordinal) != 0 || processAll) { processor.processOrdinal(object.ordinal); processAll = true; }
                if(last == null || Utility.compare(last.top, object.top) != 0 || processAll) { processor.processTop(object.top); processAll = true; }
                if(last == null || Utility.compare(last.left, object.left) != 0 || processAll) { processor.processLeft(object.left); processAll = true; }
                processor.processTuple(object.bottom, object.right);                                         
                last = object;
            }
                          
            public Class<PictureOccurrence> getInputClass() {
                return PictureOccurrence.class;
            }
            
            public void close() throws IOException {
                processor.close();
            }                     
        }
    } 
    public static class IdOrder implements Order<PictureOccurrence> {
        public int hash(PictureOccurrence object) {
            int h = 0;
            h += Utility.hash(object.id);
            return h;
        } 
        public Comparator<PictureOccurrence> greaterThan() {
            return new Comparator<PictureOccurrence>() {
                public int compare(PictureOccurrence one, PictureOccurrence two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.id, two.id);
                        if(result != 0) break;
                    } while (false);
                    return -result;
                }
            };
        }     
        public Comparator<PictureOccurrence> lessThan() {
            return new Comparator<PictureOccurrence>() {
                public int compare(PictureOccurrence one, PictureOccurrence two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.id, two.id);
                        if(result != 0) break;
                    } while (false);
                    return result;
                }
            };
        }     
        public TypeReader<PictureOccurrence> orderedReader(ArrayInput _input) {
            return new ShreddedReader(_input);
        }    

        public TypeReader<PictureOccurrence> orderedReader(ArrayInput _input, int bufferSize) {
            return new ShreddedReader(_input, bufferSize);
        }    
        public OrderedWriter<PictureOccurrence> orderedWriter(ArrayOutput _output) {
            ShreddedWriter w = new ShreddedWriter(_output);
            return new OrderedWriterClass(w); 
        }                                    
        public static class OrderedWriterClass extends OrderedWriter< PictureOccurrence > {
            PictureOccurrence last = null;
            ShreddedWriter shreddedWriter = null; 
            
            public OrderedWriterClass(ShreddedWriter s) {
                this.shreddedWriter = s;
            }
            
            public void process(PictureOccurrence object) throws IOException {
               boolean processAll = false;
               if (processAll || last == null || 0 != Utility.compare(object.id, last.id)) { processAll = true; shreddedWriter.processId(object.id); }
               shreddedWriter.processTuple(object.ordinal, object.top, object.bottom, object.left, object.right);
               last = object;
            }           
                 
            public void close() throws IOException {
                shreddedWriter.close();
            }
            
            public Class<PictureOccurrence> getInputClass() {
                return PictureOccurrence.class;
            }
        } 
        public ReaderSource<PictureOccurrence> orderedCombiner(Collection<TypeReader<PictureOccurrence>> readers, boolean closeOnExit) {
            ArrayList<ShreddedReader> shreddedReaders = new ArrayList();
            
            for (TypeReader<PictureOccurrence> reader : readers) {
                shreddedReaders.add((ShreddedReader)reader);
            }
            
            return new ShreddedCombiner(shreddedReaders, closeOnExit);
        }                  
        public PictureOccurrence clone(PictureOccurrence object) {
            PictureOccurrence result = new PictureOccurrence();
            if (object == null) return result;
            result.id = object.id; 
            result.ordinal = object.ordinal; 
            result.top = object.top; 
            result.bottom = object.bottom; 
            result.left = object.left; 
            result.right = object.right; 
            return result;
        }                 
        public Class<PictureOccurrence> getOrderedClass() {
            return PictureOccurrence.class;
        }                           
        public String[] getOrderSpec() {
            return new String[] {"+id"};
        }

        public static String[] getSpec() {
            return new String[] {"+id"};
        }
        public static String getSpecString() {
            return "+id";
        }
                           
        public interface ShreddedProcessor extends Step {
            public void processId(byte[] id) throws IOException;
            public void processTuple(int ordinal, int top, int bottom, int left, int right) throws IOException;
            public void close() throws IOException;
        }    
        public interface ShreddedSource extends Step {
        }                                              
        
        public static class ShreddedWriter implements ShreddedProcessor {
            ArrayOutput output;
            ShreddedBuffer buffer = new ShreddedBuffer();
            byte[] lastId;
            boolean lastFlush = false;
            
            public ShreddedWriter(ArrayOutput output) {
                this.output = output;
            }                        
            
            public void close() throws IOException {
                flush();
            }
            
            public void processId(byte[] id) {
                lastId = id;
                buffer.processId(id);
            }
            public final void processTuple(int ordinal, int top, int bottom, int left, int right) throws IOException {
                if (lastFlush) {
                    if(buffer.ids.size() == 0) buffer.processId(lastId);
                    lastFlush = false;
                }
                buffer.processTuple(ordinal, top, bottom, left, right);
                if (buffer.isFull())
                    flush();
            }
            public final void flushTuples(int pauseIndex) throws IOException {
                
                while (buffer.getReadIndex() < pauseIndex) {
                           
                    output.writeInt(buffer.getOrdinal());
                    output.writeInt(buffer.getTop());
                    output.writeInt(buffer.getBottom());
                    output.writeInt(buffer.getLeft());
                    output.writeInt(buffer.getRight());
                    buffer.incrementTuple();
                }
            }  
            public final void flushId(int pauseIndex) throws IOException {
                while (buffer.getReadIndex() < pauseIndex) {
                    int nextPause = buffer.getIdEndIndex();
                    int count = nextPause - buffer.getReadIndex();
                    
                    output.writeBytes(buffer.getId());
                    output.writeInt(count);
                    buffer.incrementId();
                      
                    flushTuples(nextPause);
                    assert nextPause == buffer.getReadIndex();
                }
            }
            public void flush() throws IOException { 
                flushId(buffer.getWriteIndex());
                buffer.reset(); 
                lastFlush = true;
            }                           
        }
        public static class ShreddedBuffer {
            ArrayList<byte[]> ids = new ArrayList();
            TIntArrayList idTupleIdx = new TIntArrayList();
            int idReadIdx = 0;
                            
            int[] ordinals;
            int[] tops;
            int[] bottoms;
            int[] lefts;
            int[] rights;
            int writeTupleIndex = 0;
            int readTupleIndex = 0;
            int batchSize;

            public ShreddedBuffer(int batchSize) {
                this.batchSize = batchSize;

                ordinals = new int[batchSize];
                tops = new int[batchSize];
                bottoms = new int[batchSize];
                lefts = new int[batchSize];
                rights = new int[batchSize];
            }                              

            public ShreddedBuffer() {    
                this(10000);
            }                                                                                                                    
            
            public void processId(byte[] id) {
                ids.add(id);
                idTupleIdx.add(writeTupleIndex);
            }                                      
            public void processTuple(int ordinal, int top, int bottom, int left, int right) {
                assert ids.size() > 0;
                ordinals[writeTupleIndex] = ordinal;
                tops[writeTupleIndex] = top;
                bottoms[writeTupleIndex] = bottom;
                lefts[writeTupleIndex] = left;
                rights[writeTupleIndex] = right;
                writeTupleIndex++;
            }
            public void resetData() {
                ids.clear();
                idTupleIdx.clear();
                writeTupleIndex = 0;
            }                  
                                 
            public void resetRead() {
                readTupleIndex = 0;
                idReadIdx = 0;
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
            public void incrementId() {
                idReadIdx++;  
            }                                                                                              

            public void autoIncrementId() {
                while (readTupleIndex >= getIdEndIndex() && readTupleIndex < writeTupleIndex)
                    idReadIdx++;
            }                 
            public void incrementTuple() {
                readTupleIndex++;
            }                    
            public int getIdEndIndex() {
                if ((idReadIdx+1) >= idTupleIdx.size())
                    return writeTupleIndex;
                return idTupleIdx.get(idReadIdx+1);
            }
            public int getReadIndex() {
                return readTupleIndex;
            }   

            public int getWriteIndex() {
                return writeTupleIndex;
            } 
            public byte[] getId() {
                assert readTupleIndex < writeTupleIndex;
                assert idReadIdx < ids.size();
                
                return ids.get(idReadIdx);
            }
            public int getOrdinal() {
                assert readTupleIndex < writeTupleIndex;
                return ordinals[readTupleIndex];
            }                                         
            public int getTop() {
                assert readTupleIndex < writeTupleIndex;
                return tops[readTupleIndex];
            }                                         
            public int getBottom() {
                assert readTupleIndex < writeTupleIndex;
                return bottoms[readTupleIndex];
            }                                         
            public int getLeft() {
                assert readTupleIndex < writeTupleIndex;
                return lefts[readTupleIndex];
            }                                         
            public int getRight() {
                assert readTupleIndex < writeTupleIndex;
                return rights[readTupleIndex];
            }                                         
            public void copyTuples(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                   output.processTuple(getOrdinal(), getTop(), getBottom(), getLeft(), getRight());
                   incrementTuple();
                }
            }                                                                           
            public void copyUntilIndexId(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                    output.processId(getId());
                    assert getIdEndIndex() <= endIndex;
                    copyTuples(getIdEndIndex(), output);
                    incrementId();
                }
            }  
            public void copyUntilId(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                while (!isAtEnd()) {
                    if (other != null) {   
                        assert !other.isAtEnd();
                        int c = + Utility.compare(getId(), other.getId());
                    
                        if (c > 0) {
                            break;   
                        }
                        
                        output.processId(getId());
                                      
                        copyTuples(getIdEndIndex(), output);
                    } else {
                        output.processId(getId());
                        copyTuples(getIdEndIndex(), output);
                    }
                    incrementId();  
                    
               
                }
            }
            public void copyUntil(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                copyUntilId(other, output);
            }
            
        }                         
        public static class ShreddedCombiner implements ReaderSource<PictureOccurrence>, ShreddedSource {   
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
                } else if (processor instanceof PictureOccurrence.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((PictureOccurrence.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<PictureOccurrence>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<PictureOccurrence> getOutputClass() {
                return PictureOccurrence.class;
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

            public PictureOccurrence read() throws IOException {
                if (uninitialized)
                    initialize();

                PictureOccurrence result = null;

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
        public static class ShreddedReader implements Step, Comparable<ShreddedReader>, TypeReader<PictureOccurrence>, ShreddedSource {      
            public ShreddedProcessor processor;
            ShreddedBuffer buffer;
            PictureOccurrence last = new PictureOccurrence();         
            long updateIdCount = -1;
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
                    result = + Utility.compare(buffer.getId(), otherBuffer.getId());
                    if(result != 0) break;
                } while (false);                                             
                
                return result;
            }
            
            public final ShreddedBuffer getBuffer() {
                return buffer;
            }                
            
            public final PictureOccurrence read() throws IOException {
                if (buffer.isAtEnd()) {
                    fill();             
                
                    if (buffer.isAtEnd()) {
                        return null;
                    }
                }
                      
                assert !buffer.isAtEnd();
                PictureOccurrence result = new PictureOccurrence();
                
                result.id = buffer.getId();
                result.ordinal = buffer.getOrdinal();
                result.top = buffer.getTop();
                result.bottom = buffer.getBottom();
                result.left = buffer.getLeft();
                result.right = buffer.getRight();
                
                buffer.incrementTuple();
                buffer.autoIncrementId();
                
                return result;
            }           
            
            public final void fill() throws IOException {
                try {   
                    buffer.reset();
                    
                    if (tupleCount != 0) {
                                                      
                        if(updateIdCount - tupleCount > 0) {
                            buffer.ids.add(last.id);
                            buffer.idTupleIdx.add((int) (updateIdCount - tupleCount));
                        }
                        bufferStartCount = tupleCount;
                    }
                    
                    while (!buffer.isFull()) {
                        updateId();
                        buffer.processTuple(input.readInt(), input.readInt(), input.readInt(), input.readInt(), input.readInt());
                        tupleCount++;
                    }
                } catch(EOFException e) {}
            }

            public final void updateId() throws IOException {
                if (updateIdCount > tupleCount)
                    return;
                     
                last.id = input.readBytes();
                updateIdCount = tupleCount + input.readInt();
                                      
                buffer.processId(last.id);
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
                } else if (processor instanceof PictureOccurrence.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((PictureOccurrence.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<PictureOccurrence>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<PictureOccurrence> getOutputClass() {
                return PictureOccurrence.class;
            }                
        }
        
        public static class DuplicateEliminator implements ShreddedProcessor {
            public ShreddedProcessor processor;
            PictureOccurrence last = new PictureOccurrence();
            boolean idProcess = true;
                                           
            public DuplicateEliminator() {}
            public DuplicateEliminator(ShreddedProcessor processor) {
                this.processor = processor;
            }
            
            public void setShreddedProcessor(ShreddedProcessor processor) {
                this.processor = processor;
            }

            public void processId(byte[] id) throws IOException {  
                if (idProcess || Utility.compare(id, last.id) != 0) {
                    last.id = id;
                    processor.processId(id);
                    idProcess = false;
                }
            }  
            
            public void resetId() {
                 idProcess = true;
            }                                                
                               
            public void processTuple(int ordinal, int top, int bottom, int left, int right) throws IOException {
                processor.processTuple(ordinal, top, bottom, left, right);
            } 
            
            public void close() throws IOException {
                processor.close();
            }                    
        }
        public static class TupleUnshredder implements ShreddedProcessor {
            PictureOccurrence last = new PictureOccurrence();
            public org.lemurproject.galago.tupleflow.Processor<PictureOccurrence> processor;                               
            
            public TupleUnshredder(PictureOccurrence.Processor processor) {
                this.processor = processor;
            }         
            
            public TupleUnshredder(org.lemurproject.galago.tupleflow.Processor<PictureOccurrence> processor) {
                this.processor = processor;
            }
            
            public PictureOccurrence clone(PictureOccurrence object) {
                PictureOccurrence result = new PictureOccurrence();
                if (object == null) return result;
                result.id = object.id; 
                result.ordinal = object.ordinal; 
                result.top = object.top; 
                result.bottom = object.bottom; 
                result.left = object.left; 
                result.right = object.right; 
                return result;
            }                 
            
            public void processId(byte[] id) throws IOException {
                last.id = id;
            }   
                
            
            public void processTuple(int ordinal, int top, int bottom, int left, int right) throws IOException {
                last.ordinal = ordinal;
                last.top = top;
                last.bottom = bottom;
                last.left = left;
                last.right = right;
                processor.process(clone(last));
            }               
            
            public void close() throws IOException {
                processor.close();
            }
        }     
        public static class TupleShredder implements Processor {
            PictureOccurrence last = null;
            public ShreddedProcessor processor;
            
            public TupleShredder(ShreddedProcessor processor) {
                this.processor = processor;
            }                              
            
            public PictureOccurrence clone(PictureOccurrence object) {
                PictureOccurrence result = new PictureOccurrence();
                if (object == null) return result;
                result.id = object.id; 
                result.ordinal = object.ordinal; 
                result.top = object.top; 
                result.bottom = object.bottom; 
                result.left = object.left; 
                result.right = object.right; 
                return result;
            }                 
            
            public void process(PictureOccurrence object) throws IOException {                                                                                                                                                   
                boolean processAll = false;
                if(last == null || Utility.compare(last.id, object.id) != 0 || processAll) { processor.processId(object.id); processAll = true; }
                processor.processTuple(object.ordinal, object.top, object.bottom, object.left, object.right);                                         
                last = object;
            }
                          
            public Class<PictureOccurrence> getInputClass() {
                return PictureOccurrence.class;
            }
            
            public void close() throws IOException {
                processor.close();
            }                     
        }
    } 
}    