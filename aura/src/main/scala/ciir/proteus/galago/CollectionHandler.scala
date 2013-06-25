package ciir.proteus.galago

import scala.xml.Node
import scala.xml.NodeSeq
import scala.xml.XML

import java.io.File

import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions._

import ciir.proteus.thrift._
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

object CollectionHandler {
  def apply(p : Parameters) = new CollectionHandler(p)
}

class CollectionHandler(p : Parameters) extends Handler(p) 
with Searchable {
  val archiveReaderUrl = "http://archive.org/stream"
  val retrieval = RetrievalFactory.instance(parameters)
  val retrievalType = ProteusType.Collection

  override def search(srequest: SearchRequest): List[SearchResult] = {
    val (root, scored) = runQueryAgainstIndex(srequest)
    if (scored == null) return List()
    val queryTerms = StructuredQuery.findQueryTerms(root).toSet;
    generator.setStemming(root.toString().contains("part=stemmedPostings"));
    val c = new Parameters;
    c.set("terms", false);
    c.set("tags", false);
    var results = ListBuffer[SearchResult]()
    for (scoredDocument <- scored) {
      val identifier = scoredDocument.documentName;
       try {
      val document = retrieval.getDocument(identifier, c);
      val accessId = AccessIdentifier(identifier = identifier, 
				      `type` = ProteusType.Collection, 
				      resourceId = siteId)
      val summary = ResultSummary(getSummary(document, queryTerms), List())
      val externalUrl = String.format("%s/%s#page/cover/mode/2up",
				      archiveReaderUrl,
				      accessId.identifier)
      var result = SearchResult(id = accessId,
				score = scoredDocument.score,
				title = Some(getDisplayTitle(document, queryTerms)),
				summary = Some(summary),
				externalUrl = Some(externalUrl),
				thumbUrl = getThumbUrl(accessId),
				imgUrl = getImgUrl(accessId))
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

  override def lookup(id: AccessIdentifier) : ProteusObject =
    getCollectionObject(id)

  override def lookup(ids: Set[AccessIdentifier]): List[ProteusObject] = 
    ids.map { id => getCollectionObject(id) }.filter { A => A != null }.toList

  val c = new Parameters;
  c.set("terms", true);
  c.set("tags", true);    
  private def getCollectionObject(id: AccessIdentifier) : ProteusObject = {
    val document = retrieval.getDocument(id.identifier, c)
    if (document == null) return null
    val creatorList = new ListBuffer[String]
    var publicationD = None
    var publisher = None
    var numPages = None
      
//    try {
//      val bookXml = XML.loadString(document.text)
//      val metadata = bookXml \\ "metadata"
//      val creators = (metadata \\ "creator").mkString(" ")
//      val subject = metadata \\ "subject"
//      val description = metadata \\ "description"
//      val publisher = metadata \\ "publisher"
//      val date = metadata \\ "date"
//      val language = metadata \\ "language"
//      
//    } catch {
//      case e => println("Error parsing text from book " + id + "\n" + e.printStackTrace())
//    }
        
    // TODO!!!  These metadata fields are important.  We need to fix these up!
    // We should to parse earlier, or produce valid XML to extract them from.
    val creator = document.metadata.getOrElse("creator", "")
    creatorList += creator
    
    val subject = Some(document.metadata.getOrElse("subject",""))
    val pages = Some(document.metadata.getOrElse("numPages","-1").toInt)
    val publicationYear = Some(document.metadata.getOrElse("date", "-1").toLong)   
    var collection = Collection(fullText = Some(document.text), 
				creators = creatorList.toList, 
				publicationDate = publicationYear)
    var pObject = ProteusObject(id = id,
				title = Some(getTitle(document)),
				description = Some("A book"),
				thumbUrl = getImgUrl(id),
				collection = Some(collection))
    return pObject
  }

  private def getImgUrl(id: AccessIdentifier) : Some[String] = {
    return Some(String.format("http://www.archive.org/download/%s/page/cover.jpg",
		     id.identifier))
  }

  private def getThumbUrl(id: AccessIdentifier) : Some[String] = {
    return Some(
      String.format("http://www.archive.org/download/%s/page/cover_thumb.jpg",
		    id.identifier))
  }
}
