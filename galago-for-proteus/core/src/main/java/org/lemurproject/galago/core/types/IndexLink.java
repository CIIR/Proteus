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


public class IndexLink implements Type<IndexLink> {
    public byte[] id;
    public int pos;
    public String srctype;
    public String targetid;
    public int targetpos;
    public String targettype; 
    
    public IndexLink() {}
    public IndexLink(byte[] id, int pos, String srctype, String targetid, int targetpos, String targettype) {
        this.id = id;
        this.pos = pos;
        this.srctype = srctype;
        this.targetid = targetid;
        this.targetpos = targetpos;
        this.targettype = targettype;
    }  
    
    public String toString() {
        try {
            return String.format("%s,%d,%s,%s,%d,%s",
                                   new String(id, "UTF-8"), pos, srctype, targetid, targetpos, targettype);
        } catch(UnsupportedEncodingException e) {
            throw new RuntimeException("Couldn't convert string to UTF-8.");
        }
    } 

    public Order<IndexLink> getOrder(String... spec) {
        if (Arrays.equals(spec, new String[] { "+srctype", "+targettype", "+id", "+targetid", "+targetpos" })) {
            return new SrctypeTargettypeIdTargetidTargetposOrder();
        }
        return null;
    } 
      
    public interface Processor extends Step, org.lemurproject.galago.tupleflow.Processor<IndexLink> {
        public void process(IndexLink object) throws IOException;
        public void close() throws IOException;
    }                        
    public interface Source extends Step {
    }
    public static class SrctypeTargettypeIdTargetidTargetposOrder implements Order<IndexLink> {
        public int hash(IndexLink object) {
            int h = 0;
            h += Utility.hash(object.srctype);
            h += Utility.hash(object.targettype);
            h += Utility.hash(object.id);
            h += Utility.hash(object.targetid);
            h += Utility.hash(object.targetpos);
            return h;
        } 
        public Comparator<IndexLink> greaterThan() {
            return new Comparator<IndexLink>() {
                public int compare(IndexLink one, IndexLink two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.srctype, two.srctype);
                        if(result != 0) break;
                        result = + Utility.compare(one.targettype, two.targettype);
                        if(result != 0) break;
                        result = + Utility.compare(one.id, two.id);
                        if(result != 0) break;
                        result = + Utility.compare(one.targetid, two.targetid);
                        if(result != 0) break;
                        result = + Utility.compare(one.targetpos, two.targetpos);
                        if(result != 0) break;
                    } while (false);
                    return -result;
                }
            };
        }     
        public Comparator<IndexLink> lessThan() {
            return new Comparator<IndexLink>() {
                public int compare(IndexLink one, IndexLink two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.srctype, two.srctype);
                        if(result != 0) break;
                        result = + Utility.compare(one.targettype, two.targettype);
                        if(result != 0) break;
                        result = + Utility.compare(one.id, two.id);
                        if(result != 0) break;
                        result = + Utility.compare(one.targetid, two.targetid);
                        if(result != 0) break;
                        result = + Utility.compare(one.targetpos, two.targetpos);
                        if(result != 0) break;
                    } while (false);
                    return result;
                }
            };
        }     
        public TypeReader<IndexLink> orderedReader(ArrayInput _input) {
            return new ShreddedReader(_input);
        }    

        public TypeReader<IndexLink> orderedReader(ArrayInput _input, int bufferSize) {
            return new ShreddedReader(_input, bufferSize);
        }    
        public OrderedWriter<IndexLink> orderedWriter(ArrayOutput _output) {
            ShreddedWriter w = new ShreddedWriter(_output);
            return new OrderedWriterClass(w); 
        }                                    
        public static class OrderedWriterClass extends OrderedWriter< IndexLink > {
            IndexLink last = null;
            ShreddedWriter shreddedWriter = null; 
            
            public OrderedWriterClass(ShreddedWriter s) {
                this.shreddedWriter = s;
            }
            
            public void process(IndexLink object) throws IOException {
               boolean processAll = false;
               if (processAll || last == null || 0 != Utility.compare(object.srctype, last.srctype)) { processAll = true; shreddedWriter.processSrctype(object.srctype); }
               if (processAll || last == null || 0 != Utility.compare(object.targettype, last.targettype)) { processAll = true; shreddedWriter.processTargettype(object.targettype); }
               if (processAll || last == null || 0 != Utility.compare(object.id, last.id)) { processAll = true; shreddedWriter.processId(object.id); }
               if (processAll || last == null || 0 != Utility.compare(object.targetid, last.targetid)) { processAll = true; shreddedWriter.processTargetid(object.targetid); }
               if (processAll || last == null || 0 != Utility.compare(object.targetpos, last.targetpos)) { processAll = true; shreddedWriter.processTargetpos(object.targetpos); }
               shreddedWriter.processTuple(object.pos);
               last = object;
            }           
                 
            public void close() throws IOException {
                shreddedWriter.close();
            }
            
            public Class<IndexLink> getInputClass() {
                return IndexLink.class;
            }
        } 
        public ReaderSource<IndexLink> orderedCombiner(Collection<TypeReader<IndexLink>> readers, boolean closeOnExit) {
            ArrayList<ShreddedReader> shreddedReaders = new ArrayList();
            
            for (TypeReader<IndexLink> reader : readers) {
                shreddedReaders.add((ShreddedReader)reader);
            }
            
            return new ShreddedCombiner(shreddedReaders, closeOnExit);
        }                  
        public IndexLink clone(IndexLink object) {
            IndexLink result = new IndexLink();
            if (object == null) return result;
            result.id = object.id; 
            result.pos = object.pos; 
            result.srctype = object.srctype; 
            result.targetid = object.targetid; 
            result.targetpos = object.targetpos; 
            result.targettype = object.targettype; 
            return result;
        }                 
        public Class<IndexLink> getOrderedClass() {
            return IndexLink.class;
        }                           
        public String[] getOrderSpec() {
            return new String[] {"+srctype", "+targettype", "+id", "+targetid", "+targetpos"};
        }

        public static String[] getSpec() {
            return new String[] {"+srctype", "+targettype", "+id", "+targetid", "+targetpos"};
        }
        public static String getSpecString() {
            return "+srctype +targettype +id +targetid +targetpos";
        }
                           
        public interface ShreddedProcessor extends Step {
            public void processSrctype(String srctype) throws IOException;
            public void processTargettype(String targettype) throws IOException;
            public void processId(byte[] id) throws IOException;
            public void processTargetid(String targetid) throws IOException;
            public void processTargetpos(int targetpos) throws IOException;
            public void processTuple(int pos) throws IOException;
            public void close() throws IOException;
        }    
        public interface ShreddedSource extends Step {
        }                                              
        
        public static class ShreddedWriter implements ShreddedProcessor {
            ArrayOutput output;
            ShreddedBuffer buffer = new ShreddedBuffer();
            String lastSrctype;
            String lastTargettype;
            byte[] lastId;
            String lastTargetid;
            int lastTargetpos;
            boolean lastFlush = false;
            
            public ShreddedWriter(ArrayOutput output) {
                this.output = output;
            }                        
            
            public void close() throws IOException {
                flush();
            }
            
            public void processSrctype(String srctype) {
                lastSrctype = srctype;
                buffer.processSrctype(srctype);
            }
            public void processTargettype(String targettype) {
                lastTargettype = targettype;
                buffer.processTargettype(targettype);
            }
            public void processId(byte[] id) {
                lastId = id;
                buffer.processId(id);
            }
            public void processTargetid(String targetid) {
                lastTargetid = targetid;
                buffer.processTargetid(targetid);
            }
            public void processTargetpos(int targetpos) {
                lastTargetpos = targetpos;
                buffer.processTargetpos(targetpos);
            }
            public final void processTuple(int pos) throws IOException {
                if (lastFlush) {
                    if(buffer.srctypes.size() == 0) buffer.processSrctype(lastSrctype);
                    if(buffer.targettypes.size() == 0) buffer.processTargettype(lastTargettype);
                    if(buffer.ids.size() == 0) buffer.processId(lastId);
                    if(buffer.targetids.size() == 0) buffer.processTargetid(lastTargetid);
                    if(buffer.targetposs.size() == 0) buffer.processTargetpos(lastTargetpos);
                    lastFlush = false;
                }
                buffer.processTuple(pos);
                if (buffer.isFull())
                    flush();
            }
            public final void flushTuples(int pauseIndex) throws IOException {
                
                while (buffer.getReadIndex() < pauseIndex) {
                           
                    output.writeInt(buffer.getPos());
                    buffer.incrementTuple();
                }
            }  
            public final void flushSrctype(int pauseIndex) throws IOException {
                while (buffer.getReadIndex() < pauseIndex) {
                    int nextPause = buffer.getSrctypeEndIndex();
                    int count = nextPause - buffer.getReadIndex();
                    
                    output.writeString(buffer.getSrctype());
                    output.writeInt(count);
                    buffer.incrementSrctype();
                      
                    flushTargettype(nextPause);
                    assert nextPause == buffer.getReadIndex();
                }
            }
            public final void flushTargettype(int pauseIndex) throws IOException {
                while (buffer.getReadIndex() < pauseIndex) {
                    int nextPause = buffer.getTargettypeEndIndex();
                    int count = nextPause - buffer.getReadIndex();
                    
                    output.writeString(buffer.getTargettype());
                    output.writeInt(count);
                    buffer.incrementTargettype();
                      
                    flushId(nextPause);
                    assert nextPause == buffer.getReadIndex();
                }
            }
            public final void flushId(int pauseIndex) throws IOException {
                while (buffer.getReadIndex() < pauseIndex) {
                    int nextPause = buffer.getIdEndIndex();
                    int count = nextPause - buffer.getReadIndex();
                    
                    output.writeBytes(buffer.getId());
                    output.writeInt(count);
                    buffer.incrementId();
                      
                    flushTargetid(nextPause);
                    assert nextPause == buffer.getReadIndex();
                }
            }
            public final void flushTargetid(int pauseIndex) throws IOException {
                while (buffer.getReadIndex() < pauseIndex) {
                    int nextPause = buffer.getTargetidEndIndex();
                    int count = nextPause - buffer.getReadIndex();
                    
                    output.writeString(buffer.getTargetid());
                    output.writeInt(count);
                    buffer.incrementTargetid();
                      
                    flushTargetpos(nextPause);
                    assert nextPause == buffer.getReadIndex();
                }
            }
            public final void flushTargetpos(int pauseIndex) throws IOException {
                while (buffer.getReadIndex() < pauseIndex) {
                    int nextPause = buffer.getTargetposEndIndex();
                    int count = nextPause - buffer.getReadIndex();
                    
                    output.writeInt(buffer.getTargetpos());
                    output.writeInt(count);
                    buffer.incrementTargetpos();
                      
                    flushTuples(nextPause);
                    assert nextPause == buffer.getReadIndex();
                }
            }
            public void flush() throws IOException { 
                flushSrctype(buffer.getWriteIndex());
                buffer.reset(); 
                lastFlush = true;
            }                           
        }
        public static class ShreddedBuffer {
            ArrayList<String> srctypes = new ArrayList();
            ArrayList<String> targettypes = new ArrayList();
            ArrayList<byte[]> ids = new ArrayList();
            ArrayList<String> targetids = new ArrayList();
            TIntArrayList targetposs = new TIntArrayList();
            TIntArrayList srctypeTupleIdx = new TIntArrayList();
            TIntArrayList targettypeTupleIdx = new TIntArrayList();
            TIntArrayList idTupleIdx = new TIntArrayList();
            TIntArrayList targetidTupleIdx = new TIntArrayList();
            TIntArrayList targetposTupleIdx = new TIntArrayList();
            int srctypeReadIdx = 0;
            int targettypeReadIdx = 0;
            int idReadIdx = 0;
            int targetidReadIdx = 0;
            int targetposReadIdx = 0;
                            
            int[] poss;
            int writeTupleIndex = 0;
            int readTupleIndex = 0;
            int batchSize;

            public ShreddedBuffer(int batchSize) {
                this.batchSize = batchSize;

                poss = new int[batchSize];
            }                              

            public ShreddedBuffer() {    
                this(10000);
            }                                                                                                                    
            
            public void processSrctype(String srctype) {
                srctypes.add(srctype);
                srctypeTupleIdx.add(writeTupleIndex);
            }                                      
            public void processTargettype(String targettype) {
                targettypes.add(targettype);
                targettypeTupleIdx.add(writeTupleIndex);
            }                                      
            public void processId(byte[] id) {
                ids.add(id);
                idTupleIdx.add(writeTupleIndex);
            }                                      
            public void processTargetid(String targetid) {
                targetids.add(targetid);
                targetidTupleIdx.add(writeTupleIndex);
            }                                      
            public void processTargetpos(int targetpos) {
                targetposs.add(targetpos);
                targetposTupleIdx.add(writeTupleIndex);
            }                                      
            public void processTuple(int pos) {
                assert srctypes.size() > 0;
                assert targettypes.size() > 0;
                assert ids.size() > 0;
                assert targetids.size() > 0;
                assert targetposs.size() > 0;
                poss[writeTupleIndex] = pos;
                writeTupleIndex++;
            }
            public void resetData() {
                srctypes.clear();
                targettypes.clear();
                ids.clear();
                targetids.clear();
                targetposs.clear();
                srctypeTupleIdx.clear();
                targettypeTupleIdx.clear();
                idTupleIdx.clear();
                targetidTupleIdx.clear();
                targetposTupleIdx.clear();
                writeTupleIndex = 0;
            }                  
                                 
            public void resetRead() {
                readTupleIndex = 0;
                srctypeReadIdx = 0;
                targettypeReadIdx = 0;
                idReadIdx = 0;
                targetidReadIdx = 0;
                targetposReadIdx = 0;
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
            public void incrementSrctype() {
                srctypeReadIdx++;  
            }                                                                                              

            public void autoIncrementSrctype() {
                while (readTupleIndex >= getSrctypeEndIndex() && readTupleIndex < writeTupleIndex)
                    srctypeReadIdx++;
            }                 
            public void incrementTargettype() {
                targettypeReadIdx++;  
            }                                                                                              

            public void autoIncrementTargettype() {
                while (readTupleIndex >= getTargettypeEndIndex() && readTupleIndex < writeTupleIndex)
                    targettypeReadIdx++;
            }                 
            public void incrementId() {
                idReadIdx++;  
            }                                                                                              

            public void autoIncrementId() {
                while (readTupleIndex >= getIdEndIndex() && readTupleIndex < writeTupleIndex)
                    idReadIdx++;
            }                 
            public void incrementTargetid() {
                targetidReadIdx++;  
            }                                                                                              

            public void autoIncrementTargetid() {
                while (readTupleIndex >= getTargetidEndIndex() && readTupleIndex < writeTupleIndex)
                    targetidReadIdx++;
            }                 
            public void incrementTargetpos() {
                targetposReadIdx++;  
            }                                                                                              

            public void autoIncrementTargetpos() {
                while (readTupleIndex >= getTargetposEndIndex() && readTupleIndex < writeTupleIndex)
                    targetposReadIdx++;
            }                 
            public void incrementTuple() {
                readTupleIndex++;
            }                    
            public int getSrctypeEndIndex() {
                if ((srctypeReadIdx+1) >= srctypeTupleIdx.size())
                    return writeTupleIndex;
                return srctypeTupleIdx.get(srctypeReadIdx+1);
            }

            public int getTargettypeEndIndex() {
                if ((targettypeReadIdx+1) >= targettypeTupleIdx.size())
                    return writeTupleIndex;
                return targettypeTupleIdx.get(targettypeReadIdx+1);
            }

            public int getIdEndIndex() {
                if ((idReadIdx+1) >= idTupleIdx.size())
                    return writeTupleIndex;
                return idTupleIdx.get(idReadIdx+1);
            }

            public int getTargetidEndIndex() {
                if ((targetidReadIdx+1) >= targetidTupleIdx.size())
                    return writeTupleIndex;
                return targetidTupleIdx.get(targetidReadIdx+1);
            }

            public int getTargetposEndIndex() {
                if ((targetposReadIdx+1) >= targetposTupleIdx.size())
                    return writeTupleIndex;
                return targetposTupleIdx.get(targetposReadIdx+1);
            }
            public int getReadIndex() {
                return readTupleIndex;
            }   

            public int getWriteIndex() {
                return writeTupleIndex;
            } 
            public String getSrctype() {
                assert readTupleIndex < writeTupleIndex;
                assert srctypeReadIdx < srctypes.size();
                
                return srctypes.get(srctypeReadIdx);
            }
            public String getTargettype() {
                assert readTupleIndex < writeTupleIndex;
                assert targettypeReadIdx < targettypes.size();
                
                return targettypes.get(targettypeReadIdx);
            }
            public byte[] getId() {
                assert readTupleIndex < writeTupleIndex;
                assert idReadIdx < ids.size();
                
                return ids.get(idReadIdx);
            }
            public String getTargetid() {
                assert readTupleIndex < writeTupleIndex;
                assert targetidReadIdx < targetids.size();
                
                return targetids.get(targetidReadIdx);
            }
            public int getTargetpos() {
                assert readTupleIndex < writeTupleIndex;
                assert targetposReadIdx < targetposs.size();
                
                return targetposs.get(targetposReadIdx);
            }
            public int getPos() {
                assert readTupleIndex < writeTupleIndex;
                return poss[readTupleIndex];
            }                                         
            public void copyTuples(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                   output.processTuple(getPos());
                   incrementTuple();
                }
            }                                                                           
            public void copyUntilIndexSrctype(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                    output.processSrctype(getSrctype());
                    assert getSrctypeEndIndex() <= endIndex;
                    copyUntilIndexTargettype(getSrctypeEndIndex(), output);
                    incrementSrctype();
                }
            } 
            public void copyUntilIndexTargettype(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                    output.processTargettype(getTargettype());
                    assert getTargettypeEndIndex() <= endIndex;
                    copyUntilIndexId(getTargettypeEndIndex(), output);
                    incrementTargettype();
                }
            } 
            public void copyUntilIndexId(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                    output.processId(getId());
                    assert getIdEndIndex() <= endIndex;
                    copyUntilIndexTargetid(getIdEndIndex(), output);
                    incrementId();
                }
            } 
            public void copyUntilIndexTargetid(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                    output.processTargetid(getTargetid());
                    assert getTargetidEndIndex() <= endIndex;
                    copyUntilIndexTargetpos(getTargetidEndIndex(), output);
                    incrementTargetid();
                }
            } 
            public void copyUntilIndexTargetpos(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                    output.processTargetpos(getTargetpos());
                    assert getTargetposEndIndex() <= endIndex;
                    copyTuples(getTargetposEndIndex(), output);
                    incrementTargetpos();
                }
            }  
            public void copyUntilSrctype(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                while (!isAtEnd()) {
                    if (other != null) {   
                        assert !other.isAtEnd();
                        int c = + Utility.compare(getSrctype(), other.getSrctype());
                    
                        if (c > 0) {
                            break;   
                        }
                        
                        output.processSrctype(getSrctype());
                                      
                        if (c < 0) {
                            copyUntilIndexTargettype(getSrctypeEndIndex(), output);
                        } else if (c == 0) {
                            copyUntilTargettype(other, output);
                            autoIncrementSrctype();
                            break;
                        }
                    } else {
                        output.processSrctype(getSrctype());
                        copyUntilIndexTargettype(getSrctypeEndIndex(), output);
                    }
                    incrementSrctype();  
                    
               
                }
            }
            public void copyUntilTargettype(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                while (!isAtEnd()) {
                    if (other != null) {   
                        assert !other.isAtEnd();
                        int c = + Utility.compare(getTargettype(), other.getTargettype());
                    
                        if (c > 0) {
                            break;   
                        }
                        
                        output.processTargettype(getTargettype());
                                      
                        if (c < 0) {
                            copyUntilIndexId(getTargettypeEndIndex(), output);
                        } else if (c == 0) {
                            copyUntilId(other, output);
                            autoIncrementTargettype();
                            break;
                        }
                    } else {
                        output.processTargettype(getTargettype());
                        copyUntilIndexId(getTargettypeEndIndex(), output);
                    }
                    incrementTargettype();  
                    
                    if (getSrctypeEndIndex() <= readTupleIndex)
                        break;   
                }
            }
            public void copyUntilId(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                while (!isAtEnd()) {
                    if (other != null) {   
                        assert !other.isAtEnd();
                        int c = + Utility.compare(getId(), other.getId());
                    
                        if (c > 0) {
                            break;   
                        }
                        
                        output.processId(getId());
                                      
                        if (c < 0) {
                            copyUntilIndexTargetid(getIdEndIndex(), output);
                        } else if (c == 0) {
                            copyUntilTargetid(other, output);
                            autoIncrementId();
                            break;
                        }
                    } else {
                        output.processId(getId());
                        copyUntilIndexTargetid(getIdEndIndex(), output);
                    }
                    incrementId();  
                    
                    if (getTargettypeEndIndex() <= readTupleIndex)
                        break;   
                }
            }
            public void copyUntilTargetid(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                while (!isAtEnd()) {
                    if (other != null) {   
                        assert !other.isAtEnd();
                        int c = + Utility.compare(getTargetid(), other.getTargetid());
                    
                        if (c > 0) {
                            break;   
                        }
                        
                        output.processTargetid(getTargetid());
                                      
                        if (c < 0) {
                            copyUntilIndexTargetpos(getTargetidEndIndex(), output);
                        } else if (c == 0) {
                            copyUntilTargetpos(other, output);
                            autoIncrementTargetid();
                            break;
                        }
                    } else {
                        output.processTargetid(getTargetid());
                        copyUntilIndexTargetpos(getTargetidEndIndex(), output);
                    }
                    incrementTargetid();  
                    
                    if (getIdEndIndex() <= readTupleIndex)
                        break;   
                }
            }
            public void copyUntilTargetpos(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                while (!isAtEnd()) {
                    if (other != null) {   
                        assert !other.isAtEnd();
                        int c = + Utility.compare(getTargetpos(), other.getTargetpos());
                    
                        if (c > 0) {
                            break;   
                        }
                        
                        output.processTargetpos(getTargetpos());
                                      
                        copyTuples(getTargetposEndIndex(), output);
                    } else {
                        output.processTargetpos(getTargetpos());
                        copyTuples(getTargetposEndIndex(), output);
                    }
                    incrementTargetpos();  
                    
                    if (getTargetidEndIndex() <= readTupleIndex)
                        break;   
                }
            }
            public void copyUntil(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                copyUntilSrctype(other, output);
            }
            
        }                         
        public static class ShreddedCombiner implements ReaderSource<IndexLink>, ShreddedSource {   
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
                } else if (processor instanceof IndexLink.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((IndexLink.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<IndexLink>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<IndexLink> getOutputClass() {
                return IndexLink.class;
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

            public IndexLink read() throws IOException {
                if (uninitialized)
                    initialize();

                IndexLink result = null;

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
        public static class ShreddedReader implements Step, Comparable<ShreddedReader>, TypeReader<IndexLink>, ShreddedSource {      
            public ShreddedProcessor processor;
            ShreddedBuffer buffer;
            IndexLink last = new IndexLink();         
            long updateSrctypeCount = -1;
            long updateTargettypeCount = -1;
            long updateIdCount = -1;
            long updateTargetidCount = -1;
            long updateTargetposCount = -1;
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
                    result = + Utility.compare(buffer.getSrctype(), otherBuffer.getSrctype());
                    if(result != 0) break;
                    result = + Utility.compare(buffer.getTargettype(), otherBuffer.getTargettype());
                    if(result != 0) break;
                    result = + Utility.compare(buffer.getId(), otherBuffer.getId());
                    if(result != 0) break;
                    result = + Utility.compare(buffer.getTargetid(), otherBuffer.getTargetid());
                    if(result != 0) break;
                    result = + Utility.compare(buffer.getTargetpos(), otherBuffer.getTargetpos());
                    if(result != 0) break;
                } while (false);                                             
                
                return result;
            }
            
            public final ShreddedBuffer getBuffer() {
                return buffer;
            }                
            
            public final IndexLink read() throws IOException {
                if (buffer.isAtEnd()) {
                    fill();             
                
                    if (buffer.isAtEnd()) {
                        return null;
                    }
                }
                      
                assert !buffer.isAtEnd();
                IndexLink result = new IndexLink();
                
                result.srctype = buffer.getSrctype();
                result.targettype = buffer.getTargettype();
                result.id = buffer.getId();
                result.targetid = buffer.getTargetid();
                result.targetpos = buffer.getTargetpos();
                result.pos = buffer.getPos();
                
                buffer.incrementTuple();
                buffer.autoIncrementSrctype();
                buffer.autoIncrementTargettype();
                buffer.autoIncrementId();
                buffer.autoIncrementTargetid();
                buffer.autoIncrementTargetpos();
                
                return result;
            }           
            
            public final void fill() throws IOException {
                try {   
                    buffer.reset();
                    
                    if (tupleCount != 0) {
                                                      
                        if(updateSrctypeCount - tupleCount > 0) {
                            buffer.srctypes.add(last.srctype);
                            buffer.srctypeTupleIdx.add((int) (updateSrctypeCount - tupleCount));
                        }                              
                        if(updateTargettypeCount - tupleCount > 0) {
                            buffer.targettypes.add(last.targettype);
                            buffer.targettypeTupleIdx.add((int) (updateTargettypeCount - tupleCount));
                        }                              
                        if(updateIdCount - tupleCount > 0) {
                            buffer.ids.add(last.id);
                            buffer.idTupleIdx.add((int) (updateIdCount - tupleCount));
                        }                              
                        if(updateTargetidCount - tupleCount > 0) {
                            buffer.targetids.add(last.targetid);
                            buffer.targetidTupleIdx.add((int) (updateTargetidCount - tupleCount));
                        }                              
                        if(updateTargetposCount - tupleCount > 0) {
                            buffer.targetposs.add(last.targetpos);
                            buffer.targetposTupleIdx.add((int) (updateTargetposCount - tupleCount));
                        }
                        bufferStartCount = tupleCount;
                    }
                    
                    while (!buffer.isFull()) {
                        updateTargetpos();
                        buffer.processTuple(input.readInt());
                        tupleCount++;
                    }
                } catch(EOFException e) {}
            }

            public final void updateSrctype() throws IOException {
                if (updateSrctypeCount > tupleCount)
                    return;
                     
                last.srctype = input.readString();
                updateSrctypeCount = tupleCount + input.readInt();
                                      
                buffer.processSrctype(last.srctype);
            }
            public final void updateTargettype() throws IOException {
                if (updateTargettypeCount > tupleCount)
                    return;
                     
                updateSrctype();
                last.targettype = input.readString();
                updateTargettypeCount = tupleCount + input.readInt();
                                      
                buffer.processTargettype(last.targettype);
            }
            public final void updateId() throws IOException {
                if (updateIdCount > tupleCount)
                    return;
                     
                updateTargettype();
                last.id = input.readBytes();
                updateIdCount = tupleCount + input.readInt();
                                      
                buffer.processId(last.id);
            }
            public final void updateTargetid() throws IOException {
                if (updateTargetidCount > tupleCount)
                    return;
                     
                updateId();
                last.targetid = input.readString();
                updateTargetidCount = tupleCount + input.readInt();
                                      
                buffer.processTargetid(last.targetid);
            }
            public final void updateTargetpos() throws IOException {
                if (updateTargetposCount > tupleCount)
                    return;
                     
                updateTargetid();
                last.targetpos = input.readInt();
                updateTargetposCount = tupleCount + input.readInt();
                                      
                buffer.processTargetpos(last.targetpos);
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
                } else if (processor instanceof IndexLink.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((IndexLink.Processor) processor));
                } else if (processor instanceof org.lemurproject.galago.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.lemurproject.galago.tupleflow.Processor<IndexLink>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<IndexLink> getOutputClass() {
                return IndexLink.class;
            }                
        }
        
        public static class DuplicateEliminator implements ShreddedProcessor {
            public ShreddedProcessor processor;
            IndexLink last = new IndexLink();
            boolean srctypeProcess = true;
            boolean targettypeProcess = true;
            boolean idProcess = true;
            boolean targetidProcess = true;
            boolean targetposProcess = true;
                                           
            public DuplicateEliminator() {}
            public DuplicateEliminator(ShreddedProcessor processor) {
                this.processor = processor;
            }
            
            public void setShreddedProcessor(ShreddedProcessor processor) {
                this.processor = processor;
            }

            public void processSrctype(String srctype) throws IOException {  
                if (srctypeProcess || Utility.compare(srctype, last.srctype) != 0) {
                    last.srctype = srctype;
                    processor.processSrctype(srctype);
            resetTargettype();
                    srctypeProcess = false;
                }
            }
            public void processTargettype(String targettype) throws IOException {  
                if (targettypeProcess || Utility.compare(targettype, last.targettype) != 0) {
                    last.targettype = targettype;
                    processor.processTargettype(targettype);
            resetId();
                    targettypeProcess = false;
                }
            }
            public void processId(byte[] id) throws IOException {  
                if (idProcess || Utility.compare(id, last.id) != 0) {
                    last.id = id;
                    processor.processId(id);
            resetTargetid();
                    idProcess = false;
                }
            }
            public void processTargetid(String targetid) throws IOException {  
                if (targetidProcess || Utility.compare(targetid, last.targetid) != 0) {
                    last.targetid = targetid;
                    processor.processTargetid(targetid);
            resetTargetpos();
                    targetidProcess = false;
                }
            }
            public void processTargetpos(int targetpos) throws IOException {  
                if (targetposProcess || Utility.compare(targetpos, last.targetpos) != 0) {
                    last.targetpos = targetpos;
                    processor.processTargetpos(targetpos);
                    targetposProcess = false;
                }
            }  
            
            public void resetSrctype() {
                 srctypeProcess = true;
            resetTargettype();
            }                                                
            public void resetTargettype() {
                 targettypeProcess = true;
            resetId();
            }                                                
            public void resetId() {
                 idProcess = true;
            resetTargetid();
            }                                                
            public void resetTargetid() {
                 targetidProcess = true;
            resetTargetpos();
            }                                                
            public void resetTargetpos() {
                 targetposProcess = true;
            }                                                
                               
            public void processTuple(int pos) throws IOException {
                processor.processTuple(pos);
            } 
            
            public void close() throws IOException {
                processor.close();
            }                    
        }
        public static class TupleUnshredder implements ShreddedProcessor {
            IndexLink last = new IndexLink();
            public org.lemurproject.galago.tupleflow.Processor<IndexLink> processor;                               
            
            public TupleUnshredder(IndexLink.Processor processor) {
                this.processor = processor;
            }         
            
            public TupleUnshredder(org.lemurproject.galago.tupleflow.Processor<IndexLink> processor) {
                this.processor = processor;
            }
            
            public IndexLink clone(IndexLink object) {
                IndexLink result = new IndexLink();
                if (object == null) return result;
                result.id = object.id; 
                result.pos = object.pos; 
                result.srctype = object.srctype; 
                result.targetid = object.targetid; 
                result.targetpos = object.targetpos; 
                result.targettype = object.targettype; 
                return result;
            }                 
            
            public void processSrctype(String srctype) throws IOException {
                last.srctype = srctype;
            }   
                
            public void processTargettype(String targettype) throws IOException {
                last.targettype = targettype;
            }   
                
            public void processId(byte[] id) throws IOException {
                last.id = id;
            }   
                
            public void processTargetid(String targetid) throws IOException {
                last.targetid = targetid;
            }   
                
            public void processTargetpos(int targetpos) throws IOException {
                last.targetpos = targetpos;
            }   
                
            
            public void processTuple(int pos) throws IOException {
                last.pos = pos;
                processor.process(clone(last));
            }               
            
            public void close() throws IOException {
                processor.close();
            }
        }     
        public static class TupleShredder implements Processor {
            IndexLink last = null;
            public ShreddedProcessor processor;
            
            public TupleShredder(ShreddedProcessor processor) {
                this.processor = processor;
            }                              
            
            public IndexLink clone(IndexLink object) {
                IndexLink result = new IndexLink();
                if (object == null) return result;
                result.id = object.id; 
                result.pos = object.pos; 
                result.srctype = object.srctype; 
                result.targetid = object.targetid; 
                result.targetpos = object.targetpos; 
                result.targettype = object.targettype; 
                return result;
            }                 
            
            public void process(IndexLink object) throws IOException {                                                                                                                                                   
                boolean processAll = false;
                if(last == null || Utility.compare(last.srctype, object.srctype) != 0 || processAll) { processor.processSrctype(object.srctype); processAll = true; }
                if(last == null || Utility.compare(last.targettype, object.targettype) != 0 || processAll) { processor.processTargettype(object.targettype); processAll = true; }
                if(last == null || Utility.compare(last.id, object.id) != 0 || processAll) { processor.processId(object.id); processAll = true; }
                if(last == null || Utility.compare(last.targetid, object.targetid) != 0 || processAll) { processor.processTargetid(object.targetid); processAll = true; }
                if(last == null || Utility.compare(last.targetpos, object.targetpos) != 0 || processAll) { processor.processTargetpos(object.targetpos); processAll = true; }
                processor.processTuple(object.pos);                                         
                last = object;
            }
                          
            public Class<IndexLink> getInputClass() {
                return IndexLink.class;
            }
            
            public void close() throws IOException {
                processor.close();
            }                     
        }
    } 
}    