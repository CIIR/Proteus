// Trait providing query handling for end points. This trait crucially 
// depends on the methods starting: run....Transform, and runSearch. They must 
// be implemented elsewhere.
package edu.umass.ciir.proteus.triton.core

trait EndPointQueryManagement { 
  this: Actor =>
    // If your end point does not support direct searches, 
    // then return empty results and no error.
    //
    // If your end point does support searching, but not over 
    // ANY of the requested types, then return empty results and an error message.
    def runSearch(s: Search) : SearchResponse
  
  // For the transformation methods:
  // If the type is not supported by your end point, then return 
  // with no results (empty list) and an error message set.
  //
  // If the type is supported but that operation is not (either in general or just for that type)
  // then return no results and no error message.
  def runContainerTransform(transform: ContainerTransform) : SearchResponse
  def runContentsTransform(transform: ContentsTransform) : SearchResponse
  def runOverlapsTransform(transform: OverlapsTransform) : SearchResponse 
  def runOccurAsObjTransform(transform: OccurAsObjTransform) : SearchResponse 
  def runOccurAsSubjTransform(transform: OccurAsSubjTransform) : SearchResponse 
  def runOccurHasObjTransform(transform: OccurHasObjTransform) : SearchResponse 
  def runOccurHasSubjTransform(transform: OccurHasSubjTransform) : SearchResponse 
  def runNearbyLocationsTransform(transform: NearbyLocationsTransform) : SearchResponse  	
  protected def prepareToSend(response: SearchResponse) : SearchResponse
  protected def errorResponse(errorText: String): SearchResponse 
  
  // Handle the messages relating to queries and transforms.
  // NOTE: There is a bug in Akka 1.2 (maybe also 1.3) that causes problems for 
  // remote actors doing forwards, as well as trying to do a reply in a Future. So, 
  // we have a work around implemented that should be removed when upgraded.
  protected def queryManagement : Receive = {
    // All of these simply call the method, modify the response, and then send it back out
    case s: Search => 
      val chan = self.channel // This is a workaround until the bug in Akka 1.2 is fixed
    // Lets add in some exception handling for safety.
    Future { 
      try {
	prepareToSend(runSearch(s)) 
      } catch {
	case ex: Exception => System.err.println("Search on endpoint threw an exception:")
	ex.printStackTrace()
	errorResponse("Search threw an exception --> " + ex.toString)
      }
    } onResult { case r: SearchResponse => chan ! r}
    
    case trans: ContainerTransform => 
      val chan = self.channel
    Future { 
      try {
	prepareToSend(runContainerTransform(trans)) 
      } catch {
	case ex: Exception => System.err.println("Container Transform on endpoint threw an exception:")
	ex.printStackTrace()
	errorResponse("Container Transform threw an exception --> " + ex.toString)
      }
    } onResult { case r: SearchResponse => chan ! r }
    
    case trans: ContentsTransform => 
      val chan = self.channel
    Future { 
      try {
	prepareToSend(runContentsTransform(trans)) 
      } catch {
	case ex: Exception => System.err.println("Contents Transform on endpoint threw an exception:")
	ex.printStackTrace()
	errorResponse("Contents Transform threw an exception --> " + ex.toString)
      }
    } onResult { case r: SearchResponse => chan ! r }
    
    case trans: OverlapsTransform => 
      val chan = self.channel
    Future { 
      try {
	prepareToSend(runOverlapsTransform(trans)) 
      } catch {
	case ex: Exception => System.err.println("Overlaps Transform on endpoint threw an exception:")
	ex.printStackTrace()
	errorResponse("Overlaps Transform threw an exception --> " + ex.toString)
      }
    } onResult { case r: SearchResponse => chan ! r }
    
    case trans: OccurAsObjTransform => 
      val chan = self.channel
    Future { 
      try {
	prepareToSend(runOccurAsObjTransform(trans)) 
      } catch {
	case ex: Exception => System.err.println("Occurrence as Object Transform on endpoint threw an exception:")
	ex.printStackTrace()
	errorResponse("Occurrence as Object Transform threw an exception --> " + ex.toString)
      }
    } onResult { case r: SearchResponse => chan ! r }
    
    case trans: OccurAsSubjTransform => 
      val chan = self.channel
    Future { 
      try {
	prepareToSend(runOccurAsSubjTransform(trans)) 
      } catch {
	case ex: Exception => System.err.println("Occurrence as Subject Transform on endpoint threw an exception:")
	ex.printStackTrace()
	errorResponse("Occurrence as Subject Transform threw an exception --> " + ex.toString)
      }
    } onResult { case r: SearchResponse => chan ! r }
    
    case trans: OccurHasObjTransform => 
      val chan = self.channel
    Future { 
      try {
	prepareToSend(runOccurHasObjTransform(trans)) 
      } catch {
	case ex: Exception => System.err.println("Occurrence has Object Transform on endpoint threw an exception:")
	ex.printStackTrace()
	errorResponse("Occurrence has Object Transform threw an exception --> " + ex.toString)
      }
    } onResult { case r: SearchResponse => chan ! r }
    
    case trans: OccurHasSubjTransform => 
      val chan = self.channel
    Future { 
      try {
	prepareToSend(runOccurHasSubjTransform(trans)) 
      } catch {
	case ex: Exception => System.err.println("Occurrence has Subject Transform on endpoint threw an exception:")
	ex.printStackTrace()
	errorResponse("Occurrence has Subject Transform threw an exception --> " + ex.toString)
      }
    } onResult { case r: SearchResponse => chan ! r }
    
    case trans: NearbyLocationsTransform => 
      val chan = self.channel
    Future { 
      try {
	prepareToSend(runNearbyLocationsTransform(trans)) 
      } catch {
	case ex: Exception => System.err.println("Nearby Locations Transform on endpoint threw an exception:")
	ex.printStackTrace()
	errorResponse("Nearby Locations Transform threw an exception --> " + ex.toString)
      }
    } onResult { case r: SearchResponse => chan ! r }    
  }
}
