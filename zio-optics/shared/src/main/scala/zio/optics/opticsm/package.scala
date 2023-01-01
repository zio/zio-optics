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
