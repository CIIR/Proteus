package cc.refectorie.user.dietz.tacco.entitylinking.loadrun

import java.io.File
import java.io.Writer
import java.io.BufferedWriter
import java.io.PrintWriter
import java.io.OutputStreamWriter
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import scala.io.Source

object PharosConversionScriptGenerator {

  def main(args: Array[String]) {
    
    val outputFile = new File("./scripts/link-tac-jobs")
     
    val trainingQueries = SimpleRankUtil.selectTrainingQueries
    val testQueries = SimpleRankUtil.selectTestQueries 
    val allqueries = trainingQueries ++ testQueries
    
    var targetQueries = allqueries
    for (q <- targetQueries) {
       
     }
     
     val n = 1000
     var curLine = 0
     var curBatch = 0
     var p = new PrintWriter(outputFile + curBatch.toString + ".sh", "UTF-8")
//     for (line <- source.getLines ) {
//       val sb = new StringBuilder
//       curLine += 1
//       if (curLine % n == 0) {
//        p.close
//        curBatch += 1
//        curLine = 0
//        p = new PrintWriter(outputFile + curBatch.toString + ".sh", "UTF-8")
//       }
//       val tei = line.replace("djvu.xml.bz2", "mbtei.xml.gz")
//       val inputFile = new File(line)
//       
//       val bookId = inputFile.getParentFile.getName
//       
//       sb append "qsub -b y -l mem_free=4G -l mem_token=4G -cwd -o ./out/"
//       sb append bookId
//       sb append " -e ./err/"
//       sb append bookId
//       
//       sb append " /work1/allan/jdalton/Proteus/pharos/scripts/runPharosLinker.sh "
//       
//       // input file
//       sb append tei.trim
//       sb append " "
//       
//       // output file
//       sb append tei.trim.replace("mbtei", "mbtei_linked").replace("/work3/data/oca/text/data/", "/work1/allan/jdalton/linked-books/data/")
//       
//     // println(sb.toString)
//      p.println(sb.toString)
//     }
//   //  source.close()
//     p.close()
      
  }
}