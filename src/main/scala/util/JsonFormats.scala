package util

import protobuf.data.{QueryRequest, QueryResponse}
import spray.json.DefaultJsonProtocol._
import spray.json._

/**
 * Case classes to serve as intermediate representations for proto-generated classes.
 * These classes simplify JSON serialization and deserialization.
 */
case class LlmQueryRequestCase(input: String, maxWords: Int)
case class LlmQueryResponseCase(input: String, output: String)

/**
 * Object containing JSON format definitions for proto-generated classes and their intermediate representations.
 * This enables seamless conversion between JSON and Scala objects.
 */
object JsonFormats {

  // JSON formats for the intermediate case classes
  implicit val llmQueryRequestCaseFormat: RootJsonFormat[LlmQueryRequestCase] = jsonFormat2(LlmQueryRequestCase)
  implicit val llmQueryResponseCaseFormat: RootJsonFormat[LlmQueryResponseCase] = jsonFormat2(LlmQueryResponseCase)

  /**
   * JSON format for the `LlmQueryRequest` proto-generated class.
   * Converts between `LlmQueryRequest` and its JSON representation using the intermediate `LlmQueryRequestCase`.
   */
  implicit val llmQueryRequestFormat: RootJsonFormat[QueryRequest] = new RootJsonFormat[QueryRequest] {
    override def write(obj: QueryRequest): JsValue = {
      // Serialize `LlmQueryRequest` to JSON via `LlmQueryRequestCase`
      LlmQueryRequestCase(obj.input, obj.maxWords).toJson
    }

    override def read(json: JsValue): QueryRequest = {
      // Deserialize JSON to `LlmQueryRequest` via `LlmQueryRequestCase`
      val caseClass = json.convertTo[LlmQueryRequestCase]
      QueryRequest(caseClass.input, caseClass.maxWords)
    }
  }

  /**
   * JSON format for the `LlmQueryResponse` proto-generated class.
   * Converts between `LlmQueryResponse` and its JSON representation using the intermediate `LlmQueryResponseCase`.
   */
  implicit val llmQueryResponseFormat: RootJsonFormat[QueryResponse] = new RootJsonFormat[QueryResponse] {
    override def write(obj: QueryResponse): JsValue = {
      // Serialize `LlmQueryResponse` to JSON via `LlmQueryResponseCase`
      LlmQueryResponseCase(obj.input, obj.output).toJson
    }

    override def read(json: JsValue): QueryResponse = {
      // Deserialize JSON to `LlmQueryResponse` via `LlmQueryResponseCase`
      val caseClass = json.convertTo[LlmQueryResponseCase]
      QueryResponse(caseClass.input, caseClass.output)
    }
  }
}
