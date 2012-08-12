package ciir.proteus.galago

import java.io.File
import scala.collection.mutable.MapBuilder
import scala.collection.immutable.HashMap
import scala.collection.JavaConversions._
import ciir.proteus._
import com.twitter.finagle.builder._
import com.twitter.finagle.thrift.ThriftServerFramedCodec
import com.twitter.util.{Duration,Future}
import org.apache.thrift.protocol._
import org.lemurproject.galago.core.retrieval._
import org.lemurproject.galago.core.parse.Document
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.core.retrieval.query.StructuredQuery;
import org.lemurproject.galago.core.retrieval.RetrievalFactory
import org.lemurproject.galago.core.index.corpus.SnippetGenerator;
import org.lemurproject.galago.tupleflow.Parameters;

// This class handles:
// 1) Receiving requests
// 2) Fanning those requests out to the appropriate hooked up retrieval servers
// 3) Collecting the responses from each server and composing them as a single 
//    response to the sender
// 4) reports status on the health of the component indexes
class GalagoAdapter(parameters: Parameters) extends ProteusProvider.FutureIface {
  // Construction
  val siteIdentifier = parameters.getString("siteId")
  val handlersSection = parameters.getMap("handlers")
  val hBuilder = Map.newBuilder[ProteusType, Handler]
  for (key <- handlersSection.getKeys) {
    try {
      val handlerParameters = handlersSection.getMap(key)
      handlerParameters.set("siteId", siteIdentifier)      
      ProteusType.valueOf(key) match {
	case None => System.err.printf("'%s' is not a valid handler key.\n", key)
	case Some(pKey: ProteusType) => {
	  Handler(pKey, handlerParameters) match {
	    case Some(h: Handler) => {
	      hBuilder += (pKey -> h)
	      System.out.printf("Loaded handler '%s'\n", key)
	    }
	    case None => System.err.printf("Not handling '%s' yet.\n", key)
	  }
	}
      }
    } catch {
      case e => System.err.printf("Unable to load handler '%s': %s\n",
				  key,
				  e.getMessage)
    }
  }
  val handlerMap: Map[ProteusType, Handler] = hBuilder.result
  val handlerKeys = handlerMap.keySet
  val links = LinkProvider(parameters)
  val dates = WordDateProvider(parameters)
  //  End Construction

  override def search(srequest: SearchRequest): Future[SearchResponse] = {
    val activeKeys = handlerKeys & srequest.`types`.toSet
    val resultsSet = activeKeys.map { 
      key: ProteusType => handlerMap(key) match {
	case s:Searchable => {
	  var results = s.search(srequest)
	  if (activeKeys(ProteusType.Picture) &&
	      key == ProteusType.Page) {
	    val picHandler : PictureHandler = handlerMap(ProteusType.Picture).asInstanceOf[PictureHandler]
	    (results ++ picHandler.scorePictures(results)).toList
	  } else {
	    results
	  }
	}
	case _ => List()
      }
    }
    val resultList = resultsSet.reduceLeft { (A, B) => A ++ B }.toList
    return Future(SearchResponse(results = resultList, error = None))
  }

  override def lookup(lrequest: LookupRequest): Future[LookupResponse] = {
    val foundObjects : Set[List[ProteusObject]]= handlerKeys.map {
      key: ProteusType => {
	val typedLookupRequests = lrequest.ids.filter { 
	  id: AccessIdentifier =>
	    id.`type` == key
	}
	handlerMap(key).lookup(typedLookupRequests.toSet)
      }
    }
    val objectList = foundObjects.reduceLeft { (A, B) => A ++ B }.toList
    return Future(LookupResponse(objects = objectList))
  }

  override def transform(trequest: TransformRequest): Future[TransformResponse] = {
    val accessIds = links.getTargetIds(trequest.referenceId,
				       trequest.targetType.get).take(50)
    printf("found %d links\n", accessIds.size)
    val objects = 
      accessIds.filter((A) => handlerMap.contains(A.`type`)).map { 
	aid =>
	  handlerMap(aid.`type`).lookup(aid)
      }.filter {
	obj =>
	  obj != null
      }    
    return Future(TransformResponse(objects))
  }

  override def status : Future[StatusResponse] = {
    val colInfo = handlerMap.values.map {
      handler =>
	handler.getInfo
    }.toList.filter(_.isDefined).map(_.get)
    val linkInfo = links.getInfo
    val topicInfo = handlerMap(ProteusType.Topic).asInstanceOf[TopicHandler].getTopicInfo
    val resp = StatusResponse(siteId = siteIdentifier,
			      collectionData = colInfo,
			      linkData = linkInfo,
			      topicData = topicInfo)
    return Future(resp)
  }


  // Performs a "search" on behalf of the previous results given.
  override def related(rrequest: RelatedRequest) : Future[SearchResponse] = {
    var acc = HashMap[AccessIdentifier, Double]()
    try {
      for (belief <- rrequest.beliefs; targetType <- rrequest.targetTypes) {
	if (!((belief.id.`type` == ProteusType.Collection && targetType == ProteusType.Collection) 
              || (belief.id.`type` == ProteusType.Page && targetType == ProteusType.Page))) {
		val targetIds = links.countOccurrences(belief.id, targetType)
		for ((tid, count) <- targetIds) {
		  if (!acc.containsKey(tid)) {
		    acc += (tid -> (count * belief.score))
		  } else {
		    val newscore = acc(tid) + (count * belief.score)
		    acc += (tid -> newscore)
		  }
		}
	      }
      }
    } catch {
      case e => { e.printStackTrace() }
    }
    val sortedResults = acc.toList.sortWith((A,B) => A._2 > B._2).take(50)
    val optionalResults : List[Option[SearchResult]] = sortedResults.map { 
      A => {
	val aid = A._1
	val score = A._2
	if (!handlerMap.containsKey(aid.`type`)) {
	  None
	} else {
	  val handler = handlerMap(aid.`type`)
	  try {
	    val pObject = handler.lookup(aid)
	    Some(proteusObjectToSearchResult(pObject, score))
	  } catch {
            case e => { e.printStackTrace()}
	    None
	  }
	}
      }
    }
    val finalResults = optionalResults.filter(_.isDefined).map(_.get)
    return Future(SearchResponse(results = finalResults))
  }

  def proteusObjectToSearchResult(pObj: ProteusObject, score: Double) : SearchResult =
    SearchResult(id = pObj.id, 
		 score = score,
		 title = pObj.title,
		 imgUrl = pObj.imgUrl,
		 thumbUrl = pObj.thumbUrl,
		 externalUrl = pObj.externalUrl,
		 summary = Some(ResultSummary(text = pObj.description.getOrElse("No summary"), 
					      highlights = List())))

  override def wordFrequencies(words: Seq[String]) : Future[Map[String, LongValueList]] = {
    val optionals = words.map(w => (w -> dates.lookup(w))).filter(_._2.isDefined)
    var rBuilder = Map.newBuilder[String, LongValueList]
    for (T <- optionals) { rBuilder += (T._1 -> T._2.get) }
    Future(rBuilder.result)
  }
}
