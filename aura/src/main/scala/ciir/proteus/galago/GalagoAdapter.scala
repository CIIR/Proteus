package ciir.proteus.galago

import java.io.File
import scala.collection.mutable.MapBuilder
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
// 4) (TODO) reports status on the health of the component indexes
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
  //  End Construction

  override def search(srequest: SearchRequest): Future[SearchResponse] = {
    val activeKeys = handlerKeys & srequest.`types`.toSet
    val resultsSet = activeKeys.map { 
      key: ProteusType => 
	handlerMap(key).search(srequest)
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
    return null
  }
}
