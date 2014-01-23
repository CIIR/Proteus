package code.snippet

import code.lib.Galago
import code.lib.Mongo
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
import scala.xml.Text
import org.joda.time.DateTime
import scala.collection.mutable.ListBuffer

class VerbSubjObj {

  def buttonClick = {
    "* [onclick]" #> SHtml.ajaxCall(ValById("the_input"), verb =>
      (SetHtml("", renderVerbGraph(verb, "subj", "container1")) &
        SetHtml("", renderVerbGraph(verb, "obj", "container2"))
        ))
  }

  def boilerplate(varname: String, graph: String) =
    {
      JsCmds.Script(JsRaw(
        """var """ + varname + """;""" +
          """$(document).ready(function() {""" +
          varname + """ = new Highcharts.Chart({""" +
          graph +
          """}); })"""))
    }

  def renderVerbGraph(query: String, pos: String, container: String) = {
    var n = 0
    var which = if (pos == "subj") "subjects" else "objects"
    val chart = new Chart(defaultSeriesType = SeriesType.Line) //zoomType = ZoomType.X)
    val title = new Title(text = "Top " + which + " for verb: " + query)
    val yAxis = new Axis(title = new Title(text = "frequency"),
      maxZoom = 31556926 * 5,
      min = 0)

    val verbResults = Mongo.find(query, pos)
    val relevantVerbs = verbResults.map(vob => ((vob.date, vob.pos, vob.verb), vob.tups.toList))

    val sortedYearToWordCount = relevantVerbs.map(d => (d._1._1, d._2)).filter(d => d._1 > 1000).sortBy(_._1)
    // ("subject" -> Array( ("subject",(year, count)), ... )
    val wordToYearCount = sortedYearToWordCount.map(d => d._2.map(l => (l._1, (d._1, l._2)))).flatMap { _.toList } groupBy (_._1)
    val yearToWords = sortedYearToWordCount.map(i => (i._1, i._2.map(j => (j._1))))
    val wordsToYear = yearToWords.map(i => i._2.map(j => (j, i._1))).flatten.groupBy(_._1)

    val wordsAcrossNyears = {
      if (pos == "subj") n = 15 else n = 15
      wordsToYear.values.filter(arr => arr.size > n).flatten.map(t => t._1).toList.distinct
    }

    var mymap = Map[String, List[(Double, Double)]]()
    for (word <- wordsAcrossNyears) {
      val some = wordToYearCount(word).map(t => (t._2._1.toDouble, t._2._2.toDouble))
      val smoothedNormalized = smoothNormalize(some)
      mymap += word -> smoothedNormalized
    }

    var seriesList = List[Series[DateNumericValue]]()

    for (word <- wordsAcrossNyears) {
      var worddata = List[DateNumericValue]()
      for (datum <- mymap(word)) {
        val date = new DateTime(datum._1.toInt, 2, 20, 13, 0, 0, 0)
        val point = DateNumericValue(date, datum._2)
        worddata = point :: worddata
      }
      var datas = Series[DateNumericValue](name = word, data = worddata)
      seriesList = datas :: seriesList
    }

    val xAxis = new Axis(axisType = AxisType.Datetime)

    var highChart = new HighChart(chart = chart,
      title = title,
      xAxis = Seq(xAxis),
      yAxis = Seq(yAxis),
      series = seriesList)

    val serializedChart = highChart.build(container)
    boilerplate(container, serializedChart)
  }

  private def smoothNormalize(data: List[(Double, Double)]) = {
    var unz_keys = data.unzip._1
    var unz_vals = data.unzip._2
    var temp1 = centralMovingAverage(unz_vals, 6)
    var temp2 = relativeFrequency(unz_keys.zip(temp1))
    temp2
  }

  private def relativeFrequency(data: List[(Double, Double)]) = {
    var sum = data.map(t => t._2).reduceLeft(_ + _)
    data.map(d => (d._1, d._2 / sum))
  }

  private def simpleMovingAverage(values: List[Double], period: Int): List[Double] = {
    (for (i <- 1 to values.length)
      yield if (i < period) 0.00
    else values.slice(i - period, i).reduceLeft(_ + _) / period).toList
  }
  
  private def centralMovingAverage(values: List[Double], period: Int): List[Double] = {
    var avgList = ListBuffer[Double]()
    var num = period/2
    var iter = values.sliding(period)
    avgList.appendAll(values.take(num))
    while (iter.hasNext) {
      avgList.append(iter.next.reduceLeft(_+_) / period)
    }
    avgList.appendAll(values.takeRight(num))
    avgList.toList
   }

}