package ciir.proteus.app

import ciir.proteus._
import com.twitter.finagle.builder._
import com.twitter.finagle.thrift.ThriftServerFramedCodec
import org.apache.thrift.protocol._
import java.net.InetSocketAddress

object RemoteDataServer {
  def main(args: Array[String]) {
    val dataSource = new RemoteRandomDataProvider()
    
    val service = new ProteusProvider.FinagledService(dataSource, new TBinaryProtocol.Factory());

    val server : Server = ServerBuilder()
      .name("RandomProteusDataService")
      .bindTo(new InetSocketAddress(8888))
      .codec(ThriftServerFramedCodec())
      .build(service)  // calls start underneath
  }
}
