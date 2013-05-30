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


public class WordDateCount implements Type<WordDateCount> {
    public byte[] word;
    public int date;
    public long count; 
    
    public WordDateCount() {}
    public WordDateCount(byte[] word, int date, long count) {
        this.word = word;
        this.date = date;
        this.count = count;
    }  
    
    public String toString() {
        try {
            return String.format("%s,%d,%d",
                                   new String(word, "UTF-8"), date, count);
        } catch(UnsupportedEncodingException e) {
            throw new RuntimeException("Couldn't convert string to UTF-8.");
        }
    } 

    public Order<WordDateCount> getOrder(String... spec) {
        if (Arrays.equals(spec, new String[] { "+word", "+date" })) {
            return new WordDateOrder();
        }
        return null;
    } 
      
    public interface Processor extends Step, org.lemurproject.galago.tupleflow.Processor<WordDateCount> {
        public void process(WordDateCount object) throws IOException;
        public void close() throws IOException;
    }                        
    public interface Source extends Step {
    }
    public static class WordDateOrder implements Order<WordDateCount> {
        public int hash(WordDateCount object) {
            int h = 0;
            h += Utility.hash(object.word);
            h += Utility.hash(object.date);
            return h;
        } 
        public Comparator<WordDateCount> greaterThan() {
            return new Comparator<WordDateCount>() {
                public int compare(WordDateCount one, WordDateCount two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.word, two.word);
                        if(result != 0) break;
                        result = + Utility.compare(one.date, two.date);
                        if(result != 0) break;
                    } while (false);
                    return -result;
                }
            };
        }     
        public Comparator<WordDateCount> lessThan() {
            return new Comparator<WordDateCount>() {
                public int compare(WordDateCount one, WordDateCount two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.word, two.word);
                        if(result != 0) break;
                        result = + Utility.compare(one.date, two.date);
                        if(result != 0) break;
                    } while (false);
                    return result;
                }
            };
        }     
        public TypeReader<WordDateCount> orderedReader(ArrayInput _input) {
            return new ShreddedReader(_input);
        }    

        public TypeReader<WordDateCount> orderedReader(ArrayInput _input, int bufferSize) {
            return new ShreddedReader(_input, bufferSize);
        }    
        public OrderedWriter<WordDateCount> orderedWriter(ArrayOutput _output) {
            ShreddedWriter w = new ShreddedWriter(_output);
            return new OrderedWriterClass(w); 
        }                                    
        public static class OrderedWriterClass extends OrderedWriter< WordDateCount > {
            WordDateCount last = null;
            ShreddedWriter shreddedWriter = null; 
            
            public OrderedWriterClass(ShreddedWriter s) {
                this.shreddedWriter = s;
            }
            
            public void process(WordDateCount object) throws IOException {
               boolean processAll = false;
               if (processAll || last == null || 0 != Utility.compare(object.word, last.word)) { processAll = true; shreddedWriter.processWord(object.word); }
               if (processAll || last == null || 0 != Utility.compare(object.date, last.date)) { processAll = true; shreddedWriter.processDate(object.date); }
               shreddedWriter.processTuple(object.count);
               last = object;
            }           
                 
            public void close() throws IOException {
                shreddedWriter.close();
            }
            
            public Class<WordDateCount> getInputClass() {
                return WordDateCount.class;
            }
        } 
        public ReaderSource<WordDateCount> orderedCombiner(Collection<TypeReader<WordDateCount>> readers, boolean closeOnExit) {
            ArrayList<ShreddedReader> shreddedReaders = new ArrayList();
            
            for (TypeReader<WordDateCount> reader : readers) {
                shreddedReaders.add((ShreddedReader)reader);
            }
            
            return new ShreddedCombiner(shreddedReaders, closeOnExit);
        }                  
        public WordDateCount clone(WordDateCount object) {
            WordDateCount result = new WordDateCount();
            if (object == null) return result;
            result.word = object.word; 
            result.date = object.date; 
            result.count = object.count; 
            return result;
        }                 
        public Class<WordDateCount> getOrderedClass() {
            return WordDateCount.class;
        }                           
        public String[] getOrderSpec() {
            return new String[] {"+word", "+date"};
        }

        public static String[] getSpec() {
            return new String[] {"+word", "+date"};
        }
        public static String getSpecString() {
            return "+word +date";
        }
                           
        public interface ShreddedProcessor extends Step {
            public void processWord(byte[] word) throws IOException;
            public void processDate(int date) throws IOException;
            public void processTuple(long count) throws IOException;
            public void close() throws IOException;
        }    
        public interface ShreddedSource extends Step {
        }                                              
        
        public static class ShreddedWriter implements ShreddedProcessor {
            ArrayOutput output;
            ShreddedBuffer buffer = new ShreddedBuffer();
            byte[] lastWord;
            int lastDate;
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
            public void processDate(int date) {
                lastDate = date;
                buffer.processDate(date);
            }
            public final void processTuple(long count) throws IOException {
                if (lastFlush) {
                    if(buffer.words.size() == 0) buffer.processWord(lastWord);
                    if(buffer.dates.size() == 0) buffer.processDate(lastDate);
                    lastFlush = false;
                }
                buffer.processTuple(count);
                if (buffer.isFull())
                    flush();
            }
            public final void flushTuples(int pauseIndex) throws IOException {
                
                while (buffer.getReadIndex() < pauseIndex) {
                           
                    output.writeLong(buffer.getCount());
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
                      
                    flushDate(nextPause);
                    assert nextPause == buffer.getReadIndex();
                }
            }
            public final void flushDate(int pauseIndex) throws IOException {
                while (buffer.getReadIndex() < pauseIndex) {
                    int nextPause = buffer.getDateEndIndex();
                    int count = nextPause - buffer.getReadIndex();
                    
                    output.writeInt(buffer.getDate());
                    output.writeInt(count);
                    buffer.incrementDate();
                      
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
            TIntArrayList dates = new TIntArrayList();
            TIntArrayList wordTupleIdx = new TIntArrayList();
            TIntArrayList dateTupleIdx = new TIntArrayList();
            int wordReadIdx = 0;
            int dateReadIdx = 0;
                            
            long[] counts;
            int writeTupleIndex = 0;
            int readTupleIndex = 0;
            int batchSize;

            public ShreddedBuffer(int batchSize) {
                this.batchSize = batchSize;

                counts = new long[batchSize];
            }                              

            public ShreddedBuffer() {    
                this(10000);
            }                                                                                                                    
            
            public void processWord(byte[] word) {
                words.add(word);
                wordTupleIdx.add(writeTupleIndex);
            }                                      
            public void processDate(int date) {
                dates.add(date);
                dateTupleIdx.add(writeTupleIndex);
            }                                      
            public void processTuple(long count) {
                assert words.size() > 0;
                assert dates.size() > 0;
                counts[writeTupleIndex] = count;
                writeTupleIndex++;
            }
            public void resetData() {
                words.clear();
                dates.clear();
                wordTupleIdx.clear();
                dateTupleIdx.clear();
                writeTupleIndex = 0;
            }                  
                                 
            public void resetRead() {
                readTupleIndex = 0;
                wordReadIdx = 0;
                dateReadIdx = 0;
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
            public void incrementDate() {
                dateReadIdx++;  
            }                                                                                              

            public void autoIncrementDate() {
                while (readTupleIndex >= getDateEndIndex() && readTupleIndex < writeTupleIndex)
                    dateReadIdx++;
            }                 
            public void incrementTuple() {
                readTupleIndex++;
            }                    
            public int getWordEndIndex() {
                if ((wordReadIdx+1) >= wordTupleIdx.size())
                    return writeTupleIndex;
                return wordTupleIdx.get(wordReadIdx+1);
            }

            public int getDateEndIndex() {
                if ((dateReadIdx+1) >= dateTupleIdx.size())
                    return writeTupleIndex;
                return dateTupleIdx.get(dateReadIdx+1);
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
            public int getDate() {
                assert readTupleIndex < writeTupleIndex;
                assert dateReadIdx < dates.size();
                
                return dates.get(dateReadIdx);
            }
            public long getCount() {
                assert readTupleIndex < writeTupleIndex;
                return counts[readTupleIndex];
            }                                         
            public void copyTuples(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                   output.processTuple(getCount());
                   incrementTuple();
                }
            }                                                                           
            public void copyUntilIndexWord(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                    output.processWord(getWord());
                    assert getWordEndIndex() <= endIndex;
                    copyUntilIndexDate(getWordEndIndex(), output);
                    incrementWord();
                }
            } 
            public void copyUntilIndexDate(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                    output.processDate(getDate());
                    assert getDateEndIndex() <= endIndex;
                    copyTuples(getDateEndIndex(), output);
                    incrementDate();
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
                            copyUntilIndexDate(getWordEndIndex(), output);
                        } else if (c == 0) {
                            copyUntilDate(other, output);
                            autoIncrementWord();
                            break;
                        }
                    } else {
                        output.processWord(getWord());
                        copyUntilIndexDate(getWordEndIndex(), output);
                    }
                    incrementWord();  
                    
               
                }
            }
            public void copyUntilDate(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                while (!isAtEnd()) {
                    if (other != null) {   
                        assert !other.isAtEnd();
                        int c = + Utility.compare(getDate(), other.getDate());
                    
                        if (c > 0) {
                            break;   
                        }
                        
                        output.processDate(getDate());
                                      
                        copyTuples(getDateEndIndex(), output);
                    } else {
                        output.processDate(getDate());
                        copyTuples(getDateEndIndex(), output);
                    }
                    incrementDate();  
                    
                    if (getWordEndIndex() <= readTupleIndex)
                        break;   
                }
            }
            public void copyUntil(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                copyUntilWord(other, output);
            }
            
        }                         
        public static class ShreddedCombiner implements ReaderSource<WordDateCount>, ShreddedSource {   
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
                } else if (processor instanceof WordDateCount.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((WordDateCount.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<WordDateCount>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<WordDateCount> getOutputClass() {
                return WordDateCount.class;
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

            public WordDateCount read() throws IOException {
                if (uninitialized)
                    initialize();

                WordDateCount result = null;

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
        public static class ShreddedReader implements Step, Comparable<ShreddedReader>, TypeReader<WordDateCount>, ShreddedSource {      
            public ShreddedProcessor processor;
            ShreddedBuffer buffer;
            WordDateCount last = new WordDateCount();         
            long updateWordCount = -1;
            long updateDateCount = -1;
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
                    result = + Utility.compare(buffer.getDate(), otherBuffer.getDate());
                    if(result != 0) break;
                } while (false);                                             
                
                return result;
            }
            
            public final ShreddedBuffer getBuffer() {
                return buffer;
            }                
            
            public final WordDateCount read() throws IOException {
                if (buffer.isAtEnd()) {
                    fill();             
                
                    if (buffer.isAtEnd()) {
                        return null;
                    }
                }
                      
                assert !buffer.isAtEnd();
                WordDateCount result = new WordDateCount();
                
                result.word = buffer.getWord();
                result.date = buffer.getDate();
                result.count = buffer.getCount();
                
                buffer.incrementTuple();
                buffer.autoIncrementWord();
                buffer.autoIncrementDate();
                
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
                        if(updateDateCount - tupleCount > 0) {
                            buffer.dates.add(last.date);
                            buffer.dateTupleIdx.add((int) (updateDateCount - tupleCount));
                        }
                        bufferStartCount = tupleCount;
                    }
                    
                    while (!buffer.isFull()) {
                        updateDate();
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
            public final void updateDate() throws IOException {
                if (updateDateCount > tupleCount)
                    return;
                     
                updateWord();
                last.date = input.readInt();
                updateDateCount = tupleCount + input.readInt();
                                      
                buffer.processDate(last.date);
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
                } else if (processor instanceof WordDateCount.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((WordDateCount.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<WordDateCount>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<WordDateCount> getOutputClass() {
                return WordDateCount.class;
            }                
        }
        
        public static class DuplicateEliminator implements ShreddedProcessor {
            public ShreddedProcessor processor;
            WordDateCount last = new WordDateCount();
            boolean wordProcess = true;
            boolean dateProcess = true;
                                           
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
            resetDate();
                    wordProcess = false;
                }
            }
            public void processDate(int date) throws IOException {  
                if (dateProcess || Utility.compare(date, last.date) != 0) {
                    last.date = date;
                    processor.processDate(date);
                    dateProcess = false;
                }
            }  
            
            public void resetWord() {
                 wordProcess = true;
            resetDate();
            }                                                
            public void resetDate() {
                 dateProcess = true;
            }                                                
                               
            public void processTuple(long count) throws IOException {
                processor.processTuple(count);
            } 
            
            public void close() throws IOException {
                processor.close();
            }                    
        }
        public static class TupleUnshredder implements ShreddedProcessor {
            WordDateCount last = new WordDateCount();
            public org.lemurproject.galago.tupleflow.Processor<WordDateCount> processor;                               
            
            public TupleUnshredder(WordDateCount.Processor processor) {
                this.processor = processor;
            }         
            
            public TupleUnshredder(org.lemurproject.galago.tupleflow.Processor<WordDateCount> processor) {
                this.processor = processor;
            }
            
            public WordDateCount clone(WordDateCount object) {
                WordDateCount result = new WordDateCount();
                if (object == null) return result;
                result.word = object.word; 
                result.date = object.date; 
                result.count = object.count; 
                return result;
            }                 
            
            public void processWord(byte[] word) throws IOException {
                last.word = word;
            }   
                
            public void processDate(int date) throws IOException {
                last.date = date;
            }   
                
            
            public void processTuple(long count) throws IOException {
                last.count = count;
                processor.process(clone(last));
            }               
            
            public void close() throws IOException {
                processor.close();
            }
        }     
        public static class TupleShredder implements Processor {
            WordDateCount last = null;
            public ShreddedProcessor processor;
            
            public TupleShredder(ShreddedProcessor processor) {
                this.processor = processor;
            }                              
            
            public WordDateCount clone(WordDateCount object) {
                WordDateCount result = new WordDateCount();
                if (object == null) return result;
                result.word = object.word; 
                result.date = object.date; 
                result.count = object.count; 
                return result;
            }                 
            
            public void process(WordDateCount object) throws IOException {                                                                                                                                                   
                boolean processAll = false;
                if(last == null || Utility.compare(last.word, object.word) != 0 || processAll) { processor.processWord(object.word); processAll = true; }
                if(last == null || Utility.compare(last.date, object.date) != 0 || processAll) { processor.processDate(object.date); processAll = true; }
                processor.processTuple(object.count);                                         
                last = object;
            }
                          
            public Class<WordDateCount> getInputClass() {
                return WordDateCount.class;
            }
            
            public void close() throws IOException {
                processor.close();
            }                     
        }
    } 
}    