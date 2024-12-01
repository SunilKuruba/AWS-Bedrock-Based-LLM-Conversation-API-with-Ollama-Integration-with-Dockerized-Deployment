import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import util.JsonFormats._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}
import protobuf.llmQuery._

import scala.util.{Failure, Success}

/**
 * Routes object that defines the HTTP endpoints for interacting with the LLM service.
 *
 * Provides endpoints for querying the LLM, starting a conversational agent, and checking service health.
 */
object Endpoint {
  private val logger = LoggerFactory.getLogger(getClass)

  /**
   * Defines the HTTP routes for the application.
   *
   * @param system The Akka ActorSystem used for handling concurrency and scheduling.
   * @return A Route object representing the HTTP endpoints.
   */
  def routes(implicit system: ActorSystem): Route = {
    implicit val ec: ExecutionContext = system.dispatcher

    // Combine multiple routes using the `concat` directive
    concat(
      /**
       * Route for querying the LLM.
       *
       * Endpoint: GET /query-llm
       * Input: JSON payload representing an LlmQueryRequest.
       * Output: JSON payload representing an LlmQueryResponse.
       */
      path("single-response-query") {
        get {
          entity(as[LlmQueryRequest]) { request =>
            // Use onSuccess to handle the asynchronous API call to GrpcApiInvoker
            onSuccess(LambdaInvoker.get(request)) { response =>
              complete(response) // Respond with the LLM query result
            }
          }
        }
      },

      /**
       * Route for starting the Automated Conversational Agent.
       *
       * Endpoint: GET /start-conversation-agent
       * Input: JSON payload representing an LlmQueryRequest.
       * Output: HTTP Status 202 (Accepted) with a confirmation message.
       */
      path("conversation-query") {
        get {
          entity(as[LlmQueryRequest]) { request =>
            // Start the conversational agent in a separate thread using a Future
            Future {
              logger.info("Starting chat...")
              OllamaAPIClient.start(request)
            }.onComplete {
              case Success(_)  => logger.info("Successfully completed the chat")
              case Failure(ex) => logger.error(s"Error occurred during chat: ${ex.getMessage}", ex)
            }

            complete(
              StatusCodes.Accepted,
              "Job submitted successfully. Shortly the output will be available at \'src/main/resources\'"
            )
          }
        }
      },

      /**
       * Route for checking the health of the LLM REST Service.
       *
       * Endpoint: GET /health
       * Output: HTTP Status 200 (OK) with a health check message.
       */
      path("health") {
        get {
          complete(StatusCodes.OK, "LLM REST Service is up and running!")
        }
      }
    )
  }
}
