package edu.umass.ciir.proteus.triton.core

import akka.actor.Actor
import edu.umass.ciir.proteus.protocol.ProteusProtocol._
import edu.umass.ciir.proteus.triton.core.ProteusAPI

// The base trait for the librarian and for the end point (library). 
trait LibraryServer extends Actor with ProteusAPI {
  def serverHostname : String
  def serverPort : Int
  
  override def preStart() = {
    remote.start(serverHostname, serverPort)
    .register(proteus_service_name, self)
  }
  
  // Actor message handler
  def receive: Receive = connectionManagement orElse queryManagement orElse lookupManagement
  
  // Abstract methods to be defined elsewhere
  protected def connectionManagement : Receive
  protected def queryManagement : Receive
  protected def lookupManagement : Receive 
  protected def supportsType(ptype: ProteusType) : Boolean
  protected def supportsDynTransform(dtID: DynamicTransformID) : Boolean
}
