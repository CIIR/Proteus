/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package code
package snippet

import code.model._
import code.comet._
import net.liftweb._


import http._
import util._
import Helpers._
import sitemap._
import Loc._
import net.liftweb.common.Empty
import scala.xml.NodeSeq


object SearchPage {
    def menu = Menu.i("Search (hide)") / "search" >> Hidden >> If(() => S.param("q").isDefined, "Need Search Term") >> 
    Snippet("Items", render: (NodeSeq => NodeSeq))

  
    def render = {
        TheCart.searchBooks(S.param("q").open_!, S.param("lang").openOr("english"))
    
        S.session.foreach(
            _.sendCometActorMessage("BookBag", Empty,
                                    "showBooks"))
        
        "tbody *" #> NodeSeq.Empty
    
        S.redirectTo("/item")

    }
}

