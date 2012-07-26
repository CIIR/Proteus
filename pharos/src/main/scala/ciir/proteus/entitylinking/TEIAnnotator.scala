package ciir.proteus.entitylinking

import java.io.Writer
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import collection.JavaConversions._
import cc.refectorie.user.dietz.tacco.data.WikipediaEntity
import scala.xml._
import scala.xml.transform._
import scala.collection.mutable.HashMap

object TEIAnnotator {

      val linker = new BooksEntityLinker
     
       def main(args: Array[String]) {
       	   if (args.length < 1) {
	      println("Usage: scala TEIAnnotator inputFile outputFile")
	      exit
	   }
       println("Annotating TEI file " + args(0) + ", new file saving to " + args(1))
	   processTeiFile(args(0), args(1))
       }
      
  def processTeiFile(input_filename: String, output_filename: String) {
    val input = new java.io.InputStreamReader(new GZIPInputStream(new FileInputStream(input_filename)))
    var data = XML.load(reader = input)
//    var entities = data \\ "name"
//    val bookId = input_filename.split("/").last.split("_")(0)
//    
//    val entitiesWithIdx = entities.zipWithIndex take 2
//     for ((e, idx) <- entitiesWithIdx) {
//       val words = e \\ "w" \\ "@form"
//       val entityText = words.mkString(" ")
//       println("Now linking entity " + idx + " name: " + entityText)
//       val query = BookELQueryImpl(docId = bookId, enttype = e.attribute("type").get.text, queryId = bookId+idx, name = entityText, contextTerms =  Seq[String]())
//       println("Linking query: " + query.name)
//       val annotation = linker.link(query)
//       
//       updateWikiLink(e, annotation.head.title)
//     }
    val transformed = EntityRuleTransformer(data)
    println(transformed)
    val outputWriter = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(output_filename))))
    scala.xml.XML.write(outputWriter, transformed,"utf-8",true,null)
    outputWriter.close()
  }
  
  
  object LinkTransformer extends RewriteRule {
    
      case class GenAttr(pre: Option[String], 
                   key: String, 
                   value: Seq[Node], 
                   next: MetaData) {
  def toMetaData = Attribute(pre, key, value, next)
}

def decomposeMetaData(m: MetaData): Option[GenAttr] = m match {
  case Null => None
  case PrefixedAttribute(pre, key, value, next) => 
    Some(GenAttr(Some(pre), key, value, next))
  case UnprefixedAttribute(key, value, next) => 
    Some(GenAttr(None, key, value, next))
}

def unchainMetaData(m: MetaData): Iterable[GenAttr] = 
  m flatMap (decomposeMetaData)

def doubleValues(l: Iterable[GenAttr]) = l map {
  case g @ GenAttr(_, _, Text(v), _) if v matches "\\d+" => 
    g.copy(value = Text(v.toInt * 2 toString))
  case other => other
}

def chainMetaData(l: Iterable[GenAttr]): MetaData = l match {
  case Nil => Null
  case head :: tail => head.copy(next = chainMetaData(tail)).toMetaData
}

def mapMetaData(m: MetaData)(f: GenAttr => GenAttr): MetaData = 
  chainMetaData(unchainMetaData(m).map(f))

  val cache = new HashMap[String, String]
  val attribs = "type"  
  override def transform(n: Node): Seq[Node] = (n match {
    case e: Elem =>
      {
        if (e.label equals "name") {
          var attributes1 = unchainMetaData(e.attributes)
          if (attributes1.size > 0) {
            val entity = linkEntity(e)
            var newAttrib = GenAttr(None, "Wiki_Title", Text(entity), Null)
            var newList = attributes1.toBuffer
            newList += newAttrib
            var iterableList = newList.toList
            val newMeta = chainMetaData(iterableList)
            e.copy(attributes = newMeta)
          } else {
            e
          }
        } else {
          e
        }
      }
    case other => other
  }).toSeq
  
    

     def linkEntity(e: Node) : String = {
       var bookId = ""
       val words = e \\ "w" \\ "@form"
       val entityText = words.mkString(" ")
       
       def performLinking(query: String) : String = {
         var cleanQuery = query.replace(".","")
         if (cleanQuery.trim().length() < 4) {
           "NIL"
         } else {
           val query = BookELQueryImpl(docId = bookId, enttype = e.attribute("type").get.text, queryId = bookId, name = cleanQuery, contextTerms =  Seq[String]())
                   println("Linking query: " + query.name)
                   val annotation = linker.link(query)
                   annotation.head.title
         }
       }
       val result = cache.getOrElseUpdate(entityText, performLinking(entityText))

       result
     }
  }
  
  object EntityRuleTransformer extends RuleTransformer(LinkTransformer)
 


}



