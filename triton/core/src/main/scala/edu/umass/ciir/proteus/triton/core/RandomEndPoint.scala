package edu.umass.ciir.proteus.triton.core

import akka.actor.Actor
import akka.actor.Actor._
import akka.remoteinterface._
import scala.collection.JavaConverters._
import util.Random

import edu.umass.ciir.proteus.protocol.ProteusProtocol._

// Random End Point (Library resource) class
// Once the data store trait is completed it is an extremely simple class to create.
//
// myServer describes where this library server is running
// librarian describes where the librarian/manager is running, in order to notify it of our existence
class RandomEndPoint(myServer: ServerSetting, librarian: ServerSetting) extends 
LibraryServer with 
EndPointConnectionManagement with
EndPointQueryManagement with
EndPointLookupManagement with 
RandomDataStore {  
  val serverHostname : String = myServer.hostname
  val serverPort : Int = myServer.port
  val serverGroupID : String = "randProteus"
  var connection : ConnectLibrary = null
  
  override def preStart() = {
    super.preStart
    connection = buildConnection
    connectToLibrarian(librarian.hostname, librarian.port)
  }
}

// A little app to let us run this end point
object randEndPointApp extends App {
  val libraryService = try {
    val mysettings = ServerSetting(args(0), args(1).toInt)
    try {
      val librarySettings = ServerSetting(args(2), args(3).toInt)
      actorOf(new RandomEndPoint(mysettings, librarySettings)).start()
    } catch {
      case _ => println("Library connection settings not given on command line using defaults.")
      actorOf(new RandomEndPoint(mysettings, ServerSetting(mysettings.hostname, 8081))).start()
    }
  } catch {
    case _ => println("No arguments supplied using defaults.")
    actorOf(new RandomEndPoint(ServerSetting("localhost", 8082), ServerSetting("localhost", 8081))).start()
  }	
}

/********* Implementation of the Random End Point / Random Library *************/
//
// A random data store implementation. Useful as an example of how to implement an end point easily.
trait RandomDataStore extends EndPointDataStore with RandomDataGenerator {	
  // Some random links used in our data store
  final val wikimedia = "http://upload.wikimedia.org/wikipedia/commons/thumb"
  val imgURLs = List[String](wikimedia + "/4/44/Abraham_Lincoln_head_on_shoulders_photo_portrait.jpg/220px-Abraham_Lincoln_head_on_shoulders_photo_portrait.jpg", 
		  	     wikimedia + "/3/39/GodfreyKneller-IsaacNewton-1689.jpg/225px-GodfreyKneller-IsaacNewton-1689.jpg", 
		  	     wikimedia + "/d/d4/Thomas_Bayes.gif/300px-Thomas_Bayes.gif", 
		  	     wikimedia + "/d/d4/Johannes_Kepler_1610.jpg/225px-Johannes_Kepler_1610.jpg", 
		  	     wikimedia + "/f/f5/Einstein_1921_portrait2.jpg/225px-Einstein_1921_portrait2.jpg", 
		  	     wikimedia + "/d/d4/Justus_Sustermans_-_Portrait_of_Galileo_Galilei%2C_1636.jpg/225px-Justus_Sustermans_-_Portrait_of_Galileo_Galilei%2C_1636.jpg", 
		  	     wikimedia + "/1/1e/Thomas_Jefferson_by_Rembrandt_Peale%2C_1800.jpg/220px-Thomas_Jefferson_by_Rembrandt_Peale%2C_1800.jpg", 
		  	     wikimedia + "/c/cc/BenFranklinDuplessis.jpg/220px-BenFranklinDuplessis.jpg", 
		  	     wikimedia + "/9/9d/EU-Austria.svg/250px-EU-Austria.svg.png", 
		  	     wikimedia + "/b/b2/Clock_Tower_-_Palace_of_Westminster%2C_London_-_September_2006-2.jpg/220px-Clock_Tower_-_Palace_of_Westminster%2C_London_-_September_2006-2.jpg", 
		  	     wikimedia + "/a/a0/Grand_canyon_hermits_rest_2010.JPG/250px-Grand_canyon_hermits_rest_2010.JPG", 
		  	     wikimedia + "/f/fa/Great_Wall_of_China_July_2006.JPG/248px-Great_Wall_of_China_July_2006.JPG")
  
  val thumbURLs = List[String](wikimedia + "/4/44/Abraham_Lincoln_head_on_shoulders_photo_portrait.jpg/220px-Abraham_Lincoln_head_on_shoulders_photo_portrait.jpg", 
		  	       wikimedia + "/3/39/GodfreyKneller-IsaacNewton-1689.jpg/225px-GodfreyKneller-IsaacNewton-1689.jpg", 
		  	       wikimedia + "/d/d4/Thomas_Bayes.gif/300px-Thomas_Bayes.gif", 
		  	       wikimedia + "/d/d4/Johannes_Kepler_1610.jpg/225px-Johannes_Kepler_1610.jpg", 
		  	       wikimedia + "/f/f5/Einstein_1921_portrait2.jpg/225px-Einstein_1921_portrait2.jpg", 
		  	       wikimedia + "/d/d4/Justus_Sustermans_-_Portrait_of_Galileo_Galilei%2C_1636.jpg/225px-Justus_Sustermans_-_Portrait_of_Galileo_Galilei%2C_1636.jpg", 
		  	       wikimedia + "/1/1e/Thomas_Jefferson_by_Rembrandt_Peale%2C_1800.jpg/220px-Thomas_Jefferson_by_Rembrandt_Peale%2C_1800.jpg", 
		  	       wikimedia + "/c/cc/BenFranklinDuplessis.jpg/220px-BenFranklinDuplessis.jpg", 
		  	       wikimedia + "/9/9d/EU-Austria.svg/250px-EU-Austria.svg.png", 
		  	       wikimedia + "/b/b2/Clock_Tower_-_Palace_of_Westminster%2C_London_-_September_2006-2.jpg/220px-Clock_Tower_-_Palace_of_Westminster%2C_London_-_September_2006-2.jpg", 
		  	       wikimedia + "/a/a0/Grand_canyon_hermits_rest_2010.JPG/250px-Grand_canyon_hermits_rest_2010.JPG", 
		  	       wikimedia + "/f/fa/Great_Wall_of_China_July_2006.JPG/248px-Great_Wall_of_China_July_2006.JPG")
  val wikiLinks = List[String]("http://en.wikipedia.org/wiki/University_of_Massachusetts_Amherst",
    			       "http://en.wikipedia.org/wiki/Bono",
    			       "http://en.wikipedia.org/wiki/One_Laptop_per_Child")    
  val extLinks = List[String]("http://www.archive.org/stream/surveypart5ofconditiounitrich", 
		  	      "http://www.archive.org/stream/historyofoneidac11cook",
		  	      "http://www.archive.org/stream/completeguidetoh00foxduoft",
		  	      "http://www.youtube.com/watch?v=4M98x-FLp7E", 
		  	      "http://www.youtube.com/watch?v=qYx7YG0RsFY",
		  	      "http://www.youtube.com/watch?v=W1czBcnX1Ww",
		  	      "http://www.youtube.com/watch?v=dQw4w9WgXcQ")	
  
  /** Methods Defined Elsewhere **/
  def containerFor(ptype: ProteusType) : List[ProteusType]
  def getResourceKey : String
  def numProteusTypes : Int
  
  /** Methods Used Here and Elsewhere (MUST BE PROVIDED) **/
  def getSupportedTypes : List[ProteusType] = List.range(0, numProteusTypes).map(i => ProteusType.valueOf(i))
  override def getDynamicTransforms : List[DynamicTransformID] = {
    val firstDTID = DynamicTransformID.newBuilder
    .setName("weird")
    .setFromType(ProteusType.COLLECTION)
    .build
    val secondDTID = DynamicTransformID.newBuilder
    .setName("illustrative")
    .setFromType(ProteusType.PERSON)
    .build
    val overloadedDTID = DynamicTransformID.newBuilder
    .setName("weird")
    .setFromType(ProteusType.PAGE)
    .build	
    
    return List(firstDTID, secondDTID, overloadedDTID)
  }
  
  def supportsType(ptype: ProteusType) : Boolean = true
  def supportsDynTransform(dtID: DynamicTransformID) : Boolean = getDynamicTransforms.contains(dtID)
  
  /** Internal Utility Methods **/
  def generateRandomSummary : ResultSummary = {
    return ResultSummary.newBuilder
    .setText("Summarizing... " + List.range(0, Random.nextInt(30)+2).map(_ => genKey()).mkString(" "))
    .addHighlights(TextRegion.newBuilder.setStart(0).setStop(Random.nextInt(10)+1).build)
    .build
  }
  
  def generateRandomResult(ptype: ProteusType) : SearchResult = {
    val accessID = AccessIdentifier.newBuilder
    .setIdentifier(genKey())
    .setResourceId(getResourceKey)
    .build
    
    val result = SearchResult.newBuilder
    .setId(accessID)
    .setProteusType(ptype)
    .setTitle("Title: " + genKey())
    .setSummary(generateRandomSummary)
    .setImgUrl(imgURLs(Random.nextInt(imgURLs.length)))
    .setThumbUrl(thumbURLs(Random.nextInt(thumbURLs.length)))
    .setExternalUrl(extLinks(Random.nextInt(extLinks.length)))
    .build
    return result
  }
  
  def genNRandomResults(ptype: ProteusType, N: Int) :  List[SearchResult] = {
    List.range(0, N).map(_ => generateRandomResult(ptype))
  }
  
  def genSimpleRandomResponse(ptype: ProteusType, numReq: Int) : SearchResponse = {
    val total_results = Random.shuffle(genNRandomResults(ptype, 
    							      Random.nextInt(numReq)))
    
    return SearchResponse.newBuilder
    .addAllResults(total_results.asJava)
    .build
  }
  
  def genRandomDates : LongValueHistogram = {
    LongValueHistogram.newBuilder
    .addAllDates(List.range(0, Random.nextInt(50))
  		 .map(_ => WeightedDate.newBuilder
  		      .setDate(Random.nextLong)
  		      .setWeight(Random.nextDouble)
  		      .build).asJava)
    .build
  }
  
  def genRandomTermHist : TermHistogram = {
    TermHistogram.newBuilder
    .addAllTerms(List.range(0, Random.nextInt(50))
  		 .map(_ => WeightedTerm.newBuilder
  		      .setTerm(genKey())
  		      .setWeight(Random.nextDouble)
  		      .build).asJava)
    .build
  }
  
  def genRandCoordinates : Coordinates = {
    val left = Random.nextInt(500)
    val up = Random.nextInt(500)
    
    return Coordinates.newBuilder
    .setLeft(left)
    .setUp(up)
    .setRight(left + Random.nextInt(500) + 10)
    .setDown(up + Random.nextInt(500) + 10)
    .build
  }
  
  /** Core Functionality Methods (MUST BE PROVIDED) **/
  override def runSearch(s: Search) : SearchResponse = {
    // For each type requested generate a random number of results
    val search_request = s.getSearchQuery
    val total_results = Random.shuffle(search_request.getTypesList.asScala
  	  				    .map(t => genNRandomResults(t, Random.nextInt(search_request.getParams.getNumRequested)))
  	  				    .flatten)
    
    return SearchResponse.newBuilder
    .addAllResults(total_results.slice(0, search_request.getParams.getNumRequested).asJava)
    .build
  }
  
  override def runContainerTransform(transform: ContainerTransform) : SearchResponse = {
    // Figure out what the correct container type is, then return a singleton response
    val result = genNRandomResults(transform.getToType, Random.nextInt(transform.getParams.getNumRequested))
    
    return SearchResponse.newBuilder
    .addAllResults(result.asJava)
    .build
  }
  
  override def runContentsTransform(transform: ContentsTransform) : SearchResponse = {
    if (containerFor(transform.getToType) == null || !containerFor(transform.getToType).contains(transform.getFromType)) {
      return SearchResponse.newBuilder
      .setError("Error in runContentsTransform: Incompatible to/from proteus types")
      .build
    } else {
      return genSimpleRandomResponse(transform.getToType, transform.getParams.getNumRequested)
    }
  }
  
  override def runOverlapsTransform(transform: OverlapsTransform) : SearchResponse = {
    transform.getFromType match {
      // An example of an unsupported transform on a supported type
      case ProteusType.PERSON | ProteusType.LOCATION | ProteusType.ORGANIZATION => 
    	return SearchResponse.newBuilder.build // Empty, but no error
      case frmType: ProteusType => 
    	return genSimpleRandomResponse(frmType, transform.getParams.getNumRequested)
    }
  }
  
  override def runOccurAsObjTransform(transform: OccurAsObjTransform) : SearchResponse = {
    transform.getFromType match {
      case ProteusType.PERSON | ProteusType.LOCATION | ProteusType.ORGANIZATION => 
    	return genSimpleRandomResponse(ProteusType.PAGE, transform.getParams.getNumRequested)
      
      // An example of an unsupported implimentationtransform on a supported type
      case _ => 
    	return SearchResponse.newBuilder.build // Empty, but no error
    }  
  }
  
  override def runOccurAsSubjTransform(transform: OccurAsSubjTransform) : SearchResponse  = {
    transform.getFromType match {
      case ProteusType.PERSON | ProteusType.LOCATION | ProteusType.ORGANIZATION => 
    	return genSimpleRandomResponse(ProteusType.PAGE, transform.getParams.getNumRequested)
      
      // An example of an unsupported transform on a supported type
      case _ => 
    	return SearchResponse.newBuilder.build // Empty, but no error
    }  
  }
  
  override def runOccurHasObjTransform(transform: OccurHasObjTransform) : SearchResponse = {
    transform.getFromType match {
      case ProteusType.PERSON | ProteusType.LOCATION | ProteusType.ORGANIZATION => 
    	return genSimpleRandomResponse(ProteusType.PAGE, transform.getParams.getNumRequested)
      
      // An example of an unsupported transform on a supported type
      case _ => 
    	return SearchResponse.newBuilder.build // Empty, but no error
    }  
  }
  
  override def runOccurHasSubjTransform(transform: OccurHasSubjTransform) : SearchResponse  = {
    transform.getFromType match {
      case ProteusType.PERSON | ProteusType.LOCATION | ProteusType.ORGANIZATION => 
    	return genSimpleRandomResponse(ProteusType.PAGE, transform.getParams.getNumRequested)
      
      // An example of an unsupported transform on a supported type
      case _ => 
    	return SearchResponse.newBuilder.build // Empty, but no error
    }  
  }
  
  override def runNearbyLocationsTransform(transform: NearbyLocationsTransform) : SearchResponse  = {
    return genSimpleRandomResponse(ProteusType.LOCATION, transform.getParams.getNumRequested)
  }
  
  override def runDynamicTransform(transform: DynamicTransform) : SearchResponse = {
    if (!getDynamicTransforms.contains(transform.getTransformId)) {
      // This indicates a bug in the routing from the librarian
      return SearchResponse.newBuilder
      .setError("Error in runDynamicTransform: Unsupported transformation: " + 
    		transform.getTransformId + " " + transform.getTransformId.getFromType.getValueDescriptor.getName)
      .build
    } else {
      return genSimpleRandomResponse(ProteusType.valueOf(Random.nextInt(numProteusTypes)), 
    				     transform.getParams.getNumRequested)
    }
  }
  
  
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
      val builder = Collection.newBuilder
      builder.setId(accessID)
      builder.setTitle("Collection: " + genKey())
      builder.setSummary(generateRandomSummary)
      builder.setImgUrl(imgURLs(Random.nextInt(imgURLs.length)))
      builder.setThumbUrl(thumbURLs(Random.nextInt(thumbURLs.length)))
      builder.setExternalUrl(extLinks(Random.nextInt(extLinks.length)))
      builder.setDateFreq(genRandomDates)
      builder.setLanguageModel(genRandomTermHist)
      // Not setting the optional field language (override defaults to english), as an example
      // builder.setLanguage("en")
      builder.setPublicationDate(Random.nextLong)
      builder.setPublisher("Publishing house of Umass")
      builder.setEdition("First")
      builder.setNumPages(Random.nextInt(2000) + 1)
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
      return Page.newBuilder
      .setId(accessID)
      .setTitle("Page: " + genKey())
      .setSummary(generateRandomSummary)
      .setImgUrl(imgURLs(Random.nextInt(imgURLs.length)))
      .setThumbUrl(thumbURLs(Random.nextInt(thumbURLs.length)))
      .setExternalUrl(extLinks(Random.nextInt(extLinks.length)))
      .setDateFreq(genRandomDates)
      .setLanguageModel(genRandomTermHist)
      .setLanguage("en")
      .setFullText(List.range(0, Random.nextInt(1000)+10).map(_ => genKey(Random.nextInt(13)+1)).mkString(" "))
      .addCreators("Logan Giorda")
      .addCreators("Will Dabney")
      .setPageNumber(Random.nextInt(1000))
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
      return Picture.newBuilder
      .setId(accessID)
      .setTitle("Picture: " + genKey())
      .setSummary(generateRandomSummary)
      .setImgUrl(imgURLs(Random.nextInt(imgURLs.length)))
      .setThumbUrl(thumbURLs(Random.nextInt(thumbURLs.length)))
      .setExternalUrl(extLinks(Random.nextInt(extLinks.length)))
      .setDateFreq(genRandomDates)
      .setLanguageModel(genRandomTermHist)
      .setLanguage("en")
      .setCaption("Caption: " + List.range(0, Random.nextInt(5)+1).map(_ => genKey(Random.nextInt(8)+1)).mkString(" "))
      .setCoordinates(genRandCoordinates)
      .addCreators("Logan Giorda")
      .addCreators("Will Dabney")
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
      return Video.newBuilder
      .setId(accessID)
      .setTitle("Video Clip: " + genKey())
      .setSummary(generateRandomSummary)
      .setImgUrl(imgURLs(Random.nextInt(imgURLs.length)))
      .setThumbUrl(thumbURLs(Random.nextInt(thumbURLs.length)))
      .setExternalUrl(extLinks(Random.nextInt(extLinks.length)))
      .setDateFreq(genRandomDates)
      .setLanguageModel(genRandomTermHist)
      .setLanguage("en")
      .setCaption("Caption: " + List.range(0, Random.nextInt(5)+1).map(_ => genKey(Random.nextInt(8)+1)).mkString(" "))
      .setCoordinates(genRandCoordinates)
      .addCreators("Logan Giorda")
      .addCreators("Will Dabney")
      .setLength(Random.nextInt(5000))
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
      return Audio.newBuilder
      .setId(accessID)
      .setTitle("Audio Clip: " + genKey())
      .setSummary(generateRandomSummary)
      .setImgUrl(imgURLs(Random.nextInt(imgURLs.length)))
      .setThumbUrl(thumbURLs(Random.nextInt(thumbURLs.length)))
      .setExternalUrl(extLinks(Random.nextInt(extLinks.length)))
      .setDateFreq(genRandomDates)
      .setLanguageModel(genRandomTermHist)
      .setLanguage("en")
      .setCaption("Caption: " + List.range(0, Random.nextInt(5)+1).map(_ => genKey(Random.nextInt(8)+1)).mkString(" "))
      .setCoordinates(genRandCoordinates)
      .addCreators("Logan Giorda")
      .addCreators("Will Dabney")
      .setLength(Random.nextInt(5000))
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
      return Person.newBuilder
      .setId(accessID)
      .setTitle("Person: " + genKey())
      .setSummary(generateRandomSummary)
      .setImgUrl(imgURLs(Random.nextInt(imgURLs.length)))
      .setThumbUrl(thumbURLs(Random.nextInt(thumbURLs.length)))
      .setDateFreq(genRandomDates)
      .setLanguageModel(genRandomTermHist)
      .setFullName(genKey().capitalize + " " + genKey().capitalize)
      .addAllAlternateNames(List.range(0, Random.nextInt(5)).map(_ => genKey().capitalize + " " + genKey().capitalize).asJava)
      .setWikiLink(wikiLinks(Random.nextInt(wikiLinks.length)))
      .setBirthDate(Random.nextLong)
      .setDeathDate(Random.nextLong)
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
      return Location.newBuilder
      .setId(accessID)
      .setTitle("Location: " + genKey())
      .setSummary(generateRandomSummary)
      .setImgUrl(imgURLs(Random.nextInt(imgURLs.length)))
      .setThumbUrl(thumbURLs(Random.nextInt(thumbURLs.length)))
      .setDateFreq(genRandomDates)
      .setLanguageModel(genRandomTermHist)
      .setFullName(genKey().capitalize + " " + genKey().capitalize)
      .addAllAlternateNames(List.range(0, Random.nextInt(5)).map(_ => genKey().capitalize + " " + genKey().capitalize).asJava)
      .setWikiLink(wikiLinks(Random.nextInt(wikiLinks.length)))
      .setLongitude((Random.nextDouble - 0.5) * 2.0 * 180.0)
      .setLatitude((Random.nextDouble - 0.5) * 2.0 * 90.0)
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
      return Organization.newBuilder
      .setId(accessID)
      .setTitle("Organization: " + genKey())
      .setSummary(generateRandomSummary)
      .setImgUrl(imgURLs(Random.nextInt(imgURLs.length)))
      .setThumbUrl(thumbURLs(Random.nextInt(thumbURLs.length)))
      .setDateFreq(genRandomDates)
      .setLanguageModel(genRandomTermHist)
      .setFullName(genKey().capitalize + " " + genKey().capitalize)
      .setWikiLink(wikiLinks(Random.nextInt(wikiLinks.length)))
      .build
    }
  } 
  
  
}
