/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package code
package snippet

// TODO: This file has conversions to be done

import collection.mutable.ListMap
//import org.galagosearch.core.tools.Search
//import org.galagosearch.core.tools.Search._
import net.liftweb._
import http._
//import org.galagosearch.tupleflow.Parameters
import scala.collection.mutable.HashMap
import util._
import Helpers._
/*import org.apache.commons.lang*/
import scala.collection.JavaConversions._
//import org.galagosearch.core.retrieval.BadOperatorException
import code.comet.TheCart
import code.snippet._
import edu.umass.ciir.proteus.protocol.ProteusProtocol._
import edu.umass.ciir.proteus._
/*
 * This class will take a document query and return a list of books ranked the sum of their document scores
 * The top 200 results will be returned for now
 */
class Book extends Query { //with PaginatorSnippet[String] {
//
//  var search = createSearch("documents")
  var booksList:List[String] = Nil

  /*
   * Pagination put on hold for now...
   *
  override def itemsPerPage = 10
  override def count = booksList.size //books.open_!.size
  override def page = booksList.slice(first.asInstanceOf[Int],(first+itemsPerPage).asInstanceOf[Int])
  ////books.open_!.slice(first.asInstanceOf[Int],(first+itemsPerPage).asInstanceOf[Int])
  // carries the query term state in between page requests
  override def pageUrl(offset: Long) = {
    val currentState = queryTerm.is
    S.fmapFunc(S.NFuncHolder(() => queryTerm(currentState))){ name =>
      Helpers.appendParams(super.pageUrl(offset), List(name -> "_"))
    }
  }
  */
 

  def searchBook(query :String, lang: String) : List[SearchResult] = {
    
    val all_results = Librarian.performSearch(query, List("collection", "page", "picture", "person", "location"))
    return all_results
    
  }

}
