package zio.optics

import zio._
import zio.test.Assertion._
import zio.test._

object PrismSpec extends ZIOSpecDefault {

  def spec: Spec[Environment, Any] = suite("PrismSpec")(
    suite("constructors")(
      test("cons")(prismLaws(Gen.listOf(Gen.int), Gen.int.zip(Gen.listOf(Gen.int)))(Prism.cons)),
      test("first")(prismLaws(Gen.either(Gen.int, Gen.int), Gen.int)(Prism.left)),
      test("second")(prismLaws(Gen.either(Gen.int, Gen.int), Gen.int)(Prism.right)),
      test("some")(prismLaws(Gen.option(Gen.int), Gen.int)(Prism.some))
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
