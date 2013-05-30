
package ciir.proteus.entitylinking


import cc.refectorie.user.dietz.tacco.entitylinking.baseline.MentionEntityPairs.MentionWithContext
import cc.refectorie.user.dietz.tacco.data._
import cc.refectorie.user.dietz.tacco.entitylinking.eval.TacEntityLinkingEvaluation
import cc.refectorie.user.dietz.tacco.entitylinking.{TacPipelineHookup, TacGalagoPipelineHookups}
import cc.refectorie.user.dietz.tacco.entitylinking.baseline.ranker.ConfigurableFeatureRankingMain
import cc.refectorie.user.dietz.tacco.entitylinking.baseline.nil.ConfigurableNilClassifyMain
import cc.refectorie.user.dietz.tacco.util.ConfInfo
import java.lang.String
import java.io.{FileWriter, File}
import cc.refectorie.user.dietz.tacco.entitylinking.baseline.{RankingWriter, GalagoOnlyEntityPipeline}
import cc.refectorie.user.dietz.tacco.{GlobalCounters, PausableTimer, GlobalTimers, PrintConfInfo}
import cc.refectorie.user.dietz.tacco.entitylinking.baseline.GalagoCandidateGenerator
import cc.refectorie.user.dietz.tacco.entitylinking.baseline.MentionEntityPairs.{CoMentions, CandEntity, QueryMention}
import cc.refectorie.user.dietz.tacco.features.mention2kbentity.{Mention2EntityFeatureHasher, Mention2EntityFeatureConfig}
import collection.mutable.ListBuffer
import ciir.umass.edu.learning.{Ranker, RankerFactory, DataPoint}
import collection.mutable.HashMap


class BooksEntityLinker() {
  
//  type RawEntity <: WikipediaEntity
//  type RawQuery <: BookELQuery
  
  val nilThreshold = -0.05
    
  //val ranker = new ConfigurableFeatureRankingMain(true)
  //val nilPredict = new ConfigurableNilClassifyMain(true)

  println("Ranker model: " + PharosProperties.rankerModelFile)
  val ranker = new RankerFactory().loadRanker(PharosProperties.rankerModelFile);

   val domainMap = {
    val domainMap = new HashMap[ String, Int]()
    val f = io.Source.fromFile("./ltr/domainMap") 
    for (line <- f.getLines()) {
      val fields = line.split("\t")
      domainMap += (fields(0) -> fields(1).toInt)
    }
    domainMap
  }
  
  PrintConfInfo.print()

  val candidateGenerator = new GalagoCandidateGenerator[BookELQuery, WikipediaEntity]()

  def link(query: BookELQuery) : Option[WikipediaEntity] = {
     var rawCands = candidateGenerator.findCandidateEntities(query, 50)
     
     try {
     rawCands = LinkerUtil.filterByDate(rawCands)
     } catch {
       case e => System.err.println("Error filtering candidates!" + e.getMessage())
     }
     val cands = rawCands.map(c => new CandEntity(c.toString, c))
     val queryMention = new QueryMention(query.toString, query, trueAnswer = None, candidateEntities = cands)
     
     
     val candidateFeatures = 
       for (cand <- cands) yield {
         val m2eFeatures = Mention2EntityFeatureHasher.featuresAsMap(ConfInfo.rankingFeatures, queryMention.data, cand.data, cands.map(_.data))
         (query.asInstanceOf[TacELQuery2], cand.data.asInstanceOf[WikipediaEntity], m2eFeatures)
       }
      
     val reranked = rerankResults(candidateFeatures)
     
     if (rawCands.size > 0) {
       println("Linking result:\tquery: " + query.name + " " + "\ttop cand: " + rawCands.head.docId + "\treranked: " 
           + reranked.head.title + "\tscore: " + rawCands.head.score + "\tNIL?: " + (if (rawCands.head.score > nilThreshold)  false  else  true)) 
      if (rawCands.head.score > nilThreshold)  Some(reranked.head)  else  None
     } else {
       println("Linking result: query: " + query.name + " " + "top cand: NIL reranked: NIL") 
       None
     }
  }
  
  def rerankResults(results: Seq[(TacELQuery2, WikipediaEntity, Map[String, Double])]) : Seq[WikipediaEntity] = {
    
    val m2eDomainSet = results.flatMap(_._3.keys).toSet
    
    var rerankedResults = new ListBuffer[GalagoWikipediaEntity[ELQuery]]
    for ( (query, entity, m2eFeatures) <- results)  {
      val galagoEnt = entity.asInstanceOf[GalagoWikipediaEntity[ELQuery]]
      val dataPoint = m2eToRankLib(galagoEnt, m2eFeatures, m2eDomainSet)
      val score = ranker.eval(dataPoint)
      galagoEnt.score = score
      rerankedResults += galagoEnt
      //val isNil = if (score < nilThreshold ) { true} else {false}
    }
    
    val sorted = rerankedResults.sortBy(d => (-d.score, d.rank))
    val sortwidx = sorted.zipWithIndex
    for ((result, idx) <- sortwidx) {
       result.rank = (idx+1)
         //println(result.rank + " " + result.documentName + " " + result.score)
    }
    
    sorted
    
  }
  
   def m2eToRankLib(result : GalagoWikipediaEntity[ELQuery], m2eFeatures : Map[String, Double], m2eDomainSet : Set[String]) : DataPoint = {
   val sb = new StringBuilder

  
   val target = 0
   sb append target
   sb append " " 
          sb append "qid:"
          sb append "MADE_UP"
          sb append " "
          // now for the features
          val docFeatures = m2eFeatures
          for (feature <- domainMap.keys) {
            val domain = 
            sb append domainMap(feature)
            sb append ":" 
            sb append m2eFeatures.getOrElse(feature, 0.0)
            sb append " "
          }
          
             // append the galago rank!
          sb append (domainMap.size+1) + ":" 
          sb append (result.rank-1)
          sb append " " 
          
          
          sb append "#"
          sb append " "
          sb append result.docId
          //println(sb)
          val featureData = new DataPoint(sb.toString)
          featureData
  }

}

object BookEntityLinkingMain {
  def main(args:Array[String]) {
    GlobalTimers("total").continue()
    val configFile = new File("./config/properties/pharos.properties")
   
   PharosProperties.loadProperties(configFile.getAbsolutePath)
   val ents = Seq(BookELQueryImpl(docId = "mybook001", enttype = "person", queryId = "34823", name = "Michael Jackson", context = Seq("Michael jackson is the king of pop."),"",None,false, Seq(), Seq()))
   val linker = new BooksEntityLinker()
// linker.train()

   for (ent <- ents) {
     println("Results: " + linker.link(ent).head.title)
   }
   

//    linker.shutdown()

    println(GlobalTimers.printTimerInfo())
//    println("tfidf per item took "  +PausableTimer.runningForHumanFormat( (GlobalTimers("tfidf").runningFor / GlobalCounters("tfidf"))))
  }
}




