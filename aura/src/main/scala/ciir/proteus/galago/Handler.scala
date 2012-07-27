package ciir.proteus.galago

import ciir.proteus._
import org.lemurproject.galago.core.retrieval._
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.core.retrieval.query.StructuredQuery;
import org.lemurproject.galago.tupleflow.Parameters;

object Handler {
  def apply(pType: ProteusType, 
	    parameters: Parameters) : Option[Handler] = {
    return pType match {
      case ProteusType.Page => Some(PageHandler(parameters))
      case ProteusType.Collection => Some(CollectionHandler(parameters))
      case ProteusType.Person => Some(PersonHandler(parameters))
      case ProteusType.Location => Some(LocationHandler(parameters))
      case _ => None
    }    
  }
}

abstract class Handler(val parameters: Parameters) {
  val siteId = parameters.getString("siteId")
  val dummyThumbUrl = "http://ciir.cs.umass.edu/~irmarc/imgs/opera-house-thumb.JPG"
  val dummyImgUrl = "http://ciir.cs.umass.edu/~irmarc/imgs/opera-house.JPG"
  val dummyExtUrl = "http://ciir.cs.umass.edu/~irmarc/etc/placeholder.html"
  def search(srequest : SearchRequest) : List[SearchResult]
  def lookup(ids: Set[AccessIdentifier]) : List[ProteusObject]

  // Returns the correct subset of the results. Query is run,
  // the subset from [offset, offset+count] is returned
  val retrieval : Retrieval
  def runQueryAgainstIndex(srequest: SearchRequest) : Tuple2[Node, Array[ScoredDocument]] = {
    val optionalParams = srequest.parameters
    val (count, offset, lang) = srequest.parameters match {
      case Some(p) => (p.numResultsRequested, p.startAt, p.language)
      case None => (10, 0, "en")
    }
    
    val searchParams = new Parameters
    searchParams.set("count", count+offset)
    val root = StructuredQuery.parse(srequest.rawQuery);
    val transformed : Node = retrieval.transformQuery(root, searchParams);
    val start = System.currentTimeMillis
    printf("Transformed query: %s\n", transformed)
    if (retrieval == null) println("JESUS");
    var scored : Array[ScoredDocument] = null
    try {
      scored = retrieval.runQuery(transformed, searchParams)
    } catch {
      case e => printf("Problem: %s\n", e.getMessage)
    }
    val finish = System.currentTimeMillis
    if (scored == null) scored = Array[ScoredDocument]()
    printf("Whatever\n");
    printf("[q=%s,ms=%d,nr=%d]\n", srequest.rawQuery, (finish-start), scored.length)
    var limit = Math.min(offset + count, scored.length)
    printf("WAT\n");
    return Tuple2(root, scored.slice(offset, limit))
  }
}
