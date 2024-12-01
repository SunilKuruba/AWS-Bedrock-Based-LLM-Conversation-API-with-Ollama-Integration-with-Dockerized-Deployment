import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.concurrent.duration._

/**
 * A simple Akka HTTP server that binds to localhost:8080 and serves requests
 * defined in the `LLMRoutes` object.
 *
 * This server runs asynchronously, handling HTTP requests using Akka HTTP,
 * and gracefully shuts down when the user presses the RETURN key.
 */
object AkkaHttpServer {

  // Implicit values needed for Akka HTTP and Akka Streams
  implicit val system: ActorSystem = ActorSystem("AkkaHttpServer")
  implicit val actorMaterializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  def main(args: Array[String]): Unit = {
    // Bind the HTTP server to localhost:8080 and handle requests via LLMRoutes.routes
    val bindingFuture = Http().newServerAt("0.0.0.0", 8080).bind(Endpoint.routes)

    // Inform the user and wait for input to terminate
    println("Akka HTTP Server running at http://0.0.0.0:8080")

    // Schedule server shutdown after 30 minutes
    val shutdownFuture: Future[Unit] = Future {
      Thread.sleep(30.minutes.toMillis)
      println("Shutting down the server after 30 minutes.")
      bindingFuture
        .flatMap(_.unbind())
        .onComplete(_ => system.terminate())
    }

    shutdownFuture.onComplete(_ => system.terminate())
  }
}
