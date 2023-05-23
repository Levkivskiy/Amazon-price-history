package config

import zio.ZLayer
import zio.config.magnolia.descriptor
import zio.config.{PropertyTreePath, ReadError, read}
import zio.config.typesafe.TypesafeConfigSource

case class PriceChangesConfig(intervalS: Int)

object PriceChangesConfig {
  val live: ZLayer[Any, ReadError[String], PriceChangesConfig] =
    ZLayer {
      read {
        descriptor[PriceChangesConfig].from(
          TypesafeConfigSource.fromResourcePath
            .at(PropertyTreePath.$("price.changes"))
        )
      }
    }
}