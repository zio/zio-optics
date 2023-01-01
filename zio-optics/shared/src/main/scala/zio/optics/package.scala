/*
 * Copyright 2021-2023 John A. De Goes and the ZIO Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
