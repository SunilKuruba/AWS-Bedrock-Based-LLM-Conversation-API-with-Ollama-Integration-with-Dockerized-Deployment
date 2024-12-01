package test

import akka.actor.ActorSystem
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import protobuf.data.QueryRequest
import util._

class IntegrationSpec extends AnyFunSpec with Matchers {
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
      val originalRequest = new QueryRequest("Test Input", 100)
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
  // Clean up
  system.terminate()
}