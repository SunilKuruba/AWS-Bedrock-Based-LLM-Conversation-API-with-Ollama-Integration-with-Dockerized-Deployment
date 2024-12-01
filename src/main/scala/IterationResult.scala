import org.yaml.snakeyaml.{DumperOptions, Yaml}

import scala.collection.mutable.ListBuffer
import java.io.{BufferedWriter, File, FileWriter}
import java.time.Instant
import scala.jdk.CollectionConverters._

// TODO: Write result to txt file
case class IterationResult(
                            iteration: String,
                            question: String,
                            llmResponse: String,
                            ollamaResponse: String
                          )

object YAML_Helper {
  private val options = new DumperOptions
  options.setPrettyFlow(true)
  options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK) // Use block formatting for readability

  private val yaml = new Yaml(options)

  def createMutableResult(): ListBuffer[IterationResult] = {
    // List of iteration results
    ListBuffer.empty[IterationResult]
  }

  def appendResult(
                    results: ListBuffer[IterationResult],
                    iteration: Int,
                    question: String,
                    llmResp: String,
                    ollamaResp: String
                  ): Unit = {
    results += IterationResult(s"Itr-$iteration", question, llmResp, ollamaResp)
  }

  def save(results: ListBuffer[IterationResult]): Unit = {
    val file = new File("src/main/resources/conversation-agents/iteration_results-" + Instant.now().toString + ".yaml")
    val writer = new BufferedWriter(new FileWriter(file))

    try {
      results.foreach { result =>
        val entry = Map(
          result.iteration -> Map(
            "question" -> result.question,
            "LLM Response" -> result.llmResponse,
            "Ollama Response" -> result.ollamaResponse
          ).asJava
        ).asJava
        yaml.dump(entry, writer)
      }
      println(s"YAML file created at: ${file.getAbsolutePath}")
    } finally {
      writer.close()
    }
  }

}