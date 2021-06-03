---
id: overview_constructing_optics
title: "Constructing Optics"
---

ZIO Optics comes included with a variety of optics for working with data structures defined in the Scala standard library.

All of these are available in the `Optic` companion object. For example, if we want to access the first value in a `(String, Int)` we could do:

```scala mdoc:silent
import zio.optics._

val first: Lens[(String, Int), String] =
  Optic.first
```

In addition, constructors are available in the companion object of each optic. So if we wanted to access the right value of an `Either` we could also do it as:

```scala mdoc:silent
val right: Prism[Either[String, Int], Int] =
  Prism.right
```

To create optics for your own data types you just need to define a getter and a setter. For instance, we can create an optic that accesses the age of a person like this:

```scala mdoc
case class Person(name: String, age: Int)

object Person {
  val age: Lens[Person, Int] =
    Lens(
      person => Right(person.age),
      age => person => Right(person.copy(age = age))
    )
}
```

If your optic fails you should return a `Left` with an `OpticFailure`. An `OpticFailure` allows you to provide an error message which you can use to describe why the optic failed.

In the future ZIO Optics will derive these optics automatically so you will not have to do this yourself.