package ciir.proteus.entitylinking

import java.io._
import java.util.Properties
import scala.collection.JavaConversions._

object PharosProperties {

  var conf : Properties = null
  //val defaultPropertiesFile = "./config/trec-pharos.properties"
  //loadProperties(defaultPropertiesFile)
  
  def galagoJsonParameterFile = conf.getProperty("pharos.galagoJsonParameterFile")
  def useLocalIndex = conf.getProperty("pharos.useLocalIndex").toBoolean
  def galagoSrv = conf.getProperty("pharos.galagoSrv")
  def galagoKbaPort = conf.getProperty("pharos.galagoKbaPort")
  def numberOfRequestedResults = conf.getProperty("pharos.numberOfRequestedResults").toInt
  def numberFirstPassExpansionRMTerms = conf.getProperty("pharos.numberFirstPassExpansionRMTerms").toInt
  def numberSecondPassExpansionRMTerms = conf.getProperty("pharos.numberSecondPassExpansionRMTerms").toInt
  def useTwoPassWorkingSet = conf.getProperty("pharos.useTwoPassWorkingSet").toBoolean
  def useTaccoQuery = conf.getProperty("pharos.useTaccoQuery").toBoolean
  def rankerModelFile = conf.getProperty("pharos.rankerModelFile", "./ltr/ranklib1.model")
  def performNilPrediction = conf.getProperty("pharos.performNilPrediction", "false").toBoolean
  
  def loadProperties(propertiesFile : String) = {
     println("trying to load pharos properties from: " + propertiesFile)
     try {
       val properties = System.getProperties()
       val propStream = new FileInputStream(propertiesFile)
       properties.load(propStream)
       System.out.println("...loaded from "+propertiesFile)
       
       conf = properties
     } catch {
       case e => println("Unable to load file : " + propertiesFile + " " + e.getMessage())
       throw e
     }
  }
  
  def printProperties() {
    println("TrecKbaProperties:")
    
    val kbaProperties = conf.keys().toList.filterNot(e => (e.toString().startsWith("pharos")))
    for ( key <- conf.keys()) {
      if (key.toString().startsWith("pharos.")) {
          println(key + ":" + conf.getProperty(key.toString))
      }
    }
  }
}