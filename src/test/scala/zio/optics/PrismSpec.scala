package zio.optics

import zio._
import zio.test._
import zio.test.Assertion._

object PrismSpec extends DefaultRunnableSpec {

  def spec = suite("PrismSpec")(
    suite("constructors")(
      testM("cons")(prismLaws(Gen.listOf(Gen.anyInt), Gen.anyInt.zip(Gen.listOf(Gen.anyInt)))(Prism.cons)),
      testM("first")(prismLaws(Gen.either(Gen.anyInt, Gen.anyInt), Gen.anyInt)(Prism.left)),
      testM("second")(prismLaws(Gen.either(Gen.anyInt, Gen.anyInt), Gen.anyInt)(Prism.right)),
      testM("some")(prismLaws(Gen.option(Gen.anyInt), Gen.anyInt)(Prism.some))
    )
  )

  def prismLaws[Env <: TestConfig, Whole, Piece](genWhole: Gen[Env, Whole], genPiece: Gen[Env, Piece])(
    prism: Prism[Whole, Piece]
  ): URIO[Env, TestResult] =
    for {
      getSetIdentity <- getSetIdentity(genWhole)(prism)
      setGetIdentity <- setGetIdentity(genPiece)(prism)
    } yield getSetIdentity && setGetIdentity

  def getSetIdentity[Env <: TestConfig, Whole, Piece](
    genWhole: Gen[Env, Whole]
  )(prism: Prism[Whole, Piece]): URIO[Env, TestResult] =
    check(genWhole) { wholeBefore =>
      val piece      = prism.get(wholeBefore)
      val wholeAfter = piece.flatMap(prism.set)
      assert(piece)(isRight(anything)) ==> assert(wholeAfter)(isRight(equalTo(wholeBefore)))
    }

  def setGetIdentity[Env <: TestConfig, Whole, Piece](
    genPiece: Gen[Env, Piece]
  )(prism: Prism[Whole, Piece]): URIO[Env, TestResult] =
    check(genPiece) { pieceBefore =>
      val pieceAfter = prism.set(pieceBefore).flatMap(prism.get)
      assert(pieceAfter)(isRight(equalTo(pieceBefore)))
    }
}
