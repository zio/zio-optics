package zio.optics

import zio.{Chunk, ChunkBuilder}

trait OpticResultModule { self =>

  /**
   * An `OpticResult` represents the result from getting or setting a piece of
   * a whole using an optic. This allows the library to abstract over pure
   * optics, transactional optics, and effectual optics.
   */
  type OpticResult[+E, +A]

  /**
   * Constructs an optic result that fails with the specified value.
   */
  protected def fail[E](e: E): OpticResult[E, Nothing]

  /**
   * Constructs an optic result by applying a function that returns an optic
   * result to the success value of the specified optic result.
   */
  protected def flatMap[E, A, B](opticResult: OpticResult[E, A])(f: A => OpticResult[E, B]): OpticResult[E, B]

  /**
   * Constructs an optic results that folds over the success and failure
   * values of the specified optic result, transforming them with the
   * functions `f` and `g` that return an optic result.
   */
  protected def foldM[E, E2, A, B](
    opticResult: OpticResult[E, A]
  )(f: E => OpticResult[E2, B], g: A => OpticResult[E2, B]): OpticResult[E2, B]

  /**
   * Constructs an optic result by applying a function to the success value of
   * this optic result.
   */
  protected def map[E, A, B](opticResult: OpticResult[E, A])(f: A => B): OpticResult[E, B]

  /**
   * Constructs an optic result that succeeds with the specified value.
   */
  protected def succeed[A](a: A): OpticResult[Nothing, A]

  /**
   * Constructs an optic result that succeeds with all the success values in
   * the specified collection of optic results, discarding the failures.
   */
  protected final def collectAllSuccesses[E, A](
    iterable: Iterable[OpticResult[E, A]]
  ): OpticResult[Nothing, Chunk[A]]                                                                                =
    foreach(iterable)(_.foldM(e => succeed(Left(e)), a => succeed(Right(a)))).map(_.collect { case Right(a) => a })

  /**
   * Constructs an optic result that applies a function returning an optic
   * result to each element in the specified collection and collects the
   * results into a single optic result.
   */
  protected final def foreach[E, A, B](iterable: Iterable[A])(f: A => OpticResult[E, B]): OpticResult[E, Chunk[B]] =
    iterable
      .foldLeft[OpticResult[E, ChunkBuilder[B]]](succeed(ChunkBuilder.make[B]()))((builder, a) =>
        builder.zipWith(f(a))(_ += _)
      )
      .map(_.result())

  /**
   * Constructs an optic result by applying a function to the failure value of
   * this optic result.
   */
  protected final def mapError[E, E2, A](opticResult: OpticResult[E, A])(f: E => E2): OpticResult[E2, A] =
    foldM(opticResult)(e => fail(f(e)), a => succeed(a))

  /**
   * Constructs an optic result that is equal to the left optic result if it
   * is successful or else the right optic result.
   */
  protected final def orElse[E, E2, A](left: => OpticResult[E, A], right: => OpticResult[E2, A]): OpticResult[E2, A] =
    foldM(left)(_ => right, a => succeed(a))

  /**
   * Constructs an optic result that combines the left and right optic results.
   */
  protected final def zip[E, A, B](left: => OpticResult[E, A], right: => OpticResult[E, B]): OpticResult[E, (A, B)] =
    zipWith(left, right)((_, _))

  /**
   * Constructs an optic result that combines the left and right optic results,
   * transforming their success values with the specified function.
   */
  protected final def zipWith[E, A, B, C](left: => OpticResult[E, A], right: => OpticResult[E, B])(
    f: (A, B) => C
  ): OpticResult[E, C] =
    flatMap(left)(a => map(right)(b => f(a, b)))

  /**
   * Provides implicit syntax for working with optic results.
   */
  private[optics] final implicit class OpticResultSyntax[E, A](private val opticResult: OpticResult[E, A]) {

    /**
     * Transforms the success value of this optic result with the specified
     * function that returns an optic result.
     */
    def flatMap[E1 >: E, B](f: A => OpticResult[E1, B]): OpticResult[E1, B] =
      self.flatMap[E1, A, B](opticResult)(f)

    /**
     * Folds over the success and failure values of this optic result,
     * transforming them with the functions `f` and `g` that return an optic
     * result.
     */
    def foldM[E2, B](f: E => OpticResult[E2, B], g: A => OpticResult[E2, B]): OpticResult[E2, B] =
      self.foldM(opticResult)(f, g)

    /**
     * Transforms the success value of this optic result with the specified
     * function.
     */
    def map[B](f: A => B): OpticResult[E, B] =
      self.map(opticResult)(f)

    /**
     * Transforms the failure value of this optic result with the specified
     * function.
     */
    def mapError[E2](f: E => E2): OpticResult[E2, A] =
      self.mapError(opticResult)(f)

    /**
     * Returns this optic result if it is successful or else that optic
     * result.
     */
    def orElse[E2, A1 >: A](that: => OpticResult[E2, A1]): OpticResult[E2, A1] =
      self.orElse(opticResult, that)

    /**
     * Combines this optic result with that optic result.
     */
    def zip[E1 >: E, B](that: => OpticResult[E1, B]): OpticResult[E1, (A, B)] =
      self.zip(opticResult, that)

    /**
     * Combines this optic result with that optic result, transforming their
     * success values with the specified function.
     */
    def zipWith[E1 >: E, B, C](that: => OpticResult[E1, B])(f: (A, B) => C): OpticResult[E1, C] =
      self.zipWith(opticResult, that)(f)
  }
}
