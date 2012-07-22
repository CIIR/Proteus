
import cc.refectorie.user.dietz.tacco.entitylinking.baseline.MentionEntityPairs.MentionWithContext
import cc.refectorie.user.dietz.tacco.data.{TacELQuery, ELQuery, WikipediaEntity}
import cc.refectorie.user.dietz.tacco.entitylinking.eval.TacEntityLinkingEvaluation
import cc.refectorie.user.dietz.tacco.entitylinking.{TacMockPipelineHookup, TacPipelineHookup, TacGalagoPipelineHookups}
import cc.refectorie.user.dietz.tacco.entitylinking.baseline.ranker.ConfigurableFeatureRankingMain
import cc.refectorie.user.dietz.tacco.entitylinking.baseline.nil.ConfigurableNilClassifyMain
import cc.refectorie.user.dietz.tacco.util.ConfInfo
import java.lang.String
import java.io.{FileWriter, File}
import cc.refectorie.user.dietz.tacco.entitylinking.baseline.{RankingWriter, GalagoOnlyEntityPipeline}
import cc.refectorie.user.dietz.tacco.{GlobalCounters, PausableTimer, GlobalTimers, PrintConfInfo}

trait BookELQuery extends TacELQuery with MentionWithContext {
  def queryId:String
  val nodeId = queryId
  val kbEntry = None
  override val source = "books"
}

case class BookELQueryImpl(docId: String, enttype:String, queryId:String, name:String, contextTerms:Seq[String], 
	fullText:String="", groundTruthWikiTitle:Option[String]=None, isNuissance:Boolean = false) extends BookELQuery


class BooksEntityLinking(loadQueries: Seq[BookELQuery]) extends GalagoOnlyEntityPipeline with TacPipelineHookup with RankingWriter {
  val ranker = new ConfigurableFeatureRankingMain(true)
  val nilPredict = new ConfigurableNilClassifyMain(true)
  type PredictionEntry = PredictEntry
  type Predictions = Seq[PredictionEntry]

  PrintConfInfo.print()
/*
  val loadQueries:Seq[RawQuery] = {
    Seq[RawQuery](
      BookELQueryImpl(docId = "mybook001", enttype = "organization", queryId = "9394823", name = "AZ", contextTerms = Seq("text","text","text","and","out","movie","movie","movie"))
      , BookELQueryImpl(docId = "mybook001", enttype = "location", queryId = "34823", name = "AUSTRALIA", contextTerms = Seq("the","rahman","who"))
      , BookELQueryImpl(docId = "mybook001", enttype = "person", queryId = "34823", name = "MAUSTRALIA", contextTerms = Seq("the","group","theory"))
      , BookELQueryImpl(docId = "mybook001", enttype = "organization", queryId = "34823", name = "Bill Clinton", contextTerms = Seq("the","rahman","who"))
    )
  }
*/
  override lazy val (loadTrainQueries, loadTestQueries) = (Seq[RawQuery](), loadQueries)

  def train() {
//    trainPipeline()
//    printMissingNlpInfo()
  }

  def predict: Seq[WikipediaEntity] = {
	println("Predicting books..")
    val fullpredictions = predictPipeline()
//    GlobalTimers("evaluate").continue()
    val predictions = fullpredictions.map(_.getPair)
//    printPredictionOutput(predictions, ConfInfo.predictionOutput)
//    rankingWriter(fullpredictions, ConfInfo.rankingOutput)
    println("Predictions made....\n\n")
    println(predictions)
/*    val eval =  new TacEntityLinkingEvaluation(this)
    val result = eval.evaluatePredictions(predictions)
    GlobalTimers("evaluate").break()
    printMissingNlpInfo()
    println(result)*/
    println("returning...")
    return predictions.map( x => x._2.getOrElse(null))
	
  }



}

object BookEntityLinkingMain {
  def main(args:Array[String]) {
    GlobalTimers("total").continue()

   val ents = Seq(BookELQueryImpl(docId = "mybook001", enttype = "person", queryId = "34823", name = "Australia", contextTerms = Seq("president")))
   val linker = new BooksEntityLinking(ents)
// linker.train()

   println("Results: " + linker.predict)

//    linker.shutdown()

    println(GlobalTimers.printTimerInfo())
//    println("tfidf per item took "  +PausableTimer.runningForHumanFormat( (GlobalTimers("tfidf").runningFor / GlobalCounters("tfidf"))))
  }
}




