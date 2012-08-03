package ciir.proteus

import java.io.File

import org.eclipse.jetty.server.nio.SelectChannelConnector
import org.eclipse.jetty.server.{ Server }
import org.eclipse.jetty.server.handler.ContextHandlerCollection
import org.eclipse.jetty.webapp.WebAppContext
import org.eclipse.jetty.servlet.{ DefaultServlet, ServletContextHandler, ServletHolder }

import org.lemurproject.galago.tupleflow.Parameters

object JettyMain {
  
  def main(args: Array[String]) = {
    val (fileArgs: Array[String], nonFileArgs: Array[String]) = args.partition { 
      arg : String => 
	new File(arg).exists()
    }

    // Read in all files
    for (path <- fileArgs; f = new File(path)) {
      val tmp = Parameters.parse(f)
      ProteusServlet.parameters.copyFrom(tmp)
    }

    // Command-line overrides file args
    val tmp = new Parameters(nonFileArgs)
    ProteusServlet.parameters.copyFrom(tmp)

    if (!ProteusServlet.parameters.containsKey("servers")) {
      println("Please provide at least one entry in 'servers' in the parameters.")
      System.exit(1)
    } else if(!ProteusServlet.parameters.containsKey("port")) {
      println("Please provide a port for the servlet to bind to, in the parameters.")
      System.exit(1)
    }

    val server: Server = new Server

    server setGracefulShutdown 5000
    server setSendServerVersion false
    server setSendDateHeader true
    server setStopAtShutdown true

    val connector = new SelectChannelConnector
    connector setPort ProteusServlet.parameters.getLong("port").toInt
    connector setMaxIdleTime 90000
    server addConnector connector

    val webapp = "morpheus/src/main/webapp"
    val webApp = new WebAppContext

    webApp setContextPath "/"
    webApp setResourceBase webapp
    webApp setDescriptor (webapp+"/WEB-INF/web.xml");

    server setHandler webApp

    server.start()
  }
}