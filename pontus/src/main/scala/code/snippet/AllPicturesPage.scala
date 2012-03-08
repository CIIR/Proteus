/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


package code
package snippet

import code.lib._
import code.comet._
import code.comet.BookBag

import net.liftweb._
import http._
import net.liftweb.common.Logger
import net.liftweb.http.js.jquery.JqWiringSupport
import scala.xml.NodeSeq
import sitemap._
import util._
import Helpers._
import code.comet.TheCart

object AllPicturesPage extends Logger {
  // define the menu item for the page that
  // will display all pictures
  //lazy val menu = Menu.i("Pictures") / "picture" >> Loc.Snippet("Pictures", render)
  lazy val menu = Menu("Pictures "+{
      val num = TheCart.get.numPictures.get
      if (num == 0) {
        ""
      } else {
        "("+ num +")"
      }
    }) / "picture"

  // display the items
  def render = {
    info("SESSION ID: " + S.session.openOr("NONE") + ", LIST PICS")
    if (TheCart.get.queryText.isEmpty)
      "*" #> <h4>Oops, it looks like you haven't run a query!</h4>
    else if (TheCart.get.numPictures.get == 0)
      "*" #> <h4>No picture results for your query!</h4>
    else {
      "#piclist" #>
      Helpers.findOrCreateId(id =>  // make sure tbody has an id
        // when the cart contents updates
        WiringUI.history(TheCart.get.currentPicPage) {
          (old, nw, ns) => {
            // capture the tr part of the template

            val theTR = ("div ^^" #> "**")(ns)
            
            def ciToId(ci: PictureItem): String = ci.id + "_" + ci.hashCode

            // build a row out of any item
            def html(ci: PictureItem): NodeSeq = {
              
              ( "div [id]" #> ciToId(ci) &
               "@pic *" #> <a href={ci.item.getImgUrl}>
                  <img title="Click to see picture page at the Internet Archive" 
                    src={ci.item.getImgUrl}
                    height={"120"} 
                    style={"padding: 7px 7px 7px 7px"}>
                  </img>
                           </a> 
               
              )(theTR)
            }
            
            // calculate the delta between the lists and
            // based on the deltas, emit the current jQuery
            // stuff to update the display
            JqWiringSupport.calculateDeltas(old, nw, id)(ciToId _, html _)
          }
        }) & 
      "@query" #> WiringUI.asText(TheCart.get.wiredQuery) &
      "@slice" #> ("displaying items " + (TheCart.get.firstPic+1) + " to " + (math.min((TheCart.get.firstPic + TheCart.get.picsPerPage), TheCart.get.numPictures.get))) &
      "@picsCount" #> WiringUI.asText(TheCart.get.numPictures) &
      "@right [onclick]" #> SHtml.ajaxInvoke(() => TheCart.get.picPageRight) &
      "@left [onclick]" #> SHtml.ajaxInvoke(() => TheCart.get.picPageLeft)   
    }
  } // end render

}