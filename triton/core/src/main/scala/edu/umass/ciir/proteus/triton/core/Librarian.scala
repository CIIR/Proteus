package edu.umass.ciir.proteus.triton.core

import akka.actor.Actor._
import akka.actor.UntypedChannel
import akka.actor.Actor
import akka.actor.ActorRef
import akka.config.Supervision._
import akka.dispatch._
import scala.collection.JavaConverters._

import edu.umass.ciir.proteus.protocol.ProteusProtocol._

/**
 * BasicLibrarian class puts all the pieces of a basic librarian together along with 
 * the settings for running the server. Specifically, we are extending LibraryServer, which 
 * forces us to implement all the necessary methods, and then we mix in the basic librarian 
 * connection, query, and lookup management traits.
 */
class BasicLibrarian(settings: ServerSetting) extends 
	LibraryServer with 
	LibrarianConnectionManagement with 
	LibrarianQueryManagement with 
	LibrarianLookupManagement {
  
  	val serverHostname : String = settings.hostname
	val serverPort : Int = settings.port

}

object basicLibrarianApp extends App {
  val librarianService = try {
    actorOf(new BasicLibrarian(ServerSetting(args(0), args(1).toInt))).start()
  } catch {
    case ex: Exception => 
	println("Unable to load hostname and port from arguments, defaulting to localhost:8081")
	actorOf(new BasicLibrarian(ServerSetting("localhost", 8081))).start()
  }
}

/************ Implementation of a Basic Librarian (Library Management/Router) *************/
/**
 * Connection management for the librarian. 
 */
trait LibrarianConnectionManagement extends RandomDataGenerator with ProteusAPI { this: Actor =>

    // Hashmaps for quickly looking up the library/endpoint from its key
  	val libraries = new collection.mutable.HashMap[String,  RemoteLibrary]()
  	// Libraries with the same groupID can support each other and combine to form a single resource
  	val group_membership = new collection.mutable.HashMap[String, List[String]]()
  	
  	var supported_types = Set[ProteusType]()
  	var supported_dyn_transforms = List[DynamicTransformID]()
  	
  	/**
  	 * Determines if the library should be referred to by a new generated key, 
  	 * the one it requested, or if there is a collision and the connection is trashed.
  	 */
    protected def libKeyID(c: ConnectLibrary) : String = {
      val reqKey = if (c.hasRequestedKey) c.getRequestedKey else genKey()
      if (libraries.contains(reqKey)) {
    	  val collision = libraries(reqKey)
          if (collision.getHostname == c.getHostname && 
              collision.getPort == c.getPort && 
              collision.getGroupId == c.getGroupId)
            return reqKey
          else
        	return ""
      } else 
    	  return reqKey
    }
     
    /**
     * Handle messages for connecting to end points (libraries) for the librarian.
     */
	protected def connectionManagement : Receive = {
    	case c: ConnectLibrary =>
    		// Generate a new identifier for this library
    	    val id = libKeyID(c)
    	    if (id == "") {
    	    	val lib_actor = remote.actorFor(proteus_service_name, c.getHostname, c.getPort)
    	        // Conflict on keys, refuse connection
    	    	val connected_message = LibraryConnected.newBuilder
    	    		.setError("Requested Key conflicts with existing incompatible server")
    	    		.build
    	    		
    	    	lib_actor ! connected_message
    	    } else {
	    	    // Add group membership
    	        val gID: String = if(c.hasGroupId) c.getGroupId else genKey()
    	        if (group_membership.contains(gID))
    	        	group_membership(gID) ::= id
    	        else
    	        	group_membership += gID -> List(id)
    	        
    	        val updated_connection = c.toBuilder()
		    	        	.setGroupId(gID)
		    	        	.setRequestedKey(id)
		    	        	.build()
    	        	
    	        libraries += id -> new RemoteLibrary(updated_connection)	
	    	    	    	    
	    	    // Update support types/transforms
	    	    supported_types ++= Set() ++ c.getSupportedTypesList.asScala
	    	    supported_dyn_transforms ++= c.getDynamicTransformsList.asScala
    	    }
  	}
	// TODO: we are currently asking the contents for who they are contained by, need to reverse this!
	
    /**
     * The main workhorse of this trait, which conditionally forwards messages to libraries (whose ids are in list members), 
     * or sends the message to all of them and recombines the results, alternatively may also return an error based response.
     * This is where most of the interesting Librarian intelligent code would go (such as load balancing and topic grouping).
     */
    protected def sendOrForwardTo(members: List[String], message: Any, chan: UntypedChannel) {
        if (members.length == 0) {
    	  	chan ! (SearchResponse.newBuilder
    	  			  		.setError("No library support for this operation...")
    	  			  		.build) 
    	  			  		
        } else if (members.length == 1) {
          libraries(members(0)).forwardMessage(message, chan)
        } else {
          // All the re-ordering code for reassembling the multiple responses should go here
          // First send the message to all the libraries, and get the Future result
          val futureList = members.map(id => (libraries(id).client ? message).mapTo[SearchResponse])
          // Next, combine these results into a single future over a list of search results
          val resultsList = Futures.fold(List[SearchResult]())(futureList)((a: List[SearchResult], b: SearchResponse) => b.getResultsList.asScala.toList ::: a)
          // Finally, turn this into a SearchResponse and return it.
          val final_result = resultsList
          		.map(srList => SearchResponse.newBuilder.addAllResults(srList.asJava).build)
          		.recover {
	    	  	    case _ => SearchResponse.newBuilder
	    	  			  		.setError("Error in responses from libraries...")
	    	  			  		.build
    	  	  	}
          final_result onResult { case r: SearchResponse => chan ! r }
        }
    }	
    
    /**
	 * Get the libraries which belong to groupId and support ptype
	 */
    protected def groupMemberTypeSupport(ptype: ProteusType, groupId: String) : List[String] = {
      group_membership(groupId).filter(id => libraries(id).supportsType(ptype))
    }
    
    /**
     * Get the libraries that support the dynamic transform given and are in groupId
     */
    protected def groupMemberDynTransSupport(dtID: DynamicTransformID, groupId: String) : List[String] = {
      group_membership(groupId).filter(id => 
        libraries(id).getDynamicTransforms.exists(dt => 
          dt.getName == dtID.getName && dt.getFromType == dtID.getFromType))
    }
    
    /**
     * Return the library ids that support one or more types in the list
     */
    protected def typeSupport(ptypes: List[ProteusType]) : List[String] = {
      libraries.keys.toList.foreach(k => System.out.println(libraries(k).getSupportedTypes))
      libraries.keys.toList.filter(k => libraries(k).getSupportedTypes.exists(t => ptypes.contains(t)))
    }
    
    // Does this librarian have a library that supports ptype
    protected def supportsType(ptype: ProteusType) : Boolean = supported_types.contains(ptype)
    // Does this librarian have a library that supports the dynamic transform dtID
	protected def supportsDynTransform(dtID: DynamicTransformID) : Boolean = supported_dyn_transforms.contains(dtID)
	// Given a library's resource ID get that remote library (or null if none exist with that id)
  	protected def getLibrary(id: String) : RemoteLibrary = if (libraries.contains(id)) libraries(id) else null
  	
}

/**
 * Trait providing query management and transform management for the librarian class
 */
trait LibrarianQueryManagement { this: Actor =>
    /**
     * All these message handlers do essentially the same thing (which is a work around for the bug), 
     * and that is to call sendOrForwardTo, and then send that response back to the client.
     */
  	protected def queryManagement : Receive = {
    	case s: Search =>
    	  	val search_query = s.getSearchQuery
    	  	val members = typeSupport(search_query.getTypesList.asScala.toList)
    	  	System.out.println("Members for search: " + members + " " + search_query.getTypesList.asScala.toList)
    	  	sendOrForwardTo(members, s, self.channel)
    	  	
    	// Libraries must be able to transform to its contained type when getting contents, and from its type when getting container
        case trans: ContainerTransform => // If you support the from type, then you must be able to get its container
            val members = groupMemberTypeSupport(trans.getToType, getLibrary(trans.getId.getResourceId).getGroupId)
    	  	sendOrForwardTo(members, trans, self.channel)
        
        case trans: ContentsTransform => // If you support the From Type then you must be able to get the contents (equivalent statement as above)
        	val members = groupMemberTypeSupport(trans.getFromType, getLibrary(trans.getId.getResourceId).getGroupId)
    	  	sendOrForwardTo(members, trans, self.channel)
        
        case trans: OverlapsTransform =>
        	val members = groupMemberTypeSupport(trans.getFromType, getLibrary(trans.getId.getResourceId).getGroupId)
    	  	sendOrForwardTo(members, trans, self.channel)
        
        case trans: OccurAsObjTransform => 
        	val members = groupMemberTypeSupport(trans.getFromType, getLibrary(trans.getId.getResourceId).getGroupId)
    	  	sendOrForwardTo(members, trans, self.channel)
        
        case trans: OccurAsSubjTransform => 
        	val members = groupMemberTypeSupport(trans.getFromType, getLibrary(trans.getId.getResourceId).getGroupId)
    	  	sendOrForwardTo(members, trans, self.channel)
        
        case trans: OccurHasObjTransform => 
      		val members = groupMemberTypeSupport(trans.getFromType, getLibrary(trans.getId.getResourceId).getGroupId)
    	  	sendOrForwardTo(members, trans, self.channel)
        
        case trans: OccurHasSubjTransform =>
        	val members = groupMemberTypeSupport(trans.getFromType, getLibrary(trans.getId.getResourceId).getGroupId)
    	  	sendOrForwardTo(members, trans, self.channel)
        
        case trans: NearbyLocationsTransform =>
        	val members = groupMemberTypeSupport(ProteusType.LOCATION, getLibrary(trans.getId.getResourceId).getGroupId)
    	  	sendOrForwardTo(members, trans, self.channel)
        
        case dtrans: DynamicTransform =>
        	val members = groupMemberDynTransSupport(dtrans.getTransformId, getLibrary(dtrans.getId.getResourceId).getGroupId)
    	  	sendOrForwardTo(members, dtrans, self.channel)
  	}
    	  	
  	// Methods this trait relies upon that must be implemented in another trait/class
  	protected def groupMemberDynTransSupport(dtID: DynamicTransformID, groupId: String) : List[String]
  	protected def typeSupport(ptypes: List[ProteusType]) : List[String]
  	protected def groupMemberTypeSupport(ptype: ProteusType, groupId: String) : List[String]
  	protected def sendOrForwardTo(members: List[String], message: Any, chan: UntypedChannel)
  	protected def getLibrary(id: String) : RemoteLibrary
}

/**
 * Provide lookup management for the librarian.
 */
trait LibrarianLookupManagement { this: Actor =>
    /** Handle messages to the librarian for looking up resource data. */
  	def lookupManagement : Receive = {
    	case lookup: LookupCollection =>
    	  	val lib = getLibrary(lookup.getId.getResourceId)
    	  	if(lib == null)
    	  		self.reply(Collection.newBuilder
    	  				  .setId(lookup.getId.toBuilder
    	  						 .setError("Received lookup with unrecognized resource ID: " + lookup.getId.getResourceId)
    	  						 .build)
    					  .build)
    	    else // This is simply a placeholder until the remote actor Akka bug gets fixed (supposedly was just fixed in latest version, def. by 2.0)
    	    	lib.forwardMessage(lookup, self.channel)
	  	
    	case lookup: LookupPage =>
    	  	val lib = getLibrary(lookup.getId.getResourceId)
    	  	if(lib == null)
    	  		self.reply(Page.newBuilder
    	  		    .setId(lookup.getId.toBuilder
    	  						 .setError("Received lookup with unrecognized resource ID: " + lookup.getId.getResourceId)
    	  						 .build)
    			  .build)
    	  	else lib.forwardMessage(lookup, self.channel)
    	  	
    	case lookup: LookupPicture =>
    	  	val lib = getLibrary(lookup.getId.getResourceId)
    	  	if(lib == null)
    	  		self.reply(Picture.newBuilder
    	  		    .setId(lookup.getId.toBuilder
    	  						 .setError("Received lookup with unrecognized resource ID: " + lookup.getId.getResourceId)
    	  						 .build)
    			  .build)
    	  	else lib.forwardMessage(lookup, self.channel)
    	  	
    	case lookup: LookupVideo =>
    	  	val lib = getLibrary(lookup.getId.getResourceId)
    	  	if(lib == null)
    	  		self.reply(Video.newBuilder
    	  		    .setId(lookup.getId.toBuilder
    	  						 .setError("Received lookup with unrecognized resource ID: " + lookup.getId.getResourceId)
    	  						 .build)
    			  .build)
    	  	else lib.forwardMessage(lookup, self.channel)
    	  	
    	case lookup: LookupAudio =>
    	  	val lib = getLibrary(lookup.getId.getResourceId)
    	  	if(lib == null)
    	  		self.reply(Audio.newBuilder
    	  		    .setId(lookup.getId.toBuilder
    	  						 .setError("Received lookup with unrecognized resource ID: " + lookup.getId.getResourceId)
    	  						 .build)
    			  .build)
    	  	else lib.forwardMessage(lookup, self.channel)
    	  	
    	case lookup: LookupPerson =>
    	  	val lib = getLibrary(lookup.getId.getResourceId)
    	  	if(lib == null)
    	  		self.reply(Person.newBuilder
    	  		    .setId(lookup.getId.toBuilder
    	  						 .setError("Received lookup with unrecognized resource ID: " + lookup.getId.getResourceId)
    	  						 .build)
    			  .build)
    	  	else lib.forwardMessage(lookup, self.channel)
    	  	
    	case lookup: LookupLocation =>
    	  	val lib = getLibrary(lookup.getId.getResourceId)
    	  	if(lib == null)
    	  		self.reply(Location.newBuilder
    	  		    .setId(lookup.getId.toBuilder
    	  						 .setError("Received lookup with unrecognized resource ID: " + lookup.getId.getResourceId)
    	  						 .build)
    			  .build)
    	  	else lib.forwardMessage(lookup, self.channel)
    	  	
    	case lookup: LookupOrganization =>
    	  	val lib = getLibrary(lookup.getId.getResourceId)
    	  	if(lib == null)
    	  		self.reply(Organization.newBuilder
    	  		    .setId(lookup.getId.toBuilder
    	  						 .setError("Received lookup with unrecognized resource ID: " + lookup.getId.getResourceId)
    	  						 .build)
    			  .build)
    	  	else lib.forwardMessage(lookup, self.channel)
  	}

  	// Method implemented elsewhere.
  	protected def getLibrary(id: String) : RemoteLibrary
}

/**
 * A local class to make interacting with the remote actors (libraries) a little cleaner.
 */
class RemoteLibrary(details: ConnectLibrary) extends ProteusAPI {
  // Connect to the remote actor (library)
  val client = {
    val lib_actor = remote.actorFor(proteus_service_name, details.getHostname, details.getPort)
    // Tell the library we have them connected
    val connected_message = LibraryConnected.newBuilder
	   		.setResourceId(details.getRequestedKey)
	   		.build
	    	    		
	lib_actor ! connected_message
	lib_actor
  }
  
  // Methods for accessing information about this library
  def getHostname: String = details.getHostname
  def getPort: Int = details.getPort.toInt
  def getGroupId: String = details.getGroupId
  def getSupportedTypes: List[ProteusType] = details.getSupportedTypesList.asScala.toList
  def getDynamicTransforms: List[DynamicTransformID] = details.getDynamicTransformsList.asScala.toList
  def supportsType(ptype: ProteusType): Boolean = details.getSupportedTypesList.asScala.contains(ptype)
  def supportsDynTransform(dtID: DynamicTransformID): Boolean = details.getDynamicTransformsList.asScala.contains(dtID)
  
  // Forward a message to the remote actor (meaning that supposedly, it will reply directly to the client)
  def forwardMessage(message: Any, channel: UntypedChannel) {
    // When the akka bug is fixed we can instead do: client forward message
    client ? message onResult { case r => channel ! r }
  }
  
}


