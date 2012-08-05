package ciir.proteus.galago

import java.io.File

import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions._

import ciir.proteus._
import com.twitter.util.Future
import org.apache.thrift.protocol._
import org.lemurproject.galago.tupleflow.Parameters;
import org.lemurproject.galago.tupleflow.Utility;
import org.apache.thrift.protocol._
import org.apache.thrift.transport._
import org.lemurproject.galago.core.index.disk.DiskBTreeReader
import ciir.proteus._

object PictureHandler {
  def apply(p: Parameters) = new PictureHandler(p)
}

class PictureHandler(p: Parameters) extends Handler(p) 
with TypedStore {  
  class PagesToPictures(index: DiskBTreeReader) {
    def getEntry(key: String) : Option[PictureList] = {
      if (index == null) return None
      val iterator = index.getIterator(Utility.fromString(key))
      if (iterator == null) return None
      
      val transport = new TMemoryInputTransport(iterator.getValueBytes)
      val protocol = factory.getProtocol(transport)
      return Some(PictureList.decoder(protocol))
    }
  }

  class IdsToPictures(index: DiskBTreeReader) {
    def getEntry(key: String) : Option[Picture] = {
      val iterator = index.getIterator(Utility.fromString(key))
      if (iterator == null) return None
      val transport = new TMemoryInputTransport(iterator.getValueBytes)
      val protocol = factory.getProtocol(transport)
      return Some(Picture.decoder(protocol))
    }
  }

  val factory = new TCompactProtocol.Factory
  val retrievalType = ProteusType.Picture
  val indexPath = p.getString("index")
  
  def scorePictures(pageResults: List[SearchResult]) : List[SearchResult] = {
    List()
  }

  override def lookup(id: AccessIdentifier): ProteusObject = ProteusObject(id)
  override def lookup(ids: Set[AccessIdentifier]): List[ProteusObject] = List()
  override def getInfo() : CollectionInfo = CollectionInfo(`type` = ProteusType.Miscellaneous,
							   numDocs = 0,
							   vocabSize = 0,
							   numTokens = 0,
							   fields = List())
}
