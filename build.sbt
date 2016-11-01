name := "Muse-CGH"

version := "1.4"

scalaVersion := "2.11.7"

scalaSource in Compile := baseDirectory.value / "src"

libraryDependencies ++= Seq(
  "org.scalacheck" %% "scalacheck" % "1.13.2" % "test"
)

fork in run := true
baseDirectory in run := file(".")