package ciir.proteus.entitylinking

import collection.mutable.ListBuffer
import collection.mutable.HashMap

import scala.io.Source
import scala.xml._
import cc.refectorie.user.dietz.tacco.nlp.NlpData.{Token,NlpXmlMention, NlpXmlNerMention}

object TeiToNerMention {

  
//  def extractNerMentions(rawTeiString : String) : Seq[(String,  NlpXmlNerMention)] = {
//    
//    var currMention = ""
//    var currTokens = ListBuffer[Token]()
//    var currNerType = "O"
//    var lastTokenIdx = -1
//    val mentions = ListBuffer[(String, NlpXmlNerMention)]()
//    var sentenceId = 1;
//    
//    var tokens = ListBuffer[Token]()
//    
//    var data = XML.load(rawTeiString)
//    
//    val sentences = data \\ "s"
//    val coordToCharOffset = new HashMap[String, (Int, Int, Int)]
//    var curOffset = 0;
//    for ((sent, sentenceId) <- sentences.zipWithIndex) {
//      val words = sent \\ "w"
//      for (w <- words) {
//        val wordLen = (w \\ "@form").text.length()
//        val start = curOffset
//        coordToCharOffset += (w \\ "@coords").text -> (sentenceId, start, wordLen)
//        curOffset += wordLen
//      }
//    }
//    
//    val ners = data \\ "name"
//    for (ner <- ners) {
//       val words = ner \\ "w"
//    }
//    
//    for (line <- source.getLines()) {
//      if (line.length < 2) { // Sentence boundary
//        tokens = new ListBuffer[Token]()
//        sentenceId += 1
//      } else {
//
//        val fields = line.split("\t")
//        if (fields.length != 7) {
//            System.out.println("num fields: " + fields.length + " " + line)
//        } else {
//        val tokenIdx = fields(0).toInt
//        val word = fields(1)
//        val trueCased = fields(2)
//        val partOfSpeech = fields(3)
//        val ner = fields(4)
//        val charBegin = fields(5).toInt
//        val charEnd = fields(6).stripLineEnd.toInt
//        val token =  Token(rawWord = word, ner = ner, pos=partOfSpeech, lemma=trueCased, charBegin=charBegin, charEnd=charEnd)
//        
//        // NOTE: Below is taken from NerXmlReader in TACCO
//         if (ner != "O") {
//            if (currNerType == ner) {
//              if (currMention != "") currMention += " "
//              currMention += word
//              currTokens += token
//            } else {
//              if (currMention != "") {
//                // submit previous mention
//                assert(currNerType != "O",{"error case 1: currMention="+currMention})
//                mentions += (currNerType -> NlpXmlNerMention(currMention, currTokens, sentenceId, true, tokenIdx-1-currTokens.length, tokenIdx-1, currTokens.head.charBegin, currTokens.last.charEnd, currNerType))
//              }
//              // start new mention
//              currMention = word
//              currTokens = new ListBuffer[Token]()
//              currTokens += token
//            }
//
//
//          } else {
//            if (currMention != "") {
//              assert(currNerType != "O",{"error case 2: currMention="+currMention})
////              mentions += (currNerType -> currMention)
//              mentions += (currNerType -> NlpXmlNerMention(currMention, currTokens, sentenceId, true, tokenIdx-1-currTokens.length, tokenIdx-1, currTokens.head.charBegin, currTokens.last.charEnd, currNerType))
//              currMention = ""
//              currTokens = new ListBuffer[Token]()
//            }
//
//          }
//          currNerType = ner
//          lastTokenIdx = tokenIdx
//        }
//       }
//      }
//      if (currMention != "") {
//          assert(currNerType != "O",{"error case 3: currMention="+currMention})
//          mentions += (currNerType -> NlpXmlNerMention(currMention, currTokens, sentenceId, true, lastTokenIdx-currTokens.length, lastTokenIdx, currTokens.head.charBegin, currTokens.last.charEnd, currNerType))
//          currMention = ""
//          currTokens = new ListBuffer[Token]()
//      }
//      currNerType = "O"
//    mentions
//  }
  
}