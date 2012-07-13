import sbtprotobuf.{ProtobufPlugin=>PB}

seq(PB.protobufSettings: _*)

javaSource in PB.protobufConfig <<= (sourceDirectory in Compile)(_ / "generated")

version in PB.protobufConfig := "2.4.1"

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
  "JBoss Repo" at "http://repository.jboss.org/nexus/content/groups/public/",
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
)

// when using the sbt web app plugin 0.2.4+, use "container" instead of "jetty" for the context
// Customize any further dependencies as desired
libraryDependencies ++= Seq(
  "com.typesafe.akka" % "akka-actor" % "2.0.2" % "compile",
  "com.typesafe.akka" % "akka-slf4j" % "2.0.2",
  "com.typesafe.akka" % "akka-remote" % "2.0.2"
)

publishArtifact in Compile := true
