// Trait for providing lookup management for the end point.
// Relies crucially on the lookupX methods that must be implemented elsewhere.
package edu.umass.ciir.proteus.triton.core

import akka.dispatch._
import akka.actor.Actor._
import akka.actor.Actor
import akka.config.Supervision._
import edu.umass.ciir.proteus.protocol.ProteusProtocol._
import scala.collection.JavaConverters._
// Note: The analagous situation with object variables can be done using extensions, the librarian/manager can stay the same

trait EndPointLookupManagement { 
  this: Actor =>  
    def lookupCollection(accessID: AccessIdentifier) : Collection
  def lookupPage(accessID: AccessIdentifier) : Page
  def lookupPicture(accessID: AccessIdentifier) : Picture
  def lookupVideo(accessID: AccessIdentifier) : Video
  def lookupAudio(accessID: AccessIdentifier) : Audio
  def lookupPerson(accessID: AccessIdentifier) : Person
  def lookupLocation(accessID: AccessIdentifier) : Location
  def lookupOrganization(accessID: AccessIdentifier) : Organization  

  protected def errorResponse(errorText: String): SearchResponse 

  // Handle receiving the lookup messages. Note, the same bug affects this trait as well.
  protected def lookupManagement : Receive = {
    case lookup: LookupCollection =>
      val chan = self.channel 
    Future { 
      try {
    	lookupCollection(lookup.getId) 
      } catch {
    	case ex: Exception => System.err.println("Lookup Collection threw an exception")
    	ex.printStackTrace
    	Collection.newBuilder
    	.setId(lookup.getId.toBuilder.setError("Lookup Collection threw an exception --> " + ex.toString).build)
    	.build
      }
    } onResult { case r: Collection => chan ! r }    	  	
    case lookup: LookupPage =>
      val chan = self.channel
    Future { 
      try {
    	lookupPage(lookup.getId) 
      } catch {
    	case ex: Exception => System.err.println("Lookup Page threw an exception")
    	ex.printStackTrace
    	Page.newBuilder
    	.setId(lookup.getId.toBuilder.setError("Lookup Page threw an exception --> " + ex.toString).build)
    	.build
      }
    } onResult { case r: Page => chan ! r }
    
    case lookup: LookupPicture =>
      val chan = self.channel
    Future {
      try {
    	lookupPicture(lookup.getId) 
      } catch {
    	case ex: Exception => System.err.println("Lookup Picture threw an exception")
    	ex.printStackTrace
    	Picture.newBuilder
    	.setId(lookup.getId.toBuilder.setError("Lookup Picture threw an exception --> " + ex.toString).build)
    	.build
      }
    } onResult { case r: Picture => chan ! r }
    
    case lookup: LookupVideo =>
      val chan = self.channel
    Future {
      try {
    	lookupVideo(lookup.getId) 
      } catch {
    	case ex: Exception => System.err.println("Lookup Video threw an exception")
    	ex.printStackTrace
    	Video.newBuilder
    	.setId(lookup.getId.toBuilder.setError("Lookup Video threw an exception --> " + ex.toString).build)
    	.build
      }
    } onResult { case r: Video => chan ! r }
    
    case lookup: LookupAudio =>
      val chan = self.channel
    Future {
      try {
    	lookupAudio(lookup.getId) 
      } catch {
    	case ex: Exception => System.err.println("Lookup Audio threw an exception")
    	ex.printStackTrace
    	Audio.newBuilder
    	.setId(lookup.getId.toBuilder.setError("Lookup Audio threw an exception --> " + ex.toString).build)
    	.build
      }
    } onResult { case r: Audio => chan ! r }
    
    case lookup: LookupPerson =>
      val chan = self.channel
    Future { 
      try {
    	lookupPerson(lookup.getId) 
      } catch {
    	case ex: Exception => System.err.println("Lookup Person threw an exception")
    	ex.printStackTrace
    	Person.newBuilder
    	.setId(lookup.getId.toBuilder.setError("Lookup Person threw an exception --> " + ex.toString).build)
    	.build
      }
    } onResult { case r: Person => chan ! r }
    
    case lookup: LookupLocation =>
      val chan = self.channel
    Future {
      try {
    	lookupLocation(lookup.getId) 
      } catch {
    	case ex: Exception => System.err.println("Lookup Location threw an exception")
    	ex.printStackTrace
    	Location.newBuilder
    	.setId(lookup.getId.toBuilder.setError("Lookup Location threw an exception --> " + ex.toString).build)
    	.build
      }
    } onResult { case r: Location => chan ! r }
    
    case lookup: LookupOrganization =>
      val chan = self.channel
    Future { 
      try {
    	lookupOrganization(lookup.getId) 
      } catch {
    	case ex: Exception => System.err.println("Lookup Organization threw an exception")
    	ex.printStackTrace
    	Organization.newBuilder
    	.setId(lookup.getId.toBuilder.setError("Lookup Organization threw an exception --> " + ex.toString).build)
    	.build
      }
    } onResult { case r: Organization => chan ! r }

  }
}

