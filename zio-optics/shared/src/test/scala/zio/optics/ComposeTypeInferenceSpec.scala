package zio.optics

import zio.test._

object ComposeTypeInferenceSpec extends ZIOSpecDefault {

  def spec: ZSpec[Environment, Any] = suite("ComposeTypeInferenceSpec")(
    test("the composition of an iso and a prism is a prism") {
      lazy val a: ZIso[S, T, A, B]   = ???
      lazy val b: ZPrism[A, B, C, D] = ???
      lazy val _: ZPrism[S, T, C, D] = a >>> b
      assertCompletes
    },
    test("the composition of an iso and an iso is a iso") {
      lazy val a: ZIso[S, T, A, B] = ???
      lazy val b: ZIso[A, B, C, D] = ???
      lazy val _: ZIso[S, T, C, D] = a >>> b
      assertCompletes
    },
    test("the composition of an iso and a lens is a lens") {
      lazy val a: ZIso[S, T, A, B]  = ???
      lazy val b: ZLens[A, B, C, D] = ???
      lazy val _: ZLens[S, T, C, D] = a >>> b
      assertCompletes
    },
    test("the composition of an iso and an optional is an optional") {
      lazy val a: ZIso[S, T, A, B]      = ???
      lazy val b: ZOptional[A, B, C, D] = ???
      lazy val _: ZOptional[S, T, C, D] = a >>> b
      assertCompletes
    },
    test("the composition of a lens and an iso is a lens") {
      lazy val a: ZLens[S, T, A, B] = ???
      lazy val b: ZIso[A, B, C, D]  = ???
      lazy val _: ZLens[S, T, C, D] = a >>> b
      assertCompletes
    },
    test("the composition of a lens and a lens is a lens") {
      lazy val a: ZLens[S, T, A, B] = ???
      lazy val b: ZLens[A, B, C, D] = ???
      lazy val _: ZLens[S, T, C, D] = a >>> b
      assertCompletes
    },
    test("the composition of a lens and an optional is an optional") {
      lazy val a: ZLens[S, T, A, B]     = ???
      lazy val b: ZOptional[A, B, C, D] = ???
      lazy val _: ZOptional[S, T, C, D] = a >>> b
      assertCompletes
    },
    test("the composition of a lens and a prism is an optional") {
      lazy val a: ZLens[S, T, A, B]     = ???
      lazy val b: ZPrism[A, B, C, D]    = ???
      lazy val _: ZOptional[S, T, C, D] = a >>> b
      assertCompletes
    },
    test("the composition of an optional and an iso is an optional") {
      lazy val a: ZOptional[S, T, A, B] = ???
      lazy val b: ZIso[A, B, C, D]      = ???
      lazy val _: ZOptional[S, T, C, D] = a >>> b
      assertCompletes
    },
    test("the composition of an optional and a lens is an optional") {
      lazy val a: ZOptional[S, T, A, B] = ???
      lazy val b: ZLens[A, B, C, D]     = ???
      lazy val _: ZOptional[S, T, C, D] = a >>> b
      assertCompletes
    },
    test("the composition of an optional and an optional is an optional") {
      lazy val a: ZOptional[S, T, A, B] = ???
      lazy val b: ZOptional[A, B, C, D] = ???
      lazy val _: ZOptional[S, T, C, D] = a >>> b
      assertCompletes
    },
    test("the composition of an optional and a prism is an optional") {
      lazy val a: ZOptional[S, T, A, B] = ???
      lazy val b: ZPrism[A, B, C, D]    = ???
      lazy val _: ZOptional[S, T, C, D] = a >>> b
      assertCompletes
    },
    test("the composition of a traversal and a lens is a traversal") {
      lazy val a: ZTraversal[S, T, A, B] = ???
      lazy val b: ZLens[A, B, C, D]      = ???
      lazy val _: ZTraversal[S, T, C, D] = a.foreach(b)
      assertCompletes
    },
    test("the composition of an optional and a traversal is a traversal") {
      assertCompletes
    },
    test("the composition of a prism and an iso is a prism") {
      lazy val a: ZPrism[S, T, A, B] = ???
      lazy val b: ZIso[A, B, C, D]   = ???
      lazy val _: ZPrism[S, T, C, D] = a >>> b
      assertCompletes
    },
    test("the composition of a prism and a lens is an optional") {
      lazy val a: ZPrism[S, T, A, B]    = ???
      lazy val b: ZLens[A, B, C, D]     = ???
      lazy val _: ZOptional[S, T, C, D] = a >>> b
      assertCompletes
    },
    test("the composition of a prism and an optional is a optional") {
      lazy val a: ZPrism[S, T, A, B]    = ???
      lazy val b: ZOptional[A, B, C, D] = ???
      lazy val _: ZOptional[S, T, C, D] = a >>> b
      assertCompletes
    },
    test("the composition of a prism and a prism is a prism") {
      lazy val a: ZPrism[S, T, A, B] = ???
      lazy val b: ZPrism[A, B, C, D] = ???
      lazy val _: ZPrism[S, T, C, D] = a >>> b
      assertCompletes
    }
  )

  trait S
  trait T
  trait A
  trait B
  trait C
  trait D
}
