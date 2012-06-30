// Class used by client programs to interact with the librarian/manager.
package edu.umass.ciir.proteus.triton.core

import akka.actor.Actor._
import akka.actor.Actor
import akka.dispatch.Future
import scala.collection.JavaConverters._
import edu.umass.ciir.proteus.protocol.ProteusProtocol._

class LibrarianClient(libHostName: String, libPort: Int) extends ProteusAPI {  
  // The connection to the librarian
  val librarian_actor = remote.actorFor(proteus_service_name, libHostName, libPort)
  
  // Queries the librarian for the query text (text) over the requested types (types_requested). 
  // The result is a Future for a SearchResponse. The results of which can be then looked up to get 
  // the full objects.
  def query(text: String, 
	    types_requested: List[ProteusType], 
	    num_requested: Int = 30, 
	    start_at: Int = 0, 
	    language: String = "en") : Future[SearchResponse] = {
    // Build the parts of the message
    val search_params = SearchParameters.newBuilder
    .setNumRequested(num_requested)
    .setStartAt(start_at)
    .setLanguage(language)
    .build
    
    val search_request = SearchRequest.newBuilder
    .setQuery(text)
    .setParams(search_params)
    .addAllTypes(types_requested.asJava)
    .build
    
    val search_message = Search.newBuilder
    .setSearchQuery(search_request)
    .build
    // Send the message to the librarian		
    val response = librarian_actor ? search_message
    return response.mapTo[SearchResponse]
  } 

  // A transformation query which gets the contents (reference requests) belonging to 
  // a given access identifier (which specifies a data resource). The response is returned as a Future.
  def getContents(id: AccessIdentifier, 
		  id_type: ProteusType, 
		  contents_type: ProteusType, 
		  num_requested: Int = 30, 
		  start_at: Int = 0, 
		  language: String = "en") : Future[SearchResponse] = {
    if(!contents_map(id_type).contains(contents_type))
      throw new IllegalArgumentException("Mismatched to/from types for getContents: (" + 
					 id_type.getValueDescriptor.getName + ", " + 
					 contents_type.getValueDescriptor.getName + ")")
    val search_params = SearchParameters.newBuilder
    .setNumRequested(num_requested)
    .setStartAt(start_at)
    .setLanguage(language)
    .build  
    
    val transform_message = ContentsTransform.newBuilder
    .setId(id)
    .setFromType(id_type)
    .setToType(contents_type)
    .setParams(search_params)
    .build
    
    return (librarian_actor ? transform_message).mapTo[SearchResponse]
  }
  
  // Get the reference result for the containing data resource of of this access identifier
  def getContainer(id: AccessIdentifier, 
		   id_type: ProteusType, 
		   container_type: ProteusType, 
		   num_requested: Int = 30, 
		   start_at: Int = 0, 
		   language: String = "en") : Future[SearchResponse] = {
    
    if((!container_map.contains(id_type) && id_type != container_type) || 
       (container_map.contains(id_type) && !container_map(id_type).contains(container_type)))
      throw new IllegalArgumentException("Mismatched to/from types for getContainer: (" + 
					 id_type.getValueDescriptor.getName + ", " + 
					 container_type.getValueDescriptor.getName + ")")    
    val transform_message = ContainerTransform.newBuilder
    .setId(id)
    .setFromType(id_type)
    .setToType(container_type)
    .build
    
    return (librarian_actor ? transform_message).mapTo[SearchResponse]
  }
  
  // Recursively move through the type_path obeying the type hierarchy and either go ascending or descending.
  // Once we've reached the end of the types list return those searchresults.
  private def recurseHierarchy(result: SearchResult, 
			       type_path: List[ProteusType], 
			       num_requested: Int, 
			       language: String, 
			       ascend: Boolean = false) : Future[List[SearchResult]] = {    
    if (type_path.length == 1)
      return Future { List(result) }
    else {
      val converted = if(!ascend) getContents(result.getId, type_path(0), type_path(1), num_requested=num_requested,language=language)
	    	      else getContainer(result.getId, type_path(0), type_path(1), num_requested=num_requested, language=language)
      
      val recursed = converted flatMap {
	next_results => 
	  val result_list = next_results.getResultsList.asScala
	Future.traverse(result_list)(result => 
	  recurseHierarchy(result, type_path.drop(1), scala.math.ceil(num_requested.toDouble / result_list.length).toInt, language, ascend = ascend))
      }
      return recursed map { _.flatten.toList }
    }
  }
  
  // A transformation query which gets the contents (reference requests) belonging to 
  // a given access identifier (which specifies a data resource). The response is returned as a Future.
  def getDescendants(start_item: SearchResult, type_path: List[ProteusType], 
		     num_requested: Int = 30, language: String = "en") : Future[List[SearchResult]] = {
    
    // Verify that the type_path is a valid path
    if(!type_path.dropRight(1).zip(type_path.drop(1)).forall(t => contents_map(t._1).contains(t._2)))
      throw new IllegalArgumentException("Mismatched type path for getDescendants: (" + 
					 type_path.map(_.getValueDescriptor.getName).mkString(", ") + ")")
    
    // Recursively go through and getContents, until we reach the end...
    val results = recurseHierarchy(start_item, type_path, num_requested, language)
    return results
  }
  
  // Get the reference result for the containing data resource of of this access identifier
  def getAncesters(start_item: SearchResult, 
		   type_path: List[ProteusType], 
		   num_requested: Int = 30, 
		   language: String = "en") : Future[List[SearchResult]] = {
    // Verify that the type_path is a valid path
    if(!type_path.dropRight(1).zip(type_path.drop(1)).forall(t => container_map(t._1).contains(t._2)))
      throw new IllegalArgumentException("Mismatched type path for getAncesters: (" + type_path.map(_.getValueDescriptor.getName).mkString(", ") + ")")
    
    // Recursively go through and getContainer, until we reach the end...
    val results = recurseHierarchy(start_item, type_path, num_requested, language, ascend=true)
    return results
  }
  
  // Get the overlaping resources of the same type as this one. Where the precise meaning of overlapping 
  // is up to the end point data stores to decide.
  def getOverlaps(id: AccessIdentifier, 
		  id_type: ProteusType, 
		  num_requested: Int = 30, 
		  start_at: Int = 0, 
		  language: String = "en") : Future[SearchResponse] = {
    val search_params = SearchParameters.newBuilder
    .setNumRequested(num_requested)
    .setStartAt(start_at)
    .setLanguage(language)
    .build  
    
    val transform_message = OverlapsTransform.newBuilder
    .setId(id)
    .setFromType(id_type)
    .setParams(search_params)
    .build
    
    return (librarian_actor ? transform_message).mapTo[SearchResponse] 
  }
  
  // Get the references to Pages where this person, location, or organization identified by id, 
  // occurs as an object of the provided term.
  def getOccurrencesAsObj(id: AccessIdentifier, 
			  id_type: ProteusType, 
			  term: String, 
			  num_requested: Int = 30, 
			  start_at: Int = 0, 
			  language: String = "en") : Future[SearchResponse] = {    
    val search_params = SearchParameters.newBuilder
    .setNumRequested(num_requested)
    .setStartAt(start_at)
    .setLanguage(language)
    .build  
    
    val transform_message = OccurAsObjTransform.newBuilder
    .setId(id)
    .setFromType(id_type)
    .setParams(search_params)
    .build
    
    return (librarian_actor ? transform_message).mapTo[SearchResponse] 
  }

  // Get the references to Pages where this person, location, or organization identified by id, 
  // occurs as a subject of the provided term.
  def getOccurencesAsSubj(id: AccessIdentifier, 
			  id_type: ProteusType, 
			  term: String, 
			  num_requested: Int = 30, 
			  start_at: Int = 0, 
			  language: String = "en") : Future[SearchResponse] = {   
    val search_params = SearchParameters.newBuilder
    .setNumRequested(num_requested)
    .setStartAt(start_at)
    .setLanguage(language)
    .build  
    
    val transform_message = OccurAsSubjTransform.newBuilder
    .setId(id)
    .setFromType(id_type)
    .setParams(search_params)
    .build
    
    return (librarian_actor ? transform_message).mapTo[SearchResponse] 
  }

  // Get the references to Pages where this person, location, or organization identified by id, 
  // occurs having as its object the provided term.
  def getOccurrencesHasObj(id: AccessIdentifier, 
			   id_type: ProteusType, 
			   term: String, 
			   num_requested: Int = 30, 
			   start_at: Int = 0, 
			   language: String = "en") : Future[SearchResponse] = {    
    val search_params = SearchParameters.newBuilder
    .setNumRequested(num_requested)
    .setStartAt(start_at)
    .setLanguage(language)
    .build  
    
    val transform_message = OccurHasObjTransform.newBuilder
    .setId(id)
    .setFromType(id_type)
    .setParams(search_params)
    .build
    
    return (librarian_actor ? transform_message).mapTo[SearchResponse] 
  }

  // Get the references to Pages where this person, location, or organization identified by id, 
  // occurs having as its subject the provided term.
  def getOccurrencesHasSubj(id: AccessIdentifier, 
			    id_type: ProteusType, 
			    term: String, 
			    num_requested: Int = 30, start_at: Int = 0, 
			    language: String = "en") : Future[SearchResponse] = {
    
    val search_params = SearchParameters.newBuilder
    .setNumRequested(num_requested)
    .setStartAt(start_at)
    .setLanguage(language)
    .build  
    
    val transform_message = OccurHasSubjTransform.newBuilder
    .setId(id)
    .setFromType(id_type)
    .setParams(search_params)
    .build
    
    return (librarian_actor ? transform_message).mapTo[SearchResponse] 
  }

  /**
  * Get locations within radius of the location described by id
  */
  def getNearbyLocations(id: AccessIdentifier, radius: Int, 
			 num_requested: Int = 30, start_at: Int = 0, language: String = "en") : Future[SearchResponse] = {
    
    val search_params = SearchParameters.newBuilder
    .setNumRequested(num_requested)
    .setStartAt(start_at)
    .setLanguage(language)
    .build  
    
    val transform_message = NearbyLocationsTransform.newBuilder
    .setId(id)
    .setRadiusMiles(radius)
    .setParams(search_params)
    .build
    
    return (librarian_actor ? transform_message).mapTo[SearchResponse] 
  }

  /**
  * Use a dynamically loaded transform from id (of corresponding type id_type), where the name of the transform is 
  * transform_name. The librarian must have a end point supporting this transform loaded for this to succeed.
  */
  def useDynamicTransform(id: AccessIdentifier, id_type: ProteusType, transform_name: String,  
			  num_requested: Int = 30, start_at: Int = 0, language: String = "en") : Future[SearchResponse] = {
    
    val search_params = SearchParameters.newBuilder
    .setNumRequested(num_requested)
    .setStartAt(start_at)
    .setLanguage(language)
    .build  
    
    val dt_id = DynamicTransformID.newBuilder
    .setName(transform_name)
    .setFromType(id_type)
    .build
    
    val transform_message = DynamicTransform.newBuilder
    .setId(id)
    .setTransformId(dt_id)
    .setParams(search_params)
    .build
    
    return (librarian_actor ? transform_message).mapTo[SearchResponse] 
  }
  
  /*** Lookup Methods ***/
  
  /**
  * Request that the librarian look up a Collection by its reference (SearchResult) and return 
  * a Future to the full object.
  */
  def lookupCollection(result: SearchResult) : Future[Collection] = {
    // Sanity checking first
    if (result.getProteusType != ProteusType.COLLECTION)
      throw new IllegalArgumentException("Mismatched type with lookup method")
    
    val lookup_message = LookupCollection.newBuilder
    .setId(result.getId)
    .build
    
    return (librarian_actor ? lookup_message).mapTo[Collection]
  }
  
  /**
  * Request the librarian look up a Page by its result reference
  */
  def lookupPage(result: SearchResult) : Future[Page] = {
    // Sanity checking first
    if (result.getProteusType != ProteusType.PAGE)
      throw new IllegalArgumentException("Mismatched type with lookup method")
    
    val lookup_message = LookupPage.newBuilder
    .setId(result.getId)
    .build
    
    return (librarian_actor ? lookup_message).mapTo[Page]
  }
  
  /**
  * Request the librarian look up a Picture by its result reference
  */
  def lookupPicture(result: SearchResult) : Future[Picture] = {
    // Sanity checking first
    if (result.getProteusType != ProteusType.PICTURE)
      throw new IllegalArgumentException("Mismatched type with lookup method")
    
    val lookup_message = LookupPicture.newBuilder
    .setId(result.getId)
    .build
    
    return (librarian_actor ? lookup_message).mapTo[Picture]
  }
  
  /**
  * Request the librarian look up a Video by its result reference
  */
  def lookupVideo(result: SearchResult) : Future[Video] = {
    // Sanity checking first
    if (result.getProteusType != ProteusType.VIDEO)
      throw new IllegalArgumentException("Mismatched type with lookup method")
    
    val lookup_message = LookupVideo.newBuilder
    .setId(result.getId)
    .build
    
    return (librarian_actor ? lookup_message).mapTo[Video]
  }
  
  /**
  * Request the librarian look up a Audio clip by its result reference
  */
  def lookupAudio(result: SearchResult) : Future[Audio] = {
    // Sanity checking first
    if (result.getProteusType != ProteusType.AUDIO)
      throw new IllegalArgumentException("Mismatched type with lookup method")
    
    val lookup_message = LookupAudio.newBuilder
    .setId(result.getId)
    .build
    
    return (librarian_actor ? lookup_message).mapTo[Audio]
  }
  
  /**
  * Request the librarian look up a Person by its result reference
  */
  def lookupPerson(result: SearchResult) : Future[Person] = {
    if (result.getProteusType != ProteusType.PERSON)
      throw new IllegalArgumentException("Mismatched type with lookup method")    
    val lookup_message = LookupPerson.newBuilder
    .setId(result.getId)
    .build
    
    return (librarian_actor ? lookup_message).mapTo[Person]
  }
  
  // Request the librarian look up a Location by its result reference
  def lookupLocation(result: SearchResult) : Future[Location] = {
    if (result.getProteusType != ProteusType.LOCATION)
      throw new IllegalArgumentException("Mismatched type with lookup method")    
    val lookup_message = LookupLocation.newBuilder
    .setId(result.getId)
    .build
    
    return (librarian_actor ? lookup_message).mapTo[Location]
  }
  
  // Request the librarian look up a Page by its result reference
  def lookupOrganization(result: SearchResult) : Future[Organization] = {
    if (result.getProteusType != ProteusType.ORGANIZATION)
      throw new IllegalArgumentException("Mismatched type with lookup method")    
    val lookup_message = LookupOrganization.newBuilder
    .setId(result.getId)
    .build
    
    return (librarian_actor ? lookup_message).mapTo[Organization]
  }

  /** Utility functions to make interacting with the data easier **/

  // Take a ResultSummary and turn it into a string with html tags around the
  // highlighted text regions.
  def tagTerms(summary: ResultSummary, startTag: String = "<b>", endTag: String = "</b>") = {
    if (summary.getHighlightsCount > 0) 
      wrapTerms(summary.getText, summary.getHighlightsList.asScala.toList, startTag=startTag, endTag=endTag)
    else
      summary.getText
  }

  protected def wrapTerms(description: String, 
			  locations: List[TextRegion], 
			  startTag: String, 
			  endTag: String) : String = {
    if (locations.length == 0) {
      return ""
    } else if (locations.length == 1) {
      return startTag + description.slice(locations(0).getStart, locations(0).getStop) + endTag +
	    description.slice(locations(0).getStop, description.length)
    } else {
      return startTag + description.slice(locations(0).getStart, locations(0).getStop) + 
	    endTag + description.slice(locations(0).getStop, locations(1).getStart) + 
	    wrapTerms(description, locations.drop(1), startTag, endTag)
    }
  }

}
