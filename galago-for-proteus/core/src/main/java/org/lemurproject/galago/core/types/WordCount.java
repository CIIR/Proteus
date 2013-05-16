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


public class WordCount implements Type<WordCount> {
    public byte[] word;
    public long count;
    public long documents; 
    
    public WordCount() {}
    public WordCount(byte[] word, long count, long documents) {
        this.word = word;
        this.count = count;
        this.documents = documents;
    }  
    
    public String toString() {
        try {
            return String.format("%s,%d,%d",
                                   new String(word, "UTF-8"), count, documents);
        } catch(UnsupportedEncodingException e) {
            throw new RuntimeException("Couldn't convert string to UTF-8.");
        }
    } 

    public Order<WordCount> getOrder(String... spec) {
        if (Arrays.equals(spec, new String[] { "+word" })) {
            return new WordOrder();
        }
        if (Arrays.equals(spec, new String[] { "+count" })) {
            return new CountOrder();
        }
        if (Arrays.equals(spec, new String[] { "-count" })) {
            return new DescCountOrder();
        }
        return null;
    } 
      
    public interface Processor extends Step, org.lemurproject.galago.tupleflow.Processor<WordCount> {
        public void process(WordCount object) throws IOException;
        public void close() throws IOException;
    }                        
    public interface Source extends Step {
    }
    public static class WordOrder implements Order<WordCount> {
        public int hash(WordCount object) {
            int h = 0;
            h += Utility.hash(object.word);
            return h;
        } 
        public Comparator<WordCount> greaterThan() {
            return new Comparator<WordCount>() {
                public int compare(WordCount one, WordCount two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.word, two.word);
                        if(result != 0) break;
                    } while (false);
                    return -result;
                }
            };
        }     
        public Comparator<WordCount> lessThan() {
            return new Comparator<WordCount>() {
                public int compare(WordCount one, WordCount two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.word, two.word);
                        if(result != 0) break;
                    } while (false);
                    return result;
                }
            };
        }     
        public TypeReader<WordCount> orderedReader(ArrayInput _input) {
            return new ShreddedReader(_input);
        }    

        public TypeReader<WordCount> orderedReader(ArrayInput _input, int bufferSize) {
            return new ShreddedReader(_input, bufferSize);
        }    
        public OrderedWriter<WordCount> orderedWriter(ArrayOutput _output) {
            ShreddedWriter w = new ShreddedWriter(_output);
            return new OrderedWriterClass(w); 
        }                                    
        public static class OrderedWriterClass extends OrderedWriter< WordCount > {
            WordCount last = null;
            ShreddedWriter shreddedWriter = null; 
            
            public OrderedWriterClass(ShreddedWriter s) {
                this.shreddedWriter = s;
            }
            
            public void process(WordCount object) throws IOException {
               boolean processAll = false;
               if (processAll || last == null || 0 != Utility.compare(object.word, last.word)) { processAll = true; shreddedWriter.processWord(object.word); }
               shreddedWriter.processTuple(object.count, object.documents);
               last = object;
            }           
                 
            public void close() throws IOException {
                shreddedWriter.close();
            }
            
            public Class<WordCount> getInputClass() {
                return WordCount.class;
            }
        } 
        public ReaderSource<WordCount> orderedCombiner(Collection<TypeReader<WordCount>> readers, boolean closeOnExit) {
            ArrayList<ShreddedReader> shreddedReaders = new ArrayList();
            
            for (TypeReader<WordCount> reader : readers) {
                shreddedReaders.add((ShreddedReader)reader);
            }
            
            return new ShreddedCombiner(shreddedReaders, closeOnExit);
        }                  
        public WordCount clone(WordCount object) {
            WordCount result = new WordCount();
            if (object == null) return result;
            result.word = object.word; 
            result.count = object.count; 
            result.documents = object.documents; 
            return result;
        }                 
        public Class<WordCount> getOrderedClass() {
            return WordCount.class;
        }                           
        public String[] getOrderSpec() {
            return new String[] {"+word"};
        }

        public static String[] getSpec() {
            return new String[] {"+word"};
        }
        public static String getSpecString() {
            return "+word";
        }
                           
        public interface ShreddedProcessor extends Step {
            public void processWord(byte[] word) throws IOException;
            public void processTuple(long count, long documents) throws IOException;
            public void close() throws IOException;
        }    
        public interface ShreddedSource extends Step {
        }                                              
        
        public static class ShreddedWriter implements ShreddedProcessor {
            ArrayOutput output;
            ShreddedBuffer buffer = new ShreddedBuffer();
            byte[] lastWord;
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
            public final void processTuple(long count, long documents) throws IOException {
                if (lastFlush) {
                    if(buffer.words.size() == 0) buffer.processWord(lastWord);
                    lastFlush = false;
                }
                buffer.processTuple(count, documents);
                if (buffer.isFull())
                    flush();
            }
            public final void flushTuples(int pauseIndex) throws IOException {
                
                while (buffer.getReadIndex() < pauseIndex) {
                           
                    output.writeLong(buffer.getCount());
                    output.writeLong(buffer.getDocuments());
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
            TIntArrayList wordTupleIdx = new TIntArrayList();
            int wordReadIdx = 0;
                            
            long[] counts;
            long[] documentss;
            int writeTupleIndex = 0;
            int readTupleIndex = 0;
            int batchSize;

            public ShreddedBuffer(int batchSize) {
                this.batchSize = batchSize;

                counts = new long[batchSize];
                documentss = new long[batchSize];
            }                              

            public ShreddedBuffer() {    
                this(10000);
            }                                                                                                                    
            
            public void processWord(byte[] word) {
                words.add(word);
                wordTupleIdx.add(writeTupleIndex);
            }                                      
            public void processTuple(long count, long documents) {
                assert words.size() > 0;
                counts[writeTupleIndex] = count;
                documentss[writeTupleIndex] = documents;
                writeTupleIndex++;
            }
            public void resetData() {
                words.clear();
                wordTupleIdx.clear();
                writeTupleIndex = 0;
            }                  
                                 
            public void resetRead() {
                readTupleIndex = 0;
                wordReadIdx = 0;
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
            public void incrementTuple() {
                readTupleIndex++;
            }                    
            public int getWordEndIndex() {
                if ((wordReadIdx+1) >= wordTupleIdx.size())
                    return writeTupleIndex;
                return wordTupleIdx.get(wordReadIdx+1);
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
            public long getCount() {
                assert readTupleIndex < writeTupleIndex;
                return counts[readTupleIndex];
            }                                         
            public long getDocuments() {
                assert readTupleIndex < writeTupleIndex;
                return documentss[readTupleIndex];
            }                                         
            public void copyTuples(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                   output.processTuple(getCount(), getDocuments());
                   incrementTuple();
                }
            }                                                                           
            public void copyUntilIndexWord(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                    output.processWord(getWord());
                    assert getWordEndIndex() <= endIndex;
                    copyTuples(getWordEndIndex(), output);
                    incrementWord();
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
                                      
                        copyTuples(getWordEndIndex(), output);
                    } else {
                        output.processWord(getWord());
                        copyTuples(getWordEndIndex(), output);
                    }
                    incrementWord();  
                    
               
                }
            }
            public void copyUntil(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                copyUntilWord(other, output);
            }
            
        }                         
        public static class ShreddedCombiner implements ReaderSource<WordCount>, ShreddedSource {   
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
                } else if (processor instanceof WordCount.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((WordCount.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<WordCount>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<WordCount> getOutputClass() {
                return WordCount.class;
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

            public WordCount read() throws IOException {
                if (uninitialized)
                    initialize();

                WordCount result = null;

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
        public static class ShreddedReader implements Step, Comparable<ShreddedReader>, TypeReader<WordCount>, ShreddedSource {      
            public ShreddedProcessor processor;
            ShreddedBuffer buffer;
            WordCount last = new WordCount();         
            long updateWordCount = -1;
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
                } while (false);                                             
                
                return result;
            }
            
            public final ShreddedBuffer getBuffer() {
                return buffer;
            }                
            
            public final WordCount read() throws IOException {
                if (buffer.isAtEnd()) {
                    fill();             
                
                    if (buffer.isAtEnd()) {
                        return null;
                    }
                }
                      
                assert !buffer.isAtEnd();
                WordCount result = new WordCount();
                
                result.word = buffer.getWord();
                result.count = buffer.getCount();
                result.documents = buffer.getDocuments();
                
                buffer.incrementTuple();
                buffer.autoIncrementWord();
                
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
                        bufferStartCount = tupleCount;
                    }
                    
                    while (!buffer.isFull()) {
                        updateWord();
                        buffer.processTuple(input.readLong(), input.readLong());
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
                } else if (processor instanceof WordCount.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((WordCount.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<WordCount>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<WordCount> getOutputClass() {
                return WordCount.class;
            }                
        }
        
        public static class DuplicateEliminator implements ShreddedProcessor {
            public ShreddedProcessor processor;
            WordCount last = new WordCount();
            boolean wordProcess = true;
                                           
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
                    wordProcess = false;
                }
            }  
            
            public void resetWord() {
                 wordProcess = true;
            }                                                
                               
            public void processTuple(long count, long documents) throws IOException {
                processor.processTuple(count, documents);
            } 
            
            public void close() throws IOException {
                processor.close();
            }                    
        }
        public static class TupleUnshredder implements ShreddedProcessor {
            WordCount last = new WordCount();
            public org.lemurproject.galago.tupleflow.Processor<WordCount> processor;                               
            
            public TupleUnshredder(WordCount.Processor processor) {
                this.processor = processor;
            }         
            
            public TupleUnshredder(org.lemurproject.galago.tupleflow.Processor<WordCount> processor) {
                this.processor = processor;
            }
            
            public WordCount clone(WordCount object) {
                WordCount result = new WordCount();
                if (object == null) return result;
                result.word = object.word; 
                result.count = object.count; 
                result.documents = object.documents; 
                return result;
            }                 
            
            public void processWord(byte[] word) throws IOException {
                last.word = word;
            }   
                
            
            public void processTuple(long count, long documents) throws IOException {
                last.count = count;
                last.documents = documents;
                processor.process(clone(last));
            }               
            
            public void close() throws IOException {
                processor.close();
            }
        }     
        public static class TupleShredder implements Processor {
            WordCount last = null;
            public ShreddedProcessor processor;
            
            public TupleShredder(ShreddedProcessor processor) {
                this.processor = processor;
            }                              
            
            public WordCount clone(WordCount object) {
                WordCount result = new WordCount();
                if (object == null) return result;
                result.word = object.word; 
                result.count = object.count; 
                result.documents = object.documents; 
                return result;
            }                 
            
            public void process(WordCount object) throws IOException {                                                                                                                                                   
                boolean processAll = false;
                if(last == null || Utility.compare(last.word, object.word) != 0 || processAll) { processor.processWord(object.word); processAll = true; }
                processor.processTuple(object.count, object.documents);                                         
                last = object;
            }
                          
            public Class<WordCount> getInputClass() {
                return WordCount.class;
            }
            
            public void close() throws IOException {
                processor.close();
            }                     
        }
    } 
    public static class CountOrder implements Order<WordCount> {
        public int hash(WordCount object) {
            int h = 0;
            h += Utility.hash(object.count);
            return h;
        } 
        public Comparator<WordCount> greaterThan() {
            return new Comparator<WordCount>() {
                public int compare(WordCount one, WordCount two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.count, two.count);
                        if(result != 0) break;
                    } while (false);
                    return -result;
                }
            };
        }     
        public Comparator<WordCount> lessThan() {
            return new Comparator<WordCount>() {
                public int compare(WordCount one, WordCount two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.count, two.count);
                        if(result != 0) break;
                    } while (false);
                    return result;
                }
            };
        }     
        public TypeReader<WordCount> orderedReader(ArrayInput _input) {
            return new ShreddedReader(_input);
        }    

        public TypeReader<WordCount> orderedReader(ArrayInput _input, int bufferSize) {
            return new ShreddedReader(_input, bufferSize);
        }    
        public OrderedWriter<WordCount> orderedWriter(ArrayOutput _output) {
            ShreddedWriter w = new ShreddedWriter(_output);
            return new OrderedWriterClass(w); 
        }                                    
        public static class OrderedWriterClass extends OrderedWriter< WordCount > {
            WordCount last = null;
            ShreddedWriter shreddedWriter = null; 
            
            public OrderedWriterClass(ShreddedWriter s) {
                this.shreddedWriter = s;
            }
            
            public void process(WordCount object) throws IOException {
               boolean processAll = false;
               if (processAll || last == null || 0 != Utility.compare(object.count, last.count)) { processAll = true; shreddedWriter.processCount(object.count); }
               shreddedWriter.processTuple(object.word, object.documents);
               last = object;
            }           
                 
            public void close() throws IOException {
                shreddedWriter.close();
            }
            
            public Class<WordCount> getInputClass() {
                return WordCount.class;
            }
        } 
        public ReaderSource<WordCount> orderedCombiner(Collection<TypeReader<WordCount>> readers, boolean closeOnExit) {
            ArrayList<ShreddedReader> shreddedReaders = new ArrayList();
            
            for (TypeReader<WordCount> reader : readers) {
                shreddedReaders.add((ShreddedReader)reader);
            }
            
            return new ShreddedCombiner(shreddedReaders, closeOnExit);
        }                  
        public WordCount clone(WordCount object) {
            WordCount result = new WordCount();
            if (object == null) return result;
            result.word = object.word; 
            result.count = object.count; 
            result.documents = object.documents; 
            return result;
        }                 
        public Class<WordCount> getOrderedClass() {
            return WordCount.class;
        }                           
        public String[] getOrderSpec() {
            return new String[] {"+count"};
        }

        public static String[] getSpec() {
            return new String[] {"+count"};
        }
        public static String getSpecString() {
            return "+count";
        }
                           
        public interface ShreddedProcessor extends Step {
            public void processCount(long count) throws IOException;
            public void processTuple(byte[] word, long documents) throws IOException;
            public void close() throws IOException;
        }    
        public interface ShreddedSource extends Step {
        }                                              
        
        public static class ShreddedWriter implements ShreddedProcessor {
            ArrayOutput output;
            ShreddedBuffer buffer = new ShreddedBuffer();
            long lastCount;
            boolean lastFlush = false;
            
            public ShreddedWriter(ArrayOutput output) {
                this.output = output;
            }                        
            
            public void close() throws IOException {
                flush();
            }
            
            public void processCount(long count) {
                lastCount = count;
                buffer.processCount(count);
            }
            public final void processTuple(byte[] word, long documents) throws IOException {
                if (lastFlush) {
                    if(buffer.counts.size() == 0) buffer.processCount(lastCount);
                    lastFlush = false;
                }
                buffer.processTuple(word, documents);
                if (buffer.isFull())
                    flush();
            }
            public final void flushTuples(int pauseIndex) throws IOException {
                
                while (buffer.getReadIndex() < pauseIndex) {
                           
                    output.writeBytes(buffer.getWord());
                    output.writeLong(buffer.getDocuments());
                    buffer.incrementTuple();
                }
            }  
            public final void flushCount(int pauseIndex) throws IOException {
                while (buffer.getReadIndex() < pauseIndex) {
                    int nextPause = buffer.getCountEndIndex();
                    int count = nextPause - buffer.getReadIndex();
                    
                    output.writeLong(buffer.getCount());
                    output.writeInt(count);
                    buffer.incrementCount();
                      
                    flushTuples(nextPause);
                    assert nextPause == buffer.getReadIndex();
                }
            }
            public void flush() throws IOException { 
                flushCount(buffer.getWriteIndex());
                buffer.reset(); 
                lastFlush = true;
            }                           
        }
        public static class ShreddedBuffer {
            TLongArrayList counts = new TLongArrayList();
            TIntArrayList countTupleIdx = new TIntArrayList();
            int countReadIdx = 0;
                            
            byte[][] words;
            long[] documentss;
            int writeTupleIndex = 0;
            int readTupleIndex = 0;
            int batchSize;

            public ShreddedBuffer(int batchSize) {
                this.batchSize = batchSize;

                words = new byte[batchSize][];
                documentss = new long[batchSize];
            }                              

            public ShreddedBuffer() {    
                this(10000);
            }                                                                                                                    
            
            public void processCount(long count) {
                counts.add(count);
                countTupleIdx.add(writeTupleIndex);
            }                                      
            public void processTuple(byte[] word, long documents) {
                assert counts.size() > 0;
                words[writeTupleIndex] = word;
                documentss[writeTupleIndex] = documents;
                writeTupleIndex++;
            }
            public void resetData() {
                counts.clear();
                countTupleIdx.clear();
                writeTupleIndex = 0;
            }                  
                                 
            public void resetRead() {
                readTupleIndex = 0;
                countReadIdx = 0;
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
            public void incrementCount() {
                countReadIdx++;  
            }                                                                                              

            public void autoIncrementCount() {
                while (readTupleIndex >= getCountEndIndex() && readTupleIndex < writeTupleIndex)
                    countReadIdx++;
            }                 
            public void incrementTuple() {
                readTupleIndex++;
            }                    
            public int getCountEndIndex() {
                if ((countReadIdx+1) >= countTupleIdx.size())
                    return writeTupleIndex;
                return countTupleIdx.get(countReadIdx+1);
            }
            public int getReadIndex() {
                return readTupleIndex;
            }   

            public int getWriteIndex() {
                return writeTupleIndex;
            } 
            public long getCount() {
                assert readTupleIndex < writeTupleIndex;
                assert countReadIdx < counts.size();
                
                return counts.get(countReadIdx);
            }
            public byte[] getWord() {
                assert readTupleIndex < writeTupleIndex;
                return words[readTupleIndex];
            }                                         
            public long getDocuments() {
                assert readTupleIndex < writeTupleIndex;
                return documentss[readTupleIndex];
            }                                         
            public void copyTuples(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                   output.processTuple(getWord(), getDocuments());
                   incrementTuple();
                }
            }                                                                           
            public void copyUntilIndexCount(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                    output.processCount(getCount());
                    assert getCountEndIndex() <= endIndex;
                    copyTuples(getCountEndIndex(), output);
                    incrementCount();
                }
            }  
            public void copyUntilCount(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                while (!isAtEnd()) {
                    if (other != null) {   
                        assert !other.isAtEnd();
                        int c = + Utility.compare(getCount(), other.getCount());
                    
                        if (c > 0) {
                            break;   
                        }
                        
                        output.processCount(getCount());
                                      
                        copyTuples(getCountEndIndex(), output);
                    } else {
                        output.processCount(getCount());
                        copyTuples(getCountEndIndex(), output);
                    }
                    incrementCount();  
                    
               
                }
            }
            public void copyUntil(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                copyUntilCount(other, output);
            }
            
        }                         
        public static class ShreddedCombiner implements ReaderSource<WordCount>, ShreddedSource {   
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
                } else if (processor instanceof WordCount.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((WordCount.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<WordCount>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<WordCount> getOutputClass() {
                return WordCount.class;
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

            public WordCount read() throws IOException {
                if (uninitialized)
                    initialize();

                WordCount result = null;

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
        public static class ShreddedReader implements Step, Comparable<ShreddedReader>, TypeReader<WordCount>, ShreddedSource {      
            public ShreddedProcessor processor;
            ShreddedBuffer buffer;
            WordCount last = new WordCount();         
            long updateCountCount = -1;
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
                    result = + Utility.compare(buffer.getCount(), otherBuffer.getCount());
                    if(result != 0) break;
                } while (false);                                             
                
                return result;
            }
            
            public final ShreddedBuffer getBuffer() {
                return buffer;
            }                
            
            public final WordCount read() throws IOException {
                if (buffer.isAtEnd()) {
                    fill();             
                
                    if (buffer.isAtEnd()) {
                        return null;
                    }
                }
                      
                assert !buffer.isAtEnd();
                WordCount result = new WordCount();
                
                result.count = buffer.getCount();
                result.word = buffer.getWord();
                result.documents = buffer.getDocuments();
                
                buffer.incrementTuple();
                buffer.autoIncrementCount();
                
                return result;
            }           
            
            public final void fill() throws IOException {
                try {   
                    buffer.reset();
                    
                    if (tupleCount != 0) {
                                                      
                        if(updateCountCount - tupleCount > 0) {
                            buffer.counts.add(last.count);
                            buffer.countTupleIdx.add((int) (updateCountCount - tupleCount));
                        }
                        bufferStartCount = tupleCount;
                    }
                    
                    while (!buffer.isFull()) {
                        updateCount();
                        buffer.processTuple(input.readBytes(), input.readLong());
                        tupleCount++;
                    }
                } catch(EOFException e) {}
            }

            public final void updateCount() throws IOException {
                if (updateCountCount > tupleCount)
                    return;
                     
                last.count = input.readLong();
                updateCountCount = tupleCount + input.readInt();
                                      
                buffer.processCount(last.count);
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
                } else if (processor instanceof WordCount.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((WordCount.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<WordCount>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<WordCount> getOutputClass() {
                return WordCount.class;
            }                
        }
        
        public static class DuplicateEliminator implements ShreddedProcessor {
            public ShreddedProcessor processor;
            WordCount last = new WordCount();
            boolean countProcess = true;
                                           
            public DuplicateEliminator() {}
            public DuplicateEliminator(ShreddedProcessor processor) {
                this.processor = processor;
            }
            
            public void setShreddedProcessor(ShreddedProcessor processor) {
                this.processor = processor;
            }

            public void processCount(long count) throws IOException {  
                if (countProcess || Utility.compare(count, last.count) != 0) {
                    last.count = count;
                    processor.processCount(count);
                    countProcess = false;
                }
            }  
            
            public void resetCount() {
                 countProcess = true;
            }                                                
                               
            public void processTuple(byte[] word, long documents) throws IOException {
                processor.processTuple(word, documents);
            } 
            
            public void close() throws IOException {
                processor.close();
            }                    
        }
        public static class TupleUnshredder implements ShreddedProcessor {
            WordCount last = new WordCount();
            public org.lemurproject.galago.tupleflow.Processor<WordCount> processor;                               
            
            public TupleUnshredder(WordCount.Processor processor) {
                this.processor = processor;
            }         
            
            public TupleUnshredder(org.lemurproject.galago.tupleflow.Processor<WordCount> processor) {
                this.processor = processor;
            }
            
            public WordCount clone(WordCount object) {
                WordCount result = new WordCount();
                if (object == null) return result;
                result.word = object.word; 
                result.count = object.count; 
                result.documents = object.documents; 
                return result;
            }                 
            
            public void processCount(long count) throws IOException {
                last.count = count;
            }   
                
            
            public void processTuple(byte[] word, long documents) throws IOException {
                last.word = word;
                last.documents = documents;
                processor.process(clone(last));
            }               
            
            public void close() throws IOException {
                processor.close();
            }
        }     
        public static class TupleShredder implements Processor {
            WordCount last = null;
            public ShreddedProcessor processor;
            
            public TupleShredder(ShreddedProcessor processor) {
                this.processor = processor;
            }                              
            
            public WordCount clone(WordCount object) {
                WordCount result = new WordCount();
                if (object == null) return result;
                result.word = object.word; 
                result.count = object.count; 
                result.documents = object.documents; 
                return result;
            }                 
            
            public void process(WordCount object) throws IOException {                                                                                                                                                   
                boolean processAll = false;
                if(last == null || Utility.compare(last.count, object.count) != 0 || processAll) { processor.processCount(object.count); processAll = true; }
                processor.processTuple(object.word, object.documents);                                         
                last = object;
            }
                          
            public Class<WordCount> getInputClass() {
                return WordCount.class;
            }
            
            public void close() throws IOException {
                processor.close();
            }                     
        }
    } 
    public static class DescCountOrder implements Order<WordCount> {
        public int hash(WordCount object) {
            int h = 0;
            h += Utility.hash(object.count);
            return h;
        } 
        public Comparator<WordCount> greaterThan() {
            return new Comparator<WordCount>() {
                public int compare(WordCount one, WordCount two) {
                    int result = 0;
                    do {
                        result = - Utility.compare(one.count, two.count);
                        if(result != 0) break;
                    } while (false);
                    return -result;
                }
            };
        }     
        public Comparator<WordCount> lessThan() {
            return new Comparator<WordCount>() {
                public int compare(WordCount one, WordCount two) {
                    int result = 0;
                    do {
                        result = - Utility.compare(one.count, two.count);
                        if(result != 0) break;
                    } while (false);
                    return result;
                }
            };
        }     
        public TypeReader<WordCount> orderedReader(ArrayInput _input) {
            return new ShreddedReader(_input);
        }    

        public TypeReader<WordCount> orderedReader(ArrayInput _input, int bufferSize) {
            return new ShreddedReader(_input, bufferSize);
        }    
        public OrderedWriter<WordCount> orderedWriter(ArrayOutput _output) {
            ShreddedWriter w = new ShreddedWriter(_output);
            return new OrderedWriterClass(w); 
        }                                    
        public static class OrderedWriterClass extends OrderedWriter< WordCount > {
            WordCount last = null;
            ShreddedWriter shreddedWriter = null; 
            
            public OrderedWriterClass(ShreddedWriter s) {
                this.shreddedWriter = s;
            }
            
            public void process(WordCount object) throws IOException {
               boolean processAll = false;
               if (processAll || last == null || 0 != Utility.compare(object.count, last.count)) { processAll = true; shreddedWriter.processCount(object.count); }
               shreddedWriter.processTuple(object.word, object.documents);
               last = object;
            }           
                 
            public void close() throws IOException {
                shreddedWriter.close();
            }
            
            public Class<WordCount> getInputClass() {
                return WordCount.class;
            }
        } 
        public ReaderSource<WordCount> orderedCombiner(Collection<TypeReader<WordCount>> readers, boolean closeOnExit) {
            ArrayList<ShreddedReader> shreddedReaders = new ArrayList();
            
            for (TypeReader<WordCount> reader : readers) {
                shreddedReaders.add((ShreddedReader)reader);
            }
            
            return new ShreddedCombiner(shreddedReaders, closeOnExit);
        }                  
        public WordCount clone(WordCount object) {
            WordCount result = new WordCount();
            if (object == null) return result;
            result.word = object.word; 
            result.count = object.count; 
            result.documents = object.documents; 
            return result;
        }                 
        public Class<WordCount> getOrderedClass() {
            return WordCount.class;
        }                           
        public String[] getOrderSpec() {
            return new String[] {"-count"};
        }

        public static String[] getSpec() {
            return new String[] {"-count"};
        }
        public static String getSpecString() {
            return "-count";
        }
                           
        public interface ShreddedProcessor extends Step {
            public void processCount(long count) throws IOException;
            public void processTuple(byte[] word, long documents) throws IOException;
            public void close() throws IOException;
        }    
        public interface ShreddedSource extends Step {
        }                                              
        
        public static class ShreddedWriter implements ShreddedProcessor {
            ArrayOutput output;
            ShreddedBuffer buffer = new ShreddedBuffer();
            long lastCount;
            boolean lastFlush = false;
            
            public ShreddedWriter(ArrayOutput output) {
                this.output = output;
            }                        
            
            public void close() throws IOException {
                flush();
            }
            
            public void processCount(long count) {
                lastCount = count;
                buffer.processCount(count);
            }
            public final void processTuple(byte[] word, long documents) throws IOException {
                if (lastFlush) {
                    if(buffer.counts.size() == 0) buffer.processCount(lastCount);
                    lastFlush = false;
                }
                buffer.processTuple(word, documents);
                if (buffer.isFull())
                    flush();
            }
            public final void flushTuples(int pauseIndex) throws IOException {
                
                while (buffer.getReadIndex() < pauseIndex) {
                           
                    output.writeBytes(buffer.getWord());
                    output.writeLong(buffer.getDocuments());
                    buffer.incrementTuple();
                }
            }  
            public final void flushCount(int pauseIndex) throws IOException {
                while (buffer.getReadIndex() < pauseIndex) {
                    int nextPause = buffer.getCountEndIndex();
                    int count = nextPause - buffer.getReadIndex();
                    
                    output.writeLong(buffer.getCount());
                    output.writeInt(count);
                    buffer.incrementCount();
                      
                    flushTuples(nextPause);
                    assert nextPause == buffer.getReadIndex();
                }
            }
            public void flush() throws IOException { 
                flushCount(buffer.getWriteIndex());
                buffer.reset(); 
                lastFlush = true;
            }                           
        }
        public static class ShreddedBuffer {
            TLongArrayList counts = new TLongArrayList();
            TIntArrayList countTupleIdx = new TIntArrayList();
            int countReadIdx = 0;
                            
            byte[][] words;
            long[] documentss;
            int writeTupleIndex = 0;
            int readTupleIndex = 0;
            int batchSize;

            public ShreddedBuffer(int batchSize) {
                this.batchSize = batchSize;

                words = new byte[batchSize][];
                documentss = new long[batchSize];
            }                              

            public ShreddedBuffer() {    
                this(10000);
            }                                                                                                                    
            
            public void processCount(long count) {
                counts.add(count);
                countTupleIdx.add(writeTupleIndex);
            }                                      
            public void processTuple(byte[] word, long documents) {
                assert counts.size() > 0;
                words[writeTupleIndex] = word;
                documentss[writeTupleIndex] = documents;
                writeTupleIndex++;
            }
            public void resetData() {
                counts.clear();
                countTupleIdx.clear();
                writeTupleIndex = 0;
            }                  
                                 
            public void resetRead() {
                readTupleIndex = 0;
                countReadIdx = 0;
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
            public void incrementCount() {
                countReadIdx++;  
            }                                                                                              

            public void autoIncrementCount() {
                while (readTupleIndex >= getCountEndIndex() && readTupleIndex < writeTupleIndex)
                    countReadIdx++;
            }                 
            public void incrementTuple() {
                readTupleIndex++;
            }                    
            public int getCountEndIndex() {
                if ((countReadIdx+1) >= countTupleIdx.size())
                    return writeTupleIndex;
                return countTupleIdx.get(countReadIdx+1);
            }
            public int getReadIndex() {
                return readTupleIndex;
            }   

            public int getWriteIndex() {
                return writeTupleIndex;
            } 
            public long getCount() {
                assert readTupleIndex < writeTupleIndex;
                assert countReadIdx < counts.size();
                
                return counts.get(countReadIdx);
            }
            public byte[] getWord() {
                assert readTupleIndex < writeTupleIndex;
                return words[readTupleIndex];
            }                                         
            public long getDocuments() {
                assert readTupleIndex < writeTupleIndex;
                return documentss[readTupleIndex];
            }                                         
            public void copyTuples(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                   output.processTuple(getWord(), getDocuments());
                   incrementTuple();
                }
            }                                                                           
            public void copyUntilIndexCount(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                    output.processCount(getCount());
                    assert getCountEndIndex() <= endIndex;
                    copyTuples(getCountEndIndex(), output);
                    incrementCount();
                }
            }  
            public void copyUntilCount(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                while (!isAtEnd()) {
                    if (other != null) {   
                        assert !other.isAtEnd();
                        int c = - Utility.compare(getCount(), other.getCount());
                    
                        if (c > 0) {
                            break;   
                        }
                        
                        output.processCount(getCount());
                                      
                        copyTuples(getCountEndIndex(), output);
                    } else {
                        output.processCount(getCount());
                        copyTuples(getCountEndIndex(), output);
                    }
                    incrementCount();  
                    
               
                }
            }
            public void copyUntil(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                copyUntilCount(other, output);
            }
            
        }                         
        public static class ShreddedCombiner implements ReaderSource<WordCount>, ShreddedSource {   
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
                } else if (processor instanceof WordCount.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((WordCount.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<WordCount>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<WordCount> getOutputClass() {
                return WordCount.class;
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

            public WordCount read() throws IOException {
                if (uninitialized)
                    initialize();

                WordCount result = null;

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
        public static class ShreddedReader implements Step, Comparable<ShreddedReader>, TypeReader<WordCount>, ShreddedSource {      
            public ShreddedProcessor processor;
            ShreddedBuffer buffer;
            WordCount last = new WordCount();         
            long updateCountCount = -1;
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
                    result = - Utility.compare(buffer.getCount(), otherBuffer.getCount());
                    if(result != 0) break;
                } while (false);                                             
                
                return result;
            }
            
            public final ShreddedBuffer getBuffer() {
                return buffer;
            }                
            
            public final WordCount read() throws IOException {
                if (buffer.isAtEnd()) {
                    fill();             
                
                    if (buffer.isAtEnd()) {
                        return null;
                    }
                }
                      
                assert !buffer.isAtEnd();
                WordCount result = new WordCount();
                
                result.count = buffer.getCount();
                result.word = buffer.getWord();
                result.documents = buffer.getDocuments();
                
                buffer.incrementTuple();
                buffer.autoIncrementCount();
                
                return result;
            }           
            
            public final void fill() throws IOException {
                try {   
                    buffer.reset();
                    
                    if (tupleCount != 0) {
                                                      
                        if(updateCountCount - tupleCount > 0) {
                            buffer.counts.add(last.count);
                            buffer.countTupleIdx.add((int) (updateCountCount - tupleCount));
                        }
                        bufferStartCount = tupleCount;
                    }
                    
                    while (!buffer.isFull()) {
                        updateCount();
                        buffer.processTuple(input.readBytes(), input.readLong());
                        tupleCount++;
                    }
                } catch(EOFException e) {}
            }

            public final void updateCount() throws IOException {
                if (updateCountCount > tupleCount)
                    return;
                     
                last.count = input.readLong();
                updateCountCount = tupleCount + input.readInt();
                                      
                buffer.processCount(last.count);
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
                } else if (processor instanceof WordCount.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((WordCount.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<WordCount>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<WordCount> getOutputClass() {
                return WordCount.class;
            }                
        }
        
        public static class DuplicateEliminator implements ShreddedProcessor {
            public ShreddedProcessor processor;
            WordCount last = new WordCount();
            boolean countProcess = true;
                                           
            public DuplicateEliminator() {}
            public DuplicateEliminator(ShreddedProcessor processor) {
                this.processor = processor;
            }
            
            public void setShreddedProcessor(ShreddedProcessor processor) {
                this.processor = processor;
            }

            public void processCount(long count) throws IOException {  
                if (countProcess || Utility.compare(count, last.count) != 0) {
                    last.count = count;
                    processor.processCount(count);
                    countProcess = false;
                }
            }  
            
            public void resetCount() {
                 countProcess = true;
            }                                                
                               
            public void processTuple(byte[] word, long documents) throws IOException {
                processor.processTuple(word, documents);
            } 
            
            public void close() throws IOException {
                processor.close();
            }                    
        }
        public static class TupleUnshredder implements ShreddedProcessor {
            WordCount last = new WordCount();
            public org.lemurproject.galago.tupleflow.Processor<WordCount> processor;                               
            
            public TupleUnshredder(WordCount.Processor processor) {
                this.processor = processor;
            }         
            
            public TupleUnshredder(org.lemurproject.galago.tupleflow.Processor<WordCount> processor) {
                this.processor = processor;
            }
            
            public WordCount clone(WordCount object) {
                WordCount result = new WordCount();
                if (object == null) return result;
                result.word = object.word; 
                result.count = object.count; 
                result.documents = object.documents; 
                return result;
            }                 
            
            public void processCount(long count) throws IOException {
                last.count = count;
            }   
                
            
            public void processTuple(byte[] word, long documents) throws IOException {
                last.word = word;
                last.documents = documents;
                processor.process(clone(last));
            }               
            
            public void close() throws IOException {
                processor.close();
            }
        }     
        public static class TupleShredder implements Processor {
            WordCount last = null;
            public ShreddedProcessor processor;
            
            public TupleShredder(ShreddedProcessor processor) {
                this.processor = processor;
            }                              
            
            public WordCount clone(WordCount object) {
                WordCount result = new WordCount();
                if (object == null) return result;
                result.word = object.word; 
                result.count = object.count; 
                result.documents = object.documents; 
                return result;
            }                 
            
            public void process(WordCount object) throws IOException {                                                                                                                                                   
                boolean processAll = false;
                if(last == null || Utility.compare(last.count, object.count) != 0 || processAll) { processor.processCount(object.count); processAll = true; }
                processor.processTuple(object.word, object.documents);                                         
                last = object;
            }
                          
            public Class<WordCount> getInputClass() {
                return WordCount.class;
            }
            
            public void close() throws IOException {
                processor.close();
            }                     
        }
    } 
}    