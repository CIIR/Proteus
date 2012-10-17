import sbt._
import Keys._
// plugin import settings
import com.github.siasia.WebPlugin.webSettings
import com.twitter.sbt.CompileThriftScrooge

object ProteusBuild extends Build {
  import BuildSettings.buildSettings

  lazy val proteus = Project(
    id = "proteus",
    base = file("."),
    settings = buildSettings
  ) aggregate(aura, morpheus)

  lazy val aura = Project(
    id = "aura",
    base = file("aura"),
    settings = buildSettings ++
              CompileThriftScrooge.newSettings ++
              Seq(resolvers := Resolver.withDefaultResolvers(Resolvers.all, true, true), 
                  libraryDependencies ++= AuraDeps.deps,
                  CompileThriftScrooge.scroogeVersion := "2.5.4")
  )

  lazy val morpheus = Project(
    id = "morpheus",
    base = file("morpheus"),
    settings = buildSettings ++
               webSettings ++
               Seq(resolvers := Resolvers.all,
                   libraryDependencies ++= MorpheusDeps.deps)
  ) dependsOn (aura)
}


object BuildSettings {
  val buildOrganization = "edu.ciir.umass"
  val buildVersion      = "0.1"
  val buildScalaVersion = "2.9.1"

  val buildSettings = Defaults.defaultSettings ++ Seq (
    organization := buildOrganization,
    version      := buildVersion,
    scalaVersion := buildScalaVersion
  )
}

object Resolvers {
  val twitter = "twitter" at "http://maven.twttr.com/"
  val sonatype = "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"
  val mavenLocal = "Local Maven Repository" at "file://"+Path.userHome+"/.m2/repository"
  val maven2 = "Maven 2 repo" at "http://repo2.maven.org/maven2"
  val galagoSF = "Galago @ SourceForge" at "http://lemur.sf.net/repo"
  val blikiRepo = "Bliki Google Code Repo" at "http://gwtwiki.googlecode.com/svn/maven-repository/"

  def all = Seq(twitter, sonatype, blikiRepo, maven2, mavenLocal)
}

object AuraDeps {

  val finagleVer = "3.0.0"

  val thriftLib = "org.apache.thrift" % "libthrift" % "0.5.0"
  val finagleCore = "com.twitter" %% "finagle-core" % finagleVer
  val finagleThrift = "com.twitter" %% "finagle-thrift" % finagleVer
  val ostrich = "com.twitter" %% "finagle-ostrich4" % finagleVer
  val scroogeRuntime = "com.twitter" %% "scrooge-runtime" % "1.1.3"
  val galagoCore = "org.lemurproject.galago" % "core" % "3.3-PROTEUS"
  val galagoTupleflow = "org.lemurproject.galago" % "tupleflow" % "3.3-PROTEUS"
  val gson = "com.google.code.gson" % "gson" % "2.2.2"
  val logback = "ch.qos.logback" % "logback-classic" % "1.0.7"

  def deps = Seq(thriftLib, finagleCore, finagleThrift, ostrich, scroogeRuntime,
	       galagoCore, galagoTupleflow, gson, logback)
}

object MorpheusDeps {

  val scalatra = "org.scalatra" % "scalatra" % "2.1.1"
  val scalate = "org.scalatra" % "scalatra-scalate" % "2.1.1"
  val scalatraSpecs2 = "org.scalatra" % "scalatra-specs2" % "2.1.1" % "test"
  val logback = "ch.qos.logback" % "logback-classic" % "1.0.7"
  val jetty = "org.eclipse.jetty" % "jetty-webapp" % "8.1.7.v20120910" % "container"
  val jettyOrbit = "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "container;provided;test" artifacts (Artifact("javax.servlet", "jar", "jar"))
  val galagoCore = "org.lemurproject.galago" % "core" % "3.3-PROTEUS"
  val galagoTupleflow = "org.lemurproject.galago" % "tupleflow" % "3.3-PROTEUS"
  val liftjson = "net.liftweb" %% "lift-json" % "2.4"
  def deps = Seq(scalatra, scalate, scalatraSpecs2, logback, jetty, jettyOrbit,
		 galagoCore, galagoTupleflow, liftjson)

}


