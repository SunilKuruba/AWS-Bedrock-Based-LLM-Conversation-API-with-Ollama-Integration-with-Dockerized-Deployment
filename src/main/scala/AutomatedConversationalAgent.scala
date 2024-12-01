import akka.http.scaladsl.model.headers.`Content-Type`
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest, Uri}
import io.github.ollama4j.OllamaAPI
import io.github.ollama4j.models.OllamaResult
import io.github.ollama4j.utils.Options
import org.slf4j.LoggerFactory
import protobuf.llmQuery.LlmQueryRequest

object AutomatedConversationalAgent {
  private val logger = LoggerFactory.getLogger(getClass)
  private val OLLAMA_HOST = "ollama.host"
  private val OLLAMA_REQUEST_TIMEOUT = "ollama.request-timeout-seconds"
  private val OLLAMA_MODEL = "ollama.model"
  private val OLLAMA_QUERIES_RANGE = "ollama.range"

  def main(args: Array[String]): Unit = {
    if (args.isEmpty) {
      logger.error("input seed text not passed")
      sys.exit(-1)
    }
    val seedText = args(0)

    val llamaAPI: OllamaAPI = new OllamaAPI(ConfigLoader.getConfig(OLLAMA_HOST))
    llamaAPI.setRequestTimeoutSeconds(ConfigLoader.getConfig(OLLAMA_REQUEST_TIMEOUT).toLong)
    val llamaModel = ConfigLoader.getConfig(OLLAMA_MODEL)
    val range = ConfigLoader.getConfig(OLLAMA_QUERIES_RANGE).toInt
    val protoRequest : LlmQueryRequest = new LlmQueryRequest(seedText, 100)

    Iterator.range(0, range).foreach { _ =>
      try {
        // Create HTTP request
        val httpRequest = HttpRequest(
          method = HttpMethods.GET,
          uri = Uri("http://localhost:8080/llm-query"),
          headers = List(`Content-Type`(ContentTypes.`application/grpc+proto`)),
          entity = HttpEntity(ContentTypes.`application/grpc+proto`, protoRequest.toProtoString.getBytes)
        )
        val result: OllamaResult = llamaAPI.generate(llamaModel, seedText, false, new Options(null))

        println(result.getResponse)
      } catch {
        case e: Exception =>
          println("PROCESS FAILED", e.getMessage)
          e.printStackTrace()
      }
    }
  }
}