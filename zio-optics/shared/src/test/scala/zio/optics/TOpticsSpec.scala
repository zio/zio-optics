package zio.optics

import zio.optics.toptics._
import zio.stm._
import zio.test.Assertion._
import zio.test._

object TOpticsSpec extends DefaultRunnableSpec {

  def spec: ZSpec[Environment, Failure] =
    suite("TOpticsSpec")(
      test("TMap syntax") {
        val transaction = for {
          map      <- TMap.empty[String, Either[String, Int]]
          _        <- map.put("foo", Right(42))
          _        <- map.key("foo").right.update(_ + 1)
          snapshot <- map.toMap
        } yield snapshot
        assertM(transaction.commit)(equalTo(Map("foo" -> Right(43))))
      }
    )
}
