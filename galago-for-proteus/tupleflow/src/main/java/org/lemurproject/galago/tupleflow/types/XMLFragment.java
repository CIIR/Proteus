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


public class XMLFragment implements Type<XMLFragment> {
    public String nodePath;
    public String innerText; 
    
    public XMLFragment() {}
    public XMLFragment(String nodePath, String innerText) {
        this.nodePath = nodePath;
        this.innerText = innerText;
    }  
    
    public String toString() {
            return String.format("%s,%s",
                                   nodePath, innerText);
    } 

    public Order<XMLFragment> getOrder(String... spec) {
        if (Arrays.equals(spec, new String[] { "+nodePath" })) {
            return new NodePathOrder();
        }
        return null;
    } 
      
    public interface Processor extends Step, org.lemurproject.galago.tupleflow.Processor<XMLFragment> {
        public void process(XMLFragment object) throws IOException;
        public void close() throws IOException;
    }                        
    public interface Source extends Step {
    }
    public static class NodePathOrder implements Order<XMLFragment> {
        public int hash(XMLFragment object) {
            int h = 0;
            h += Utility.hash(object.nodePath);
            return h;
        } 
        public Comparator<XMLFragment> greaterThan() {
            return new Comparator<XMLFragment>() {
                public int compare(XMLFragment one, XMLFragment two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.nodePath, two.nodePath);
                        if(result != 0) break;
                    } while (false);
                    return -result;
                }
            };
        }     
        public Comparator<XMLFragment> lessThan() {
            return new Comparator<XMLFragment>() {
                public int compare(XMLFragment one, XMLFragment two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.nodePath, two.nodePath);
                        if(result != 0) break;
                    } while (false);
                    return result;
                }
            };
        }     
        public TypeReader<XMLFragment> orderedReader(ArrayInput _input) {
            return new ShreddedReader(_input);
        }    

        public TypeReader<XMLFragment> orderedReader(ArrayInput _input, int bufferSize) {
            return new ShreddedReader(_input, bufferSize);
        }    
        public OrderedWriter<XMLFragment> orderedWriter(ArrayOutput _output) {
            ShreddedWriter w = new ShreddedWriter(_output);
            return new OrderedWriterClass(w); 
        }                                    
        public static class OrderedWriterClass extends OrderedWriter< XMLFragment > {
            XMLFragment last = null;
            ShreddedWriter shreddedWriter = null; 
            
            public OrderedWriterClass(ShreddedWriter s) {
                this.shreddedWriter = s;
            }
            
            public void process(XMLFragment object) throws IOException {
               boolean processAll = false;
               if (processAll || last == null || 0 != Utility.compare(object.nodePath, last.nodePath)) { processAll = true; shreddedWriter.processNodePath(object.nodePath); }
               shreddedWriter.processTuple(object.innerText);
               last = object;
            }           
                 
            public void close() throws IOException {
                shreddedWriter.close();
            }
            
            public Class<XMLFragment> getInputClass() {
                return XMLFragment.class;
            }
        } 
        public ReaderSource<XMLFragment> orderedCombiner(Collection<TypeReader<XMLFragment>> readers, boolean closeOnExit) {
            ArrayList<ShreddedReader> shreddedReaders = new ArrayList();
            
            for (TypeReader<XMLFragment> reader : readers) {
                shreddedReaders.add((ShreddedReader)reader);
            }
            
            return new ShreddedCombiner(shreddedReaders, closeOnExit);
        }                  
        public XMLFragment clone(XMLFragment object) {
            XMLFragment result = new XMLFragment();
            if (object == null) return result;
            result.nodePath = object.nodePath; 
            result.innerText = object.innerText; 
            return result;
        }                 
        public Class<XMLFragment> getOrderedClass() {
            return XMLFragment.class;
        }                           
        public String[] getOrderSpec() {
            return new String[] {"+nodePath"};
        }

        public static String[] getSpec() {
            return new String[] {"+nodePath"};
        }
        public static String getSpecString() {
            return "+nodePath";
        }
                           
        public interface ShreddedProcessor extends Step {
            public void processNodePath(String nodePath) throws IOException;
            public void processTuple(String innerText) throws IOException;
            public void close() throws IOException;
        }    
        public interface ShreddedSource extends Step {
        }                                              
        
        public static class ShreddedWriter implements ShreddedProcessor {
            ArrayOutput output;
            ShreddedBuffer buffer = new ShreddedBuffer();
            String lastNodePath;
            boolean lastFlush = false;
            
            public ShreddedWriter(ArrayOutput output) {
                this.output = output;
            }                        
            
            public void close() throws IOException {
                flush();
            }
            
            public void processNodePath(String nodePath) {
                lastNodePath = nodePath;
                buffer.processNodePath(nodePath);
            }
            public final void processTuple(String innerText) throws IOException {
                if (lastFlush) {
                    if(buffer.nodePaths.size() == 0) buffer.processNodePath(lastNodePath);
                    lastFlush = false;
                }
                buffer.processTuple(innerText);
                if (buffer.isFull())
                    flush();
            }
            public final void flushTuples(int pauseIndex) throws IOException {
                
                while (buffer.getReadIndex() < pauseIndex) {
                           
                    output.writeString(buffer.getInnerText());
                    buffer.incrementTuple();
                }
            }  
            public final void flushNodePath(int pauseIndex) throws IOException {
                while (buffer.getReadIndex() < pauseIndex) {
                    int nextPause = buffer.getNodePathEndIndex();
                    int count = nextPause - buffer.getReadIndex();
                    
                    output.writeString(buffer.getNodePath());
                    output.writeInt(count);
                    buffer.incrementNodePath();
                      
                    flushTuples(nextPause);
                    assert nextPause == buffer.getReadIndex();
                }
            }
            public void flush() throws IOException { 
                flushNodePath(buffer.getWriteIndex());
                buffer.reset(); 
                lastFlush = true;
            }                           
        }
        public static class ShreddedBuffer {
            ArrayList<String> nodePaths = new ArrayList();
            TIntArrayList nodePathTupleIdx = new TIntArrayList();
            int nodePathReadIdx = 0;
                            
            String[] innerTexts;
            int writeTupleIndex = 0;
            int readTupleIndex = 0;
            int batchSize;

            public ShreddedBuffer(int batchSize) {
                this.batchSize = batchSize;

                innerTexts = new String[batchSize];
            }                              

            public ShreddedBuffer() {    
                this(10000);
            }                                                                                                                    
            
            public void processNodePath(String nodePath) {
                nodePaths.add(nodePath);
                nodePathTupleIdx.add(writeTupleIndex);
            }                                      
            public void processTuple(String innerText) {
                assert nodePaths.size() > 0;
                innerTexts[writeTupleIndex] = innerText;
                writeTupleIndex++;
            }
            public void resetData() {
                nodePaths.clear();
                nodePathTupleIdx.clear();
                writeTupleIndex = 0;
            }                  
                                 
            public void resetRead() {
                readTupleIndex = 0;
                nodePathReadIdx = 0;
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
            public void incrementNodePath() {
                nodePathReadIdx++;  
            }                                                                                              

            public void autoIncrementNodePath() {
                while (readTupleIndex >= getNodePathEndIndex() && readTupleIndex < writeTupleIndex)
                    nodePathReadIdx++;
            }                 
            public void incrementTuple() {
                readTupleIndex++;
            }                    
            public int getNodePathEndIndex() {
                if ((nodePathReadIdx+1) >= nodePathTupleIdx.size())
                    return writeTupleIndex;
                return nodePathTupleIdx.get(nodePathReadIdx+1);
            }
            public int getReadIndex() {
                return readTupleIndex;
            }   

            public int getWriteIndex() {
                return writeTupleIndex;
            } 
            public String getNodePath() {
                assert readTupleIndex < writeTupleIndex;
                assert nodePathReadIdx < nodePaths.size();
                
                return nodePaths.get(nodePathReadIdx);
            }
            public String getInnerText() {
                assert readTupleIndex < writeTupleIndex;
                return innerTexts[readTupleIndex];
            }                                         
            public void copyTuples(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                   output.processTuple(getInnerText());
                   incrementTuple();
                }
            }                                                                           
            public void copyUntilIndexNodePath(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                    output.processNodePath(getNodePath());
                    assert getNodePathEndIndex() <= endIndex;
                    copyTuples(getNodePathEndIndex(), output);
                    incrementNodePath();
                }
            }  
            public void copyUntilNodePath(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                while (!isAtEnd()) {
                    if (other != null) {   
                        assert !other.isAtEnd();
                        int c = + Utility.compare(getNodePath(), other.getNodePath());
                    
                        if (c > 0) {
                            break;   
                        }
                        
                        output.processNodePath(getNodePath());
                                      
                        copyTuples(getNodePathEndIndex(), output);
                    } else {
                        output.processNodePath(getNodePath());
                        copyTuples(getNodePathEndIndex(), output);
                    }
                    incrementNodePath();  
                    
               
                }
            }
            public void copyUntil(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                copyUntilNodePath(other, output);
            }
            
        }                         
        public static class ShreddedCombiner implements ReaderSource<XMLFragment>, ShreddedSource {   
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
                } else if (processor instanceof XMLFragment.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((XMLFragment.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<XMLFragment>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<XMLFragment> getOutputClass() {
                return XMLFragment.class;
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

            public XMLFragment read() throws IOException {
                if (uninitialized)
                    initialize();

                XMLFragment result = null;

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
        public static class ShreddedReader implements Step, Comparable<ShreddedReader>, TypeReader<XMLFragment>, ShreddedSource {      
            public ShreddedProcessor processor;
            ShreddedBuffer buffer;
            XMLFragment last = new XMLFragment();         
            long updateNodePathCount = -1;
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
                    result = + Utility.compare(buffer.getNodePath(), otherBuffer.getNodePath());
                    if(result != 0) break;
                } while (false);                                             
                
                return result;
            }
            
            public final ShreddedBuffer getBuffer() {
                return buffer;
            }                
            
            public final XMLFragment read() throws IOException {
                if (buffer.isAtEnd()) {
                    fill();             
                
                    if (buffer.isAtEnd()) {
                        return null;
                    }
                }
                      
                assert !buffer.isAtEnd();
                XMLFragment result = new XMLFragment();
                
                result.nodePath = buffer.getNodePath();
                result.innerText = buffer.getInnerText();
                
                buffer.incrementTuple();
                buffer.autoIncrementNodePath();
                
                return result;
            }           
            
            public final void fill() throws IOException {
                try {   
                    buffer.reset();
                    
                    if (tupleCount != 0) {
                                                      
                        if(updateNodePathCount - tupleCount > 0) {
                            buffer.nodePaths.add(last.nodePath);
                            buffer.nodePathTupleIdx.add((int) (updateNodePathCount - tupleCount));
                        }
                        bufferStartCount = tupleCount;
                    }
                    
                    while (!buffer.isFull()) {
                        updateNodePath();
                        buffer.processTuple(input.readString());
                        tupleCount++;
                    }
                } catch(EOFException e) {}
            }

            public final void updateNodePath() throws IOException {
                if (updateNodePathCount > tupleCount)
                    return;
                     
                last.nodePath = input.readString();
                updateNodePathCount = tupleCount + input.readInt();
                                      
                buffer.processNodePath(last.nodePath);
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
                } else if (processor instanceof XMLFragment.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((XMLFragment.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<XMLFragment>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<XMLFragment> getOutputClass() {
                return XMLFragment.class;
            }                
        }
        
        public static class DuplicateEliminator implements ShreddedProcessor {
            public ShreddedProcessor processor;
            XMLFragment last = new XMLFragment();
            boolean nodePathProcess = true;
                                           
            public DuplicateEliminator() {}
            public DuplicateEliminator(ShreddedProcessor processor) {
                this.processor = processor;
            }
            
            public void setShreddedProcessor(ShreddedProcessor processor) {
                this.processor = processor;
            }

            public void processNodePath(String nodePath) throws IOException {  
                if (nodePathProcess || Utility.compare(nodePath, last.nodePath) != 0) {
                    last.nodePath = nodePath;
                    processor.processNodePath(nodePath);
                    nodePathProcess = false;
                }
            }  
            
            public void resetNodePath() {
                 nodePathProcess = true;
            }                                                
                               
            public void processTuple(String innerText) throws IOException {
                processor.processTuple(innerText);
            } 
            
            public void close() throws IOException {
                processor.close();
            }                    
        }
        public static class TupleUnshredder implements ShreddedProcessor {
            XMLFragment last = new XMLFragment();
            public org.lemurproject.galago.tupleflow.Processor<XMLFragment> processor;                               
            
            public TupleUnshredder(XMLFragment.Processor processor) {
                this.processor = processor;
            }         
            
            public TupleUnshredder(org.lemurproject.galago.tupleflow.Processor<XMLFragment> processor) {
                this.processor = processor;
            }
            
            public XMLFragment clone(XMLFragment object) {
                XMLFragment result = new XMLFragment();
                if (object == null) return result;
                result.nodePath = object.nodePath; 
                result.innerText = object.innerText; 
                return result;
            }                 
            
            public void processNodePath(String nodePath) throws IOException {
                last.nodePath = nodePath;
            }   
                
            
            public void processTuple(String innerText) throws IOException {
                last.innerText = innerText;
                processor.process(clone(last));
            }               
            
            public void close() throws IOException {
                processor.close();
            }
        }     
        public static class TupleShredder implements Processor {
            XMLFragment last = null;
            public ShreddedProcessor processor;
            
            public TupleShredder(ShreddedProcessor processor) {
                this.processor = processor;
            }                              
            
            public XMLFragment clone(XMLFragment object) {
                XMLFragment result = new XMLFragment();
                if (object == null) return result;
                result.nodePath = object.nodePath; 
                result.innerText = object.innerText; 
                return result;
            }                 
            
            public void process(XMLFragment object) throws IOException {                                                                                                                                                   
                boolean processAll = false;
                if(last == null || Utility.compare(last.nodePath, object.nodePath) != 0 || processAll) { processor.processNodePath(object.nodePath); processAll = true; }
                processor.processTuple(object.innerText);                                         
                last = object;
            }
                          
            public Class<XMLFragment> getInputClass() {
                return XMLFragment.class;
            }
            
            public void close() throws IOException {
                processor.close();
            }                     
        }
    } 
}    