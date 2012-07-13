// Trait for providing lookup management for the end point.
// Relies crucially on the lookupX methods that must be implemented elsewhere.
package edu.umass.ciir.proteus.triton.core

import akka.dispatch._
import akka.actor.Actor._
import akka.actor.Actor
import edu.umass.ciir.proteus.protocol.ProteusProtocol._
import scala.collection.JavaConverters._

// Note: The analagous situation with object variables can be done using extensions, the librarian/manager can stay the same
trait EndPointLookupManagement { this: Actor =>  
  // Handle receiving the lookup messages. Note, the same bug affects this trait as well.
  protected def lookupManagement : Receive = {
    case lookup_message : Lookup => {
      Future {
	try {
	  retrieveResource(id: AccessIdentifier, proteus_type: ProteusType)
	} catch {
	  // TODO(irmarc): Switch all the error printing to Akka logging
    	  case ex: Exception => 
	    System.err.println("Lookup throw an exception:" + ex.toString)
    	  ex.printStackTrace
    	  ProteusObject.newBuilder
    	  .setId(lookup.getId.toBuilder.setError("Lookup Collection threw an exception: " + ex.toString).build)
    	  .build	  
	}
      } onResult { 
	case result : ProteusObject  => sender ! result 
      }
    }
  }

  // Abstract in this trait
  def retrieveResource(id: AccessIdentifier, proteus_type: ProteusType) : ProteusObject
  protected def errorResponse(errorText: String): SearchResponse
}

