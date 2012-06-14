

import java.util.LinkedList
import java.util.Queue

import java.io.File
import java.io.FileInputStream
import java.io.FilenameFilter
import java.io.InputStream
import java.io.PrintWriter
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.io.FileOutputStream
import java.util.Arrays
import java.util.HashMap
import java.util.HashSet
import java.util.regex.Pattern
import java.util.regex.Matcher
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import collection.JavaConversions._
import cc.refectorie.user.dietz.tacco.data.WikipediaEntity

case class EntityType(name: String) { override def toString : String = name }
case object PERSON extends EntityType("person")
case object LOCATION extends EntityType("location")
case object ORGANIZATION extends EntityType("organization")

object TEIPatterns {
    val person_pattern = Pattern.compile("(.*)</person>") //<person name=\"([^>.]*)\">(.*)</person>(.*)")
    val location_pattern = Pattern.compile("(.*)</location>") //<location name=\"([^>.]*)\">(.*)</location>(.*)")
    val organization_pattern = Pattern.compile("(.*)</organization>") //<organization name=\"([^>.]*)\">(.*)</organization>(.*)")
    val tag_pattern = Pattern.compile("<[^>]*>|[.,-_!~;\"*']|&quot|&amp|&apos|&gt|&lt|^")

val stopwords : Set[String] = try {
	  io.Source.fromInputStream(getClass().getResourceAsStream("/stopwords/inquery")).getLines().toSet

    } catch {
      case _ =>
      println(" Oh well")
      Set()
    }
}



import scala.collection.Iterator

class TEIEntityIterator(input_filename: String) extends Iterator[EntityTag] {
	val input = io.Source.fromInputStream(new GZIPInputStream(new FileInputStream(input_filename)))
	val entity_pattern = Pattern.compile("<(person|location|organization) name=\"([^>]*)\"")
	var current_entity_type:EntityType = null
	var current_entity_name: String = ""
	var previous_tag: EntityTag = null


	def takeToEntity : (String, EntityType, String) = {
		var buffer = ""
		var found_name:String = ""
		var found_type:EntityType = null
		while (!input.isEmpty && found_type == null) {
			val (chunk, ftype, fname) = takeStep
			found_type = ftype
			found_name = fname
			buffer = buffer + chunk
		}
		return (buffer, found_type, found_name)
	}

	def takeStep : (String,EntityType,String) = {
		if (input.isEmpty)
			return ("", null, null)

		val prefix = input.takeWhile( _ != '<').mkString

		if (input.isEmpty)
			return (prefix, null, null)

		val tagid = input.ch + input.takeWhile( _ != '>').mkString
		// Now check if the tagid is an entity
		val matcher = entity_pattern.matcher(tagid)
		if (matcher.find) {
			// Matched, we have an entity...
			val entity_type = matcher.group(1) match {
				case "person" => PERSON
				case "location" => LOCATION
				case "organization" => ORGANIZATION
			}

			return (prefix, entity_type, matcher.group(2))
		} 
		
		return (prefix + tagid + input.ch, null, null)
	}

	def next: EntityTag = {
		val (data, ent_type, ent_name) = takeToEntity
		if (ent_type != null) {
			val prevText = if(previous_tag == null) "" else previous_tag.cleanFollowText
			val entity = new EntityTag(prevText, current_entity_type, current_entity_name, data)
			previous_tag = entity
			current_entity_type = ent_type
			current_entity_name = ent_name
			return entity 
		} else {
		  val entity = new EntityTag("", current_entity_type, current_entity_name, data)
		  return entity
		}
	}

	def hasNext: Boolean = !input.isEmpty
    
}


class EntityTag(prevTagText: String, ent_type: EntityType, surface_form: String, textBlock:String, context_size:Int = 6) {
	
	val matcher = ent_type match {
		case PERSON => TEIPatterns.person_pattern.matcher(textBlock)
		case LOCATION => TEIPatterns.location_pattern.matcher(textBlock)
		case ORGANIZATION => TEIPatterns.organization_pattern.matcher(textBlock)
		case null => null
		}

	if(matcher != null && !matcher.find)
		System.err.println("Error: Malformed textblock for creating Entity Tag: " + ent_type + " " + textBlock + "\n\n")
	val tag_text = if(matcher == null) "" else matcher.group(1) // The text inside the tag matching group
	val follow_text = if(matcher == null) textBlock else {
	    val endtag = "</" + ent_type + ">"
	    textBlock.slice(textBlock.indexOf(endtag) + endtag.length, textBlock.length) // The text after the tag.
	    }
	// Don't include . in [] it thinks its a period
	val surrounding_text = TEIPatterns.tag_pattern.matcher(prevTagText.toLowerCase).replaceAll("") + cleanFollowText
	var identifier = ""

	lazy val cleanFollowText : String = TEIPatterns.tag_pattern.matcher(follow_text.toLowerCase).replaceAll("")
	def setAnnotation(wiki: WikipediaEntity) {
	    if (wiki != null) {
	      identifier = wiki.wikipediaTitle
	      // TODO: use wiki metadata to figure out what entity type to use
	    }
	}
	def getIdentifier : String = if (identifier != "") identifier else surface_form
	def getName : String = surface_form
	lazy val contextTerms: Seq[String] = {
	    val terms = surrounding_text.split("\\s").distinct.filter(t => t.length > 1 && !TEIPatterns.stopwords.contains(t))
	    val index = ((terms.length - context_size)/2).toInt
	    terms.distinct.filter(t => !TEIPatterns.stopwords.contains(t)).drop(index).dropRight(index).toSeq
	}

	def getTagText : String = "<" + ent_type + " name=\"" + getIdentifier + "\">" + tag_text + "</" + ent_type + ">"
	def getTEIText : String = if(ent_type != null) getTagText + follow_text else follow_text
	override def toString : String = getTagText
	def getEntityType: EntityType = ent_type
	lazy val queryId = surrounding_text.hashCode.toString

	def toQuery(bookId: String): BookELQuery = {
          return BookELQueryImpl(docId = bookId, enttype = ent_type.toString, queryId = queryId, name = surface_form.toLowerCase, contextTerms = contextTerms)
	}

    }

object TEIAnnotator {

       def main(args: Array[String]) {
       	   if (args.length < 2) {
	      println("Usage: scala TEIAnnotator inputFile outputFile")
	      exit
	   }

       	   println("Annotating TEI file " + args(0) + ", new file saving to " + args(1))
	   annotate(args(0), args(1), 10, 20)
       }

def processEntities(entities: Queue[EntityTag], bookId:String) : Queue[EntityTag] = {
	  val activeQueries = new LinkedList[BookELQuery]()
	  val queryMaps = new LinkedList[Int]()
	  for(current <- entities) {
	      val existing_index = activeQueries.indexWhere(c => c.name.equals(current.getName.toLowerCase))
	      if(existing_index >= 0) // Could also merge context terms here
	          queryMaps.add(existing_index)
	      else {
		  queryMaps.add(activeQueries.size)
	          activeQueries.add(current.toQuery(bookId))
	      }
	  }
          
          println("Linking entities... " + activeQueries)
	  val linker = (new BooksEntityLinking(activeQueries.toSeq)).predict.toList
	  entities.zip(queryMaps).foreach( entindx => entindx._1.setAnnotation(linker(entindx._2)))
          println("Done linking... " + queryMaps.size + " : " + linker.length)
	  entities
}

def annotate(input_file: String, output_file: String, context_size: Int, buffer_size: Int) {
      val entity_iterator = new TEIEntityIterator(input_file)
      val bookId = input_file.split("/").last.split("_")(0)
    // Maintain two fixed length queues: 
    // Context queue (already written to disk used for entity linking based context queries), 
    // Active queue awaiting being written out to disk. Also provides context for entity linking but in the forward direction.

      val context:Queue[EntityTag] = new LinkedList[EntityTag]()
      val active:Queue[EntityTag] = new LinkedList[EntityTag]()

      val output = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(output_file)))))
      for ( tag <- entity_iterator ) {
      	  active.add(tag)
	  if (active.size > context_size + buffer_size) {
	    val entity_list:Queue[EntityTag] = new LinkedList[EntityTag]()
	    while(active.size >= context_size) {
	       // Poll off the active queue, link with context queue, write out, and add to context queue
	       val current = active.poll 
	       // Could use the context queue here as a list to add related entities
	       // ...
	       if (current.getEntityType == null)
		 output.write(current.getTEIText)
	       else {
		 entity_list.add(current)
	       }
	    }
	    // Write out and add to context queue
	    processEntities(entity_list, bookId).foreach( ent => { output.write(ent.getTEIText); context.add(ent) } )
          }
	  // Maintain context queue
	  while (context.size >= context_size) 
	  	context.poll
     }      
      
      if (!active.isEmpty) {
	processEntities(active, bookId).foreach( ent => output.write(ent.getTEIText) )
      }

      output.close
   }

}
