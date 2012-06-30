// ************ Implementation of a Basic Librarian (Library Management/Router) *************
// BasicLibrarian class puts all the pieces of a basic librarian together along with 
// the settings for running the server. Specifically, we are extending LibraryServer, which 
// forces us to implement all the necessary methods, and then we mix in the basic librarian 
// connection, query, and lookup management traits.
package edu.umass.ciir.proteus.triton.core

import edu.umass.ciir.proteus.protocol.ProteusProtocol._

class BasicLibrarian(settings: ServerSetting) extends 
LibraryServer with 
LibrarianConnectionManagement with 
LibrarianQueryManagement with 
LibrarianLookupManagement {
  val serverHostname : String = settings.hostname
  val serverPort : Int = settings.port
}

object basicLibrarianApp extends App {
  val librarianService = try {
    actorOf(new BasicLibrarian(ServerSetting(args(0), args(1).toInt))).start()
  } catch {
    case ex: Exception => 
      println("Unable to load hostname and port from arguments, defaulting to localhost:8081")
    actorOf(new BasicLibrarian(ServerSetting("localhost", 8081))).start()
  }
}
