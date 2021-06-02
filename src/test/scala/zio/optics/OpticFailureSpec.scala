package zio.optics

import zio.test._
import zio.test.Assertion._

object OpticFailureSpec extends DefaultRunnableSpec {

  def spec = suite("OpticFailureSpec")(
    suite("error reporting")(
      test("should report error with prism") {
        val whole                               = Right(42)
        val prism: Prism[Either[Int, Int], Int] = Prism.left
        val actual                              = prism.getOptic(whole)
        val message                             = actual.left.map(_._1.message)
        assert(message)(isLeft(equalTo("Right(42) did not satisfy isLeft")))
      }
    )
  )
}
