package util

import org.yaml.snakeyaml.{DumperOptions, Yaml}

import java.io.{BufferedWriter, File, FileWriter}
import java.time.Instant
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters._

case class OutputWriter(question: String, llmResponse: String)

object YAML_Helper {
  private val options = new DumperOptions()
  options.setPrettyFlow(true)
  options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK)
  private val yaml = new Yaml(options)

  def createMutableResult(): ListBuffer[OutputWriter] = {
    ListBuffer.empty[OutputWriter]
  }

  def appendResult(results: ListBuffer[OutputWriter], question: String, llmResp: String): Unit = {
    results += OutputWriter(question, llmResp)
  }

  def save(results: ListBuffer[OutputWriter]): Unit = {
    val env = ConfigLoader.get("env")
    val path = ConfigLoader.get(s"${env}-output-path")
    val file = new File(s"${path}output-${Instant.now().toString}.yaml")
    val writer = new BufferedWriter(new FileWriter(file))

    try {
      // Convert the entire results list to a list of maps for YAML dumping
      val yamlEntries = results.zipWithIndex.map { case (result, index) =>
        Map(
          s"" -> Map(
            "* Ollama" -> { result.question + "\n"},
            "* AWS Bedrock" -> { result.llmResponse+ "\n"}
          ).asJava
        ).asJava
      }.asJava

      // Dump the entire list of entries at once
      yaml.dump(yamlEntries, writer)

      println(s"YAML file created at: ${file.getAbsolutePath}")
    } finally {
      writer.close()
    }
  }
}