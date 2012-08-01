package ciir.proteus.galago

import java.io.File;
import ciir.proteus._
import scala.collection.JavaConversions._
import org.lemurproject.galago.core.thrift.IndexLink;
import org.lemurproject.galago.core.thrift.Target;
import org.lemurproject.galago.core.index.IndexLinkReader;
import org.lemurproject.galago.tupleflow.Parameters;

object LinkProvider {
  def apply(parameters: Parameters) = new LinkProvider(parameters)
}

class LinkProvider(parameters: Parameters) {
  val siteId = parameters.getString("siteId")
  // Need to load all of the link indexes
  val lBuilder = 
    Map.newBuilder[String, scala.collection.mutable.Map[String, IndexLinkReader]]
  val linkPaths = new File(parameters.getString("linkDirectory")).listFiles
  linkPaths.map { (A) => A.getName.split("\\.")(0) }.foreach {
    (hdr) =>
      lBuilder += (hdr -> 
		   scala.collection.mutable.HashMap[String, IndexLinkReader]())
  }
  val linkMap = lBuilder.result
  for (path <- linkPaths if (!path.getName().endsWith("json"))) {
    val Array(src, target) = path.getName.split("\\.")
    printf("Opening link index: %s -> %s\n", src, target)
    linkMap(src)(target) = new IndexLinkReader(path.getCanonicalPath())
  }

  val extToInt = Map[ProteusType, String](
    ProteusType.Collection -> "collection",
    ProteusType.Page -> "page",
    ProteusType.Person -> "PER",
    ProteusType.Location -> "LOC",
    ProteusType.Organization -> "ORG",
    ProteusType.Miscellaneous -> "MISC")

  val intToExt = Map[String, ProteusType](
    "collection" -> ProteusType.Collection,
    "page" -> ProteusType.Page,
    "PER" -> ProteusType.Person,
    "LOC" -> ProteusType.Location,
    "ORG" -> ProteusType.Organization,
    "MISC" -> ProteusType.Miscellaneous)
  
  def getTargetIds(trequest : TransformRequest) : List[AccessIdentifier] = {
    val srcKey = extToInt(trequest.referenceId.`type`)
    val targetKey = extToInt(trequest.targetType.get)
    val index = linkMap(srcKey)(targetKey)
    var list = List[AccessIdentifier]()
    if (index.containsKey(trequest.referenceId.identifier)) {
      System.err.printf("Retrieving %s -> %s linkset for %s\n",
			srcKey, targetKey, trequest.referenceId.identifier)
      val links = index.getLinks(trequest.referenceId.identifier)
      for (t: Target <- links.target) {
	val aid = AccessIdentifier(identifier = t.id,
				   `type` = trequest.targetType.get,
				   resourceId = siteId)
	list = aid +: list
      }
    }
    return list
  }

  def getInfo : List[LinkInfo] = {
    var list = List[LinkInfo]()
    for ((intSrcType, innerMap) <- linkMap;
	 (intTargetType, index) <- innerMap) {
	   val manifest = index.getManifest;
	   val srcType = intToExt(intSrcType)
	   val targetType = intToExt(intTargetType)
	   val linkInfo = LinkInfo(src = srcType,
				   target = targetType,
				   numLinks = manifest.getLong("keyCount"))
	   list = linkInfo +: list
	 }
    return list
  }
}
