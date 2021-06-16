package zio.optics

import zio.test._

object OpticSubtypingSpec extends DefaultRunnableSpec {

  def spec: ZSpec[Environment, Failure] = suite("SubtypingSpec")(
    test("an isomorphism is a lens")(isSubtypeOf[ZIso[S, T, A, B], ZLens[S, T, A, B]]),
    test("an isomorphism is a prism")(isSubtypeOf[ZIso[S, T, A, B], ZPrism[S, T, A, B]]),
    test("a lens is an optional")(isSubtypeOf[ZLens[S, T, A, B], ZOptional[S, T, A, B]]),
    test("an optional is a getter")(isSubtypeOf[ZOptional[S, T, A, B], Getter[S, A]]),
    test("an optional is a setter")(isSubtypeOf[ZOptional[S, T, A, B], ZSetter[S, T, B]]),
    test("a prism is an optional")(isSubtypeOf[ZPrism[S, T, A, B], ZOptional[S, T, A, B]]),
    test("a traversal is a fold")(isSubtypeOf[ZTraversal[S, T, A, B], Fold[S, A]])
  )

  def isSubtypeOf[A, B](implicit ev: A <:< B): TestResult = {
    val _ = ev
    assertCompletes
  }

  trait S
  trait T
  trait A
  trait B
}
