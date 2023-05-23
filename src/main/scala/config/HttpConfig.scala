package config

import zio.ZLayer
import zio.config.magnolia.descriptor
import zio.config.{PropertyTreePath, ReadError, read}
import zio.config.typesafe.TypesafeConfigSource

case class HttpConfig(port: Int,
                      host: String)

object HttpConfig {
  val live: ZLayer[Any, ReadError[String], HttpConfig] =
    ZLayer {
      read {
        descriptor[HttpConfig].from(
          TypesafeConfigSource.fromResourcePath
            .at(PropertyTreePath.$("http"))
        )
      }
    }
}