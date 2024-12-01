import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

/**
 * A simple Akka HTTP server that binds to localhost:8080 and serves requests
 * defined in the `LLMRoutes` object.
 *
 * This server runs asynchronously, handling HTTP requests using Akka HTTP,
 * and gracefully shuts down when the user presses the RETURN key.
 */
object AkkaHttpServer extends App {

  // Implicit values needed for Akka HTTP and Akka Streams
  implicit val system: ActorSystem = ActorSystem("AkkaHttpServer")
  implicit val actorMaterializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  // Bind the HTTP server to localhost:8080 and handle requests via LLMRoutes.routes
  val bindingFuture = Http().newServerAt("localhost", 8080).bind(Endpoint.routes)

  // Inform the user and wait for input to terminate
  println("Akka HTTP Server running at http://localhost:8080/\nPress RETURN to terminate...")
  StdIn.readLine()

  // Unbind the server and terminate the ActorSystem after the user input
  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ => system.terminate())
}
