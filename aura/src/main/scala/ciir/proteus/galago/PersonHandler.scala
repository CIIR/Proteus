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

object PersonHandler {
  def apply(p: Parameters) = new PersonHandler(p)
}

class PersonHandler(p: Parameters) extends Handler(p) {

  val retrieval = RetrievalFactory.instance(parameters)

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
				      `type` = ProteusType.Person, 
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

  override def lookup(ids: Set[AccessIdentifier]): List[ProteusObject] =
    ids.map { id => getPersonObject(id) }.toList

  val c = new Parameters;
  c.set("terms", true);
  c.set("tags", true);    
  private def getPersonObject(id: AccessIdentifier): ProteusObject = {
    val document = retrieval.getDocument(id.identifier, c)
    var person = Person(fullName = Some(document.name),
		      alternateNames = List[String]())    
    var pObject = ProteusObject(id = id,
				title = Some(getTitle(document)),
				description = Some("A page in a book"),
				thumbUrl = Some(dummyThumbUrl),
				person = Some(person))
    return pObject
  }
}
