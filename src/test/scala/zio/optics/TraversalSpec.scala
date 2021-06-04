package zio.optics

import zio._
import zio.test._
import zio.test.Assertion._

object TraversalSpec extends DefaultRunnableSpec with EitherCompat {

  def spec = suite("TraversalSpec")(
    suite("operators")(
      testM("update") {
        check(Gen.chunkOf(Gen.either(Gen.anyInt, Gen.anyInt)), Gen.function(Gen.anyInt)) { (chunk, f) =>
          val actual   = chunk.optic.foreach(Prism.right).update(_.map(f))
          val expected = chunk.map(_.map(f))
          assert(actual)(isRight(equalTo(expected)))
        }
      }
    )
  )
}
