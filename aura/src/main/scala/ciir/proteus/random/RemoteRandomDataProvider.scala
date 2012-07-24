package ciir.proteus

import com.twitter.util.Future

class RemoteRandomDataProvider extends ProteusProvider.FutureIface with FakeDataGenerator {

  override def search(srequest: SearchRequest): Future[SearchResponse] = {
    val count = srequest.parameters match {
      case Some(params: RequestParameters) => params.numResultsRequested
      case None => 1
    }
    var results : List[SearchResult] = List[SearchResult]()
    for (ptype <- srequest.types) {
      results = getSearchResults(count, ptype) ::: results
    }
    return Future(SearchResponse(results, None))
  }

  override def lookup(lrequest: LookupRequest): Future[LookupResponse] = {
    var objects = List[ProteusObject]()
    for (aid <- lrequest.ids) {
      objects = getProteusObject(aid) :: objects
    }
    return Future(LookupResponse(objects))
  }

  override def transform(trequest: TransformRequest): Future[TransformResponse] = {
    return null
  }
}
