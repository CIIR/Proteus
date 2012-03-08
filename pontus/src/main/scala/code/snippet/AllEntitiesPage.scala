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
//import edu.umass.ciir.web.snippet.Document
//import edu.umass.ciir.web.snippet.Entity
import net.liftweb._
import http._
import net.liftweb.common.Logger
import net.liftweb.http.js.jquery.JqWiringSupport
import scala.xml.NodeSeq
import sitemap._
import util._
import Helpers._

object AllEntitiesPage extends Logger {
    // define the menu item for the page that
    // will display all items
    // lazy val menu = Menu.i("Entities") / "entity" >>
    // Loc.Snippet("Entities", render)
    lazy val menu = Menu("Entities "+{
            val num = TheCart.get.numEntities.get
            if (num == 0) {
                ""
            } else {
                "("+ num +")"
            }
        }) / "entity"

    // display the items
    def render = {
        info("SESSION ID: " + S.session.openOr("NONE") + ", LIST DUPLICATES")
        if (TheCart.get.queryText.isEmpty)
            "*" #> <h4>Oops, it looks like you haven't run a query!</h4>
        else if (TheCart.get.numEntities.get == 0)
            "*" #> <h4>No entity results for your query!</h4>
        else {
            "#entities" #> (
                "tbody" #>
                Helpers.findOrCreateId(id =>  // make sure tbody has an id
                    // when the cart contents updates
                    WiringUI.history(TheCart.filtered_entities) {
                        (old, nw, ns) => {
                            // capture the tr part of the template
                            val theTR = ("tr ^^" #> "**")(ns)

                            def ciToId(ci: EntityItem): String = ci.id + "_" + ci.hashCode

                            // build a row out of a cart item
                            def html(ci: EntityItem): NodeSeq = {
                                ( "tr [id]" #> ciToId(ci) &
                                 "a *" #> <strong>{ci.item.getTitle}</strong> &
                                 "a [href]" #> Entity.getEntityLink(ci.hashCode.toString, ci.hashCode.toString) & //AnEntityPage.menu.calcHref(ci) &
                                 "@cat *"  #> {ci.item.getProteusType.getValueDescriptor.getName.capitalize} &
                                 "@url *"  #> {Entity.getAdditionalInfo(ci)} &
                                 "@ident *" #> {ci.hashCode}
                                )(theTR)

                            }

                            // calculate the delta between the lists and
                            // based on the deltas, emit the current jQuery
                            // stuff to update the display
                            JqWiringSupport.calculateDeltas(old, nw, id)(ciToId _, html _)
                        }
                    })) & 
            "@query" #> WiringUI.asText(TheCart.wiredQuery) &
            "@entCount" #> WiringUI.asText(TheCart.numEntities)
        } // end else statement
    } //end render
}