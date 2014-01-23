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

class PageHandler(p: Parameters) extends Handler(p) 
with Searchable {
  val archiveReaderUrl = "http://archive.org/stream"
  val retrieval = RetrievalFactory.instance(parameters)
  val retrievalType = ProteusType.Page

  override def search(srequest: SearchRequest): List[SearchResult] = {
    val (root, scored) = runQueryAgainstIndex(srequest)
    if (scored == null) return List[SearchResult]()        
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
				      `type` = ProteusType.Page, 
				      resourceId = siteId)
      val summary = ResultSummary(getSummary(document, queryTerms), List())
      val Array(bookId, pageNo) = identifier.split("_")
      val iaPageNumber = (pageNo.toInt - 1)
      val externalUrl = String.format("%s/%s#page/n%s/mode/2up",
				      archiveReaderUrl,
				      bookId,
				      iaPageNumber.toString)
      var result = SearchResult(id = accessId,
				score = scoredDocument.score,
				title = Some(getDisplayTitle(document, queryTerms) + ", Page: " + pageNo),
				summary = Some(summary),
				externalUrl = Some(externalUrl),
				thumbUrl = getThumbUrl(accessId, iaPageNumber),
				imgUrl = getImgUrl(accessId, iaPageNumber))
      if (document.metadata.containsKey("url")) {
	result = result.copy(externalUrl = Some(document.metadata.get("url")));
      }
      results += result
    }
    return results.toList
  }

  override def lookup(id: AccessIdentifier): ProteusObject =
    getPageObject(id)
  
  override def lookup(ids: Set[AccessIdentifier]): List[ProteusObject] =
    ids.map { id => getPageObject(id) }.filter { (A) => A != null }.toList

  val c = new Parameters;
  c.set("terms", true);
  c.set("tags", true);    
  private def getPageObject(id: AccessIdentifier): ProteusObject = {
    val document = retrieval.getDocument(id.identifier, c)
    if (document == null) return null
    
    val bookAccessId = new AccessIdentifier(identifier = id.identifier.split("_").head, 
                      `type` = ProteusType.Collection, 
                      resourceId = siteId)
    
    val parentBookDocument = retrieval.getDocument(bookAccessId.identifier, c)
    val creatorList = new ListBuffer[String]
    val creator = document.metadata.getOrElse("creator", "")
    creatorList += creator
    
    val subject = Some(document.metadata.getOrElse("subject",""))
    val pages = Some(document.metadata.getOrElse("numPages","-1").toInt)
    val publicationYear = Some(document.metadata.getOrElse("date", "-1").toLong)   
    var bookDocument = Collection(fullText = Some(document.text), 
				  creators = creatorList.toList, 
				  publicationDate = publicationYear)        
    var page = Page(fullText = Some(document.text),
		    creators = List[String](),
		    pageNumber = Some(id.identifier.split("_").last.toInt),
		    bookId = Some(id.identifier.split("_").head),
            book = Some(bookDocument))
    
	val iaPageNumber = (page.pageNumber.get - 1)

    var pObject = ProteusObject(id = id,
				title = Some(getTitle(document) + ", Page " + page.pageNumber.get ),
				description = Some("Page Number:" + page.pageNumber.get),
				thumbUrl = getThumbUrl(id, iaPageNumber),
				imgUrl = getImgUrl(id, iaPageNumber),
				page = Some(page))
    return pObject
  }

  private def getImgUrl(id: AccessIdentifier, pageNumber: Int): Some[String] = {
    val Array(archiveId, pageNo) = id.identifier.split("_")
    return Some(String.format("http://www.archive.org/download/%s/page/n%s.jpg",
		     archiveId,
		     (pageNumber).toString()))
  }

  private def getThumbUrl(id: AccessIdentifier, pageNumber: Int): Some[String] = {
    val Array(archiveId, pageNo) = id.identifier.split("_")
    return Some(
      String.format("http://www.archive.org/download/%s/page/n%s_thumb.jpg",
		    archiveId,
		     (pageNumber).toString()))
  }
}
