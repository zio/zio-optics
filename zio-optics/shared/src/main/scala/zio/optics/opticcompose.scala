package zio.optics

trait OpticComposeModule { self: OpticFailureModule with OpticModule with OpticResultModule with OpticTypesModule =>

  /**
   * `OpticCompose` abstracts over the different ways that optics can be
   * composed together, allowing all optics to be composed with a single
   * operator.
   */
  trait OpticCompose[
    GetWhole,
    SetWholeBefore,
    SetWholeBefore1,
    SetWholeBefore2,
    GetError,
    SetError,
    SetError1,
    GetPiece
  ] {
    def compose[SetPiece, SetPiece1, GetError1 >: GetError, GetPiece1, SetWholeAfter](
      left: Optic[GetWhole, SetWholeBefore, SetPiece, GetError, SetError, GetPiece, SetWholeAfter],
      right: Optic[GetPiece, SetWholeBefore1, SetPiece1, GetError1, SetError1, GetPiece1, SetPiece]
    ): Optic[GetWhole, SetWholeBefore2, SetPiece1, GetError1, SetError1, GetPiece1, SetWholeAfter]
  }

  object OpticCompose extends LowPriorityOpticCompose {

    /**
     * Compose two optics when the piece of the whole returned by the get
     * operator of the first optic is not needed by the set operator of the
     * second optic.
     */
    implicit final def prismCompose[
      GetWhole <: SetWholeBefore,
      SetWholeBefore,
      GetError,
      SetError <: SetError1,
      SetError1,
      GetPiece
    ]: OpticCompose[GetWhole, SetWholeBefore, Any, SetWholeBefore, GetError, SetError, SetError1, GetPiece] =
      new OpticCompose[GetWhole, SetWholeBefore, Any, SetWholeBefore, GetError, SetError, SetError1, GetPiece] {
        def compose[SetPiece, SetPiece1, GetError1 >: GetError, GetPiece1, SetWholeAfter](
          left: Optic[GetWhole, SetWholeBefore, SetPiece, GetError, SetError, GetPiece, SetWholeAfter],
          right: Optic[GetPiece, Any, SetPiece1, GetError1, SetError1, GetPiece1, SetPiece]
        ): Optic[GetWhole, SetWholeBefore, SetPiece1, GetError1, SetError1, GetPiece1, SetWholeAfter] =
          Optic(
            getWhole =>
              left
                .getOptic(getWhole)
                .foldM(
                  { case (getError, setWholeAfter) => fail((getError, setWholeAfter)) },
                  getPiece =>
                    right
                      .getOptic(getPiece)
                      .foldM(
                        { case (getError, setPiece) =>
                          left
                            .setOptic(setPiece)(getWhole)
                            .foldM(
                              { case (_, setWholeAfter) => fail((getError, setWholeAfter)) },
                              setWholeAfter => fail((getError, setWholeAfter))
                            )
                        },
                        getPiece => succeed(getPiece)
                      )
                ),
            setPiece =>
              setWholeBefore =>
                right
                  .setOptic(setPiece)(())
                  .foldM(
                    { case (setError, setPiece) =>
                      left
                        .setOptic(setPiece)(setWholeBefore)
                        .foldM(
                          { case (_, setWholeAfter) => fail((setError, setWholeAfter)) },
                          setWholeAfter => fail((setError, setWholeAfter))
                        )
                    },
                    setPiece => left.setOptic(setPiece)(setWholeBefore)
                  )
          )
      }
  }

  trait LowPriorityOpticCompose {

    /**
     * Compose two optics when the piece of the whole returned by the first
     * optic is needed by the set operator of the second optic. This is more
     * powerful but requires unifying the error types.
     */
    implicit final def lensCompose[
      GetWhole <: SetWholeBefore,
      SetWholeBefore,
      SetWholeBefore1,
      GetPiece <: SetWholeBefore1,
      GetError <: SetError1,
      SetError <: SetError1,
      SetError1
    ]: OpticCompose[GetWhole, SetWholeBefore, SetWholeBefore1, GetWhole, GetError, SetError, SetError1, GetPiece] =
      new OpticCompose[GetWhole, SetWholeBefore, SetWholeBefore1, GetWhole, GetError, SetError, SetError1, GetPiece] {
        def compose[SetPiece, SetPiece1, GetError1 >: GetError, GetPiece1, SetWholeAfter](
          left: Optic[GetWhole, SetWholeBefore, SetPiece, GetError, SetError, GetPiece, SetWholeAfter],
          right: Optic[GetPiece, SetWholeBefore1, SetPiece1, GetError1, SetError1, GetPiece1, SetPiece]
        ): Optic[GetWhole, GetWhole, SetPiece1, GetError1, SetError1, GetPiece1, SetWholeAfter] =
          Optic(
            getWhole =>
              left
                .getOptic(getWhole)
                .foldM(
                  e => fail(e),
                  getPiece =>
                    right
                      .getOptic(getPiece)
                      .foldM(
                        { case (getError, setPiece) =>
                          left
                            .setOptic(setPiece)(getWhole)
                            .foldM(
                              { case (_, setWholeAfter) => fail((getError, setWholeAfter)) },
                              setWholeAfter => fail((getError -> setWholeAfter))
                            )
                        },
                        a => succeed(a)
                      )
                ),
            setPiece =>
              setWhole =>
                left
                  .getOptic(setWhole)
                  .foldM(
                    { case (getError, setWholeAfter) => fail((getError, setWholeAfter)) },
                    getPiece =>
                      right
                        .setOptic(setPiece)(getPiece)
                        .foldM(
                          { case (setError, setPiece) =>
                            left
                              .setOptic(setPiece)(setWhole)
                              .foldM(
                                { case (_, setWholeAfter) => fail((setError, setWholeAfter)) },
                                setWholeAfter => fail((setError, setWholeAfter))
                              )
                          },
                          setPiece => left.setOptic(setPiece)(setWhole)
                        )
                  )
          )
      }
  }
}
