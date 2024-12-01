import spray.json._
import DefaultJsonProtocol._
import protobuf.llmQuery.{LlmQueryRequest, LlmQueryResponse}

// Intermediate case classes for proto objects
case class LlmQueryRequestCase(input: String, maxWords: Int)
case class LlmQueryResponseCase(input: String, output: String)

//case class LLMResponse(input: String, output: String)

// JSON Formats
object JsonFormats {
  // JSON formats for intermediate case classes
  implicit val llmQueryRequestCaseFormat: RootJsonFormat[LlmQueryRequestCase] = jsonFormat2(LlmQueryRequestCase)
  implicit val llmQueryResponseCaseFormat: RootJsonFormat[LlmQueryResponseCase] = jsonFormat2(LlmQueryResponseCase)

  //  implicit val LLMQueryResponse: RootJsonFormat[LLMResponse] = jsonFormat2(LLMResponse)

  // Custom formats for proto-generated classes
  implicit val llmQueryRequestFormat: RootJsonFormat[LlmQueryRequest] = new RootJsonFormat[LlmQueryRequest] {
    override def write(obj: LlmQueryRequest): JsValue = {
      LlmQueryRequestCase(obj.input, obj.maxWords).toJson
    }
    override def read(json: JsValue): LlmQueryRequest = {
      val caseClass = json.convertTo[LlmQueryRequestCase]
      LlmQueryRequest(caseClass.input, caseClass.maxWords)
    }
  }

  implicit val llmQueryResponseFormat: RootJsonFormat[LlmQueryResponse] = new RootJsonFormat[LlmQueryResponse] {
    override def write(obj: LlmQueryResponse): JsValue = {
      LlmQueryResponseCase(obj.input, obj.output).toJson
    }
    override def read(json: JsValue): LlmQueryResponse = {
      val caseClass = json.convertTo[LlmQueryResponseCase]
      LlmQueryResponse(caseClass.input, caseClass.output)
    }
  }
}