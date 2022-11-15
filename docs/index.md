---
id: index
title: "Introduction to ZIO Optics"
sidebar_label: "ZIO Optics"
---

ZIO Optics makes it easy to modify parts of larger data structures.

For example, say we have a web application where users can vote on which of various topics they are interested in. We maintain our state of how many votes each topic has received as a `Ref[Map[String, Int]]`.

```scala mdoc
import zio._

lazy val voteRef: Ref[Map[String, Int]] =
  ???
```

If we want to increment the number of votes for one of the topics here is what it would look like:

```scala mdoc
def incrementVotes(topic: String): Task[Unit] =
  voteRef.modify { voteMap =>
    voteMap.get(topic) match {
      case Some(votes) =>
        (ZIO.unit, voteMap + (topic -> (votes + 1)))
      case None        =>
        val message = s"voteMap $voteMap did not contain topic $topic"
        (ZIO.fail(new NoSuchElementException(message)), voteMap)
    }
  }.flatten
```

This is alright, but there is a lot of code here for a relatively simple operation of incrementing one of the keys. We have to get the value from the `Ref`, then get the value from the `Map`, and finally set the new value in the `Map`.

We also have to explicitly handle the possibility that the value is not in the map. And this is all for a relatively simple data structure!

Here is what this would look like with ZIO Optics.

```scala mdoc:nest
import zio.optics._

def incrementVotes(topic: String): Task[Unit] =
  voteRef.key(topic).update(_ + 1)
```

The `key` optic "zooms in" on part of a larger structure, in this case transforming the `Ref[Map[String, Int]]` into a `Ref` that accesses the value at the specified key. We can then simply call the `update` operator on `Ref` to increment the value.

The optic also handles the possibility of failure for us, failing with an `OpticFailure` that is a subtype of `Throwable` and contains a helpful error message if the key cannot be found.

ZIO Optics makes it easy to compose more complex optics from simpler ones, to define optics for your own data types, and to work with optics that use `ZIO` or `STM` effects.

## Installation

Include ZIO Optics in your project by adding the following to your `build.sbt` file:

```scala
libraryDependencies += "dev.zio" %% "zio-optics" % "@VERSION@"
```
