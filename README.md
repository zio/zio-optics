# ZIO Optics

| Project Stage | CI | Release | Snapshot | Discord |
| --- | --- | --- | --- | --- |
| [![Project stage][Badge-Stage]][Link-Stage-Page] | ![CI][Badge-CI] | [![Release Artifacts][Badge-SonatypeReleases]][Link-SonatypeReleases] | [![Snapshot Artifacts][Badge-SonatypeSnapshots]][Link-SonatypeSnapshots] | [![Badge-Discord]][Link-Discord] |

# Summary

ZIO Optics is a library that makes it easy to modify parts of larger data structures based on a single representation of an optic as a combination of a getter and setter.

ZIO Optics features:

* **A unified representation of optics** - All optics compose the same way because they are all instances of the same data type. 
* **Deep ZIO integration** - Represent effectual and transactional optics that work with ZIO data structures like `Ref` and `TMap`.
* **Helpful error messages** - When an optic fails see where it failed and why.
* **Zero dependencies** - No dependencies other than ZIO itself.
* **No unnecessary abstractions** - Concrete representation makes it easy to learn.

For more information check out the microsite below.

# Documentation
[ZIO Optics Microsite](https://zio.github.io/zio-optics/)

# Contributing
[Documentation for contributors](https://zio.github.io/zio-optics/docs/about/about_contributing)

## Code of Conduct

See the [Code of Conduct](https://zio.github.io/zio-optics/docs/about/about_coc)

## Support

Come chat with us on [![Badge-Discord]][Link-Discord].


# License
[License](LICENSE)

[Badge-SonatypeReleases]: https://img.shields.io/nexus/r/https/oss.sonatype.org/dev.zio/zio-optics_2.12.svg "Sonatype Releases"
[Badge-SonatypeSnapshots]: https://img.shields.io/nexus/s/https/oss.sonatype.org/dev.zio/zio-optics_2.12.svg "Sonatype Snapshots"
[Badge-Discord]: https://img.shields.io/discord/629491597070827530?logo=discord "chat on discord"
[Badge-CI]: https://github.com/zio/zio-optics/workflows/CI/badge.svg
[Link-SonatypeReleases]: https://oss.sonatype.org/content/repositories/releases/dev/zio/zio-optics_2.12/ "Sonatype Releases"
[Link-SonatypeSnapshots]: https://oss.sonatype.org/content/repositories/snapshots/dev/zio/zio-optics_2.12/ "Sonatype Snapshots"
[Link-Discord]: https://discord.gg/2ccFBr4 "Discord"
[Badge-Stage]: https://img.shields.io/badge/Project%20Stage-Development-yellowgreen.svg
[Link-Stage-Page]: https://github.com/zio/zio/wiki/Project-Stages

