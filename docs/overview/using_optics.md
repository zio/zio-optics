---
id: overview_using_optics
title: "Using Optics"
---

We can use optics to work with our data structures using the `get`, `set`, and `update` operators on `Optic`.

```scala mdoc
import zio._
import zio.optics._

case class Person(name: String, age: Int)

object Person {
  val age: Lens[Person, Int] =
    Lens(
      person => Right(person.age),
      age => person => Right(person.copy(age = age))
    )
}

val person = Person("Jane Doe", 42)

Person.age.get(person)

Person.age.set(43)(person)

Person.age.update(person)(_ + 1)
```

In addition to this, ZIO Optics provides special support for accessing values inside of ZIO data types such as `Ref`. Instead of having to create an optic and then use it to modify a data structure we can "zoom in" on the value inside of the `ZRef` directly using "dot" syntax.

```scala mdoc:compile-only
val ref: Ref[Map[String, Either[String, Chunk[Int]]]] =
  ???

val io: IO[OpticFailure, Unit] =
  ref.key("key").right.at(0).update(_ + 1)
```

You can use this dot syntax with ordinary values using the `optic` operator.

```scala mdoc:compile-only
val map: Map[String, Either[String, Chunk[Int]]] =
  ???

val updated: Either[OpticFailure, Map[String, Either[String, Chunk[Int]]]] =
  map.optic.key("key").right.at(0).update(_ + 1)
```

Note that this syntax is currently only available for optics defined in ZIO Optics. When automatic derivation of optics is introduced this syntax will be supported for user defined data structures as well.
