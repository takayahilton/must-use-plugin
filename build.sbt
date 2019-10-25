scalaVersion  := "2.12.10"

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-library"  % scalaVersion.value,
  "org.scala-lang" % "scala-reflect"  % scalaVersion.value,
  "org.scala-lang" % "scala-compiler" % scalaVersion.value,
  "org.scalatest"  %% "scalatest"     % "3.0.8" % Test
)

Test / scalacOptions ++= {
  val jar = (Compile / packageBin).value
  Seq(s"-Xplugin:${jar.getAbsolutePath}", s"-Jdummy=${jar.lastModified}") // ensures recompile
}