package code
package lib

import code.model._
import code.snippet.Entity
import code.snippet.Picture
import code.snippet.Book
import code.snippet.Document
import java.io.File
import java.io.FileOutputStream
import net.liftweb._
import net.liftweb.common.Empty
import net.liftweb.http.S
import net.liftweb.http.js.JsCmds
import net.liftweb.common._
import util._
import Helpers._

//import proteus.web._
//import proteus.base._

import edu.umass.ciir.proteus.protocol.ProteusProtocol._



/**
 * The shopping cart
 */
object Timer {
  var start: Long = 0L
  var end: Long = 0L
  var activity: String = ""

  def go(what: String) = {
    start = System.currentTimeMillis
    activity = what
  }
  def stop = {
    end = System.currentTimeMillis
    "QUERYTIME: " + activity + " took " + (end - start) / 1000.0 + "s"
  }
}

class Cart extends Logger {
  var queryText = ""
  var language = "english"

  val bookSearch: Book = new Book

  /**
   *   The book-level results
   */

  /**
   * Lets switch all these over from CartItems to typed QueryResults.
   */
  var wiredQuery = ValueCell("")
  val contents = ValueCell[Vector[BookItem]](Vector()) // Books?
  val book_map = contents.lift(w => w.map(v => v.hashCode -> v).toMap)

  val pictures = ValueCell[Vector[PictureItem]](Vector())
  val filtered_pictures = pictures.lift(w => w.filter(x => x.selected).map(v => v))
  val picture_map = pictures.lift(w => w.map(v => v.hashCode -> v).toMap)

  val pages = ValueCell[Vector[PageItem]](Vector())
  val filtered_pages = pages.lift(w => w.filter(x => x.selected).map(v => v))
  val page_map = pages.lift(w => w.map(v => v.hashCode -> v).toMap)

  val entities = ValueCell[Vector[EntityItem]](Vector())
  val filtered_entities = entities.lift(w => w.filter(x => x.selected).map(v => v))
  val entity_map = entities.lift(w => w.map(v => v.hashCode -> v).toMap)

  val numPages = filtered_pages.lift(_.length)
  val numEntities = filtered_entities.lift(_.length)
  val numPictures = filtered_pictures.lift(_.length)
  val subtotal = contents.lift(_.length)
  val total = contents.lift(_.filter(_.selected).length)

  // Trying out pagination here   
  val itemsPerPage = 4
  var first = ValueCell[Int](0)
  def pageLeft = {
    if (first.get <= 0) {
      JsCmds.Noop
    } else {
      first = ValueCell[Int](first.get - itemsPerPage)
      S.session.foreach(
        _.sendCometActorMessage("BookBag", Empty,
          "pageUpdate"))
      info("SESSION ID: " + S.session.openOr("NONE") + " BOOK LEFT")
      JsCmds.Noop
    }
  }

  def pageRight = {

    if ((first.get + itemsPerPage) >= contents.get.size)
      JsCmds.Noop
    else {
      first = ValueCell[Int](first.get + itemsPerPage)
      S.session.foreach(
        _.sendCometActorMessage("BookBag", Empty,
          "pageUpdate"))
      info("SESSION ID: " + S.session.openOr("NONE") + " BOOK RIGHT")
      JsCmds.Noop
    }
  }

  def currentPage = {
    contents.lift(w => w.slice(first.get, first.get + itemsPerPage))
  }

  // Page-Level pagination, hacky but works
  var firstDoc = 0
  var docsPerPage = 6
  def currentDocPage = {
    filtered_pages.lift(w => w.slice(firstDoc, firstDoc + docsPerPage))
  }
  def docPageLeft = {
    if (firstDoc <= 0) {
      JsCmds.Noop
    } else {
      firstDoc = (firstDoc - docsPerPage)
      info("SESSION ID: " + S.session.openOr("NONE") + ", DOC LEFT")
      S.redirectTo("/item")
    }

  }
  def docPageRight = {
    if ((firstDoc + docsPerPage) >= filtered_pages.get.size)
      JsCmds.Noop
    else {
      firstDoc = firstDoc + docsPerPage
      info("SESSION ID: " + S.session.openOr("NONE") + ", DOC RIGHT")
      S.redirectTo("/item")
    }
  }

  // Picture pagination, again hacky...
  var firstPic = 0
  var picsPerPage = 15
  def currentPicPage = {
    filtered_pictures.lift(w => w.slice(firstPic, firstPic + picsPerPage))
  }
  def picPageLeft = {
    if (firstPic <= 0) {
      JsCmds.Noop
    } else {
      firstPic = (firstPic - picsPerPage)
      info("SESSION ID: " + S.session.openOr("NONE") + ", PIC LEFT")
      S.redirectTo("/picture")
    }

  }
  def picPageRight = {
    if ((firstPic + picsPerPage) >= filtered_pictures.get.size)
      JsCmds.Noop
    else {
      firstPic = firstPic + picsPerPage
      info("SESSION ID: " + S.session.openOr("NONE") + ", PIC RIGHT")
      S.redirectTo("/picture")
    }
  }

  // Helper methods 
  def findEntityItem(numID: Int): EntityItem = {
    if (entity_map.get.contains(numID))
      return entity_map.get.apply(numID) //entities.get.find(p => p.item.description == numID).get 
    else
      return null
  }
  
  def findBookItem(numID: Int): BookItem = {
    if (book_map.get.contains(numID))
      return book_map.get.apply(numID) //entities.get.find(p => p.item.description == numID).get 
    else
      return null
  }
  
  def findPageItem(numID: Int): PageItem = {
    if (page_map.get.contains(numID))
      return page_map.get.apply(numID) //entities.get.find(p => p.item.description == numID).get 
    else
      return null
  }
  
  def findPictureItem(numID: Int): PictureItem = {
    if (picture_map.get.contains(numID))
      return picture_map.get.apply(numID) //entities.get.find(p => p.item.description == numID).get 
    else
      return null
  }
  
  /**
   * A nice constant zero
   */
  def zero = BigDecimal(0)

  def searchBooks(query: String, lang: String) = {
    clearCart
    queryText = query
    info("SESSION ID: " + S.session + ", QUERY: " + query)
    wiredQuery.set(query)
    language = lang

    Timer.go("book search")
    //bookSearch.searchBook(query, lang)
    //info(Timer.stop)

    for (resultItem <- bookSearch.searchBook(query, lang)) {
      addItem(resultItem)
    }

    info(Timer.stop)
  }

  /**
   * Add an item to the cart. If it's already in the cart,
   * then increment the quantity
   */
  def addItem(item: SearchResult) {
    item.getProteusType.getValueDescriptor.getName.toLowerCase match {
      case "collection" =>
        contents.atomicUpdate(v => v.find(_.item == item) match {
          case Some(ci) => v.map(ci => ci.copy(selected = true))
          case _ =>
            v :+ BookItem(item, true)
        })
      case "page" =>
        pages.atomicUpdate(v =>
          {
            v :+ PageItem(item, true)
          })
      case "person" | "location" =>
        entities.atomicUpdate(v => {
          v :+ EntityItem(item, true)
        })
      case "picture" =>
        pictures.atomicUpdate(v => {
          v :+ PictureItem(item, true)
        })
    }
  }

  def clearCart() {

    firstPic = 0
    firstDoc = 0
    first = ValueCell(0)
    contents.set(Vector())
    pictures.set(Vector())
    pages.set(Vector())
    entities.set(Vector())
    queryText = ""
  }

}

/**
 * An item in the cart
 */
trait CombinedHashable  {
  def item: SearchResult
  override def hashCode : Int = (item.getId.getIdentifier + item.getId.getResourceId).hashCode
}

case class BookItem(item: SearchResult, selected: Boolean, full_obj: edu.umass.ciir.proteus.protocol.ProteusProtocol.Collection = null, id: String = Helpers.nextFuncName) extends CombinedHashable
case class PageItem(item: SearchResult, selected: Boolean, full_obj: edu.umass.ciir.proteus.protocol.ProteusProtocol.Page = null, id: String = Helpers.nextFuncName) extends CombinedHashable
case class PictureItem(item: SearchResult, selected: Boolean, full_obj: edu.umass.ciir.proteus.protocol.ProteusProtocol.Picture = null, id: String = Helpers.nextFuncName) extends CombinedHashable
case class EntityItem(item: SearchResult, selected: Boolean, full_obj: Object = null, id: String = Helpers.nextFuncName) extends CombinedHashable {
  def isPerson: Boolean = item.getProteusType == ProteusType.PERSON
  def isLocation: Boolean = item.getProteusType == ProteusType.LOCATION
  def asPerson : Person = if (full_obj == null) null else full_obj.asInstanceOf[Person]
  def asLocation : Location = if (full_obj == null) null else full_obj.asInstanceOf[Location]
}

                    
                                
