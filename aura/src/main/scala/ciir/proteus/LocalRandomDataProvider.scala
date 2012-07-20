package ciir.proteus

class LocalRandomDataProvider extends ProteusProvider.Iface with FakeDataGenerator {

  override def search(srequest: SearchRequest): SearchResponse = {
    return null
  }

  override def lookup(lrequest: LookupRequest): LookupResponse = {
    return null
  }

  override def transform(trequest: TransformRequest): TransformResponse = {
    return null
  }
}

