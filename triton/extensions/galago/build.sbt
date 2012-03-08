
name := "triton-galago"

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
  "edu.umass.ciir.proteus"    %% "triton-core" % "0.1",
  "joda-time" % "joda-time" % "2.0", 
  "org.joda" % "joda-convert" % "1.1",
  "org.lemurproject.galago" % "core" % "3.2",
  "org.lemurproject.galago" % "tupleflow" % "3.2"
)

publishArtifact in Compile := true
