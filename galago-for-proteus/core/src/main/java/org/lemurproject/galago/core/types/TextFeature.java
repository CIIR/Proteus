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


public class TextFeature implements Type<TextFeature> {
    public int file;
    public long filePosition;
    public byte[] feature; 
    
    public TextFeature() {}
    public TextFeature(int file, long filePosition, byte[] feature) {
        this.file = file;
        this.filePosition = filePosition;
        this.feature = feature;
    }  
    
    public String toString() {
        try {
            return String.format("%d,%d,%s",
                                   file, filePosition, new String(feature, "UTF-8"));
        } catch(UnsupportedEncodingException e) {
            throw new RuntimeException("Couldn't convert string to UTF-8.");
        }
    } 

    public Order<TextFeature> getOrder(String... spec) {
        if (Arrays.equals(spec, new String[] { "+feature" })) {
            return new FeatureOrder();
        }
        if (Arrays.equals(spec, new String[] { "+file", "+filePosition" })) {
            return new FileFilePositionOrder();
        }
        if (Arrays.equals(spec, new String[] { "+file" })) {
            return new FileOrder();
        }
        return null;
    } 
      
    public interface Processor extends Step, org.lemurproject.galago.tupleflow.Processor<TextFeature> {
        public void process(TextFeature object) throws IOException;
        public void close() throws IOException;
    }                        
    public interface Source extends Step {
    }
    public static class FeatureOrder implements Order<TextFeature> {
        public int hash(TextFeature object) {
            int h = 0;
            h += Utility.hash(object.feature);
            return h;
        } 
        public Comparator<TextFeature> greaterThan() {
            return new Comparator<TextFeature>() {
                public int compare(TextFeature one, TextFeature two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.feature, two.feature);
                        if(result != 0) break;
                    } while (false);
                    return -result;
                }
            };
        }     
        public Comparator<TextFeature> lessThan() {
            return new Comparator<TextFeature>() {
                public int compare(TextFeature one, TextFeature two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.feature, two.feature);
                        if(result != 0) break;
                    } while (false);
                    return result;
                }
            };
        }     
        public TypeReader<TextFeature> orderedReader(ArrayInput _input) {
            return new ShreddedReader(_input);
        }    

        public TypeReader<TextFeature> orderedReader(ArrayInput _input, int bufferSize) {
            return new ShreddedReader(_input, bufferSize);
        }    
        public OrderedWriter<TextFeature> orderedWriter(ArrayOutput _output) {
            ShreddedWriter w = new ShreddedWriter(_output);
            return new OrderedWriterClass(w); 
        }                                    
        public static class OrderedWriterClass extends OrderedWriter< TextFeature > {
            TextFeature last = null;
            ShreddedWriter shreddedWriter = null; 
            
            public OrderedWriterClass(ShreddedWriter s) {
                this.shreddedWriter = s;
            }
            
            public void process(TextFeature object) throws IOException {
               boolean processAll = false;
               if (processAll || last == null || 0 != Utility.compare(object.feature, last.feature)) { processAll = true; shreddedWriter.processFeature(object.feature); }
               shreddedWriter.processTuple(object.file, object.filePosition);
               last = object;
            }           
                 
            public void close() throws IOException {
                shreddedWriter.close();
            }
            
            public Class<TextFeature> getInputClass() {
                return TextFeature.class;
            }
        } 
        public ReaderSource<TextFeature> orderedCombiner(Collection<TypeReader<TextFeature>> readers, boolean closeOnExit) {
            ArrayList<ShreddedReader> shreddedReaders = new ArrayList();
            
            for (TypeReader<TextFeature> reader : readers) {
                shreddedReaders.add((ShreddedReader)reader);
            }
            
            return new ShreddedCombiner(shreddedReaders, closeOnExit);
        }                  
        public TextFeature clone(TextFeature object) {
            TextFeature result = new TextFeature();
            if (object == null) return result;
            result.file = object.file; 
            result.filePosition = object.filePosition; 
            result.feature = object.feature; 
            return result;
        }                 
        public Class<TextFeature> getOrderedClass() {
            return TextFeature.class;
        }                           
        public String[] getOrderSpec() {
            return new String[] {"+feature"};
        }

        public static String[] getSpec() {
            return new String[] {"+feature"};
        }
        public static String getSpecString() {
            return "+feature";
        }
                           
        public interface ShreddedProcessor extends Step {
            public void processFeature(byte[] feature) throws IOException;
            public void processTuple(int file, long filePosition) throws IOException;
            public void close() throws IOException;
        }    
        public interface ShreddedSource extends Step {
        }                                              
        
        public static class ShreddedWriter implements ShreddedProcessor {
            ArrayOutput output;
            ShreddedBuffer buffer = new ShreddedBuffer();
            byte[] lastFeature;
            boolean lastFlush = false;
            
            public ShreddedWriter(ArrayOutput output) {
                this.output = output;
            }                        
            
            public void close() throws IOException {
                flush();
            }
            
            public void processFeature(byte[] feature) {
                lastFeature = feature;
                buffer.processFeature(feature);
            }
            public final void processTuple(int file, long filePosition) throws IOException {
                if (lastFlush) {
                    if(buffer.features.size() == 0) buffer.processFeature(lastFeature);
                    lastFlush = false;
                }
                buffer.processTuple(file, filePosition);
                if (buffer.isFull())
                    flush();
            }
            public final void flushTuples(int pauseIndex) throws IOException {
                
                while (buffer.getReadIndex() < pauseIndex) {
                           
                    output.writeInt(buffer.getFile());
                    output.writeLong(buffer.getFilePosition());
                    buffer.incrementTuple();
                }
            }  
            public final void flushFeature(int pauseIndex) throws IOException {
                while (buffer.getReadIndex() < pauseIndex) {
                    int nextPause = buffer.getFeatureEndIndex();
                    int count = nextPause - buffer.getReadIndex();
                    
                    output.writeBytes(buffer.getFeature());
                    output.writeInt(count);
                    buffer.incrementFeature();
                      
                    flushTuples(nextPause);
                    assert nextPause == buffer.getReadIndex();
                }
            }
            public void flush() throws IOException { 
                flushFeature(buffer.getWriteIndex());
                buffer.reset(); 
                lastFlush = true;
            }                           
        }
        public static class ShreddedBuffer {
            ArrayList<byte[]> features = new ArrayList();
            TIntArrayList featureTupleIdx = new TIntArrayList();
            int featureReadIdx = 0;
                            
            int[] files;
            long[] filePositions;
            int writeTupleIndex = 0;
            int readTupleIndex = 0;
            int batchSize;

            public ShreddedBuffer(int batchSize) {
                this.batchSize = batchSize;

                files = new int[batchSize];
                filePositions = new long[batchSize];
            }                              

            public ShreddedBuffer() {    
                this(10000);
            }                                                                                                                    
            
            public void processFeature(byte[] feature) {
                features.add(feature);
                featureTupleIdx.add(writeTupleIndex);
            }                                      
            public void processTuple(int file, long filePosition) {
                assert features.size() > 0;
                files[writeTupleIndex] = file;
                filePositions[writeTupleIndex] = filePosition;
                writeTupleIndex++;
            }
            public void resetData() {
                features.clear();
                featureTupleIdx.clear();
                writeTupleIndex = 0;
            }                  
                                 
            public void resetRead() {
                readTupleIndex = 0;
                featureReadIdx = 0;
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
            public void incrementFeature() {
                featureReadIdx++;  
            }                                                                                              

            public void autoIncrementFeature() {
                while (readTupleIndex >= getFeatureEndIndex() && readTupleIndex < writeTupleIndex)
                    featureReadIdx++;
            }                 
            public void incrementTuple() {
                readTupleIndex++;
            }                    
            public int getFeatureEndIndex() {
                if ((featureReadIdx+1) >= featureTupleIdx.size())
                    return writeTupleIndex;
                return featureTupleIdx.get(featureReadIdx+1);
            }
            public int getReadIndex() {
                return readTupleIndex;
            }   

            public int getWriteIndex() {
                return writeTupleIndex;
            } 
            public byte[] getFeature() {
                assert readTupleIndex < writeTupleIndex;
                assert featureReadIdx < features.size();
                
                return features.get(featureReadIdx);
            }
            public int getFile() {
                assert readTupleIndex < writeTupleIndex;
                return files[readTupleIndex];
            }                                         
            public long getFilePosition() {
                assert readTupleIndex < writeTupleIndex;
                return filePositions[readTupleIndex];
            }                                         
            public void copyTuples(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                   output.processTuple(getFile(), getFilePosition());
                   incrementTuple();
                }
            }                                                                           
            public void copyUntilIndexFeature(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                    output.processFeature(getFeature());
                    assert getFeatureEndIndex() <= endIndex;
                    copyTuples(getFeatureEndIndex(), output);
                    incrementFeature();
                }
            }  
            public void copyUntilFeature(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                while (!isAtEnd()) {
                    if (other != null) {   
                        assert !other.isAtEnd();
                        int c = + Utility.compare(getFeature(), other.getFeature());
                    
                        if (c > 0) {
                            break;   
                        }
                        
                        output.processFeature(getFeature());
                                      
                        copyTuples(getFeatureEndIndex(), output);
                    } else {
                        output.processFeature(getFeature());
                        copyTuples(getFeatureEndIndex(), output);
                    }
                    incrementFeature();  
                    
               
                }
            }
            public void copyUntil(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                copyUntilFeature(other, output);
            }
            
        }                         
        public static class ShreddedCombiner implements ReaderSource<TextFeature>, ShreddedSource {   
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
                } else if (processor instanceof TextFeature.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((TextFeature.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<TextFeature>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<TextFeature> getOutputClass() {
                return TextFeature.class;
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

            public TextFeature read() throws IOException {
                if (uninitialized)
                    initialize();

                TextFeature result = null;

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
        public static class ShreddedReader implements Step, Comparable<ShreddedReader>, TypeReader<TextFeature>, ShreddedSource {      
            public ShreddedProcessor processor;
            ShreddedBuffer buffer;
            TextFeature last = new TextFeature();         
            long updateFeatureCount = -1;
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
                    result = + Utility.compare(buffer.getFeature(), otherBuffer.getFeature());
                    if(result != 0) break;
                } while (false);                                             
                
                return result;
            }
            
            public final ShreddedBuffer getBuffer() {
                return buffer;
            }                
            
            public final TextFeature read() throws IOException {
                if (buffer.isAtEnd()) {
                    fill();             
                
                    if (buffer.isAtEnd()) {
                        return null;
                    }
                }
                      
                assert !buffer.isAtEnd();
                TextFeature result = new TextFeature();
                
                result.feature = buffer.getFeature();
                result.file = buffer.getFile();
                result.filePosition = buffer.getFilePosition();
                
                buffer.incrementTuple();
                buffer.autoIncrementFeature();
                
                return result;
            }           
            
            public final void fill() throws IOException {
                try {   
                    buffer.reset();
                    
                    if (tupleCount != 0) {
                                                      
                        if(updateFeatureCount - tupleCount > 0) {
                            buffer.features.add(last.feature);
                            buffer.featureTupleIdx.add((int) (updateFeatureCount - tupleCount));
                        }
                        bufferStartCount = tupleCount;
                    }
                    
                    while (!buffer.isFull()) {
                        updateFeature();
                        buffer.processTuple(input.readInt(), input.readLong());
                        tupleCount++;
                    }
                } catch(EOFException e) {}
            }

            public final void updateFeature() throws IOException {
                if (updateFeatureCount > tupleCount)
                    return;
                     
                last.feature = input.readBytes();
                updateFeatureCount = tupleCount + input.readInt();
                                      
                buffer.processFeature(last.feature);
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
                } else if (processor instanceof TextFeature.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((TextFeature.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<TextFeature>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<TextFeature> getOutputClass() {
                return TextFeature.class;
            }                
        }
        
        public static class DuplicateEliminator implements ShreddedProcessor {
            public ShreddedProcessor processor;
            TextFeature last = new TextFeature();
            boolean featureProcess = true;
                                           
            public DuplicateEliminator() {}
            public DuplicateEliminator(ShreddedProcessor processor) {
                this.processor = processor;
            }
            
            public void setShreddedProcessor(ShreddedProcessor processor) {
                this.processor = processor;
            }

            public void processFeature(byte[] feature) throws IOException {  
                if (featureProcess || Utility.compare(feature, last.feature) != 0) {
                    last.feature = feature;
                    processor.processFeature(feature);
                    featureProcess = false;
                }
            }  
            
            public void resetFeature() {
                 featureProcess = true;
            }                                                
                               
            public void processTuple(int file, long filePosition) throws IOException {
                processor.processTuple(file, filePosition);
            } 
            
            public void close() throws IOException {
                processor.close();
            }                    
        }
        public static class TupleUnshredder implements ShreddedProcessor {
            TextFeature last = new TextFeature();
            public org.lemurproject.galago.tupleflow.Processor<TextFeature> processor;                               
            
            public TupleUnshredder(TextFeature.Processor processor) {
                this.processor = processor;
            }         
            
            public TupleUnshredder(org.lemurproject.galago.tupleflow.Processor<TextFeature> processor) {
                this.processor = processor;
            }
            
            public TextFeature clone(TextFeature object) {
                TextFeature result = new TextFeature();
                if (object == null) return result;
                result.file = object.file; 
                result.filePosition = object.filePosition; 
                result.feature = object.feature; 
                return result;
            }                 
            
            public void processFeature(byte[] feature) throws IOException {
                last.feature = feature;
            }   
                
            
            public void processTuple(int file, long filePosition) throws IOException {
                last.file = file;
                last.filePosition = filePosition;
                processor.process(clone(last));
            }               
            
            public void close() throws IOException {
                processor.close();
            }
        }     
        public static class TupleShredder implements Processor {
            TextFeature last = null;
            public ShreddedProcessor processor;
            
            public TupleShredder(ShreddedProcessor processor) {
                this.processor = processor;
            }                              
            
            public TextFeature clone(TextFeature object) {
                TextFeature result = new TextFeature();
                if (object == null) return result;
                result.file = object.file; 
                result.filePosition = object.filePosition; 
                result.feature = object.feature; 
                return result;
            }                 
            
            public void process(TextFeature object) throws IOException {                                                                                                                                                   
                boolean processAll = false;
                if(last == null || Utility.compare(last.feature, object.feature) != 0 || processAll) { processor.processFeature(object.feature); processAll = true; }
                processor.processTuple(object.file, object.filePosition);                                         
                last = object;
            }
                          
            public Class<TextFeature> getInputClass() {
                return TextFeature.class;
            }
            
            public void close() throws IOException {
                processor.close();
            }                     
        }
    } 
    public static class FileFilePositionOrder implements Order<TextFeature> {
        public int hash(TextFeature object) {
            int h = 0;
            h += Utility.hash(object.file);
            h += Utility.hash(object.filePosition);
            return h;
        } 
        public Comparator<TextFeature> greaterThan() {
            return new Comparator<TextFeature>() {
                public int compare(TextFeature one, TextFeature two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.file, two.file);
                        if(result != 0) break;
                        result = + Utility.compare(one.filePosition, two.filePosition);
                        if(result != 0) break;
                    } while (false);
                    return -result;
                }
            };
        }     
        public Comparator<TextFeature> lessThan() {
            return new Comparator<TextFeature>() {
                public int compare(TextFeature one, TextFeature two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.file, two.file);
                        if(result != 0) break;
                        result = + Utility.compare(one.filePosition, two.filePosition);
                        if(result != 0) break;
                    } while (false);
                    return result;
                }
            };
        }     
        public TypeReader<TextFeature> orderedReader(ArrayInput _input) {
            return new ShreddedReader(_input);
        }    

        public TypeReader<TextFeature> orderedReader(ArrayInput _input, int bufferSize) {
            return new ShreddedReader(_input, bufferSize);
        }    
        public OrderedWriter<TextFeature> orderedWriter(ArrayOutput _output) {
            ShreddedWriter w = new ShreddedWriter(_output);
            return new OrderedWriterClass(w); 
        }                                    
        public static class OrderedWriterClass extends OrderedWriter< TextFeature > {
            TextFeature last = null;
            ShreddedWriter shreddedWriter = null; 
            
            public OrderedWriterClass(ShreddedWriter s) {
                this.shreddedWriter = s;
            }
            
            public void process(TextFeature object) throws IOException {
               boolean processAll = false;
               if (processAll || last == null || 0 != Utility.compare(object.file, last.file)) { processAll = true; shreddedWriter.processFile(object.file); }
               if (processAll || last == null || 0 != Utility.compare(object.filePosition, last.filePosition)) { processAll = true; shreddedWriter.processFilePosition(object.filePosition); }
               shreddedWriter.processTuple(object.feature);
               last = object;
            }           
                 
            public void close() throws IOException {
                shreddedWriter.close();
            }
            
            public Class<TextFeature> getInputClass() {
                return TextFeature.class;
            }
        } 
        public ReaderSource<TextFeature> orderedCombiner(Collection<TypeReader<TextFeature>> readers, boolean closeOnExit) {
            ArrayList<ShreddedReader> shreddedReaders = new ArrayList();
            
            for (TypeReader<TextFeature> reader : readers) {
                shreddedReaders.add((ShreddedReader)reader);
            }
            
            return new ShreddedCombiner(shreddedReaders, closeOnExit);
        }                  
        public TextFeature clone(TextFeature object) {
            TextFeature result = new TextFeature();
            if (object == null) return result;
            result.file = object.file; 
            result.filePosition = object.filePosition; 
            result.feature = object.feature; 
            return result;
        }                 
        public Class<TextFeature> getOrderedClass() {
            return TextFeature.class;
        }                           
        public String[] getOrderSpec() {
            return new String[] {"+file", "+filePosition"};
        }

        public static String[] getSpec() {
            return new String[] {"+file", "+filePosition"};
        }
        public static String getSpecString() {
            return "+file +filePosition";
        }
                           
        public interface ShreddedProcessor extends Step {
            public void processFile(int file) throws IOException;
            public void processFilePosition(long filePosition) throws IOException;
            public void processTuple(byte[] feature) throws IOException;
            public void close() throws IOException;
        }    
        public interface ShreddedSource extends Step {
        }                                              
        
        public static class ShreddedWriter implements ShreddedProcessor {
            ArrayOutput output;
            ShreddedBuffer buffer = new ShreddedBuffer();
            int lastFile;
            long lastFilePosition;
            boolean lastFlush = false;
            
            public ShreddedWriter(ArrayOutput output) {
                this.output = output;
            }                        
            
            public void close() throws IOException {
                flush();
            }
            
            public void processFile(int file) {
                lastFile = file;
                buffer.processFile(file);
            }
            public void processFilePosition(long filePosition) {
                lastFilePosition = filePosition;
                buffer.processFilePosition(filePosition);
            }
            public final void processTuple(byte[] feature) throws IOException {
                if (lastFlush) {
                    if(buffer.files.size() == 0) buffer.processFile(lastFile);
                    if(buffer.filePositions.size() == 0) buffer.processFilePosition(lastFilePosition);
                    lastFlush = false;
                }
                buffer.processTuple(feature);
                if (buffer.isFull())
                    flush();
            }
            public final void flushTuples(int pauseIndex) throws IOException {
                
                while (buffer.getReadIndex() < pauseIndex) {
                           
                    output.writeBytes(buffer.getFeature());
                    buffer.incrementTuple();
                }
            }  
            public final void flushFile(int pauseIndex) throws IOException {
                while (buffer.getReadIndex() < pauseIndex) {
                    int nextPause = buffer.getFileEndIndex();
                    int count = nextPause - buffer.getReadIndex();
                    
                    output.writeInt(buffer.getFile());
                    output.writeInt(count);
                    buffer.incrementFile();
                      
                    flushFilePosition(nextPause);
                    assert nextPause == buffer.getReadIndex();
                }
            }
            public final void flushFilePosition(int pauseIndex) throws IOException {
                while (buffer.getReadIndex() < pauseIndex) {
                    int nextPause = buffer.getFilePositionEndIndex();
                    int count = nextPause - buffer.getReadIndex();
                    
                    output.writeLong(buffer.getFilePosition());
                    output.writeInt(count);
                    buffer.incrementFilePosition();
                      
                    flushTuples(nextPause);
                    assert nextPause == buffer.getReadIndex();
                }
            }
            public void flush() throws IOException { 
                flushFile(buffer.getWriteIndex());
                buffer.reset(); 
                lastFlush = true;
            }                           
        }
        public static class ShreddedBuffer {
            TIntArrayList files = new TIntArrayList();
            TLongArrayList filePositions = new TLongArrayList();
            TIntArrayList fileTupleIdx = new TIntArrayList();
            TIntArrayList filePositionTupleIdx = new TIntArrayList();
            int fileReadIdx = 0;
            int filePositionReadIdx = 0;
                            
            byte[][] features;
            int writeTupleIndex = 0;
            int readTupleIndex = 0;
            int batchSize;

            public ShreddedBuffer(int batchSize) {
                this.batchSize = batchSize;

                features = new byte[batchSize][];
            }                              

            public ShreddedBuffer() {    
                this(10000);
            }                                                                                                                    
            
            public void processFile(int file) {
                files.add(file);
                fileTupleIdx.add(writeTupleIndex);
            }                                      
            public void processFilePosition(long filePosition) {
                filePositions.add(filePosition);
                filePositionTupleIdx.add(writeTupleIndex);
            }                                      
            public void processTuple(byte[] feature) {
                assert files.size() > 0;
                assert filePositions.size() > 0;
                features[writeTupleIndex] = feature;
                writeTupleIndex++;
            }
            public void resetData() {
                files.clear();
                filePositions.clear();
                fileTupleIdx.clear();
                filePositionTupleIdx.clear();
                writeTupleIndex = 0;
            }                  
                                 
            public void resetRead() {
                readTupleIndex = 0;
                fileReadIdx = 0;
                filePositionReadIdx = 0;
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
            public void incrementFile() {
                fileReadIdx++;  
            }                                                                                              

            public void autoIncrementFile() {
                while (readTupleIndex >= getFileEndIndex() && readTupleIndex < writeTupleIndex)
                    fileReadIdx++;
            }                 
            public void incrementFilePosition() {
                filePositionReadIdx++;  
            }                                                                                              

            public void autoIncrementFilePosition() {
                while (readTupleIndex >= getFilePositionEndIndex() && readTupleIndex < writeTupleIndex)
                    filePositionReadIdx++;
            }                 
            public void incrementTuple() {
                readTupleIndex++;
            }                    
            public int getFileEndIndex() {
                if ((fileReadIdx+1) >= fileTupleIdx.size())
                    return writeTupleIndex;
                return fileTupleIdx.get(fileReadIdx+1);
            }

            public int getFilePositionEndIndex() {
                if ((filePositionReadIdx+1) >= filePositionTupleIdx.size())
                    return writeTupleIndex;
                return filePositionTupleIdx.get(filePositionReadIdx+1);
            }
            public int getReadIndex() {
                return readTupleIndex;
            }   

            public int getWriteIndex() {
                return writeTupleIndex;
            } 
            public int getFile() {
                assert readTupleIndex < writeTupleIndex;
                assert fileReadIdx < files.size();
                
                return files.get(fileReadIdx);
            }
            public long getFilePosition() {
                assert readTupleIndex < writeTupleIndex;
                assert filePositionReadIdx < filePositions.size();
                
                return filePositions.get(filePositionReadIdx);
            }
            public byte[] getFeature() {
                assert readTupleIndex < writeTupleIndex;
                return features[readTupleIndex];
            }                                         
            public void copyTuples(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                   output.processTuple(getFeature());
                   incrementTuple();
                }
            }                                                                           
            public void copyUntilIndexFile(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                    output.processFile(getFile());
                    assert getFileEndIndex() <= endIndex;
                    copyUntilIndexFilePosition(getFileEndIndex(), output);
                    incrementFile();
                }
            } 
            public void copyUntilIndexFilePosition(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                    output.processFilePosition(getFilePosition());
                    assert getFilePositionEndIndex() <= endIndex;
                    copyTuples(getFilePositionEndIndex(), output);
                    incrementFilePosition();
                }
            }  
            public void copyUntilFile(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                while (!isAtEnd()) {
                    if (other != null) {   
                        assert !other.isAtEnd();
                        int c = + Utility.compare(getFile(), other.getFile());
                    
                        if (c > 0) {
                            break;   
                        }
                        
                        output.processFile(getFile());
                                      
                        if (c < 0) {
                            copyUntilIndexFilePosition(getFileEndIndex(), output);
                        } else if (c == 0) {
                            copyUntilFilePosition(other, output);
                            autoIncrementFile();
                            break;
                        }
                    } else {
                        output.processFile(getFile());
                        copyUntilIndexFilePosition(getFileEndIndex(), output);
                    }
                    incrementFile();  
                    
               
                }
            }
            public void copyUntilFilePosition(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                while (!isAtEnd()) {
                    if (other != null) {   
                        assert !other.isAtEnd();
                        int c = + Utility.compare(getFilePosition(), other.getFilePosition());
                    
                        if (c > 0) {
                            break;   
                        }
                        
                        output.processFilePosition(getFilePosition());
                                      
                        copyTuples(getFilePositionEndIndex(), output);
                    } else {
                        output.processFilePosition(getFilePosition());
                        copyTuples(getFilePositionEndIndex(), output);
                    }
                    incrementFilePosition();  
                    
                    if (getFileEndIndex() <= readTupleIndex)
                        break;   
                }
            }
            public void copyUntil(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                copyUntilFile(other, output);
            }
            
        }                         
        public static class ShreddedCombiner implements ReaderSource<TextFeature>, ShreddedSource {   
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
                } else if (processor instanceof TextFeature.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((TextFeature.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<TextFeature>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<TextFeature> getOutputClass() {
                return TextFeature.class;
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

            public TextFeature read() throws IOException {
                if (uninitialized)
                    initialize();

                TextFeature result = null;

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
        public static class ShreddedReader implements Step, Comparable<ShreddedReader>, TypeReader<TextFeature>, ShreddedSource {      
            public ShreddedProcessor processor;
            ShreddedBuffer buffer;
            TextFeature last = new TextFeature();         
            long updateFileCount = -1;
            long updateFilePositionCount = -1;
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
                    result = + Utility.compare(buffer.getFile(), otherBuffer.getFile());
                    if(result != 0) break;
                    result = + Utility.compare(buffer.getFilePosition(), otherBuffer.getFilePosition());
                    if(result != 0) break;
                } while (false);                                             
                
                return result;
            }
            
            public final ShreddedBuffer getBuffer() {
                return buffer;
            }                
            
            public final TextFeature read() throws IOException {
                if (buffer.isAtEnd()) {
                    fill();             
                
                    if (buffer.isAtEnd()) {
                        return null;
                    }
                }
                      
                assert !buffer.isAtEnd();
                TextFeature result = new TextFeature();
                
                result.file = buffer.getFile();
                result.filePosition = buffer.getFilePosition();
                result.feature = buffer.getFeature();
                
                buffer.incrementTuple();
                buffer.autoIncrementFile();
                buffer.autoIncrementFilePosition();
                
                return result;
            }           
            
            public final void fill() throws IOException {
                try {   
                    buffer.reset();
                    
                    if (tupleCount != 0) {
                                                      
                        if(updateFileCount - tupleCount > 0) {
                            buffer.files.add(last.file);
                            buffer.fileTupleIdx.add((int) (updateFileCount - tupleCount));
                        }                              
                        if(updateFilePositionCount - tupleCount > 0) {
                            buffer.filePositions.add(last.filePosition);
                            buffer.filePositionTupleIdx.add((int) (updateFilePositionCount - tupleCount));
                        }
                        bufferStartCount = tupleCount;
                    }
                    
                    while (!buffer.isFull()) {
                        updateFilePosition();
                        buffer.processTuple(input.readBytes());
                        tupleCount++;
                    }
                } catch(EOFException e) {}
            }

            public final void updateFile() throws IOException {
                if (updateFileCount > tupleCount)
                    return;
                     
                last.file = input.readInt();
                updateFileCount = tupleCount + input.readInt();
                                      
                buffer.processFile(last.file);
            }
            public final void updateFilePosition() throws IOException {
                if (updateFilePositionCount > tupleCount)
                    return;
                     
                updateFile();
                last.filePosition = input.readLong();
                updateFilePositionCount = tupleCount + input.readInt();
                                      
                buffer.processFilePosition(last.filePosition);
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
                } else if (processor instanceof TextFeature.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((TextFeature.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<TextFeature>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<TextFeature> getOutputClass() {
                return TextFeature.class;
            }                
        }
        
        public static class DuplicateEliminator implements ShreddedProcessor {
            public ShreddedProcessor processor;
            TextFeature last = new TextFeature();
            boolean fileProcess = true;
            boolean filePositionProcess = true;
                                           
            public DuplicateEliminator() {}
            public DuplicateEliminator(ShreddedProcessor processor) {
                this.processor = processor;
            }
            
            public void setShreddedProcessor(ShreddedProcessor processor) {
                this.processor = processor;
            }

            public void processFile(int file) throws IOException {  
                if (fileProcess || Utility.compare(file, last.file) != 0) {
                    last.file = file;
                    processor.processFile(file);
            resetFilePosition();
                    fileProcess = false;
                }
            }
            public void processFilePosition(long filePosition) throws IOException {  
                if (filePositionProcess || Utility.compare(filePosition, last.filePosition) != 0) {
                    last.filePosition = filePosition;
                    processor.processFilePosition(filePosition);
                    filePositionProcess = false;
                }
            }  
            
            public void resetFile() {
                 fileProcess = true;
            resetFilePosition();
            }                                                
            public void resetFilePosition() {
                 filePositionProcess = true;
            }                                                
                               
            public void processTuple(byte[] feature) throws IOException {
                processor.processTuple(feature);
            } 
            
            public void close() throws IOException {
                processor.close();
            }                    
        }
        public static class TupleUnshredder implements ShreddedProcessor {
            TextFeature last = new TextFeature();
            public org.lemurproject.galago.tupleflow.Processor<TextFeature> processor;                               
            
            public TupleUnshredder(TextFeature.Processor processor) {
                this.processor = processor;
            }         
            
            public TupleUnshredder(org.lemurproject.galago.tupleflow.Processor<TextFeature> processor) {
                this.processor = processor;
            }
            
            public TextFeature clone(TextFeature object) {
                TextFeature result = new TextFeature();
                if (object == null) return result;
                result.file = object.file; 
                result.filePosition = object.filePosition; 
                result.feature = object.feature; 
                return result;
            }                 
            
            public void processFile(int file) throws IOException {
                last.file = file;
            }   
                
            public void processFilePosition(long filePosition) throws IOException {
                last.filePosition = filePosition;
            }   
                
            
            public void processTuple(byte[] feature) throws IOException {
                last.feature = feature;
                processor.process(clone(last));
            }               
            
            public void close() throws IOException {
                processor.close();
            }
        }     
        public static class TupleShredder implements Processor {
            TextFeature last = null;
            public ShreddedProcessor processor;
            
            public TupleShredder(ShreddedProcessor processor) {
                this.processor = processor;
            }                              
            
            public TextFeature clone(TextFeature object) {
                TextFeature result = new TextFeature();
                if (object == null) return result;
                result.file = object.file; 
                result.filePosition = object.filePosition; 
                result.feature = object.feature; 
                return result;
            }                 
            
            public void process(TextFeature object) throws IOException {                                                                                                                                                   
                boolean processAll = false;
                if(last == null || Utility.compare(last.file, object.file) != 0 || processAll) { processor.processFile(object.file); processAll = true; }
                if(last == null || Utility.compare(last.filePosition, object.filePosition) != 0 || processAll) { processor.processFilePosition(object.filePosition); processAll = true; }
                processor.processTuple(object.feature);                                         
                last = object;
            }
                          
            public Class<TextFeature> getInputClass() {
                return TextFeature.class;
            }
            
            public void close() throws IOException {
                processor.close();
            }                     
        }
    } 
    public static class FileOrder implements Order<TextFeature> {
        public int hash(TextFeature object) {
            int h = 0;
            h += Utility.hash(object.file);
            return h;
        } 
        public Comparator<TextFeature> greaterThan() {
            return new Comparator<TextFeature>() {
                public int compare(TextFeature one, TextFeature two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.file, two.file);
                        if(result != 0) break;
                    } while (false);
                    return -result;
                }
            };
        }     
        public Comparator<TextFeature> lessThan() {
            return new Comparator<TextFeature>() {
                public int compare(TextFeature one, TextFeature two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.file, two.file);
                        if(result != 0) break;
                    } while (false);
                    return result;
                }
            };
        }     
        public TypeReader<TextFeature> orderedReader(ArrayInput _input) {
            return new ShreddedReader(_input);
        }    

        public TypeReader<TextFeature> orderedReader(ArrayInput _input, int bufferSize) {
            return new ShreddedReader(_input, bufferSize);
        }    
        public OrderedWriter<TextFeature> orderedWriter(ArrayOutput _output) {
            ShreddedWriter w = new ShreddedWriter(_output);
            return new OrderedWriterClass(w); 
        }                                    
        public static class OrderedWriterClass extends OrderedWriter< TextFeature > {
            TextFeature last = null;
            ShreddedWriter shreddedWriter = null; 
            
            public OrderedWriterClass(ShreddedWriter s) {
                this.shreddedWriter = s;
            }
            
            public void process(TextFeature object) throws IOException {
               boolean processAll = false;
               if (processAll || last == null || 0 != Utility.compare(object.file, last.file)) { processAll = true; shreddedWriter.processFile(object.file); }
               shreddedWriter.processTuple(object.filePosition, object.feature);
               last = object;
            }           
                 
            public void close() throws IOException {
                shreddedWriter.close();
            }
            
            public Class<TextFeature> getInputClass() {
                return TextFeature.class;
            }
        } 
        public ReaderSource<TextFeature> orderedCombiner(Collection<TypeReader<TextFeature>> readers, boolean closeOnExit) {
            ArrayList<ShreddedReader> shreddedReaders = new ArrayList();
            
            for (TypeReader<TextFeature> reader : readers) {
                shreddedReaders.add((ShreddedReader)reader);
            }
            
            return new ShreddedCombiner(shreddedReaders, closeOnExit);
        }                  
        public TextFeature clone(TextFeature object) {
            TextFeature result = new TextFeature();
            if (object == null) return result;
            result.file = object.file; 
            result.filePosition = object.filePosition; 
            result.feature = object.feature; 
            return result;
        }                 
        public Class<TextFeature> getOrderedClass() {
            return TextFeature.class;
        }                           
        public String[] getOrderSpec() {
            return new String[] {"+file"};
        }

        public static String[] getSpec() {
            return new String[] {"+file"};
        }
        public static String getSpecString() {
            return "+file";
        }
                           
        public interface ShreddedProcessor extends Step {
            public void processFile(int file) throws IOException;
            public void processTuple(long filePosition, byte[] feature) throws IOException;
            public void close() throws IOException;
        }    
        public interface ShreddedSource extends Step {
        }                                              
        
        public static class ShreddedWriter implements ShreddedProcessor {
            ArrayOutput output;
            ShreddedBuffer buffer = new ShreddedBuffer();
            int lastFile;
            boolean lastFlush = false;
            
            public ShreddedWriter(ArrayOutput output) {
                this.output = output;
            }                        
            
            public void close() throws IOException {
                flush();
            }
            
            public void processFile(int file) {
                lastFile = file;
                buffer.processFile(file);
            }
            public final void processTuple(long filePosition, byte[] feature) throws IOException {
                if (lastFlush) {
                    if(buffer.files.size() == 0) buffer.processFile(lastFile);
                    lastFlush = false;
                }
                buffer.processTuple(filePosition, feature);
                if (buffer.isFull())
                    flush();
            }
            public final void flushTuples(int pauseIndex) throws IOException {
                
                while (buffer.getReadIndex() < pauseIndex) {
                           
                    output.writeLong(buffer.getFilePosition());
                    output.writeBytes(buffer.getFeature());
                    buffer.incrementTuple();
                }
            }  
            public final void flushFile(int pauseIndex) throws IOException {
                while (buffer.getReadIndex() < pauseIndex) {
                    int nextPause = buffer.getFileEndIndex();
                    int count = nextPause - buffer.getReadIndex();
                    
                    output.writeInt(buffer.getFile());
                    output.writeInt(count);
                    buffer.incrementFile();
                      
                    flushTuples(nextPause);
                    assert nextPause == buffer.getReadIndex();
                }
            }
            public void flush() throws IOException { 
                flushFile(buffer.getWriteIndex());
                buffer.reset(); 
                lastFlush = true;
            }                           
        }
        public static class ShreddedBuffer {
            TIntArrayList files = new TIntArrayList();
            TIntArrayList fileTupleIdx = new TIntArrayList();
            int fileReadIdx = 0;
                            
            long[] filePositions;
            byte[][] features;
            int writeTupleIndex = 0;
            int readTupleIndex = 0;
            int batchSize;

            public ShreddedBuffer(int batchSize) {
                this.batchSize = batchSize;

                filePositions = new long[batchSize];
                features = new byte[batchSize][];
            }                              

            public ShreddedBuffer() {    
                this(10000);
            }                                                                                                                    
            
            public void processFile(int file) {
                files.add(file);
                fileTupleIdx.add(writeTupleIndex);
            }                                      
            public void processTuple(long filePosition, byte[] feature) {
                assert files.size() > 0;
                filePositions[writeTupleIndex] = filePosition;
                features[writeTupleIndex] = feature;
                writeTupleIndex++;
            }
            public void resetData() {
                files.clear();
                fileTupleIdx.clear();
                writeTupleIndex = 0;
            }                  
                                 
            public void resetRead() {
                readTupleIndex = 0;
                fileReadIdx = 0;
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
            public void incrementFile() {
                fileReadIdx++;  
            }                                                                                              

            public void autoIncrementFile() {
                while (readTupleIndex >= getFileEndIndex() && readTupleIndex < writeTupleIndex)
                    fileReadIdx++;
            }                 
            public void incrementTuple() {
                readTupleIndex++;
            }                    
            public int getFileEndIndex() {
                if ((fileReadIdx+1) >= fileTupleIdx.size())
                    return writeTupleIndex;
                return fileTupleIdx.get(fileReadIdx+1);
            }
            public int getReadIndex() {
                return readTupleIndex;
            }   

            public int getWriteIndex() {
                return writeTupleIndex;
            } 
            public int getFile() {
                assert readTupleIndex < writeTupleIndex;
                assert fileReadIdx < files.size();
                
                return files.get(fileReadIdx);
            }
            public long getFilePosition() {
                assert readTupleIndex < writeTupleIndex;
                return filePositions[readTupleIndex];
            }                                         
            public byte[] getFeature() {
                assert readTupleIndex < writeTupleIndex;
                return features[readTupleIndex];
            }                                         
            public void copyTuples(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                   output.processTuple(getFilePosition(), getFeature());
                   incrementTuple();
                }
            }                                                                           
            public void copyUntilIndexFile(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                    output.processFile(getFile());
                    assert getFileEndIndex() <= endIndex;
                    copyTuples(getFileEndIndex(), output);
                    incrementFile();
                }
            }  
            public void copyUntilFile(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                while (!isAtEnd()) {
                    if (other != null) {   
                        assert !other.isAtEnd();
                        int c = + Utility.compare(getFile(), other.getFile());
                    
                        if (c > 0) {
                            break;   
                        }
                        
                        output.processFile(getFile());
                                      
                        copyTuples(getFileEndIndex(), output);
                    } else {
                        output.processFile(getFile());
                        copyTuples(getFileEndIndex(), output);
                    }
                    incrementFile();  
                    
               
                }
            }
            public void copyUntil(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                copyUntilFile(other, output);
            }
            
        }                         
        public static class ShreddedCombiner implements ReaderSource<TextFeature>, ShreddedSource {   
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
                } else if (processor instanceof TextFeature.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((TextFeature.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<TextFeature>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<TextFeature> getOutputClass() {
                return TextFeature.class;
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

            public TextFeature read() throws IOException {
                if (uninitialized)
                    initialize();

                TextFeature result = null;

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
        public static class ShreddedReader implements Step, Comparable<ShreddedReader>, TypeReader<TextFeature>, ShreddedSource {      
            public ShreddedProcessor processor;
            ShreddedBuffer buffer;
            TextFeature last = new TextFeature();         
            long updateFileCount = -1;
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
                    result = + Utility.compare(buffer.getFile(), otherBuffer.getFile());
                    if(result != 0) break;
                } while (false);                                             
                
                return result;
            }
            
            public final ShreddedBuffer getBuffer() {
                return buffer;
            }                
            
            public final TextFeature read() throws IOException {
                if (buffer.isAtEnd()) {
                    fill();             
                
                    if (buffer.isAtEnd()) {
                        return null;
                    }
                }
                      
                assert !buffer.isAtEnd();
                TextFeature result = new TextFeature();
                
                result.file = buffer.getFile();
                result.filePosition = buffer.getFilePosition();
                result.feature = buffer.getFeature();
                
                buffer.incrementTuple();
                buffer.autoIncrementFile();
                
                return result;
            }           
            
            public final void fill() throws IOException {
                try {   
                    buffer.reset();
                    
                    if (tupleCount != 0) {
                                                      
                        if(updateFileCount - tupleCount > 0) {
                            buffer.files.add(last.file);
                            buffer.fileTupleIdx.add((int) (updateFileCount - tupleCount));
                        }
                        bufferStartCount = tupleCount;
                    }
                    
                    while (!buffer.isFull()) {
                        updateFile();
                        buffer.processTuple(input.readLong(), input.readBytes());
                        tupleCount++;
                    }
                } catch(EOFException e) {}
            }

            public final void updateFile() throws IOException {
                if (updateFileCount > tupleCount)
                    return;
                     
                last.file = input.readInt();
                updateFileCount = tupleCount + input.readInt();
                                      
                buffer.processFile(last.file);
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
                } else if (processor instanceof TextFeature.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((TextFeature.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<TextFeature>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<TextFeature> getOutputClass() {
                return TextFeature.class;
            }                
        }
        
        public static class DuplicateEliminator implements ShreddedProcessor {
            public ShreddedProcessor processor;
            TextFeature last = new TextFeature();
            boolean fileProcess = true;
                                           
            public DuplicateEliminator() {}
            public DuplicateEliminator(ShreddedProcessor processor) {
                this.processor = processor;
            }
            
            public void setShreddedProcessor(ShreddedProcessor processor) {
                this.processor = processor;
            }

            public void processFile(int file) throws IOException {  
                if (fileProcess || Utility.compare(file, last.file) != 0) {
                    last.file = file;
                    processor.processFile(file);
                    fileProcess = false;
                }
            }  
            
            public void resetFile() {
                 fileProcess = true;
            }                                                
                               
            public void processTuple(long filePosition, byte[] feature) throws IOException {
                processor.processTuple(filePosition, feature);
            } 
            
            public void close() throws IOException {
                processor.close();
            }                    
        }
        public static class TupleUnshredder implements ShreddedProcessor {
            TextFeature last = new TextFeature();
            public org.lemurproject.galago.tupleflow.Processor<TextFeature> processor;                               
            
            public TupleUnshredder(TextFeature.Processor processor) {
                this.processor = processor;
            }         
            
            public TupleUnshredder(org.lemurproject.galago.tupleflow.Processor<TextFeature> processor) {
                this.processor = processor;
            }
            
            public TextFeature clone(TextFeature object) {
                TextFeature result = new TextFeature();
                if (object == null) return result;
                result.file = object.file; 
                result.filePosition = object.filePosition; 
                result.feature = object.feature; 
                return result;
            }                 
            
            public void processFile(int file) throws IOException {
                last.file = file;
            }   
                
            
            public void processTuple(long filePosition, byte[] feature) throws IOException {
                last.filePosition = filePosition;
                last.feature = feature;
                processor.process(clone(last));
            }               
            
            public void close() throws IOException {
                processor.close();
            }
        }     
        public static class TupleShredder implements Processor {
            TextFeature last = null;
            public ShreddedProcessor processor;
            
            public TupleShredder(ShreddedProcessor processor) {
                this.processor = processor;
            }                              
            
            public TextFeature clone(TextFeature object) {
                TextFeature result = new TextFeature();
                if (object == null) return result;
                result.file = object.file; 
                result.filePosition = object.filePosition; 
                result.feature = object.feature; 
                return result;
            }                 
            
            public void process(TextFeature object) throws IOException {                                                                                                                                                   
                boolean processAll = false;
                if(last == null || Utility.compare(last.file, object.file) != 0 || processAll) { processor.processFile(object.file); processAll = true; }
                processor.processTuple(object.filePosition, object.feature);                                         
                last = object;
            }
                          
            public Class<TextFeature> getInputClass() {
                return TextFeature.class;
            }
            
            public void close() throws IOException {
                processor.close();
            }                     
        }
    } 
}    