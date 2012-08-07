package ciir.proteus

import ciir.proteus.ProteusFunctions._
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.util.{Duration,Future}
import com.twitter.finagle.thrift.ThriftClientFramedCodec
import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit
import org.apache.thrift.protocol._
import org.scalatra._
import scalate.ScalateSupport
import scala.collection.JavaConversions._
import scala.collection.mutable.MapBuilder
import org.lemurproject.galago.tupleflow.Parameters
import java.util.ArrayList

object ProteusServlet {			      
  var parameters: Parameters = new Parameters()
}

class ProteusServlet extends ScalatraServlet 
with ScalateSupport 
with FakeDataGenerator {
  import ProteusServlet._

  val kNumSearchResults = 10
  // Load the first server parameters 
  val auraServer = ProteusServlet.parameters.getAsList("servers").asInstanceOf[ArrayList[Parameters]].first
  if(!auraServer.containsKey("host") || !auraServer.containsKey("port")) {
    println("Please provide both a host and port key value for each server listed in servers list in parameters.")
    System.exit(1)
  }

  val dataService = ClientBuilder()
  .hosts(new InetSocketAddress(auraServer.getString("host"),
			       auraServer.getLong("port").toInt))
  .codec(ThriftClientFramedCodec())
  .hostConnectionLimit(1)
  .tcpConnectTimeout(Duration(1, TimeUnit.SECONDS))
  .retries(2)
  .build()
  val dataClient = new ProteusProvider.FinagledClient(dataService)

  def renderHTML(template: String, args: Map[String, Any] = Map[String,Any]()) = {
    contentType = "text/html"
    templateEngine.layout(template, args)
  }

  get("/") { renderHTML("index.scaml") }
  get("/index") { renderHTML("index.scaml") }
  get("/about") { renderHTML("about.scaml") }
  get("/contact") { renderHTML("contact.scaml") }
  get("/details") {
    val aid = ProteusFunctions.externalId(params("id"))
    val request = LookupRequest(List(aid))
    val futureResponse = dataClient.lookup(request)
    val response = futureResponse()
    val obj = response.objects.head
    val actuals = Map[String, Any]("pObject" -> obj)
    renderHTML("details.scaml", actuals)
  }

  get("/status") {
    val response = dataClient.status()()
    printf("linkdata: %s\n", response.linkData.toString)
    renderHTML("status.scaml", Map("siteId" -> response.siteId,
				   "collectionData" -> response.collectionData,
				   "linkData" -> response.linkData))
  }

  post("/related") {
    val beliefs = multiParams("score").map {
      scoreElement => {
	val Array(did, sc) = scoreElement.split(",")
	val aid = externalId(did)
	SearchResult(id = aid, score = sc.toDouble)
      }
    }
    val targetTypes = multiParams("targetType").map {
      tElem =>
	ProteusType.valueOf(tElem)
    }.filter(_.isDefined).map(_.get)

    val rrequest = RelatedRequest(beliefs = beliefs,
				  targetTypes = targetTypes)
    val response = dataClient.related(rrequest)()
    renderHTML("search.scaml", Map("results" -> splitResults(response.results)))
  }

  get("/transform") {
    val transformType = TransformType(params("tv").toInt)
    val srcAid = externalId(params("did"))
    val targetType = ProteusType.valueOf(params("t"))
    val trequest = TransformRequest(transformType = transformType,
				    referenceId = srcAid,
				    targetType = targetType)
    val response = dataClient.transform(trequest)()
    val objects = response.objects.toList
    renderHTML("viewobjects.scaml", Map("pObjects" -> objects))
  }

  get("/lookup") {
    val accessIds = multiParams("id") map { 
      pid => 
	ProteusFunctions.externalId(pid)
    }
    val request = LookupRequest(accessIds)
    val response = dataClient.lookup(request)()
    // Need to split the results by type
    var splitResults = Map[String, AnyRef]()
    for (typeStr : String <- kReturnableTypes) {
      val filteredByType = response.objects.filter { 
	obj : ProteusObject => 
	  obj.id.`type` == ProteusType.valueOf(typeStr).get
      }
      // If we found any results of that type in the filter,
      // then add it as a typed result list.
      if (filteredByType.length > 0) {
	splitResults += (typeStr -> filteredByType)
      }
    }
    var actuals = Map[String, Any]("result" -> splitResults)
    renderHTML("lookup.scaml", actuals)
  }

  get("/search") {
    var actuals = Map[String, Any]() 
    
    // If we have a query, put together a SearchRequest and ship it
    if (params.contains("q")) {
      // Request this many
      val count = kNumSearchResults
      val requestedTypes = if (multiParams("st").contains("all")) {
	kReturnableTypes.map { rt : String => ProteusType.valueOf(rt).get }
      } else {
	multiParams("st") map { 
	  str => 
	    ProteusType.valueOf(str).get
	}
      }
      val parameters = RequestParameters(count, 0)
      val request = SearchRequest(rawQuery = params("q"), 
				  types = requestedTypes, 
				  parameters = Some(parameters))

      val futureResponse = dataClient.search(request)
      val response = futureResponse()
      // Need to split the results by type
      actuals += ("results" -> splitResults(response.results))
      actuals += ("q" -> params("q"))
    }
    renderHTML("search.scaml", actuals)
  }

  notFound {
    serveStaticResource()
  }

  def splitResults(results: Seq[SearchResult]) : Map[String, AnyRef] = {
    val splitBuilder = Map.newBuilder[String, AnyRef]
    for (typeStr : String <- kReturnableTypes) {
      val filteredByType = results.filter { 
	result => 
	  result.id.`type` == ProteusType.valueOf(typeStr).get
      }
      // If we found any results of that type in the filter,
      // then add it as a typed result list.
      if (filteredByType.length > 0) {
	splitBuilder += (typeStr -> filteredByType)
      }
    }
    return splitBuilder.result
  }
}
