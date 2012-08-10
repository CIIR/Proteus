package ciir.proteus.galago

import java.io.File

import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions._

import ciir.proteus._
import com.twitter.finagle.builder._
import com.twitter.finagle.thrift.ThriftServerFramedCodec
import com.twitter.util.{Duration,Future}
import org.apache.thrift.protocol._

import org.lemurproject.galago.core.retrieval._
import org.lemurproject.galago.core.parse.Document
import org.lemurproject.galago.core.parse.PseudoDocument
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.core.retrieval.query.StructuredQuery;
import org.lemurproject.galago.core.retrieval.RetrievalFactory
import org.lemurproject.galago.core.index.corpus.SnippetGenerator;
import org.lemurproject.galago.tupleflow.Parameters;

object LocationHandler {
  def apply(p: Parameters) = new LocationHandler(p)
}

class LocationHandler(p: Parameters) extends Handler(p) 
with Searchable {
  val retrieval = RetrievalFactory.instance(parameters)
  val retrievalType = ProteusType.Location

  override def search(srequest: SearchRequest): List[SearchResult] = {
    val (root, scored) = runQueryAgainstIndex(srequest)
    if (scored == null) return List[SearchResult]()        
    val queryTerms = StructuredQuery.findQueryTerms(root).toSet;
    generator.setStemming(root.toString().contains("part=stemmedPostings"));
    val c = new Parameters;
    c.set("pseudo", true);
    c.set("terms", false);
    c.set("tags", false);
    var results = ListBuffer[SearchResult]()
    for (scoredDocument <- scored) {
       val identifier = scoredDocument.documentName;
      try {
     
      println("Fetching doc with ID: " + identifier);
      val document = retrieval.getDocument(identifier, c).asInstanceOf[PseudoDocument]
      val accessId = AccessIdentifier(identifier = identifier, 
				      `type` = ProteusType.Location, 
				      resourceId = siteId)
      val summary = ResultSummary(getSummary(document, queryTerms), List())
      
      
      val externalId = WikipediaEntityUtil.determineExternalId(document)
      
      var wikiEntity : Option[WikipediaEntity] = None
      var externalUrl : Option[String] = None
      
      if (externalId != None) {
         externalUrl = Some(WikipediaEntityUtil.wikipediaIdToUrl(externalId.get))
         
         var thumbUrls = List[String]()
         var dbpediaData = Seq[Tuple3[String, String, String]]()
         try {
            dbpediaData = WikipediaEntityUtil.fetchDbpediaData(externalId.get)
            thumbUrls = WikipediaEntityUtil.extractThumbUrl(dbpediaData)
         } catch {
             case e => { e.printStackTrace()  }
         }
         wikiEntity = Some(WikipediaEntity(externalId.get, 
             WikipediaEntityUtil.getDisplayTitle(externalId.get), 
             WikipediaEntityUtil.wikipediaIdToUrl(externalId.get), 
             thumbUrls, 
             Some(dbpediaData.mkString("\n"))))
      }
        
      var result = SearchResult(id = accessId,
				score = scoredDocument.score,
				title = Some(getDisplayTitle(document, queryTerms)),
				summary = Some(summary),
				externalUrl = externalUrl,
				thumbUrl = getThumb(wikiEntity),
				imgUrl = None)
      if (document.metadata.containsKey("url")) {
	result = result.copy(externalUrl = Some(document.metadata.get("url")));
      }
      results += result
    } catch {
      case e => { e.printStackTrace()
        if (identifier != null) {
          println("Error handling result " + identifier)
        }
      }
    }
    }
    println("returning results " + results.size);
    return results.toList
  }
 

  override def lookup(id: AccessIdentifier) : ProteusObject =
    getLocationObject(id)

  override def lookup(ids: Set[AccessIdentifier]): List[ProteusObject] =
    ids.map { id => getLocationObject(id) }.filter { A => A != null }.toList

  val c = new Parameters;
  c.set("pseudo", true);
  c.set("terms", false);
  c.set("tags", true);
  c.set("sampleLimit", 50)
  private def getLocationObject(id: AccessIdentifier): ProteusObject = {
    val document = retrieval.getDocument(id.identifier, c).asInstanceOf[PseudoDocument]
    if (document == null) return null
    var location = Location(fullName = Some(document.name),
			  alternateNames = List[String]())
			  
	val contexts = extractContexts(document)

	 val externalId = WikipediaEntityUtil.determineExternalId(document)
      
      var wikiEntity : Option[WikipediaEntity] = None
      var externalUrl : Option[String] = None
      
       if (externalId != None) {
         externalUrl = Some(WikipediaEntityUtil.wikipediaIdToUrl(externalId.get))
         
         var thumbUrls = List[String]()
         var dbpediaData = Seq[Tuple3[String, String, String]]()
         try {
            dbpediaData = WikipediaEntityUtil.fetchDbpediaData(externalId.get)
            thumbUrls = WikipediaEntityUtil.extractThumbUrl(dbpediaData)
         } catch {
             case e => { e.printStackTrace()  }
         }
         wikiEntity = Some(WikipediaEntity(externalId.get, 
             WikipediaEntityUtil.getDisplayTitle(externalId.get), 
             WikipediaEntityUtil.wikipediaIdToUrl(externalId.get), 
             thumbUrls, 
             Some(dbpediaData.mkString("\n"))))
      }
    
    var pObject = ProteusObject(id = id,
				title = Some(getTitle(document)),
				description = None,
				thumbUrl = getThumb(wikiEntity),
				location = Some(location),
				externalUrl = externalUrl,
				contexts = Some(contexts),
				externalEntity = wikiEntity)
    return pObject
  }
}
