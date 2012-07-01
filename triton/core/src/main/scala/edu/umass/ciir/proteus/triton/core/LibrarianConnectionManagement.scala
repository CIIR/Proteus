package edu.umass.ciir.proteus.triton.core

// Connection management for the librarian. 
trait LibrarianConnectionManagement extends RandomDataGenerator with ProteusAPI { this: Actor =>  
  // Hashmaps for quickly looking up the library/endpoint from its key
  val libraries = new collection.mutable.HashMap[String,  RemoteLibrary]()
  
  // Libraries with the same groupID can support each other and combine to form a single resource
  val group_membership = new collection.mutable.HashMap[String, List[String]]()
  var supported_types = Set[ProteusType]()
  
  // Determines if the library should be referred to by a new generated key, 
  // the one it requested, or if there is a collision and the connection is trashed.
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
  
  // Handle messages for connecting to end points (libraries) for the librarian.
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
    }
  }
  // TODO: we are currently asking the contents for who they are contained by, need to reverse this!
  
  // The main workhorse of this trait, which conditionally forwards messages to libraries (whose ids are in list members), 
  // or sends the message to all of them and recombines the results, alternatively may also return an error based response.
  // This is where most of the interesting Librarian intelligent code would go (such as load balancing and topic grouping).
  protected def sendOrForwardTo(members: List[String], message: Any, chan: UntypedChannel) : Unit = {
    members.length match {
      case 0 => 
	chan ! (SearchResponse.newBuilder
    		.setError("No library support for this operation...")
    		.build) 
    } else if (members.length == 1) {
      libraries(members(0)).forward(message)
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
      }.get
      chan ! final_result
    }
  }	
  
  
  // Get the libraries which belong to groupId and support ptype
  protected def groupMemberTypeSupport(ptype: ProteusType, groupId: String) : List[String] = {
    group_membership(groupId).filter(id => libraries(id).supportsType(ptype))
  }
    
  // Return the library ids that support one or more types in the list
  protected def typeSupport(ptypes: List[ProteusType]) : List[String] = {
    libraries.keys.toList.foreach(k => System.out.println(libraries(k).getSupportedTypes))
    libraries.keys.toList.filter(k => libraries(k).getSupportedTypes.exists(t => ptypes.contains(t)))
  }
  
  // Does this librarian have a library that supports ptype
  protected def supportsType(ptype: ProteusType) : Boolean = supported_types.contains(ptype)
  // Given a library's resource ID get that remote library (or null if none exist with that id)
  protected def getLibrary(id: String) : RemoteLibrary = if (libraries.contains(id)) libraries(id) else null  
}
