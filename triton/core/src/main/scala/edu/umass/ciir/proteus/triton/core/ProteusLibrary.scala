package edu.umass.ciir.proteus.triton.core

import akka.dispatch._
import akka.actor.Actor._
import akka.actor.Actor
import akka.config.Supervision._
import edu.umass.ciir.proteus.protocol.ProteusProtocol._
import scala.collection.JavaConverters._

// ServerSetting is used to pass around the hostname and port for connecting to some server.
case class ServerSetting(hostname: String, port: Int)
case class EndPointConfiguration(dataType : ProteusType, resourcePath: String)

/**
 * The base trait for scala object model of the Proteus API.
 * Provides implicit conversion between strings and proteus types, as well as 
 * information (maps) about the type hierarchy. Also provides the name of the service that 
 * proteus actors are supporting.
 */
trait ProteusAPI {
  implicit def str2ProteusType(str: String) = convertType(str)

  // The name of the service being provided by any
  val proteus_service_name = "library-service" 
  def numProteusTypes : Int = 8
  
  // What a given type can have contents of type
  // For example, the first mapping states that a collection can contain
  // pages. The next mapping says a page may contain picutres, videos,
  // and so on.
  //
  // Note that we define "contains" w.r.t. the modality in question,
  // for example an audio object may contain persons, locations, but 
  // in the sense of them being mentioned, whereas a page would contain
  // the text of those entities.
  val contents_map = Map(ProteusType.COLLECTION	-> List(ProteusType.PAGE),
			 ProteusType.PAGE -> List(ProteusType.PICTURE,
						  ProteusType.VIDEO,
						  ProteusType.AUDIO,
						  ProteusType.PERSON,
						  ProteusType.LOCATION,
						  ProteusType.ORGANIZATION),
			 ProteusType.PICTURE -> List(ProteusType.PERSON,
						     ProteusType.LOCATION,
						     ProteusType.ORGANIZATION),
			 ProteusType.VIDEO -> List(ProteusType.PERSON,
						   ProteusType.LOCATION,
						   ProteusType.ORGANIZATION),
			 ProteusType.AUDIO -> List(ProteusType.PERSON,
						   ProteusType.LOCATION,
						   ProteusType.ORGANIZATION))
  
  // An mapping of objects to what objects may contain them. A reverse mapping
  // of 'contents_map', defined above.
  val container_map = Map(ProteusType.PAGE -> List(ProteusType.COLLECTION), 
			  ProteusType.PICTURE -> List(ProteusType.PAGE), 
			  ProteusType.VIDEO -> List(ProteusType.PAGE),
			  ProteusType.AUDIO -> List(ProteusType.PAGE),
			  ProteusType.PERSON -> List(ProteusType.PAGE,
						     ProteusType.PICTURE,
						     ProteusType.VIDEO,
						     ProteusType.AUDIO),
			  ProteusType.LOCATION -> List(ProteusType.PAGE,
						       ProteusType.PICTURE,
						       ProteusType.VIDEO,
						       ProteusType.AUDIO), 
			  ProteusType.ORGANIZATION -> List(ProteusType.PAGE,
							   ProteusType.PICTURE,
							   ProteusType.VIDEO,
							   ProteusType.AUDIO))
  
  // Returns the list of types that may contain the given type. Returns
  // an empty list if no containers can be found.
  //
  // TODO(irmarc) : Remove the null return, return [] instead.
  def containerFor(ptype: ProteusType) : List[ProteusType] = 
    if(container_map.contains(ptype)) container_map(ptype) else null
  
  // Converts a string into the given Proteus type.
  // TODO(irmarc) : Should we ever need to do this? The protocol
  //                buffer type should be the only one we should ever use.
  def convertType(strType: String) : ProteusType = strType match {
    case "collection" => ProteusType.COLLECTION
    case "page" => ProteusType.PAGE
    case "picture" => ProteusType.PICTURE
    case "video" => ProteusType.VIDEO
    case "audio" => ProteusType.AUDIO
    case "person" => ProteusType.PERSON
    case "location" => ProteusType.LOCATION
    case "organization" => ProteusType.ORGANIZATION
    case _ => throw new IllegalArgumentException("Invalid proteus type: " + strType)
  }

  // Convert a list of strings to a list of types in bulk.
  def convertTypes(types: List[String]) : List[ProteusType] = {
    types.map(convertType(_))
  }
}

