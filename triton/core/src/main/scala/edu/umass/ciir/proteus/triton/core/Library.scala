package edu.umass.ciir.proteus.triton.core

import akka.dispatch._
import akka.actor.Actor._
import akka.actor.Actor
import akka.config.Supervision._
import scala.collection.JavaConverters._

import edu.umass.ciir.proteus.protocol.ProteusProtocol._



/** 
 * Traits for building a fairly basic library (end point). 
 * All that it requires is an implementation of EndPointDataStore 
 */

/**
 * Trait for providing connection management for the end points of the system.
 * It is mainly responsible for sending a connection message to the librarian.
 */
trait EndPointConnectionManagement { this: Actor =>
  
    val serverGroupID : String
    def serverHostname : String
	def serverPort : Int
	def proteus_service_name : String
     def getSupportedTypes : List[ProteusType]
  	 def getDynamicTransforms : List[DynamicTransformID]
    
    var connection : ConnectLibrary // This gets set elsewhere
	
    // Build the connectlibrary object we will send to the librarian
	def buildConnection : ConnectLibrary = {
      return ConnectLibrary.newBuilder
    		  	.setHostname(serverHostname)
    		  	.setPort(serverPort)
    		  	.setGroupId(serverGroupID)
    		  	.addAllSupportedTypes(getSupportedTypes.asJava)
    		  	.addAllDynamicTransforms(getDynamicTransforms.asJava)
    		  	.build
    }
    
    /** Get the resource key/id for this library */
     def getResourceKey : String = connection.getRequestedKey  
    /** Connect to the specified librarian */
    protected def connectToLibrarian(hostName: String, port: Int) {
		val librarian = remote.actorFor(proteus_service_name, hostName, port)
		librarian ! connection
    }
  	/** Prepare a search response for sending to the client by adding our resource id to it */
  	protected def prepareToSend(response: SearchResponse) : SearchResponse = {
      val builder = response.toBuilder
      builder.getResultsList.asScala.foreach(r => r.toBuilder.getIdBuilder.setResourceId(getResourceKey))
      return builder.build
    }
  	/** Handle messages related to connecting to the librarian (or if we were to act as a secondary librarian) */
	protected def connectionManagement : Receive = {
    	case c: LibraryConnected =>
    		// Check that we connected successfully, and without errors
    	    if (c.hasError)	println("ERROR Connecting to Librarian: " + c.getError)
    	    else { 
    	    	// Update our key (the one received here will always be the same as the existing non-trivial key)
    	        connection = connection.toBuilder.setRequestedKey(c.getResourceId).build
    	        println("Connected to librarian, assigned ID: " + getResourceKey)
    	    }
  	}
}

/**
 * Trait providing query handling for end points. This trait crucially 
 * depends on the methods starting: run....Transform, and runSearch. They must 
 * be implemented elsewhere.
 */
trait EndPointQueryManagement { this: Actor =>
    /**
     * If your end point does not support direct searches, 
     * then return empty results and no error.
     * 
     * If your end point does support searching, but not over 
     * ANY of the requested types, then return empty results and an error message.
     */
  	 def runSearch(s: Search) : SearchResponse
  	
  	/**
  	 * For the transformation methods:
  	 * 		If the type is not supported by your end point, then return 
  	 * 		with no results (empty list) and an error message set.
  	 * 
  	 * 		If the type is supported but that operation is not (either in general or just for that type)
  	 * 		then return no results and no error message.
  	 */
  	 def runContainerTransform(transform: ContainerTransform) : SearchResponse
  	 def runContentsTransform(transform: ContentsTransform) : SearchResponse
  	 def runOverlapsTransform(transform: OverlapsTransform) : SearchResponse 
  	 def runOccurAsObjTransform(transform: OccurAsObjTransform) : SearchResponse 
  	 def runOccurAsSubjTransform(transform: OccurAsSubjTransform) : SearchResponse 
  	 def runOccurHasObjTransform(transform: OccurHasObjTransform) : SearchResponse 
  	 def runOccurHasSubjTransform(transform: OccurHasSubjTransform) : SearchResponse 
  	 def runNearbyLocationsTransform(transform: NearbyLocationsTransform) : SearchResponse 
  	
  	/**
  	 * If you get a dynamic transform that you do not support, return with no results 
  	 * and an error message. (Because would be a real bug)
  	 */
  	 def runDynamicTransform(transform: DynamicTransform) : SearchResponse 
  	protected def prepareToSend(response: SearchResponse) : SearchResponse
    
  	/**
  	 * Handle the messages relating to queries and transforms.
  	 * 
  	 * NOTE: There is a bug in Akka 1.2 (maybe also 1.3) that causes problems for 
  	 * remote actors doing forwards, as well as trying to do a reply in a Future. So, 
  	 * we have a work around implemented that should be removed when upgraded.
  	 */
  	protected def queryManagement : Receive = {
  	  // All of these simply call the method, modify the response, and then send it back out
    	case s: Search => 
    	  	val chan = self.channel // This is a workaround until the bug in Akka 1.2 is fixed
    		Future { prepareToSend(runSearch(s)) } onResult { case r: SearchResponse => chan ! r}
    	
    	case trans: ContainerTransform => 
    	  	val chan = self.channel
    		Future { prepareToSend(runContainerTransform(trans)) } onResult { case r: SearchResponse => chan ! r }
        
    	case trans: ContentsTransform => 
    	  	val chan = self.channel
    		Future { prepareToSend(runContentsTransform(trans)) } onResult { case r: SearchResponse => chan ! r }
        
    	case trans: OverlapsTransform => 
    	  	val chan = self.channel
    		Future { prepareToSend(runOverlapsTransform(trans)) } onResult { case r: SearchResponse => chan ! r }
        
    	case trans: OccurAsObjTransform => 
    	  	val chan = self.channel
    		Future { prepareToSend(runOccurAsObjTransform(trans)) } onResult { case r: SearchResponse => chan ! r }
        
    	case trans: OccurAsSubjTransform => 
    	  	val chan = self.channel
    		Future { prepareToSend(runOccurAsSubjTransform(trans)) } onResult { case r: SearchResponse => chan ! r }
        
    	case trans: OccurHasObjTransform => 
    	  	val chan = self.channel
    		Future { prepareToSend(runOccurHasObjTransform(trans)) } onResult { case r: SearchResponse => chan ! r }
        
    	case trans: OccurHasSubjTransform => 
    	  	val chan = self.channel
    		Future { prepareToSend(runOccurHasSubjTransform(trans)) } onResult { case r: SearchResponse => chan ! r }
        
    	case trans: NearbyLocationsTransform => 
    	  	val chan = self.channel
    		Future { prepareToSend(runNearbyLocationsTransform(trans)) } onResult { case r: SearchResponse => chan ! r }
        
    	case dtrans: DynamicTransform => 
    	  	val chan = self.channel
    	  	Future { prepareToSend(runDynamicTransform(dtrans)) } onResult { case r: SearchResponse => chan ! r }
  	}
}

/**
 * Trait for providing lookup management for the end point.
 * Relies crucially on the lookup... methods that must be implemented elsewhere.
 */
trait EndPointLookupManagement { this: Actor =>
  
     def lookupCollection(accessID: AccessIdentifier) : Collection
     def lookupPage(accessID: AccessIdentifier) : Page
     def lookupPicture(accessID: AccessIdentifier) : Picture
     def lookupVideo(accessID: AccessIdentifier) : Video
     def lookupAudio(accessID: AccessIdentifier) : Audio
     def lookupPerson(accessID: AccessIdentifier) : Person
     def lookupLocation(accessID: AccessIdentifier) : Location
     def lookupOrganization(accessID: AccessIdentifier) : Organization
    
    /**
     * Handle receiving the lookup messages. Note, the same bug affects this trait as well.
     */
  	protected def lookupManagement : Receive = {
    	case lookup: LookupCollection =>
    	  	val chan = self.channel
    	  	Future { lookupCollection(lookup.getId) } onResult { case r: Collection => chan ! r }
    	  	
    	case lookup: LookupPage =>
    	  	val chan = self.channel
    	  	Future { lookupPage(lookup.getId) } onResult { case r: Page => chan ! r }
    	  	
    	case lookup: LookupPicture =>
    	  	val chan = self.channel
    	  	Future { lookupPicture(lookup.getId) } onResult { case r: Picture => chan ! r }
    	  	
    	case lookup: LookupVideo =>
    	  	val chan = self.channel
    	  	Future { lookupVideo(lookup.getId) } onResult { case r: Video => chan ! r }
    	  	
    	case lookup: LookupAudio =>
    	  	val chan = self.channel
    	  	Future { lookupAudio(lookup.getId) } onResult { case r: Audio => chan ! r }
    	  	
    	case lookup: LookupPerson =>
    	  	val chan = self.channel
    	  	Future { lookupPerson(lookup.getId) } onResult { case r: Person => chan ! r }
    	  	
    	case lookup: LookupLocation =>
    	  	val chan = self.channel
    	  	Future { lookupLocation(lookup.getId) } onResult { case r: Location => chan ! r }
    	  	
    	case lookup: LookupOrganization =>
    	  	val chan = self.channel
    	  	Future { lookupOrganization(lookup.getId) } onResult { case r: Organization => chan ! r }

  	}
}

