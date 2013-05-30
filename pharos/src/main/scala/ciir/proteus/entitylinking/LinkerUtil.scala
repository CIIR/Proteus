package ciir.proteus.entitylinking

import collection.mutable.ListBuffer

import scala.io.Source._
import java.net._
import cc.refectorie.user.dietz.tacco.data._

object LinkerUtil {

  
  def filterByDate(entities : Seq[WikipediaEntity]) : Seq[WikipediaEntity] = {
    
    val filtered = entities.filter( e => 
      {
      val triples = fetchDbpediaData(e.docId)
      val yearTriples = triples.filter( tuple => (tuple._2.equals("<http://dbpedia.org/ontology/birthYear>") 
          || tuple._2.equals("<http://dbpedia.org/ontology/foundingYear>") || 
              tuple._2.equals("<http://dbpedia.org/property/establishedDate>") ||
              tuple._2.equals("<http://dbpedia.org/ontology/formationYear>") ||
              tuple._2.equals("<http://dbpedia.org/ontology/activeYearsStartYear>")||
              tuple._2.equals("<http://dbpedia.org/ontology/releaseDate>") ) )
      if (yearTriples.length > 0) {
        var yearText = yearTriples.head._3
        
        val year = try {
          yearText = yearText.replace("\"","")
          val dashIdx = yearText.indexOf("-")
          if (dashIdx > 0) {
            yearText = yearText.substring(0,dashIdx)
          }
          yearText.toInt
        } catch {
          case e => -1
        }
        
        if (year > 1930) {
        false
      } else {
        true
      }
      } else {
        true
      }
      }
    )
    filtered
  }
  
  def fetchDbpediaData(wikipediaId : String) : Seq[Tuple3[String, String, String]] = {
    val dbpediaUrl = "http://dbpedia.org/data/" + wikipediaId + ".ntriples"
    val dbpediaContent = getURLContent(dbpediaUrl).get
    val validTuples = new ListBuffer[Tuple3[String, String, String]]
    for (line <- dbpediaContent)  {
      val fields = line.split("\\s+") 
      if (fields.length >= 3) {
        validTuples += new Tuple3(fields(0), fields(1), fields(2))
      }
    }
    validTuples
  }
  
  
  def getURLContent(urlString: String) : Option[Iterator[String]] = {
        val url = new URL(urlString);
        val urlConnection = url.openConnection().asInstanceOf[HttpURLConnection]
        urlConnection.setConnectTimeout(1000);
        urlConnection.setReadTimeout(1000);
        val connectionType = urlConnection.getContentType();
        val responseCode = urlConnection.getResponseCode();

        var content : Option[Iterator[String]] = None
        if ((null != connectionType) && (200 == responseCode)) {
            content  = Some(fromInputStream (urlConnection.getInputStream).getLines)
        }
        content
    }
  
}