package code
package snippet

// TODO: This file conversions needed


import java.util.HashSet
import java.util.regex.Pattern
import net.liftweb._
import code.comet.TheCart
import code.snippet._
import code.lib._
import http._
//import org.galagosearch.tupleflow.Parameters
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.ListBuffer
import util._
import Helpers._
import org.slf4j.LoggerFactory
import net.liftweb.common.Logger
import net.liftweb.common.Logger._
import scala.collection.JavaConversions._
//import org.galagosearch.core.retrieval.BadOperatorException
//import org.galagosearch.core.tools.Search.SearchResult
//import org.galagosearch.core.retrieval.Retrieval

import net.liftweb.common._
import net.liftweb._
import http._
import net.liftweb.http.js.jquery.JqWiringSupport

import scala.xml.NodeSeq
import sitemap._
import util._
import Helpers._

import util._
import js._
import js.jquery._
import JsCmds._
//import org.galagosearch.tupleflow.Utility;
//import org.tartarus.snowball.SnowballStemmer;
//import org.tartarus.snowball.ext.englishStemmer;

import code.comet.BookBag._
import edu.umass.ciir.proteus.protocol.ProteusProtocol._
import scala.collection.JavaConverters._

object Entity extends Query {

  val cannedAmbiguousResponse = "This entity has several possible matches within the Wikipedia.  Click the link above to search the Wikipedia for matches.  Words that occur near the entity name are listed below along with the number of times they occur on the same page in this collection."

  val stopwords : Set[String] = try {
	  io.Source.fromInputStream(getClass().getResourceAsStream("/stopwords/inquery")).getLines().toSet

    } catch {
      case _ =>
      // Oh well
      Set()
    }

  
  def entitySearch(query :String, lang: String) : List[SearchResult] = {
    
    val all_results = Librarian.performSearch(query, List("person", "location"))
    return all_results
  }

  def logAnnotations = "* [onclick]" #> SHtml.ajaxInvoke(() => {logging; JsCmds.RedirectTo("/")})

  val log = LoggerFactory.getLogger("annotations")

  def logging {
    
    // Append on the user's name to the annotations and write them to a file (appending)
//    annotations.foreach(a => log.info((userName :: a ::: page_metadata).map(v => "<" + v + ">").mkString(", ")))
  }

  /*
   *  If we use this again, dont perform another search, use results from
   *
   *
   *
   */
  def listEntityResults = {
    
    // page_12357
    val page = S.param("term").open_!
    val rlist = if (page.startsWith("page_")) {
	      val pageItem = TheCart.get.findPageItem(page.slice(5, page.length).toInt).item
        val people = Librarian.library.getContents(pageItem.getId, ProteusType.PAGE, ProteusType.PERSON).get.getResultsList.asScala.toList
        val locations = Librarian.library.getContents(pageItem.getId, ProteusType.PAGE, ProteusType.LOCATION).get.getResultsList.asScala.toList
	      people ::: locations
	    } else {
	      Librarian.performSearch(page, List("person", "location"))
	    }

    rlist.foreach(r => TheCart.addItem(r))
    "#entities" #> rlist.map(ent => TheCart.get.findEntityItem((ent.getId.getIdentifier + ent.getId.getResourceId).hashCode)).map(ent =>
      "a *" #> <strong>{ent.item.getTitle}</strong> &
      "a [href]" #> Entity.getEntityLink(ent.hashCode.toString, ent.hashCode.toString) &
      "@cat *"  #> {ent.item.getProteusType.getValueDescriptor.getName.capitalize} &
      "@url *"  #> {Entity.getAdditionalInfo(ent)} &
      "@ident *" #> {ent.hashCode}
    ) &
    "@quantity" #> rlist.size

  }


  val NORMAL = 1
  val ESCAPE = 2
  val UNICODE_ESCAPE = 3

  def convertUnicodeEscape(s: String) : String = {
    var out = Array.fill[Character](s.length)(' ')

    var state = NORMAL
    var j = 0
    var k = 0
    var unicode = 0
    var c = ' '
    for (i <- 0 to s.length - 1) {
      c = s.charAt(i);
      if (state == ESCAPE) {
        if (c == 'u') {
          state = UNICODE_ESCAPE;
          unicode = 0;
        }
        else { // we don't care about other escapes
          out(j) = '\\';
          j+=1
          out(j) = c;
          j+=1
          state = NORMAL;
        }
      }
      else if (state == UNICODE_ESCAPE) {
        if ((c >= '0') && (c <= '9')) {
          unicode = (unicode << 4) + c - '0';
        }
        else if ((c >= 'a') && (c <= 'f')) {
          unicode = (unicode << 4) + 10 + c - 'a';
        }
        else if ((c >= 'A') && (c <= 'F')) {
          unicode = (unicode << 4) + 10 + c - 'A';
        }
        else {
          throw new IllegalArgumentException("Malformed unicode escape");
        }
        k+=1;

        if (k == 4) {
          out(j) = unicode.toChar;
          j+=1
          k = 0;
          state = NORMAL;
        }
      }
      else if (c == '\\') {
        state = ESCAPE;
      }
      else {
        out(j) = c;
        j+=1
      }
    }

    if (state == ESCAPE) {
      out(j) = c;
      j+=1
    }

    return new String(out.map(_.charValue));
  }

  
  def getAdditionalInfo(result: EntityItem) : String = {
    if (result.item.getProteusType.equals(ProteusType.PERSON)) {
        val person = Librarian.library.lookupPerson(result.item).get
        val birth = person.getBirthDate
        val death = person.getDeathDate
        val birthStr = if(birth != -1) java.text.DateFormat.getDateInstance(java.text.DateFormat.MEDIUM).format(new java.util.Date(birth)) else "??"
        val deathStr = if(death != -1) java.text.DateFormat.getDateInstance(java.text.DateFormat.MEDIUM).format(new java.util.Date(death)) else "??"
        return "(" + birthStr + " - " + deathStr + ")"
    } else if (result.item.getProteusType.equals(ProteusType.ORGANIZATION)) {
      println("ERROR: How'd you get that...")
      return result.item.getProteusType.getValueDescriptor.getName.capitalize
    } else if (result.item.getProteusType.equals(ProteusType.LOCATION)) {
        val location = Librarian.library.lookupLocation(result.item).get
        return "(" + location.getLongitude + ", " + location.getLatitude + ")"
    } else return result.item.getProteusType.getValueDescriptor.getName.capitalize

  }
   
  def printEntInfo = {
    // id is a Box[String] object
    // Box is full even if the string is empty ("")...
        
    info("SESSION ID: " + S.session.openOr("NONE") + ", ENTITY DETAIL")
//    search = updateSearchIndex(search, "entities")
    var id: String = S.param("e").openOr("bad entity name")
    val numID = S.param("id").openOr("0").toInt
   // val id_check: String = getEntNameFromNumID(numID)
    val histogramBuilder = new StringBuilder
    val kwicBuilder = new StringBuilder
    //println("In printEntInfo, ent id = " + id + " " + id_check)
//    if(!id.equals(id_check)) {
//      println("ERROR: Entity identifier doesn't match looked up identifier based on numID: " + id + " : " + id_check + " : " + numID)
//      id = id_check
//    }
//    val document = search.open_!.getDocument(id_check)
    val document = TheCart.entity_map.get.apply(numID)
   // val descriptor = extractEntityParts(id)

    //println("DESCRIPTOR: " + descriptor)
    
    if (isAmbiguous(id)) {

      try {
        val details = if(document.isPerson) {
	          val person = Librarian.library.lookupPerson(document.item).get
	          (person.getLanguageModel.getTermsList.asScala, person.getWikiLink)
	        } else {
	          val loc = Librarian.library.lookupLocation(document.item).get
	          (loc.getLanguageModel.getTermsList.asScala, loc.getWikiLink)
	        } //document.terms.toList
        val terms = details._1
        val wiki = details._2
        
        ".enttype" #> "Ambiguous Entity" &
        ".entterm" #>  document.item.getTitle &
        ".ambig" #> cannedAmbiguousResponse &
        ".searchAmb" #> <a href={"/search?q=entity_" + numID}>Search for this Entity</a> &
        //".desc" #> getEntityText(id) &
        //".desc" #> "No description information given..."
        //".entpic" #> <img src={getEntityImage(id)} /> &
        ".entwiki *" #> <a href={wiki}>Search wikipedia</a> &
        ".histTitle" #> "Histogram of surrounding terms:" &
        ".histogram *" #> terms.map { (B) => <tr><td>{B.getTerm}</td> <td>{B.getWeight}</td></tr>}
      } catch {
        case _: NullPointerException => {
            ".enttype" #> "Ambiguous Entity" &
            ".entterm" #> document.item.getTitle  &
            ".searchAmb" #> <a href={"/search?q=entity_" + numID}>Search for this Entity</a> &
            ".ambig" #> {cannedAmbiguousResponse}
            //".entpic" #> <img src={getEntityImage(id)} /> &
//            ".entwiki *" #> <a href={"http://en.wikipedia.org/w/index.php?search="+ getTitle(id)}>Search Wikipedia</a>
          }
      }
                       
    }
    // entity is disambiguated, display different items
    // to double quote string vars, ("\""*1)+getTitle(id)+("\""*1)
    else {
      val details = if(document.isPerson) {
	          val person = Librarian.library.lookupPerson(document.item).get
	          (person.getLanguageModel.getTermsList.asScala, person.getWikiLink)
	        } else if(document.isLocation) {
	          val loc = Librarian.library.lookupLocation(document.item).get
	          (loc.getLanguageModel.getTermsList.asScala, loc.getWikiLink)
	        } else {
	          println("This shouldn't happen: ERROR: " + document.item.getProteusType)
	          val org = Librarian.library.lookupOrganization(document.item).get
	          (org.getLanguageModel.getTermsList.asScala, org.getWikiLink)
	        }
      val terms = details._1
      val wiki = details._2
      val info = getAdditionalInfo(document)
      
//      println("ENTITY: " + getTitle(id))
      ".enttype" #> "Disambiguated Entity" &
      ".desc" #> document.item.getSummary.getText &
      ".searchWithThisEnt" #> <a href={"/search?q=entity_" + numID}>Search for this Entity</a> &
      //".findPagesWithThisEnt" #> <a href={getBooksWithEntityLink(numID)}>Show Pages mentioning this Entity</a> &
      ".entterm" #> document.item.getTitle &
      ".entpic" #> <img src={document.item.getImgUrl} /> &
      ".wikilink" #> <a href={wiki}>Wikipedia Article</a> &
      ".longlat" #> info &
      ".histTitle" #> (if (terms.isEmpty) "" else "Histogram of surrounding terms:") &
      ".histogram *" #> terms.map { (B) => <tr><td>{B.getTerm}</td> <td>{B.getWeight}</td></tr>}
    }
  }

  /**
   * Input: either [/text, CATEG], [text, CATEG], [/text_(something), CATEG], /text_more_text, /text_(something)
   * Output: boolean indicating whether or not entity is disambiguated --
   *         (starts with a "/")
   */
  def isAmbiguous(text: String) : Boolean = {
      return false
    
  }
  
  def getPageListFromEntityID(numID: String) = {
    "/dquery?term=" + ("entity_"+numID)+"&index="+S.param("index").openOr("default").toString+"&language="+S.param("language").openOr("english").toString
  }

}
