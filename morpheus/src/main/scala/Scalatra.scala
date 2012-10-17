import ciir.proteus._
import org.scalatra._
import javax.servlet.ServletContext
//import org.eclipse.jetty.webapp.WebAppContext
//import org.eclipse.jetty.webapp.WebAppContext._

/**
 * This is the Scalatra bootstrap file. You can use it to mount servlets or
 * filters. It's also a good place to put initialization code which needs to
 * run at application start (e.g. database configurations), and init params.
 */
class Scalatra extends LifeCycle {
  override def init(context: ServletContext) {

    /**
     * Set up parameters for the servlet here,
     * based on the environment setting. This way we only have to
     * load parameters once based on the environment, then we just
     * look them up afterwards.
     *
     * IF YOU WANT TO MODIFY CONFIGURATION, DO IT HERE
     */
    import ProteusServlet._
    val configuration =  System.getenv("org.scalatra.environment")
    if (configuration == null || configuration.startsWith("dev")) {
      // dev configuration
      port = 9009
      hosts = Site("ayr.cs.umass.edu", 8201) +: hosts
    } else {
      // prod configuration
      port = 9000
      hosts = Site("ayr.cs.umass.edu", 8200) +: hosts
    }

    val servlet = new ProteusServlet
    context.mount(servlet, "/*")
  }
}
