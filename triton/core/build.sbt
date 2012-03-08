import sbtprotobuf.{ProtobufPlugin=>PB}

seq(PB.protobufSettings: _*)

javaSource in PB.protobufConfig <<= (sourceDirectory in Compile)(_ / "generated")

version in PB.protobufConfig := "2.3.0"

name := "triton-core"

organization := "edu.umass.ciir.proteus"

version := "0.1"
 
scalaVersion := "2.9.1"
 
resolvers ++= Seq(
  "Scala Tools Snapshot" at "http://scala-tools.org/repo-releases/",
  "Java.net Maven2 Repository" at "http://download.java.net/maven/2/",
  "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
  "Akka Repo" at "http://akka.io/repository",
  "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository",
  "GuiceyFruit Repo" at "http://guiceyfruit.googlecode.com/svn/repo/releases/",
  "JBoss Repo" at "http://repository.jboss.org/nexus/content/groups/public/"
)

// when using the sbt web app plugin 0.2.4+, use "container" instead of "jetty" for the context
// Customize any further dependencies as desired
libraryDependencies ++= Seq(
  "se.scalablesolutions.akka" % "akka-actor" % "1.2" % "compile",
  "se.scalablesolutions.akka" % "akka-slf4j" % "1.2",
  "se.scalablesolutions.akka" % "akka-remote" % "1.2",
  "se.scalablesolutions.akka" % "akka-camel" % "1.2",
  "se.scalablesolutions.akka" % "akka-typed-actor" % "1.2",
  "se.scalablesolutions.akka" % "akka-stm" % "1.2",
  "se.scalablesolutions.akka" % "akka-camel-typed" % "1.2"
)

publishArtifact in Compile := true
