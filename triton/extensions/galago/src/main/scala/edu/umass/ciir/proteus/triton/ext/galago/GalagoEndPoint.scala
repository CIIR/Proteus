package edu.umass.ciir.proteus.triton.ext.galago

import java.util.regex.Pattern

import akka.actor.Actor
import akka.actor.Actor._
import akka.remoteinterface._
import scala.collection.JavaConverters._

//import org.joda._
import org.joda.time.format._
import org.joda.time._

import edu.umass.ciir.proteus.triton.core._
import edu.umass.ciir.proteus.protocol.ProteusProtocol._

import org.lemurproject.galago.core.tools
import org.lemurproject.galago.tupleflow.Parameters
import org.lemurproject.galago.core.parse.Document
import org.lemurproject.galago.core.parse.Tag


/**
 * Galago End Point (Library resource) class
 * Once the data store trait is completed it is an extremely simple class to create.
 * 
 * myServer describes where this library server is running
 * librarian describes where the librarian/manager is running, in order to notify it of our existence
 */
class GalagoEndPoint(myServer: ServerSetting, librarian: ServerSetting) extends 
LibraryServer with 
EndPointConnectionManagement with
EndPointQueryManagement with
EndPointLookupManagement with 
GalagoDataStore {

	val serverHostname : String = myServer.hostname
			val serverPort : Int = myServer.port
			val serverGroupID : String = "galagoProteus"
			var connection : ConnectLibrary = buildConnection

			override def preStart() = {
	super.preStart
	initialize(EndPointConfiguration(ProteusType.PAGE, "/usr/mildura/scratch1/indexes/pages_small"))
	connectToLibrarian(librarian.hostname, librarian.port)
}
}

class IAGalagoEndPoint(myServer: ServerSetting, librarian: ServerSetting) extends GalagoEndPoint(myServer, librarian) {
	override def getURL(metadata: Map[String, String], fallback: String) : String = {
			val prefix = "http://www.archive.org/stream/" + tryMetaData(metadata, "identifier", fallback)
					dataType match {
					case ProteusType.COLLECTION => prefix
					case ProteusType.PAGE => prefix + "#page/" + tryMetaData(metadata, "pagenumber", "1") + "/mode/2up"
					case ProteusType.PICTURE => prefix + "#page/" + tryMetaData(metadata, "pagenumber", "1") + "/mode/2up"
					case _ => fallback
			}
	}

	override def getImgURL(metadata: Map[String, String], fallback: String) : String = {
			val prefix = "http://www.archive.org/download/" + tryMetaData(metadata, "identifier", fallback)
					dataType match {
					case ProteusType.COLLECTION => prefix + "/page/cover_s2.jpg"
					case ProteusType.PAGE => prefix + "/page/leaf" + tryMetaData(metadata, "pagenumber", "1") + "_s4.jpg"
					case ProteusType.PICTURE => prefix + "/page/leaf" + tryMetaData(metadata, "pagenumber", "1") + "_s4.jpg"
					case _ => fallback
			}
	}

	override def getThumbURL(metadata: Map[String, String], fallback: String) : String = {
			val prefix = "http://www.archive.org/download/" + tryMetaData(metadata, "identifier", fallback)
					dataType match {
					case ProteusType.COLLECTION => prefix + "/page/cover_thumb.jpg"
					case ProteusType.PAGE => prefix + "/page/leaf" + tryMetaData(metadata, "pagenumber", "1") + "_thumb.jpg"
					case ProteusType.PICTURE => prefix + "/page/leaf" + tryMetaData(metadata, "pagenumber", "1") + "_thumb.jpg"
					case _ => fallback
			}
	}

}

/**
 * A little app to let us run this end point
 */
object galagoEndPointApp extends App {
	val libraryService = try {
		val mysettings = ServerSetting(args(0), args(1).toInt)
				try {
					val librarySettings = ServerSetting(args(2), args(3).toInt)
							actorOf(new IAGalagoEndPoint(mysettings, librarySettings)).start()
				} catch {
				case _ => println("Library connection settings not given on command line using defaults.")
						actorOf(new IAGalagoEndPoint(mysettings, ServerSetting(mysettings.hostname, 8081))).start()
				}
	} catch {
	case _ => println("No arguments supplied using defaults.")
			actorOf(new IAGalagoEndPoint(ServerSetting("localhost", 8082), ServerSetting("localhost", 8081))).start()
	}	
}

/********* Implementation of the Random End Point / Random Library *************/
/**
 * A data store implementation using Galago. Useful as an example of how to implement an end point easily.
 */
trait GalagoDataStore extends EndPointDataStore with RandomDataGenerator {


	/** Methods Defined Elsewhere **/
	def containerFor(ptype: ProteusType) : List[ProteusType]
			def getResourceKey : String

			var galago: tools.Search = null
			var dataType : ProteusType = ProteusType.COLLECTION

			override def initialize(configuration: EndPointConfiguration) {
		galago = new tools.Search(new Parameters(List(
				"command" -> ("search --index=" + configuration.resourcePath + " --corpus=" + configuration.resourcePath + "/corpus"), 
				"index" -> configuration.resourcePath,
				"corpus" -> (configuration.resourcePath + "/corpus")).toMap.asJava))
		dataType = configuration.dataType
	}

	/** Methods Used Here and Elsewhere (MUST BE PROVIDED) **/
	def getSupportedTypes : List[ProteusType] = List(dataType)
			def supportsType(ptype: ProteusType) : Boolean = getSupportedTypes.contains(ptype)
			def supportsDynTransform(dtID: DynamicTransformID) : Boolean = getDynamicTransforms.contains(dtID)

			/** Internal Utility Methods **/

			val dateRE1 = Pattern.compile("^(\\d{1}|\\d{2})[- / ]?(\\d{1}|\\d{2})[- /]?(\\d{4}|\\d{3}|\\d{2})($|AD$|BC$)")
			val dateRE2 = Pattern.compile("^(jan|feb|mar|apr|may|jun|jul|aug|sept|sep|oct|nov|dec)($|uary|ch|e|ust|tember|ember|ober|ember)")


			def generateSummary(summary: String): ResultSummary = {
			// Analyze summary compared with transformedQuery to get the text regions
			// Summaries come with <strong> tags around key terms already, so we want to detect them, remove them, and 
			// put them into the TextRegion format.
			val start_tag = "<strong>"
					val stop_tag = "</strong>"
					var start_index = summary.indexOf(start_tag)
					var stop_index = summary.indexOf(stop_tag)
					var modified_summary = if(start_index >= 0) summary.slice(0, start_index) else summary
					var regions = Seq[TextRegion]()
					var offset = 0
					val builder = ResultSummary.newBuilder
					while(start_index >= 0) {
						//System.out.println(start_index + ", " + stop_index + " " + summary.slice(start_index+start_tag.length, stop_index))
						//regions = regions ++ Seq(TextRegion.newBuilder.setStart(start_index - offset).setStop(stop_index - start_tag.length - offset).build)
						builder.addHighlights(TextRegion.newBuilder.setStart(start_index - offset).setStop(stop_index - start_tag.length - offset).build)
						modified_summary += summary.slice(start_index+start_tag.length, stop_index)
						start_index = summary.indexOf(start_tag, stop_index)
						if(start_index >= 0) 
							modified_summary += summary.slice(stop_index + stop_tag.length, start_index) 
							else 
								modified_summary += summary.slice(stop_index + stop_tag.length, summary.length)
								stop_index = summary.indexOf(stop_tag, start_index)
								offset += start_tag.length + stop_tag.length
					}

			builder.setText(modified_summary)
			return builder.build
		}


		protected def tryMetaData(metadata: Map[String, String], key: String, default: String) : String = {
				if(metadata.contains(key))
					return metadata(key)
							else
								return default
		}

		protected def convertParameters(params: SearchParameters): Parameters = {
				return new Parameters(List("--startAt=" + params.getStartAt,
						"--resultCount=" + params.getNumRequested).toArray)
		}

		protected def convertResult(galagoResult: tools.Search.SearchResultItem): SearchResult = {
				// Assuming for now that it is one type per index or you will write your own way to know which type to use
				val proteusType = getSupportedTypes.apply(0)
						val accessID = AccessIdentifier.newBuilder
						.setIdentifier(galagoResult.identifier)
						.setResourceId(getResourceKey)
						.build

						SearchResult.newBuilder
						.setId(accessID)
						.setProteusType(proteusType)
						.setTitle(galagoResult.displayTitle.replaceAll("<strong>", "").replaceAll("</strong>",""))
						.setSummary(generateSummary(galagoResult.summary))
						.setImgUrl(docImgURL(galagoResult))
						.setThumbUrl(docThumbURL(galagoResult))
						.setExternalUrl(docURL(galagoResult))
						.build
		}


		/** Override these five methods to change where in the metadata to look for important information **/
		def getTitle(metadata: Map[String, String], fallback: String) : String = tryMetaData(metadata, "title", fallback)
				def getURL(metadata: Map[String, String], fallback: String) : String = tryMetaData(metadata, "url", fallback)
				def getSummary(metadata: Map[String, String], fallback: String) : String = tryMetaData(metadata, "description", fallback)
				def getImgURL(metadata: Map[String, String], fallback: String) : String = tryMetaData(metadata, "imgurl", fallback)
				def getThumbURL(metadata: Map[String, String], fallback: String) : String = tryMetaData(metadata, "thumburl", fallback)

				def docTitle(doc: Document): String = getTitle(doc.metadata.asScala.toMap, doc.name)
				def docURL(doc: Document): String = getURL(doc.metadata.asScala.toMap, "")
				def docSummary(document: Document) = ResultSummary.newBuilder.setText(getSummary(document.metadata.asScala.toMap, document.text)).build
				def docImgURL(doc: Document) = getImgURL(doc.metadata.asScala.toMap, "")
				def docThumbURL(doc: Document) = getThumbURL(doc.metadata.asScala.toMap, docImgURL(doc))

				def docImgURL(result: tools.Search.SearchResultItem) = getImgURL(result.metadata.asScala.toMap, "")
				def docThumbURL(result: tools.Search.SearchResultItem) = getThumbURL(result.metadata.asScala.toMap, docImgURL(result))
				def docURL(result: tools.Search.SearchResultItem): String = getURL(result.metadata.asScala.toMap, "")

				def quickResult(id: String, pType: ProteusType, title: String, summary: String) : SearchResult = { 
				val accessID = AccessIdentifier.newBuilder.setIdentifier(id).setResourceId(getResourceKey).build
						SearchResult.newBuilder
						.setId(accessID)
						.setProteusType(pType)
						.setTitle(title)
						.setSummary(generateSummary(summary))
						.build
		}

		def fieldSearch(query: String, field: String, params: SearchParameters) : tools.Search.SearchResult = {
				galago.runQuery(query + "." + field, convertParameters(params), true) // same as: "#inside( #text:" + query +"() #field:" + field + "() )"?
		}

		def termCounts(terms: List[String]) : Map[String, Int] = {
				var termMap = new scala.collection.mutable.HashMap[String, Int]()
						terms.foreach(t => if(termMap.contains(t)) termMap(t) += 1 else termMap(t) = 1)
						return termMap.toMap
		}



		/**
		 * A simple regular expression approach to quickly checking if a term MIGHT be a date
		 */
		def isDate(term: String) : Boolean = {
				dateRE1.matcher(term).matches || dateRE2.matcher(term).matches
		}

		/**
		 * The date parsing function that should have already existed. It tries all the standard and them all of the 
		 * not so standard ways to phrase3 a date as a single term. It returns the tuple containing the parsed date as a 
		 * long and a boolean for whether or not the long is a valid parsing.
		 * TODO: Reorder the patterns so that it tries the most standard ones first.
		 */
		def parseDate(term: String) : (Long, Boolean) = {
				val dateParsers = List(DateTimeFormat.forPattern("y"), DateTimeFormat.forPattern("MMMM-y"), 
						DateTimeFormat.forPattern("MMMM/y"), DateTimeFormat.forPattern("M/y"), 
						DateTimeFormat.forPattern("M/d/y"), DateTimeFormat.forPattern("d/M/y"), 
						DateTimeFormat.forPattern("y/M/d"), DateTimeFormat.forPattern("y/d/M"),
						DateTimeFormat.forPattern("M-y"), DateTimeFormat.forPattern("M-d-y"), 
						DateTimeFormat.forPattern("d-M-y"), DateTimeFormat.forPattern("y-M-d"), DateTimeFormat.forPattern("y-d-M"),  
						DateTimeFormat.forPattern("M--y"), DateTimeFormat.forPattern("M--d--y"), 
						DateTimeFormat.forPattern("d--M--y"), DateTimeFormat.forPattern("y--M--d"), 
						DateTimeFormat.forPattern("y--d--M"), DateTimeFormat.forPattern("MMMM/y"), 
						DateTimeFormat.forPattern("MMMM/d/y"), DateTimeFormat.forPattern("MMMM-d-y"), 
						DateTimeFormat.forPattern("d-MMMM-y"), DateTimeFormat.forPattern("y-MMMM-d"), 
						DateTimeFormat.forPattern("y-d-MMMM"), DateTimeFormat.forPattern("MMMM--y"), 
						DateTimeFormat.forPattern("MMMM--d--y"), DateTimeFormat.forPattern("y--MMMM--d"), DateTimeFormat.forPattern("y--d--MMMM"))    						

						val parsedDates = dateParsers.map(sdf => try { sdf.asInstanceOf[DateTimeFormatter].parseDateTime(term).getMillis } catch { case _ => None }).filter(_ != None)
						if (parsedDates.length > 0)
							return (parsedDates(0).asInstanceOf[Long], true)
									else
										return (-1, false)
		}

		def genDateHistogram(termMap: Map[String, Int]) : LongValueHistogram = {
				val dateKeys = termMap.keys.filter(isDate _).toList
						val keyDates = dateKeys.map(k => {val r = parseDate(k); (k, r._1, r._2)}).filter(_._3).map(t => (t._1, (t._2, termMap(t._1)))).toMap
						val totalCount = keyDates.values.map(_._2).sum.toDouble
						return LongValueHistogram.newBuilder
								.addAllDates(keyDates.keys.toList.map(key => WeightedDate.newBuilder
								.setDate(keyDates(key)._1)
								.setWeight(keyDates(key)._2.toDouble / totalCount)
								.build).asJava)
								.build
		}


		def genTermHistogram(termMap: Map[String, Int]) : TermHistogram = {
				val totalCount = termMap.values.sum.toDouble
						return TermHistogram.newBuilder
								.addAllTerms(termMap.keys.toList.map(key => WeightedTerm.newBuilder
								.setTerm(key)
								.setWeight(termMap(key).toDouble / totalCount)
								.build).asJava)
								.build
		}


		/** Core Functionality Methods (MUST BE PROVIDED) **/
		override def runSearch(s: Search) : SearchResponse = {
				// For each type requested generate a random number of results
				val search_request = s.getSearchQuery
						val result = galago.runQuery(search_request.getQuery, convertParameters(search_request.getParams), true)
						return SearchResponse.newBuilder
								.addAllResults(result.items.asScala.map(r => convertResult(r)).asJava)
								.build

		}


		override def runContainerTransform(transform: ContainerTransform) : SearchResponse = {
				// Figure out what the correct container type is, then return a singleton response
				val result = fieldSearch(transform.getId.getIdentifier, transform.getFromType.toString.toLowerCase, transform.getParams)

						return SearchResponse.newBuilder
								.addAllResults(result.items.asScala.map(r => convertResult(r)).asJava)
								.build
		}

		override def runContentsTransform(transform: ContentsTransform) : SearchResponse = {
				if (containerFor(transform.getToType) == null || !containerFor(transform.getToType).contains(transform.getFromType)) {
					return SearchResponse.newBuilder
							.setError("Error in runContentsTransform: Incompatible to/from proteus types")
							.build
				} else {
					val document = galago.getDocument(transform.getId.getIdentifier)
							// Get all tags for the to_type contents field
							val contents_tags = document.tags.asScala.toList.filter(t => t.name.equals(transform.getToType.toString.toLowerCase))
							// Generate results from these identifiers
							return SearchResponse.newBuilder
									.addAllResults(contents_tags.map(t => {
										val id = document.text.slice(t.begin, t.end)
												val title = transform.getToType.toString + " from " + docTitle(document)
												quickResult(id, transform.getToType, title, title)
									}).slice(0, transform.getParams.getNumRequested).asJava)
									.build
				}
		}


		override def runOccurAsObjTransform(transform: OccurAsObjTransform) : SearchResponse = {
				transform.getFromType match {
				case ProteusType.PERSON | ProteusType.LOCATION | ProteusType.ORGANIZATION => 
				val result = galago.runQuery("obj_of_" + transform.getTerm, convertParameters(transform.getParams), true)
				return SearchResponse.newBuilder.addAllResults(result.items.asScala.map(r => convertResult(r)).asJava).build
						// An example of an unsupported implimentationtransform on a supported type
				case _ => 
				return SearchResponse.newBuilder.build // Empty, but no error
				}  
		}

		override def runOccurAsSubjTransform(transform: OccurAsSubjTransform) : SearchResponse  = {
				transform.getFromType match {
				case ProteusType.PERSON | ProteusType.LOCATION | ProteusType.ORGANIZATION => 
				val result = galago.runQuery("subj_of_" + transform.getTerm, convertParameters(transform.getParams), true)
				return SearchResponse.newBuilder.addAllResults(result.items.asScala.map(r => convertResult(r)).asJava).build

						// An example of an unsupported transform on a supported type
				case _ => 
				return SearchResponse.newBuilder.build // Empty, but no error
				}  
		}

		override def runOccurHasObjTransform(transform: OccurHasObjTransform) : SearchResponse = {
				transform.getFromType match {
				case ProteusType.PERSON | ProteusType.LOCATION | ProteusType.ORGANIZATION => 
				val result = galago.runQuery("has_obj_" + transform.getTerm, convertParameters(transform.getParams), true)
				return SearchResponse.newBuilder.addAllResults(result.items.asScala.map(r => convertResult(r)).asJava).build

						// An example of an unsupported transform on a supported type
				case _ => 
				return SearchResponse.newBuilder.build // Empty, but no error
				}  
		}

		override def runOccurHasSubjTransform(transform: OccurHasSubjTransform) : SearchResponse  = {
				transform.getFromType match {
				case ProteusType.PERSON | ProteusType.LOCATION | ProteusType.ORGANIZATION => 
				val result = galago.runQuery("has_subj_" + transform.getTerm, convertParameters(transform.getParams), true)
				return SearchResponse.newBuilder.addAllResults(result.items.asScala.map(r => convertResult(r)).asJava).build

						// An example of an unsupported transform on a supported type
				case _ => 
				return SearchResponse.newBuilder.build // Empty, but no error
				}  
		}

		/** Lookup Methods **/

		override def lookupCollection(accessID: AccessIdentifier) : Collection = {
				if (accessID.getResourceId != getResourceKey) {
					return Collection.newBuilder
							.setId(AccessIdentifier.newBuilder
									.setIdentifier(accessID.getIdentifier)
									.setResourceId(getResourceKey)
									.setError("Received lookup with mismatched resource ID: " + 
											accessID.getResourceId + " vs " + getResourceKey)
											.build)
											.build
				} else {
					val document = galago.getDocument(accessID.getIdentifier)
							val termMap = termCounts(document.terms.asScala.toList)
							val builder = Collection.newBuilder
							builder.setId(accessID)
							builder.setTitle(docTitle(document))
							builder.setSummary(docSummary(document))
							builder.setImgUrl(docImgURL(document))
							builder.setThumbUrl(docThumbURL(document))
							builder.setExternalUrl(docURL(document))
							builder.setDateFreq(genDateHistogram(termMap))
							builder.setLanguageModel(genTermHistogram(termMap))
							return builder.build
				}
		}

		override def lookupPage(accessID: AccessIdentifier) : Page = {
				if (accessID.getResourceId != getResourceKey) {
					return Page.newBuilder
							.setId(AccessIdentifier.newBuilder
									.setIdentifier(accessID.getIdentifier)
									.setResourceId(getResourceKey)
									.setError("Received lookup with mismatched resource ID: " + 
											accessID.getResourceId + " vs " + getResourceKey)
											.build)
											.build
				} else {
					// The other way to build. This one is a little nicer IMO
					val document = galago.getDocument(accessID.getIdentifier)
							val termMap = termCounts(document.terms.asScala.toList)
							return Page.newBuilder
									.setId(accessID)
									.setTitle(docTitle(document))
									.setSummary(docSummary(document))
									.setImgUrl(docImgURL(document))
									.setThumbUrl(docThumbURL(document))
									.setExternalUrl(docURL(document))
									.setDateFreq(genDateHistogram(termMap))
									.setLanguageModel(genTermHistogram(termMap))
									.build
				}
		}



		override def lookupPicture(accessID: AccessIdentifier) : Picture = {
				if (accessID.getResourceId != getResourceKey) {
					return Picture.newBuilder
							.setId(AccessIdentifier.newBuilder
									.setIdentifier(accessID.getIdentifier)
									.setResourceId(getResourceKey)
									.setError("Received lookup with mismatched resource ID: " + 
											accessID.getResourceId + " vs " + getResourceKey)
											.build)
											.build
				} else {
					val document = galago.getDocument(accessID.getIdentifier)
							return Picture.newBuilder
									.setId(accessID)
									.setTitle(docTitle(document))
									.setSummary(docSummary(document))
									.setImgUrl(docImgURL(document))
									.setThumbUrl(docThumbURL(document))
									.setExternalUrl(docURL(document))
									.build
				}
		}

		override def lookupVideo(accessID: AccessIdentifier) : Video = {
				if (accessID.getResourceId != getResourceKey) {
					return Video.newBuilder
							.setId(AccessIdentifier.newBuilder
									.setIdentifier(accessID.getIdentifier)
									.setResourceId(getResourceKey)
									.setError("Received lookup with mismatched resource ID: " + 
											accessID.getResourceId + " vs " + getResourceKey)
											.build)
											.build
				} else {
					val document = galago.getDocument(accessID.getIdentifier)
							return Video.newBuilder
									.setId(accessID)
									.setTitle(docTitle(document))
									.setSummary(docSummary(document))
									.setImgUrl(docImgURL(document))
									.setThumbUrl(docThumbURL(document))
									.setExternalUrl(docURL(document))
									.build
				}
		}

		override def lookupAudio(accessID: AccessIdentifier) : Audio = {
				if (accessID.getResourceId != getResourceKey) {
					return Audio.newBuilder
							.setId(AccessIdentifier.newBuilder
									.setIdentifier(accessID.getIdentifier)
									.setResourceId(getResourceKey)
									.setError("Received lookup with mismatched resource ID: " + 
											accessID.getResourceId + " vs " + getResourceKey)
											.build)
											.build
				} else {
					val document = galago.getDocument(accessID.getIdentifier)
							return Audio.newBuilder
									.setId(accessID)
									.setTitle(docTitle(document))
									.setSummary(docSummary(document))
									.setImgUrl(docImgURL(document))
									.setThumbUrl(docThumbURL(document))
									.setExternalUrl(docURL(document))
									.build
				}
		}

		override def lookupPerson(accessID: AccessIdentifier) : Person = {
				if (accessID.getResourceId != getResourceKey) {
					return Person.newBuilder
							.setId(AccessIdentifier.newBuilder
									.setIdentifier(accessID.getIdentifier)
									.setResourceId(getResourceKey)
									.setError("Received lookup with mismatched resource ID: " + 
											accessID.getResourceId + " vs " + getResourceKey)
											.build)
											.build
				} else {
					val document = galago.getDocument(accessID.getIdentifier)
							val termMap = termCounts(document.terms.asScala.toList)
							return Person.newBuilder
									.setId(accessID)
									.setTitle(docTitle(document))
									.setSummary(docSummary(document))
									.setImgUrl(docImgURL(document))
									.setThumbUrl(docThumbURL(document))
									.setWikiLink(docURL(document))
									.setDateFreq(genDateHistogram(termMap))
									.setLanguageModel(genTermHistogram(termMap))
									.setFullName(document.name)
									.build

				}
		}   

		override def lookupLocation(accessID: AccessIdentifier) : Location = {
				if (accessID.getResourceId != getResourceKey) {
					return Location.newBuilder
							.setId(AccessIdentifier.newBuilder
									.setIdentifier(accessID.getIdentifier)
									.setResourceId(getResourceKey)
									.setError("Received lookup with mismatched resource ID: " + 
											accessID.getResourceId + " vs " + getResourceKey)
											.build)
											.build
				} else {
					val document = galago.getDocument(accessID.getIdentifier)
							val termMap = termCounts(document.terms.asScala.toList)
							return Location.newBuilder
									.setId(accessID)
									.setTitle(docTitle(document))
									.setSummary(docSummary(document))
									.setImgUrl(docImgURL(document))
									.setThumbUrl(docThumbURL(document))
									.setWikiLink(docURL(document))
									.setDateFreq(genDateHistogram(termMap))
									.setLanguageModel(genTermHistogram(termMap))
									.setFullName(document.name)
									.setLongitude((util.Random.nextDouble - 0.5) * 2.0 * 180.0)
									.setLatitude((util.Random.nextDouble - 0.5) * 2.0 * 90.0)
									.build
				}
		} 

		override def lookupOrganization(accessID: AccessIdentifier) : Organization = {
				if (accessID.getResourceId != getResourceKey) {
					return Organization.newBuilder
							.setId(AccessIdentifier.newBuilder
									.setIdentifier(accessID.getIdentifier)
									.setResourceId(getResourceKey)
									.setError("Received lookup with mismatched resource ID: " + 
											accessID.getResourceId + " vs " + getResourceKey)
											.build)
											.build
				} else {
					val document = galago.getDocument(accessID.getIdentifier)
							val termMap = termCounts(document.terms.asScala.toList)
							return Organization.newBuilder
									.setId(accessID)
									.setTitle(docTitle(document))
									.setSummary(docSummary(document))
									.setImgUrl(docImgURL(document))
									.setThumbUrl(docThumbURL(document))
									.setWikiLink(docURL(document))
									.setDateFreq(genDateHistogram(termMap))
									.setLanguageModel(genTermHistogram(termMap))
									.setFullName(document.name)
									.build
				}
		} 


}
