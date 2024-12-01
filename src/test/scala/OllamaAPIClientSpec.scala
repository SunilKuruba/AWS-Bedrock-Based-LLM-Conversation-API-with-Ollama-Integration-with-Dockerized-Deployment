import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import protobuf.data.QueryRequest
import spray.json._
import util.JsonFormats._

import scala.concurrent.ExecutionContext

class OllamaAPIClientSpec extends AnyWordSpec with Matchers with ScalatestRouteTest {

  // Initialize ActorSystem for the test
  override implicit val system: ActorSystem = ActorSystem("TestSystem")
  implicit val ec: ExecutionContext = system.dispatcher

  // Your route definition here
  val route = Endpoint.routes(system)  // assuming the routes are defined in the Endpoint object

  "The Ollama API" should {
    "respond to a POST request at /single-response-query with valid JSON" in {
      // Define a valid query request
      val queryRequest = QueryRequest(input = "Hello, LLM!", maxWords = 5)
      val requestEntity = HttpEntity(ContentTypes.`application/json`, queryRequest.toJson.toString())

      // Send POST request to /single-response-query with valid JSON
      Post("/single-response-query", requestEntity) ~> route ~> check {
        status shouldBe StatusCodes.OK
        responseAs[String] should include("Mocked response for Hello, LLM!")
      }
    }

    "respond to a GET request at /health with 200 OK" in {
      // Send GET request to /health
      Get("/health") ~> route ~> check {
        status shouldBe StatusCodes.OK
        responseAs[String] shouldEqual "LLM REST Service is up and running!"
      }
    }
  }

  // Clean up the ActorSystem after the tests
  override def afterAll(): Unit = {
    system.terminate()
    super.afterAll()
  }
}
