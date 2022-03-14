package zio.optics

import zio._
import zio.stm._

package object toptics
    extends OpticComposeModule
    with OpticFailureModule
    with OpticModule
    with OpticResultModule
    with OpticTypesModule {

  type OpticResult[+E, +A] = STM[E, A]

  protected def fail[E](e: E): STM[E, Nothing] =
    STM.fail(e)

  protected def flatMap[E, A, B](stm: STM[E, A])(f: A => STM[E, B]): STM[E, B] =
    stm.flatMap(f)

  protected def foldM[E, E2, A, B](stm: STM[E, A])(f: E => STM[E2, B], g: A => STM[E2, B]): STM[E2, B] =
    stm.foldSTM(f, g)

  protected def map[E, A, B](opticResult: OpticResult[E, A])(f: A => B): OpticResult[E, B] =
    opticResult.map(f)

  protected def succeed[A](a: A): STM[Nothing, A] =
    STM.succeed(a)

  /**
   * Provides implicit syntax accessing the value at the specified key in a
   * `TMap`.
   */
  final implicit class TMapOpticsSyntax[K, V](private val map: TMap[K, V]) {

    /**
     * Accesses the value at the specified key.
     */
    def key(k: K): OptionalPartiallyApplied[TMap[K, V], V] =
      TOptics.key(k)(map)
  }

  /**
   * Providing implicit syntax for accessing pieces of a value in a `ZTRef`
   * when the error types are unified.
   */
  implicit class ETRefSyntax[EA >: EB, EB, A, B](private val self: ZTRef[EA, EB, A, B]) {

    /**
     * Accesses some of the elements of a collection.
     */
    def accessElements[EC >: EA, ED >: EB, C, D](
      optic: Optic[B, B, Chunk[C], ED, EC, Chunk[D], A]
    ): ZTRef[EC, ED, Chunk[C], Chunk[D]] =
      self.foldAllSTM(
        identity,
        identity,
        identity,
        cs => b => optic.setOptic(cs)(b).mapError(_._1),
        b => optic.getOptic(b).mapError(_._1)
      )

    /**
     * Accesses a field of a product type.
     */
    def accessField[EC >: EA, ED >: EB, C, D](optic: Optic[B, B, C, ED, EC, D, A]): ZTRef[EC, ED, C, D] =
      self.foldAllSTM(
        identity,
        identity,
        identity,
        c => b => optic.setOptic(c)(b).mapError(_._1),
        b => optic.getOptic(b).mapError(_._1)
      )
  }

  /**
   * Providing implicit syntax for accessing pieces of a value in a `ZTRef`.
   */
  implicit class ZTRefSyntax[EA, EB, A, B](private val self: ZTRef[EA, EB, A, B]) {

    /**
     * Accesses a term of a sum type.
     */
    final def accessCase[EC >: EA, ED >: EB, C, D](optic: Optic[B, Any, C, ED, EC, D, A]): ZTRef[EC, ED, C, D] =
      self.foldSTM(
        identity,
        identity,
        c => optic.setOptic(c)(()).mapError(_._1),
        d => optic.getOptic(d).mapError(_._1)
      )
  }

  /**
   * Provides implicit syntax for accessing the specified index of a chunk in a
   * `ZTRef`.
   */
  implicit class AtZTRefSyntax[EA >: EB, EB >: OpticFailure, A](private val self: ZTRef[EA, EB, Chunk[A], Chunk[A]]) {

    /**
     * Accesses the specified index of a chunk.
     */
    def at(n: Int): ZTRef[EA, EB, A, A] =
      self.accessField(Optic.at(n))
  }

  /**
   * Provides implicit syntax for accessing the `::` case of a `List` in a
   * `ZTRef`.
   */
  implicit class ConsZTRefSyntax[EA, EB >: OpticFailure, A](private val self: ZTRef[EA, EB, List[A], List[A]]) {

    /**
     * Accesses the `::` case of a `List`.
     */
    def cons: ZTRef[EA, EB, (A, List[A]), (A, List[A])] =
      self.accessCase(Optic.cons)
  }

  /**
   * Provides implicit syntax for accessing a filtered subset of a chunk in a
   * `ZTRef`.
   */
  implicit class FilterZTRefSyntax[EA >: EB, EB >: OpticFailure, A](
    private val self: ZTRef[EA, EB, Chunk[A], Chunk[A]]
  ) {

    /**
     * Accesses a filtered subset of a chunk.
     */
    def filter(f: A => Boolean): ZTRef[EA, EB, Chunk[A], Chunk[A]] =
      self.accessElements(Optic.filter(f))
  }

  /**
   * Provides implicit syntax for accessing the first element of a tuple in a
   * `ZTRef`.
   */
  implicit class FirstZTRefSyntax[EA >: EB, EB, A, B, C](private val self: ZTRef[EA, EB, (C, B), (A, B)]) {

    /**
     * Accesses the first element of a tuple.
     */
    def first: ZTRef[EA, EB, C, A] =
      self.accessField(Optic.first)
  }

  /**
   * Provides implicit syntax for accessing the head of a list in a `ZTRef`.
   */
  implicit class HeadZTRefSyntax[EA >: EB, EB >: OpticFailure, A](private val self: ZTRef[EA, EB, List[A], List[A]]) {

    /**
     * Accesses the head of a list.
     */
    def head: ZTRef[EA, EB, A, A] =
      self.accessField(Optic.head)
  }

  /**
   * Provides implicit syntax for accessing the value at the specified key in a
   * map in a `ZTRef`.
   */
  implicit class KeyZTRefSyntax[EA >: EB, EB >: OpticFailure, K, V](
    private val self: ZTRef[EA, EB, Map[K, V], Map[K, V]]
  ) {

    /**
     * Accesses the value at the specified key in a map.
     */
    def key(k: K): ZTRef[EA, EB, V, V] =
      self.accessField(Optic.key(k))

    /**
     * Accesses the value at the specified key in a map or a provided
     * default if the key is missing
     */
    def keyOrDefault(k: K, default: => V): ZTRef[EA, EB, V, V] =
      self.accessField(Optic.keyOrDefault(k, default))
  }

  /**
   * Provides implicit syntax for accessing the `Left` case of an `Either` in a
   * `ZTRef`.
   */
  implicit class LeftZTRefSyntax[EA, EB >: OpticFailure, A, B, C](
    private val self: ZTRef[EA, EB, Either[C, B], Either[A, B]]
  ) {

    /**
     * Accesses the `Left` case of an `Either`.
     */
    def left: ZTRef[EA, EB, C, A] =
      self.accessCase(Optic.left)
  }

  /**
   * Provides implicit syntax for accessing the `None` case of an `Option` in a
   * `ZTRef`.
   */
  implicit class NoneZTRefSyntax[EA, EB >: OpticFailure, A](private val self: ZTRef[EA, EB, Option[A], Option[A]]) {

    /**
     * Accesses the `None` case of an `Option`.
     */
    def none: ZTRef[EA, EB, Unit, Unit] =
      self.accessCase(Optic.none)
  }

  /**
   * Provides implicit syntax for accessing the `Right` case of an `Either` in
   * a `ZTRef`.
   */
  implicit class RightZTRefSyntax[EA, EB >: OpticFailure, A, B, C](
    private val self: ZTRef[EA, EB, Either[A, C], Either[A, B]]
  ) {

    /**
     * Accesses the `Right` case of an `Either`.
     */
    def right: ZTRef[EA, EB, C, B] =
      self.accessCase(Optic.right)
  }

  /**
   * Provides implicit syntax for accessing the second element of a tuple in a
   * `ZTRef`.
   */
  implicit class SecondZTRefSyntax[EA >: EB, EB, A, B, C](private val self: ZTRef[EA, EB, (A, C), (A, B)]) {

    /**
     * Accesses the second element of a tuple.
     */
    def second: ZTRef[EA, EB, C, B] =
      self.accessField(Optic.second)
  }

  /**
   * Provides implicit syntax for accessing a slice of a chunk in a `ZTRef`.
   */
  implicit class SliceZTRefSyntax[EA >: EB, EB >: OpticFailure, A](
    private val self: ZTRef[EA, EB, Chunk[A], Chunk[A]]
  ) {

    /**
     * Accesses a slice of a chunk.
     */
    def slice(from: Int, until: Int): ZTRef[EA, EB, Chunk[A], Chunk[A]] =
      self.accessElements(Optic.slice(from, until))
  }

  /**
   * Provides implicit syntax for accessing the `Some` case of an `Option` in a
   * `ZTRef`.
   */
  implicit class SomeZTRefSyntax[EA, EB >: OpticFailure, A, B](private val self: ZTRef[EA, EB, Option[B], Option[A]]) {

    /**
     * Accesses the `Some` case of an `Option`.
     */
    def some: ZTRef[EA, EB, B, A] =
      self.accessCase(Optic.some)
  }

  /**
   * Provides implicit syntax for accessing the tail of a list in a `ZTRef`.
   */
  implicit class TailZTRefSyntax[EA >: EB, EB >: OpticFailure, A](private val self: ZTRef[EA, EB, List[A], List[A]]) {

    /**
     * Accesses the tail of a list.
     */
    def tail: ZTRef[EA, EB, List[A], List[A]] =
      self.accessField(Optic.tail)
  }
}
