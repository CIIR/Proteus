package ciir.proteus

trait FakeDataGenerator {
  val chars =  ('a' to 'z')
  val intRange = 5 to 15
  val dblRange = 25.0 to 150.0
  val creators = ("marc", "will", "jeff", "james")
  val rnd = scala.util.Random

  private def genStr = {
    1 to intRange(rnd.nextInt(intRange length)) map { index =>
      chars(index)
    } mkString("")
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
    ResultSummary(genStr, regions)
  }

  def getSearchResults(count: Int, ptype: ProteusType) = {
    var results = List[SearchResult]()
    for (i <- 1 to count) {
      results =  SearchResult(genAccessId(ptype), genDbl, Some(genStr),
			      Some(genResultSummary), Some(genLink),
			      Some(genLink), Some(genLink)) :: results
    }
    results
  }

  def getPersonObjects(count: Int) = {
    1 to count map { _ =>
      Person(Some(genStr), List(genStr, genStr), Some(genLink), Some(genDate), Some(genDate))
    }
  }

  def getCollectionObjects(count: Int) = {
    1 to count map { _ =>
      Collection(Some(genDate), Some(genStr), Some(genStr), Some(genNum), List(genStr, genStr))
    }
  }

  def getPageObjects(count: Int) = {
    1 to count map { _ =>
      Page(Some(genStr), List(genStr, genStr), Some(genNum))
    }
  }

  def getPictureObjects(count: Int) = {
    1 to count map { _ =>
      Picture(Some(genStr), Some(Coordinates(0,0,0,0)), List(genStr, genStr))
    }
  }

  def getVideoObjects(count: Int) = {
    1 to count map { _ =>
      Video(Some(genStr), Some(Coordinates(0,0,0,0)), Some(genNum), List(genStr, genStr))
    }
  }

  def getAudioObjects(count: Int) = {
    1 to count map { _ =>
      Audio(Some(genStr), Some(Coordinates(0,0,0,0)), Some(genNum), List(genStr, genStr))
    }
  }

  def getLocationObjects(count: Int) = {
    1 to count map { _ =>
      Location(Some(genStr), List(genStr, genStr), Some(genLink), Some(genDbl), Some(genDbl) )
    }
  }

  def getOrganizationObjects(count: Int) = {
    1 to count map { _ =>
      Organization(Some(genStr), List(genStr, genStr), Some(genLink))
    }
  }
}
