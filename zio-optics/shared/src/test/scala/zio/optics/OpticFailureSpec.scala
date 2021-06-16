package zio.optics

import zio.test.Assertion._
import zio.test._

object OpticFailureSpec extends DefaultRunnableSpec {

  def spec: ZSpec[Environment, Failure] = suite("OpticFailureSpec")(
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
