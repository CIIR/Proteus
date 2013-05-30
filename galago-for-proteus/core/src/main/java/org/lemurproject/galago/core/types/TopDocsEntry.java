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


public class TopDocsEntry implements Type<TopDocsEntry> {
    public int document;
    public byte[] word;
    public double probability;
    public int count;
    public int doclength; 
    
    public TopDocsEntry() {}
    public TopDocsEntry(int document, byte[] word, double probability, int count, int doclength) {
        this.document = document;
        this.word = word;
        this.probability = probability;
        this.count = count;
        this.doclength = doclength;
    }  
    
    public String toString() {
        try {
            return String.format("%d,%s,%f,%d,%d",
                                   document, new String(word, "UTF-8"), probability, count, doclength);
        } catch(UnsupportedEncodingException e) {
            throw new RuntimeException("Couldn't convert string to UTF-8.");
        }
    } 

    public Order<TopDocsEntry> getOrder(String... spec) {
        if (Arrays.equals(spec, new String[] { "+word", "-probability", "+document" })) {
            return new WordDescProbabilityDocumentOrder();
        }
        if (Arrays.equals(spec, new String[] { "+word", "+document" })) {
            return new WordDocumentOrder();
        }
        return null;
    } 
      
    public interface Processor extends Step, org.lemurproject.galago.tupleflow.Processor<TopDocsEntry> {
        public void process(TopDocsEntry object) throws IOException;
        public void close() throws IOException;
    }                        
    public interface Source extends Step {
    }
    public static class WordDescProbabilityDocumentOrder implements Order<TopDocsEntry> {
        public int hash(TopDocsEntry object) {
            int h = 0;
            h += Utility.hash(object.word);
            h += Utility.hash(object.probability);
            h += Utility.hash(object.document);
            return h;
        } 
        public Comparator<TopDocsEntry> greaterThan() {
            return new Comparator<TopDocsEntry>() {
                public int compare(TopDocsEntry one, TopDocsEntry two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.word, two.word);
                        if(result != 0) break;
                        result = - Utility.compare(one.probability, two.probability);
                        if(result != 0) break;
                        result = + Utility.compare(one.document, two.document);
                        if(result != 0) break;
                    } while (false);
                    return -result;
                }
            };
        }     
        public Comparator<TopDocsEntry> lessThan() {
            return new Comparator<TopDocsEntry>() {
                public int compare(TopDocsEntry one, TopDocsEntry two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.word, two.word);
                        if(result != 0) break;
                        result = - Utility.compare(one.probability, two.probability);
                        if(result != 0) break;
                        result = + Utility.compare(one.document, two.document);
                        if(result != 0) break;
                    } while (false);
                    return result;
                }
            };
        }     
        public TypeReader<TopDocsEntry> orderedReader(ArrayInput _input) {
            return new ShreddedReader(_input);
        }    

        public TypeReader<TopDocsEntry> orderedReader(ArrayInput _input, int bufferSize) {
            return new ShreddedReader(_input, bufferSize);
        }    
        public OrderedWriter<TopDocsEntry> orderedWriter(ArrayOutput _output) {
            ShreddedWriter w = new ShreddedWriter(_output);
            return new OrderedWriterClass(w); 
        }                                    
        public static class OrderedWriterClass extends OrderedWriter< TopDocsEntry > {
            TopDocsEntry last = null;
            ShreddedWriter shreddedWriter = null; 
            
            public OrderedWriterClass(ShreddedWriter s) {
                this.shreddedWriter = s;
            }
            
            public void process(TopDocsEntry object) throws IOException {
               boolean processAll = false;
               if (processAll || last == null || 0 != Utility.compare(object.word, last.word)) { processAll = true; shreddedWriter.processWord(object.word); }
               if (processAll || last == null || 0 != Utility.compare(object.probability, last.probability)) { processAll = true; shreddedWriter.processProbability(object.probability); }
               if (processAll || last == null || 0 != Utility.compare(object.document, last.document)) { processAll = true; shreddedWriter.processDocument(object.document); }
               shreddedWriter.processTuple(object.count, object.doclength);
               last = object;
            }           
                 
            public void close() throws IOException {
                shreddedWriter.close();
            }
            
            public Class<TopDocsEntry> getInputClass() {
                return TopDocsEntry.class;
            }
        } 
        public ReaderSource<TopDocsEntry> orderedCombiner(Collection<TypeReader<TopDocsEntry>> readers, boolean closeOnExit) {
            ArrayList<ShreddedReader> shreddedReaders = new ArrayList();
            
            for (TypeReader<TopDocsEntry> reader : readers) {
                shreddedReaders.add((ShreddedReader)reader);
            }
            
            return new ShreddedCombiner(shreddedReaders, closeOnExit);
        }                  
        public TopDocsEntry clone(TopDocsEntry object) {
            TopDocsEntry result = new TopDocsEntry();
            if (object == null) return result;
            result.document = object.document; 
            result.word = object.word; 
            result.probability = object.probability; 
            result.count = object.count; 
            result.doclength = object.doclength; 
            return result;
        }                 
        public Class<TopDocsEntry> getOrderedClass() {
            return TopDocsEntry.class;
        }                           
        public String[] getOrderSpec() {
            return new String[] {"+word", "-probability", "+document"};
        }

        public static String[] getSpec() {
            return new String[] {"+word", "-probability", "+document"};
        }
        public static String getSpecString() {
            return "+word -probability +document";
        }
                           
        public interface ShreddedProcessor extends Step {
            public void processWord(byte[] word) throws IOException;
            public void processProbability(double probability) throws IOException;
            public void processDocument(int document) throws IOException;
            public void processTuple(int count, int doclength) throws IOException;
            public void close() throws IOException;
        }    
        public interface ShreddedSource extends Step {
        }                                              
        
        public static class ShreddedWriter implements ShreddedProcessor {
            ArrayOutput output;
            ShreddedBuffer buffer = new ShreddedBuffer();
            byte[] lastWord;
            double lastProbability;
            int lastDocument;
            boolean lastFlush = false;
            
            public ShreddedWriter(ArrayOutput output) {
                this.output = output;
            }                        
            
            public void close() throws IOException {
                flush();
            }
            
            public void processWord(byte[] word) {
                lastWord = word;
                buffer.processWord(word);
            }
            public void processProbability(double probability) {
                lastProbability = probability;
                buffer.processProbability(probability);
            }
            public void processDocument(int document) {
                lastDocument = document;
                buffer.processDocument(document);
            }
            public final void processTuple(int count, int doclength) throws IOException {
                if (lastFlush) {
                    if(buffer.words.size() == 0) buffer.processWord(lastWord);
                    if(buffer.probabilitys.size() == 0) buffer.processProbability(lastProbability);
                    if(buffer.documents.size() == 0) buffer.processDocument(lastDocument);
                    lastFlush = false;
                }
                buffer.processTuple(count, doclength);
                if (buffer.isFull())
                    flush();
            }
            public final void flushTuples(int pauseIndex) throws IOException {
                
                while (buffer.getReadIndex() < pauseIndex) {
                           
                    output.writeInt(buffer.getCount());
                    output.writeInt(buffer.getDoclength());
                    buffer.incrementTuple();
                }
            }  
            public final void flushWord(int pauseIndex) throws IOException {
                while (buffer.getReadIndex() < pauseIndex) {
                    int nextPause = buffer.getWordEndIndex();
                    int count = nextPause - buffer.getReadIndex();
                    
                    output.writeBytes(buffer.getWord());
                    output.writeInt(count);
                    buffer.incrementWord();
                      
                    flushProbability(nextPause);
                    assert nextPause == buffer.getReadIndex();
                }
            }
            public final void flushProbability(int pauseIndex) throws IOException {
                while (buffer.getReadIndex() < pauseIndex) {
                    int nextPause = buffer.getProbabilityEndIndex();
                    int count = nextPause - buffer.getReadIndex();
                    
                    output.writeDouble(buffer.getProbability());
                    output.writeInt(count);
                    buffer.incrementProbability();
                      
                    flushDocument(nextPause);
                    assert nextPause == buffer.getReadIndex();
                }
            }
            public final void flushDocument(int pauseIndex) throws IOException {
                while (buffer.getReadIndex() < pauseIndex) {
                    int nextPause = buffer.getDocumentEndIndex();
                    int count = nextPause - buffer.getReadIndex();
                    
                    output.writeInt(buffer.getDocument());
                    output.writeInt(count);
                    buffer.incrementDocument();
                      
                    flushTuples(nextPause);
                    assert nextPause == buffer.getReadIndex();
                }
            }
            public void flush() throws IOException { 
                flushWord(buffer.getWriteIndex());
                buffer.reset(); 
                lastFlush = true;
            }                           
        }
        public static class ShreddedBuffer {
            ArrayList<byte[]> words = new ArrayList();
            TDoubleArrayList probabilitys = new TDoubleArrayList();
            TIntArrayList documents = new TIntArrayList();
            TIntArrayList wordTupleIdx = new TIntArrayList();
            TIntArrayList probabilityTupleIdx = new TIntArrayList();
            TIntArrayList documentTupleIdx = new TIntArrayList();
            int wordReadIdx = 0;
            int probabilityReadIdx = 0;
            int documentReadIdx = 0;
                            
            int[] counts;
            int[] doclengths;
            int writeTupleIndex = 0;
            int readTupleIndex = 0;
            int batchSize;

            public ShreddedBuffer(int batchSize) {
                this.batchSize = batchSize;

                counts = new int[batchSize];
                doclengths = new int[batchSize];
            }                              

            public ShreddedBuffer() {    
                this(10000);
            }                                                                                                                    
            
            public void processWord(byte[] word) {
                words.add(word);
                wordTupleIdx.add(writeTupleIndex);
            }                                      
            public void processProbability(double probability) {
                probabilitys.add(probability);
                probabilityTupleIdx.add(writeTupleIndex);
            }                                      
            public void processDocument(int document) {
                documents.add(document);
                documentTupleIdx.add(writeTupleIndex);
            }                                      
            public void processTuple(int count, int doclength) {
                assert words.size() > 0;
                assert probabilitys.size() > 0;
                assert documents.size() > 0;
                counts[writeTupleIndex] = count;
                doclengths[writeTupleIndex] = doclength;
                writeTupleIndex++;
            }
            public void resetData() {
                words.clear();
                probabilitys.clear();
                documents.clear();
                wordTupleIdx.clear();
                probabilityTupleIdx.clear();
                documentTupleIdx.clear();
                writeTupleIndex = 0;
            }                  
                                 
            public void resetRead() {
                readTupleIndex = 0;
                wordReadIdx = 0;
                probabilityReadIdx = 0;
                documentReadIdx = 0;
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
            public void incrementWord() {
                wordReadIdx++;  
            }                                                                                              

            public void autoIncrementWord() {
                while (readTupleIndex >= getWordEndIndex() && readTupleIndex < writeTupleIndex)
                    wordReadIdx++;
            }                 
            public void incrementProbability() {
                probabilityReadIdx++;  
            }                                                                                              

            public void autoIncrementProbability() {
                while (readTupleIndex >= getProbabilityEndIndex() && readTupleIndex < writeTupleIndex)
                    probabilityReadIdx++;
            }                 
            public void incrementDocument() {
                documentReadIdx++;  
            }                                                                                              

            public void autoIncrementDocument() {
                while (readTupleIndex >= getDocumentEndIndex() && readTupleIndex < writeTupleIndex)
                    documentReadIdx++;
            }                 
            public void incrementTuple() {
                readTupleIndex++;
            }                    
            public int getWordEndIndex() {
                if ((wordReadIdx+1) >= wordTupleIdx.size())
                    return writeTupleIndex;
                return wordTupleIdx.get(wordReadIdx+1);
            }

            public int getProbabilityEndIndex() {
                if ((probabilityReadIdx+1) >= probabilityTupleIdx.size())
                    return writeTupleIndex;
                return probabilityTupleIdx.get(probabilityReadIdx+1);
            }

            public int getDocumentEndIndex() {
                if ((documentReadIdx+1) >= documentTupleIdx.size())
                    return writeTupleIndex;
                return documentTupleIdx.get(documentReadIdx+1);
            }
            public int getReadIndex() {
                return readTupleIndex;
            }   

            public int getWriteIndex() {
                return writeTupleIndex;
            } 
            public byte[] getWord() {
                assert readTupleIndex < writeTupleIndex;
                assert wordReadIdx < words.size();
                
                return words.get(wordReadIdx);
            }
            public double getProbability() {
                assert readTupleIndex < writeTupleIndex;
                assert probabilityReadIdx < probabilitys.size();
                
                return probabilitys.get(probabilityReadIdx);
            }
            public int getDocument() {
                assert readTupleIndex < writeTupleIndex;
                assert documentReadIdx < documents.size();
                
                return documents.get(documentReadIdx);
            }
            public int getCount() {
                assert readTupleIndex < writeTupleIndex;
                return counts[readTupleIndex];
            }                                         
            public int getDoclength() {
                assert readTupleIndex < writeTupleIndex;
                return doclengths[readTupleIndex];
            }                                         
            public void copyTuples(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                   output.processTuple(getCount(), getDoclength());
                   incrementTuple();
                }
            }                                                                           
            public void copyUntilIndexWord(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                    output.processWord(getWord());
                    assert getWordEndIndex() <= endIndex;
                    copyUntilIndexProbability(getWordEndIndex(), output);
                    incrementWord();
                }
            } 
            public void copyUntilIndexProbability(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                    output.processProbability(getProbability());
                    assert getProbabilityEndIndex() <= endIndex;
                    copyUntilIndexDocument(getProbabilityEndIndex(), output);
                    incrementProbability();
                }
            } 
            public void copyUntilIndexDocument(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                    output.processDocument(getDocument());
                    assert getDocumentEndIndex() <= endIndex;
                    copyTuples(getDocumentEndIndex(), output);
                    incrementDocument();
                }
            }  
            public void copyUntilWord(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                while (!isAtEnd()) {
                    if (other != null) {   
                        assert !other.isAtEnd();
                        int c = + Utility.compare(getWord(), other.getWord());
                    
                        if (c > 0) {
                            break;   
                        }
                        
                        output.processWord(getWord());
                                      
                        if (c < 0) {
                            copyUntilIndexProbability(getWordEndIndex(), output);
                        } else if (c == 0) {
                            copyUntilProbability(other, output);
                            autoIncrementWord();
                            break;
                        }
                    } else {
                        output.processWord(getWord());
                        copyUntilIndexProbability(getWordEndIndex(), output);
                    }
                    incrementWord();  
                    
               
                }
            }
            public void copyUntilProbability(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                while (!isAtEnd()) {
                    if (other != null) {   
                        assert !other.isAtEnd();
                        int c = - Utility.compare(getProbability(), other.getProbability());
                    
                        if (c > 0) {
                            break;   
                        }
                        
                        output.processProbability(getProbability());
                                      
                        if (c < 0) {
                            copyUntilIndexDocument(getProbabilityEndIndex(), output);
                        } else if (c == 0) {
                            copyUntilDocument(other, output);
                            autoIncrementProbability();
                            break;
                        }
                    } else {
                        output.processProbability(getProbability());
                        copyUntilIndexDocument(getProbabilityEndIndex(), output);
                    }
                    incrementProbability();  
                    
                    if (getWordEndIndex() <= readTupleIndex)
                        break;   
                }
            }
            public void copyUntilDocument(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                while (!isAtEnd()) {
                    if (other != null) {   
                        assert !other.isAtEnd();
                        int c = + Utility.compare(getDocument(), other.getDocument());
                    
                        if (c > 0) {
                            break;   
                        }
                        
                        output.processDocument(getDocument());
                                      
                        copyTuples(getDocumentEndIndex(), output);
                    } else {
                        output.processDocument(getDocument());
                        copyTuples(getDocumentEndIndex(), output);
                    }
                    incrementDocument();  
                    
                    if (getProbabilityEndIndex() <= readTupleIndex)
                        break;   
                }
            }
            public void copyUntil(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                copyUntilWord(other, output);
            }
            
        }                         
        public static class ShreddedCombiner implements ReaderSource<TopDocsEntry>, ShreddedSource {   
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
                } else if (processor instanceof TopDocsEntry.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((TopDocsEntry.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<TopDocsEntry>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<TopDocsEntry> getOutputClass() {
                return TopDocsEntry.class;
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

            public TopDocsEntry read() throws IOException {
                if (uninitialized)
                    initialize();

                TopDocsEntry result = null;

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
        public static class ShreddedReader implements Step, Comparable<ShreddedReader>, TypeReader<TopDocsEntry>, ShreddedSource {      
            public ShreddedProcessor processor;
            ShreddedBuffer buffer;
            TopDocsEntry last = new TopDocsEntry();         
            long updateWordCount = -1;
            long updateProbabilityCount = -1;
            long updateDocumentCount = -1;
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
                    result = + Utility.compare(buffer.getWord(), otherBuffer.getWord());
                    if(result != 0) break;
                    result = - Utility.compare(buffer.getProbability(), otherBuffer.getProbability());
                    if(result != 0) break;
                    result = + Utility.compare(buffer.getDocument(), otherBuffer.getDocument());
                    if(result != 0) break;
                } while (false);                                             
                
                return result;
            }
            
            public final ShreddedBuffer getBuffer() {
                return buffer;
            }                
            
            public final TopDocsEntry read() throws IOException {
                if (buffer.isAtEnd()) {
                    fill();             
                
                    if (buffer.isAtEnd()) {
                        return null;
                    }
                }
                      
                assert !buffer.isAtEnd();
                TopDocsEntry result = new TopDocsEntry();
                
                result.word = buffer.getWord();
                result.probability = buffer.getProbability();
                result.document = buffer.getDocument();
                result.count = buffer.getCount();
                result.doclength = buffer.getDoclength();
                
                buffer.incrementTuple();
                buffer.autoIncrementWord();
                buffer.autoIncrementProbability();
                buffer.autoIncrementDocument();
                
                return result;
            }           
            
            public final void fill() throws IOException {
                try {   
                    buffer.reset();
                    
                    if (tupleCount != 0) {
                                                      
                        if(updateWordCount - tupleCount > 0) {
                            buffer.words.add(last.word);
                            buffer.wordTupleIdx.add((int) (updateWordCount - tupleCount));
                        }                              
                        if(updateProbabilityCount - tupleCount > 0) {
                            buffer.probabilitys.add(last.probability);
                            buffer.probabilityTupleIdx.add((int) (updateProbabilityCount - tupleCount));
                        }                              
                        if(updateDocumentCount - tupleCount > 0) {
                            buffer.documents.add(last.document);
                            buffer.documentTupleIdx.add((int) (updateDocumentCount - tupleCount));
                        }
                        bufferStartCount = tupleCount;
                    }
                    
                    while (!buffer.isFull()) {
                        updateDocument();
                        buffer.processTuple(input.readInt(), input.readInt());
                        tupleCount++;
                    }
                } catch(EOFException e) {}
            }

            public final void updateWord() throws IOException {
                if (updateWordCount > tupleCount)
                    return;
                     
                last.word = input.readBytes();
                updateWordCount = tupleCount + input.readInt();
                                      
                buffer.processWord(last.word);
            }
            public final void updateProbability() throws IOException {
                if (updateProbabilityCount > tupleCount)
                    return;
                     
                updateWord();
                last.probability = input.readDouble();
                updateProbabilityCount = tupleCount + input.readInt();
                                      
                buffer.processProbability(last.probability);
            }
            public final void updateDocument() throws IOException {
                if (updateDocumentCount > tupleCount)
                    return;
                     
                updateProbability();
                last.document = input.readInt();
                updateDocumentCount = tupleCount + input.readInt();
                                      
                buffer.processDocument(last.document);
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
                } else if (processor instanceof TopDocsEntry.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((TopDocsEntry.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<TopDocsEntry>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<TopDocsEntry> getOutputClass() {
                return TopDocsEntry.class;
            }                
        }
        
        public static class DuplicateEliminator implements ShreddedProcessor {
            public ShreddedProcessor processor;
            TopDocsEntry last = new TopDocsEntry();
            boolean wordProcess = true;
            boolean probabilityProcess = true;
            boolean documentProcess = true;
                                           
            public DuplicateEliminator() {}
            public DuplicateEliminator(ShreddedProcessor processor) {
                this.processor = processor;
            }
            
            public void setShreddedProcessor(ShreddedProcessor processor) {
                this.processor = processor;
            }

            public void processWord(byte[] word) throws IOException {  
                if (wordProcess || Utility.compare(word, last.word) != 0) {
                    last.word = word;
                    processor.processWord(word);
            resetProbability();
                    wordProcess = false;
                }
            }
            public void processProbability(double probability) throws IOException {  
                if (probabilityProcess || Utility.compare(probability, last.probability) != 0) {
                    last.probability = probability;
                    processor.processProbability(probability);
            resetDocument();
                    probabilityProcess = false;
                }
            }
            public void processDocument(int document) throws IOException {  
                if (documentProcess || Utility.compare(document, last.document) != 0) {
                    last.document = document;
                    processor.processDocument(document);
                    documentProcess = false;
                }
            }  
            
            public void resetWord() {
                 wordProcess = true;
            resetProbability();
            }                                                
            public void resetProbability() {
                 probabilityProcess = true;
            resetDocument();
            }                                                
            public void resetDocument() {
                 documentProcess = true;
            }                                                
                               
            public void processTuple(int count, int doclength) throws IOException {
                processor.processTuple(count, doclength);
            } 
            
            public void close() throws IOException {
                processor.close();
            }                    
        }
        public static class TupleUnshredder implements ShreddedProcessor {
            TopDocsEntry last = new TopDocsEntry();
            public org.lemurproject.galago.tupleflow.Processor<TopDocsEntry> processor;                               
            
            public TupleUnshredder(TopDocsEntry.Processor processor) {
                this.processor = processor;
            }         
            
            public TupleUnshredder(org.lemurproject.galago.tupleflow.Processor<TopDocsEntry> processor) {
                this.processor = processor;
            }
            
            public TopDocsEntry clone(TopDocsEntry object) {
                TopDocsEntry result = new TopDocsEntry();
                if (object == null) return result;
                result.document = object.document; 
                result.word = object.word; 
                result.probability = object.probability; 
                result.count = object.count; 
                result.doclength = object.doclength; 
                return result;
            }                 
            
            public void processWord(byte[] word) throws IOException {
                last.word = word;
            }   
                
            public void processProbability(double probability) throws IOException {
                last.probability = probability;
            }   
                
            public void processDocument(int document) throws IOException {
                last.document = document;
            }   
                
            
            public void processTuple(int count, int doclength) throws IOException {
                last.count = count;
                last.doclength = doclength;
                processor.process(clone(last));
            }               
            
            public void close() throws IOException {
                processor.close();
            }
        }     
        public static class TupleShredder implements Processor {
            TopDocsEntry last = null;
            public ShreddedProcessor processor;
            
            public TupleShredder(ShreddedProcessor processor) {
                this.processor = processor;
            }                              
            
            public TopDocsEntry clone(TopDocsEntry object) {
                TopDocsEntry result = new TopDocsEntry();
                if (object == null) return result;
                result.document = object.document; 
                result.word = object.word; 
                result.probability = object.probability; 
                result.count = object.count; 
                result.doclength = object.doclength; 
                return result;
            }                 
            
            public void process(TopDocsEntry object) throws IOException {                                                                                                                                                   
                boolean processAll = false;
                if(last == null || Utility.compare(last.word, object.word) != 0 || processAll) { processor.processWord(object.word); processAll = true; }
                if(last == null || Utility.compare(last.probability, object.probability) != 0 || processAll) { processor.processProbability(object.probability); processAll = true; }
                if(last == null || Utility.compare(last.document, object.document) != 0 || processAll) { processor.processDocument(object.document); processAll = true; }
                processor.processTuple(object.count, object.doclength);                                         
                last = object;
            }
                          
            public Class<TopDocsEntry> getInputClass() {
                return TopDocsEntry.class;
            }
            
            public void close() throws IOException {
                processor.close();
            }                     
        }
    } 
    public static class WordDocumentOrder implements Order<TopDocsEntry> {
        public int hash(TopDocsEntry object) {
            int h = 0;
            h += Utility.hash(object.word);
            h += Utility.hash(object.document);
            return h;
        } 
        public Comparator<TopDocsEntry> greaterThan() {
            return new Comparator<TopDocsEntry>() {
                public int compare(TopDocsEntry one, TopDocsEntry two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.word, two.word);
                        if(result != 0) break;
                        result = + Utility.compare(one.document, two.document);
                        if(result != 0) break;
                    } while (false);
                    return -result;
                }
            };
        }     
        public Comparator<TopDocsEntry> lessThan() {
            return new Comparator<TopDocsEntry>() {
                public int compare(TopDocsEntry one, TopDocsEntry two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.word, two.word);
                        if(result != 0) break;
                        result = + Utility.compare(one.document, two.document);
                        if(result != 0) break;
                    } while (false);
                    return result;
                }
            };
        }     
        public TypeReader<TopDocsEntry> orderedReader(ArrayInput _input) {
            return new ShreddedReader(_input);
        }    

        public TypeReader<TopDocsEntry> orderedReader(ArrayInput _input, int bufferSize) {
            return new ShreddedReader(_input, bufferSize);
        }    
        public OrderedWriter<TopDocsEntry> orderedWriter(ArrayOutput _output) {
            ShreddedWriter w = new ShreddedWriter(_output);
            return new OrderedWriterClass(w); 
        }                                    
        public static class OrderedWriterClass extends OrderedWriter< TopDocsEntry > {
            TopDocsEntry last = null;
            ShreddedWriter shreddedWriter = null; 
            
            public OrderedWriterClass(ShreddedWriter s) {
                this.shreddedWriter = s;
            }
            
            public void process(TopDocsEntry object) throws IOException {
               boolean processAll = false;
               if (processAll || last == null || 0 != Utility.compare(object.word, last.word)) { processAll = true; shreddedWriter.processWord(object.word); }
               if (processAll || last == null || 0 != Utility.compare(object.document, last.document)) { processAll = true; shreddedWriter.processDocument(object.document); }
               shreddedWriter.processTuple(object.probability, object.count, object.doclength);
               last = object;
            }           
                 
            public void close() throws IOException {
                shreddedWriter.close();
            }
            
            public Class<TopDocsEntry> getInputClass() {
                return TopDocsEntry.class;
            }
        } 
        public ReaderSource<TopDocsEntry> orderedCombiner(Collection<TypeReader<TopDocsEntry>> readers, boolean closeOnExit) {
            ArrayList<ShreddedReader> shreddedReaders = new ArrayList();
            
            for (TypeReader<TopDocsEntry> reader : readers) {
                shreddedReaders.add((ShreddedReader)reader);
            }
            
            return new ShreddedCombiner(shreddedReaders, closeOnExit);
        }                  
        public TopDocsEntry clone(TopDocsEntry object) {
            TopDocsEntry result = new TopDocsEntry();
            if (object == null) return result;
            result.document = object.document; 
            result.word = object.word; 
            result.probability = object.probability; 
            result.count = object.count; 
            result.doclength = object.doclength; 
            return result;
        }                 
        public Class<TopDocsEntry> getOrderedClass() {
            return TopDocsEntry.class;
        }                           
        public String[] getOrderSpec() {
            return new String[] {"+word", "+document"};
        }

        public static String[] getSpec() {
            return new String[] {"+word", "+document"};
        }
        public static String getSpecString() {
            return "+word +document";
        }
                           
        public interface ShreddedProcessor extends Step {
            public void processWord(byte[] word) throws IOException;
            public void processDocument(int document) throws IOException;
            public void processTuple(double probability, int count, int doclength) throws IOException;
            public void close() throws IOException;
        }    
        public interface ShreddedSource extends Step {
        }                                              
        
        public static class ShreddedWriter implements ShreddedProcessor {
            ArrayOutput output;
            ShreddedBuffer buffer = new ShreddedBuffer();
            byte[] lastWord;
            int lastDocument;
            boolean lastFlush = false;
            
            public ShreddedWriter(ArrayOutput output) {
                this.output = output;
            }                        
            
            public void close() throws IOException {
                flush();
            }
            
            public void processWord(byte[] word) {
                lastWord = word;
                buffer.processWord(word);
            }
            public void processDocument(int document) {
                lastDocument = document;
                buffer.processDocument(document);
            }
            public final void processTuple(double probability, int count, int doclength) throws IOException {
                if (lastFlush) {
                    if(buffer.words.size() == 0) buffer.processWord(lastWord);
                    if(buffer.documents.size() == 0) buffer.processDocument(lastDocument);
                    lastFlush = false;
                }
                buffer.processTuple(probability, count, doclength);
                if (buffer.isFull())
                    flush();
            }
            public final void flushTuples(int pauseIndex) throws IOException {
                
                while (buffer.getReadIndex() < pauseIndex) {
                           
                    output.writeDouble(buffer.getProbability());
                    output.writeInt(buffer.getCount());
                    output.writeInt(buffer.getDoclength());
                    buffer.incrementTuple();
                }
            }  
            public final void flushWord(int pauseIndex) throws IOException {
                while (buffer.getReadIndex() < pauseIndex) {
                    int nextPause = buffer.getWordEndIndex();
                    int count = nextPause - buffer.getReadIndex();
                    
                    output.writeBytes(buffer.getWord());
                    output.writeInt(count);
                    buffer.incrementWord();
                      
                    flushDocument(nextPause);
                    assert nextPause == buffer.getReadIndex();
                }
            }
            public final void flushDocument(int pauseIndex) throws IOException {
                while (buffer.getReadIndex() < pauseIndex) {
                    int nextPause = buffer.getDocumentEndIndex();
                    int count = nextPause - buffer.getReadIndex();
                    
                    output.writeInt(buffer.getDocument());
                    output.writeInt(count);
                    buffer.incrementDocument();
                      
                    flushTuples(nextPause);
                    assert nextPause == buffer.getReadIndex();
                }
            }
            public void flush() throws IOException { 
                flushWord(buffer.getWriteIndex());
                buffer.reset(); 
                lastFlush = true;
            }                           
        }
        public static class ShreddedBuffer {
            ArrayList<byte[]> words = new ArrayList();
            TIntArrayList documents = new TIntArrayList();
            TIntArrayList wordTupleIdx = new TIntArrayList();
            TIntArrayList documentTupleIdx = new TIntArrayList();
            int wordReadIdx = 0;
            int documentReadIdx = 0;
                            
            double[] probabilitys;
            int[] counts;
            int[] doclengths;
            int writeTupleIndex = 0;
            int readTupleIndex = 0;
            int batchSize;

            public ShreddedBuffer(int batchSize) {
                this.batchSize = batchSize;

                probabilitys = new double[batchSize];
                counts = new int[batchSize];
                doclengths = new int[batchSize];
            }                              

            public ShreddedBuffer() {    
                this(10000);
            }                                                                                                                    
            
            public void processWord(byte[] word) {
                words.add(word);
                wordTupleIdx.add(writeTupleIndex);
            }                                      
            public void processDocument(int document) {
                documents.add(document);
                documentTupleIdx.add(writeTupleIndex);
            }                                      
            public void processTuple(double probability, int count, int doclength) {
                assert words.size() > 0;
                assert documents.size() > 0;
                probabilitys[writeTupleIndex] = probability;
                counts[writeTupleIndex] = count;
                doclengths[writeTupleIndex] = doclength;
                writeTupleIndex++;
            }
            public void resetData() {
                words.clear();
                documents.clear();
                wordTupleIdx.clear();
                documentTupleIdx.clear();
                writeTupleIndex = 0;
            }                  
                                 
            public void resetRead() {
                readTupleIndex = 0;
                wordReadIdx = 0;
                documentReadIdx = 0;
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
            public void incrementWord() {
                wordReadIdx++;  
            }                                                                                              

            public void autoIncrementWord() {
                while (readTupleIndex >= getWordEndIndex() && readTupleIndex < writeTupleIndex)
                    wordReadIdx++;
            }                 
            public void incrementDocument() {
                documentReadIdx++;  
            }                                                                                              

            public void autoIncrementDocument() {
                while (readTupleIndex >= getDocumentEndIndex() && readTupleIndex < writeTupleIndex)
                    documentReadIdx++;
            }                 
            public void incrementTuple() {
                readTupleIndex++;
            }                    
            public int getWordEndIndex() {
                if ((wordReadIdx+1) >= wordTupleIdx.size())
                    return writeTupleIndex;
                return wordTupleIdx.get(wordReadIdx+1);
            }

            public int getDocumentEndIndex() {
                if ((documentReadIdx+1) >= documentTupleIdx.size())
                    return writeTupleIndex;
                return documentTupleIdx.get(documentReadIdx+1);
            }
            public int getReadIndex() {
                return readTupleIndex;
            }   

            public int getWriteIndex() {
                return writeTupleIndex;
            } 
            public byte[] getWord() {
                assert readTupleIndex < writeTupleIndex;
                assert wordReadIdx < words.size();
                
                return words.get(wordReadIdx);
            }
            public int getDocument() {
                assert readTupleIndex < writeTupleIndex;
                assert documentReadIdx < documents.size();
                
                return documents.get(documentReadIdx);
            }
            public double getProbability() {
                assert readTupleIndex < writeTupleIndex;
                return probabilitys[readTupleIndex];
            }                                         
            public int getCount() {
                assert readTupleIndex < writeTupleIndex;
                return counts[readTupleIndex];
            }                                         
            public int getDoclength() {
                assert readTupleIndex < writeTupleIndex;
                return doclengths[readTupleIndex];
            }                                         
            public void copyTuples(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                   output.processTuple(getProbability(), getCount(), getDoclength());
                   incrementTuple();
                }
            }                                                                           
            public void copyUntilIndexWord(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                    output.processWord(getWord());
                    assert getWordEndIndex() <= endIndex;
                    copyUntilIndexDocument(getWordEndIndex(), output);
                    incrementWord();
                }
            } 
            public void copyUntilIndexDocument(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                    output.processDocument(getDocument());
                    assert getDocumentEndIndex() <= endIndex;
                    copyTuples(getDocumentEndIndex(), output);
                    incrementDocument();
                }
            }  
            public void copyUntilWord(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                while (!isAtEnd()) {
                    if (other != null) {   
                        assert !other.isAtEnd();
                        int c = + Utility.compare(getWord(), other.getWord());
                    
                        if (c > 0) {
                            break;   
                        }
                        
                        output.processWord(getWord());
                                      
                        if (c < 0) {
                            copyUntilIndexDocument(getWordEndIndex(), output);
                        } else if (c == 0) {
                            copyUntilDocument(other, output);
                            autoIncrementWord();
                            break;
                        }
                    } else {
                        output.processWord(getWord());
                        copyUntilIndexDocument(getWordEndIndex(), output);
                    }
                    incrementWord();  
                    
               
                }
            }
            public void copyUntilDocument(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                while (!isAtEnd()) {
                    if (other != null) {   
                        assert !other.isAtEnd();
                        int c = + Utility.compare(getDocument(), other.getDocument());
                    
                        if (c > 0) {
                            break;   
                        }
                        
                        output.processDocument(getDocument());
                                      
                        copyTuples(getDocumentEndIndex(), output);
                    } else {
                        output.processDocument(getDocument());
                        copyTuples(getDocumentEndIndex(), output);
                    }
                    incrementDocument();  
                    
                    if (getWordEndIndex() <= readTupleIndex)
                        break;   
                }
            }
            public void copyUntil(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                copyUntilWord(other, output);
            }
            
        }                         
        public static class ShreddedCombiner implements ReaderSource<TopDocsEntry>, ShreddedSource {   
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
                } else if (processor instanceof TopDocsEntry.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((TopDocsEntry.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<TopDocsEntry>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<TopDocsEntry> getOutputClass() {
                return TopDocsEntry.class;
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

            public TopDocsEntry read() throws IOException {
                if (uninitialized)
                    initialize();

                TopDocsEntry result = null;

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
        public static class ShreddedReader implements Step, Comparable<ShreddedReader>, TypeReader<TopDocsEntry>, ShreddedSource {      
            public ShreddedProcessor processor;
            ShreddedBuffer buffer;
            TopDocsEntry last = new TopDocsEntry();         
            long updateWordCount = -1;
            long updateDocumentCount = -1;
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
                    result = + Utility.compare(buffer.getWord(), otherBuffer.getWord());
                    if(result != 0) break;
                    result = + Utility.compare(buffer.getDocument(), otherBuffer.getDocument());
                    if(result != 0) break;
                } while (false);                                             
                
                return result;
            }
            
            public final ShreddedBuffer getBuffer() {
                return buffer;
            }                
            
            public final TopDocsEntry read() throws IOException {
                if (buffer.isAtEnd()) {
                    fill();             
                
                    if (buffer.isAtEnd()) {
                        return null;
                    }
                }
                      
                assert !buffer.isAtEnd();
                TopDocsEntry result = new TopDocsEntry();
                
                result.word = buffer.getWord();
                result.document = buffer.getDocument();
                result.probability = buffer.getProbability();
                result.count = buffer.getCount();
                result.doclength = buffer.getDoclength();
                
                buffer.incrementTuple();
                buffer.autoIncrementWord();
                buffer.autoIncrementDocument();
                
                return result;
            }           
            
            public final void fill() throws IOException {
                try {   
                    buffer.reset();
                    
                    if (tupleCount != 0) {
                                                      
                        if(updateWordCount - tupleCount > 0) {
                            buffer.words.add(last.word);
                            buffer.wordTupleIdx.add((int) (updateWordCount - tupleCount));
                        }                              
                        if(updateDocumentCount - tupleCount > 0) {
                            buffer.documents.add(last.document);
                            buffer.documentTupleIdx.add((int) (updateDocumentCount - tupleCount));
                        }
                        bufferStartCount = tupleCount;
                    }
                    
                    while (!buffer.isFull()) {
                        updateDocument();
                        buffer.processTuple(input.readDouble(), input.readInt(), input.readInt());
                        tupleCount++;
                    }
                } catch(EOFException e) {}
            }

            public final void updateWord() throws IOException {
                if (updateWordCount > tupleCount)
                    return;
                     
                last.word = input.readBytes();
                updateWordCount = tupleCount + input.readInt();
                                      
                buffer.processWord(last.word);
            }
            public final void updateDocument() throws IOException {
                if (updateDocumentCount > tupleCount)
                    return;
                     
                updateWord();
                last.document = input.readInt();
                updateDocumentCount = tupleCount + input.readInt();
                                      
                buffer.processDocument(last.document);
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
                } else if (processor instanceof TopDocsEntry.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((TopDocsEntry.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<TopDocsEntry>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<TopDocsEntry> getOutputClass() {
                return TopDocsEntry.class;
            }                
        }
        
        public static class DuplicateEliminator implements ShreddedProcessor {
            public ShreddedProcessor processor;
            TopDocsEntry last = new TopDocsEntry();
            boolean wordProcess = true;
            boolean documentProcess = true;
                                           
            public DuplicateEliminator() {}
            public DuplicateEliminator(ShreddedProcessor processor) {
                this.processor = processor;
            }
            
            public void setShreddedProcessor(ShreddedProcessor processor) {
                this.processor = processor;
            }

            public void processWord(byte[] word) throws IOException {  
                if (wordProcess || Utility.compare(word, last.word) != 0) {
                    last.word = word;
                    processor.processWord(word);
            resetDocument();
                    wordProcess = false;
                }
            }
            public void processDocument(int document) throws IOException {  
                if (documentProcess || Utility.compare(document, last.document) != 0) {
                    last.document = document;
                    processor.processDocument(document);
                    documentProcess = false;
                }
            }  
            
            public void resetWord() {
                 wordProcess = true;
            resetDocument();
            }                                                
            public void resetDocument() {
                 documentProcess = true;
            }                                                
                               
            public void processTuple(double probability, int count, int doclength) throws IOException {
                processor.processTuple(probability, count, doclength);
            } 
            
            public void close() throws IOException {
                processor.close();
            }                    
        }
        public static class TupleUnshredder implements ShreddedProcessor {
            TopDocsEntry last = new TopDocsEntry();
            public org.lemurproject.galago.tupleflow.Processor<TopDocsEntry> processor;                               
            
            public TupleUnshredder(TopDocsEntry.Processor processor) {
                this.processor = processor;
            }         
            
            public TupleUnshredder(org.lemurproject.galago.tupleflow.Processor<TopDocsEntry> processor) {
                this.processor = processor;
            }
            
            public TopDocsEntry clone(TopDocsEntry object) {
                TopDocsEntry result = new TopDocsEntry();
                if (object == null) return result;
                result.document = object.document; 
                result.word = object.word; 
                result.probability = object.probability; 
                result.count = object.count; 
                result.doclength = object.doclength; 
                return result;
            }                 
            
            public void processWord(byte[] word) throws IOException {
                last.word = word;
            }   
                
            public void processDocument(int document) throws IOException {
                last.document = document;
            }   
                
            
            public void processTuple(double probability, int count, int doclength) throws IOException {
                last.probability = probability;
                last.count = count;
                last.doclength = doclength;
                processor.process(clone(last));
            }               
            
            public void close() throws IOException {
                processor.close();
            }
        }     
        public static class TupleShredder implements Processor {
            TopDocsEntry last = null;
            public ShreddedProcessor processor;
            
            public TupleShredder(ShreddedProcessor processor) {
                this.processor = processor;
            }                              
            
            public TopDocsEntry clone(TopDocsEntry object) {
                TopDocsEntry result = new TopDocsEntry();
                if (object == null) return result;
                result.document = object.document; 
                result.word = object.word; 
                result.probability = object.probability; 
                result.count = object.count; 
                result.doclength = object.doclength; 
                return result;
            }                 
            
            public void process(TopDocsEntry object) throws IOException {                                                                                                                                                   
                boolean processAll = false;
                if(last == null || Utility.compare(last.word, object.word) != 0 || processAll) { processor.processWord(object.word); processAll = true; }
                if(last == null || Utility.compare(last.document, object.document) != 0 || processAll) { processor.processDocument(object.document); processAll = true; }
                processor.processTuple(object.probability, object.count, object.doclength);                                         
                last = object;
            }
                          
            public Class<TopDocsEntry> getInputClass() {
                return TopDocsEntry.class;
            }
            
            public void close() throws IOException {
                processor.close();
            }                     
        }
    } 
}    