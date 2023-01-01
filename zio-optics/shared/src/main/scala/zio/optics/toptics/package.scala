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
}
