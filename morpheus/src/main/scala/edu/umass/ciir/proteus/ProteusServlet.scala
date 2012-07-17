package edu.umass.ciir.proteus

import org.scalatra._
import scalate.ScalateSupport

class ProteusServlet extends ScalatraServlet with ScalateSupport {
  before() { contentType = "text/html" }
  get("/") { templateEngine.layout("index.scaml") }
  get("/index") { templateEngine.layout("index.scaml") }
  get("/about") { templateEngine.layout("about.scaml") }
  get("/contact") { templateEngine.layout("contact.scaml") }

  get("/search") {     
    templateEngine.layout("search.scaml") 
  }

  notFound {
    // Try to render a ScalateTemplate if no route matched
    findTemplate(requestPath) map { path =>
      contentType = "text/html"
      layoutTemplate(path)
    } orElse serveStaticResource() getOrElse resourceNotFound() 
  }
}
