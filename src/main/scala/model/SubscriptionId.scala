package model

import sttp.tapir.Schema
import zio.json.{DeriveJsonCodec, JsonCodec}

case class SubscriptionId(id: Int)

object SubscriptionId {
  implicit val jsonCodec: JsonCodec[SubscriptionId] = DeriveJsonCodec.gen
  implicit val schema: Schema[SubscriptionId] = Schema.derived
}
