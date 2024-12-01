import util.JsonFormats._
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import org.slf4j.LoggerFactory
import protobuf.data.{QueryRequest, QueryResponse}
import spray.json._
import util.ConfigLoader

import scala.concurrent.Future
import scala.concurrent.duration._

/**
 * This object defines a method to send an HTTP GET request to a configured API endpoint
 * for querying a large language model. It handles API requests, parses the response, and
 * performs logging for debugging and monitoring.
 */
object LambdaInvoker {
  private val logger = LoggerFactory.getLogger(getClass)

  def get(protoRequest: QueryRequest)(implicit system: ActorSystem): Future[QueryResponse] = {
    implicit val ec = system.dispatcher
    implicit val materializer = ActorMaterializer()
    val url = ConfigLoader.get("awsLambdaApiGateway")

    // Create the HTTP GET request with headers and payload
    val httpRequest = HttpRequest(
      method = HttpMethods.GET,
      uri = Uri(url),
      entity = HttpEntity(ContentTypes.`application/grpc+proto`, protoRequest.toProtoString.getBytes)
    )

    // Send request and handle response
    val responseFuture = Http().singleRequest(httpRequest).flatMap { response =>
      response.status.intValue() match {

        // Handle successful responses (HTTP status code 200-299)
        case statusCode if statusCode >= 200 && statusCode < 300 =>
          response.entity.toStrict(5.seconds).map { entity =>
            val responseBody = entity.getData().utf8String
            val resp = responseBody.parseJson.convertTo[QueryResponse]
            logger.info(resp.toString)
            resp
          }

        // Handle client and server error responses (HTTP status code 400-599)
        case statusCode if statusCode >= 300 && statusCode <= 599  =>
          val errorMsg = s"API call failed with status: ${response.status}, \n error Message: ${response.entity},"
          logger.error(errorMsg)
          Future.failed(new RuntimeException(errorMsg))
      }
    }
    responseFuture
  }
}