package test

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import akka.actor.ActorSystem
import protobuf.llmQuery.LlmQueryRequest
import util._
import akka.http.scaladsl.model.StatusCodes
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class OllamaAPITestSuite extends AnyFunSpec with Matchers {
  implicit val system: ActorSystem = ActorSystem("TestSystem")

  describe("ConfigLoader") {
    it("should load configuration values correctly") {
      val host = ConfigLoader.get("ollama.host")
      val model = ConfigLoader.get("ollama.model")

      host shouldNot be(null)
      host.nonEmpty shouldBe true
      model shouldNot be(null)
      model.nonEmpty shouldBe true
    }
  }

  describe("JsonFormats") {
    it("should serialize and deserialize LlmQueryRequest") {
      val originalRequest = new LlmQueryRequest("Test Input", 100)
      val jsonValue = JsonFormats.llmQueryRequestFormat.write(originalRequest)
      val reconstructedRequest = JsonFormats.llmQueryRequestFormat.read(jsonValue)

      reconstructedRequest.input shouldBe originalRequest.input
      reconstructedRequest.maxWords shouldBe originalRequest.maxWords
    }
  }

  describe("YAML_Helper") {
    it("should create and save YAML results") {
      val results = YAML_Helper.createMutableResult()
      YAML_Helper.appendResult(results, "Test Question", "Test Response")

      results.size shouldBe 1
      results.head.question shouldBe "Test Question"
      results.head.llmResponse shouldBe "Test Response"

      noException should be thrownBy {
        YAML_Helper.save(results)
      }
    }
  }

  describe("LambdaInvoker") {
    it("should successfully invoke LLM API with a valid request") {
      val protoRequest = new LlmQueryRequest("What is cloud computing?", 100)

      val responseFuture = LambdaInvoker.get(protoRequest)

      val response = Await.result(responseFuture, 10.seconds)

      response shouldNot be(null)
      response.input shouldBe protoRequest.input
      response.output shouldNot be(empty)
    }

    it("should fail for an invalid request") {
      val invalidRequest = new LlmQueryRequest("", 0)

      val responseFuture = LambdaInvoker.get(invalidRequest)

      a[RuntimeException] should be thrownBy {
        Await.result(responseFuture, 10.seconds)
      }
    }
  }

  describe("Endpoint") {
    import akka.http.scaladsl.testkit.ScalatestRouteTest
    import akka.http.scaladsl.server.Route

    trait TestRoutes extends ScalatestRouteTest {
      def routes: Route = Endpoint.routes
    }

    it("should return health check successfully") {
      new TestRoutes {
        Get("/health") ~> routes ~> check {
          status shouldBe StatusCodes.OK
          responseAs[String] shouldBe "LLM REST Service is up and running!"
        }
      }
    }
  }

  describe("OllamaAPIClient") {
    it("should run a complete conversation iteration") {
      implicit val system: ActorSystem = ActorSystem("ConversationTest")

      val protoRequest = new LlmQueryRequest("What is machine learning?", 100)

      noException should be thrownBy {
        OllamaAPIClient.start(protoRequest)
      }
    }
  }

  // Clean up
  system.terminate()
}