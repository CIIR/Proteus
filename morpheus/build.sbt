organization := "edu.umass.ciir"

name := "morpheus"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.9.1"

seq(webSettings :_*)

resolvers ++= Seq("Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
	  "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository",
	  "Local Ivy Repository" at "file://" + Path.userHome.absolutePath + "/.ivy2/local"
	  )

libraryDependencies ++= Seq(
  "org.scalatra" %% "scalatra" % "2.0.4",
  "org.scalatra" %% "scalatra-scalate" % "2.0.4",
  "org.scalatra" %% "scalatra-specs2" % "2.0.4" % "test",
  "ch.qos.logback" % "logback-classic" % "1.0.0" % "runtime",
  "org.eclipse.jetty" % "jetty-webapp" % "7.6.0.v20120127" % "container",
  "javax.servlet" % "servlet-api" % "2.5" % "provided",
  "edu.umass.ciir" %% "aura" % "0.1" 
)


