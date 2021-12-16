package zio.optics

import zio._

package object opticsm
    extends OpticComposeModule
    with OpticFailureModule
    with OpticModule
    with OpticResultModule
    with OpticTypesModule {

  type OpticResult[+E, +A] = IO[E, A]

  protected def fail[E](e: E): IO[E, Nothing] =
    IO.fail(e)

  protected def flatMap[E, A, B](io: IO[E, A])(f: A => IO[E, B]): IO[E, B] =
    io.flatMap(f)

  protected def foldM[E, E2, A, B](io: IO[E, A])(f: E => IO[E2, B], g: A => IO[E2, B]): IO[E2, B] =
    io.foldZIO(f, g)

  protected def map[E, A, B](opticResult: OpticResult[E, A])(f: A => B): OpticResult[E, B] =
    opticResult.map(f)

  protected def succeed[A](a: A): IO[Nothing, A] =
    IO.succeed(a)

  /**
   * Providing implicit syntax for accessing pieces of a value in a `ZRefM`
   * when the error types are unified.
   */

  implicit class ERefMSyntax[RA, RB, EA >: EB, EB, A, B](private val self: ZRef.Synchronized[RA, RB, EA, EB, A, B]) {

    /**
     * Accesses some of the elements of a collection.
     */
    def accessElements[EC >: EA, ED >: EB, C, D](
      optic: Optic[B, B, Chunk[C], ED, EC, Chunk[D], A]
    ): ZRef.Synchronized[RA with RB, RB, EC, ED, Chunk[C], Chunk[D]] =
      self.foldAllZIO(
        identity,
        identity,
        identity,
        cs => b => optic.setOptic(cs)(b).mapError(_._1),
        b => optic.getOptic(b).mapError(_._1)
      )

    /**
     * Accesses a field of a product type.
     */
    def accessField[EC >: EA, ED >: EB, C, D](
      optic: Optic[B, B, C, ED, EC, D, A]
    ): ZRef.Synchronized[RA with RB, RB, EC, ED, C, D] =
      self.foldAllZIO(
        identity,
        identity,
        identity,
        c => b => optic.setOptic(c)(b).mapError(_._1),
        b => optic.getOptic(b).mapError(_._1)
      )
  }

  /**
   * Providing implicit syntax for accessing pieces of a value in a `ZRefM`.
   */
  implicit class ZRefMSyntax[RA, RB, EA, EB, A, B](private val self: ZRef.Synchronized[RA, RB, EA, EB, A, B]) {

    /**
     * Accesses a term of a sum type.
     */
    final def accessCase[EC >: EA, ED >: EB, C, D](optic: Optic[B, Any, C, ED, EC, D, A]): ZRef.Synchronized[RA, RB, EC, ED, C, D] =
      self.foldZIO(
        identity,
        identity,
        c => optic.setOptic(c)(()).mapError(_._1),
        d => optic.getOptic(d).mapError(_._1)
      )
  }

  /**
   * Provides implicit syntax for accessing the specified index of a chunk in a
   * `ZRefM`.
   */
  implicit class AtZRefMSyntax[RA, RB, EA >: EB, EB >: OpticFailure, A](
    private val self: ZRef.Synchronized[RA, RB, EA, EB, Chunk[A], Chunk[A]]
  ) {

    /**
     * Accesses the specified index of a chunk.
     */
    def at(n: Int): ZRef.Synchronized[RA with RB, RB, EA, EB, A, A] =
      self.accessField(Optic.at(n))
  }

  /**
   * Provides implicit syntax for accessing the `::` case of a `List` in a
   * `ZRefM`.
   */
  implicit class ConsZRefMSyntax[RA, RB, EA, EB >: OpticFailure, A](
    private val self: ZRef.Synchronized[RA, RB, EA, EB, List[A], List[A]]
  ) {

    /**
     * Accesses the `::` case of a `List`.
     */
    def cons: ZRef.Synchronized[RA, RB, EA, EB, (A, List[A]), (A, List[A])] =
      self.accessCase(Optic.cons)
  }

  /**
   * Provides implicit syntax for accessing a filtered subset of a chunk in a
   * `ZRefM`.
   */
  implicit class FilterZRefMSyntax[RA, RB, EA >: EB, EB >: OpticFailure, A](
    private val self: ZRef.Synchronized[RA, RB, EA, EB, Chunk[A], Chunk[A]]
  ) {

    /**
     * Accesses a filtered subset of a chunk.
     */
    def filter(f: A => Boolean): ZRef.Synchronized[RA with RB, RB, EA, EB, Chunk[A], Chunk[A]] =
      self.accessElements(Optic.filter(f))
  }

  /**
   * Provides implicit syntax for accessing the first element of a tuple in a
   * `ZRefM`.
   */
  implicit class FirstZRefMSyntax[RA, RB, EA >: EB, EB, A, B, C](
    private val self: ZRef.Synchronized[RA, RB, EA, EB, (C, B), (A, B)]
  ) {

    /**
     * Accesses the first element of a tuple.
     */
    def first: ZRef.Synchronized[RA with RB, RB, EA, EB, C, A] =
      self.accessField(Optic.first)
  }

  /**
   * Provides implicit syntax for accessing the head of a list in a `ZRefM`.
   */
  implicit class HeadZRefMSyntax[RA, RB, EA >: EB, EB >: OpticFailure, A](
    private val self: ZRef.Synchronized[RA, RB, EA, EB, List[A], List[A]]
  ) {

    /**
     * Accesses the head of a list.
     */
    def head: ZRef.Synchronized[RA with RB, RB, EA, EB, A, A] =
      self.accessField(Optic.head)
  }

  /**
   * Provides implicit syntax for accessing the value at the specified key in a
   * map in a `ZRefM`.
   */
  implicit class KeyZRefMSyntax[RA, RB, EA >: EB, EB >: OpticFailure, K, V](
    private val self: ZRef.Synchronized[RA, RB, EA, EB, Map[K, V], Map[K, V]]
  ) {

    /**
     * Accesses the value at the specified key in a map.
     */
    def key(k: K): ZRef.Synchronized[RA with RB, RB, EA, EB, V, V] =
      self.accessField(Optic.key(k))
  }

  /**
   * Provides implicit syntax for accessing the `Left` case of an `Either` in a
   * `ZRefM`.
   */
  implicit class LeftZRefMSyntax[RA, RB, EA, EB >: OpticFailure, A, B, C](
    private val self: ZRef.Synchronized[RA, RB, EA, EB, Either[C, B], Either[A, B]]
  ) {

    /**
     * Accesses the `Left` case of an `Either`.
     */
    def left: ZRef.Synchronized[RA, RB, EA, EB, C, A] =
      self.accessCase(Optic.left)
  }

  /**
   * Provides implicit syntax for accessing the `None` case of an `Option` in a
   * `ZRefM`.
   */
  implicit class NoneZRefMSyntax[RA, RB, EA, EB >: OpticFailure, A](
    private val self: ZRef.Synchronized[RA, RB, EA, EB, Option[A], Option[A]]
  ) {

    /**
     * Accesses the `None` case of an `Option`.
     */
    def none: ZRef.Synchronized[RA, RB, EA, EB, Unit, Unit] =
      self.accessCase(Optic.none)
  }

  /**
   * Provides implicit syntax for accessing the `Right` case of an `Either` in
   * a `ZRefM`.
   */
  implicit class RightZRefMSyntax[RA, RB, EA, EB >: OpticFailure, A, B, C](
    private val self: ZRef.Synchronized[RA, RB, EA, EB, Either[A, C], Either[A, B]]
  ) {

    /**
     * Accesses the `Right` case of an `Either`.
     */
    def right: ZRef.Synchronized[RA, RB, EA, EB, C, B] =
      self.accessCase(Optic.right)
  }

  /**
   * Provides implicit syntax for accessing the second element of a tuple in a
   * `ZRefM`.
   */
  implicit class SecondZRefMSyntax[RA, RB, EA >: EB, EB, A, B, C](
    private val self: ZRef.Synchronized[RA, RB, EA, EB, (A, C), (A, B)]
  ) {

    /**
     * Accesses the second element of a tuple.
     */
    def second: ZRef.Synchronized[RA with RB, RB, EA, EB, C, B] =
      self.accessField(Optic.second)
  }

  /**
   * Provides implicit syntax for accessing a slice of a chunk in a `ZRefM`.
   */
  implicit class SliceZRefMSyntax[RA, RB, EA >: EB, EB >: OpticFailure, A](
    private val self: ZRef.Synchronized[RA, RB, EA, EB, Chunk[A], Chunk[A]]
  ) {

    /**
     * Accesses a slice of a chunk.
     */
    def slice(from: Int, until: Int): ZRef.Synchronized[RA with RB, RB, EA, EB, Chunk[A], Chunk[A]] =
      self.accessElements(Optic.slice(from, until))
  }

  /**
   * Provides implicit syntax for accessing the `Some` case of an `Option` in a
   * `ZRefM`.
   */
  implicit class SomeZRefMSyntax[RA, RB, EA, EB >: OpticFailure, A, B](
    private val self: ZRef.Synchronized[RA, RB, EA, EB, Option[B], Option[A]]
  ) {

    /**
     * Accesses the `Some` case of an `Option`.
     */
    def some: ZRef.Synchronized[RA, RB, EA, EB, B, A] =
      self.accessCase(Optic.some)
  }

  /**
   * Provides implicit syntax for accessing the tail of a list in a `ZRefM`.
   */
  implicit class TailZRefMSyntax[RA, RB, EA >: EB, EB >: OpticFailure, A](
    private val self: ZRef.Synchronized[RA, RB, EA, EB, List[A], List[A]]
  ) {

    /**
     * Accesses the tail of a list.
     */
    def tail: ZRef.Synchronized[RA with RB, RB, EA, EB, List[A], List[A]] =
      self.accessField(Optic.tail)
  }
}
