---
id: overview_composing_optics
title: "Composing Optics"
---

One of the great features of optics is that they compose.

If we have an optic that accesses a part `Piece` of a larger structure `Whole` and another optic that accesses a part `Piece2` of `Piece` we can combine them to create an optic that accesses the part `Piece2` of the original structure `Whole`. We do this using the `>>>` operator or its named alias `andThen`.

For example, say we have an `Either[String, Person]` representing either a person or a failure message indicating why the person does not exist. We would like to modify the age of the person, if it exists.

Rather than having to define a new optic for this, we can combine the existing `right` and `age` optics. The `right` optic will first "zoom in" to the `Person` on the right side of the either and then the `age` optic will further zoom in to the age field within the person.

```scala mdoc:silent
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

val optic: Optional[Either[String, Person], Int] =
  Optic.right >>> Person.age
```

The `>>>` operator will automatically take care of composing the optics together for us, returning the most specific optic possible given the two optics that we are combining.

Sometimes instead of directly accessing part of the value accessed by the first optic like this we would like to access each value within a collection accessed by the first optic.

For example, if we have an optic that returns a `Chunk[Person]` we may want to modify a field in every `Person` returned by the first optic instead of modifying the `Chunk[Person]` itself. We can do this using the `foreach` operator.

```scala mdoc:silent
def hasJanuaryBirthday(person: Person): Boolean =
  ???

val januaryAges: Traversal[Chunk[Person], Int] =
  Optic.filter(hasJanuaryBirthday).foreach(Person.age)
```
