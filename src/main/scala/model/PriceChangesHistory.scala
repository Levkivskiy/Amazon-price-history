package model

import sttp.tapir.Schema
import zio.json.{DeriveJsonCodec, JsonCodec}

import java.time.LocalDate

case class PriceChangesHistory(price: Double, date: LocalDate)

object PriceChangesHistory {
  implicit val jsonCodec: JsonCodec[PriceChangesHistory] = DeriveJsonCodec.gen
  implicit val schema: Schema[PriceChangesHistory] = Schema.derived
}
