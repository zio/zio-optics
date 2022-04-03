package zio.optics

import zio._
import zio.test.Assertion._
import zio.test._

object LensSpec extends ZIOSpecDefault {

  def spec: ZSpec[Environment, Any] = suite("LensSpec")(
    suite("constructors")(
      test("first")(lensLaws(Gen.int.zip(Gen.int), Gen.int)(Lens.first)),
      test("second")(lensLaws(Gen.int.zip(Gen.int), Gen.int)(Lens.second))
    )
  )

  def lensLaws[Env <: TestConfig, Whole, Piece](genWhole: Gen[Env, Whole], genPiece: Gen[Env, Piece])(
    lens: Lens[Whole, Piece]
  ): URIO[Env, TestResult] =
    for {
      getSetIdentity <- getSetIdentity(genWhole)(lens)
      setGetIdentity <- setGetIdentity(genWhole, genPiece)(lens)
      setSetIdentity <- setSetIdentity(genWhole, genPiece)(lens)
    } yield setGetIdentity && getSetIdentity && setSetIdentity

  def getSetIdentity[Env <: TestConfig, Whole, Piece](genWhole: Gen[Env, Whole])(
    lens: Lens[Whole, Piece]
  ): URIO[Env, TestResult] =
    check(genWhole) { wholeBefore =>
      val wholeAfter = lens.get(wholeBefore).flatMap(lens.set(_)(wholeBefore))
      assert(wholeAfter)(isRight(equalTo(wholeBefore)))
    }

  def setGetIdentity[Env <: TestConfig, Whole, Piece](genWhole: Gen[Env, Whole], genPiece: Gen[Env, Piece])(
    lens: Lens[Whole, Piece]
  ): URIO[Env, TestResult] =
    check(genWhole, genPiece) { (whole, pieceBefore) =>
      val pieceAfter = lens.set(pieceBefore)(whole).flatMap(lens.get)
      assert(pieceAfter)(isRight(equalTo(pieceBefore)))
    }

  def setSetIdentity[Env <: TestConfig, Whole, Piece](genWhole: Gen[Env, Whole], genPiece: Gen[Env, Piece])(
    lens: Lens[Whole, Piece]
  ): URIO[Env, TestResult] =
    check(genWhole, genPiece) { (whole, piece) =>
      val wholeBefore = lens.set(piece)(whole)
      val wholeAfter  = lens.set(piece)(whole).flatMap(lens.set(piece))
      assert(wholeAfter)(equalTo(wholeBefore))
    }
}
