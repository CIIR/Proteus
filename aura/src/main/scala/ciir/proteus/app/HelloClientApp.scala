package ciir.aura.app

import com.twitter.util.Future
import com.twitter.conversions.time._
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.thrift.{ ThriftClientFramedCodec, ThriftClientRequest }

import ciir.aura.hello._

object HelloClientApp extends App {

  val helloService = ClientBuilder()
    .hosts("localhost:8001")
    .codec(ThriftClientFramedCodec())
    .hostConnectionLimit(1)
    .build()

  val helloClient = new HelloService.FinagledClient(helloService)

  val response: Future[String] = helloClient.sayHello(HelloMsg("world"))

  val result  = response(3 seconds)

  println(result)

  helloService.release()
}