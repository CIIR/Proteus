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
      val document = retrieval.getDocument(identifier, c);
      val accessId = AccessIdentifier(identifier = identifier, 
				      `type` = ProteusType.Location, 
				      resourceId = siteId)
      val summary = ResultSummary(getSummary(document, queryTerms), List())
      var result = SearchResult(id = accessId,
				score = scoredDocument.score,
				title = Some(getDisplayTitle(document, queryTerms)),
				summary = Some(summary),
				externalUrl = Some(dummyExtUrl),
				thumbUrl = Some(dummyThumbUrl),
				imgUrl = Some(dummyImgUrl))
      if (document.metadata.containsKey("url")) {
	result = result.copy(externalUrl = Some(document.metadata.get("url")));
      }
      results += result
    }
    return results.toList
  }

  override def lookup(id: AccessIdentifier) : ProteusObject =
    getLocationObject(id)

  override def lookup(ids: Set[AccessIdentifier]): List[ProteusObject] =
    ids.map { id => getLocationObject(id) }.filter { A => A != null }.toList

  val c = new Parameters;
  c.set("terms", true);
  c.set("tags", true);    
  private def getLocationObject(id: AccessIdentifier): ProteusObject = {
    val document = retrieval.getDocument(id.identifier, c)
    if (document == null) return null
    var location = Location(fullName = Some(document.name),
			  alternateNames = List[String]())    
    var pObject = ProteusObject(id = id,
				title = Some(getTitle(document)),
				description = Some("A page in a book"),
				thumbUrl = Some(dummyThumbUrl),
				location = Some(location))
    return pObject
  }
}
