package config

import zio.ZLayer
import zio.config.magnolia.descriptor
import zio.config.{PropertyTreePath, ReadError, read}
import zio.config.typesafe.TypesafeConfigSource

case class HistoryConfig(daysUntil: Int)

object HistoryConfig {
  val live: ZLayer[Any, ReadError[String], HistoryConfig] =
    ZLayer {
      read {
        descriptor[HistoryConfig].from(
          TypesafeConfigSource.fromResourcePath
            .at(PropertyTreePath.$("history"))
        )
      }
    }
}