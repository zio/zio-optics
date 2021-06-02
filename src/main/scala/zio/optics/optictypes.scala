package zio.optics

import zio._

trait OpticTypesModule {
  self: OpticComposeModule with OpticFailureModule with OpticModule with OpticResultModule =>

  type ZIso[-S, +T, +A, -B]       = Optic[S, Any, B, Nothing, Nothing, A, T]
  type ZLens[-S, +T, +A, -B]      = Optic[S, S, B, Nothing, Nothing, A, T]
  type ZOptional[-S, +T, +A, -B]  = Optic[S, S, B, OpticFailure, OpticFailure, A, T]
  type ZPrism[-S, +T, +A, -B]     = Optic[S, Any, B, OpticFailure, Nothing, A, T]
  type ZTraversal[-S, +T, +A, -B] = Optic[S, S, Chunk[B], OpticFailure, OpticFailure, Chunk[A], T]

  type Iso[S, A]       = ZIso[S, S, A, A]
  type Lens[S, A]      = ZLens[S, S, A, A]
  type Optional[S, A]  = ZOptional[S, S, A, A]
  type Prism[S, A]     = ZPrism[S, S, A, A]
  type Traversal[S, A] = ZTraversal[S, S, A, A]

  type Fold[-S, +A]   = Optic[S, Nothing, Nothing, OpticFailure, Any, Chunk[A], Any]
  type Getter[-S, +A] = Optic[S, Nothing, Nothing, OpticFailure, Any, A, Any]

  type ZSetter[-S, +T, -A] = Optic[Nothing, S, A, Any, OpticFailure, Any, T]
  type Setter[S, -A]       = ZSetter[S, S, A]

  type EOptic[-GetWhole, -SetWholeBefore, -SetPiece, +Error, +GetPiece, +SetWholeAfter] =
    Optic[GetWhole, SetWholeBefore, SetPiece, Error, Error, GetPiece, SetWholeAfter]

  type OpticPartiallyApplied[-SetPiece, +GetError, +SetError, +GetPiece, +SetWhole] =
    Optic[Any, Any, SetPiece, GetError, SetError, GetPiece, SetWhole]

  type ZIsoPartiallyApplied[+S, +A, -B]       = ZIso[Any, S, A, B]
  type ZLensPartiallyApplied[+S, +A, -B]      = ZLens[Any, S, A, B]
  type ZOptionalPartiallyApplied[+S, +A, -B]  = ZOptional[Any, S, A, B]
  type ZPrismPartiallyApplied[+S, +A, -B]     = ZPrism[Any, S, A, B]
  type ZTraversalPartiallyApplied[+S, +A, -B] = ZTraversal[Any, S, A, B]

  type IsoPartiallyApplied[+S, A]       = ZIsoPartiallyApplied[S, A, A]
  type LensPartiallyApplied[+S, A]      = ZLensPartiallyApplied[S, A, A]
  type OptionalPartiallyApplied[+S, A]  = ZOptionalPartiallyApplied[S, A, A]
  type PrismPartiallyApplied[+S, A]     = ZPrismPartiallyApplied[S, A, A]
  type TraversalPartiallyApplied[+S, A] = ZTraversalPartiallyApplied[S, A, A]

  object Lens {

    /**
     * Constructs a `Lens` from a `get` and a  `set` function.
     */
    def apply[S, A](get: S => OpticResult[Nothing, A], set: A => S => OpticResult[Nothing, S]): Lens[S, A] =
      ZLens(get, set)

    /**
     * An optic that accesses the first element of a tuple.
     */
    def first[A, B]: Lens[(A, B), A] =
      ZLens.first

    /**
     * An optic that accesses the second element of a tuple.
     */
    def second[A, B]: Lens[(A, B), B] =
      ZLens.second
  }

  object Optional {

    /**
     * Constructs an `Optional` from a `get` and a  `set` function.
     */
    def apply[S, A](
      get: S => OpticResult[OpticFailure, A],
      set: A => S => OpticResult[OpticFailure, S]
    ): Optional[S, A] =
      ZOptional(
        s => get(s).mapError((_, s)),
        a => s => set(a)(s).mapError((_, s))
      )

    /**
     * An optic that accesses the specified index of a chunk.
     */
    def at[A](n: Int): Optional[Chunk[A], A] =
      Optic.at(n)

    /**
     * An optic that accesses the head of a list.
     */
    def head[A]: Optional[List[A], A] =
      ZOptional.head

    /**
     * An optic that accesses the value at the specified key in a map.
     */
    def key[K, V](k: K): Optional[Map[K, V], V] =
      ZOptional.key(k)

    /**
     * An optic that accesses the tail of a list.
     */
    def tail[A]: Optional[List[A], List[A]] =
      ZOptional.tail
  }

  object Prism {

    /**
     * Constructs a `Prism` from a `get` and a `set` function.
     */
    def apply[S, A](get: S => OpticResult[OpticFailure, A], set: A => OpticResult[Nothing, S]): Prism[S, A] =
      ZPrism(s => get(s).mapError((_, s)), set)

    /**
     * An optic that accesses the `::` case of a `List`.
     */
    def cons[A, B]: Prism[List[A], (A, List[A])] =
      ZPrism.cons

    /**
     * An optic that accesses the `Left` case of an `Either`.
     */
    def left[A, B]: Prism[Either[A, B], A] =
      ZPrism.left

    /**
     * An optic that accesses the `None` case of an `Option`.
     */
    def none[A]: Prism[Option[A], Unit] =
      ZPrism.none

    /**
     * An optic that accesses the `Right` case of an `Either`.
     */
    def right[A, B]: Prism[Either[A, B], B] =
      ZPrism.right

    /**
     * An optic that accesses the `Some` case of an `Option`.
     */
    def some[A]: Prism[Option[A], A] =
      ZPrism.some
  }

  object Traversal {

    /**
     * Constructs an `Traversal` from a `get` and a  `set` function.
     */
    def apply[S, A](
      get: S => OpticResult[OpticFailure, Chunk[A]],
      set: Chunk[A] => S => OpticResult[OpticFailure, S]
    ): Traversal[S, A] =
      ZTraversal(s => get(s).mapError((_, s)), as => s => set(as)(s).mapError((_, s)))

    /**
     * An optic that accesses a filtered subset of a chunk.
     */
    def filter[A](f: A => Boolean): Traversal[Chunk[A], A] =
      ZTraversal.filter(f)

    /**
     * An optic that accesses a slice of a chunk.
     */
    def slice[A](from: Int, until: Int): Traversal[Chunk[A], A] =
      ZTraversal.slice(from, until)
  }

  object ZLens {

    /**
     * Constructs a `ZLens` from a `get` and a  `set` function.
     */
    def apply[S, T, A, B](
      get: S => OpticResult[Nothing, A],
      set: B => S => OpticResult[Nothing, T]
    ): ZLens[S, T, A, B] =
      Optic(get, set)

    /**
     * An optic that accesses the first element of a tuple.
     */
    def first[A, B, C]: ZLens[(A, B), (C, B), A, C] =
      Optic.first

    /**
     * An optic that accesses the second element of a tuple.
     */
    def second[A, B, C]: ZLens[(A, B), (A, C), B, C] =
      Optic.second
  }

  object ZOptional {

    /**
     * Constructs an optional from a `get` and a  `set` function.
     */
    def apply[S, T, A, B](
      get: S => OpticResult[(OpticFailure, T), A],
      set: B => S => OpticResult[(OpticFailure, T), T]
    ): ZOptional[S, T, A, B] =
      Optic(get, set)

    /**
     * An optic that accesses the specified index of a chunk.
     */
    def at[A](n: Int): Optional[Chunk[A], A] =
      Optic.at(n)

    /**
     * An optic that accesses the head of a list.
     */
    def head[A]: Optional[List[A], A] =
      Optic.head

    /**
     * An optic that accesses the value at the specified key in a map.
     */
    def key[K, V](k: K): Optional[Map[K, V], V] =
      Optic.key(k)

    /**
     * An optic that accesses the tail of a list.
     */
    def tail[A]: Optional[List[A], List[A]] =
      Optic.tail
  }

  object ZPrism {

    /**
     * Constructs a `ZPrism` from a `get` and a `set` function.
     */
    def apply[S, T, A, B](
      get: S => OpticResult[(OpticFailure, T), A],
      set: B => OpticResult[Nothing, T]
    ): ZPrism[S, T, A, B] =
      Optic(get, b => _ => set(b))

    /**
     * An optic that accesses the `::` case of a `List`.
     */
    def cons[A, B]: ZPrism[List[A], List[B], (A, List[A]), (B, List[B])] =
      Optic.cons

    /**
     * An optic that accesses the `Left` case of an `Either`.
     */
    def left[A, B, C]: ZPrism[Either[A, B], Either[C, B], A, C] =
      Optic.left

    /**
     * An optic that accesses the `None` case of an `Option`.
     */
    def none[A]: Prism[Option[A], Unit] =
      Optic.none

    /**
     * An optic that accesses the `Right` case of an `Either`.
     */
    def right[A, B, C]: ZPrism[Either[A, B], Either[A, C], B, C] =
      Optic.right

    /**
     * An optic that accesses the `Some` case of an `Option`.
     */
    def some[A, B]: ZPrism[Option[A], Option[B], A, B] =
      Optic.some
  }

  object ZTraversal {

    /**
     * Constructs an `ZTraversal` from a `get` and a  `set` function.
     */
    def apply[S, T, A, B](
      get: S => OpticResult[(OpticFailure, T), Chunk[A]],
      set: Chunk[B] => S => OpticResult[(OpticFailure, T), T]
    ): ZTraversal[S, T, A, B] =
      Optic(get, set)

    /**
     * An optic that accesses a filtered subset of a chunk.
     */
    def filter[A](f: A => Boolean): Traversal[Chunk[A], A] =
      Optic.filter(f)

    /**
     * An optic that accesses a slice of a chunk
     */
    def slice[A](from: Int, until: Int): Traversal[Chunk[A], A] =
      Optic.slice(from, until)
  }
}
