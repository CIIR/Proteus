package edu.umass.ciir.proteus

import org.scalatra._
import scalate.ScalateSupport
import scala.collection.JavaConversions._

class ProteusServlet extends ScalatraServlet with ScalateSupport {
  before() { contentType = "text/html" }
  get("/") { templateEngine.layout("index.scaml") }
  get("/index") { templateEngine.layout("index.scaml") }
  get("/about") { templateEngine.layout("about.scaml") }
  get("/contact") { templateEngine.layout("contact.scaml") }

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
    templateEngine.layout("search.scaml", actuals)
  }

  notFound {
    // Try to render a ScalateTemplate if no route matched
    findTemplate(requestPath) map { path =>
      contentType = "text/html"
      layoutTemplate(path)
    } orElse serveStaticResource() getOrElse resourceNotFound() 
  }
}
