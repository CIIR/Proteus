package code.lib

import scala.collection.JavaConversions._
import org.lemurproject.galago.core.tools.Search
import org.lemurproject.galago.core.tools.Search.SearchResultItem
import org.lemurproject.galago.tupleflow.Parameters
import org.lemurproject.galago.core.parse.Document

import ciir.models._

object Galago {

	// hard coding this is not cool man...not cool
	var indexPath = "http://laguna.cs.umass.edu:9900"
	var requested = 10L	
	  
	var params = new Parameters
	params.set("index", indexPath)
	params.set("requested", requested)
	params.set("startAt", 0L)
    params.set("resultCount", requested)
        
	val searchObject = new Search(params)
	val provideSnippets = false
	
	def search(query: String): List[SearchResultItem] = {		
		var searchResult = searchObject.runQuery(query, params, provideSnippets)
		var items = searchResult.items
		return items.toList
	}
	
	def search(query: String, num: Long): List[SearchResultItem] = {		
	  var params = new Parameters
	  params.set("index", indexPath)
	  params.set("requested", num)
	  params.set("startAt", 0L)
	  params.set("resultCount", num)
	  var searchResult = searchObject.runQuery(query, params, provideSnippets)
	  var items = searchResult.items
      return items.toList
	}
	
	def getDocument(identifier: String) : Document = searchObject.getDocument(identifier)
	def generateLanguageModels = { }
		
}