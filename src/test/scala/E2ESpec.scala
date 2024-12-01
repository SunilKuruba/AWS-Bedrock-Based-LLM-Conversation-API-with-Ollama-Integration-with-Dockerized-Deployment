import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class E2ESpec extends AnyFlatSpec with Matchers{
  "End-to-end program" should "work with param args" in {
    val seedToken = "Hey there!"
    OllamaAPIClient.main(Array(seedToken))
  }
}
