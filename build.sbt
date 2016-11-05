name := "Muse-CGH"

version := "1.4.1"

scalaVersion := "2.11.7"

scalaSource in Compile := baseDirectory.value / "src"

libraryDependencies ++= Seq(
  "org.scalacheck" %% "scalacheck" % "1.13.2" % "test"
)

fork in run := true
baseDirectory in run := file(".")

val museJarName = settingKey[String]("output jar name")
museJarName := s"muse-${version.value}.jar"

mainClass in assembly := Some("ui.command_line.CommandLineMain")
test in assembly := {}
assemblyJarName in assembly := museJarName.value
