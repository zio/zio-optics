package zio.optics

import zio._
import zio.test.Assertion._
import zio.test._

object OpticsSpec extends DefaultRunnableSpec {

  def spec: ZSpec[Environment, Failure] = suite("OpticsSpec")(
    suite("optics")(
      suite("lens")(
        test("set and get") {
          check(Gen.int.zip(Gen.int), Gen.int) { (s, a) =>
            for {
              ref    <- Ref.make(s)
              derived = ref.first
              _      <- derived.set(a)
              value  <- derived.get
            } yield assert(value)(equalTo(a))
          }
        },
        test("get and set") {
          check(Gen.int.zip(Gen.int)) { s =>
            for {
              ref    <- Ref.make(s)
              derived = ref.first
              value1 <- derived.get
              _      <- derived.set(value1)
              value2 <- ref.get
            } yield assert(value2)(equalTo(s))
          }
        },
        test("double set") {
          check(Gen.int.zip(Gen.int), Gen.int) { (s, a) =>
            for {
              ref    <- Ref.make(s)
              derived = ref.first
              _      <- derived.set(a)
              value1 <- ref.get
              _      <- derived.set(a)
              value2 <- ref.get
            } yield assert(value1)(equalTo(value2))
          }
        }
      ),
      suite("optional")(
        test("modifies matching field") {
          for {
            ref    <- Ref.make(Chunk(1, 2, 3, 4, 5))
            derived = ref.at(1)
            _      <- derived.update(_ * 10)
            value  <- ref.get
          } yield assert(value)(equalTo(Chunk(1, 20, 3, 4, 5)))
        }
      ),
      suite("prism")(
        test("set and get") {
          check(Gen.either(Gen.int, Gen.int), Gen.int) { (s, a) =>
            for {
              ref    <- Ref.make(s)
              derived = ref.left
              _      <- derived.set(a)
              value  <- derived.get
            } yield assert(value)(equalTo(a))
          }
        },
        test("get and set") {
          check(Gen.either(Gen.int, Gen.int)) { s =>
            for {
              ref    <- Ref.make(s)
              derived = ref.left
              _      <- derived.get.foldZIO(_ => ZIO.unit, derived.set)
              value  <- ref.get
            } yield assert(value)(equalTo(s))
          }
        }
      ),
      suite("traversal")(
        test("modifies matching fields") {
          for {
            ref    <- Ref.make(Chunk(1, 2, 3, 4, 5))
            derived = ref.filter(_ % 2 == 0)
            _      <- derived.update(_.map(_ * 10))
            value  <- ref.get
          } yield assert(value)(equalTo(Chunk(1, 20, 3, 40, 5)))
        }
      )
    ),
    suite("examples from documentation")(
      test("lens") {
        case class Person(name: String, age: Int)
        def age: Lens[Person, Int] =
          Lens(person => Right(person.age), age => person => Right(person.copy(age = age)))
        for {
          ref   <- Ref.make(Person("User", 42))
          _     <- ref.accessField(age).update(_ + 1)
          value <- ref.get
        } yield assert(value)(equalTo(Person("User", 43)))
      },
      test("prism") {
        for {
          ref   <- Ref.make[Either[List[String], Int]](Left(Nil))
          _     <- ref.left.update("fail" :: _)
          value <- ref.get
        } yield assert(value)(isLeft(equalTo(List("fail"))))
      },
      test("optional") {
        for {
          ref   <- Ref.make(Chunk(1, 2, 3))
          _     <- ref.at(2).set(4)
          value <- ref.get
        } yield assert(value)(equalTo(Chunk(1, 2, 4)))
      },
      test("traversal") {
        def negate(as: Chunk[Int]): Chunk[Int] =
          for (a <- as) yield -a
        for {
          ref   <- Ref.make(Chunk(0, 1, 2, 3, 4, 5, 6, 7, 8, 9))
          _     <- ref.slice(3, 6).update(negate)
          value <- ref.get
        } yield assert(value)(equalTo(Chunk(0, 1, 2, -3, -4, -5, 6, 7, 8, 9)))
      }
    )
  )
}
