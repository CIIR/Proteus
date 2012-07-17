package ciir.aura.hello

import com.twitter.util.Future

class HelloServiceImpl extends HelloService.FutureIface {

  override def sayHello(msg: HelloMsg): Future[String] =
    Future.value("Hello, %s!" format msg.name)

}


