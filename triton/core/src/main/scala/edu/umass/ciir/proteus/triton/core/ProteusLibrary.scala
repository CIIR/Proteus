package edu.umass.ciir.proteus.triton.core

import akka.dispatch._
import akka.actor.Actor._
import akka.actor.Actor
import akka.config.Supervision._
import scala.collection.JavaConverters._

import edu.umass.ciir.proteus.protocol.ProteusProtocol._

// ServerSetting is used to pass around the hostname and port for connecting to some server.
case class ServerSetting(hostname: String, port: Int)
case class EndPointConfiguration(dataType : ProteusType, resourcePath: String)
/**
 * The base trait for scala object model of the Proteus API.
 * Provides implicit conversion between strings and proteus types, as well as 
 * information (maps) about the type hierarchy. Also provides the name of the service that 
 * proteus actors are supporting.
 */
trait ProteusAPI {
    implicit def str2ProteusType(str: String) = convertType(str)
  	
  	val proteus_service_name = "library-service" 
	def numProteusTypes : Int = 8
	
	// What a given type can have contents of type
	val contents_map = Map(ProteusType.COLLECTION 	-> List(ProteusType.PAGE),	// Collection contains Pages 
						   ProteusType.PAGE 		-> List(ProteusType.PICTURE,ProteusType.VIDEO,ProteusType.AUDIO,ProteusType.PERSON,ProteusType.LOCATION,ProteusType.ORGANIZATION),
						   ProteusType.PICTURE 		-> List(ProteusType.PERSON,ProteusType.LOCATION,ProteusType.ORGANIZATION),		// Picture contains entities
						   ProteusType.VIDEO 		-> List(ProteusType.PERSON,ProteusType.LOCATION,ProteusType.ORGANIZATION),		// Video contains entities
						   ProteusType.AUDIO 		-> List(ProteusType.PERSON,ProteusType.LOCATION,ProteusType.ORGANIZATION))
			
	val container_map = Map(ProteusType.PAGE -> List(ProteusType.COLLECTION), 
							ProteusType.PICTURE -> List(ProteusType.PAGE), 
							ProteusType.VIDEO -> List(ProteusType.PAGE), 
							ProteusType.AUDIO -> List(ProteusType.PAGE), 
							ProteusType.PERSON -> List(ProteusType.PAGE, ProteusType.PICTURE, ProteusType.VIDEO, ProteusType.AUDIO), 
							ProteusType.LOCATION -> List(ProteusType.PAGE, ProteusType.PICTURE, ProteusType.VIDEO, ProteusType.AUDIO), 
							ProteusType.ORGANIZATION -> List(ProteusType.PAGE, ProteusType.PICTURE, ProteusType.VIDEO, ProteusType.AUDIO))
							
    def containerFor(ptype: ProteusType) : List[ProteusType] = if(container_map.contains(ptype)) container_map(ptype) else null
							
	def convertType(strType: String) : ProteusType = strType match {
		  case "collection" => ProteusType.COLLECTION
		  case "page" => ProteusType.PAGE
		  case "picture" => ProteusType.PICTURE
		  case "video" => ProteusType.VIDEO
		  case "audio" => ProteusType.AUDIO
		  case "person" => ProteusType.PERSON
		  case "location" => ProteusType.LOCATION
		  case "organization" => ProteusType.ORGANIZATION
		  case _ => throw new IllegalArgumentException("Invalid proteus type: " + strType)
	}
	
	def convertTypes(types: List[String]) : List[ProteusType] = {
		types.map(convertType(_))
	}
					
}

/**
 * The base trait for the librarian and for the end point (library). 
 */
trait LibraryServer extends Actor with ProteusAPI {

	def serverHostname : String
	def serverPort : Int
	
	override def preStart() = {
	    remote.start(serverHostname, serverPort)
              .register(proteus_service_name, self)
	}
  
    // Actor message handler
	def receive: Receive = connectionManagement orElse queryManagement orElse lookupManagement
	
	// Abstract methods to be defined elsewhere
	protected def connectionManagement : Receive
	protected def queryManagement : Receive
	protected def lookupManagement : Receive
	
	protected def supportsType(ptype: ProteusType) : Boolean
	protected def supportsDynTransform(dtID: DynamicTransformID) : Boolean

}

/**
 * Trait for generating pseudo random strings and keys.
 */
trait RandomDataGenerator {
    val keyChars: String = (('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9')).mkString("")
    /**
     * Generates a random alpha-numeric string of fixed length (8).
     * Granted, this is a BAD way to do it, because it doesn't guarantee true randomness.
     */
    def genKey(length: Int = 8): String = (1 to length).map(x => keyChars.charAt(util.Random.nextInt(keyChars.length))).mkString
  
}

/**
 * Abstract base class which contains abstract methods for all the methods that a data store (library/endpoint) trait 
 * needs to implement.
 */
abstract trait EndPointDataStore {
  
	protected def unsupportedResponse(method: String): SearchResponse = SearchResponse.newBuilder.setError("Unsupported by end point: " + method).build
  
  	/** Methods Used Here and Elsewhere (MUST BE PROVIDED) **/
	def getResourceKey : String
  	def getSupportedTypes : List[ProteusType]
	
	/** Core Functionality Methods (MUST BE PROVIDED) **/
    def initialize(configuration: EndPointConfiguration) {}
	
	def supportsType(ptype: ProteusType) : Boolean
	def supportsDynTransform(dtID: DynamicTransformID) : Boolean
	
	
  	def getDynamicTransforms : List[DynamicTransformID] = List[DynamicTransformID]()
	def runSearch(s: Search) : SearchResponse = unsupportedResponse("Search")
  	
  	def runContainerTransform(transform: ContainerTransform) : SearchResponse = unsupportedResponse("Container Transform")  	
  	def runContentsTransform(transform: ContentsTransform) : SearchResponse = unsupportedResponse("Contents Transform")  	
  	def runOverlapsTransform(transform: OverlapsTransform) : SearchResponse = unsupportedResponse("Overlaps Transform")  	
  	def runOccurAsObjTransform(transform: OccurAsObjTransform) : SearchResponse = unsupportedResponse("Occurrences as Object") 	
  	def runOccurAsSubjTransform(transform: OccurAsSubjTransform) : SearchResponse  = unsupportedResponse("Occurrences as Subject") 	
  	def runOccurHasObjTransform(transform: OccurHasObjTransform) : SearchResponse = unsupportedResponse("Occurrences having Object")  	
  	def runOccurHasSubjTransform(transform: OccurHasSubjTransform) : SearchResponse  = unsupportedResponse("Occurrences having Subject")  	
  	def runNearbyLocationsTransform(transform: NearbyLocationsTransform) : SearchResponse  = unsupportedResponse("Nearby Locations")
  	def runDynamicTransform(transform: DynamicTransform) : SearchResponse = unsupportedResponse("Dynamic Transform")
  	
  	
  	def lookupCollection(accessID: AccessIdentifier) : Collection = {
    	return Collection.newBuilder
    			  .setId(AccessIdentifier.newBuilder
    					  .setIdentifier(accessID.getIdentifier)
    					  .setResourceId(getResourceKey)
    					  .setError("error: lookup not supported by " + getResourceKey)
    					  .build)
    			  .build
  	}
  	
    def lookupPage(accessID: AccessIdentifier) : Page = {
  		return Page.newBuilder
    			  .setId(AccessIdentifier.newBuilder
    					  .setIdentifier(accessID.getIdentifier)
    					  .setResourceId(getResourceKey)
    					  .setError("error: lookup not supported by " + getResourceKey)
    					  .build)
    			  .build
    }
    
 	

    def lookupPicture(accessID: AccessIdentifier) : Picture = {
  		return Picture.newBuilder
    			  .setId(AccessIdentifier.newBuilder
    					  .setIdentifier(accessID.getIdentifier)
    					  .setResourceId(getResourceKey)
    					  .setError("error: lookup not supported by " + getResourceKey)
    					  .build)
    			  .build
    }
    
    def lookupVideo(accessID: AccessIdentifier) : Video = {
  		return Video.newBuilder
    			  .setId(AccessIdentifier.newBuilder
    					  .setIdentifier(accessID.getIdentifier)
    					  .setResourceId(getResourceKey)
    					  .setError("error: lookup not supported by " + getResourceKey)
    					  .build)
    			  .build
    }
    
    def lookupAudio(accessID: AccessIdentifier) : Audio = {
  		return Audio.newBuilder
    			  .setId(AccessIdentifier.newBuilder
    					  .setIdentifier(accessID.getIdentifier)
    					  .setResourceId(getResourceKey)
    					  .setError("error: lookup not supported by " + getResourceKey)
    					  .build)
    			  .build
    }
    
    def lookupPerson(accessID: AccessIdentifier) : Person = {
  		return Person.newBuilder
    			  .setId(AccessIdentifier.newBuilder
    					  .setIdentifier(accessID.getIdentifier)
    					  .setResourceId(getResourceKey)
    					  .setError("error: lookup not supported by " + getResourceKey)
    					  .build)
    			  .build

    }   
    
    def lookupLocation(accessID: AccessIdentifier) : Location = {
  		return Location.newBuilder
    			  .setId(AccessIdentifier.newBuilder
    					  .setIdentifier(accessID.getIdentifier)
    					  .setResourceId(getResourceKey)
    					  .setError("error: lookup not supported by " + getResourceKey)
    					  .build)
    			  .build
    } 
    
    def lookupOrganization(accessID: AccessIdentifier) : Organization = {
  		return Organization.newBuilder
    			  .setId(AccessIdentifier.newBuilder
    					  .setIdentifier(accessID.getIdentifier)
    					  .setResourceId(getResourceKey)
    					  .setError("error: lookup not supported by " + getResourceKey)
    					  .build)
    			  .build
    } 
}
