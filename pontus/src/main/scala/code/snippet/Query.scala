package code
package snippet

//import proteus.web
// TODO: NEEDS CONVERT

import java.io.FileWriter
import java.io.IOException
import net.liftweb._
import net.liftweb.common._
//import edu.umass.ciir.megabooks.tools.Search
import http._ 
//import org.galagosearch.core.index.StructuredIndex
//import org.galagosearch.core.tools.Search.SearchResult
//import org.galagosearch.tupleflow.Parameters
import scala.collection.mutable.ListBuffer
import scala.xml.NodeSeq
import util._
import Helpers._

import scala.collection.JavaConversions._
//import edu.umass.ciir.megabooks.traversal.StemmingTraversal
//import edu.umass.ciir.megabooks.index.EntityNameReader
//import org.galagosearch.core.index.DocumentNameReader

import scala.collection.mutable.HashSet
import java.io.File

import edu.umass.ciir.proteus.protocol.ProteusProtocol._
import edu.umass.ciir.proteus._

import scala.collection.JavaConversions._

object Librarian extends Logger with ProteusAPI {
  val library = new LibrarianClient("mildura.cs.umass.edu", 8080)

  def performSearch(query:String, typesRequested: List[String]) : List[SearchResult] = {
      
        // catch illegal operator exception here
        val processed = processQuery(trace("processing query", query))
        
        info("scala.Query.performSearch => PROCESSED_QUERY: " + processed)
        Timer.go("Lower level doc search")
        val result = library.query(processed, typesRequested.map(convertType(_))).get.asInstanceOf[SearchResponse]//search.runQuery(processed, p, provideSnippets)
        info(Timer.stop)
        return result.getResultsList.toList
    }

    // Need to split up multi-word queries, but ignore components that may be useful
    val ignored = """has_(obj|sub)|(obj|sub)_of|entity_|page_""".r
    val stripRE = """\.""".r

    def processQuery(query: String) : String = {
        if (query.startsWith("#")) 
            return query
    
       
        val result = stripRE.replaceAllIn(query, " ").toLowerCase
        return result
    }
  
}

abstract class Query extends Logger  {
  
//    var search : Box[Search]
    val prefix = System.getProperty("label", "default") + "."
    var indexes = "default"
    val selectedLanguage = "english"


    def selectParts = {
        val part = whichAction
        if (part == 1) {
            ".parts" #> <input type="radio" name="part" onclick="document.searchform.action='/dquery';">Pages</input>
            <input type="radio" name="part" checked="checked" onclick="document.searchform.action='/equery';">Entities</input>
            <input type="radio" name="part" onclick="document.searchform.action='/pquery';">Pictures</input>
        }
        else if (part == 2) {
            ".parts" #> <input type="radio" name="part" onclick="document.searchform.action='/dquery';">Pages</input>
            <input type="radio" name="part" onclick="document.searchform.action='/equery';">Entities</input>
            <input type="radio" name="part" checked="checked" onclick="document.searchform.action='/pquery';">Pictures</input>
        }
        else if (part == 3) {
            ".parts" #> <input type="radio" name="part" onclick="document.searchform.action='/dquery';">Pages</input>
            <input type="radio" name="part" checked="checked" onclick="document.searchform.action='/bquery';">Books</input>
            <input type="radio" name="part" onclick="document.searchform.action='/equery';">Entities</input>
            <input type="radio" name="part" onclick="document.searchform.action='/pquery';">Pictures</input>
        }
        else {
            ".parts" #> <input type="radio" name="part" checked="checked" onclick="document.searchform.action='/dquery';">Pages</input>
            <input type="radio" name="part" onclick="document.searchform.action='/equery';">Entities</input>
            <input type="radio" name="part" onclick="document.searchform.action='/pquery';">Pictures</input>
        }
    }

    def whichAction : Int = {
        if (S.uri.length > 1 && S.uri.charAt(1) == 'e')
            return 1
        else if(S.uri.length > 1 && S.uri.charAt(1) == 'p')
            return 2
        else if(S.uri.length > 1 && S.uri.charAt(1) == 'b')
            return 3
        else
            return 0
    }
  
    def getIndexForm = {
        val names = List("/dquery", "/equery", "/pquery", "/bquery")
        ".indexform" #> <form name="searchform" action={names.get(whichAction)}>
            <input size="45" name="term" />
            <input type="hidden" name="index" value={indexes} /> <input type="hidden" name="language" value={selectedLanguage} />
                        </form>

    }

    def displayAvailableIndexes = {
        val rlist = List("Default")
        ".indexAvail" #> rlist.map(r =>
            if(indexes.contains(r.toLowerCase))
                ".indexTitle" #> <option selected='selected' value={r.toLowerCase}>{r}</option>
            else
                ".indexTitle" #> <option value={r.toLowerCase}>{r}</option>
        )
    }

    def displayAvailableLanguages = {
        val rlist = List("English")
        
        ".langAvail" #> rlist.map(r =>
            if(selectedLanguage.equals(r.toLowerCase))
                ".lang" #> <option selected='selected' value={r.toLowerCase}>{r}</option>
            else
                ".lang" #> <option value={r.toLowerCase}>{r}</option>
        )
    }

    val archDownload = "http://www.archive.org/download/"
    val archStream = "http://www.archive.org/stream/"

    def getEntityLink(identifier: String, numID: String) = {
        //info("SESSION ID: " + S.session.openOr("NONE") + ", GET ENTITY LINK")
        "/ent?e=" + identifier + "&id="+numID+"&index="+S.param("index").openOr("default").toString+"&language="+S.param("language").openOr("english").toString
    }
    
    protected def getBooksWithEntityLink(numID: String) = {
        //info("SESSION ID: " + S.session.openOr("NONE") + ", FIND BOOKS WITH ENTITY")
        "/dquery?term=" + ("entity_"+numID)+"&index="+S.param("index").openOr("default").toString+"&language="+S.param("language").openOr("english").toString
    }

    def getEntitySearchLink(numID: String) = {
        //info("SESSION ID: " + S.session.openOr("NONE") + ", SEARCH ENTITIES")
        // For normal entity lists on pages
        "equery?term="+("page_"+numID)+"&index="+S.param("index").openOr("default").toString+"&language="+S.param("language").openOr("english").toString
        // For doing entity annotation ground truths from users
        //"elabel?term="+("page_"+numID)+"&index="+S.param("index").openOr("default").toString+"&language="+S.param("language").openOr("english").toString
    }

    def getDocLink(identifier: String) = {
        //info("SESSION ID: " + S.session.openOr("NONE") + ", VIEW OCR TEXT")
        "/doc?d=" + identifier+"&index="+S.param("index").openOr("default").toString+"&language="+S.param("language").openOr("english").toString
    }


}
