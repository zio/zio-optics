package zio.optics

trait OpticFailureModule { self =>

  /**
   * An `OpticFailure` describes how getting or setting a piece of a whole
   * using an optic failed.
   */
  case class OpticFailure(message: String) extends Exception(message)
}
