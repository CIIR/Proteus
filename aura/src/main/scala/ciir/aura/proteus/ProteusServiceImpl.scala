package ciir.aura.proteus

import com.twitter.util.Future

class ProteusServiceImpl extends ProteusProvider.FutureIface {

  override def search(srequest: SearchRequest): Future[SearchResponse] = { }

  override def lookup(lrequest: LookupRequest): Future[LookupResponse] = { }

  override def transform(trequest: TransformRequest): Future[TransformResponse] = { }

}
