package code.snippet

import scala.xml.NodeSeq
import net.liftweb.util._
import Helpers._
import net.liftweb.http._
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JE._

class RegistrationController {
	
	private val whence = S.referer openOr "/"
	
	def render = {
		"type=submit" #> SHtml.submit("Register", process,
				"onclick" -> JsIf(JsEq(ValById("first_name"),""), Alert("alert") &
						JsReturn(false)).toJsCmd)
		"type=button" #> SHtml.ajaxButton("Populate form", () => SetValById("first_name","John"))
	}
	
	private def process() = {
		S.redirectTo(whence)
	}

}