package edu.umass.ciir.proteus

import ciir.proteus._
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.util.{Duration,Future}
import com.twitter.finagle.thrift.ThriftClientFramedCodec
import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit
import org.apache.thrift.protocol._
import org.scalatra._
import scalate.ScalateSupport
import scala.collection.JavaConversions._

class ProteusServlet extends ScalatraServlet with ScalateSupport {
  val extensionPattern = """\.([a-zA-Z]+)$""".r

  val randomDataService = ClientBuilder()
    .hosts(new InetSocketAddress("ayr.cs.umass.edu", 8888))
    .codec(ThriftClientFramedCodec())
    .hostConnectionLimit(1)
    .tcpConnectTimeout(Duration(1, TimeUnit.SECONDS))
    .retries(2)
    .build()
  val dataClient = new ProteusProvider.FinagledClient(randomDataService)

  def renderHTML(template: String, args: Map[String, Any] = Map[String,Any]()) = {
    contentType = "text/html"
    templateEngine.layout(template, args)
  }

  get("/") { renderHTML("index.scaml") }
  get("/index") { renderHTML("index.scaml") }
  get("/about") { renderHTML("about.scaml") }
  get("/contact") { renderHTML("contact.scaml") }
  get("/search") {
    var actuals = Map[String, Any]() 
    if (params.contains("q")) {
      val results = 
	Map("pages" -> List(ProteusObject("page1"), 
			    ProteusObject("page2"), 
			    ProteusObject("page3")),
	    "books" -> List(ProteusObject("book1"), 
			    ProteusObject("book2"), 
			    ProteusObject("book3"), 
			    ProteusObject("book4"), 
			    ProteusObject("book5")),
	    "people" -> List(ProteusObject("person1"), 
			    ProteusObject("person2"), 
			    ProteusObject("person3"), 
			    ProteusObject("person4")),
	    "locations" -> List(ProteusObject("location1")),
	    "pictures" -> List(ProteusObject("picture1"),
			       ProteusObject("picture2")))
      actuals += Tuple2("q", params("q"))
      actuals += Tuple2("results", results)
    }
    renderHTML("search.scaml", actuals)
  }

  notFound {
    serveStaticResource()
  }
}
