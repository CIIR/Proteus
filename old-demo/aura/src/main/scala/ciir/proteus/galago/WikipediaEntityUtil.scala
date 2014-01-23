package ciir.proteus.galago

import org.lemurproject.galago.core.parse.PseudoDocument
import scala.collection.JavaConversions._
import scala.io.Source._
import java.net._
import ciir.proteus.thrift.WikipediaEntity
import collection.mutable.ListBuffer

object WikipediaEntityUtil {

  
  def wikipediaIdToUrl(wikipediaId : String) : String = {
    "http://en.wikipedia.org/wiki/" + wikipediaId
  }
  
  def getDisplayTitle(wikipediaId : String) : String = {
    wikipediaId.replace("_", " ")
  }
  
  def determineExternalId(doc: PseudoDocument) : Option[String] = {
    val externalLinks = doc.samples.toList.take(100).map({ sample => sample.externalLink}).filter(link => link != null)
    
    var externalLink : Option[String] = None
    if (externalLinks.size > 0) {
      externalLink = Some(externalLinks.head)
    }
    
    externalLink
  }
  
  def extractThumbUrl (dbpediaData : Seq[Tuple3[String, String, String]]) : List[String] = {

    val imgResult = dbpediaData.filter( tuple => (tuple._2.equals("<http://dbpedia.org/ontology/thumbnail>") /*|| tuple._2.equals("<http://xmlns.com/foaf/0.1/depiction>")*/))
    val imgs = imgResult.map (r1 => r1._3.replace("<","").replace(">","").replace(" .", "")).toList
    imgs
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
