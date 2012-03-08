package code
package comet


import code.lib._

import net.liftweb._
import code.snippet.Document
import code.snippet.Duplicates
import common._
import http._
import util._
import js._
import js.jquery._
import JsCmds._
import scala.xml.NodeSeq
import Helpers._

/**
 * What's the current cart for this session
 */
object TheCart extends SessionVar(new Cart())

/**
 * The BookBag is the CometActor the represents the shopping cart
 */
class BookBag extends CometActor {
  // our current cart
  private var cart = TheCart.get
  

  def render = {
println("Cart is empty? " + cart.queryText.isEmpty)
    if (cart.queryText.isEmpty)
      "*" #> NodeSeq.Empty
    else {
    "#contents" #> (
      "tbody" #> 
      Helpers.findOrCreateId(id =>  // make sure tbody has an id
        // when the cart contents updates
        WiringUI.history(cart.currentPage) {
          (old, nw, ns) => {
            
            // capture the tablerow part of the template
            val theTR = ("@booktable ^^" #> "**")(ns)
            
            def ciToId(ci: BookItem): String = ci.id + "_" + ci.selected

            // build a row out of a cart item
            def html(ci: BookItem): NodeSeq = {
              
              ( "tr [id]" #> ciToId(ci) &
               "@title *" #> <i>{ci.item.getTitle}</i> &
               "@img *" #> <a class="thumbnail" href={ci.item.getThumbUrl}> <img src = {ci.item.getImgUrl} width={"90"} height={"120"} alt="Image Loading" title="Click to view book at the Internet Archive"/>
               <span><img src={ci.item.getImgUrl} width={"300"} height={"500"}></img></span> </a> &
               "@map *" #> <a href={S.hostAndPath + "/map?book=" + ci.hashCode}>Map Locations</a> &
               "@dupes *" #> Duplicates.dupeLinked(ci.hashCode.toString)
              )(theTR)
              
            }
            
            // calculate the delta between the lists and
            // based on the deltas, emit the current jQuery
            // stuff to update the display
            JqWiringSupport.calculateDeltas(old, nw, id)(ciToId _, html _)
          }
        })) &
    "@query" #> WiringUI.asText(cart.wiredQuery) &
    "@slice" #> ("Displaying books " + (cart.first.get+1) + " to " + (math.min((cart.first.get + cart.itemsPerPage), cart.subtotal.get))) &
    "#subtotal" #> WiringUI.asText(cart.subtotal) & // display the subttotal
    "#total" #> WiringUI.asText(cart.total) &
    "@right [onclick]" #> SHtml.ajaxInvoke(() => TheCart.get.pageRight) &
    "@left [onclick]" #> SHtml.ajaxInvoke(() => TheCart.get.pageLeft) 
    }
  }
   
  /**
   * Process messages from external sources
   */
  override def lowPriority = {
    // if someone sends us a new cart
    case SetNewCart(newCart) => {
      // unregister from the old cart
      unregisterFromAllDepenencies()

      // remove all the dependencies for the old cart
      // from the postPageJavaScript
      theSession.clearPostPageJavaScriptForThisPage()

      // set the new cart
      cart = newCart

      // do a full reRender including the fixed render piece
      reRender(true)
    }
    case "pageUpdate" => reRender(true)
    case "showBooks" => reRender(true)
    case "clear" => cart.clearCart
  }
}



/**
 * Set a new cart for the BookBag
 */
case class SetNewCart(cart: Cart)

object BookBag extends Logger {
  def logLink(href: String, text: String, logText: String) = {
      SHtml.a(() => {info(logText); JsCmds.RedirectTo(href)}, <span>{text}</span>)
    }

  
}