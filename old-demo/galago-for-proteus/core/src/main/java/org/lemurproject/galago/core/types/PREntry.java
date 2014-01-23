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


public class PREntry implements Type<PREntry> {
    public long docNum;
    public double score; 
    
    public PREntry() {}
    public PREntry(long docNum, double score) {
        this.docNum = docNum;
        this.score = score;
    }  
    
    public String toString() {
            return String.format("%d,%f",
                                   docNum, score);
    } 

    public Order<PREntry> getOrder(String... spec) {
        if (Arrays.equals(spec, new String[] { "+docNum" })) {
            return new DocNumOrder();
        }
        return null;
    } 
      
    public interface Processor extends Step, org.lemurproject.galago.tupleflow.Processor<PREntry> {
        public void process(PREntry object) throws IOException;
        public void close() throws IOException;
    }                        
    public interface Source extends Step {
    }
    public static class DocNumOrder implements Order<PREntry> {
        public int hash(PREntry object) {
            int h = 0;
            h += Utility.hash(object.docNum);
            return h;
        } 
        public Comparator<PREntry> greaterThan() {
            return new Comparator<PREntry>() {
                public int compare(PREntry one, PREntry two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.docNum, two.docNum);
                        if(result != 0) break;
                    } while (false);
                    return -result;
                }
            };
        }     
        public Comparator<PREntry> lessThan() {
            return new Comparator<PREntry>() {
                public int compare(PREntry one, PREntry two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.docNum, two.docNum);
                        if(result != 0) break;
                    } while (false);
                    return result;
                }
            };
        }     
        public TypeReader<PREntry> orderedReader(ArrayInput _input) {
            return new ShreddedReader(_input);
        }    

        public TypeReader<PREntry> orderedReader(ArrayInput _input, int bufferSize) {
            return new ShreddedReader(_input, bufferSize);
        }    
        public OrderedWriter<PREntry> orderedWriter(ArrayOutput _output) {
            ShreddedWriter w = new ShreddedWriter(_output);
            return new OrderedWriterClass(w); 
        }                                    
        public static class OrderedWriterClass extends OrderedWriter< PREntry > {
            PREntry last = null;
            ShreddedWriter shreddedWriter = null; 
            
            public OrderedWriterClass(ShreddedWriter s) {
                this.shreddedWriter = s;
            }
            
            public void process(PREntry object) throws IOException {
               boolean processAll = false;
               if (processAll || last == null || 0 != Utility.compare(object.docNum, last.docNum)) { processAll = true; shreddedWriter.processDocNum(object.docNum); }
               shreddedWriter.processTuple(object.score);
               last = object;
            }           
                 
            public void close() throws IOException {
                shreddedWriter.close();
            }
            
            public Class<PREntry> getInputClass() {
                return PREntry.class;
            }
        } 
        public ReaderSource<PREntry> orderedCombiner(Collection<TypeReader<PREntry>> readers, boolean closeOnExit) {
            ArrayList<ShreddedReader> shreddedReaders = new ArrayList();
            
            for (TypeReader<PREntry> reader : readers) {
                shreddedReaders.add((ShreddedReader)reader);
            }
            
            return new ShreddedCombiner(shreddedReaders, closeOnExit);
        }                  
        public PREntry clone(PREntry object) {
            PREntry result = new PREntry();
            if (object == null) return result;
            result.docNum = object.docNum; 
            result.score = object.score; 
            return result;
        }                 
        public Class<PREntry> getOrderedClass() {
            return PREntry.class;
        }                           
        public String[] getOrderSpec() {
            return new String[] {"+docNum"};
        }

        public static String[] getSpec() {
            return new String[] {"+docNum"};
        }
        public static String getSpecString() {
            return "+docNum";
        }
                           
        public interface ShreddedProcessor extends Step {
            public void processDocNum(long docNum) throws IOException;
            public void processTuple(double score) throws IOException;
            public void close() throws IOException;
        }    
        public interface ShreddedSource extends Step {
        }                                              
        
        public static class ShreddedWriter implements ShreddedProcessor {
            ArrayOutput output;
            ShreddedBuffer buffer = new ShreddedBuffer();
            long lastDocNum;
            boolean lastFlush = false;
            
            public ShreddedWriter(ArrayOutput output) {
                this.output = output;
            }                        
            
            public void close() throws IOException {
                flush();
            }
            
            public void processDocNum(long docNum) {
                lastDocNum = docNum;
                buffer.processDocNum(docNum);
            }
            public final void processTuple(double score) throws IOException {
                if (lastFlush) {
                    if(buffer.docNums.size() == 0) buffer.processDocNum(lastDocNum);
                    lastFlush = false;
                }
                buffer.processTuple(score);
                if (buffer.isFull())
                    flush();
            }
            public final void flushTuples(int pauseIndex) throws IOException {
                
                while (buffer.getReadIndex() < pauseIndex) {
                           
                    output.writeDouble(buffer.getScore());
                    buffer.incrementTuple();
                }
            }  
            public final void flushDocNum(int pauseIndex) throws IOException {
                while (buffer.getReadIndex() < pauseIndex) {
                    int nextPause = buffer.getDocNumEndIndex();
                    int count = nextPause - buffer.getReadIndex();
                    
                    output.writeLong(buffer.getDocNum());
                    output.writeInt(count);
                    buffer.incrementDocNum();
                      
                    flushTuples(nextPause);
                    assert nextPause == buffer.getReadIndex();
                }
            }
            public void flush() throws IOException { 
                flushDocNum(buffer.getWriteIndex());
                buffer.reset(); 
                lastFlush = true;
            }                           
        }
        public static class ShreddedBuffer {
            TLongArrayList docNums = new TLongArrayList();
            TIntArrayList docNumTupleIdx = new TIntArrayList();
            int docNumReadIdx = 0;
                            
            double[] scores;
            int writeTupleIndex = 0;
            int readTupleIndex = 0;
            int batchSize;

            public ShreddedBuffer(int batchSize) {
                this.batchSize = batchSize;

                scores = new double[batchSize];
            }                              

            public ShreddedBuffer() {    
                this(10000);
            }                                                                                                                    
            
            public void processDocNum(long docNum) {
                docNums.add(docNum);
                docNumTupleIdx.add(writeTupleIndex);
            }                                      
            public void processTuple(double score) {
                assert docNums.size() > 0;
                scores[writeTupleIndex] = score;
                writeTupleIndex++;
            }
            public void resetData() {
                docNums.clear();
                docNumTupleIdx.clear();
                writeTupleIndex = 0;
            }                  
                                 
            public void resetRead() {
                readTupleIndex = 0;
                docNumReadIdx = 0;
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
            public void incrementDocNum() {
                docNumReadIdx++;  
            }                                                                                              

            public void autoIncrementDocNum() {
                while (readTupleIndex >= getDocNumEndIndex() && readTupleIndex < writeTupleIndex)
                    docNumReadIdx++;
            }                 
            public void incrementTuple() {
                readTupleIndex++;
            }                    
            public int getDocNumEndIndex() {
                if ((docNumReadIdx+1) >= docNumTupleIdx.size())
                    return writeTupleIndex;
                return docNumTupleIdx.get(docNumReadIdx+1);
            }
            public int getReadIndex() {
                return readTupleIndex;
            }   

            public int getWriteIndex() {
                return writeTupleIndex;
            } 
            public long getDocNum() {
                assert readTupleIndex < writeTupleIndex;
                assert docNumReadIdx < docNums.size();
                
                return docNums.get(docNumReadIdx);
            }
            public double getScore() {
                assert readTupleIndex < writeTupleIndex;
                return scores[readTupleIndex];
            }                                         
            public void copyTuples(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                   output.processTuple(getScore());
                   incrementTuple();
                }
            }                                                                           
            public void copyUntilIndexDocNum(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                    output.processDocNum(getDocNum());
                    assert getDocNumEndIndex() <= endIndex;
                    copyTuples(getDocNumEndIndex(), output);
                    incrementDocNum();
                }
            }  
            public void copyUntilDocNum(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                while (!isAtEnd()) {
                    if (other != null) {   
                        assert !other.isAtEnd();
                        int c = + Utility.compare(getDocNum(), other.getDocNum());
                    
                        if (c > 0) {
                            break;   
                        }
                        
                        output.processDocNum(getDocNum());
                                      
                        copyTuples(getDocNumEndIndex(), output);
                    } else {
                        output.processDocNum(getDocNum());
                        copyTuples(getDocNumEndIndex(), output);
                    }
                    incrementDocNum();  
                    
               
                }
            }
            public void copyUntil(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                copyUntilDocNum(other, output);
            }
            
        }                         
        public static class ShreddedCombiner implements ReaderSource<PREntry>, ShreddedSource {   
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
                } else if (processor instanceof PREntry.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((PREntry.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<PREntry>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<PREntry> getOutputClass() {
                return PREntry.class;
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

            public PREntry read() throws IOException {
                if (uninitialized)
                    initialize();

                PREntry result = null;

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
        public static class ShreddedReader implements Step, Comparable<ShreddedReader>, TypeReader<PREntry>, ShreddedSource {      
            public ShreddedProcessor processor;
            ShreddedBuffer buffer;
            PREntry last = new PREntry();         
            long updateDocNumCount = -1;
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
                    result = + Utility.compare(buffer.getDocNum(), otherBuffer.getDocNum());
                    if(result != 0) break;
                } while (false);                                             
                
                return result;
            }
            
            public final ShreddedBuffer getBuffer() {
                return buffer;
            }                
            
            public final PREntry read() throws IOException {
                if (buffer.isAtEnd()) {
                    fill();             
                
                    if (buffer.isAtEnd()) {
                        return null;
                    }
                }
                      
                assert !buffer.isAtEnd();
                PREntry result = new PREntry();
                
                result.docNum = buffer.getDocNum();
                result.score = buffer.getScore();
                
                buffer.incrementTuple();
                buffer.autoIncrementDocNum();
                
                return result;
            }           
            
            public final void fill() throws IOException {
                try {   
                    buffer.reset();
                    
                    if (tupleCount != 0) {
                                                      
                        if(updateDocNumCount - tupleCount > 0) {
                            buffer.docNums.add(last.docNum);
                            buffer.docNumTupleIdx.add((int) (updateDocNumCount - tupleCount));
                        }
                        bufferStartCount = tupleCount;
                    }
                    
                    while (!buffer.isFull()) {
                        updateDocNum();
                        buffer.processTuple(input.readDouble());
                        tupleCount++;
                    }
                } catch(EOFException e) {}
            }

            public final void updateDocNum() throws IOException {
                if (updateDocNumCount > tupleCount)
                    return;
                     
                last.docNum = input.readLong();
                updateDocNumCount = tupleCount + input.readInt();
                                      
                buffer.processDocNum(last.docNum);
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
                } else if (processor instanceof PREntry.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((PREntry.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<PREntry>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<PREntry> getOutputClass() {
                return PREntry.class;
            }                
        }
        
        public static class DuplicateEliminator implements ShreddedProcessor {
            public ShreddedProcessor processor;
            PREntry last = new PREntry();
            boolean docNumProcess = true;
                                           
            public DuplicateEliminator() {}
            public DuplicateEliminator(ShreddedProcessor processor) {
                this.processor = processor;
            }
            
            public void setShreddedProcessor(ShreddedProcessor processor) {
                this.processor = processor;
            }

            public void processDocNum(long docNum) throws IOException {  
                if (docNumProcess || Utility.compare(docNum, last.docNum) != 0) {
                    last.docNum = docNum;
                    processor.processDocNum(docNum);
                    docNumProcess = false;
                }
            }  
            
            public void resetDocNum() {
                 docNumProcess = true;
            }                                                
                               
            public void processTuple(double score) throws IOException {
                processor.processTuple(score);
            } 
            
            public void close() throws IOException {
                processor.close();
            }                    
        }
        public static class TupleUnshredder implements ShreddedProcessor {
            PREntry last = new PREntry();
            public org.lemurproject.galago.tupleflow.Processor<PREntry> processor;                               
            
            public TupleUnshredder(PREntry.Processor processor) {
                this.processor = processor;
            }         
            
            public TupleUnshredder(org.lemurproject.galago.tupleflow.Processor<PREntry> processor) {
                this.processor = processor;
            }
            
            public PREntry clone(PREntry object) {
                PREntry result = new PREntry();
                if (object == null) return result;
                result.docNum = object.docNum; 
                result.score = object.score; 
                return result;
            }                 
            
            public void processDocNum(long docNum) throws IOException {
                last.docNum = docNum;
            }   
                
            
            public void processTuple(double score) throws IOException {
                last.score = score;
                processor.process(clone(last));
            }               
            
            public void close() throws IOException {
                processor.close();
            }
        }     
        public static class TupleShredder implements Processor {
            PREntry last = null;
            public ShreddedProcessor processor;
            
            public TupleShredder(ShreddedProcessor processor) {
                this.processor = processor;
            }                              
            
            public PREntry clone(PREntry object) {
                PREntry result = new PREntry();
                if (object == null) return result;
                result.docNum = object.docNum; 
                result.score = object.score; 
                return result;
            }                 
            
            public void process(PREntry object) throws IOException {                                                                                                                                                   
                boolean processAll = false;
                if(last == null || Utility.compare(last.docNum, object.docNum) != 0 || processAll) { processor.processDocNum(object.docNum); processAll = true; }
                processor.processTuple(object.score);                                         
                last = object;
            }
                          
            public Class<PREntry> getInputClass() {
                return PREntry.class;
            }
            
            public void close() throws IOException {
                processor.close();
            }                     
        }
    } 
}    