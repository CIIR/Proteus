package ciir.proteus

import com.twitter.finagle.builder.ClientBuilder
import com.twitter.util.{Duration,Future}
import com.twitter.finagle.thrift.ThriftClientFramedCodec
import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit
import org.apache.thrift.protocol._
import org.scalatra._
import scalate.ScalateSupport
import scala.collection.JavaConversions._
import org.lemurproject.galago.tupleflow.Parameters

class ProteusServlet extends ScalatraServlet 
with ScalateSupport 
with FakeDataGenerator {
  val kReturnableTypes = List("page", "collection", "picture", "person", "location")
  val kNumSearchResults = 10

  val parameters = new Parameters()

  val dataService = ClientBuilder()
  .hosts(new InetSocketAddress(parameters.get("host", "ayr.cs.umass.edu"),
			       parameters.get("port", 8101).toInt))
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
  get("/lookup-single") {
    contentType = "text/html"
    "This will contain content for" + multiParams("pid").toString
  }
  get("/lookup") {
    val accessIds = multiParams("pid") map { pid => decrypt(pid) }
    val request = LookupRequest(accessIds)
    val futureResponse = dataClient.lookup(request)
    val response = futureResponse()
    // Need to split the results by type
    var splitResults = Map[String, AnyRef]()
    for (typeStr : String <- kReturnableTypes) {
      val filteredByType = response.results.filter { 
	result : SearchResult => 
	  result.id.`type` == ProteusType.valueOf(typeStr).get
      }
      // If we found any results of that type in the filter,
      // then add it as a typed result list.
      if (filteredByType.length > 0) {
	splitResults += (typeStr -> filteredByType)
      }
    }
    actuals += ("results" -> splitResults)
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
      var splitResults = Map[String, AnyRef]()
      for (typeStr : String <- kReturnableTypes) {
	val filteredByType = response.results.filter { 
	  result : SearchResult => 
	    result.id.`type` == ProteusType.valueOf(typeStr).get
	}
	// If we found any results of that type in the filter,
	// then add it as a typed result list.
	if (filteredByType.length > 0) {
	  splitResults += (typeStr -> filteredByType)
	}
      }
      actuals += ("results" -> splitResults)
      actuals += ("q" -> params("q"))
    }
    renderHTML("search.scaml", actuals)
  }

  notFound {
    serveStaticResource()
  }
}
