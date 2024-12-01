import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.net.URI
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class E2ESpec extends AnyFlatSpec with Matchers{
  "End-to-end program" should "work with param args" in {
    // Define your function to run in Thread 1
    def functionInThread1(): Unit = {
      AkkaHttpServer.main(Array())
    }

    // Define the curl-like GET request with a body in Thread 2
    def sendGetRequestWithBody(endpoint: String, body: String): Unit = {
      val client = HttpClient.newHttpClient()
      val request = HttpRequest.newBuilder()
        .uri(URI.create(endpoint))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(body)) // Simulate sending a body with GET request
        .build()

      val response = client.send(request, HttpResponse.BodyHandlers.ofString())
      println(s"Response: ${response.statusCode()} - ${response.body()}")
    }

    // Use Futures to run tasks in threads
    implicit val ec: ExecutionContext = ExecutionContext.global

    val thread1 = Future {
      functionInThread1()
    }

    val thread2 = Future {
      val endpoint = "http://localhost:8080/conversation-query"
      val body = """{ "input": "what is cloud computing?", "maxWords": 200 }""".stripMargin
      sendGetRequestWithBody(endpoint, body)
    }

    val combined = for { _ <- thread2} yield ()

    combined.onComplete {
      case Success(_) => println("Both threads have completed successfully.")
      case Failure(exception) => println(s"An error occurred: ${exception.getMessage}")
    }

    // Keep the main thread alive until all threads are done
    Thread.sleep(5000)
  }
}
