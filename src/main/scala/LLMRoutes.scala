import JsonFormats.{llmQueryRequestFormat, llmQueryResponseFormat}
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import org.slf4j.LoggerFactory
import spray.json._
import protobuf.llmQuery.{LlmQueryRequest, LlmQueryResponse}

import scala.concurrent.Future
import scala.concurrent.duration._

object LLMRoutes {
  private val logger = LoggerFactory.getLogger(getClass)

  private def queryLLM(protoRequest: LlmQueryRequest)(implicit system: ActorSystem): Future[LlmQueryResponse] = {
    implicit val ec = system.dispatcher
    implicit val materializer = ActorMaterializer()

    val url = ConfigLoader.getConfig("lambdaApiGateway")
    val maxWords = if (protoRequest.maxWords != 0) protoRequest.maxWords
    else ConfigLoader.getConfig("maxWords").toInt

    val httpRequest = HttpRequest(
      method = HttpMethods.GET,
      uri = Uri(url),
      headers = List(`Content-Type`(ContentTypes.`application/grpc+proto`)),
      entity = HttpEntity(ContentTypes.`application/grpc+proto`, protoRequest.toProtoString.getBytes)
    )

    Http().singleRequest(httpRequest).flatMap { response =>
      if (response.status.isSuccess()) {
        response.entity.toStrict(5.seconds).map { entity =>
          val responseBody = entity.getData().utf8String
          val resp = responseBody.parseJson.convertTo[LlmQueryResponse]

          // Truncate response if it exceeds max words
          if (resp.output.split(" ").length > maxWords) {
            val truncatedOutput = resp.output.split(" ").take(maxWords).mkString(" ")
            logger.info(s"Truncated response: $truncatedOutput")
            resp.copy(output = truncatedOutput)
          } else {
            logger.info(s"Full response: $resp")
            resp
          }
        }
      } else {
        val errorMsg = s"API call failed with status: ${response.status}, error: ${response.entity}"
        logger.error(errorMsg)
        Future.failed(new RuntimeException(errorMsg))
      }
    }
  }

  def routes(implicit system: ActorSystem): Route = concat(
    path("query-llm") {
      get {
        entity(as[LlmQueryRequest]) { request =>
          onSuccess(queryLLM(request))(complete(_))
        }
      }
    },
    path("health") {
      get {
        complete(StatusCodes.OK, "LLM REST Service is up and running!")
      }
    }
  )
}