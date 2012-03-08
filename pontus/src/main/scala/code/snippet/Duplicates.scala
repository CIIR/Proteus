package code
package snippet

// TODO: This needs conversion

//import edu.umass.ciir.megabooks.index.EntityNameReader
//import org.galagosearch.core.tools.Search._
import net.liftweb.util.Props
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.util.HashMap
import java.util.Scanner
import net.liftweb._
import http._
import code.snippet._
//import org.galagosearch.tupleflow.Parameters
import scala.collection.mutable.HashSet
import scala.collection.mutable.ArrayBuffer
import scala.xml.NodeSeq
import util._
import Helpers._
import scala.collection.JavaConversions._
//import org.galagosearch.core.retrieval.BadOperatorException
//import org.galagosearch.core.tools.Search.SearchResult

import edu.umass.ciir.proteus.protocol.ProteusProtocol._

object Duplicates extends Query {

  /*
   *  called from Documents class, in listDocumentResults()
   */
  def dupeLink(in: NodeSeq) : scala.xml.NodeSeq = {
//    if (search.isEmpty) {
//      return in
//    }

    val id = S.attr("id").open_!
    return dupeLinked(id)
  }
  
  import code.comet._
  import code.lib._
import scala.collection.JavaConverters._
  
  def dupeLinked(id: String) : scala.xml.NodeSeq = {
//    val rlist = try {performSearch("#neighbors:"+id+":part=postings()", search.open_!).items.toList}
//                catch { case ex: Exception =>  print("Not found in dupe index"); ex.printStackTrace; Nil}
    val thebook = TheCart.get.findBookItem(id.toInt)
    println("Getting dupes for: " + id + " " + thebook.hashCode)
    val rlist = Librarian.library.getOverlaps(thebook.item.getId, ProteusType.COLLECTION).get.getResultsList.asScala.toList
      //thebook.item.getOverlappingCollections.getResults().get.map(i => i.asInstanceOf[CollectionType])

    if (rlist.length > 0) { //dupeHash.contains(id)) {
      val count = if(rlist.length >= 5) "5+" else rlist.length.toString //dupeHash.get(id)
      val term = rlist.length match {
        case 1 => "duplicate"
        case _ => "duplicates"
      }
      return <a href={"/dupes?d=" + id + "&i=" + id}>{count} {term}</a>
    } else {
      return <span></span>
    }
  }

  /*
   * This method interfaces with dupes.html and "replaces" the <scan class=".."> tags
   * with the appropriate class names
   * 
   */
  def listDuplicates = {
//    if (search.isDefined) {
      var id = S.param("i").openOr("error in listDupes")
      var dupeQuery = S.param("d").openOr("error in listDupes")
//      var decoded = urlDecode(dupeQuery.toString)

    // Librarian.library.getOverlaps(thebook.item.getId, ProteusType.COLLECTION).get.getResultsList.asScala
      val foundItem = TheCart.get.findBookItem(id.toInt).item
      val dList = Librarian.library.getOverlaps(foundItem.getId, ProteusType.COLLECTION).get.getResultsList.asScala
      // Add these results back into the cart/book bag
      dList.foreach(d => TheCart.addItem(d))
      val rlist = dList.map(d => TheCart.get.findBookItem((d.getId.getIdentifier + d.getId.getResourceId).hashCode))
      info("SESSION ID: " + S.session.openOr("NONE") + ", LIST DUPLICATES")
      // this is called CSS Selector Syntax, and is a nice feature of Lift
      ".dupeResult" #> rlist.map(d => {
//          val order = getOrder(id, d.identifier)
          //".dupeTitle" #> d.displayTitle &
          ".archLink" #> d.item.getExternalUrl &
          //[book cover] [title] [score] [bar image]
          ".topbar" #> <a class="thumbnail" href="#thumb"> <img src={d.item.getThumbUrl} width={"60"} height={"100"} />
            <span><img src={d.item.getImgUrl} width={"300"} height={"500"}></img></span> </a> &
          ".title1" #> {<span>(reference)</span>} &
          ".title2" #> {<a href={d.item.getExternalUrl}>{d.item.getTitle}</a> } &
          ".graph" #>  <img width="200" height="75" src={"http://laguna.cs.umass.edu:6800/gimages/unknown.png"}></img> })
      //green, red bars (images are named alphabetically)
//    } else {
//      ".dupeResult" #> ""
//    }
  }

  def duplicateTitle = {
    var id = S.param("i").openOr("error in duplicateTitle")
    val baseDupe = TheCart.get.findBookItem(id.toInt)
    ".showTitle" #> <span>Partial duplicates of <I>{baseDupe.item.getTitle}</I></span> &
    ".minh" #> <a class="thumbnail" href="#thumb"> <img src ={baseDupe.item.getThumbUrl} width={"85"} height={"150"}/> </a>
  }

}
