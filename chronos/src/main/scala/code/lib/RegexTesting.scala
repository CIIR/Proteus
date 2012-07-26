package code.lib

object RegexTesting {

	
	def main(args: Array[String]) = {
		
		
		val datepattern = """<(date)>(.*?)</date>""".r
		val yearpattern = """[0-9]{4}""".r
		val text = """<html><body><date>1901907</date></body></html>"""
			
		datepattern.findFirstIn(text) match {
			case date:Option[String] => { 
				yearpattern.findFirstIn(date.get) match {
					case year:Option[String] => println(year.get.toInt)
					case _ => println("bad date format in date tag")
				}
			}
			case _ => println("no date xml tag found")
		}
		
		
		
	}
	
}