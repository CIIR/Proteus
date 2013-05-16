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


public class WordOffset implements Type<WordOffset> {
    public byte[] word;
    public long offset; 
    
    public WordOffset() {}
    public WordOffset(byte[] word, long offset) {
        this.word = word;
        this.offset = offset;
    }  
    
    public String toString() {
        try {
            return String.format("%s,%d",
                                   new String(word, "UTF-8"), offset);
        } catch(UnsupportedEncodingException e) {
            throw new RuntimeException("Couldn't convert string to UTF-8.");
        }
    } 

    public Order<WordOffset> getOrder(String... spec) {
        if (Arrays.equals(spec, new String[] { "+word" })) {
            return new WordOrder();
        }
        return null;
    } 
      
    public interface Processor extends Step, org.lemurproject.galago.tupleflow.Processor<WordOffset> {
        public void process(WordOffset object) throws IOException;
        public void close() throws IOException;
    }                        
    public interface Source extends Step {
    }
    public static class WordOrder implements Order<WordOffset> {
        public int hash(WordOffset object) {
            int h = 0;
            h += Utility.hash(object.word);
            return h;
        } 
        public Comparator<WordOffset> greaterThan() {
            return new Comparator<WordOffset>() {
                public int compare(WordOffset one, WordOffset two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.word, two.word);
                        if(result != 0) break;
                    } while (false);
                    return -result;
                }
            };
        }     
        public Comparator<WordOffset> lessThan() {
            return new Comparator<WordOffset>() {
                public int compare(WordOffset one, WordOffset two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.word, two.word);
                        if(result != 0) break;
                    } while (false);
                    return result;
                }
            };
        }     
        public TypeReader<WordOffset> orderedReader(ArrayInput _input) {
            return new ShreddedReader(_input);
        }    

        public TypeReader<WordOffset> orderedReader(ArrayInput _input, int bufferSize) {
            return new ShreddedReader(_input, bufferSize);
        }    
        public OrderedWriter<WordOffset> orderedWriter(ArrayOutput _output) {
            ShreddedWriter w = new ShreddedWriter(_output);
            return new OrderedWriterClass(w); 
        }                                    
        public static class OrderedWriterClass extends OrderedWriter< WordOffset > {
            WordOffset last = null;
            ShreddedWriter shreddedWriter = null; 
            
            public OrderedWriterClass(ShreddedWriter s) {
                this.shreddedWriter = s;
            }
            
            public void process(WordOffset object) throws IOException {
               boolean processAll = false;
               if (processAll || last == null || 0 != Utility.compare(object.word, last.word)) { processAll = true; shreddedWriter.processWord(object.word); }
               shreddedWriter.processTuple(object.offset);
               last = object;
            }           
                 
            public void close() throws IOException {
                shreddedWriter.close();
            }
            
            public Class<WordOffset> getInputClass() {
                return WordOffset.class;
            }
        } 
        public ReaderSource<WordOffset> orderedCombiner(Collection<TypeReader<WordOffset>> readers, boolean closeOnExit) {
            ArrayList<ShreddedReader> shreddedReaders = new ArrayList();
            
            for (TypeReader<WordOffset> reader : readers) {
                shreddedReaders.add((ShreddedReader)reader);
            }
            
            return new ShreddedCombiner(shreddedReaders, closeOnExit);
        }                  
        public WordOffset clone(WordOffset object) {
            WordOffset result = new WordOffset();
            if (object == null) return result;
            result.word = object.word; 
            result.offset = object.offset; 
            return result;
        }                 
        public Class<WordOffset> getOrderedClass() {
            return WordOffset.class;
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
            public void processTuple(long offset) throws IOException;
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
            public final void processTuple(long offset) throws IOException {
                if (lastFlush) {
                    if(buffer.words.size() == 0) buffer.processWord(lastWord);
                    lastFlush = false;
                }
                buffer.processTuple(offset);
                if (buffer.isFull())
                    flush();
            }
            public final void flushTuples(int pauseIndex) throws IOException {
                
                while (buffer.getReadIndex() < pauseIndex) {
                           
                    output.writeLong(buffer.getOffset());
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
                            
            long[] offsets;
            int writeTupleIndex = 0;
            int readTupleIndex = 0;
            int batchSize;

            public ShreddedBuffer(int batchSize) {
                this.batchSize = batchSize;

                offsets = new long[batchSize];
            }                              

            public ShreddedBuffer() {    
                this(10000);
            }                                                                                                                    
            
            public void processWord(byte[] word) {
                words.add(word);
                wordTupleIdx.add(writeTupleIndex);
            }                                      
            public void processTuple(long offset) {
                assert words.size() > 0;
                offsets[writeTupleIndex] = offset;
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
            public long getOffset() {
                assert readTupleIndex < writeTupleIndex;
                return offsets[readTupleIndex];
            }                                         
            public void copyTuples(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                   output.processTuple(getOffset());
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
        public static class ShreddedCombiner implements ReaderSource<WordOffset>, ShreddedSource {   
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
                } else if (processor instanceof WordOffset.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((WordOffset.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<WordOffset>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<WordOffset> getOutputClass() {
                return WordOffset.class;
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

            public WordOffset read() throws IOException {
                if (uninitialized)
                    initialize();

                WordOffset result = null;

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
        public static class ShreddedReader implements Step, Comparable<ShreddedReader>, TypeReader<WordOffset>, ShreddedSource {      
            public ShreddedProcessor processor;
            ShreddedBuffer buffer;
            WordOffset last = new WordOffset();         
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
            
            public final WordOffset read() throws IOException {
                if (buffer.isAtEnd()) {
                    fill();             
                
                    if (buffer.isAtEnd()) {
                        return null;
                    }
                }
                      
                assert !buffer.isAtEnd();
                WordOffset result = new WordOffset();
                
                result.word = buffer.getWord();
                result.offset = buffer.getOffset();
                
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
                        buffer.processTuple(input.readLong());
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
                } else if (processor instanceof WordOffset.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((WordOffset.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<WordOffset>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<WordOffset> getOutputClass() {
                return WordOffset.class;
            }                
        }
        
        public static class DuplicateEliminator implements ShreddedProcessor {
            public ShreddedProcessor processor;
            WordOffset last = new WordOffset();
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
                               
            public void processTuple(long offset) throws IOException {
                processor.processTuple(offset);
            } 
            
            public void close() throws IOException {
                processor.close();
            }                    
        }
        public static class TupleUnshredder implements ShreddedProcessor {
            WordOffset last = new WordOffset();
            public org.lemurproject.galago.tupleflow.Processor<WordOffset> processor;                               
            
            public TupleUnshredder(WordOffset.Processor processor) {
                this.processor = processor;
            }         
            
            public TupleUnshredder(org.lemurproject.galago.tupleflow.Processor<WordOffset> processor) {
                this.processor = processor;
            }
            
            public WordOffset clone(WordOffset object) {
                WordOffset result = new WordOffset();
                if (object == null) return result;
                result.word = object.word; 
                result.offset = object.offset; 
                return result;
            }                 
            
            public void processWord(byte[] word) throws IOException {
                last.word = word;
            }   
                
            
            public void processTuple(long offset) throws IOException {
                last.offset = offset;
                processor.process(clone(last));
            }               
            
            public void close() throws IOException {
                processor.close();
            }
        }     
        public static class TupleShredder implements Processor {
            WordOffset last = null;
            public ShreddedProcessor processor;
            
            public TupleShredder(ShreddedProcessor processor) {
                this.processor = processor;
            }                              
            
            public WordOffset clone(WordOffset object) {
                WordOffset result = new WordOffset();
                if (object == null) return result;
                result.word = object.word; 
                result.offset = object.offset; 
                return result;
            }                 
            
            public void process(WordOffset object) throws IOException {                                                                                                                                                   
                boolean processAll = false;
                if(last == null || Utility.compare(last.word, object.word) != 0 || processAll) { processor.processWord(object.word); processAll = true; }
                processor.processTuple(object.offset);                                         
                last = object;
            }
                          
            public Class<WordOffset> getInputClass() {
                return WordOffset.class;
            }
            
            public void close() throws IOException {
                processor.close();
            }                     
        }
    } 
}    