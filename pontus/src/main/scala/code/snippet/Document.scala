package code
package snippet

// TODO: This file has conversions to do

import net.liftweb.sitemap.Loc
import net.liftweb.sitemap.Menu
import net.liftweb._
import net.liftweb.common._
import code.snippet._
import http._
//import org.galagosearch.tupleflow.Parameters
import util._
import Helpers._
import scala.collection.JavaConversions._
//import org.galagosearch.core.retrieval.BadOperatorException
//import org.galagosearch.core.tools.Search.SearchResult
import code.comet._
import code.lib.Cart
import code.comet.BookBag._

object Timer {
    var start:Long = 0L
    var end:Long = 0L
    var activity:String = ""
    
    def go(what: String) = {
        start = System.currentTimeMillis
        activity = what
    }
    def stop = {
        end = System.currentTimeMillis
        "QUERYTIME: " + activity + " took " + (end-start)/1000.0 + "s"
    }
}


object Document extends Query {
 
  def getQuery = {
    <input size="50" name="q" value={TheCart.get.queryText} />
  }

  def clearCart = {
    S.session.foreach(
      _.sendCometActorMessage("BookBag", Empty, "clear"))
    S.redirectTo("/index")
  }

   
  def getOCR = {
    var id = S.param("d").openOr("no document")
    val doc = TheCart.get.findPageItem(id.toInt)
    if(doc!=null) {
      val ocr = doc.item.getSummary.getText.toString
      ".thedoc" #> <div id="ocrdata"><p>{ocr}</p></div>
    }
    else
      ".thedoc" #> <div id="ocrdata"><p>Error document not found</p></div>
  }
}
