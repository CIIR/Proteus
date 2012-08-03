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
              Seq(resolvers := Resolvers.all,
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
  val galagoSF = "Galago @ SourceForge" at "http://lemur.sf.net/repo"
  val blikiRepo = "Bliki Google Code Repo" at "http://gwtwiki.googlecode.com/svn/maven-repository/"

  def all = Seq(twitter, sonatype, galagoSF, blikiRepo)
}

object AuraDeps {

  val finagleVer = "3.0.0"

  val thriftLib = "org.apache.thrift" % "libthrift" % "0.5.0"
  val finagleCore = "com.twitter" %% "finagle-core" % finagleVer
  val finagleThrift = "com.twitter" %% "finagle-thrift" % finagleVer
  val ostrich = "com.twitter" %% "finagle-ostrich4" % finagleVer
  val scroogeRuntime = "com.twitter" %% "scrooge-runtime" % "1.1.3"
  val galagoCore = "org.lemurproject.galago" % "core" % "3.3-SNAPSHOT"
  val galagoTupleflow = "org.lemurproject.galago" % "core" % "3.3-SNAPSHOT"

  def deps = Seq(thriftLib, finagleCore, finagleThrift, ostrich, scroogeRuntime,
	       galagoCore, galagoTupleflow)

}

object MorpheusDeps {

  val scalatra = "org.scalatra" %% "scalatra" % "2.0.4"
  val scalate = "org.scalatra" %% "scalatra-scalate" % "2.0.4"
  val scalatraSpecs2 = "org.scalatra" %% "scalatra-specs2" % "2.0.4" % "test"
  val logback = "ch.qos.logback" % "logback-classic" % "1.0.0" % "runtime"  // is this shit really necessary, it's in every project I've ever seen and only seems to clutter shit up  /rant
  val jetty = "org.eclipse.jetty" % "jetty-webapp" % "7.6.0.v20120127" % "container"
  val jettyComp = "org.eclipse.jetty" % "jetty-webapp" % "7.6.0.v20120127" % "compile"
  val javax = "javax.servlet" % "servlet-api" % "2.5" % "provided"
  val galagoCore = "org.lemurproject.galago" % "core" % "3.3-SNAPSHOT"
  val galagoTupleflow = "org.lemurproject.galago" % "core" % "3.3-SNAPSHOT"
  def deps = Seq(scalatra, scalate, scalatraSpecs2, logback, jetty, jettyComp, javax, galagoCore, galagoTupleflow)
}


