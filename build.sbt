import sbt.Keys.libraryDependencies
import sbtprotoc.ProtocPlugin.autoImport._

lazy val root = project
  .in(file("."))
  .enablePlugins(ProtocPlugin) // Enable the ProtocPlugin explicitly
  .settings(
    name := "LLM-hw3",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := "2.13.13",

    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % "2.6.20",
      "com.typesafe.akka" %% "akka-stream" % "2.6.20",
      "com.typesafe.akka" %% "akka-http" % "10.2.10",
      "com.typesafe" % "config" % "1.4.2",
      "com.typesafe.akka" %% "akka-http-spray-json" % "10.2.10", // JSON marshalling/unmarshalling
      "io.github.ollama4j" % "ollama4j" % "1.0.79", // Ollama
      "software.amazon.awssdk" % "lambda" % "2.25.27", // AWS Lambda SDK
      "software.amazon.awssdk" % "core" % "2.26.25",    // AWS Core SDK
      "com.softwaremill.sttp.client3" %% "core" % "3.9.7",
      "com.thesamet.scalapb" %% "scalapb-json4s" % "0.11.0",
      "org.yaml" % "snakeyaml" % "1.24",
      "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf",
      "org.scalatest" %% "scalatest" % "3.2.16" % Test,
      "org.slf4j" % "slf4j-simple" % "2.0.13",
      "org.scalameta" %% "munit" % "1.0.0" % Test,
      "com.typesafe.akka" %% "akka-http-testkit" % "10.2.9" % Test,
      "com.typesafe.akka" %% "akka-testkit" % "2.6.20" % Test
    ),

    // Include .proto files in the project resources
    Compile / PB.protoSources := Seq(file("src/main/protobuf")),

    Compile / PB.targets := Seq(
      scalapb.gen() -> (sourceManaged in Compile).value
    ),

    // Assembly configuration
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", xs @ _*) =>
        xs match {
          case "MANIFEST.MF" :: Nil =>   MergeStrategy.discard
          case "services" ::_       =>   MergeStrategy.concat
          case _                    =>   MergeStrategy.discard
        }
      case "reference.conf"  => MergeStrategy.concat
      case x if x.endsWith(".proto") => MergeStrategy.rename
      case x if x.contains("hadoop") => MergeStrategy.first
      case  _ => MergeStrategy.first
    }
  )

resolvers += "Conjars Repo" at "https://conjars.org/repo"