package zio

package object optics extends Optics {

  override type OpticResult[+E, +A] = Either[E, A]

  override protected def fail[E](e: E): Either[E, Nothing] =
    Left(e)

  override protected def flatMap[E, A, B](either: Either[E, A])(f: A => Either[E, B]): Either[E, B] =
    either.flatMap(f)

  override protected def foldM[E, E2, A, B](
    either: Either[E, A]
  )(f: E => Either[E2, B], g: A => Either[E2, B]): Either[E2, B] =
    either.fold(f, g)

  override protected def map[E, A, B](either: Either[E, A])(f: A => B): Either[E, B] =
    either.map(f)

  override protected def succeed[A](a: A): Either[Nothing, A] =
    Right(a)

  /**
   * Providing implicit syntax for accessing pieces of a value in a `ZRef`
   * when the error types are unified.
   */
  implicit class ERefSyntax[EA >: EB, EB, A, B](private val self: ZRef[EA, EB, A, B]) {

    /**
     * Accesses some of the elements of a collection.
     */
    def accessElements[EC >: EA, ED >: EB, C, D](
      optic: Optic[B, B, Chunk[C], ED, EC, Chunk[D], A]
    ): ZRef[EC, ED, Chunk[C], Chunk[D]] =
      self.foldAll(
        identity,
        identity,
        identity,
        cs => b => optic.setOptic(cs)(b).left.map(_._1),
        b => optic.getOptic(b).left.map(_._1)
      )

    /**
     * Accesses a field of a product type.
     */
    def accessField[EC >: EA, ED >: EB, C, D](optic: Optic[B, B, C, ED, EC, D, A]): ZRef[EC, ED, C, D] =
      self.foldAll(
        identity,
        identity,
        identity,
        c => b => optic.setOptic(c)(b).left.map(_._1),
        b => optic.getOptic(b).left.map(_._1)
      )
  }

  /**
   * Providing implicit syntax for accessing pieces of a value in a `ZRef`.
   */
  implicit class ZRefSyntax[EA, EB, A, B](private val self: ZRef[EA, EB, A, B]) {

    /**
     * Accesses a term of a sum type.
     */
    final def accessCase[EC >: EA, ED >: EB, C, D](optic: Optic[B, Any, C, ED, EC, D, A]): ZRef[EC, ED, C, D] =
      self.fold(
        identity,
        identity,
        c => optic.setOptic(c)(()).left.map(_._1),
        d => optic.getOptic(d).left.map(_._1)
      )
  }

  /**
   * Provides implicit syntax for accessing the specified index of a chunk in a
   * `ZRef`.
   */
  implicit class AtZRefSyntax[EA >: EB, EB >: OpticFailure, A](private val self: ZRef[EA, EB, Chunk[A], Chunk[A]]) {

    /**
     * Accesses the specified index of a chunk.
     */
    def at(n: Int): ZRef[EA, EB, A, A] =
      self.accessField(Optic.at(n))
  }

  /**
   * Provides implicit syntax for accessing the `::` case of a `List` in a
   * `ZRef`.
   */
  implicit class ConsZRefSyntax[EA, EB >: OpticFailure, A](private val self: ZRef[EA, EB, List[A], List[A]]) {

    /**
     * Accesses the `::` case of a `List`.
     */
    def cons: ZRef[EA, EB, (A, List[A]), (A, List[A])] =
      self.accessCase(Optic.cons)
  }

  /**
   * Provides implicit syntax for accessing a filtered subset of a chunk in a
   * `ZRef`.
   */
  implicit class FilterZRefSyntax[EA >: EB, EB >: OpticFailure, A](private val self: ZRef[EA, EB, Chunk[A], Chunk[A]]) {

    /**
     * Accesses a filtered subset of a chunk.
     */
    def filter(f: A => Boolean): ZRef[EA, EB, Chunk[A], Chunk[A]] =
      self.accessElements(Optic.filter(f))
  }

  /**
   * Provides implicit syntax for accessing the first element of a tuple in a
   * `ZRef`.
   */
  implicit class FirstZRefSyntax[EA >: EB, EB, A, B, C](private val self: ZRef[EA, EB, (C, B), (A, B)]) {

    /**
     * Accesses the first element of a tuple.
     */
    def first: ZRef[EA, EB, C, A] =
      self.accessField(Optic.first)
  }

  /**
   * Provides implicit syntax for accessing the head of a list in a `ZRef`.
   */
  implicit class HeadZRefSyntax[EA >: EB, EB >: OpticFailure, A](private val self: ZRef[EA, EB, List[A], List[A]]) {

    /**
     * Accesses the head of a list.
     */
    def head: ZRef[EA, EB, A, A] =
      self.accessField(Optic.head)
  }

  /**
   * Provides implicit syntax for accessing the value at the specified key in a
   * map in a `ZRef`.
   */
  implicit class KeyZRefSyntax[EA >: EB, EB >: OpticFailure, K, V](
    private val self: ZRef[EA, EB, Map[K, V], Map[K, V]]
  ) {

    /**
     * Accesses the value at the specified key in a map.
     */
    def key(k: K): ZRef[EA, EB, V, V] =
      self.accessField(Optic.key(k))
  }

  /**
   * Provides implicit syntax for accessing the `Left` case of an `Either` in a
   * `ZRef`.
   */
  implicit class LeftZRefSyntax[EA, EB >: OpticFailure, A, B, C](
    private val self: ZRef[EA, EB, Either[C, B], Either[A, B]]
  ) {

    /**
     * Accesses the `Left` case of an `Either`.
     */
    def left: ZRef[EA, EB, C, A] =
      self.accessCase(Optic.left)
  }

  /**
   * Provides implicit syntax for accessing the `None` case of an `Option` in a
   * `ZRef`.
   */
  implicit class NoneZRefSyntax[EA, EB >: OpticFailure, A](private val self: ZRef[EA, EB, Option[A], Option[A]]) {

    /**
     * Accesses the `None` case of an `Option`.
     */
    def none: ZRef[EA, EB, Unit, Unit] =
      self.accessCase(Optic.none)
  }

  /**
   * Provides implicit syntax for accessing the `Right` case of an `Either` in
   * a `ZRef`.
   */
  implicit class RightZRefSyntax[EA, EB >: OpticFailure, A, B, C](
    private val self: ZRef[EA, EB, Either[A, C], Either[A, B]]
  ) {

    /**
     * Accesses the `Right` case of an `Either`.
     */
    def right: ZRef[EA, EB, C, B] =
      self.accessCase(Optic.right)
  }

  /**
   * Provides implicit syntax for accessing the second element of a tuple in a
   * `ZRef`.
   */
  implicit class SecondZRefSyntax[EA >: EB, EB, A, B, C](private val self: ZRef[EA, EB, (A, C), (A, B)]) {

    /**
     * Accesses the second element of a tuple.
     */
    def second: ZRef[EA, EB, C, B] =
      self.accessField(Optic.second)
  }

  /**
   * Provides implicit syntax for accessing a slice of a chunk in a `ZRef`.
   */
  implicit class SliceZRefSyntax[EA >: EB, EB >: OpticFailure, A](private val self: ZRef[EA, EB, Chunk[A], Chunk[A]]) {

    /**
     * Accesses a slice of a chunk.
     */
    def slice(from: Int, until: Int): ZRef[EA, EB, Chunk[A], Chunk[A]] =
      self.accessElements(Optic.slice(from, until))
  }

  /**
   * Provides implicit syntax for accessing the `Some` case of an `Option` in a
   * `ZRef`.
   */
  implicit class SomeZRefSyntax[EA, EB >: OpticFailure, A, B](private val self: ZRef[EA, EB, Option[B], Option[A]]) {

    /**
     * Accesses the `Some` case of an `Option`.
     */
    def some: ZRef[EA, EB, B, A] =
      self.accessCase(Optic.some)
  }

  /**
   * Provides implicit syntax for accessing the tail of a list in a `ZRef`.
   */
  implicit class TailZRefSyntax[EA >: EB, EB >: OpticFailure, A](private val self: ZRef[EA, EB, List[A], List[A]]) {

    /**
     * Accesses the tail of a list.
     */
    def tail: ZRef[EA, EB, List[A], List[A]] =
      self.accessField(Optic.tail)
  }
}
