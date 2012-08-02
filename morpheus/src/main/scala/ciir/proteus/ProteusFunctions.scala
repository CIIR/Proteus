package ciir.proteus

import ciir.proteus.Constants._
import ciir.proteus.ProteusServlet._

object ProteusFunctions {
  def pluralDisplayName(typeString : String) : String = 
    typeString.toLowerCase match {
      case "collection" => "Books"
      case "location" => "Locations"
      case "miscellaneous" => "Other"
      case "page" => "Pages"
      case "person" => "People"
      case _ => typeString.capitalize
  }

  def singleDisplayName(typeString : String) : String = 
    typeString.toLowerCase match {
      case "collection" => "Book"
      case _ => typeString.capitalize
  }

  def displayId(aid: AccessIdentifier) : String = 
    String.format("%s.%s.%s", aid.identifier, aid.`type`.name, aid.resourceId) 

  def externalId(did: String) : AccessIdentifier = {
    val Array(id, t, r) = did.split("\\.")
    AccessIdentifier(identifier = id, 
		     `type` = ProteusType.valueOf(t).get,
		     resourceId = r)
  }

  def getContainingTypes(typeName: String) : Set[String] = {
    val normalized = typeName.toLowerCase
    if (isContainedBy.contains(normalized)) {
      isContainedBy(normalized).toSet & kReturnableTypes.toSet      
    } else {
      Set[String]()
    }
  }

  def getContainedTypes(typeName: String) : Set[String] = {
    val normalized = typeName.toLowerCase
    if (contains.contains(normalized)) {
      contains(normalized).toSet & kReturnableTypes.toSet      
    } else {
      Set[String]()
    }
  }

  val archivePattern = """(archive\.org)""".r
  val wikipediaPattern = """(wikipedia\.org)""".r
  def getExternalLinkText(url: String) : String = url match {
    case archivePattern(url) => "[View at Internet Archive]"
    case wikipediaPattern(url) => "[Search Wikipedia]"
    case _ => "[View external Source]"
  }
}
