package ciir.proteus.galago

import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions._

import ciir.proteus._

import org.lemurproject.galago.core.retrieval._
import org.lemurproject.galago.core.parse.Document
import org.lemurproject.galago.core.parse.PseudoDocument
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.core.retrieval.query.StructuredQuery;
import org.lemurproject.galago.core.retrieval.RetrievalFactory
import org.lemurproject.galago.tupleflow.Parameters;

object PersonHandler {
  def apply(p: Parameters) = new PersonHandler(p)
}

class PersonHandler(p: Parameters) extends Handler(p) 
with Searchable {
  val retrieval = RetrievalFactory.instance(parameters)
  val retrievalType = ProteusType.Person

  override def search(srequest: SearchRequest): List[SearchResult] = {
    val (root, scored) = runQueryAgainstIndex(srequest)
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
      val document = retrieval.getDocument(identifier, c).asInstanceOf[PseudoDocument];
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
   } catch {
      case e => { e.printStackTrace()
        if (identifier != null) {
          println("Error handling result " + identifier)
        }
      }
    }
    }
       
    return results.toList
  }

  override def lookup(id: AccessIdentifier): ProteusObject =
    getPersonObject(id)

  override def lookup(ids: Set[AccessIdentifier]): List[ProteusObject] =
    ids.map { id => getPersonObject(id) }.filter { A => A != null }.toList

  val c = new Parameters;
  c.set("pseudo", true);
  c.set("terms", true);
  c.set("tags", true);    
  private def getPersonObject(id: AccessIdentifier): ProteusObject = {
    val document = retrieval.getDocument(id.identifier, c)
    if (document == null) return null
    var person = Person(fullName = Some(document.name),
		      alternateNames = List[String]())
    val contexts = extractContexts(document)
    var pObject = ProteusObject(id = id,
				title = Some(getTitle(document)),
				thumbUrl = Some(dummyThumbUrl),
				person = Some(person),
				contexts = Some(contexts))
    return pObject
  }
}
