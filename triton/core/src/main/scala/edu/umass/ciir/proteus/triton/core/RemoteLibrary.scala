package edu.umass.ciir.proteus.triton.core

import akka.actor.Actor._
import akka.actor.UntypedChannel
import akka.actor.Actor
import akka.actor.ActorRef
import akka.config.Supervision._
import akka.dispatch._
import scala.collection.JavaConverters._

import edu.umass.ciir.proteus.protocol.ProteusProtocol._

// A local class to make interacting with the remote actors (libraries) a little cleaner.
class RemoteLibrary(details: ConnectLibrary) extends ProteusAPI {
  // Connect to the remote actor (library)
  val client = {
    val lib_actor = remote.actorFor(proteus_service_name, details.getHostname, details.getPort)
    // Tell the library we have them connected
    val connected_message = LibraryConnected.newBuilder
    .setResourceId(details.getRequestedKey)
    .build
    
    lib_actor ! connected_message
    lib_actor
  }
  
  // Methods for accessing information about this library
  def getHostname: String = details.getHostname
  def getPort: Int = details.getPort.toInt
  def getGroupId: String = details.getGroupId
  def getSupportedTypes: List[ProteusType] = details.getSupportedTypesList.asScala.toList
  def getDynamicTransforms: List[DynamicTransformID] = details.getDynamicTransformsList.asScala.toList
  def supportsType(ptype: ProteusType): Boolean = details.getSupportedTypesList.asScala.contains(ptype)
  def supportsDynTransform(dtID: DynamicTransformID): Boolean = details.getDynamicTransformsList.asScala.contains(dtID)
  
  // Forward a message to the remote actor (meaning that supposedly, it will reply directly to the client)
  def forwardMessage(message: Any, channel: UntypedChannel) {
    // When the akka bug is fixed we can instead do: client forward message
    client ? message onResult { case r => channel ! r }
  }
}


