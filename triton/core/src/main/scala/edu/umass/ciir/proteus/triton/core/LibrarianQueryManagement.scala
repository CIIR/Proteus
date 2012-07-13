// Trait providing query management and transform management for the librarian class
package edu.umass.ciir.proteus.triton.core

import akka.actor.Actor._
import akka.actor.Actor
import akka.actor.ActorRef
import akka.dispatch._
import scala.collection.JavaConverters._

import edu.umass.ciir.proteus.protocol.ProteusProtocol._

trait LibrarianQueryManagement { this: Actor =>
  // All these message handlers do essentially the same thing (which is a work around for the bug), 
  // and that is to call sendOrForwardTo, and then send that response back to the client.
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
  }
    	  			
  // Methods this trait relies upon that must be implemented in another trait/class
  protected def typeSupport(ptypes: List[ProteusType]) : List[String]
  protected def groupMemberTypeSupport(ptype: ProteusType, groupId: String) : List[String]
  protected def sendOrForwardTo(members: List[String], message: Any, chan: UntypedChannel)
  protected def getLibrary(id: String) : RemoteLibrary
}
