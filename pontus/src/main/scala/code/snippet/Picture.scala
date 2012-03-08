/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package code
package snippet

// TODO: CONVERT

//import org.galagosearch.core.tools.Search._
import net.liftweb._
import code.snippet._
import http._
//import org.galagosearch.tupleflow.Parameters
import scala.collection.mutable.MutableList
import util._
import Helpers._
import scala.collection.JavaConversions._
//import org.galagosearch.core.retrieval.BadOperatorException
//import org.galagosearch.core.tools.Search.SearchResult
import code.comet._
import code.lib._
object Picture extends Query {

import code.lib.Cart
import code.comet.BookBag._
  
  def printPageInfo = {
    var id = S.param("d").openOr("no document")
    var doctext = TheCart.get.findPictureItem(id.toInt).item.getTitle
    if (doctext != null)
    ".thedoc" #> {doctext}
    else
      ".thedoc" #> "Document not found.."
  }
}
