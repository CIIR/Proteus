package ciir.proteus.galago

import java.io.File;
import ciir.proteus._
import scala.collection.JavaConversions._
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

  // TODO: Get rid of these two tables. Requires that we somehow reconcile
  // things like "PER" to "Person" in some other way.
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
  


  def getTargetIds(src: AccessIdentifier, 
		   targetType: ProteusType) : List[AccessIdentifier] = {
    val srcKey = extToInt(src.`type`)
    val targetKey = extToInt(targetType)
    val index = linkMap(srcKey)(targetKey)
    var list = List[AccessIdentifier]()
    if (index.containsKey(src.identifier)) {
      System.err.printf("Retrieving %s -> %s linkset for %s\n",
			srcKey, targetKey, src.identifier)
      val links = index.getLinks(src.identifier)
      for (t: Target <- links.target) {
	val aid = AccessIdentifier(identifier = t.id,
				   `type` = targetType,
				   resourceId = siteId)
	list = aid +: list
      }
    }
    return list
  }

  def countOccurrences(src: AccessIdentifier, 
		   targetType: ProteusType) : List[Tuple2[AccessIdentifier, Int]] = {
    val srcKey = extToInt(src.`type`)
    val targetKey = extToInt(targetType)
    val index = linkMap(srcKey)(targetKey)
    var list = List[Tuple2[AccessIdentifier, Int]]()
    if (index.containsKey(src.identifier)) {
      System.err.printf("Retrieving %s -> %s counted linkset for %s\n",
			srcKey, targetKey, src.identifier)
      val links = index.getLinks(src.identifier)
      for (t: Target <- links.target) {
	val aid = AccessIdentifier(identifier = t.id,
				   `type` = targetType,
				   resourceId = siteId)
	list = (aid, t.positions.size) +: list
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
