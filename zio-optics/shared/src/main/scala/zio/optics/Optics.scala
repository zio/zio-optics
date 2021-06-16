package zio.optics

trait Optics
    extends OpticComposeModule
    with OpticFailureModule
    with OpticModule
    with OpticResultModule
    with OpticTypesModule {

  /**
   * Provides implicit syntax for working with any value as a partially
   * applied optic.
   */
  implicit final class OpticSyntax[Whole](private val self: Whole) {

    /**
     * Views this value as a partially applied optic.
     */
    def optic: OpticPartiallyApplied[Whole, Nothing, Nothing, Whole, Whole] =
      Optic.identity(self)
  }
}
