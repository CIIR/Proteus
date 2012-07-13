// Provide lookup management for the librarian.
package edu.umass.ciir.proteus.triton.core

import akka.actor.Actor._
import akka.actor.Actor
import akka.actor.ActorRef
import akka.dispatch._
import scala.collection.JavaConverters._

import edu.umass.ciir.proteus.protocol.ProteusProtocol._

trait LibrarianLookupManagement { this: Actor =>
  // Handle messages to the librarian for looking up resource data.
  def lookupManagement : Receive = {
    case lookup_message : Lookup => {
      val library : RemoteLibrary = getLibrary(lookup_message.getId.getResourceId)
      // TODO(irmarc): Remove this null here. Use an option or box
      if (library == null) {
	self.reply(ProteusObject.newBuilder
		   .setId(lookup.getId.toBuilder
    	  		  .setError("Received lookup with unrecognized resource ID: " + lookup.getId.getResourceId)
    	  		  .build)
		   .build)
      } else {
	lib.forwardMessage(lookup, self.channel)
      }
    }
  }
				 
  // Method implemented elsewhere.
  protected def getLibrary(id: String) : RemoteLibrary
}
