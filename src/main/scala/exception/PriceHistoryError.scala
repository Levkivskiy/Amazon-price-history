package exception

import sttp.tapir.Schema
import zio.json.{DeriveJsonCodec, JsonCodec}

sealed trait PriceHistoryError

object PriceHistoryError {
  implicit lazy val codec: JsonCodec[PriceHistoryError] = DeriveJsonCodec.gen

  case class InvalidInput(error: String) extends PriceHistoryError

  object InvalidInput {

    def id(id: Int): InvalidInput = InvalidInput(s"Invalid input for id: $id")

    implicit lazy val codec: JsonCodec[InvalidInput] = DeriveJsonCodec.gen
    implicit lazy val schema: Schema[InvalidInput] = Schema.derived
  }

  case class NotFound(message: String) extends PriceHistoryError

  object NotFound {
    implicit lazy val codec: JsonCodec[NotFound] = DeriveJsonCodec.gen
    implicit lazy val schema: Schema[NotFound] = Schema.derived
  }
}