package ciir.proteus.galago

import java.io.File

import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions._
import scala.collection.mutable.HashMap
import org.lemurproject.galago.tupleflow.Parameters
import org.lemurproject.galago.tupleflow.Utility
import org.lemurproject.galago.core.index.disk.DiskBTreeReader
import ciir.proteus._
import com.twitter.finagle.builder._
import com.twitter.finagle.thrift.ThriftServerFramedCodec
import com.twitter.util.{Duration,Future}
import org.apache.thrift.protocol._
import org.apache.thrift.transport._

object TopicHandler {
  def apply(p: Parameters) = new TopicHandler(p)
}

class TopicHandler(p: Parameters) extends Handler(p) {
  val factory = new TCompactProtocol.Factory

  // These are simple wrappers to deserialize the stored classes in the indexes
  class PagesReader(index: DiskBTreeReader) {
    def getEntry(key: String) : Option[PrefixedTermMap] = {
      if (index == null) return None
      val iterator = index.getIterator(Utility.fromString(key))
      if (iterator == null) return None

      val transport = new TMemoryInputTransport(iterator.getValueBytes)
      val protocol = factory.getProtocol(transport)
      return Some(PrefixedTermMap.decoder(protocol))
    }
  }
  
  class WordsReader(index: DiskBTreeReader) {
    def getEntry(key: String) : Option[TermList] = {
      val iterator = index.getIterator(Utility.fromString(key))
      if (iterator == null) {
	return None
      }
     
      val transport = new TMemoryInputTransport(iterator.getValueBytes)
      val protocol = factory.getProtocol(transport)
      return Some(TermList.decoder(protocol))
    }
  }

  val retrievalType = ProteusType.Topic
  val retrieval = null
  val indexPath = p.getString("index")
  
  // Load pages if they exist
  val pageFile = new File(indexPath, "pages")
  val pageIndex = new PagesReader(new DiskBTreeReader(pageFile))

  // Load words if possible
  val wordFile = new File(indexPath, "words")
  val wordIndex = new WordsReader(new DiskBTreeReader(wordFile))

  override def search(srequest: SearchRequest): List[SearchResult] = {
   return List[SearchResult]() 
  }

  override def lookup(id: AccessIdentifier): ProteusObject = {
    // Try to find matching stuff from the word Index
    val words = wordIndex.getEntry(id.identifier)
    val pages = pageIndex.getEntry(id.identifier)    
    val topic = Topic(words = words, pages = pages)
    var pObject = ProteusObject(id = id,
				title = Some(id.identifier),
				description = Some("An automatically generated topic"),
				topic = Some(topic))
    return pObject
  }

  override def lookup(ids: Set[AccessIdentifier]): List[ProteusObject] = 
    ids.map(lookup(_)).toList
}
