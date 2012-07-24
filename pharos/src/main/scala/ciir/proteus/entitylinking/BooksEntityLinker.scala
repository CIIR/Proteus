
package ciir.proteus.entitylinking


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
import cc.refectorie.user.dietz.tacco.entitylinking.baseline.GalagoCandidateGenerator

trait BookELQuery extends TacELQuery with MentionWithContext {
  def queryId:String
  val nodeId = queryId
  val kbEntry = None
  override val source = "books"
}

case class BookELQueryImpl(docId: String, enttype:String, queryId:String, name:String, contextTerms:Seq[String], 
        fullText:String="", groundTruthWikiTitle:Option[String]=None, isNuissance:Boolean = false) extends BookELQuery


class BooksEntityLinker() {
  
//  type RawEntity <: WikipediaEntity
//  type RawQuery <: BookELQuery
  
  val ranker = new ConfigurableFeatureRankingMain(true)
  val nilPredict = new ConfigurableNilClassifyMain(true)

  PrintConfInfo.print()

  val candidateGenerator = new GalagoCandidateGenerator[BookELQuery, WikipediaEntity]()

  def link(query: BookELQuery) : Seq[WikipediaEntity] = {
     val res = candidateGenerator.findCandidateEntities(query, 10)
     res
  }

}

//object BookEntityLinkingMain {
//  def main(args:Array[String]) {
//    GlobalTimers("total").continue()
//
//   val ents = Seq(BookELQueryImpl(docId = "mybook001", enttype = "person", queryId = "34823", name = "Australia", contextTerms = Seq("president"),"",None,false))
//   val linker = new BooksEntityLinking(ents)
//// linker.train()
//
//   println("Results: " + linker.predict)
//
////    linker.shutdown()
//
//    println(GlobalTimers.printTimerInfo())
////    println("tfidf per item took "  +PausableTimer.runningForHumanFormat( (GlobalTimers("tfidf").runningFor / GlobalCounters("tfidf"))))
//  }
//}




