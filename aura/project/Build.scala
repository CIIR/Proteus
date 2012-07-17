import sbt._
import sbt.Keys._
import com.twitter.sbt._

object ProjectBuild extends Build {

  lazy val root = Project(
    id = "root",
    base = file("."),
    settings = Project.defaultSettings ++ CompileThriftScrooge.newSettings ++ Seq(
      name := "Aura",
      organization := "edu.umass.ciir",
      version := "0.1",
      scalaVersion := "2.9.1",

      resolvers += "twitter" at "http://maven.twttr.com/",

      libraryDependencies ++= {
        val finagleVer = "3.0.0"
        Seq(
          "org.apache.thrift" % "libthrift" % "0.5.0",
          "com.twitter" %% "finagle-core" % finagleVer,
          "com.twitter" %% "finagle-thrift" % finagleVer,
          "com.twitter" %% "finagle-ostrich4" % finagleVer,
          "com.twitter" %% "scrooge-runtime" % "1.1.3"
        )
      },
      
      CompileThriftScrooge.scroogeVersion := "2.5.4"
    )
  )
}