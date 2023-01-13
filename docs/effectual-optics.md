---
id: effectual-optics
title: "Effectual Optics"
---

In addition to "pure" optics like the ones discussed so far ZIO Optics supports optics where getting and setting involve `ZIO` or `STM` effects. This allows using optics to work with ZIO data structures such as `TMap`.

These optics work exactly the same way as pure optics except that instead of the getter and setter returning an `Either` they return a `ZIO` or an `STM` value.

```scala mdoc:compile-only
import zio._

trait Optic[-GetWhole, -SetWholeBefore, -SetPiece, +GetError, +SetError, +GetPiece, +SetWholeAfter] {
  def getOptic(whole: GetWhole): IO[GetError, GetPiece]
  def setOptic(piece: SetPiece)(whole: SetWholeBefore): IO[SetError, SetWholeAfter]
}
```

To work with these optics, import `zio.optics.opticsm._` for optics where the result type is a `ZIO` value or `zio.optics.toptics._` for optics where the result type is an `STM` value. This will automatically bring the appropriate version of the `Optic` data type as well as its constructors and syntax into scope.

For example, here is how we could define an optic to work with a nested value inside a `TMap`:

```scala mdoc:silent
import zio.stm._
import zio.optics.toptics._

val optic: Optional[TMap[String, Either[String, Int]], Int] =
  TOptics.key("key") >>> Optic.right
```

Note that optics specific to STM data structures are defined in the `TOptics` object. Otherwise everything works the same way.

Just like with `Ref`, ZIO Optics provides special support for working with STM data types such as `TMap`, so we can update our `TMap` using "dot" syntax like this:

```scala:mdoc
tMap.key("key").right.at(0).update(_ + 1)
```
