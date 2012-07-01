// Trait for providing connection management for the end points of the system.
// It is mainly responsible for sending a connection message to the librarian.
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
    
  // Get the resource key/id for this library
  def getResourceKey : String = connection.getRequestedKey  

  // Connect to the specified librarian */
  protected def connectToLibrarian(hostName: String, port: Int) {
    val librarian = remote.actorFor(proteus_service_name, hostName, port)
    librarian ! connection
  }

  // Prepare a search response for sending to the client 
  // by adding our resource id to it
  protected def prepareToSend(response: SearchResponse) : SearchResponse = {
    val builder = response.toBuilder
    builder.getResultsList.asScala.foreach(r => r.toBuilder.getIdBuilder.setResourceId(getResourceKey))
    return builder.build
  }

  // Handle messages related to connecting to the librarian 
  // (or if we were to act as a secondary librarian)
  protected def connectionManagement : Receive = {
    case c: LibraryConnected =>
      // Check that we connected successfully, and without errors
      if (c.hasError) {	
	println("ERROR Connecting to Librarian: " + c.getError)
      } else { 
    	// Update our key (the one received here will always be the same as the existing non-trivial key)
    	connection = connection.toBuilder.setRequestedKey(c.getResourceId).build
    	println("Connected to librarian, assigned ID: " + getResourceKey)
      }
  }
}

