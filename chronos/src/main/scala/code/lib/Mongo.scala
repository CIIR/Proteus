package code.lib


import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.global._
import com.mongodb.casbah.MongoConnection
import com.mongodb.casbah.Imports._

case class VObject(verb: String, date: Int, pos: String, tups: scala.collection.mutable.Map[String, Int])

object Mongo {

	val mongoConn = MongoConnection("armadale.cs.umass.edu")
	val coll = mongoConn("verbs")("counts5k")
	val coll2 = mongoConn("verbs")("counts5k_np")
	
	def find(verb: String): List[VObject] = {
		val q = MongoDBObject("verb" -> verb)
		val cursor = coll2.find(q)
		var vbo: VObject = null
		var listVbo = List[VObject]()
		for (dbo <- cursor) {
			vbo = grater[VObject].asObject(dbo)
			listVbo = vbo :: listVbo
		}
		return listVbo
	}
	
	def find(verb: String, pos: String): List[VObject]= {
		val q = MongoDBObject("verb" -> verb, "pos" -> pos)
		val cursor = coll2.find(q)
		var vbo: VObject = null
		var listVbo = List[VObject]()
		for (dbo <- cursor) {
			vbo = grater[VObject].asObject(dbo)
			listVbo = vbo :: listVbo
		}
		return listVbo
	}
}