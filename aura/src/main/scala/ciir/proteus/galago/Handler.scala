package ciir.proteus.galago

import scala.collection.JavaConversions._
import ciir.proteus._
import org.lemurproject.galago.core.parse.Document
import org.lemurproject.galago.core.retrieval._
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.core.retrieval.query.StructuredQuery;
import org.lemurproject.galago.tupleflow.Parameters;
import org.lemurproject.galago.core.index.corpus.SnippetGenerator;

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
  val generator = new SnippetGenerator

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
    var scored : Array[ScoredDocument] = null
    scored = retrieval.runQuery(transformed, searchParams)
    val finish = System.currentTimeMillis
    if (scored == null) scored = Array[ScoredDocument]()
    printf("[q=%s,ms=%d,nr=%d]\n", srequest.rawQuery, (finish-start), scored.length)
    var limit = Math.min(offset + count, scored.length)
    return Tuple2(root, scored.slice(offset, limit))
  }

  def getDisplayTitle(document : Document, queryTerms: Set[String]) : String = {
    var title = if (document.metadata.containsKey("title")) {
      document.metadata.get("title")
    } else {
      String.format("No Title (%s)", document.name);
    } 
    if (title.length > 60) {
      title = String.format("%s ...", title.take(60))
    }
    generator.highlight(title, queryTerms);
  }

  def getTitle(document : Document) : String = {
    if (document.metadata.containsKey("title")) {
      document.metadata.get("title");
    } else {
      String.format("No Title (%s)", document.name)
    }    
  }

  def getSummary(document : Document, query: Set[String]) : String = {
    if (document.metadata.containsKey("description")) {
      val description = document.metadata.get("description");
      if (description.length() > 10) {
        return generator.highlight(description, query);
      }
    }
    return generator.getSnippet(document.text, query);
  }
}


