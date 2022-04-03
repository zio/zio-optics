package zio.optics

import zio._
import zio.test.Assertion._
import zio.test._

object TraversalSpec extends ZIOSpecDefault with EitherCompat {

  def spec: ZSpec[Environment with TestEnvironment with ZIOAppArgs with Scope, Any] = suite("TraversalSpec")(
    suite("operators")(
      test("update") {
        check(Gen.chunkOf(Gen.either(Gen.int, Gen.int)), Gen.function(Gen.int)) { (chunk, f) =>
          val actual   = chunk.optic.foreach(Prism.right).update(_.map(f))
          val expected = chunk.map(_.map(f))
          assert(actual)(isRight(equalTo(expected)))
        }
      }
    )
  )
}
