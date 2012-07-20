package ciir.proteus

trait FakeDataGenerator {
  val chars =  ('a' to 'z')
  val intRange = 5 to 15
  val dblRange = 25.0 to 150.0
  val creators = ("marc", "will", "jeff", "james")
  val rnd = scala.util.Random
  val zeroCoords = Some(Coordinates(0,0,0,0))

  private def genStr = {
    1 to intRange(rnd.nextInt(intRange length)) map 
      { index => chars(index) } mkString("")
  }
  
  private def genDate = rnd.nextLong()
  private def genDbl = rnd.nextDouble()
  private def genLink = "http://wiki-link/" + genStr
  private def genNum = rnd.nextInt()
  private def genAccessId(ptype: ProteusType) = 
    AccessIdentifier(genStr, ptype, genStr, None)
  private def genResultSummary() : ResultSummary = {
    var regions = List[TextRegion]()
    for (i <- 0 to 3) {
      val begin = genNum
      regions = TextRegion(begin, begin+genNum) :: regions
    }
    return ResultSummary(genStr, regions)
  }

  def getProteusObject(aid: AccessIdentifier) : ProteusObject = 
    ProteusObject(id = aid, 
		  title = Some(genStr), 
		  description = Some(genStr),
		  imgUrl = Some(genLink),
		  thumbUrl = Some(genLink),
		  externalUrl = Some(genLink),
		  dateFreq = None,
		  languageModel = None,
		  collection = getCollection(aid.`type`),
		  page = getPage(aid.`type`),
		  picture = getPicture(aid.`type`),
		  video = getVideo(aid.`type`),
		  audio = getAudio(aid.`type`),
		  person = getPerson(aid.`type`),
		  location = getLocation(aid.`type`),
		  organization = getOrganization(aid.`type`));

  def getSearchResults(count: Int, ptype: ProteusType) : List[SearchResult] = {
    var results = List[SearchResult]()
    for (i <- 1 to count) {
      results =  SearchResult(genAccessId(ptype), genDbl, Some(genStr),
			      Some(genResultSummary), Some(genLink),
			      Some(genLink), Some(genLink)) :: results
    }
    results
  }

  def getPerson(t: ProteusType) : Option[Person] = 
    if (t != ProteusType.Person)
      None
    else
      Some(Person(Some(genStr), 
		  List(genStr, genStr), 
		  Some(genLink), 
		  Some(genDate), 
		  Some(genDate)))
  
  def getCollection(t: ProteusType)  = 
    if (t != ProteusType.Collection)
      None
    else
      Some(Collection(Some(genDate),
		      Some(genStr),
		      Some(genStr),
		      Some(genNum),
		      List(genStr, genStr)))

  def getPage(t: ProteusType) = 
    if (t != ProteusType.Page)
      None
    else
      Some(Page(Some(genStr), List(genStr, genStr), Some(genNum)))

  def getPicture(t: ProteusType) = 
    if (t != ProteusType.Picture)
      None
    else
      Some(Picture(Some(genStr), zeroCoords, List(genStr, genStr)))

  def getVideo(t: ProteusType) = 
    if (t != ProteusType.Video)
      None
    else 
      Some(Video(Some(genStr), zeroCoords, Some(genNum), List(genStr, genStr)))

  def getAudio(t: ProteusType) = 
    if (t != ProteusType.Audio)
      None
    else
      Some(Audio(Some(genStr), zeroCoords, Some(genNum), List(genStr, genStr)))

  def getLocation(t: ProteusType) = 
    if (t != ProteusType.Location)
      None
    else
      Some(Location(Some(genStr),
		    List(genStr, genStr), 
		    Some(genLink), 
		    Some(genDbl), 
		    Some(genDbl)))

  def getOrganization(t: ProteusType) = 
    if (t != ProteusType.Organization)
      None
    else
      Some(Organization(Some(genStr), List(genStr, genStr), Some(genLink)))
}
