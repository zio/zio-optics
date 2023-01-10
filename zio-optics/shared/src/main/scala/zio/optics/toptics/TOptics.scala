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

package zio.optics.toptics

import zio.stm._

object TOptics {

  /**
   * An optic that accesses the value at the specified key in a `TMap`.
   */
  def key[K, V](k: K): Optional[TMap[K, V], V] =
    Optic(
      map =>
        map.get(k).flatMap {
          case Some(v) => ZSTM.succeedNow(v)
          case None    => ZSTM.fail((OpticFailure(s"$map did not satify hasKey($k)"), map))
        },
      v =>
        map =>
          map.get(k).flatMap {
            case Some(_) => map.put(k, v).as(map)
            case None    => ZSTM.fail((OpticFailure(s"$map did not satisfy hasKey($k)"), map))
          }
    )
}
