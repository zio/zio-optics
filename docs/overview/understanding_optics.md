---
id: overview_understanding_optics
title: "Understanding Optics"
---

ZIO Optics is based on a single representation of an optic as a combination of a getter and a setter.

```scala mdoc
trait Optic[-GetWhole, -SetWholeBefore, -SetPiece, +GetError, +SetError, +GetPiece, +SetWholeAfter] {
  def getOptic(whole: GetWhole): Either[GetError, GetPiece]
  def setOptic(piece: SetPiece)(whole: SetWholeBefore): Either[SetError, SetWholeAfter]
}
```

The getter can take some larger structure of type `GetWhole` and get a part of it of type `GetPiece`. It can potentially fail with an error of type `GetError` because the part we are trying to get might not exist in the larger structure.

The setter has the ability, given some piece of type `SetPiece` and an original structure of type `SetWholeBefore`, to return a new structure of type `SetWholeAfter`. Setting can fail with an error of type `SetError` because the piece we are trying to set might not exist in the structure.

## Lens

A `Lens` is an optic that accesses a field of a product type, such as a tuple or case class.

The `GetError` type of a `Lens` is `Nothing` because we can always get a field of a product type. The `SetError` type is also `Nothing` because we can always set the field of a product type to a new value.

In this case the `GetWhole`, `SetWholeBefore`, and `SetWholeAfter` types are the same and represent the product type. The `GetPiece` and `SetPiece` types are also the same and represent the field.

Thus, we have:

```scala mdoc
type Lens[S, A] = Optic[S, S, A, Nothing, Nothing, A, S]
```

The simplified signature is:

```scala mdoc:nest
trait Lens[S, A] {
  def getOptic(s: S): Either[Nothing, A]
  def setOptic(a: A)(s: S): Either[Nothing, S]
}
```

This conforms exactly to our description above. A lens is an optic where we can always get part of the larger structure and given an original structure we can always set a new value in that structure.

## Prism

A `Prism` is an optic that accesses a case of a sum type, such as the `Left` or `Right` cases of an `Either` or one of the subtypes of a sealed trait.

Getting part of a larger data structure with a prism can fail because the case we are trying to access might not exist. For example, we might be trying to access the right side of an `Either` but the either is actually a `Left`.

We use the data type `OpticFailure` to model the different ways that getting or setting with an optic can fail. So the `GetError` type of a prism will be `OpticFailure`.

```scala mdoc
trait OpticFailure extends Throwable
```

The `SetError` type of a prism will be `Nothing` because given one of the cases of a sum type we can always return a new value of the sum type since each case of the sum type is an instance of the sum type.

A prism also differs from a lens in that we do not need any original structure to set. A sum type consists of nothing but its cases so if we have a new value of the case we want to set we can just use that value and don't need the original structure.

We represent this by using `Any` for the `SetWholeBefore` type, indicating that we do not need any original structure to set a new value.

Thus, the definition of a prism is:

```scala mdoc
type Prism[S, A] = Optic[S, Any, A, OpticFailure, Nothing, A, S]
```

And the simplified signature is:

```scala mdoc:nest
trait Prism[S, A] {
  def getOptic(s: S): Either[OpticFailure, A]
  def setOptic(a: Any)(s: S): Either[Nothing, S]
}
```

Again this conforms exactly to our description. A prism is an optic where we might not be able to get a value but can always set a value and in fact do not require any original structure to set.

## Traversal

A `Traversal` is an optic that accesses zero or more values in a collection, such as a `Chunk`.

Both getting and setting can fail because, for example, we might be trying to get or set a value at an index that does not exist in the `Chunk`. We also need the original structure because we may only be modifying part of the collection.

The distinguishing feature of a `Traversal` is that it can access zero or more values instead of a single value. We represent this by parameterizing `SetPiece` and `GetPiece` on a `Chunk` instead of a single value.

The definition of a traversal looks like this:

```scala mdoc
import zio.Chunk

type Traversal[S, A] = Optic[S, S, Chunk[A], OpticFailure, OpticFailure, Chunk[A], S]
```

And the simplified signature looks like this:

```scala mdoc:nest
trait Traversal[S, A] {
  def getOptic(s: S): Either[OpticFailure, Chunk[A]]
  def setOptic(as: Chunk[A])(s: S): Either[OpticFailure, S]
}
```

## Other

ZIO Optics supports a wide variety of other optics:

* **Optional** - An `Optional` is an optic that accesses part of a larger structure where the part being accessed may not exist and the structure contains more than just that part. Both the `GetError` and `SetError` types are `OpticFailure` because the part may not exist in the structure and setting does require the original structure since it consists of more than just this one part.
* **Iso** - An `Iso` is an optic that accesses a part of a structure where the structure consists of nothing but the part. Both the `GetError` and `SetError` types are `Nothing` and the `SetWholeBefore` type is `Any`.
* **Fold** - A `Fold` is a `Traversal` that only allows getting a collection of values. The `SetWhole` before and `SetPiece` types are nothing because it is impossible to ever set.
* **Getter** - A `Getter` is an optic that only allows getting a value. Like a `Fold` the `SetWholeBefore` and `SetPiece` types are `Nothing` because it is impossible to ever set.
* **Setter** - A `Setter` is an optic that only allows setting a value. The `GetWhole` type is `Nothing` because it is impossible to ever get.

There are also more polymorphic versions of each optic that allow the types of the data structure and part before and after to differ. For example, a `ZPrism` could allow us to access the right case of an `Either[A, B]` and set a `C` value to return an `Either[A, C]`.
