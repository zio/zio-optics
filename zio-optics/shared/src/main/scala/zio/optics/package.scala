package zio

package object optics extends Optics {

  override type OpticResult[+E, +A] = Either[E, A]

  override protected def fail[E](e: E): Either[E, Nothing] =
    Left(e)

  override protected def flatMap[E, A, B](either: Either[E, A])(f: A => Either[E, B]): Either[E, B] =
    either.fold(e => Left(e), a => f(a))

  override protected def foldM[E, E2, A, B](
    either: Either[E, A]
  )(f: E => Either[E2, B], g: A => Either[E2, B]): Either[E2, B] =
    either.fold(f, g)

  override protected def map[E, A, B](either: Either[E, A])(f: A => B): Either[E, B] =
    either.fold(e => Left(e), a => Right(f(a)))

  override protected def succeed[A](a: A): Either[Nothing, A] =
    Right(a)
}
