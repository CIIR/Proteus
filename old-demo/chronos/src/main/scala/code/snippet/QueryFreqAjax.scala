package code.snippet

import scala.collection.JavaConversions._
import scala.xml.NodeSeq
import code.lib.Galago
import nl.tecon.highcharts.HighChart
import nl.tecon.highcharts.config._
import nl.tecon.highcharts.config.Conversions._
import scala.xml.NodeSeq
import net.liftweb._
import util.Helpers._
import http._
import js._
import JsCmds._
import JE._
import scala.collection.mutable
import scala.collection.immutable.TreeMap
import org.joda.time
import scala.util.control.Breaks._
import java.util.regex.Pattern
import org.tartarus.snowball.ext.englishStemmer
import org.joda.time.DateTime
import net.liftweb.common.Full
import org.lemurproject.galago.core.tools.Search.SearchResultItem
import ciir.models.{ LanguageModel, StopWordList, TermEntry }

class QueryFreqAjax {

  val stemmer = new englishStemmer()
  val datePattern = Pattern.compile("<(date)>(.*?)</date>")
  val yearPattern = Pattern.compile("[0-9]{4}")
  val datepattern = """<(date)>(.*?)</date>""".r
  val yearpattern = """[0-9]{4}""".r
  val LENGTH = 1
  var m_query: String = _
  var filterStopwords = true
  var m_masterModel: LanguageModel = _
  var m_masterModelStemmed: LanguageModel = _
  var m_docModelsStemmed: mutable.HashMap[Int, LanguageModel] = new mutable.HashMap[Int, LanguageModel]
  var m_docModels: mutable.HashMap[Int, LanguageModel] = new mutable.HashMap[Int, LanguageModel]
  var m_docs: List[org.lemurproject.galago.core.parse.Document] = Nil

  // 	  testing StopWordList singleton from boot()
  //    println("is week a stopword?")
  //    println(StopWordList.isStopWord("week"))

  def clickButton = {
    "#butt [onclick]" #> SHtml.ajaxCall(ValById("query_input"), query =>
      (SetHtml("", renderQueryGraph(query, "query_container")) //&
      //SetHtml("", renderBurstyGraph("bursty_container"))  
      ))
  }

  private def boilerplate(varname: String, graph: String) =
    {
      JsCmds.Script(JsRaw(
        """var """ + varname + """;""" +
          """$(document).ready(function() {""" +
          varname + """ = new Highcharts.Chart({""" +
          graph +
          """}); })"""))
    }

  private def searchFetchDocs(query: String) = {
    println("searching and fetching")
    m_query = query
    var resultItems = Galago.search(m_query, 1000L)
    m_docs = resultItems.map(ri => Galago.getDocument(ri.identifier))
    println("finished mapping docs")
  }

  private def renderQueryGraph(query: String, container: String) = {
    searchFetchDocs(query)
    //var result = queryAndParse(m_query)

    // collect data for graph
    println("rendering query graph")
    var timeseries_raw = getRawTimeseries()
    var timeseries_sma = simpleMovingAverage(timeseries_raw, 3)

    // main graph setup
    val chart = new Chart(defaultSeriesType = SeriesType.Line)
    val title = new Title(text = "Raw term frequencies for: " + m_query)
    val yAxis = new Axis(title = new Title(text = "frequency"), min = 0)
    val xAxis = new Axis(axisType = AxisType.Datetime)
    var seriesList = List[Series[DateNumericValue]]()

    // raw data
    var data = List[DateNumericValue]()
    for (d <- timeseries_raw) {
      val date = new DateTime(d._1, 2, 20, 13, 0, 0, 0)
      val point = DateNumericValue(date, d._2)
      data = point :: data
    }
    var datas = Series[DateNumericValue](name = m_query, data = data)
    seriesList = datas :: seriesList

    // moving average applied
    var data_sma = List[DateNumericValue]()
    for (datum <- timeseries_sma) {
      val date = new DateTime(datum._1, 2, 20, 13, 0, 0, 0)
      val point = DateNumericValue(date, datum._2)
      data_sma = point :: data_sma
    }
    var datas_sma = Series[DateNumericValue](name = m_query + " simple moving average", data = data_sma)
    seriesList = datas_sma :: seriesList

    var highChart = new HighChart(chart = chart,
      title = title,
      xAxis = Seq(xAxis),
      yAxis = Seq(yAxis),
      series = seriesList)
    val serializedChart = highChart.build(container)

    boilerplate(container, serializedChart)
  }

  private def renderBurstyGraph(container: String) = {

    // collect data for graph
  	// (year, term, score)
    var burstiness = topKBurstyTerms(5).map(entry =>
    	entry._2.map(term => (entry._1, term.getTerm, term.getProbability))).flatten.groupBy(_._2)

    // main graph setup
    val chart = new Chart(defaultSeriesType = SeriesType.Line)
    val title = new Title(text = "Burstiness for: " + m_query)
    val yAxis = new Axis(title = new Title(text = "burstiness"))
    val xAxis = new Axis(axisType = AxisType.Datetime)
    var seriesList = List[Series[DateNumericValue]]()

    // burstiness data formatting
    var data = List[DateNumericValue]()
    for (d <- burstiness) {
      val date = new DateTime(d._1, 2, 20, 13, 0, 0, 0)
      val point = DateNumericValue(date, d._2)
      data = point :: data
    }
    var datas = Series[DateNumericValue](name = m_query, data = data)
    seriesList = datas :: seriesList

    var highChart = new HighChart(chart = chart,
      title = title,
      xAxis = Seq(xAxis),
      yAxis = Seq(yAxis),
      series = seriesList)
    val serializedChart = highChart.build(container)

    boilerplate(container, serializedChart)
  }

  case class BurstEntry(word: String, score: Double)
  
  private def topKBurstyTerms(k: Int) = {

    // take some threshold, plot over time
    // what about terms that don't occur in consecutive years?
	generateTemporalLMs()
    m_masterModel.calculateProbabilities()
    m_docModels foreach { 
      case (year, langmod) => langmod.calculateProbabilities()
    }
    var burstDocModels = m_docModels.clone()
    var yearBurstEntries = new mutable.HashMap[Int, List[TermEntry]]

    var masterProb = 0.0
    var burstiness = 0.0
    
    // calculate burstiness for all terms
    burstDocModels map {
      case (year, langmod) => {
        langmod.getEntries map {
          case term: TermEntry => {
            masterProb = m_masterModel.getTermEntry(term.getTerm).getProbability
            burstiness = term.getProbability / masterProb
            term.setProbability(burstiness)
          }
        }
      }
    }
    
    // sort the term entries by their burstiness score and take the top 100
    burstDocModels foreach { 
    	case (year, langmod) => {
    		var sortedBurstiness = langmod.getEntries.toList.sortBy(- _.getProbability()) take k
    		yearBurstEntries.put(year, sortedBurstiness)
    	}
    }
    yearBurstEntries
  }

  private def getRawTimeseries() = {
    var dateCounts = new mutable.HashMap[Int, Int]
    m_docs foreach { doc =>
      getDate(doc.text) match {
        case date: Option[Int] => {
          doc.terms foreach { term =>
            if (stem(massageText(term)).equals(stem(m_query))) {
              var increment = dateCounts.getOrElseUpdate(date.get, 0) + 1
              dateCounts.put(date.get, increment)
            }
          }
        }
        case _ => println("no date for: " + doc.identifier)
      }
    }
    // series data sorted by year ascending
    TreeMap(dateCounts.toSeq: _*)
  }

  private def massageText(str: String) = {
    str.toLowerCase().replaceAll("[^A-Za-z0-9]", "")
  }

  private def generateTemporalLMs() = {
    m_masterModelStemmed = new LanguageModel(LENGTH)
    m_masterModel = new LanguageModel(LENGTH)
    m_docModelsStemmed.clear
    m_docModels.clear
    m_docs foreach { doc =>
      getDate(doc.text) match {
        case date: Option[Int] => {
          var stemmed = doc.terms.map(t => stem(t)).toArray
          m_masterModelStemmed.addDocument(stemmed, true)
          m_docModelsStemmed.getOrElseUpdate(date.get, new LanguageModel(LENGTH)).addDocument(stemmed, true)
          m_masterModel.addDocument(doc, true)
          m_docModels.getOrElseUpdate(date.get, new LanguageModel(LENGTH)).addDocument(doc, true)
        }
        case _ => // log file
      }
    }
  }

  // ideally log this info along with id identifier
  private def getDate(meta: String) = {
    datepattern.findFirstIn(meta) match {
      case None => println("got a none when seeking date tag, skipping")
      case date: Option[String] => {
        yearpattern.findFirstIn(date.get) match {
          case None => println("got a none when parsing out date, skipping")
          case year: Option[String] => Some(year.get.toInt)
          case _ => println("bad date format in date tag, skipping")
        }
      }
      case _ => println("no date xml tag found")
    }
  }

  /**
   * copied from galago.core.parse.stem.Porter2Stemmer
   */
  private def stem(term: String): String = {
    var stem = term
    stemmer.setCurrent(term)
    if (stemmer.stem())
      stem = stemmer.getCurrent()
    return stem
  }

  /**
   * This takes a raw time series and applies a moving average based on n preivious data points
   */
  private def simpleMovingAverage(timeseries: Map[Int, Int], period: Int): Map[Int, Double] = {
    val kv = timeseries.unzip
    val keys = kv._1
    val values = kv._2.toList
    val smoothed = {
      (for (i <- 1 to values.length)
        yield if (i < period) 0.0
      else values.slice(i - period, i).reduceLeft(_ + _) / period).toList
    }
    return keys.zip(smoothed).toMap
  }
}