package edu.umass.ciir.proteus.triton.core

/**
* Abstract base class which contains abstract methods for all the methods that a data store (library/endpoint) trait 
* needs to implement.
*/

import akka.dispatch._
import akka.actor.Actor._
import akka.actor.Actor
import edu.umass.ciir.proteus.protocol.ProteusProtocol._
import scala.collection.JavaConverters._

abstract trait EndPointDataStore {
  
  /** Methods Used Here and Elsewhere (MUST BE PROVIDED) **/
  def getResourceKey : String
  def getSupportedTypes : List[ProteusType]
  
  /** Core Functionality Methods (MUST BE PROVIDED) **/
  def initialize(configuration: EndPointConfiguration) {}
  def supportsType(ptype: ProteusType) : Boolean
  def runSearch(s: Search) : SearchResponse = unsupportedResponse("Search")
  
  def runContainerTransform(transform: ContainerTransform) : SearchResponse = unsupportedResponse("Container Transform")  	
  def runContentsTransform(transform: ContentsTransform) : SearchResponse = unsupportedResponse("Contents Transform")  	
  def runOverlapsTransform(transform: OverlapsTransform) : SearchResponse = unsupportedResponse("Overlaps Transform")  	
  def runOccurAsObjTransform(transform: OccurAsObjTransform) : SearchResponse = unsupportedResponse("Occurrences as Object") 	
  def runOccurAsSubjTransform(transform: OccurAsSubjTransform) : SearchResponse  = unsupportedResponse("Occurrences as Subject") 	
  def runOccurHasObjTransform(transform: OccurHasObjTransform) : SearchResponse = unsupportedResponse("Occurrences having Object")  	
  def runOccurHasSubjTransform(transform: OccurHasSubjTransform) : SearchResponse  = unsupportedResponse("Occurrences having Subject")  	
  def runNearbyLocationsTransform(transform: NearbyLocationsTransform) : SearchResponse  = unsupportedResponse("Nearby Locations")
  def lookup(accessID: AccessIdentifier, proteusType: ProteusType) : ProteusObject = {
    return ProteusObject.newBuilder
	    .setId(AccessIdentifier.newBuilder
    		   .setIdentifier(accessID.getIdentifier)
    		   .setResourceId(getResourceKey)
    		   .setError("error: lookup not supported by " + getResourceKey)
    		   .build)
	    .build
  }

  // Canned error messages for generic unsupported operations/types
  protected def unsupportedResponse(method: String): SearchResponse = 
    SearchResponse.newBuilder.setError("Unsupported by end point: " + method).build

  protected def errorResponse(errorText: String): SearchResponse = 
    SearchResponse.newBuilder.setError("EndPoint Error: " + errorText).build
}
