package ciir.proteus

import com.twitter.util.Future

class RemoteRandomDataProvider extends ProteusProvider.FutureIface with FakeDataGenerator {

  override def search(srequest: SearchRequest): Future[SearchResponse] = {
    return null
  }

  override def lookup(lrequest: LookupRequest): Future[LookupResponse] = {
    return null
  }

  override def transform(trequest: TransformRequest): Future[TransformResponse] = {
    return null
  }
}
