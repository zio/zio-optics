package zio.optics

import zio._
import zio.test._
import zio.test.Assertion._

object TraversalSpec extends DefaultRunnableSpec {

  def spec = suite("TraversalSpec")(
    suite("operators")(
      testM("update") {
        check(Gen.chunkOf(Gen.either(Gen.anyInt, Gen.anyInt)), Gen.function(Gen.anyInt)) { (chunk, f) =>
          val rights   = Optic.identity[Chunk[Either[Int, Int]]].foreach(Prism.right)
          val actual   = rights.update(chunk)(_.map(f))
          val expected = chunk.map(_.map(f))
          assert(actual)(isRight(equalTo(expected)))
        }
      }
    )
  )
}
