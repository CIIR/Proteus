import AssemblyKeys._ // put this at the top of the file

assemblySettings

jarName in assembly := "pharos.jar"

test in assembly := {}

mainClass in assembly := Some("ciir.proteus.entitylinking.TeiAnnotator2")
