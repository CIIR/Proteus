name := "my-lift-bootstrap"

organization := "ciir.umass"

version := "0.1"

scalaVersion := "2.9.1"

seq(webSettings: _*)

port in container.Configuration := 8082

scalacOptions ++= Seq("-deprecation")

compileOrder := CompileOrder.JavaThenScala

seq(site.settings:_*)

seq(ghpages.settings:_*)

git.remoteRepo := "git@github.com:fbettag/lift-bootstrap.git"


// If using JRebel with 0.1.0 of the sbt web plugin
//jettyScanDirs := Nil
// using 0.2.4+ of the sbt web plugin
scanDirectories in Compile := Nil

resolvers ++= Seq(
  "Spray Json Repo" at "http://repo.spray.cc/",
  "novusRels" at "http://repo.novus.com/releases/",
  "novusSnaps" at "http://repo.novus.com/snapshots/",
  "Local Maven Repository" at "file://"+Path.userHome+"/.m2/repository",
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/snapshots/",
  "Repo Maven" at "http://repo1.maven.org/maven2/",
  "Scala Tools Releases" at "http://scala-tools.org/repo-releases/",
  "Scala Tools Snapshot" at "http://scala-tools.org/repo-snapshots/",
  "Nexus Releases" at "http://nexus.scala-tools.org/content/repositories/releases",
  "Nexus Snapshots" at "http://nexus.scala-tools.org/content/repositories/snapshots",
  "Java.net Maven2 Repository" at "http://download.java.net/maven/2/",
  "Bryan J Swift Repository" at "http://repos.bryanjswift.com/maven2/"
)

// if you have issues pulling dependencies from the scala-tools repositories (checksums don't match), you can disable checksums
//checksums := Nil

libraryDependencies ++= {
  val liftVersion = "2.5-SNAPSHOT"
  Seq(
    "net.liftweb" %% "lift-widgets" % liftVersion % "compile->default",
    "net.liftweb" %% "lift-webkit" % liftVersion % "compile->default",
    "net.liftweb" %% "lift-mapper" % liftVersion % "compile->default",
    "net.liftweb" %% "lift-json" % liftVersion % "compile->default")
}

libraryDependencies ++= Seq(
  "ag.bett.lift" %% "bhtml" % "0.1",
  "postgresql" % "postgresql" % "9.1-901.jdbc4",
  "net.databinder" %% "dispatch-http" % "0.8.6"
)

libraryDependencies ++= Seq(
  "nl.tecon.scalahighcharts" % "highcharts" % "1.1-SNAPSHOT",
  "com.novus" %% "salat-core" % "0.0.8-SNAPSHOT",
  "com.mongodb.casbah" %% "casbah" % "2.1.5-1",
  "cc.spray" %%  "spray-json" % "1.1.0",
  "org.lemurproject.galago" % "core" % "3.2",
  "org.eclipse.jetty" % "jetty-webapp" % "7.1.0.RC1" % "container",
  "org.scala-tools.testing" % "specs_2.9.0" % "1.6.8" % "test", // For specs.org tests
  "junit" % "junit" % "4.8" % "test->default", // For JUnit 4 testing
  "javax.servlet" % "servlet-api" % "2.5" % "provided->default",
  "ch.qos.logback" % "logback-classic" % "0.9.26" % "compile->default"
)

