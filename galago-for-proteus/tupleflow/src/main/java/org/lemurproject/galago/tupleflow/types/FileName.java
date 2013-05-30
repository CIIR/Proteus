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


public class FileName implements Type<FileName> {
    public String filename; 
    
    public FileName() {}
    public FileName(String filename) {
        this.filename = filename;
    }  
    
    public String toString() {
            return String.format("%s",
                                   filename);
    } 

    public Order<FileName> getOrder(String... spec) {
        if (Arrays.equals(spec, new String[] { "+filename" })) {
            return new FilenameOrder();
        }
        return null;
    } 
      
    public interface Processor extends Step, org.lemurproject.galago.tupleflow.Processor<FileName> {
        public void process(FileName object) throws IOException;
        public void close() throws IOException;
    }                        
    public interface Source extends Step {
    }
    public static class FilenameOrder implements Order<FileName> {
        public int hash(FileName object) {
            int h = 0;
            h += Utility.hash(object.filename);
            return h;
        } 
        public Comparator<FileName> greaterThan() {
            return new Comparator<FileName>() {
                public int compare(FileName one, FileName two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.filename, two.filename);
                        if(result != 0) break;
                    } while (false);
                    return -result;
                }
            };
        }     
        public Comparator<FileName> lessThan() {
            return new Comparator<FileName>() {
                public int compare(FileName one, FileName two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.filename, two.filename);
                        if(result != 0) break;
                    } while (false);
                    return result;
                }
            };
        }     
        public TypeReader<FileName> orderedReader(ArrayInput _input) {
            return new ShreddedReader(_input);
        }    

        public TypeReader<FileName> orderedReader(ArrayInput _input, int bufferSize) {
            return new ShreddedReader(_input, bufferSize);
        }    
        public OrderedWriter<FileName> orderedWriter(ArrayOutput _output) {
            ShreddedWriter w = new ShreddedWriter(_output);
            return new OrderedWriterClass(w); 
        }                                    
        public static class OrderedWriterClass extends OrderedWriter< FileName > {
            FileName last = null;
            ShreddedWriter shreddedWriter = null; 
            
            public OrderedWriterClass(ShreddedWriter s) {
                this.shreddedWriter = s;
            }
            
            public void process(FileName object) throws IOException {
               boolean processAll = false;
               if (processAll || last == null || 0 != Utility.compare(object.filename, last.filename)) { processAll = true; shreddedWriter.processFilename(object.filename); }
               shreddedWriter.processTuple();
               last = object;
            }           
                 
            public void close() throws IOException {
                shreddedWriter.close();
            }
            
            public Class<FileName> getInputClass() {
                return FileName.class;
            }
        } 
        public ReaderSource<FileName> orderedCombiner(Collection<TypeReader<FileName>> readers, boolean closeOnExit) {
            ArrayList<ShreddedReader> shreddedReaders = new ArrayList();
            
            for (TypeReader<FileName> reader : readers) {
                shreddedReaders.add((ShreddedReader)reader);
            }
            
            return new ShreddedCombiner(shreddedReaders, closeOnExit);
        }                  
        public FileName clone(FileName object) {
            FileName result = new FileName();
            if (object == null) return result;
            result.filename = object.filename; 
            return result;
        }                 
        public Class<FileName> getOrderedClass() {
            return FileName.class;
        }                           
        public String[] getOrderSpec() {
            return new String[] {"+filename"};
        }

        public static String[] getSpec() {
            return new String[] {"+filename"};
        }
        public static String getSpecString() {
            return "+filename";
        }
                           
        public interface ShreddedProcessor extends Step {
            public void processFilename(String filename) throws IOException;
            public void processTuple() throws IOException;
            public void close() throws IOException;
        }    
        public interface ShreddedSource extends Step {
        }                                              
        
        public static class ShreddedWriter implements ShreddedProcessor {
            ArrayOutput output;
            ShreddedBuffer buffer = new ShreddedBuffer();
            String lastFilename;
            boolean lastFlush = false;
            
            public ShreddedWriter(ArrayOutput output) {
                this.output = output;
            }                        
            
            public void close() throws IOException {
                flush();
            }
            
            public void processFilename(String filename) {
                lastFilename = filename;
                buffer.processFilename(filename);
            }
            public final void processTuple() throws IOException {
                if (lastFlush) {
                    if(buffer.filenames.size() == 0) buffer.processFilename(lastFilename);
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
            public final void flushFilename(int pauseIndex) throws IOException {
                while (buffer.getReadIndex() < pauseIndex) {
                    int nextPause = buffer.getFilenameEndIndex();
                    int count = nextPause - buffer.getReadIndex();
                    
                    output.writeString(buffer.getFilename());
                    output.writeInt(count);
                    buffer.incrementFilename();
                      
                    flushTuples(nextPause);
                    assert nextPause == buffer.getReadIndex();
                }
            }
            public void flush() throws IOException { 
                flushFilename(buffer.getWriteIndex());
                buffer.reset(); 
                lastFlush = true;
            }                           
        }
        public static class ShreddedBuffer {
            ArrayList<String> filenames = new ArrayList();
            TIntArrayList filenameTupleIdx = new TIntArrayList();
            int filenameReadIdx = 0;
                            
            int writeTupleIndex = 0;
            int readTupleIndex = 0;
            int batchSize;

            public ShreddedBuffer(int batchSize) {
                this.batchSize = batchSize;

            }                              

            public ShreddedBuffer() {    
                this(10000);
            }                                                                                                                    
            
            public void processFilename(String filename) {
                filenames.add(filename);
                filenameTupleIdx.add(writeTupleIndex);
            }                                      
            public void processTuple() {
                assert filenames.size() > 0;
                writeTupleIndex++;
            }
            public void resetData() {
                filenames.clear();
                filenameTupleIdx.clear();
                writeTupleIndex = 0;
            }                  
                                 
            public void resetRead() {
                readTupleIndex = 0;
                filenameReadIdx = 0;
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
            public void incrementFilename() {
                filenameReadIdx++;  
            }                                                                                              

            public void autoIncrementFilename() {
                while (readTupleIndex >= getFilenameEndIndex() && readTupleIndex < writeTupleIndex)
                    filenameReadIdx++;
            }                 
            public void incrementTuple() {
                readTupleIndex++;
            }                    
            public int getFilenameEndIndex() {
                if ((filenameReadIdx+1) >= filenameTupleIdx.size())
                    return writeTupleIndex;
                return filenameTupleIdx.get(filenameReadIdx+1);
            }
            public int getReadIndex() {
                return readTupleIndex;
            }   

            public int getWriteIndex() {
                return writeTupleIndex;
            } 
            public String getFilename() {
                assert readTupleIndex < writeTupleIndex;
                assert filenameReadIdx < filenames.size();
                
                return filenames.get(filenameReadIdx);
            }

            public void copyTuples(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                   output.processTuple();
                   incrementTuple();
                }
            }                                                                           
            public void copyUntilIndexFilename(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                    output.processFilename(getFilename());
                    assert getFilenameEndIndex() <= endIndex;
                    copyTuples(getFilenameEndIndex(), output);
                    incrementFilename();
                }
            }  
            public void copyUntilFilename(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                while (!isAtEnd()) {
                    if (other != null) {   
                        assert !other.isAtEnd();
                        int c = + Utility.compare(getFilename(), other.getFilename());
                    
                        if (c > 0) {
                            break;   
                        }
                        
                        output.processFilename(getFilename());
                                      
                        copyTuples(getFilenameEndIndex(), output);
                    } else {
                        output.processFilename(getFilename());
                        copyTuples(getFilenameEndIndex(), output);
                    }
                    incrementFilename();  
                    
               
                }
            }
            public void copyUntil(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                copyUntilFilename(other, output);
            }
            
        }                         
        public static class ShreddedCombiner implements ReaderSource<FileName>, ShreddedSource {   
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
                } else if (processor instanceof FileName.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((FileName.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<FileName>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<FileName> getOutputClass() {
                return FileName.class;
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

            public FileName read() throws IOException {
                if (uninitialized)
                    initialize();

                FileName result = null;

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
        public static class ShreddedReader implements Step, Comparable<ShreddedReader>, TypeReader<FileName>, ShreddedSource {      
            public ShreddedProcessor processor;
            ShreddedBuffer buffer;
            FileName last = new FileName();         
            long updateFilenameCount = -1;
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
                    result = + Utility.compare(buffer.getFilename(), otherBuffer.getFilename());
                    if(result != 0) break;
                } while (false);                                             
                
                return result;
            }
            
            public final ShreddedBuffer getBuffer() {
                return buffer;
            }                
            
            public final FileName read() throws IOException {
                if (buffer.isAtEnd()) {
                    fill();             
                
                    if (buffer.isAtEnd()) {
                        return null;
                    }
                }
                      
                assert !buffer.isAtEnd();
                FileName result = new FileName();
                
                result.filename = buffer.getFilename();
                
                buffer.incrementTuple();
                buffer.autoIncrementFilename();
                
                return result;
            }           
            
            public final void fill() throws IOException {
                try {   
                    buffer.reset();
                    
                    if (tupleCount != 0) {
                                                      
                        if(updateFilenameCount - tupleCount > 0) {
                            buffer.filenames.add(last.filename);
                            buffer.filenameTupleIdx.add((int) (updateFilenameCount - tupleCount));
                        }
                        bufferStartCount = tupleCount;
                    }
                    
                    while (!buffer.isFull()) {
                        updateFilename();
                        buffer.processTuple();
                        tupleCount++;
                    }
                } catch(EOFException e) {}
            }

            public final void updateFilename() throws IOException {
                if (updateFilenameCount > tupleCount)
                    return;
                     
                last.filename = input.readString();
                updateFilenameCount = tupleCount + input.readInt();
                                      
                buffer.processFilename(last.filename);
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
                } else if (processor instanceof FileName.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((FileName.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<FileName>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<FileName> getOutputClass() {
                return FileName.class;
            }                
        }
        
        public static class DuplicateEliminator implements ShreddedProcessor {
            public ShreddedProcessor processor;
            FileName last = new FileName();
            boolean filenameProcess = true;
                                           
            public DuplicateEliminator() {}
            public DuplicateEliminator(ShreddedProcessor processor) {
                this.processor = processor;
            }
            
            public void setShreddedProcessor(ShreddedProcessor processor) {
                this.processor = processor;
            }

            public void processFilename(String filename) throws IOException {  
                if (filenameProcess || Utility.compare(filename, last.filename) != 0) {
                    last.filename = filename;
                    processor.processFilename(filename);
                    filenameProcess = false;
                }
            }  
            
            public void resetFilename() {
                 filenameProcess = true;
            }                                                
                               
            public void processTuple() throws IOException {
                processor.processTuple();
            } 
            
            public void close() throws IOException {
                processor.close();
            }                    
        }
        public static class TupleUnshredder implements ShreddedProcessor {
            FileName last = new FileName();
            public org.lemurproject.galago.tupleflow.Processor<FileName> processor;                               
            
            public TupleUnshredder(FileName.Processor processor) {
                this.processor = processor;
            }         
            
            public TupleUnshredder(org.lemurproject.galago.tupleflow.Processor<FileName> processor) {
                this.processor = processor;
            }
            
            public FileName clone(FileName object) {
                FileName result = new FileName();
                if (object == null) return result;
                result.filename = object.filename; 
                return result;
            }                 
            
            public void processFilename(String filename) throws IOException {
                last.filename = filename;
            }   
                
            
            public void processTuple() throws IOException {
                processor.process(clone(last));
            }               
            
            public void close() throws IOException {
                processor.close();
            }
        }     
        public static class TupleShredder implements Processor {
            FileName last = null;
            public ShreddedProcessor processor;
            
            public TupleShredder(ShreddedProcessor processor) {
                this.processor = processor;
            }                              
            
            public FileName clone(FileName object) {
                FileName result = new FileName();
                if (object == null) return result;
                result.filename = object.filename; 
                return result;
            }                 
            
            public void process(FileName object) throws IOException {                                                                                                                                                   
                boolean processAll = false;
                if(last == null || Utility.compare(last.filename, object.filename) != 0 || processAll) { processor.processFilename(object.filename); processAll = true; }
                processor.processTuple();                                         
                last = object;
            }
                          
            public Class<FileName> getInputClass() {
                return FileName.class;
            }
            
            public void close() throws IOException {
                processor.close();
            }                     
        }
    } 
}    