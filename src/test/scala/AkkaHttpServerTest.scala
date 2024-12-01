import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class AkkaHttpServerTest extends AnyWordSpec with Matchers with ScalatestRouteTest {
    "respond to a GET request at /health with a health check message" in {
      Get("/health") ~> Endpoint.routes ~> check {
        // Check that the status is OK
        status shouldBe StatusCodes.OK

        // Check the content of the response
        responseAs[String] shouldEqual "LLM REST Service is up and running!"
      }
    }
}
