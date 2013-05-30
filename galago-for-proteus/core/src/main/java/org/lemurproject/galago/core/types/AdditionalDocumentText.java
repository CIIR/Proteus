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


public class AdditionalDocumentText implements Type<AdditionalDocumentText> {
    public int identifier;
    public String text; 
    
    public AdditionalDocumentText() {}
    public AdditionalDocumentText(int identifier, String text) {
        this.identifier = identifier;
        this.text = text;
    }  
    
    public String toString() {
            return String.format("%d,%s",
                                   identifier, text);
    } 

    public Order<AdditionalDocumentText> getOrder(String... spec) {
        if (Arrays.equals(spec, new String[] {  })) {
            return new Unordered();
        }
        if (Arrays.equals(spec, new String[] { "+identifier" })) {
            return new IdentifierOrder();
        }
        return null;
    } 
      
    public interface Processor extends Step, org.lemurproject.galago.tupleflow.Processor<AdditionalDocumentText> {
        public void process(AdditionalDocumentText object) throws IOException;
        public void close() throws IOException;
    }                        
    public interface Source extends Step {
    }
    public static class Unordered implements Order<AdditionalDocumentText> {
        public int hash(AdditionalDocumentText object) {
            int h = 0;
            return h;
        } 
        public Comparator<AdditionalDocumentText> greaterThan() {
            return new Comparator<AdditionalDocumentText>() {
                public int compare(AdditionalDocumentText one, AdditionalDocumentText two) {
                    int result = 0;
                    do {
                    } while (false);
                    return -result;
                }
            };
        }     
        public Comparator<AdditionalDocumentText> lessThan() {
            return new Comparator<AdditionalDocumentText>() {
                public int compare(AdditionalDocumentText one, AdditionalDocumentText two) {
                    int result = 0;
                    do {
                    } while (false);
                    return result;
                }
            };
        }     
        public TypeReader<AdditionalDocumentText> orderedReader(ArrayInput _input) {
            return new ShreddedReader(_input);
        }    

        public TypeReader<AdditionalDocumentText> orderedReader(ArrayInput _input, int bufferSize) {
            return new ShreddedReader(_input, bufferSize);
        }    
        public OrderedWriter<AdditionalDocumentText> orderedWriter(ArrayOutput _output) {
            ShreddedWriter w = new ShreddedWriter(_output);
            return new OrderedWriterClass(w); 
        }                                    
        public static class OrderedWriterClass extends OrderedWriter< AdditionalDocumentText > {
            AdditionalDocumentText last = null;
            ShreddedWriter shreddedWriter = null; 
            
            public OrderedWriterClass(ShreddedWriter s) {
                this.shreddedWriter = s;
            }
            
            public void process(AdditionalDocumentText object) throws IOException {
               boolean processAll = false;
               shreddedWriter.processTuple(object.identifier, object.text);
               last = object;
            }           
                 
            public void close() throws IOException {
                shreddedWriter.close();
            }
            
            public Class<AdditionalDocumentText> getInputClass() {
                return AdditionalDocumentText.class;
            }
        } 
        public ReaderSource<AdditionalDocumentText> orderedCombiner(Collection<TypeReader<AdditionalDocumentText>> readers, boolean closeOnExit) {
            ArrayList<ShreddedReader> shreddedReaders = new ArrayList();
            
            for (TypeReader<AdditionalDocumentText> reader : readers) {
                shreddedReaders.add((ShreddedReader)reader);
            }
            
            return new ShreddedCombiner(shreddedReaders, closeOnExit);
        }                  
        public AdditionalDocumentText clone(AdditionalDocumentText object) {
            AdditionalDocumentText result = new AdditionalDocumentText();
            if (object == null) return result;
            result.identifier = object.identifier; 
            result.text = object.text; 
            return result;
        }                 
        public Class<AdditionalDocumentText> getOrderedClass() {
            return AdditionalDocumentText.class;
        }                           
        public String[] getOrderSpec() {
            return new String[] {};
        }

        public static String[] getSpec() {
            return new String[] {};
        }
        public static String getSpecString() {
            return "";
        }
                           
        public interface ShreddedProcessor extends Step {
            public void processTuple(int identifier, String text) throws IOException;
            public void close() throws IOException;
        }    
        public interface ShreddedSource extends Step {
        }                                              
        
        public static class ShreddedWriter implements ShreddedProcessor {
            ArrayOutput output;
            ShreddedBuffer buffer = new ShreddedBuffer();
            boolean lastFlush = false;
            
            public ShreddedWriter(ArrayOutput output) {
                this.output = output;
            }                        
            
            public void close() throws IOException {
                flush();
            }
            
            public final void processTuple(int identifier, String text) throws IOException {
                if (lastFlush) {
                    lastFlush = false;
                }
                buffer.processTuple(identifier, text);
                if (buffer.isFull())
                    flush();
            }
            public final void flushTuples(int pauseIndex) throws IOException {
                
                while (buffer.getReadIndex() < pauseIndex) {
                           
                    output.writeInt(buffer.getIdentifier());
                    output.writeString(buffer.getText());
                    buffer.incrementTuple();
                }
            }  
            public void flush() throws IOException { 
                flushTuples(buffer.getWriteIndex());
                buffer.reset(); 
                lastFlush = true;
            }                           
        }
        public static class ShreddedBuffer {
                            
            int[] identifiers;
            String[] texts;
            int writeTupleIndex = 0;
            int readTupleIndex = 0;
            int batchSize;

            public ShreddedBuffer(int batchSize) {
                this.batchSize = batchSize;

                identifiers = new int[batchSize];
                texts = new String[batchSize];
            }                              

            public ShreddedBuffer() {    
                this(10000);
            }                                                                                                                    
            
            public void processTuple(int identifier, String text) {
                identifiers[writeTupleIndex] = identifier;
                texts[writeTupleIndex] = text;
                writeTupleIndex++;
            }
            public void resetData() {
                writeTupleIndex = 0;
            }                  
                                 
            public void resetRead() {
                readTupleIndex = 0;
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
            public void incrementTuple() {
                readTupleIndex++;
            }                    
            public int getReadIndex() {
                return readTupleIndex;
            }   

            public int getWriteIndex() {
                return writeTupleIndex;
            } 
            public int getIdentifier() {
                assert readTupleIndex < writeTupleIndex;
                return identifiers[readTupleIndex];
            }                                         
            public String getText() {
                assert readTupleIndex < writeTupleIndex;
                return texts[readTupleIndex];
            }                                         
            public void copyTuples(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                   output.processTuple(getIdentifier(), getText());
                   incrementTuple();
                }
            }                                                                           
             
            public void copyUntil(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
            }
            
        }                         
        public static class ShreddedCombiner implements ReaderSource<AdditionalDocumentText>, ShreddedSource {   
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
                } else if (processor instanceof AdditionalDocumentText.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((AdditionalDocumentText.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<AdditionalDocumentText>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<AdditionalDocumentText> getOutputClass() {
                return AdditionalDocumentText.class;
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

            public AdditionalDocumentText read() throws IOException {
                if (uninitialized)
                    initialize();

                AdditionalDocumentText result = null;

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
        public static class ShreddedReader implements Step, Comparable<ShreddedReader>, TypeReader<AdditionalDocumentText>, ShreddedSource {      
            public ShreddedProcessor processor;
            ShreddedBuffer buffer;
            AdditionalDocumentText last = new AdditionalDocumentText();         
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
                } while (false);                                             
                
                return result;
            }
            
            public final ShreddedBuffer getBuffer() {
                return buffer;
            }                
            
            public final AdditionalDocumentText read() throws IOException {
                if (buffer.isAtEnd()) {
                    fill();             
                
                    if (buffer.isAtEnd()) {
                        return null;
                    }
                }
                      
                assert !buffer.isAtEnd();
                AdditionalDocumentText result = new AdditionalDocumentText();
                
                result.identifier = buffer.getIdentifier();
                result.text = buffer.getText();
                
                buffer.incrementTuple();
                
                return result;
            }           
            
            public final void fill() throws IOException {
                try {   
                    buffer.reset();
                    
                    if (tupleCount != 0) {
                        bufferStartCount = tupleCount;
                    }
                    
                    while (!buffer.isFull()) {
                        buffer.processTuple(input.readInt(), input.readString());
                        tupleCount++;
                    }
                } catch(EOFException e) {}
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
                } else if (processor instanceof AdditionalDocumentText.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((AdditionalDocumentText.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<AdditionalDocumentText>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<AdditionalDocumentText> getOutputClass() {
                return AdditionalDocumentText.class;
            }                
        }
        
        public static class DuplicateEliminator implements ShreddedProcessor {
            public ShreddedProcessor processor;
            AdditionalDocumentText last = new AdditionalDocumentText();
                                           
            public DuplicateEliminator() {}
            public DuplicateEliminator(ShreddedProcessor processor) {
                this.processor = processor;
            }
            
            public void setShreddedProcessor(ShreddedProcessor processor) {
                this.processor = processor;
            }

          
            
                               
            public void processTuple(int identifier, String text) throws IOException {
                processor.processTuple(identifier, text);
            } 
            
            public void close() throws IOException {
                processor.close();
            }                    
        }
        public static class TupleUnshredder implements ShreddedProcessor {
            AdditionalDocumentText last = new AdditionalDocumentText();
            public org.lemurproject.galago.tupleflow.Processor<AdditionalDocumentText> processor;                               
            
            public TupleUnshredder(AdditionalDocumentText.Processor processor) {
                this.processor = processor;
            }         
            
            public TupleUnshredder(org.lemurproject.galago.tupleflow.Processor<AdditionalDocumentText> processor) {
                this.processor = processor;
            }
            
            public AdditionalDocumentText clone(AdditionalDocumentText object) {
                AdditionalDocumentText result = new AdditionalDocumentText();
                if (object == null) return result;
                result.identifier = object.identifier; 
                result.text = object.text; 
                return result;
            }                 
            
            
            public void processTuple(int identifier, String text) throws IOException {
                last.identifier = identifier;
                last.text = text;
                processor.process(clone(last));
            }               
            
            public void close() throws IOException {
                processor.close();
            }
        }     
        public static class TupleShredder implements Processor {
            AdditionalDocumentText last = null;
            public ShreddedProcessor processor;
            
            public TupleShredder(ShreddedProcessor processor) {
                this.processor = processor;
            }                              
            
            public AdditionalDocumentText clone(AdditionalDocumentText object) {
                AdditionalDocumentText result = new AdditionalDocumentText();
                if (object == null) return result;
                result.identifier = object.identifier; 
                result.text = object.text; 
                return result;
            }                 
            
            public void process(AdditionalDocumentText object) throws IOException {                                                                                                                                                   
                boolean processAll = false;
                processor.processTuple(object.identifier, object.text);                                         
                last = object;
            }
                          
            public Class<AdditionalDocumentText> getInputClass() {
                return AdditionalDocumentText.class;
            }
            
            public void close() throws IOException {
                processor.close();
            }                     
        }
    } 
    public static class IdentifierOrder implements Order<AdditionalDocumentText> {
        public int hash(AdditionalDocumentText object) {
            int h = 0;
            h += Utility.hash(object.identifier);
            return h;
        } 
        public Comparator<AdditionalDocumentText> greaterThan() {
            return new Comparator<AdditionalDocumentText>() {
                public int compare(AdditionalDocumentText one, AdditionalDocumentText two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.identifier, two.identifier);
                        if(result != 0) break;
                    } while (false);
                    return -result;
                }
            };
        }     
        public Comparator<AdditionalDocumentText> lessThan() {
            return new Comparator<AdditionalDocumentText>() {
                public int compare(AdditionalDocumentText one, AdditionalDocumentText two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.identifier, two.identifier);
                        if(result != 0) break;
                    } while (false);
                    return result;
                }
            };
        }     
        public TypeReader<AdditionalDocumentText> orderedReader(ArrayInput _input) {
            return new ShreddedReader(_input);
        }    

        public TypeReader<AdditionalDocumentText> orderedReader(ArrayInput _input, int bufferSize) {
            return new ShreddedReader(_input, bufferSize);
        }    
        public OrderedWriter<AdditionalDocumentText> orderedWriter(ArrayOutput _output) {
            ShreddedWriter w = new ShreddedWriter(_output);
            return new OrderedWriterClass(w); 
        }                                    
        public static class OrderedWriterClass extends OrderedWriter< AdditionalDocumentText > {
            AdditionalDocumentText last = null;
            ShreddedWriter shreddedWriter = null; 
            
            public OrderedWriterClass(ShreddedWriter s) {
                this.shreddedWriter = s;
            }
            
            public void process(AdditionalDocumentText object) throws IOException {
               boolean processAll = false;
               if (processAll || last == null || 0 != Utility.compare(object.identifier, last.identifier)) { processAll = true; shreddedWriter.processIdentifier(object.identifier); }
               shreddedWriter.processTuple(object.text);
               last = object;
            }           
                 
            public void close() throws IOException {
                shreddedWriter.close();
            }
            
            public Class<AdditionalDocumentText> getInputClass() {
                return AdditionalDocumentText.class;
            }
        } 
        public ReaderSource<AdditionalDocumentText> orderedCombiner(Collection<TypeReader<AdditionalDocumentText>> readers, boolean closeOnExit) {
            ArrayList<ShreddedReader> shreddedReaders = new ArrayList();
            
            for (TypeReader<AdditionalDocumentText> reader : readers) {
                shreddedReaders.add((ShreddedReader)reader);
            }
            
            return new ShreddedCombiner(shreddedReaders, closeOnExit);
        }                  
        public AdditionalDocumentText clone(AdditionalDocumentText object) {
            AdditionalDocumentText result = new AdditionalDocumentText();
            if (object == null) return result;
            result.identifier = object.identifier; 
            result.text = object.text; 
            return result;
        }                 
        public Class<AdditionalDocumentText> getOrderedClass() {
            return AdditionalDocumentText.class;
        }                           
        public String[] getOrderSpec() {
            return new String[] {"+identifier"};
        }

        public static String[] getSpec() {
            return new String[] {"+identifier"};
        }
        public static String getSpecString() {
            return "+identifier";
        }
                           
        public interface ShreddedProcessor extends Step {
            public void processIdentifier(int identifier) throws IOException;
            public void processTuple(String text) throws IOException;
            public void close() throws IOException;
        }    
        public interface ShreddedSource extends Step {
        }                                              
        
        public static class ShreddedWriter implements ShreddedProcessor {
            ArrayOutput output;
            ShreddedBuffer buffer = new ShreddedBuffer();
            int lastIdentifier;
            boolean lastFlush = false;
            
            public ShreddedWriter(ArrayOutput output) {
                this.output = output;
            }                        
            
            public void close() throws IOException {
                flush();
            }
            
            public void processIdentifier(int identifier) {
                lastIdentifier = identifier;
                buffer.processIdentifier(identifier);
            }
            public final void processTuple(String text) throws IOException {
                if (lastFlush) {
                    if(buffer.identifiers.size() == 0) buffer.processIdentifier(lastIdentifier);
                    lastFlush = false;
                }
                buffer.processTuple(text);
                if (buffer.isFull())
                    flush();
            }
            public final void flushTuples(int pauseIndex) throws IOException {
                
                while (buffer.getReadIndex() < pauseIndex) {
                           
                    output.writeString(buffer.getText());
                    buffer.incrementTuple();
                }
            }  
            public final void flushIdentifier(int pauseIndex) throws IOException {
                while (buffer.getReadIndex() < pauseIndex) {
                    int nextPause = buffer.getIdentifierEndIndex();
                    int count = nextPause - buffer.getReadIndex();
                    
                    output.writeInt(buffer.getIdentifier());
                    output.writeInt(count);
                    buffer.incrementIdentifier();
                      
                    flushTuples(nextPause);
                    assert nextPause == buffer.getReadIndex();
                }
            }
            public void flush() throws IOException { 
                flushIdentifier(buffer.getWriteIndex());
                buffer.reset(); 
                lastFlush = true;
            }                           
        }
        public static class ShreddedBuffer {
            TIntArrayList identifiers = new TIntArrayList();
            TIntArrayList identifierTupleIdx = new TIntArrayList();
            int identifierReadIdx = 0;
                            
            String[] texts;
            int writeTupleIndex = 0;
            int readTupleIndex = 0;
            int batchSize;

            public ShreddedBuffer(int batchSize) {
                this.batchSize = batchSize;

                texts = new String[batchSize];
            }                              

            public ShreddedBuffer() {    
                this(10000);
            }                                                                                                                    
            
            public void processIdentifier(int identifier) {
                identifiers.add(identifier);
                identifierTupleIdx.add(writeTupleIndex);
            }                                      
            public void processTuple(String text) {
                assert identifiers.size() > 0;
                texts[writeTupleIndex] = text;
                writeTupleIndex++;
            }
            public void resetData() {
                identifiers.clear();
                identifierTupleIdx.clear();
                writeTupleIndex = 0;
            }                  
                                 
            public void resetRead() {
                readTupleIndex = 0;
                identifierReadIdx = 0;
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
            public void incrementIdentifier() {
                identifierReadIdx++;  
            }                                                                                              

            public void autoIncrementIdentifier() {
                while (readTupleIndex >= getIdentifierEndIndex() && readTupleIndex < writeTupleIndex)
                    identifierReadIdx++;
            }                 
            public void incrementTuple() {
                readTupleIndex++;
            }                    
            public int getIdentifierEndIndex() {
                if ((identifierReadIdx+1) >= identifierTupleIdx.size())
                    return writeTupleIndex;
                return identifierTupleIdx.get(identifierReadIdx+1);
            }
            public int getReadIndex() {
                return readTupleIndex;
            }   

            public int getWriteIndex() {
                return writeTupleIndex;
            } 
            public int getIdentifier() {
                assert readTupleIndex < writeTupleIndex;
                assert identifierReadIdx < identifiers.size();
                
                return identifiers.get(identifierReadIdx);
            }
            public String getText() {
                assert readTupleIndex < writeTupleIndex;
                return texts[readTupleIndex];
            }                                         
            public void copyTuples(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                   output.processTuple(getText());
                   incrementTuple();
                }
            }                                                                           
            public void copyUntilIndexIdentifier(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                    output.processIdentifier(getIdentifier());
                    assert getIdentifierEndIndex() <= endIndex;
                    copyTuples(getIdentifierEndIndex(), output);
                    incrementIdentifier();
                }
            }  
            public void copyUntilIdentifier(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                while (!isAtEnd()) {
                    if (other != null) {   
                        assert !other.isAtEnd();
                        int c = + Utility.compare(getIdentifier(), other.getIdentifier());
                    
                        if (c > 0) {
                            break;   
                        }
                        
                        output.processIdentifier(getIdentifier());
                                      
                        copyTuples(getIdentifierEndIndex(), output);
                    } else {
                        output.processIdentifier(getIdentifier());
                        copyTuples(getIdentifierEndIndex(), output);
                    }
                    incrementIdentifier();  
                    
               
                }
            }
            public void copyUntil(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                copyUntilIdentifier(other, output);
            }
            
        }                         
        public static class ShreddedCombiner implements ReaderSource<AdditionalDocumentText>, ShreddedSource {   
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
                } else if (processor instanceof AdditionalDocumentText.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((AdditionalDocumentText.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<AdditionalDocumentText>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<AdditionalDocumentText> getOutputClass() {
                return AdditionalDocumentText.class;
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

            public AdditionalDocumentText read() throws IOException {
                if (uninitialized)
                    initialize();

                AdditionalDocumentText result = null;

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
        public static class ShreddedReader implements Step, Comparable<ShreddedReader>, TypeReader<AdditionalDocumentText>, ShreddedSource {      
            public ShreddedProcessor processor;
            ShreddedBuffer buffer;
            AdditionalDocumentText last = new AdditionalDocumentText();         
            long updateIdentifierCount = -1;
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
                    result = + Utility.compare(buffer.getIdentifier(), otherBuffer.getIdentifier());
                    if(result != 0) break;
                } while (false);                                             
                
                return result;
            }
            
            public final ShreddedBuffer getBuffer() {
                return buffer;
            }                
            
            public final AdditionalDocumentText read() throws IOException {
                if (buffer.isAtEnd()) {
                    fill();             
                
                    if (buffer.isAtEnd()) {
                        return null;
                    }
                }
                      
                assert !buffer.isAtEnd();
                AdditionalDocumentText result = new AdditionalDocumentText();
                
                result.identifier = buffer.getIdentifier();
                result.text = buffer.getText();
                
                buffer.incrementTuple();
                buffer.autoIncrementIdentifier();
                
                return result;
            }           
            
            public final void fill() throws IOException {
                try {   
                    buffer.reset();
                    
                    if (tupleCount != 0) {
                                                      
                        if(updateIdentifierCount - tupleCount > 0) {
                            buffer.identifiers.add(last.identifier);
                            buffer.identifierTupleIdx.add((int) (updateIdentifierCount - tupleCount));
                        }
                        bufferStartCount = tupleCount;
                    }
                    
                    while (!buffer.isFull()) {
                        updateIdentifier();
                        buffer.processTuple(input.readString());
                        tupleCount++;
                    }
                } catch(EOFException e) {}
            }

            public final void updateIdentifier() throws IOException {
                if (updateIdentifierCount > tupleCount)
                    return;
                     
                last.identifier = input.readInt();
                updateIdentifierCount = tupleCount + input.readInt();
                                      
                buffer.processIdentifier(last.identifier);
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
                } else if (processor instanceof AdditionalDocumentText.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((AdditionalDocumentText.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<AdditionalDocumentText>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<AdditionalDocumentText> getOutputClass() {
                return AdditionalDocumentText.class;
            }                
        }
        
        public static class DuplicateEliminator implements ShreddedProcessor {
            public ShreddedProcessor processor;
            AdditionalDocumentText last = new AdditionalDocumentText();
            boolean identifierProcess = true;
                                           
            public DuplicateEliminator() {}
            public DuplicateEliminator(ShreddedProcessor processor) {
                this.processor = processor;
            }
            
            public void setShreddedProcessor(ShreddedProcessor processor) {
                this.processor = processor;
            }

            public void processIdentifier(int identifier) throws IOException {  
                if (identifierProcess || Utility.compare(identifier, last.identifier) != 0) {
                    last.identifier = identifier;
                    processor.processIdentifier(identifier);
                    identifierProcess = false;
                }
            }  
            
            public void resetIdentifier() {
                 identifierProcess = true;
            }                                                
                               
            public void processTuple(String text) throws IOException {
                processor.processTuple(text);
            } 
            
            public void close() throws IOException {
                processor.close();
            }                    
        }
        public static class TupleUnshredder implements ShreddedProcessor {
            AdditionalDocumentText last = new AdditionalDocumentText();
            public org.lemurproject.galago.tupleflow.Processor<AdditionalDocumentText> processor;                               
            
            public TupleUnshredder(AdditionalDocumentText.Processor processor) {
                this.processor = processor;
            }         
            
            public TupleUnshredder(org.lemurproject.galago.tupleflow.Processor<AdditionalDocumentText> processor) {
                this.processor = processor;
            }
            
            public AdditionalDocumentText clone(AdditionalDocumentText object) {
                AdditionalDocumentText result = new AdditionalDocumentText();
                if (object == null) return result;
                result.identifier = object.identifier; 
                result.text = object.text; 
                return result;
            }                 
            
            public void processIdentifier(int identifier) throws IOException {
                last.identifier = identifier;
            }   
                
            
            public void processTuple(String text) throws IOException {
                last.text = text;
                processor.process(clone(last));
            }               
            
            public void close() throws IOException {
                processor.close();
            }
        }     
        public static class TupleShredder implements Processor {
            AdditionalDocumentText last = null;
            public ShreddedProcessor processor;
            
            public TupleShredder(ShreddedProcessor processor) {
                this.processor = processor;
            }                              
            
            public AdditionalDocumentText clone(AdditionalDocumentText object) {
                AdditionalDocumentText result = new AdditionalDocumentText();
                if (object == null) return result;
                result.identifier = object.identifier; 
                result.text = object.text; 
                return result;
            }                 
            
            public void process(AdditionalDocumentText object) throws IOException {                                                                                                                                                   
                boolean processAll = false;
                if(last == null || Utility.compare(last.identifier, object.identifier) != 0 || processAll) { processor.processIdentifier(object.identifier); processAll = true; }
                processor.processTuple(object.text);                                         
                last = object;
            }
                          
            public Class<AdditionalDocumentText> getInputClass() {
                return AdditionalDocumentText.class;
            }
            
            public void close() throws IOException {
                processor.close();
            }                     
        }
    } 
}    