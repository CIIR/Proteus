import sbt._
import Keys._

object ProteusBuild extends Build {
    lazy val proteus = Project(id = "proteus",
                            base = file(".")) aggregate(aura, morpheus)

    lazy val aura = Project(id = "aura",
                           base = file("aura"))

    lazy val morpheus = Project(id = "morpheus",
                           base = file("morpheus")) dependsOn(aura)
}
