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
