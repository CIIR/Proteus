package ciir.proteus.app

import ciir.proteus._
import ciir.proteus.galago.GalagoAdapter
import com.twitter.finagle.builder._
import com.twitter.finagle.thrift.ThriftServerFramedCodec
import org.apache.thrift.protocol._
import java.net.InetSocketAddress

import org.lemurproject.galago.tupleflow.Parameters
import org.lemurproject.galago.tupleflow.Utility

object AdapterServer {
  def main(args: Array[String]) {
    val parameters = Parameters.parse(args.mkString(" "))
    if (!parameters.containsKey("adapter")) {
      println("Please provide an 'adapter' type in the parameters.");
      return;
    }

    val adapter = parameters.getString("adapter") match {
      // Hopefully we'll develop more of these in the future.
      case _ => GalagoAdapter(parameters)
    }
    val port : Int = if (parameters.containsKey("port")) {
      parameters.getLong("port").asInstanceOf[Int]
    } else {
      Utility.getFreePort
    }
    val service = new ProteusProvider.FinagledService(adapter, new TBinaryProtocol.Factory());

    val server : Server = ServerBuilder()
    .name(adapter.getClass.toString.split("\\.").last)
    .bindTo(new InetSocketAddress(port))
    .codec(ThriftServerFramedCodec())
    .build(service)  // calls start underneath
  }
}
