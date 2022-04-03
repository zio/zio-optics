package zio.optics

import zio.test.Assertion._
import zio.test._
import zio.{Scope, ZIOAppArgs}

object OpticFailureSpec extends ZIOSpecDefault {

  def spec: ZSpec[Environment with TestEnvironment with ZIOAppArgs with Scope, Any] = suite("OpticFailureSpec")(
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
