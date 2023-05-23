package model

import sttp.tapir.Schema
import zio.json.{DeriveJsonCodec, JsonCodec}

case class SubscriptionLink(link: String)

object SubscriptionLink {
  implicit val jsonCodec: JsonCodec[SubscriptionLink] = DeriveJsonCodec.gen
  implicit val schema: Schema[SubscriptionLink] = Schema.derived
}
