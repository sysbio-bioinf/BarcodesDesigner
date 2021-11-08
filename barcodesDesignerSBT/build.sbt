import sbt.Resolver

/**
  * Build the sbt (Scala Build Tool) project
  * - automatically imports dependencies
  * - use sbt shell to build file
  * - use "assembly" in sbt shell to generate jar
  * @author Marietta Hamberger
  *
  */

// Name of the project
name := "Barcodes Designer" //"barcodesDesignerSBT"

// Project version
version := "1.0" //"16.0.0-R24"

// Version of Scala used by the project
scalaVersion := "2.11.12"

idePackagePrefix := Some("main.code")

// Add dependency on ScalaFX library
libraryDependencies += "org.scalafx" %% "scalafx" % "14-R19"
resolvers += Resolver.sonatypeRepo("snapshots")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-Xcheckinit", "-encoding", "utf8", "-feature")

// Fork a new JVM for 'run' and 'test:run', to avoid JavaFX double initialization problems
fork := true

// Determine OS version of JavaFX binaries
lazy val osName = System.getProperty("os.name") match {
  case n if n.startsWith("Linux") => "linux"
  case n if n.startsWith("Mac") => "mac"
  case n if n.startsWith("Windows") => "win"
  case _ => throw new Exception("Unknown platform!")
}

// Add JavaFX dependencies
lazy val javaFXModules = Seq("base", "controls", "fxml", "graphics")//, "media", "swing", "web")
libraryDependencies ++= javaFXModules.map( m=>
  "org.openjfx" % s"javafx-$m" % "14.0.1" classifier osName
)

// Add JSON4s dependencies
libraryDependencies += "org.json4s" %% "json4s-ast" % "3.2.10"
libraryDependencies += "org.json4s" %% "json4s-core" % "3.2.10"
libraryDependencies += "org.json4s" %% "json4s-native" % "3.2.10"

// Set "linux" or "win" instead of "mac", to generate jar for other platforms
assemblyMergeStrategy in assembly := {
  case PathList("javafx-media-14.0.1-mac.jar", xs @ _*) => MergeStrategy.last
  case PathList("javafx-swing-14.0.1-mac.jar", xs @ _*) => MergeStrategy.last
  case PathList("javafx-web-14.0.1-mac.jar", xs @ _*) => MergeStrategy.last
  case PathList("reference.conf") => MergeStrategy.concat
  case PathList("module-info.class") => MergeStrategy.discard
  //case _ => MergeStrategy.first
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

// assembly
JFX.mainClass := Some("main.code.ui.App")
mainClass in assembly := Some("main.code.ui.App")

assemblyJarName in assembly := "barcodesDesigner_v1.0.jar"
