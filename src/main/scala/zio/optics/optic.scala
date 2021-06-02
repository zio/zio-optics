package zio.optics

import zio._

trait OpticModule {
  self: OpticComposeModule with OpticFailureModule with OpticResultModule with OpticTypesModule =>

  /**
   * An `Optic` is able to get and set a piece of a whole, possibly failing. In
   * the most general possible case the get and set types are distinct and
   * getting may fail with a different error than setting.
   */
  case class Optic[-GetWhole, -SetWholeBefore, -SetPiece, +GetError, +SetError, +GetPiece, +SetWholeAfter](
    getOptic: GetWhole => OpticResult[(GetError, SetWholeAfter), GetPiece],
    setOptic: SetPiece => SetWholeBefore => OpticResult[(SetError, SetWholeAfter), SetWholeAfter]
  ) { self =>

    /**
     * Applies this optic to the specified whole, returning a new optic that
     * no longer needs a whole to get and set and always gets and sets a piece
     * of the specified whole.
     */
    final def apply(
      whole: GetWhole with SetWholeBefore
    ): OpticPartiallyApplied[SetPiece, GetError, SetError, GetPiece, SetWholeAfter] =
      Optic(
        _ => self.getOptic(whole),
        piece => _ => self.setOptic(piece)(whole)
      )

    /**
     * A symbolic alias for `zip`.
     */
    final def <*>[
      GetWhole1 <: GetWhole,
      SetWholeBefore1 >: SetWholeAfter <: SetWholeBefore,
      SetPiece2,
      GetError1 >: GetError,
      SetError1 >: SetError,
      GetPiece2,
      SetWholeAfter1 >: SetWholeAfter
    ](
      that: Optic[GetWhole1, SetWholeBefore1, SetPiece2, GetError1, SetError1, GetPiece2, SetWholeAfter1]
    ): Optic[
      GetWhole1,
      SetWholeBefore1,
      (SetPiece, SetPiece2),
      GetError1,
      SetError1,
      (GetPiece, GetPiece2),
      SetWholeAfter1
    ] =
      self.zip(that)

    /**
     * A symbolic alias for `orElse`.
     */
    final def <>[
      GetWhole1 <: GetWhole,
      SetWholeBefore1 <: SetWholeBefore,
      SetPiece1 <: SetPiece,
      GetError2,
      SetError2,
      GetPiece1 >: GetPiece,
      SetWholeAfter1 >: SetWholeAfter
    ](
      that: => Optic[GetWhole1, SetWholeBefore1, SetPiece1, GetError2, SetError2, GetPiece1, SetWholeAfter1]
    ): Optic[GetWhole1, SetWholeBefore1, SetPiece1, GetError2, SetError2, GetPiece1, SetWholeAfter1] =
      self.orElse(that)

    /**
     * Gets a piece of the specified whole.
     */
    def get(whole: GetWhole): OpticResult[GetError, GetPiece] =
      getOptic(whole).mapError(_._1)

    /**
     * Constructs a new optic that attempts to get and set with this optic, but
     * if getting or setting fails falls back to getting or setting with that
     * optic.
     */
    final def orElse[
      GetWhole1 <: GetWhole,
      SetWholeBefore1 <: SetWholeBefore,
      SetPiece1 <: SetPiece,
      GetError2,
      SetError2,
      GetPiece1 >: GetPiece,
      SetWholeAfter1 >: SetWholeAfter
    ](
      that: => Optic[GetWhole1, SetWholeBefore1, SetPiece1, GetError2, SetError2, GetPiece1, SetWholeAfter1]
    ): Optic[GetWhole1, SetWholeBefore1, SetPiece1, GetError2, SetError2, GetPiece1, SetWholeAfter1] =
      Optic(
        whole => self.getOptic(whole).orElse(that.getOptic(whole)),
        piece => whole => self.setOptic(piece)(whole).orElse(that.setOptic(piece)(whole))
      )

    /**
     * Constructs a new optic that gets and sets with both this optic and that
     * optic. This optic and that optic must get and set different pieces of
     * the whole.
     */
    final def zip[
      GetWhole1 <: GetWhole,
      SetWholeBefore1 >: SetWholeAfter <: SetWholeBefore,
      SetPiece2,
      GetError1 >: GetError,
      SetError1 >: SetError,
      GetPiece2,
      SetWholeAfter1 >: SetWholeAfter
    ](
      that: Optic[GetWhole1, SetWholeBefore1, SetPiece2, GetError1, SetError1, GetPiece2, SetWholeAfter1]
    ): Optic[
      GetWhole1,
      SetWholeBefore1,
      (SetPiece, SetPiece2),
      GetError1,
      SetError1,
      (GetPiece, GetPiece2),
      SetWholeAfter1
    ] =
      Optic(
        whole => self.getOptic(whole).zip(that.getOptic(whole)),
        piece => whole => self.setOptic(piece._1)(whole).flatMap(whole => (that.setOptic(piece._2)(whole)))
      )
  }

  object Optic {

    /**
     * An optic that accesses the specified index of a chunk.
     */
    def at[A](n: Int): Optional[Chunk[A], A] =
      ZOptional(
        s =>
          if (0 <= n && n < s.length) succeed(s(n))
          else fail((OpticFailure(s"$s did not satisfy hasAt($n"), s)),
        a =>
          s =>
            if (0 <= n && n < s.length) succeed(s.updated(n, a))
            else fail((OpticFailure(s"$s did not satisfy hasAt($n"), s))
      )

    /**
     * An optic that accesses the `::` case of a `List`.
     */
    def cons[A, B]: ZPrism[List[A], List[B], (A, List[A]), (B, List[B])] =
      ZPrism(
        {
          case h :: t => succeed((h, t))
          case Nil    => fail((OpticFailure("Nil did not satisfy isCons"), Nil))
        },
        { case (h, t) => succeed(h :: t) }
      )

    /**
     * An optic that accesses a filtered subset of a chunk.
     */
    def filter[A](f: A => Boolean): Traversal[Chunk[A], A] =
      Traversal(
        s => succeed(Chunk.fromIterable(s.filter(f))),
        as =>
          s => {
            val builder       = ChunkBuilder.make[A]
            val leftIterator  = s.iterator
            val rightIterator = as.iterator
            while (leftIterator.hasNext && rightIterator.hasNext) {
              val left = leftIterator.next()
              if (f(left)) {
                val right = rightIterator.next()
                builder += right
              } else {
                builder += left
              }
            }
            while (leftIterator.hasNext) {
              val left = leftIterator.next()
              builder += left
            }
            succeed(builder.result())
          }
      )

    /**
     * An optic that accesses the first element of a tuple.
     */
    def first[A, B, C]: ZLens[(A, B), (C, B), A, C] =
      ZLens(s => succeed(s._1), c => s => succeed(s.copy(_1 = c)))

    /**
     * An optic that accesses the head of a list.
     */
    def head[A]: Optional[List[A], A] =
      cons >>> first

    /**
     * An optic that accesses the value at the specified key in a map.
     */
    def key[K, V](k: K): Optional[Map[K, V], V] =
      Optional(
        map =>
          map.get(k) match {
            case Some(v) => succeed(v)
            case None    => fail(OpticFailure(s"$map did not satisfy hasKey($k)"))
          },
        v =>
          map =>
            map.get(k) match {
              case Some(_) => succeed(map + (k -> v))
              case None    => fail(OpticFailure(s"$map did not satisfy hasKey($k)"))
            }
      )

    /**
     * An optic that accesses the `Left` case of an `Either`.
     */
    def left[A, B, C]: ZPrism[Either[A, B], Either[C, B], A, C] =
      ZPrism(
        {
          case Left(a)  => succeed(a)
          case Right(b) => fail((OpticFailure(s"Right($b) did not satisfy isLeft"), Right(b)))
        },
        c => succeed(Left(c))
      )

    /**
     * An optic that accesses the `None` case of an `Option`.
     */
    def none[A]: Prism[Option[A], Unit] =
      Prism(
        {
          case Some(a) => fail(OpticFailure(s"Some($a) did not satisfy isNone"))
          case None    => succeed(())
        },
        _ => succeed(None)
      )

    /**
     * An optic that accesses the `Right` case of an `Either`.
     */
    def right[A, B, C]: ZPrism[Either[A, B], Either[A, C], B, C] =
      ZPrism(
        {
          case Right(b) => succeed(b)
          case Left(a)  => fail((OpticFailure(s"Left($a) did not satisfy isRight"), Left(a)))

        },
        c => succeed(Right(c))
      )

    /**
     * An optic that accesses the second element of a tuple.
     */
    def second[A, B, C]: ZLens[(A, B), (A, C), B, C] =
      ZLens(s => succeed(s._2), c => s => succeed(s.copy(_2 = c)))

    /**
     * An optic that accesses a slice of a chunk.
     */
    def slice[A](from: Int, until: Int): Traversal[Chunk[A], A] =
      Traversal(
        s => succeed(s.slice(from, until)),
        as => s => succeed(s.patch(from, as, until - from))
      )

    /**
     * An optic that accesses the `Some` case of an `Option`.
     */
    def some[A, B]: ZPrism[Option[A], Option[B], A, B] =
      ZPrism(
        {
          case Some(a) => succeed(a)
          case None    => fail((OpticFailure(s"None did not satisfy isSome"), None))
        },
        b => succeed(Some(b))
      )

    /**
     * An optic that accesses the tail of a list.
     */
    def tail[A]: Optional[List[A], List[A]] =
      cons >>> second

    /**
     * Provides implicit syntax for when the error types of getting and
     * setting are unified.
     */
    implicit class EOpticSyntax[GetWhole, SetWholeBefore, SetPiece, Error, GetPiece, SetWholeAfter](
      private val self: EOptic[GetWhole, SetWholeBefore, SetPiece, Error, GetPiece, SetWholeAfter]
    ) {

      /**
       * Updates the specified whole by transforming a piece of it using the
       * function `f`.
       */
      def update(whole: GetWhole with SetWholeBefore)(f: GetPiece => SetPiece): OpticResult[Error, SetWholeAfter] =
        self.getOptic(whole).flatMap(piece => self.setOptic(f(piece))(whole)).mapError(_._1)
    }

    /**
     * Provides implicit syntax for when the whole is required to set.
     */
    implicit class LensSyntax[GetWhole, SetWholeBefore, SetPiece, GetError, SetError, GetPiece, SetWholeAfter](
      private val self: Optic[GetWhole, SetWholeBefore, SetPiece, GetError, SetError, GetPiece, SetWholeAfter]
    ) {

      /**
       * Updates the specified whole by transforming a piece of it using the
       * function `f`.
       */
      def set(piece: SetPiece)(whole: GetWhole with SetWholeBefore): OpticResult[SetError, SetWholeAfter] =
        self.setOptic(piece)(whole).mapError(_._1)
    }

    /**
     * Provides implicit syntax for when thewhole is not required to set.
     */
    implicit class PrismSyntax[GetWhole, SetPiece, GetError, SetError, GetPiece, SetWholeAfter](
      private val self: Optic[GetWhole, Any, SetPiece, GetError, SetError, GetPiece, SetWholeAfter]
    ) {

      /**
       * Updates the specified whole by transforming a piece of it using the
       * function `f`.
       */
      def set(piece: SetPiece): OpticResult[SetError, SetWholeAfter] =
        self.setOptic(piece)(()).mapError(_._1)
    }

    /**
     * Provides implicit syntax for composing optics.
     */
    implicit class ComposeSyntax[GetWhole, SetWholeBefore, SetPiece, GetError, SetError, GetPiece, SetWholeAfter](
      private val self: Optic[GetWhole, SetWholeBefore, SetPiece, GetError, SetError, GetPiece, SetWholeAfter]
    ) {

      /**
       * A symbolic alias for `andThen`.
       */
      final def >>>[
        SetWholeBefore1,
        SetPiece1,
        GetError1 >: GetError,
        GetPiece1,
        SetError1 >: SetError,
        SetWholeBefore2
      ](
        that: Optic[GetPiece, SetWholeBefore1, SetPiece1, GetError1, SetError1, GetPiece1, SetPiece]
      )(implicit
        ev: OpticCompose[
          GetWhole,
          SetWholeBefore,
          SetWholeBefore1,
          SetWholeBefore2,
          GetError,
          SetError,
          SetError1,
          GetPiece
        ]
      ): Optic[GetWhole, SetWholeBefore2, SetPiece1, GetError1, SetError1, GetPiece1, SetWholeAfter] =
        andThen(that)

      /**
       * Composes this optic with that to return a new optic that accesses a
       * piece of the piece accessed by this optic.
       */
      final def andThen[
        SetWholeBefore1,
        SetPiece1,
        GetError1 >: GetError,
        GetPiece1,
        SetError1 >: SetError,
        SetWholeBefore2
      ](
        that: Optic[GetPiece, SetWholeBefore1, SetPiece1, GetError1, SetError1, GetPiece1, SetPiece]
      )(implicit
        ev: OpticCompose[
          GetWhole,
          SetWholeBefore,
          SetWholeBefore1,
          SetWholeBefore2,
          GetError,
          SetError,
          SetError1,
          GetPiece
        ]
      ): Optic[GetWhole, SetWholeBefore2, SetPiece1, GetError1, SetError1, GetPiece1, SetWholeAfter] =
        ev.compose(self, that)
    }

    /**
     * Provides implicit syntax for applying an optic to each element of a
     * collection accessed by another optic.
     */
    implicit class ForEachSyntax[
      GetWhole,
      SetWholeBefore <: GetWhole,
      SetPiece,
      GetError >: SetError,
      SetError,
      SetError1 >: GetError,
      GetPiece,
      SetWholeAfter
    ](
      private val left: Optic[GetWhole, SetWholeBefore, Chunk[SetPiece], GetError, SetError, Chunk[
        GetPiece
      ], SetWholeAfter]
    ) {

      /**
       * Accesses a piece of each element accessed by this optic using the specified optic.
       */
      final def foreach[SetPiece1, GetError1, SetError2, GetPiece1, SetWholeAfter1](
        right: Optic[GetPiece, GetPiece, SetPiece1, GetError1, SetError2, GetPiece1, SetPiece]
      ): Optic[GetWhole, SetWholeBefore, Chunk[SetPiece1], GetError, SetError1, Chunk[GetPiece1], SetWholeAfter] =
        Optic(
          getWhole => left.getOptic(getWhole).flatMap(pieces => collectAllSuccesses(pieces.map(right.getOptic))),
          setPieces =>
            setWholeBefore =>
              left
                .getOptic(setWholeBefore)
                .foldM(
                  { case (getError, setWholeAfter) => fail((getError, setWholeAfter)) },
                  getPieces =>
                    self
                      .foreach(getPieces.zip(setPieces)) { case (getPiece, setPiece) =>
                        right
                          .setOptic(setPiece)(getPiece)
                          .foldM(
                            { case (_, setWholeAfter) => succeed(setWholeAfter) },
                            setPiece => succeed(setPiece)
                          )
                      }
                      .flatMap(bs => left.setOptic(bs)(setWholeBefore))
                )
        )
    }
  }

  /**
   * Provides implicit syntax for working with partially applied optics.
   */
  implicit class PariallyAppliedOpticSyntax[SetPiece, Error, GetPiece, SetWholeAfter](
    private val self: Optic[Any, Any, SetPiece, Error, Error, GetPiece, SetWholeAfter]
  ) {

    /**
     * Updates the whole contained in this partially applied optic by
     * transforming a piece of it using the specified function.
     */
    def update(f: GetPiece => SetPiece): OpticResult[Error, SetWholeAfter] =
      self.getOptic(()).flatMap(piece => self.setOptic(f(piece))(())).mapError(_._1)
  }

  /**
   * Provides implicit syntax for accessing the specified index of a chunk
   * accessed by a partially applied optic.
   */
  implicit class AtPartiallyAppliedSyntax[
    GetError >: OpticFailure,
    SetError >: GetError,
    Piece,
    Whole
  ](
    private val self: Optic[
      Any,
      Any,
      Chunk[Piece],
      GetError,
      SetError,
      Chunk[Piece],
      Whole
    ]
  ) {

    /**
     * Accesses the specified index of a chunk.
     */
    final def at(n: Int): Optic[Any, Any, Piece, GetError, SetError, Piece, Whole] =
      self >>> Optic.at(n)
  }

  /**
   * Provides implicit syntax for accessing the `::` case of a `List` accessed
   * by a partially applied optic.
   */
  implicit class ConsPartiallyAppliedSyntax[
    SetPiece,
    GetError >: OpticFailure,
    SetError,
    GetPiece,
    SetWhole
  ](
    private val self: Optic[
      Any,
      Any,
      List[SetPiece],
      GetError,
      SetError,
      List[GetPiece],
      SetWhole
    ]
  ) {

    /**
     * Accesses the `::` case of a `List`.
     */
    final def cons
      : Optic[Any, Any, (SetPiece, List[SetPiece]), GetError, SetError, (GetPiece, List[GetPiece]), SetWhole] =
      self >>> Optic.cons
  }

  /**
   * Provides implicit syntax for accessing a filtered subset of a chunk
   * accessed by a partially applied optic.
   */
  implicit class FilterPartiallyAppliedSyntax[
    GetError >: OpticFailure,
    SetError >: GetError,
    Piece,
    Whole
  ](
    private val self: Optic[
      Any,
      Any,
      Chunk[Piece],
      GetError,
      SetError,
      Chunk[Piece],
      Whole
    ]
  ) {

    /**
     * Accesses a filtered subset of a chunk.
     */
    final def filter(f: Piece => Boolean): Optic[Any, Any, Chunk[Piece], GetError, SetError, Chunk[Piece], Whole] =
      self >>> Optic.filter(f)
  }

  /**
   * Provides implicit syntax for accessing the first element of a tuple
   * accessed by a partially applied optic.
   */
  implicit class FirstPartiallyAppliedSyntax[
    SetPiece,
    GetError,
    SetError >: GetError,
    GetPiece,
    Whole,
    Piece2
  ](
    private val self: Optic[
      Any,
      Any,
      (SetPiece, Piece2),
      GetError,
      SetError,
      (GetPiece, Piece2),
      Whole
    ]
  ) {

    /**
     * Accesses the first element of a tuple.
     */
    final def first: Optic[Any, Any, SetPiece, GetError, SetError, GetPiece, Whole] =
      self >>> Optic.first
  }

  /**
   * Provides implicit syntax for accessing the head of a list accessed by a
   * partially applied optic.
   */
  implicit class HeadPartiallyAppliedSyntax[
    GetError >: OpticFailure,
    SetError >: GetError,
    Piece,
    Whole
  ](
    private val self: Optic[
      Any,
      Any,
      List[Piece],
      GetError,
      SetError,
      List[Piece],
      Whole
    ]
  ) {

    /**
     * Accesses the head of a list.
     */
    final def head: Optic[Any, Any, Piece, GetError, SetError, Piece, Whole] =
      self >>> Optic.head
  }

  /**
   * Provides implicit syntax for accessing the value at the specified key in a
   * map accessed by a partially applied optic.
   */
  implicit class KeyPartiallyAppliedSyntax[
    GetError >: OpticFailure,
    SetError >: GetError,
    Key,
    Value,
    Whole
  ](
    private val self: Optic[
      Any,
      Any,
      Map[Key, Value],
      GetError,
      SetError,
      Map[Key, Value],
      Whole
    ]
  ) {

    /**
     * Accesses the value at the specified key in a map.
     */
    final def key(k: Key): Optic[Any, Any, Value, GetError, SetError, Value, Whole] =
      self >>> Optic.key(k)
  }

  /**
   * Provides implicit syntax for accessing the `Left` case of an `Either`
   * accessed by a partially applied optic.
   */
  implicit class LeftPartiallyAppliedSyntax[
    SetPiece,
    GetError >: OpticFailure,
    SetError,
    GetPiece,
    SetWhole,
    Piece2
  ](
    private val self: Optic[
      Any,
      Any,
      Either[SetPiece, Piece2],
      GetError,
      SetError,
      Either[GetPiece, Piece2],
      SetWhole
    ]
  ) {

    /**
     * Accesses the `Left` case of an `Either`.
     */
    final def left: Optic[Any, Any, SetPiece, GetError, SetError, GetPiece, SetWhole] =
      self >>> Optic.left
  }

  /**
   * Provides implicit syntax for accessing the `None` case of an `Option`
   * accessed by a partially applied optic.
   */
  implicit class NonePartiallyAppliedSyntax[GetError >: OpticFailure, SetError, Piece, SetWhole](
    private val self: OpticPartiallyApplied[Option[Piece], GetError, SetError, Option[Piece], SetWhole]
  ) {

    /**
     * Accesses the `None` case of an `Option`.
     */
    final def none: Optic[Any, Any, Unit, GetError, SetError, Unit, SetWhole] =
      self >>> Optic.none
  }

  /**
   * Provides implicit syntax for accessing the `Right` case of an `Either`
   * accessed by a partially applied optic.
   */
  implicit class RightPartiallyAppliedSyntax[
    SetPiece,
    GetError >: OpticFailure,
    SetError,
    GetPiece,
    SetWhole,
    Piece2
  ](
    private val self: OpticPartiallyApplied[
      Either[Piece2, SetPiece],
      GetError,
      SetError,
      Either[Piece2, GetPiece],
      SetWhole
    ]
  ) {

    /**
     * Accesses the `Right` case of an `Either`.
     */
    final def right: Optic[Any, Any, SetPiece, GetError, SetError, GetPiece, SetWhole] =
      self >>> Optic.right
  }

  /**
   * Provides implicit syntax for accessing the second element of a tuple
   * accessed by a partially applied optic.
   */
  implicit class SecondPartiallyAppliedSyntax[
    SetPiece,
    GetError,
    SetError >: GetError,
    GetPiece,
    Whole,
    Piece2
  ](
    private val self: Optic[
      Any,
      Any,
      (Piece2, SetPiece),
      GetError,
      SetError,
      (Piece2, GetPiece),
      Whole
    ]
  ) {

    /**
     * Accesses the second element of a tuple.
     */
    final def second: Optic[Any, Any, SetPiece, GetError, SetError, GetPiece, Whole] =
      self >>> Optic.second
  }

  /**
   * Provides implicit syntax for accessing a slice of a chunk accessed by a
   * partially applied optic.
   */
  implicit class SlicePartiallyAppliedSyntax[
    GetError >: OpticFailure,
    SetError >: GetError,
    Piece,
    Whole
  ](
    private val self: Optic[
      Any,
      Any,
      Chunk[Piece],
      GetError,
      SetError,
      Chunk[Piece],
      Whole
    ]
  ) {

    /**
     * Accesses a slice of a chunk.
     */
    final def slice(from: Int, until: Int): Optic[Any, Any, Chunk[Piece], GetError, SetError, Chunk[Piece], Whole] =
      self >>> Optic.slice(from, until)
  }

  /**
   * Provides implicit syntax for accessing the `Some` case of an `Option`
   * accessed by a partially applied optic.
   */
  implicit class SomePartiallyAppliedSyntax[SetPiece, GetError >: OpticFailure, SetError, GetPiece, SetWhole](
    private val self: OpticPartiallyApplied[Option[SetPiece], GetError, SetError, Option[GetPiece], SetWhole]
  ) {

    /**
     * Accesses the `Some` case of an `Option`.
     */
    final def some: Optic[Any, Any, SetPiece, GetError, SetError, GetPiece, SetWhole] =
      self >>> Optic.some
  }

  /**
   * Provides implicit syntax for accessing the tail of a list accessed by a
   * partially applied optic.
   */
  implicit class TailPartiallyAppliedSyntax[
    GetError >: OpticFailure,
    SetError >: GetError,
    Piece,
    Whole
  ](
    private val self: Optic[
      Any,
      Any,
      List[Piece],
      GetError,
      SetError,
      List[Piece],
      Whole
    ]
  ) {

    /**
     * Accesses the tail of a list.
     */
    final def tail: Optic[Any, Any, List[Piece], GetError, SetError, List[Piece], Whole] =
      self >>> Optic.tail
  }
}
