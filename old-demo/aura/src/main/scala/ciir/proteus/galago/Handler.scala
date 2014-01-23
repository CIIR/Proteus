package ciir.proteus.galago

import scala.collection.JavaConversions._
import ciir.proteus._
import org.lemurproject.galago.core.parse.Document
import org.lemurproject.galago.core.parse.PseudoDocument
import org.lemurproject.galago.core.retrieval._
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.core.retrieval.query.StructuredQuery;
import org.lemurproject.galago.tupleflow.Parameters;
import org.lemurproject.galago.core.index.corpus.SnippetGenerator;
import scala.collection.mutable.ListBuffer

object Handler {
  def apply(pType: ProteusType, 
	    parameters: Parameters) : Option[Handler] = {
    return pType match {
      case ProteusType.Page => Some(PageHandler(parameters))
      case ProteusType.Collection => Some(CollectionHandler(parameters))
      case ProteusType.Person => Some(PersonHandler(parameters))
      case ProteusType.Location => Some(LocationHandler(parameters))
      case ProteusType.Miscellaneous => Some(MiscHandler(parameters))
      case ProteusType.Topic => Some(TopicHandler(parameters))
      case ProteusType.Picture => Some(PictureHandler(parameters))
      case _ => None
    }    
  }
}

trait TypedStore {
  val retrievalType : ProteusType
}

trait Searchable extends TypedStore {
  val retrieval : Retrieval

  def search(srequest : SearchRequest) : List[SearchResult]

  def getInfo : Option[CollectionInfo] = {
    val stats = retrieval.getRetrievalStatistics();
    val parts = retrieval.getAvailableParts.getKeys().filter {
      partName =>
	partName.startsWith("field.") && (partName.indexOf("porter") == -1)
    }.map {
      partName => 
	partName.replace("field.","")
	
    }
    return Some(CollectionInfo(`type` = retrievalType,
			       numDocs = stats.documentCount,
			       vocabSize = stats.vocabCount,
			       numTokens = stats.collectionLength,
			       fields = parts.toList))
  }

  def getDocument(aid: AccessIdentifier) : Option[Document] = {
    val c = new Parameters;
    c.set("terms", true);
    c.set("tags", true);
    val d = retrieval.getDocument(aid.identifier, c)
    if (d == null)
      None
    else
      Some(d)
  }

  // Returns the correct subset of the results. Query is run,
  // the subset from [offset, offset+count] is returned
  def runQueryAgainstIndex(srequest: SearchRequest) : Tuple2[Node, Array[ScoredDocument]] = {
    val optionalParams = srequest.parameters
    val (count, offset, lang) = srequest.parameters match {
      case Some(p) => (p.numResultsRequested, p.startAt, p.language)
      case None => (10, 0, "en")
    }
    
    val searchParams = new Parameters
    searchParams.set("mu", 8000D)
    searchParams.set("odw",0.21D)
    searchParams.set("uniw", 0.29D)
    searchParams.set("uww", 0.50D)
    searchParams.set("count", count+offset)
       
    val galagoQuery =  srequest.rawGalagoQuery match {
      case None => rawQueryToGalagoQuery(srequest.rawQuery)
      case Some(result) => result
    }
   
    val root = StructuredQuery.parse(galagoQuery);
    val transformed : Node = retrieval.transformQuery(root, searchParams);
    val start = System.currentTimeMillis
    var scored : Array[ScoredDocument] = null
    scored = retrieval.runQuery(transformed, searchParams)
    val finish = System.currentTimeMillis
    if (scored == null) scored = Array[ScoredDocument]()
    printf("[%s,q=%s,ms=%d,nr=%d]\n", 
	   retrievalType.name,
	   srequest.rawQuery, 
	   (finish-start), 
	   scored.length)
    var limit = Math.min(offset + count, scored.length)
    return Tuple2(transformed, scored.slice(offset, limit))
  }
  
  def rawQueryToGalagoQuery(request:String) : String = {
          val normalizedTokens = new ListBuffer[String]()
          val cleanQuery = request.replace("-", " ").toLowerCase.replaceAll("[^a-z01-9 ]", " ").replaceAll("\\s+", " ").trim
          val tokens = cleanQuery.split("\\s+")
          for (term <- tokens) {
              if (term.length() > 0) {
                  normalizedTokens.add(term);
              }
          }
         val normalizedQuery = "#seqdep(" + normalizedTokens.mkString(" ") + ")"
         normalizedQuery
    }
}

abstract class Handler(val parameters: Parameters) {
  val siteId = parameters.getString("siteId")
  val dummyThumbUrl = "assets/img/no_image.gif"
  val dummyImgUrl = "assets/img/no_image.gif"
  val generator = new SnippetGenerator

  // Defined by the subclasses
  def lookup(ids: Set[AccessIdentifier]) : List[ProteusObject]
  def lookup(id: AccessIdentifier) : ProteusObject
  def getInfo() : Option[CollectionInfo]

  
   def getThumb(wikiEntity:Option[WikipediaEntity]) : Option[String] = {
        wikiEntity match {
        case None => Some(dummyThumbUrl)
        case Some(r) => {
          val imgs = r.imageLinks
          if (imgs == None) {
            Some(dummyThumbUrl)
          } else {
            if (imgs.size > 0) {
              Some(imgs.head)
            } else {
              Some(dummyThumbUrl)
            }
          }
        }
      }
      }
  
  def getDisplayTitle(document : Document, queryTerms: Set[String]) : String = {
    var title = if (document.metadata.containsKey("title")) {
      document.metadata.get("title")
    } else {
      String.format("%s", document.name);
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
      String.format("%s", document.name)
    }    
  }

  def getSummary(document : Document, query: Set[String]) : String = {
//    if (document.metadata.containsKey("description")) {
//      val description = document.metadata.get("description");
//      if (description.length() > 10) {
//        return generator.highlight(description, query);
//      }
//    }
    document match {
    case pd:PseudoDocument => {
      val pdsamples = pd.samples
      val subset = pd.samples.toList.take(50)
      val textSubset = subset.map(s => s.content).mkString(" ")
      if (pdsamples.size() > 0) {
        val result = generator.getSnippet(textSubset, query)
        result
      } else {
        ""
      }
    }
    case sd:Document => generator.getSnippet(sd.text, query)
  }
}

def extractContexts(d: Document) : List[KeywordsInContext] = 
  d match {
    case pseudo: PseudoDocument => pseudo.samples.toList.take(50).map {
  sample => KeywordsInContext(id = AccessIdentifier(identifier = sample.source,
                            `type` = ProteusType.Page,
                            resourceId = siteId),
                  textContent = sample.content,
                  externalId = sample.externalLink)
    }
    case simple: Document => List()
  }
}


