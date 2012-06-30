package edu.umass.ciir.proteus.triton.core

// A small application to test the average response time
// of a Proteus installation.

import akka.actor.Actor._
import akka.actor.Actor
import akka.dispatch.Future
import scala.collection.JavaConverters._
import edu.umass.ciir.proteus.protocol.ProteusProtocol._

object clientProfilerApp extends App {
  val total_start = System.currentTimeMillis
  
  val client = new LibrarianClient(args(0), args(1).toInt)
  var total_send = 0.0
  // Run some queries...
  for (i <- 0 until 1000) {
    
    val send_start = System.currentTimeMillis
    // Do a query
    val results = client.query("Some random text " + i.toString, 
        client.convertTypes(List("collection", "page", "person", "location", "picture"))).get
    
    // Look up some of the results
    val parallel_requests = results.getResultsList.asScala.slice(0,10).map(result => {
      val lookup = result.getProteusType match {
        case ProteusType.COLLECTION => client.lookupCollection(result)
        case ProteusType.PAGE => client.lookupPage(result)
        case ProteusType.PERSON => client.lookupPerson(result)
        case ProteusType.PICTURE => client.lookupPicture(result)
        case ProteusType.LOCATION => client.lookupLocation(result)
        case _ => Future {None}
      }
      val contents = result.getProteusType match {
        case ProteusType.COLLECTION => client.getContents(result.getId, result.getProteusType, ProteusType.PAGE)
        case ProteusType.PAGE => client.getContainer(result.getId, result.getProteusType, ProteusType.COLLECTION)
        case ProteusType.PERSON => client.getContainer(result.getId, result.getProteusType, ProteusType.PAGE)
        case ProteusType.PICTURE => client.getContainer(result.getId, result.getProteusType, ProteusType.PAGE)
        case ProteusType.LOCATION => client.getContainer(result.getId, result.getProteusType, ProteusType.PAGE)
        case _ => Future {None}
      }
      (lookup, contents)})
    val send_stop = System.currentTimeMillis  
    total_send += (send_stop - send_start)
    parallel_requests.map(r => (r._1.get, r._2.get))
  }
  val total_stop = System.currentTimeMillis
  System.out.println("Start: " + total_start + " Stop: " + total_stop + " Total Time: " + (total_stop - total_start))
  System.out.println("Total time spend sending: " + total_send)
}
