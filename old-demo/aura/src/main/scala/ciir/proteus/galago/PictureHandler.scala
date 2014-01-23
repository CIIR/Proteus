package ciir.proteus.galago

import java.io.File

import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions._

import ciir.proteus.thrift._
import com.twitter.util.Future
import org.apache.thrift.protocol._
import org.lemurproject.galago.tupleflow.Parameters;
import org.lemurproject.galago.tupleflow.Utility;
import org.apache.thrift.protocol._
import org.apache.thrift.transport._
import org.lemurproject.galago.core.index.disk.DiskBTreeReader
import org.lemurproject.galago.core.parse.Document
import ciir.proteus._

object PictureHandler {
  def apply(p: Parameters) = new PictureHandler(p)
}

class PictureHandler(p: Parameters) extends Handler(p) 
with TypedStore {  
  // page_id --> PictureList  (literally a list of Picture objects)
  // The pictures are stored in their ordinal position, which matters.
  // To construct the "id" of a picture, it's built by <key>_<ordinal>.
  // Ordinals start at 0
  class PagesToPictures(index: DiskBTreeReader) {
    def getEntry(key: String) : Option[PictureList] = {
      if (index == null) return None
      val iterator = index.getIterator(Utility.fromString(key))
      if (iterator == null) return None
      
      val transport = new TMemoryInputTransport(iterator.getValueBytes)
      val protocol = factory.getProtocol(transport)
      return Some(PictureList.decoder(protocol))
    }
  }

  // Get's the "document" of the picture based on it's ordinal.
  // This may contain something for interesting someday, but for now
  // it's just the thrift Picture object again.
  class IdsToPictures(index: DiskBTreeReader) {
    def countKeys() : Int = {
      val iterator = index.getIterator()
      var total = 0
      while (!iterator.isDone) {
	total += 1
	iterator.nextKey
      }
      return total
    }

    val emptyParameters = new Parameters
    def getEntry(key: String) : Option[Picture] = {
      if (index == null) return None
      val iterator = index.getIterator(Utility.fromString(key))
      if (iterator == null) return None
      val d = Document.deserialize(iterator.getValueBytes, emptyParameters)
      Some(Picture(creators = List(),
		   coordinates = Some(Coordinates(left = d.metadata.get("left").toInt,
						  right = d.metadata.get("right").toInt,
						  top = d.metadata.get("top").toInt,
						  bottom = d.metadata.get("bottom").toInt))))
    }
  }

  val factory = new TCompactProtocol.Factory
  val retrievalType = ProteusType.Picture
  val indexPath = p.getString("index")
  
  val indexFile = new File(indexPath, "pictures.index")
  val index = new PagesToPictures(new DiskBTreeReader(indexFile))
  
  val documentFile = new File(indexPath, "pictures.corpus")
  val documentReader = new IdsToPictures(new DiskBTreeReader(documentFile))

  def getFullUrl(pageId: String, picture: Picture) : Option[String] = getThumbUrl(pageId, picture)
  def getThumbUrl(pageId: String, picture: Picture) : Option[String] = {
    val coords = picture.coordinates.get
    val height = coords.bottom - coords.top
    val width = coords.right - coords.left
    val Array(archiveId, pageNo) = pageId.split("_")
    Some("http://www.archive.org/download/%s/page/n%s_h%d_w%d_x%d_y%d.jpg".format(archiveId, pageNo.toInt-1, height, width, coords.left, coords.top))
  }
  def getPageUrl(pageId: String) : Option[String] = {
    val Array(archiveId, pageNo) = pageId.split("_")
    Some("http://www.archive.org/download/%s/page/n%s.jpg".format(archiveId, pageNo.toInt-1))
  }

  def scorePictures(pageResults: List[SearchResult]) : List[SearchResult] = {
    pageResults.map {
      pageResult => {
        val pid = pageResult.id.identifier
        index.getEntry(pid) match {
          case None => List()
          case Some(pl: PictureList) => {
            pl.pictures.zipWithIndex.map {
              A => {
                val (pic, index) = A
                val id = "%s_%s" format (pid, index)
                val thumbUrl = getThumbUrl(pid, pic)
                val pictureUrl = getFullUrl(pid, pic)
                SearchResult(id = AccessIdentifier(identifier = id,
                           `type` = ProteusType.Picture,						   
                           resourceId = siteId),
                       score = 0,
                       thumbUrl = thumbUrl,
                       imgUrl = pictureUrl,
                       externalUrl = getPageUrl(pid))
              }
            }
          }
        }	
      }
    }.flatten
  }
  
  override def lookup(id: AccessIdentifier): ProteusObject = {
    val Array(archiveId, pageNo, ordinal) = id.identifier.split("_")
    val pid = "%s_%s" format (archiveId, pageNo)
    val picture = documentReader.getEntry(id.identifier)
    if (picture.isEmpty) return null
    ProteusObject(id = id,
		  thumbUrl = getThumbUrl(pid, picture.get),
		  imgUrl = getFullUrl(pid, picture.get),
		  externalUrl = getPageUrl(pid),
		  picture = picture)
  }

  override def lookup(ids: Set[AccessIdentifier]): List[ProteusObject] =
    ids.map(lookup(_)).toList.filter(_ != null)

  lazy val pictureCount = documentReader.countKeys
  override def getInfo() : Option[CollectionInfo] = {
    Some(CollectionInfo(`type` = ProteusType.Picture,
			numDocs = pictureCount,
			vocabSize = 0,
			numTokens = 0,
			fields = List()))
  }
}
       
