package ciir.aura.app

import ciir.aura.hello._

object HelloServerApp extends App {
  
  val helloServer = new HelloServiceImpl with HelloService.ThriftServer {
    override val thriftPort = 8001
    override val serverName = "hello"
  }

  helloServer.start()
}
