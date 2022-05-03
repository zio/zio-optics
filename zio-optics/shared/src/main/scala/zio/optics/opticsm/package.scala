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
    ZIO.fail(e)

  protected def flatMap[E, A, B](io: IO[E, A])(f: A => IO[E, B]): IO[E, B] =
    io.flatMap(f)

  protected def foldM[E, E2, A, B](io: IO[E, A])(f: E => IO[E2, B], g: A => IO[E2, B]): IO[E2, B] =
    io.foldZIO(f, g)

  protected def map[E, A, B](opticResult: OpticResult[E, A])(f: A => B): OpticResult[E, B] =
    opticResult.map(f)

  protected def succeed[A](a: A): IO[Nothing, A] =
    ZIO.succeed(a)
}
