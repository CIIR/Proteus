package ciir.proteus.galago

import java.io.File

import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions._

import ciir.proteus._
import com.twitter.finagle.builder._
import com.twitter.finagle.thrift.ThriftServerFramedCodec
import com.twitter.util.{Duration,Future}
import org.apache.thrift.protocol._

import org.lemurproject.galago.core.retrieval._
import org.lemurproject.galago.core.index.corpus.SnippetGenerator;
import org.lemurproject.galago.tupleflow.Parameters;
import org.apache.thrift.protocol._
import org.apache.thrift.transport._
import org.lemurproject.galago.core.index.disk.DiskBTreeReader
import ciir.proteus._

object WordDateHandler {
  def apply(p: Parameters) = new WordDateHandler(p)
}

class WordDateHandler(p: Parameters) extends Handler(p) {
  val indexPath = p.getString("index")
  
  override def lookup(id: AccessIdentifier): ProteusObject = null
  override def lookup(ids: Set[AccessIdentifier]): List[ProteusObject] = List()
  override def getInfo() : Option[CollectionInfo] = None
}
