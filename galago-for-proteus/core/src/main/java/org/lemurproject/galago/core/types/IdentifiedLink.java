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


public class IdentifiedLink implements Type<IdentifiedLink> {
    public String identifier;
    public String url;
    public String anchorText; 
    
    public IdentifiedLink() {}
    public IdentifiedLink(String identifier, String url, String anchorText) {
        this.identifier = identifier;
        this.url = url;
        this.anchorText = anchorText;
    }  
    
    public String toString() {
            return String.format("%s,%s,%s",
                                   identifier, url, anchorText);
    } 

    public Order<IdentifiedLink> getOrder(String... spec) {
        if (Arrays.equals(spec, new String[] { "+url" })) {
            return new UrlOrder();
        }
        return null;
    } 
      
    public interface Processor extends Step, org.lemurproject.galago.tupleflow.Processor<IdentifiedLink> {
        public void process(IdentifiedLink object) throws IOException;
        public void close() throws IOException;
    }                        
    public interface Source extends Step {
    }
    public static class UrlOrder implements Order<IdentifiedLink> {
        public int hash(IdentifiedLink object) {
            int h = 0;
            h += Utility.hash(object.url);
            return h;
        } 
        public Comparator<IdentifiedLink> greaterThan() {
            return new Comparator<IdentifiedLink>() {
                public int compare(IdentifiedLink one, IdentifiedLink two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.url, two.url);
                        if(result != 0) break;
                    } while (false);
                    return -result;
                }
            };
        }     
        public Comparator<IdentifiedLink> lessThan() {
            return new Comparator<IdentifiedLink>() {
                public int compare(IdentifiedLink one, IdentifiedLink two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.url, two.url);
                        if(result != 0) break;
                    } while (false);
                    return result;
                }
            };
        }     
        public TypeReader<IdentifiedLink> orderedReader(ArrayInput _input) {
            return new ShreddedReader(_input);
        }    

        public TypeReader<IdentifiedLink> orderedReader(ArrayInput _input, int bufferSize) {
            return new ShreddedReader(_input, bufferSize);
        }    
        public OrderedWriter<IdentifiedLink> orderedWriter(ArrayOutput _output) {
            ShreddedWriter w = new ShreddedWriter(_output);
            return new OrderedWriterClass(w); 
        }                                    
        public static class OrderedWriterClass extends OrderedWriter< IdentifiedLink > {
            IdentifiedLink last = null;
            ShreddedWriter shreddedWriter = null; 
            
            public OrderedWriterClass(ShreddedWriter s) {
                this.shreddedWriter = s;
            }
            
            public void process(IdentifiedLink object) throws IOException {
               boolean processAll = false;
               if (processAll || last == null || 0 != Utility.compare(object.url, last.url)) { processAll = true; shreddedWriter.processUrl(object.url); }
               shreddedWriter.processTuple(object.identifier, object.anchorText);
               last = object;
            }           
                 
            public void close() throws IOException {
                shreddedWriter.close();
            }
            
            public Class<IdentifiedLink> getInputClass() {
                return IdentifiedLink.class;
            }
        } 
        public ReaderSource<IdentifiedLink> orderedCombiner(Collection<TypeReader<IdentifiedLink>> readers, boolean closeOnExit) {
            ArrayList<ShreddedReader> shreddedReaders = new ArrayList();
            
            for (TypeReader<IdentifiedLink> reader : readers) {
                shreddedReaders.add((ShreddedReader)reader);
            }
            
            return new ShreddedCombiner(shreddedReaders, closeOnExit);
        }                  
        public IdentifiedLink clone(IdentifiedLink object) {
            IdentifiedLink result = new IdentifiedLink();
            if (object == null) return result;
            result.identifier = object.identifier; 
            result.url = object.url; 
            result.anchorText = object.anchorText; 
            return result;
        }                 
        public Class<IdentifiedLink> getOrderedClass() {
            return IdentifiedLink.class;
        }                           
        public String[] getOrderSpec() {
            return new String[] {"+url"};
        }

        public static String[] getSpec() {
            return new String[] {"+url"};
        }
        public static String getSpecString() {
            return "+url";
        }
                           
        public interface ShreddedProcessor extends Step {
            public void processUrl(String url) throws IOException;
            public void processTuple(String identifier, String anchorText) throws IOException;
            public void close() throws IOException;
        }    
        public interface ShreddedSource extends Step {
        }                                              
        
        public static class ShreddedWriter implements ShreddedProcessor {
            ArrayOutput output;
            ShreddedBuffer buffer = new ShreddedBuffer();
            String lastUrl;
            boolean lastFlush = false;
            
            public ShreddedWriter(ArrayOutput output) {
                this.output = output;
            }                        
            
            public void close() throws IOException {
                flush();
            }
            
            public void processUrl(String url) {
                lastUrl = url;
                buffer.processUrl(url);
            }
            public final void processTuple(String identifier, String anchorText) throws IOException {
                if (lastFlush) {
                    if(buffer.urls.size() == 0) buffer.processUrl(lastUrl);
                    lastFlush = false;
                }
                buffer.processTuple(identifier, anchorText);
                if (buffer.isFull())
                    flush();
            }
            public final void flushTuples(int pauseIndex) throws IOException {
                
                while (buffer.getReadIndex() < pauseIndex) {
                           
                    output.writeString(buffer.getIdentifier());
                    output.writeString(buffer.getAnchorText());
                    buffer.incrementTuple();
                }
            }  
            public final void flushUrl(int pauseIndex) throws IOException {
                while (buffer.getReadIndex() < pauseIndex) {
                    int nextPause = buffer.getUrlEndIndex();
                    int count = nextPause - buffer.getReadIndex();
                    
                    output.writeString(buffer.getUrl());
                    output.writeInt(count);
                    buffer.incrementUrl();
                      
                    flushTuples(nextPause);
                    assert nextPause == buffer.getReadIndex();
                }
            }
            public void flush() throws IOException { 
                flushUrl(buffer.getWriteIndex());
                buffer.reset(); 
                lastFlush = true;
            }                           
        }
        public static class ShreddedBuffer {
            ArrayList<String> urls = new ArrayList();
            TIntArrayList urlTupleIdx = new TIntArrayList();
            int urlReadIdx = 0;
                            
            String[] identifiers;
            String[] anchorTexts;
            int writeTupleIndex = 0;
            int readTupleIndex = 0;
            int batchSize;

            public ShreddedBuffer(int batchSize) {
                this.batchSize = batchSize;

                identifiers = new String[batchSize];
                anchorTexts = new String[batchSize];
            }                              

            public ShreddedBuffer() {    
                this(10000);
            }                                                                                                                    
            
            public void processUrl(String url) {
                urls.add(url);
                urlTupleIdx.add(writeTupleIndex);
            }                                      
            public void processTuple(String identifier, String anchorText) {
                assert urls.size() > 0;
                identifiers[writeTupleIndex] = identifier;
                anchorTexts[writeTupleIndex] = anchorText;
                writeTupleIndex++;
            }
            public void resetData() {
                urls.clear();
                urlTupleIdx.clear();
                writeTupleIndex = 0;
            }                  
                                 
            public void resetRead() {
                readTupleIndex = 0;
                urlReadIdx = 0;
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
            public void incrementUrl() {
                urlReadIdx++;  
            }                                                                                              

            public void autoIncrementUrl() {
                while (readTupleIndex >= getUrlEndIndex() && readTupleIndex < writeTupleIndex)
                    urlReadIdx++;
            }                 
            public void incrementTuple() {
                readTupleIndex++;
            }                    
            public int getUrlEndIndex() {
                if ((urlReadIdx+1) >= urlTupleIdx.size())
                    return writeTupleIndex;
                return urlTupleIdx.get(urlReadIdx+1);
            }
            public int getReadIndex() {
                return readTupleIndex;
            }   

            public int getWriteIndex() {
                return writeTupleIndex;
            } 
            public String getUrl() {
                assert readTupleIndex < writeTupleIndex;
                assert urlReadIdx < urls.size();
                
                return urls.get(urlReadIdx);
            }
            public String getIdentifier() {
                assert readTupleIndex < writeTupleIndex;
                return identifiers[readTupleIndex];
            }                                         
            public String getAnchorText() {
                assert readTupleIndex < writeTupleIndex;
                return anchorTexts[readTupleIndex];
            }                                         
            public void copyTuples(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                   output.processTuple(getIdentifier(), getAnchorText());
                   incrementTuple();
                }
            }                                                                           
            public void copyUntilIndexUrl(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                    output.processUrl(getUrl());
                    assert getUrlEndIndex() <= endIndex;
                    copyTuples(getUrlEndIndex(), output);
                    incrementUrl();
                }
            }  
            public void copyUntilUrl(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                while (!isAtEnd()) {
                    if (other != null) {   
                        assert !other.isAtEnd();
                        int c = + Utility.compare(getUrl(), other.getUrl());
                    
                        if (c > 0) {
                            break;   
                        }
                        
                        output.processUrl(getUrl());
                                      
                        copyTuples(getUrlEndIndex(), output);
                    } else {
                        output.processUrl(getUrl());
                        copyTuples(getUrlEndIndex(), output);
                    }
                    incrementUrl();  
                    
               
                }
            }
            public void copyUntil(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                copyUntilUrl(other, output);
            }
            
        }                         
        public static class ShreddedCombiner implements ReaderSource<IdentifiedLink>, ShreddedSource {   
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
                } else if (processor instanceof IdentifiedLink.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((IdentifiedLink.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<IdentifiedLink>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<IdentifiedLink> getOutputClass() {
                return IdentifiedLink.class;
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

            public IdentifiedLink read() throws IOException {
                if (uninitialized)
                    initialize();

                IdentifiedLink result = null;

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
        public static class ShreddedReader implements Step, Comparable<ShreddedReader>, TypeReader<IdentifiedLink>, ShreddedSource {      
            public ShreddedProcessor processor;
            ShreddedBuffer buffer;
            IdentifiedLink last = new IdentifiedLink();         
            long updateUrlCount = -1;
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
                    result = + Utility.compare(buffer.getUrl(), otherBuffer.getUrl());
                    if(result != 0) break;
                } while (false);                                             
                
                return result;
            }
            
            public final ShreddedBuffer getBuffer() {
                return buffer;
            }                
            
            public final IdentifiedLink read() throws IOException {
                if (buffer.isAtEnd()) {
                    fill();             
                
                    if (buffer.isAtEnd()) {
                        return null;
                    }
                }
                      
                assert !buffer.isAtEnd();
                IdentifiedLink result = new IdentifiedLink();
                
                result.url = buffer.getUrl();
                result.identifier = buffer.getIdentifier();
                result.anchorText = buffer.getAnchorText();
                
                buffer.incrementTuple();
                buffer.autoIncrementUrl();
                
                return result;
            }           
            
            public final void fill() throws IOException {
                try {   
                    buffer.reset();
                    
                    if (tupleCount != 0) {
                                                      
                        if(updateUrlCount - tupleCount > 0) {
                            buffer.urls.add(last.url);
                            buffer.urlTupleIdx.add((int) (updateUrlCount - tupleCount));
                        }
                        bufferStartCount = tupleCount;
                    }
                    
                    while (!buffer.isFull()) {
                        updateUrl();
                        buffer.processTuple(input.readString(), input.readString());
                        tupleCount++;
                    }
                } catch(EOFException e) {}
            }

            public final void updateUrl() throws IOException {
                if (updateUrlCount > tupleCount)
                    return;
                     
                last.url = input.readString();
                updateUrlCount = tupleCount + input.readInt();
                                      
                buffer.processUrl(last.url);
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
                } else if (processor instanceof IdentifiedLink.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((IdentifiedLink.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<IdentifiedLink>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<IdentifiedLink> getOutputClass() {
                return IdentifiedLink.class;
            }                
        }
        
        public static class DuplicateEliminator implements ShreddedProcessor {
            public ShreddedProcessor processor;
            IdentifiedLink last = new IdentifiedLink();
            boolean urlProcess = true;
                                           
            public DuplicateEliminator() {}
            public DuplicateEliminator(ShreddedProcessor processor) {
                this.processor = processor;
            }
            
            public void setShreddedProcessor(ShreddedProcessor processor) {
                this.processor = processor;
            }

            public void processUrl(String url) throws IOException {  
                if (urlProcess || Utility.compare(url, last.url) != 0) {
                    last.url = url;
                    processor.processUrl(url);
                    urlProcess = false;
                }
            }  
            
            public void resetUrl() {
                 urlProcess = true;
            }                                                
                               
            public void processTuple(String identifier, String anchorText) throws IOException {
                processor.processTuple(identifier, anchorText);
            } 
            
            public void close() throws IOException {
                processor.close();
            }                    
        }
        public static class TupleUnshredder implements ShreddedProcessor {
            IdentifiedLink last = new IdentifiedLink();
            public org.lemurproject.galago.tupleflow.Processor<IdentifiedLink> processor;                               
            
            public TupleUnshredder(IdentifiedLink.Processor processor) {
                this.processor = processor;
            }         
            
            public TupleUnshredder(org.lemurproject.galago.tupleflow.Processor<IdentifiedLink> processor) {
                this.processor = processor;
            }
            
            public IdentifiedLink clone(IdentifiedLink object) {
                IdentifiedLink result = new IdentifiedLink();
                if (object == null) return result;
                result.identifier = object.identifier; 
                result.url = object.url; 
                result.anchorText = object.anchorText; 
                return result;
            }                 
            
            public void processUrl(String url) throws IOException {
                last.url = url;
            }   
                
            
            public void processTuple(String identifier, String anchorText) throws IOException {
                last.identifier = identifier;
                last.anchorText = anchorText;
                processor.process(clone(last));
            }               
            
            public void close() throws IOException {
                processor.close();
            }
        }     
        public static class TupleShredder implements Processor {
            IdentifiedLink last = null;
            public ShreddedProcessor processor;
            
            public TupleShredder(ShreddedProcessor processor) {
                this.processor = processor;
            }                              
            
            public IdentifiedLink clone(IdentifiedLink object) {
                IdentifiedLink result = new IdentifiedLink();
                if (object == null) return result;
                result.identifier = object.identifier; 
                result.url = object.url; 
                result.anchorText = object.anchorText; 
                return result;
            }                 
            
            public void process(IdentifiedLink object) throws IOException {                                                                                                                                                   
                boolean processAll = false;
                if(last == null || Utility.compare(last.url, object.url) != 0 || processAll) { processor.processUrl(object.url); processAll = true; }
                processor.processTuple(object.identifier, object.anchorText);                                         
                last = object;
            }
                          
            public Class<IdentifiedLink> getInputClass() {
                return IdentifiedLink.class;
            }
            
            public void close() throws IOException {
                processor.close();
            }                     
        }
    } 
}    