package zio.optics

import zio._
import zio.test._
import zio.test.Assertion._

object LensSpec extends DefaultRunnableSpec {

  def spec = suite("LensSpec")(
    suite("constructors")(
      testM("first")(lensLaws(Gen.anyInt.zip(Gen.anyInt), Gen.anyInt)(Lens.first)),
      testM("second")(lensLaws(Gen.anyInt.zip(Gen.anyInt), Gen.anyInt)(Lens.second))
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
