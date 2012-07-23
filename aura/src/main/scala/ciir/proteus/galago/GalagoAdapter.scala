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

object GalagoAdapter

class GalagoAdapter(parameters: Parameters) extends ProteusProvider.FutureIface {

  val generator = new SnippetGenerator
  val retrieval = RetrievalFactory.instance(parameters)

  // Need to load the index and then hook it up for the operations

  override def search(srequest: SearchRequest): Future[SearchResponse] = {
    val optionalParams = srequest.parameters
    val (count, offset, lang) = srequest.parameters match {
      case Some(p) => (p.numResultsRequested, p.startAt, p.language)
      case None => (10, 0, "en")
    }
    val searchParams = new Parameters
    searchParams.set("count", count+offset)
    val root : Node = StructuredQuery.parse(srequest.rawQuery);
    val transformed : Node = retrieval.transformQuery(root, searchParams);
    val scored : Array[ScoredDocument] = retrieval.runQuery(transformed, 
							    searchParams);
    val queryTerms = StructuredQuery.findQueryTerms(root);
    generator.setStemming(root.toString().contains("part=stemmedPostings"));
    val c = new Parameters;
    c.set("terms", false);
    c.set("tags", false);
    var results = ListBuffer[SearchResult]()
    for (i <- offset to Math.min(offset + count, results.length)) {
      val identifier = scored(i).documentName;
      val document = retrieval.getDocument(identifier, c);
      val accessId = AccessIdentifier(identifier = identifier, 
				      `type` = ProteusType.Page, 
				      resourceId = "galago1")
      val summary = ResultSummary(getSummary(document, queryTerms), List())
      val title = if (document.metadata.containsKey("title")) {
        generator.highlight(document.metadata.get("title"), queryTerms);
      } else {
	String.format("No Title (%s)", scored(i).documentName)
      }
      var result : SearchResult  = SearchResult(id = accessId,
						score = results(i).score,
						title = Some(title),
						summary = Some(summary),
						thumbUrl = Some("http://ciir.cs.umass.edu/~irmarc/imgs/opera-house-thumb.JPG"))
      if (document.metadata.containsKey("url")) {
	result = result.copy(externalUrl = Some(document.metadata.get("url")));
      }
      results += result
    }
    return Future(SearchResponse(results = results.toList, error = None))
  }

  override def lookup(lrequest: LookupRequest): Future[LookupResponse] = {
    val objectBuffer = ListBuffer[ProteusObject]()
    for (id <- lrequest.ids) {
      id.`type` match {
	case ProteusType.Page => objectBuffer += getPageObject(id)
      }
    }
    return Future(LookupResponse(objectBuffer.toList))
  }

  override def transform(trequest: TransformRequest): Future[TransformResponse] = {
    return null
  }

  val c = new Parameters;
  c.set("terms", true);
  c.set("tags", true);    
  private def getPageObject(id: AccessIdentifier) : ProteusObject = {
    printf("Looking up page %s\n", id.identifier)
    val document = retrieval.getDocument(id.identifier, c)
    val title = if (document.metadata.containsKey("title")) {
      document.metadata.get("title");
    } else {
      String.format("No title (%s)", id.identifier)
    }

    var page = Page(fullText = Some(document.text),
		    creators = List[String](),
		    pageNumber = Some(id.identifier.split("_").last.toInt))
    
    var pObject = ProteusObject(id = id,
				title = Some(title),
				description = Some("A page in a book."),
				thumbUrl = Some("http://ciir.cs.umass.edu/~irmarc/imgs/opera-house-thumb.JPG"),
				page = Some(page))
    return pObject
  }

  private def getSummary(document : Document, query: java.util.Set[String]) : String = {
    if (document.metadata.containsKey("description")) {
      val description = document.metadata.get("description");

      if (description.length() > 10) {
        return generator.highlight(description, query);
      }
    }

    return generator.getSnippet(document.text, query);
  }
}
