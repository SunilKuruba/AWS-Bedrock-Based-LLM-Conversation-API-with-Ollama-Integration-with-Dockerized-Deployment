import org.scalatest.funsuite.AnyFunSuite
import util.ConfigLoader
import com.typesafe.config.ConfigException

class ConfigLoaderTest extends AnyFunSuite {

  test("Load valid configuration value for ollama.host") {
    val host = ConfigLoader.get("ollama.host")
    assert(host == "http://localhost:11434", "Failed to load ollama.host configuration")
  }

  test("Load valid configuration value for ollama.model") {
    val model = ConfigLoader.get("ollama.model")
    assert(model == "llama3.2", "Failed to load ollama.model configuration")
  }

  test("Load valid configuration value for awsLambdaApiGateway") {
    val awsLambdaApiGateway = ConfigLoader.get("awsLambdaApiGateway")
    assert(awsLambdaApiGateway == "https://gbwdh5x7g8.execute-api.us-east-2.amazonaws.com/test/llm-bedrock",
      "Failed to load awsLambdaApiGateway configuration")
  }

  test("Throw exception for missing configuration value") {
    val exception = intercept[ConfigException.Missing] {
      ConfigLoader.get("ollama.nonexistent")
    }
    assert(exception.getMessage.contains("No configuration setting found for key 'ollama.nonexistent'"))
  }
}
