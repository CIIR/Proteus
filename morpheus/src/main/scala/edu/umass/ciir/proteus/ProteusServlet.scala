package edu.umass.ciir.proteus

import org.scalatra._
import scalate.ScalateSupport
import scala.collection.JavaConversions._

class ProteusServlet extends ScalatraServlet with ScalateSupport {
  val extensionPattern = """\.([a-zA-Z]+)$""".r
  def renderHTML(template: String, args: Map[String, Any] = Map[String,Any]()) = {
    contentType = "text/html"
    templateEngine.layout(template, args)
  }

/*
  before("""\.(jpg|jpeg)$""".r) { contentType = "image/jpeg" }
  before("""\.gif$""".r) { contentType = "image/gif" }
  before("""\.png$""".r) { contentType = "image/png" }
  before("""\.css$""".r) { contentType = "text/stylesheet" }
  before("""\.js$""".r) { contentType = "text/javascript" }
*/
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
