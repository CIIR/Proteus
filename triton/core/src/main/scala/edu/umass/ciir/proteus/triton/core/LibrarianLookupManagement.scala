// Provide lookup management for the librarian.
package edu.umass.ciir.proteus.triton.core

import akka.actor.Actor._
import akka.actor.UntypedChannel
import akka.actor.Actor
import akka.actor.ActorRef
import akka.config.Supervision._
import akka.dispatch._
import scala.collection.JavaConverters._

import edu.umass.ciir.proteus.protocol.ProteusProtocol._

trait LibrarianLookupManagement { this: Actor =>
  // Handle messages to the librarian for looking up resource data.
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
