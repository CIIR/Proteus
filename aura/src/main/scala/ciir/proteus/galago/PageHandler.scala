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

object PageHandler {
  def apply(p: Parameters) = new PageHandler(p)
}

class PageHandler(p: Parameters) extends Handler(p) {
  val archiveReaderUrl = "http://archive.org/stream"
  val generator = new SnippetGenerator
  val retrieval = RetrievalFactory.instance(parameters)

  override def search(srequest: SearchRequest): List[SearchResult] = {
    val (root, scored) = runQueryAgainstIndex(srequest)
    if (scored == null) return List[SearchResult]()        
    val queryTerms = StructuredQuery.findQueryTerms(root);
    generator.setStemming(root.toString().contains("part=stemmedPostings"));
    val c = new Parameters;
    c.set("terms", false);
    c.set("tags", false);
    var results = ListBuffer[SearchResult]()

    for (scoredDocument <- scored) {
      val identifier = scoredDocument.documentName;
      val document = retrieval.getDocument(identifier, c);
      val accessId = AccessIdentifier(identifier = identifier, 
				      `type` = ProteusType.Page, 
				      resourceId = siteId)
      val summary = ResultSummary(getSummary(document, queryTerms), List())
      val title = if (document.metadata.containsKey("title")) {
        generator.highlight(document.metadata.get("title"), queryTerms);
      } else {
	String.format("No Title (%s)", scoredDocument.documentName)
      }
      val Array(bookId, pageNo) = identifier.split("_")
      val externalUrl = String.format("%s/%s#page/n%s/mode/2up",
				      archiveReaderUrl,
				      bookId,
				      pageNo)
      var result = SearchResult(id = accessId,
				score = scoredDocument.score,
				title = Some(title),
				summary = Some(summary),
				externalUrl = Some(externalUrl),
				thumbUrl = getThumbUrl(accessId),
				imgUrl = getImgUrl(accessId))
      if (document.metadata.containsKey("url")) {
	result = result.copy(externalUrl = Some(document.metadata.get("url")));
      }
      results += result
    }
    return results.toList
  }

  override def lookup(ids: Set[AccessIdentifier]): List[ProteusObject] =
    ids.map { id => getPageObject(id) }.toList

  val c = new Parameters;
  c.set("terms", true);
  c.set("tags", true);    
  private def getPageObject(id: AccessIdentifier): ProteusObject = {
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
				description = Some("A page in a book"),
				thumbUrl = getThumbUrl(id),
				page = Some(page))
    return pObject
  }

  private def getSummary(document: Document, 
			 query: java.util.Set[String]): String = {
    if (document.metadata.containsKey("description")) {
      val description = document.metadata.get("description");

      if (description.length() > 10) {
        return generator.highlight(description, query);
      }
    }

    return generator.getSnippet(document.text, query);
  }

  private def getImgUrl(id: AccessIdentifier): Some[String] = {
    val Array(archiveId, pageNo) = id.identifier.split("_")
    return Some(String.format("http://www.archive.org/download/%s/page/n%s.jpg",
		     archiveId,
		     pageNo))
  }

  private def getThumbUrl(id: AccessIdentifier): Some[String] = {
    val Array(archiveId, pageNo) = id.identifier.split("_")
    return Some(
      String.format("http://www.archive.org/download/%s/page/n%s_thumb.jpg",
		    archiveId,
		     pageNo))
  }
}
