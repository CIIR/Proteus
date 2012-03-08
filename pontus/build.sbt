name := "pontus"
 
version := "0.1"

scalaVersion := "2.9.1" 

seq(webSettings: _*)

// If using JRebel with 0.1.0 of the sbt web plugin
//jettyScanDirs := Nil
// using 0.2.4+ of the sbt web plugin
scanDirectories in Compile := Nil

//resolvers += "Java.net Maven2 Repository" at "http://download.java.net/maven/2/"

// you can also add multiple repositories at the same time
resolvers ++= Seq(
  "Scala Tools Snapshot" at "http://scala-tools.org/repo-releases/",
  "Scala Tools Snapshot" at "http://scala-tools.org/repo-snapshots/",
  "Java.net Maven2 Repository" at "http://download.java.net/maven/2/",
  "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
  "Akka Repo" at "http://akka.io/repository",
  "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository"
)

// if you have issues pulling dependencies from the scala-tools repositories (checksums don't match), you can disable checksums
//checksums := Nil

libraryDependencies ++= {
  val liftVersion = "2.4-M4" // Put the current/latest lift version here
  Seq(
    "net.liftweb" %% "lift-webkit" % liftVersion % "compile->default",
    "net.liftweb" %% "lift-mapper" % liftVersion % "compile->default",
    "net.liftweb" %% "lift-wizard" % liftVersion % "compile->default")
}



// when using the sbt web app plugin 0.2.4+, use "container" instead of "jetty" for the context
// Customize any further dependencies as desired
libraryDependencies ++= Seq(
  "edu.umass.ciir.proteus" %% "triton-core" % "0.1" % "compile",
//  "se.scalablesolutions.akka" % "akka-actor" % "1.2" % "compile",
//  "se.scalablesolutions.akka" % "akka-slf4j" % "1.2",
//  "se.scalablesolutions.akka" % "akka-remote" % "1.2",
//  "se.scalablesolutions.akka" % "akka-camel" % "1.2",
//  "se.scalablesolutions.akka" % "akka-typed-actor" % "1.2",
//  "se.scalablesolutions.akka" % "akka-stm" % "1.2",
//  "se.scalablesolutions.akka" % "akka-camel-typed" % "1.2",
//  "cc.spray" % "spray-server" % "0.8.0-RC2" % "compile",
//  "cc.spray.can" % "spray-can" % "0.9.+" % "compile",
//  "cc.spray.json" %% "spray-json" % "1.0.1" % "compile",
//  "org.specs2" %% "specs2" % "1.6.1" % "test",
  "org.slf4j" % "slf4j-api" % "1.6.1",
//  "ch.qos.logback" % "logback-classic" % "0.9.29",
  "net.liftweb" %% "lift-json" % "2.4-M4",
//  "org.scalatra" %% "scalatra" % "2.0.2",
//  "org.scalatra" %% "scalatra-scalate" % "2.0.2",
//  "org.scalatra" %% "scalatra-specs2" % "2.0.2" % "test",
  "ch.qos.logback" % "logback-classic" % "1.0.0" % "runtime",
  "javax.servlet" % "servlet-api" % "2.5" % "provided",
  "org.eclipse.jetty" % "jetty-webapp" % "8.0.4.v20111024" % "container", // For Jetty 8
  "org.scala-tools.testing" % "specs_2.9.0" % "1.6.8" % "test", // For specs.org tests
  "junit" % "junit" % "4.8" % "test->default", // For JUnit 4 testing
  "javax.servlet" % "servlet-api" % "2.5" % "provided->default",
  "com.h2database" % "h2" % "1.2.138", // In-process database, useful for development systems
  "ch.qos.logback" % "logback-classic" % "0.9.26" % "compile->default" // Logging
)
