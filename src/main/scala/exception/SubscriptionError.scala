package exception

import sttp.tapir.Schema
import zio.json.{DeriveJsonCodec, JsonCodec}

sealed trait SubscriptionError

object SubscriptionError {
  implicit lazy val codec: JsonCodec[SubscriptionError] = DeriveJsonCodec.gen

  case class ValidationFailed(message: String) extends SubscriptionError

  object ValidationFailed {
    implicit lazy val codec: JsonCodec[ValidationFailed] = DeriveJsonCodec.gen
    implicit lazy val schema: Schema[ValidationFailed] = Schema.derived
  }

  case class InvalidInput(message: String) extends SubscriptionError

  object InvalidInput {

    def link(link: String): InvalidInput = InvalidInput(s"Invalid input for link: $link")

    def id(id: Int): NotFound = NotFound(s"Not found value for id: $id")

    implicit lazy val codec: JsonCodec[InvalidInput] = DeriveJsonCodec.gen
    implicit lazy val schema: Schema[InvalidInput] = Schema.derived
  }

  case class NotFound(message: String) extends SubscriptionError

}