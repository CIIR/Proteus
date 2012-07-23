package ciir.proteus.galago

import ciir.proteus._
import com.twitter.finagle.builder._
import com.twitter.finagle.thrift.ThriftServerFramedCodec
import org.apache.thrift.protocol._

import org.lemurproject.galago.tupleflow.Parameters

object GalagoAdapter

class GalagoAdapter(parameters: Parameters) extends ProteusProvider.FutureIface {
  val indexPath = parameters.getString("index")

  // Need to load the index and then hook it up for the operations

  override def search(srequest: SearchRequest): SearchResponse = {
    return null
  }

  override def lookup(lrequest: LookupRequest): Future[LookupResponse] = {
    return null
  }

  override def transform(trequest: TransformRequest): Future[TransformResponse] = {
    return null
  }
}
