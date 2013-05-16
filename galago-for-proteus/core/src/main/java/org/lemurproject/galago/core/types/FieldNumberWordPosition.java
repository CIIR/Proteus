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


public class FieldNumberWordPosition implements Type<FieldNumberWordPosition> {
    public String field;
    public int document;
    public byte[] word;
    public int position; 
    
    public FieldNumberWordPosition() {}
    public FieldNumberWordPosition(String field, int document, byte[] word, int position) {
        this.field = field;
        this.document = document;
        this.word = word;
        this.position = position;
    }  
    
    public String toString() {
        try {
            return String.format("%s,%d,%s,%d",
                                   field, document, new String(word, "UTF-8"), position);
        } catch(UnsupportedEncodingException e) {
            throw new RuntimeException("Couldn't convert string to UTF-8.");
        }
    } 

    public Order<FieldNumberWordPosition> getOrder(String... spec) {
        if (Arrays.equals(spec, new String[] { "+field", "+word", "+document", "+position" })) {
            return new FieldWordDocumentPositionOrder();
        }
        if (Arrays.equals(spec, new String[] { "+word" })) {
            return new WordOrder();
        }
        return null;
    } 
      
    public interface Processor extends Step, org.lemurproject.galago.tupleflow.Processor<FieldNumberWordPosition> {
        public void process(FieldNumberWordPosition object) throws IOException;
        public void close() throws IOException;
    }                        
    public interface Source extends Step {
    }
    public static class FieldWordDocumentPositionOrder implements Order<FieldNumberWordPosition> {
        public int hash(FieldNumberWordPosition object) {
            int h = 0;
            h += Utility.hash(object.field);
            h += Utility.hash(object.word);
            h += Utility.hash(object.document);
            h += Utility.hash(object.position);
            return h;
        } 
        public Comparator<FieldNumberWordPosition> greaterThan() {
            return new Comparator<FieldNumberWordPosition>() {
                public int compare(FieldNumberWordPosition one, FieldNumberWordPosition two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.field, two.field);
                        if(result != 0) break;
                        result = + Utility.compare(one.word, two.word);
                        if(result != 0) break;
                        result = + Utility.compare(one.document, two.document);
                        if(result != 0) break;
                        result = + Utility.compare(one.position, two.position);
                        if(result != 0) break;
                    } while (false);
                    return -result;
                }
            };
        }     
        public Comparator<FieldNumberWordPosition> lessThan() {
            return new Comparator<FieldNumberWordPosition>() {
                public int compare(FieldNumberWordPosition one, FieldNumberWordPosition two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.field, two.field);
                        if(result != 0) break;
                        result = + Utility.compare(one.word, two.word);
                        if(result != 0) break;
                        result = + Utility.compare(one.document, two.document);
                        if(result != 0) break;
                        result = + Utility.compare(one.position, two.position);
                        if(result != 0) break;
                    } while (false);
                    return result;
                }
            };
        }     
        public TypeReader<FieldNumberWordPosition> orderedReader(ArrayInput _input) {
            return new ShreddedReader(_input);
        }    

        public TypeReader<FieldNumberWordPosition> orderedReader(ArrayInput _input, int bufferSize) {
            return new ShreddedReader(_input, bufferSize);
        }    
        public OrderedWriter<FieldNumberWordPosition> orderedWriter(ArrayOutput _output) {
            ShreddedWriter w = new ShreddedWriter(_output);
            return new OrderedWriterClass(w); 
        }                                    
        public static class OrderedWriterClass extends OrderedWriter< FieldNumberWordPosition > {
            FieldNumberWordPosition last = null;
            ShreddedWriter shreddedWriter = null; 
            
            public OrderedWriterClass(ShreddedWriter s) {
                this.shreddedWriter = s;
            }
            
            public void process(FieldNumberWordPosition object) throws IOException {
               boolean processAll = false;
               if (processAll || last == null || 0 != Utility.compare(object.field, last.field)) { processAll = true; shreddedWriter.processField(object.field); }
               if (processAll || last == null || 0 != Utility.compare(object.word, last.word)) { processAll = true; shreddedWriter.processWord(object.word); }
               if (processAll || last == null || 0 != Utility.compare(object.document, last.document)) { processAll = true; shreddedWriter.processDocument(object.document); }
               if (processAll || last == null || 0 != Utility.compare(object.position, last.position)) { processAll = true; shreddedWriter.processPosition(object.position); }
               shreddedWriter.processTuple();
               last = object;
            }           
                 
            public void close() throws IOException {
                shreddedWriter.close();
            }
            
            public Class<FieldNumberWordPosition> getInputClass() {
                return FieldNumberWordPosition.class;
            }
        } 
        public ReaderSource<FieldNumberWordPosition> orderedCombiner(Collection<TypeReader<FieldNumberWordPosition>> readers, boolean closeOnExit) {
            ArrayList<ShreddedReader> shreddedReaders = new ArrayList();
            
            for (TypeReader<FieldNumberWordPosition> reader : readers) {
                shreddedReaders.add((ShreddedReader)reader);
            }
            
            return new ShreddedCombiner(shreddedReaders, closeOnExit);
        }                  
        public FieldNumberWordPosition clone(FieldNumberWordPosition object) {
            FieldNumberWordPosition result = new FieldNumberWordPosition();
            if (object == null) return result;
            result.field = object.field; 
            result.document = object.document; 
            result.word = object.word; 
            result.position = object.position; 
            return result;
        }                 
        public Class<FieldNumberWordPosition> getOrderedClass() {
            return FieldNumberWordPosition.class;
        }                           
        public String[] getOrderSpec() {
            return new String[] {"+field", "+word", "+document", "+position"};
        }

        public static String[] getSpec() {
            return new String[] {"+field", "+word", "+document", "+position"};
        }
        public static String getSpecString() {
            return "+field +word +document +position";
        }
                           
        public interface ShreddedProcessor extends Step {
            public void processField(String field) throws IOException;
            public void processWord(byte[] word) throws IOException;
            public void processDocument(int document) throws IOException;
            public void processPosition(int position) throws IOException;
            public void processTuple() throws IOException;
            public void close() throws IOException;
        }    
        public interface ShreddedSource extends Step {
        }                                              
        
        public static class ShreddedWriter implements ShreddedProcessor {
            ArrayOutput output;
            ShreddedBuffer buffer = new ShreddedBuffer();
            String lastField;
            byte[] lastWord;
            int lastDocument;
            int lastPosition;
            boolean lastFlush = false;
            
            public ShreddedWriter(ArrayOutput output) {
                this.output = output;
            }                        
            
            public void close() throws IOException {
                flush();
            }
            
            public void processField(String field) {
                lastField = field;
                buffer.processField(field);
            }
            public void processWord(byte[] word) {
                lastWord = word;
                buffer.processWord(word);
            }
            public void processDocument(int document) {
                lastDocument = document;
                buffer.processDocument(document);
            }
            public void processPosition(int position) {
                lastPosition = position;
                buffer.processPosition(position);
            }
            public final void processTuple() throws IOException {
                if (lastFlush) {
                    if(buffer.fields.size() == 0) buffer.processField(lastField);
                    if(buffer.words.size() == 0) buffer.processWord(lastWord);
                    if(buffer.documents.size() == 0) buffer.processDocument(lastDocument);
                    if(buffer.positions.size() == 0) buffer.processPosition(lastPosition);
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
            public final void flushField(int pauseIndex) throws IOException {
                while (buffer.getReadIndex() < pauseIndex) {
                    int nextPause = buffer.getFieldEndIndex();
                    int count = nextPause - buffer.getReadIndex();
                    
                    output.writeString(buffer.getField());
                    output.writeInt(count);
                    buffer.incrementField();
                      
                    flushWord(nextPause);
                    assert nextPause == buffer.getReadIndex();
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
                      
                    flushPosition(nextPause);
                    assert nextPause == buffer.getReadIndex();
                }
            }
            public final void flushPosition(int pauseIndex) throws IOException {
                while (buffer.getReadIndex() < pauseIndex) {
                    int nextPause = buffer.getPositionEndIndex();
                    int count = nextPause - buffer.getReadIndex();
                    
                    output.writeInt(buffer.getPosition());
                    output.writeInt(count);
                    buffer.incrementPosition();
                      
                    flushTuples(nextPause);
                    assert nextPause == buffer.getReadIndex();
                }
            }
            public void flush() throws IOException { 
                flushField(buffer.getWriteIndex());
                buffer.reset(); 
                lastFlush = true;
            }                           
        }
        public static class ShreddedBuffer {
            ArrayList<String> fields = new ArrayList();
            ArrayList<byte[]> words = new ArrayList();
            TIntArrayList documents = new TIntArrayList();
            TIntArrayList positions = new TIntArrayList();
            TIntArrayList fieldTupleIdx = new TIntArrayList();
            TIntArrayList wordTupleIdx = new TIntArrayList();
            TIntArrayList documentTupleIdx = new TIntArrayList();
            TIntArrayList positionTupleIdx = new TIntArrayList();
            int fieldReadIdx = 0;
            int wordReadIdx = 0;
            int documentReadIdx = 0;
            int positionReadIdx = 0;
                            
            int writeTupleIndex = 0;
            int readTupleIndex = 0;
            int batchSize;

            public ShreddedBuffer(int batchSize) {
                this.batchSize = batchSize;

            }                              

            public ShreddedBuffer() {    
                this(10000);
            }                                                                                                                    
            
            public void processField(String field) {
                fields.add(field);
                fieldTupleIdx.add(writeTupleIndex);
            }                                      
            public void processWord(byte[] word) {
                words.add(word);
                wordTupleIdx.add(writeTupleIndex);
            }                                      
            public void processDocument(int document) {
                documents.add(document);
                documentTupleIdx.add(writeTupleIndex);
            }                                      
            public void processPosition(int position) {
                positions.add(position);
                positionTupleIdx.add(writeTupleIndex);
            }                                      
            public void processTuple() {
                assert fields.size() > 0;
                assert words.size() > 0;
                assert documents.size() > 0;
                assert positions.size() > 0;
                writeTupleIndex++;
            }
            public void resetData() {
                fields.clear();
                words.clear();
                documents.clear();
                positions.clear();
                fieldTupleIdx.clear();
                wordTupleIdx.clear();
                documentTupleIdx.clear();
                positionTupleIdx.clear();
                writeTupleIndex = 0;
            }                  
                                 
            public void resetRead() {
                readTupleIndex = 0;
                fieldReadIdx = 0;
                wordReadIdx = 0;
                documentReadIdx = 0;
                positionReadIdx = 0;
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
            public void incrementField() {
                fieldReadIdx++;  
            }                                                                                              

            public void autoIncrementField() {
                while (readTupleIndex >= getFieldEndIndex() && readTupleIndex < writeTupleIndex)
                    fieldReadIdx++;
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
            public void incrementPosition() {
                positionReadIdx++;  
            }                                                                                              

            public void autoIncrementPosition() {
                while (readTupleIndex >= getPositionEndIndex() && readTupleIndex < writeTupleIndex)
                    positionReadIdx++;
            }                 
            public void incrementTuple() {
                readTupleIndex++;
            }                    
            public int getFieldEndIndex() {
                if ((fieldReadIdx+1) >= fieldTupleIdx.size())
                    return writeTupleIndex;
                return fieldTupleIdx.get(fieldReadIdx+1);
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

            public int getPositionEndIndex() {
                if ((positionReadIdx+1) >= positionTupleIdx.size())
                    return writeTupleIndex;
                return positionTupleIdx.get(positionReadIdx+1);
            }
            public int getReadIndex() {
                return readTupleIndex;
            }   

            public int getWriteIndex() {
                return writeTupleIndex;
            } 
            public String getField() {
                assert readTupleIndex < writeTupleIndex;
                assert fieldReadIdx < fields.size();
                
                return fields.get(fieldReadIdx);
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
            public int getPosition() {
                assert readTupleIndex < writeTupleIndex;
                assert positionReadIdx < positions.size();
                
                return positions.get(positionReadIdx);
            }

            public void copyTuples(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                   output.processTuple();
                   incrementTuple();
                }
            }                                                                           
            public void copyUntilIndexField(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                    output.processField(getField());
                    assert getFieldEndIndex() <= endIndex;
                    copyUntilIndexWord(getFieldEndIndex(), output);
                    incrementField();
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
                    copyUntilIndexPosition(getDocumentEndIndex(), output);
                    incrementDocument();
                }
            } 
            public void copyUntilIndexPosition(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                    output.processPosition(getPosition());
                    assert getPositionEndIndex() <= endIndex;
                    copyTuples(getPositionEndIndex(), output);
                    incrementPosition();
                }
            }  
            public void copyUntilField(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                while (!isAtEnd()) {
                    if (other != null) {   
                        assert !other.isAtEnd();
                        int c = + Utility.compare(getField(), other.getField());
                    
                        if (c > 0) {
                            break;   
                        }
                        
                        output.processField(getField());
                                      
                        if (c < 0) {
                            copyUntilIndexWord(getFieldEndIndex(), output);
                        } else if (c == 0) {
                            copyUntilWord(other, output);
                            autoIncrementField();
                            break;
                        }
                    } else {
                        output.processField(getField());
                        copyUntilIndexWord(getFieldEndIndex(), output);
                    }
                    incrementField();  
                    
               
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
                    
                    if (getFieldEndIndex() <= readTupleIndex)
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
                                      
                        if (c < 0) {
                            copyUntilIndexPosition(getDocumentEndIndex(), output);
                        } else if (c == 0) {
                            copyUntilPosition(other, output);
                            autoIncrementDocument();
                            break;
                        }
                    } else {
                        output.processDocument(getDocument());
                        copyUntilIndexPosition(getDocumentEndIndex(), output);
                    }
                    incrementDocument();  
                    
                    if (getWordEndIndex() <= readTupleIndex)
                        break;   
                }
            }
            public void copyUntilPosition(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                while (!isAtEnd()) {
                    if (other != null) {   
                        assert !other.isAtEnd();
                        int c = + Utility.compare(getPosition(), other.getPosition());
                    
                        if (c > 0) {
                            break;   
                        }
                        
                        output.processPosition(getPosition());
                                      
                        copyTuples(getPositionEndIndex(), output);
                    } else {
                        output.processPosition(getPosition());
                        copyTuples(getPositionEndIndex(), output);
                    }
                    incrementPosition();  
                    
                    if (getDocumentEndIndex() <= readTupleIndex)
                        break;   
                }
            }
            public void copyUntil(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                copyUntilField(other, output);
            }
            
        }                         
        public static class ShreddedCombiner implements ReaderSource<FieldNumberWordPosition>, ShreddedSource {   
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
                } else if (processor instanceof FieldNumberWordPosition.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((FieldNumberWordPosition.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<FieldNumberWordPosition>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<FieldNumberWordPosition> getOutputClass() {
                return FieldNumberWordPosition.class;
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

            public FieldNumberWordPosition read() throws IOException {
                if (uninitialized)
                    initialize();

                FieldNumberWordPosition result = null;

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
        public static class ShreddedReader implements Step, Comparable<ShreddedReader>, TypeReader<FieldNumberWordPosition>, ShreddedSource {      
            public ShreddedProcessor processor;
            ShreddedBuffer buffer;
            FieldNumberWordPosition last = new FieldNumberWordPosition();         
            long updateFieldCount = -1;
            long updateWordCount = -1;
            long updateDocumentCount = -1;
            long updatePositionCount = -1;
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
                    result = + Utility.compare(buffer.getField(), otherBuffer.getField());
                    if(result != 0) break;
                    result = + Utility.compare(buffer.getWord(), otherBuffer.getWord());
                    if(result != 0) break;
                    result = + Utility.compare(buffer.getDocument(), otherBuffer.getDocument());
                    if(result != 0) break;
                    result = + Utility.compare(buffer.getPosition(), otherBuffer.getPosition());
                    if(result != 0) break;
                } while (false);                                             
                
                return result;
            }
            
            public final ShreddedBuffer getBuffer() {
                return buffer;
            }                
            
            public final FieldNumberWordPosition read() throws IOException {
                if (buffer.isAtEnd()) {
                    fill();             
                
                    if (buffer.isAtEnd()) {
                        return null;
                    }
                }
                      
                assert !buffer.isAtEnd();
                FieldNumberWordPosition result = new FieldNumberWordPosition();
                
                result.field = buffer.getField();
                result.word = buffer.getWord();
                result.document = buffer.getDocument();
                result.position = buffer.getPosition();
                
                buffer.incrementTuple();
                buffer.autoIncrementField();
                buffer.autoIncrementWord();
                buffer.autoIncrementDocument();
                buffer.autoIncrementPosition();
                
                return result;
            }           
            
            public final void fill() throws IOException {
                try {   
                    buffer.reset();
                    
                    if (tupleCount != 0) {
                                                      
                        if(updateFieldCount - tupleCount > 0) {
                            buffer.fields.add(last.field);
                            buffer.fieldTupleIdx.add((int) (updateFieldCount - tupleCount));
                        }                              
                        if(updateWordCount - tupleCount > 0) {
                            buffer.words.add(last.word);
                            buffer.wordTupleIdx.add((int) (updateWordCount - tupleCount));
                        }                              
                        if(updateDocumentCount - tupleCount > 0) {
                            buffer.documents.add(last.document);
                            buffer.documentTupleIdx.add((int) (updateDocumentCount - tupleCount));
                        }                              
                        if(updatePositionCount - tupleCount > 0) {
                            buffer.positions.add(last.position);
                            buffer.positionTupleIdx.add((int) (updatePositionCount - tupleCount));
                        }
                        bufferStartCount = tupleCount;
                    }
                    
                    while (!buffer.isFull()) {
                        updatePosition();
                        buffer.processTuple();
                        tupleCount++;
                    }
                } catch(EOFException e) {}
            }

            public final void updateField() throws IOException {
                if (updateFieldCount > tupleCount)
                    return;
                     
                last.field = input.readString();
                updateFieldCount = tupleCount + input.readInt();
                                      
                buffer.processField(last.field);
            }
            public final void updateWord() throws IOException {
                if (updateWordCount > tupleCount)
                    return;
                     
                updateField();
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
            public final void updatePosition() throws IOException {
                if (updatePositionCount > tupleCount)
                    return;
                     
                updateDocument();
                last.position = input.readInt();
                updatePositionCount = tupleCount + input.readInt();
                                      
                buffer.processPosition(last.position);
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
                } else if (processor instanceof FieldNumberWordPosition.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((FieldNumberWordPosition.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<FieldNumberWordPosition>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<FieldNumberWordPosition> getOutputClass() {
                return FieldNumberWordPosition.class;
            }                
        }
        
        public static class DuplicateEliminator implements ShreddedProcessor {
            public ShreddedProcessor processor;
            FieldNumberWordPosition last = new FieldNumberWordPosition();
            boolean fieldProcess = true;
            boolean wordProcess = true;
            boolean documentProcess = true;
            boolean positionProcess = true;
                                           
            public DuplicateEliminator() {}
            public DuplicateEliminator(ShreddedProcessor processor) {
                this.processor = processor;
            }
            
            public void setShreddedProcessor(ShreddedProcessor processor) {
                this.processor = processor;
            }

            public void processField(String field) throws IOException {  
                if (fieldProcess || Utility.compare(field, last.field) != 0) {
                    last.field = field;
                    processor.processField(field);
            resetWord();
                    fieldProcess = false;
                }
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
            resetPosition();
                    documentProcess = false;
                }
            }
            public void processPosition(int position) throws IOException {  
                if (positionProcess || Utility.compare(position, last.position) != 0) {
                    last.position = position;
                    processor.processPosition(position);
                    positionProcess = false;
                }
            }  
            
            public void resetField() {
                 fieldProcess = true;
            resetWord();
            }                                                
            public void resetWord() {
                 wordProcess = true;
            resetDocument();
            }                                                
            public void resetDocument() {
                 documentProcess = true;
            resetPosition();
            }                                                
            public void resetPosition() {
                 positionProcess = true;
            }                                                
                               
            public void processTuple() throws IOException {
                processor.processTuple();
            } 
            
            public void close() throws IOException {
                processor.close();
            }                    
        }
        public static class TupleUnshredder implements ShreddedProcessor {
            FieldNumberWordPosition last = new FieldNumberWordPosition();
            public org.lemurproject.galago.tupleflow.Processor<FieldNumberWordPosition> processor;                               
            
            public TupleUnshredder(FieldNumberWordPosition.Processor processor) {
                this.processor = processor;
            }         
            
            public TupleUnshredder(org.lemurproject.galago.tupleflow.Processor<FieldNumberWordPosition> processor) {
                this.processor = processor;
            }
            
            public FieldNumberWordPosition clone(FieldNumberWordPosition object) {
                FieldNumberWordPosition result = new FieldNumberWordPosition();
                if (object == null) return result;
                result.field = object.field; 
                result.document = object.document; 
                result.word = object.word; 
                result.position = object.position; 
                return result;
            }                 
            
            public void processField(String field) throws IOException {
                last.field = field;
            }   
                
            public void processWord(byte[] word) throws IOException {
                last.word = word;
            }   
                
            public void processDocument(int document) throws IOException {
                last.document = document;
            }   
                
            public void processPosition(int position) throws IOException {
                last.position = position;
            }   
                
            
            public void processTuple() throws IOException {
                processor.process(clone(last));
            }               
            
            public void close() throws IOException {
                processor.close();
            }
        }     
        public static class TupleShredder implements Processor {
            FieldNumberWordPosition last = null;
            public ShreddedProcessor processor;
            
            public TupleShredder(ShreddedProcessor processor) {
                this.processor = processor;
            }                              
            
            public FieldNumberWordPosition clone(FieldNumberWordPosition object) {
                FieldNumberWordPosition result = new FieldNumberWordPosition();
                if (object == null) return result;
                result.field = object.field; 
                result.document = object.document; 
                result.word = object.word; 
                result.position = object.position; 
                return result;
            }                 
            
            public void process(FieldNumberWordPosition object) throws IOException {                                                                                                                                                   
                boolean processAll = false;
                if(last == null || Utility.compare(last.field, object.field) != 0 || processAll) { processor.processField(object.field); processAll = true; }
                if(last == null || Utility.compare(last.word, object.word) != 0 || processAll) { processor.processWord(object.word); processAll = true; }
                if(last == null || Utility.compare(last.document, object.document) != 0 || processAll) { processor.processDocument(object.document); processAll = true; }
                if(last == null || Utility.compare(last.position, object.position) != 0 || processAll) { processor.processPosition(object.position); processAll = true; }
                processor.processTuple();                                         
                last = object;
            }
                          
            public Class<FieldNumberWordPosition> getInputClass() {
                return FieldNumberWordPosition.class;
            }
            
            public void close() throws IOException {
                processor.close();
            }                     
        }
    } 
    public static class WordOrder implements Order<FieldNumberWordPosition> {
        public int hash(FieldNumberWordPosition object) {
            int h = 0;
            h += Utility.hash(object.word);
            return h;
        } 
        public Comparator<FieldNumberWordPosition> greaterThan() {
            return new Comparator<FieldNumberWordPosition>() {
                public int compare(FieldNumberWordPosition one, FieldNumberWordPosition two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.word, two.word);
                        if(result != 0) break;
                    } while (false);
                    return -result;
                }
            };
        }     
        public Comparator<FieldNumberWordPosition> lessThan() {
            return new Comparator<FieldNumberWordPosition>() {
                public int compare(FieldNumberWordPosition one, FieldNumberWordPosition two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.word, two.word);
                        if(result != 0) break;
                    } while (false);
                    return result;
                }
            };
        }     
        public TypeReader<FieldNumberWordPosition> orderedReader(ArrayInput _input) {
            return new ShreddedReader(_input);
        }    

        public TypeReader<FieldNumberWordPosition> orderedReader(ArrayInput _input, int bufferSize) {
            return new ShreddedReader(_input, bufferSize);
        }    
        public OrderedWriter<FieldNumberWordPosition> orderedWriter(ArrayOutput _output) {
            ShreddedWriter w = new ShreddedWriter(_output);
            return new OrderedWriterClass(w); 
        }                                    
        public static class OrderedWriterClass extends OrderedWriter< FieldNumberWordPosition > {
            FieldNumberWordPosition last = null;
            ShreddedWriter shreddedWriter = null; 
            
            public OrderedWriterClass(ShreddedWriter s) {
                this.shreddedWriter = s;
            }
            
            public void process(FieldNumberWordPosition object) throws IOException {
               boolean processAll = false;
               if (processAll || last == null || 0 != Utility.compare(object.word, last.word)) { processAll = true; shreddedWriter.processWord(object.word); }
               shreddedWriter.processTuple(object.field, object.document, object.position);
               last = object;
            }           
                 
            public void close() throws IOException {
                shreddedWriter.close();
            }
            
            public Class<FieldNumberWordPosition> getInputClass() {
                return FieldNumberWordPosition.class;
            }
        } 
        public ReaderSource<FieldNumberWordPosition> orderedCombiner(Collection<TypeReader<FieldNumberWordPosition>> readers, boolean closeOnExit) {
            ArrayList<ShreddedReader> shreddedReaders = new ArrayList();
            
            for (TypeReader<FieldNumberWordPosition> reader : readers) {
                shreddedReaders.add((ShreddedReader)reader);
            }
            
            return new ShreddedCombiner(shreddedReaders, closeOnExit);
        }                  
        public FieldNumberWordPosition clone(FieldNumberWordPosition object) {
            FieldNumberWordPosition result = new FieldNumberWordPosition();
            if (object == null) return result;
            result.field = object.field; 
            result.document = object.document; 
            result.word = object.word; 
            result.position = object.position; 
            return result;
        }                 
        public Class<FieldNumberWordPosition> getOrderedClass() {
            return FieldNumberWordPosition.class;
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
            public void processTuple(String field, int document, int position) throws IOException;
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
            public final void processTuple(String field, int document, int position) throws IOException {
                if (lastFlush) {
                    if(buffer.words.size() == 0) buffer.processWord(lastWord);
                    lastFlush = false;
                }
                buffer.processTuple(field, document, position);
                if (buffer.isFull())
                    flush();
            }
            public final void flushTuples(int pauseIndex) throws IOException {
                
                while (buffer.getReadIndex() < pauseIndex) {
                           
                    output.writeString(buffer.getField());
                    output.writeInt(buffer.getDocument());
                    output.writeInt(buffer.getPosition());
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
                            
            String[] fields;
            int[] documents;
            int[] positions;
            int writeTupleIndex = 0;
            int readTupleIndex = 0;
            int batchSize;

            public ShreddedBuffer(int batchSize) {
                this.batchSize = batchSize;

                fields = new String[batchSize];
                documents = new int[batchSize];
                positions = new int[batchSize];
            }                              

            public ShreddedBuffer() {    
                this(10000);
            }                                                                                                                    
            
            public void processWord(byte[] word) {
                words.add(word);
                wordTupleIdx.add(writeTupleIndex);
            }                                      
            public void processTuple(String field, int document, int position) {
                assert words.size() > 0;
                fields[writeTupleIndex] = field;
                documents[writeTupleIndex] = document;
                positions[writeTupleIndex] = position;
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
            public String getField() {
                assert readTupleIndex < writeTupleIndex;
                return fields[readTupleIndex];
            }                                         
            public int getDocument() {
                assert readTupleIndex < writeTupleIndex;
                return documents[readTupleIndex];
            }                                         
            public int getPosition() {
                assert readTupleIndex < writeTupleIndex;
                return positions[readTupleIndex];
            }                                         
            public void copyTuples(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                   output.processTuple(getField(), getDocument(), getPosition());
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
        public static class ShreddedCombiner implements ReaderSource<FieldNumberWordPosition>, ShreddedSource {   
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
                } else if (processor instanceof FieldNumberWordPosition.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((FieldNumberWordPosition.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<FieldNumberWordPosition>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<FieldNumberWordPosition> getOutputClass() {
                return FieldNumberWordPosition.class;
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

            public FieldNumberWordPosition read() throws IOException {
                if (uninitialized)
                    initialize();

                FieldNumberWordPosition result = null;

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
        public static class ShreddedReader implements Step, Comparable<ShreddedReader>, TypeReader<FieldNumberWordPosition>, ShreddedSource {      
            public ShreddedProcessor processor;
            ShreddedBuffer buffer;
            FieldNumberWordPosition last = new FieldNumberWordPosition();         
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
            
            public final FieldNumberWordPosition read() throws IOException {
                if (buffer.isAtEnd()) {
                    fill();             
                
                    if (buffer.isAtEnd()) {
                        return null;
                    }
                }
                      
                assert !buffer.isAtEnd();
                FieldNumberWordPosition result = new FieldNumberWordPosition();
                
                result.word = buffer.getWord();
                result.field = buffer.getField();
                result.document = buffer.getDocument();
                result.position = buffer.getPosition();
                
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
                        buffer.processTuple(input.readString(), input.readInt(), input.readInt());
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
                } else if (processor instanceof FieldNumberWordPosition.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((FieldNumberWordPosition.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<FieldNumberWordPosition>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<FieldNumberWordPosition> getOutputClass() {
                return FieldNumberWordPosition.class;
            }                
        }
        
        public static class DuplicateEliminator implements ShreddedProcessor {
            public ShreddedProcessor processor;
            FieldNumberWordPosition last = new FieldNumberWordPosition();
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
                               
            public void processTuple(String field, int document, int position) throws IOException {
                processor.processTuple(field, document, position);
            } 
            
            public void close() throws IOException {
                processor.close();
            }                    
        }
        public static class TupleUnshredder implements ShreddedProcessor {
            FieldNumberWordPosition last = new FieldNumberWordPosition();
            public org.lemurproject.galago.tupleflow.Processor<FieldNumberWordPosition> processor;                               
            
            public TupleUnshredder(FieldNumberWordPosition.Processor processor) {
                this.processor = processor;
            }         
            
            public TupleUnshredder(org.lemurproject.galago.tupleflow.Processor<FieldNumberWordPosition> processor) {
                this.processor = processor;
            }
            
            public FieldNumberWordPosition clone(FieldNumberWordPosition object) {
                FieldNumberWordPosition result = new FieldNumberWordPosition();
                if (object == null) return result;
                result.field = object.field; 
                result.document = object.document; 
                result.word = object.word; 
                result.position = object.position; 
                return result;
            }                 
            
            public void processWord(byte[] word) throws IOException {
                last.word = word;
            }   
                
            
            public void processTuple(String field, int document, int position) throws IOException {
                last.field = field;
                last.document = document;
                last.position = position;
                processor.process(clone(last));
            }               
            
            public void close() throws IOException {
                processor.close();
            }
        }     
        public static class TupleShredder implements Processor {
            FieldNumberWordPosition last = null;
            public ShreddedProcessor processor;
            
            public TupleShredder(ShreddedProcessor processor) {
                this.processor = processor;
            }                              
            
            public FieldNumberWordPosition clone(FieldNumberWordPosition object) {
                FieldNumberWordPosition result = new FieldNumberWordPosition();
                if (object == null) return result;
                result.field = object.field; 
                result.document = object.document; 
                result.word = object.word; 
                result.position = object.position; 
                return result;
            }                 
            
            public void process(FieldNumberWordPosition object) throws IOException {                                                                                                                                                   
                boolean processAll = false;
                if(last == null || Utility.compare(last.word, object.word) != 0 || processAll) { processor.processWord(object.word); processAll = true; }
                processor.processTuple(object.field, object.document, object.position);                                         
                last = object;
            }
                          
            public Class<FieldNumberWordPosition> getInputClass() {
                return FieldNumberWordPosition.class;
            }
            
            public void close() throws IOException {
                processor.close();
            }                     
        }
    } 
}    