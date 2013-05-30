package ciir.proteus.entitylinking

import java.io._
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import collection.JavaConversions._
import cc.refectorie.user.dietz.tacco.data.WikipediaEntity
import scala.xml.pull._
import scala.xml._
import scala.xml.transform._
import scala.io.Source
import scala.collection.mutable.HashMap
import cc.refectorie.user.dietz.tacco.entitylinking.loader.TextNormalizer
import cc.factorie.app.strings.Stopwords
import scala.collection.mutable.ListBuffer
import cc.refectorie.user.dietz.tacco.data.BookELQueryImpl
import scala.collection.mutable.Stack
import scala.collection.mutable.Queue
import cc.refectorie.user.dietz.tacco.nlp.NlpData.{Token, NlpXmlMention, NlpXmlNerMention}

object TeiAnnotator2 {
  
   val MAX_NAME_CONTEXT = 100
   val MAX_WORD_CONTEXT = 1000
   
   val configFile = new File("./config/properties/pharos.properties")
   
   PharosProperties.loadProperties(configFile.getAbsolutePath)
   
   val linker = new BooksEntityLinker
             
   def main(args: Array[String]) {
         if (args.length < 2) {
          println("Usage: scala TEIAnnotator inputFile outputFile propertiesFile")
          exit
         }
         
         PharosProperties.printProperties()
         
         println("Annotating TEI file " + args(0) + ", new file saving to " + args(1))
         processTeiFile(args(0), args(1))
   }
      
  def processTeiFile(input_filename: String, output_filename: String) {
    println("Starting to process input file: " + input_filename)
    val input = new GZIPInputStream(new FileInputStream(input_filename))
    var src = Source.fromInputStream(input)
    val eventReader = new XMLEventReader(src)
    
    val stack = Stack[XMLEvent]()
    
    val outputFile = new File(output_filename)
    if (!outputFile.exists) {
      outputFile.getParentFile().mkdirs
    }
    
    val fos = new FileOutputStream(outputFile)
    val outputWriter = new PrintWriter(new OutputStreamWriter(new GZIPOutputStream(fos)))
    
    
    def iprint(ev:XMLEvent) = {
      outputWriter.print(backToXml(ev)) 
      print(backToXml(ev))
    }
    def iprintSeq(seq :Seq[XMLEvent]) = seq.map(ev => iprint(ev))

    var curPage = -1
    var curSentence = 0
    var curTokenOffset = 0
    var curCharOffset = 0
    var tokenContext = new Queue[Token]
    var nameContext = new Queue[NlpXmlNerMention]
    var pageEntities = new ListBuffer[(Int,NlpXmlNerMention)]
    
    var inName = false
    var eventBuffer = new ListBuffer[XMLEvent]
    var nameBuffer = new ListBuffer[XMLEvent]

    var numNameTokens = 0;
    
    var curNerType = "O"
    var curNameStartIdx = -1
    // go page by page, but build up a larger context window of terms and named entities.
      
    while (eventReader.hasNext) {
      val nextEvent = eventReader.next
      nextEvent match {
        
          case x @ EvElemStart(_, "pb", attrs, _) =>
              if (eventBuffer.size > 0) {
                
                // extract page text from current buffer
                val words = eventBuffer collect { case x @ EvElemStart(_, "w", attrs, _) => replaceChars(attrs("form").text) }
                val text = words.mkString(" ")
                val linkedEvents = processPageEntities(curPage, pageEntities, eventBuffer, tokenContext, nameContext, text)
                iprintSeq(linkedEvents)
                eventBuffer.clear()
                pageEntities.clear()
              }
               curPage = attrs("n").text.toInt
               eventBuffer += x

          case x @ EvElemStart(_, "w", attrs, _) =>
              val word = attrs("form").text
              val partOfSpeech = {
                if (attrs("type") != null) {
                 attrs("type").text
                } else {
                  ""
                }
              }
              
              val trueCased = {
                if (attrs("lemma") != null) {
                 attrs("lemma").text
                } else {
                  ""
                }
              }
              val charEnd = curCharOffset + word.length()
              val token =  Token(rawWord = word, ner = curNerType, pos=partOfSpeech, lemma=trueCased, charBegin=curCharOffset, charEnd=charEnd, sentence=curSentence)
              tokenContext.enqueue(token)
               if (tokenContext.size > MAX_WORD_CONTEXT) {
                tokenContext.drop(1)
              }
              
              curCharOffset += word.length()
              eventBuffer+=x
              if (inName) {
                numNameTokens+=1
              }
              curTokenOffset+=1
              
          case x @ EvElemStart(_, "name", attrs, _) =>
              inName = true
              curNameStartIdx = eventBuffer.length
              curNerType = attrs("type").text
              eventBuffer += x              
          case x @ EvElemStart(_, _, _, _) =>    
              eventBuffer += x               
        
          case x @ EvElemEnd(_, "name") => 
              val currTokens = tokenContext.takeRight(numNameTokens)
              val name = currTokens.map(t => t.word).mkString(" ")
              // we now have all the tokens, offsets.  construct the mention.
              val mention = NlpXmlNerMention(name, currTokens , curSentence, true, curTokenOffset-numNameTokens, curTokenOffset, currTokens.head.charBegin, currTokens.last.charEnd, curNerType)
              
              nameContext.enqueue(mention)
              if (nameContext.size > MAX_NAME_CONTEXT) {
                nameContext.drop(1)
              }
              
              pageEntities += ((curNameStartIdx, mention))
              eventBuffer += x
              inName = false
              numNameTokens = 0;
              curNerType = "O"
          case x @ EvElemEnd(_, tag) => 
                 eventBuffer += x
          case x @ EvText(text) => 
                 eventBuffer += x
          case x @ EvEntityRef(entity) => 
                 eventBuffer += x
          case x => eventBuffer += x
      }
    }
    
     if (eventBuffer.size > 0) {
      // extract page text from current buffer
      val words = eventBuffer collect { case x @ EvElemStart(_, "w", attrs, _) => attrs("form").text }
      val text = words.mkString(" ")
      val linkedEvents = processPageEntities(curPage, pageEntities, eventBuffer, tokenContext, nameContext, text)
      iprintSeq(linkedEvents)
     }
    outputWriter.close()
    println("Successfully wrote output file " + output_filename);
  }
  
  
  /**
   * Links all entities in the existing page.
   */
  def processPageEntities(pageId: Int, pageEntities: Seq[(Int,NlpXmlNerMention)], pageEvents:ListBuffer[XMLEvent], tokenContext: Seq[Token], nerContext:Seq[NlpXmlNerMention], pageText: String) : Seq[XMLEvent] = {

    val cache = new HashMap[String, Option[WikipediaEntity]]
    
    for ((idx, entity) <- pageEntities) {
      
      val cacheResult = cache.get(entity.text)
      val link = if (cacheResult == None) {
        // link the entity here!
        val result = linkEntity(entity, nerContext, tokenContext, pageText)
        cache += (entity.text -> result)
        result
      } else {
        cacheResult.get
      }
      
      
      val entityLen = entity.tokens.length
      // update the xml with link info 
      val event = pageEvents(idx)
      val (score, id) = if (link == None) {
        (0.0,"NIL")
      } else {
        (link.get.score, link.get.docId)
      }
      val linkedNode = updateAttribute("Wiki_Title", id, event.asInstanceOf[EvElemStart])
      val newNode = updateAttribute("Wiki_Confidence", score.toString, linkedNode.asInstanceOf[EvElemStart])
      pageEvents(idx) = newNode
    }
    
    pageEvents
  }
  
  
  def linkEntity(entity:NlpXmlNerMention, entityContext: Seq[NlpXmlNerMention], wordContext:Seq[Token], text:String) : Option[WikipediaEntity] = {
    
     val cleanTokens = new ListBuffer[String]
       for (token <- entity.tokens) {
        val text = token.word
        val normalToken = TextNormalizer.normalizeText(text)
          if (normalToken.length() > 0 && !Stopwords.contains(normalToken)) {
            cleanTokens += normalToken
          }
        }
       
       val query = cleanTokens.mkString(" ")
       
         var cleanQuery = query.replace(".","")
         val entityLink = if (cleanQuery.trim().length() < 4) {
           None
         } else {
           val query = BookELQueryImpl(docId = "", enttype = entity.ner, 
               queryId = "", name = cleanQuery, context =  wordContext.map(w => w.rawWord), contextTokens = wordContext, text=text,
               nerContext = entityContext)
                   println("Linking query: " + query.name)
                   val annotation = linker.link(query)
                   annotation
         }
        
    entityLink
    
  }
  
  
  
  def updateAttribute(attributeName: String, attributeValue: String, event: EvElemStart) : EvElemStart = {
    var attributes1 = unchainMetaData(event.attrs)
    val newNode = if (attributes1.size > 0) {
            var newAttrib = GenAttr(None, attributeName, Text(attributeValue), Null)
            var newList = attributes1.toBuffer
            newList += newAttrib
            var iterableList = newList.toList
            val newMeta = chainMetaData(iterableList)
            event.copy(attrs = newMeta)
          } else {
            event
          }
    newNode
  }
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
  
  def unchainMetaData(m: MetaData): Iterable[GenAttr] = m flatMap (decomposeMetaData)
  
  def chainMetaData(l: Iterable[GenAttr]): MetaData = l match {
  case Nil => Null
  case head :: tail => head.copy(next = chainMetaData(tail)).toMetaData
  }
  
  def backToXml(ev: XMLEvent) = {
//    println(ev.toString())
    ev match {
      case EvElemStart(pre, label, attrs, scope) => {
        "<" + label + attrsToString(attrs) + ">"
      }
      case EvElemEnd(pre, label) => {
        "</" + label + ">"
      }
      case EvText(text) => 
               text
      case EvEntityRef(entity) => 
               entity
      case _ => ""
    }
  }
 
  def attrsToString(attrs:MetaData) = {
    attrs.length match {
      case 0 => ""
      case _ => attrs.map( (m:MetaData) => " " + m.key + "='" + m.value +"'" ).reduceLeft(_+_)
    }
  }
  
  def replaceChars(word: String) : String = word match {
        case "-LRB-" => "("
        case "-RRB-" => ")"
        case "-RSB-" => "]"
        case "-LSB-" => "["
        case "-LCB-" => "{"
        case "-RCB-" => "}"
        case x => x
  }

}