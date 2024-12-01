import AkkaHttpServer.system
import akka.actor.ActorSystem
import io.github.ollama4j.OllamaAPI
import io.github.ollama4j.utils.Options
import org.slf4j.LoggerFactory
import protobuf.data.QueryRequest
import util.{ConfigLoader, OutputWriter, YAML_Helper}

import scala.collection.mutable.ListBuffer
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.jdk.CollectionConverters._

/**
 * AutomatedConversationalAgent orchestrates API invocations to process conversational inputs
 * and generate refined responses in multiple iterations.
 */
object OllamaAPIClient {
  private val logger = LoggerFactory.getLogger(getClass)
  private val LLAMA_PREFIX = "Give me one follow-up question for "

  /**
   * Main entry point for the AutomatedConversationalAgent.
   *
   * @param args Array of arguments where the first argument is the seed text.
   */
  def main(args: Array[String]): Unit = {
    val seedText = args.headOption.getOrElse("What is cloud computing?")
    val protoRequest = new QueryRequest(seedText, 100)
    implicit val system: ActorSystem = ActorSystem("ConversationAgent")

    try {
      start(protoRequest)
    } finally {
      system.terminate()
    }
  }

  /**
   * Starts the conversational processing loop by invoking APIs in sequence.
   *
   * @param protoRequest Initial LLM request object.
   * @param system       Implicit ActorSystem for managing API calls.
   */
  def start(protoRequest: QueryRequest)(implicit system: ActorSystem): Unit = {
    val llamaAPI = initializeLlamaAPI()
    val llamaModel = ConfigLoader.get("ollama.model")
    val iterations = ConfigLoader.get("ollama.iterations").toInt

    val results = YAML_Helper.createMutableResult()
    var currentRequest = protoRequest

    // Sequentially process each iteration
    Iterator.range(0, iterations).foreach { iteration =>
      try {
        processIteration(iteration, currentRequest, llamaAPI, llamaModel, results) match {
          case Some(nextRequest) => currentRequest = nextRequest
          case None => logger.warn(s"Stopping early at iteration $iteration due to empty response.")
        }
      } catch {
        case e: Exception =>
          logger.error(s"Processing failed at iteration $iteration: ${e.getMessage}", e)
          throw e
      }
    }

    // Save results after all iterations
    YAML_Helper.save(results)
  }

  /**
   * Processes a single iteration of the conversational loop.
   *
   * @param iteration     Current iteration number.
   * @param request       LLM request object for the iteration.
   * @param llamaAPI      OllamaAPI client instance.
   * @param llamaModel    Model identifier for the Ollama API.
   * @param results       Mutable YAML results object.
   * @return              Optionally, the next request for subsequent iterations.
   */
  private def processIteration(
                                iteration: Int,
                                request: QueryRequest,
                                llamaAPI: OllamaAPI,
                                llamaModel: String,
                                results: ListBuffer[OutputWriter]
                              ): Option[QueryRequest] = {
    logger.info(s"Running iteration $iteration...")

    // Get LLM response synchronously
    val grpcResponse = Await.result(LambdaInvoker.get(request), 10.seconds)
    val input = request.input
    val output = grpcResponse.output

    // Generate response using OllamaAPI
    val llamaResponse = llamaAPI
      .generate(
        llamaModel,
        LLAMA_PREFIX + output,
        false,
        new Options(Map.empty[String, Object].asJava)
      )
      .getResponse

    // Log and store results
    logger.info(s"Generated response: $llamaResponse")
    YAML_Helper.appendResult(results, input, output)

    // Prepare the next request based on the generated response
    Some(new QueryRequest( llamaResponse, 100))
  }

  /**
   * Initializes the OllamaAPI client with configuration settings.
   *
   * @return Configured OllamaAPI instance.
   */
  private def initializeLlamaAPI(): OllamaAPI = {
    val host = ConfigLoader.get("ollama.host")
    val timeout = ConfigLoader.get("ollama.query-timeout").toLong
    val llamaAPI = new OllamaAPI(host)
    llamaAPI.setRequestTimeoutSeconds(timeout)
    logger.info(s"OllamaAPI initialized with host: $host and timeout: $timeout seconds")
    llamaAPI
  }
}
