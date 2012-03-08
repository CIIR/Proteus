package bootstrap.liftweb

import net.liftweb._
import util._
import Helpers._

import common._
import http._
import sitemap._
import Loc._
import mapper._

import code.model._

import code.snippet._
import code.lib._
import auth._
import net.liftweb.actor._

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot {
  def boot {

    // where to search snippet
    LiftRules.addToPackages("code")

    // Build SiteMap    
    def sitemap = SiteMap(
            Menu.i("New Search") / "clear",
            Menu.i("OCR Text (hide)") / "doc" >> Hidden,
            Menu.i("Annotate Page Entities (hide)") / "elabel" >> Hidden,
            Menu.i("DBPedia Query (hide)") / "dbpquery" >> Hidden,
            Menu.i("Entity Data (hide)") / "ent" >> Hidden,
            Menu.i("Map Entities (hide)") / "map" >> Hidden,
            Menu.i("Entity Query From Page (hide)") / "equery" >> Hidden,
            Menu.i("Book Query From Entity (hide)") / "dquery" >> Hidden,
            Menu.i("Duplicates") / "dupes" >> Hidden,
            Menu.i("Index") / "index" >> Hidden,
            Menu.i("Logging") / "/static/index" >> Hidden,
            SearchPage.menu,
            AllItemsPage.menu,
            AnItemPage.menu,
            AllPicturesPage.menu,
            AllEntitiesPage.menu
      
            // MapEntities.menu
        )
    

    def sitemapMutators = User.sitemapMutator

    // set the sitemap.  Note if you don't want access control for
    // each page, just comment this line out.
    LiftRules.setSiteMapFunc(() => sitemap)//sitemapMutators(sitemap))

    // Use jQuery 1.4
    LiftRules.jsArtifacts = net.liftweb.http.js.jquery.JQuery14Artifacts

    //Show the spinny image when an Ajax call starts
    LiftRules.ajaxStart =
      Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)
    
    // Make the spinny image go away when it ends
    LiftRules.ajaxEnd =
      Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    // What is the function to test if a user is logged in?
    LiftRules.loggedInTest = Full(() => User.loggedIn_?)

    // Use HTML5 for rendering
    LiftRules.htmlProperties.default.set((r: Req) =>
      new Html5Properties(r.userAgent))    
      
    LiftRules.httpAuthProtectedResource.append {
            case Req("log" :: "all" :: _, _, GetRequest) => Full(AuthRole("admin")) 
        }
    LiftRules.authentication = HttpBasicAuthentication("lift") { 
            case ("umass", "protprot", req) => 
                println("You are authenticated!") 
                userRoles(AuthRole("admin")) 
                true 
        }
    
        LiftRules.dispatch.append(ShareCart)
        LiftRules.dispatch.append(LogServer)

  }
}
