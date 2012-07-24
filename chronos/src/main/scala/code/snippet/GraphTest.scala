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

class GraphTest {

    def buttonClick = {
        "* [onclick]" #> SHtml.ajaxCall(ValById("the_input"), query => (SetHtml("", graph(query, "container1"))))
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

    def graph(query: String, container: String) = {

        val chart = new Chart(defaultSeriesType = SeriesType.Line) //zoomType = ZoomType.X)
        val title = new Title(text = "Test chart: " + query)
        val xAxis = new Axis(axisType = AxisType.Datetime)
        val yAxis = new Axis(title = new Title(text = "frequency"), min = 0)

        val data = List((1900, 4),(1901, 7),(1902, 5),(1903, 1),(1904, 0),(1905, 0),(1906, 5),(1907, 9),(1908, 1),(1909, 10))
        
        var seriesList = List[Series[DateNumericValue]]()
        
        
        var norm = List[DateNumericValue]()
        for (d <- data) {
            val date = new DateTime(d._1.toInt,2,20,13,0,0,0)
            val point = DateNumericValue(date, d._2)
            norm = point :: norm
        }
        var datas_norm = Series[DateNumericValue](name = "raw", data = norm)
        seriesList = datas_norm :: seriesList
        
        var normsmooth = List[DateNumericValue]()
        var temp1 = relativeFrequency(data.map(i => (i._1.toDouble,i._2.toDouble))) // normalize first
        var unzkeys1 = temp1.unzip._1  // dates
        var unzvals1 = temp1.unzip._2  // counts
        var temp2 = unzkeys1.zip(simpleMovingAverage(unzvals1, 3)) // then smooth       
        for (d <- temp2) {
            val date = new DateTime(d._1.toInt,2,20,13,0,0,0)
            val point = DateNumericValue(date, d._2)
            normsmooth = point :: normsmooth
        }
        var datas_normsmooth = Series[DateNumericValue](name = "Norm then Smooth", data = normsmooth)
        seriesList = datas_normsmooth :: seriesList
        
        
        
        var smoothnorm = List[DateNumericValue]()
        var unzkeys2 = data.unzip._1.map(i => i.toDouble) // dates
        var unzvals2 = data.unzip._2.map(i => i.toDouble)  // counts
        var temp3 = simpleMovingAverage(unzvals2, 3) 
        var temp4 = relativeFrequency(unzkeys2.zip(temp3))
  
        for (d <- temp4) {
            val date = new DateTime(d._1.toInt,2,20,13,0,0,0)
            val point = DateNumericValue(date, d._2)
            smoothnorm = point :: smoothnorm
        }
        var datas_smoothnorm = Series[DateNumericValue](name = "Smooth then Norm", data = smoothnorm)
        seriesList = datas_smoothnorm :: seriesList
        
        
        var highChart = new HighChart(chart = chart,
            title = title,
            xAxis = Seq(xAxis),
            yAxis = Seq(yAxis),
            series = seriesList)

        val serializedChart = highChart.build(container)
        boilerplate("testing", serializedChart)
    }

    def relativeFrequency(data: List[(Double, Double)]) = {
        var sum = data.map(t => t._2).reduceLeft(_ + _)
        data.map(d => (d._1, d._2 / sum))
    }

    def simpleMovingAverage(values: List[Double], period: Int): List[Double] = {
        (for (i <- 1 to values.length)
            yield if (i < period) 0.00
        else values.slice(i - period, i).reduceLeft(_ + _) / period).toList
    }

}