package ciir.proteus.galago

import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions._

import ciir.proteus._

import org.lemurproject.galago.core.retrieval._
import org.lemurproject.galago.core.parse.Document
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.core.retrieval.query.StructuredQuery;
import org.lemurproject.galago.core.retrieval.RetrievalFactory
import org.lemurproject.galago.tupleflow.Parameters;

object MiscHandler {
  def apply(p: Parameters) = new MiscHandler(p)
}

class MiscHandler(p: Parameters) extends Handler(p) 
with Searchable {
  val wikiSearchUrl = "http://en.wikipedia.org/w/index.php?search="
  val retrieval = RetrievalFactory.instance(parameters)
  val retrievalType = ProteusType.Miscellaneous

  override def search(srequest: SearchRequest): List[SearchResult] = {
    val (root, scored) = runQueryAgainstIndex(srequest)
    val queryTerms = StructuredQuery.findQueryTerms(root).toSet;
    generator.setStemming(root.toString().contains("part=stemmedPostings"));
    val c = new Parameters;
    c.set("terms", false);
    c.set("tags", false);
    var results = ListBuffer[SearchResult]()
    for (scoredDocument <- scored) {
      val identifier = scoredDocument.documentName;
      val document = retrieval.getDocument(identifier, c);
      val accessId = AccessIdentifier(identifier = identifier, 
				      `type` = ProteusType.Miscellaneous, 
				      resourceId = siteId)
      val summary = ResultSummary(getSummary(document, queryTerms), List())
      val externalUrl = wikiSearchUrl + accessId.identifier
      var result = SearchResult(id = accessId,
				score = scoredDocument.score,
				title = Some(getDisplayTitle(document, queryTerms)),
				summary = Some(summary),
				externalUrl = Some(externalUrl))
      if (document.metadata.containsKey("url")) {
	result = result.copy(externalUrl = Some(document.metadata.get("url")));
      }
      results += result
    }
    return results.toList
  }

  override def lookup(id: AccessIdentifier): ProteusObject =
    getMiscObject(id)

  override def lookup(ids: Set[AccessIdentifier]): List[ProteusObject] =
    ids.map { id => getMiscObject(id) }.filter { (A) => A != null }.toList

  val c = new Parameters;
  c.set("terms", true);
  c.set("tags", true);    
  private def getMiscObject(id: AccessIdentifier): ProteusObject = {
    val document = retrieval.getDocument(id.identifier, c)
    if (document == null) return null
    var pObject = ProteusObject(id = id,
				title = Some(getTitle(document)),
				description = Some("A description of misc"),
				thumbUrl = Some(dummyThumbUrl))
    return pObject
  }
}
