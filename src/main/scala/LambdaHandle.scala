import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.services.lambda.LambdaClient
import software.amazon.awssdk.services.lambda.model._

object LambdaHandle {
  def main(args: Array[String]): Unit = {
    // Create the Lambda client
    val lambdaClient: LambdaClient = LambdaClient.builder()
      .region(software.amazon.awssdk.regions.Region.US_EAST_1) // Replace with your region
      .build()

    // Define the Lambda function name
    val functionName = "YourLambdaFunctionName"

    // Create the payload (e.g., JSON string)
    val payload = """{"key1": "value1", "key2": "value2"}"""

    // Create the request to invoke the Lambda function
    val invokeRequest = InvokeRequest.builder()
      .functionName(functionName)
      .payload(SdkBytes.fromUtf8String(payload)) // Convert payload to SdkBytes
      .build()

    // Invoke the Lambda function
    try {
      val response = lambdaClient.invoke(invokeRequest)

      // Handle the response
      val statusCode = response.statusCode()
      val responsePayload = response.payload().asUtf8String()

      println(s"Response status code: $statusCode")
      println(s"Response payload: $responsePayload")
    } catch {
      case e: Exception =>
        println(s"Failed to invoke Lambda function: ${e.getMessage}")
    } finally {
      lambdaClient.close()
    }
  }
}