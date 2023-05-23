import config.{HistoryConfig, HttpConfig, PriceChangesConfig}
import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import server.AmazonPriceServerLive
import service.{AmazonPriceChangesServiceLive, AmazonPriceCollectServiceLive, PriceHistoryServiceLive, SubscriptionServiceLive}
import zio.{Scope, ZIO, ZIOAppArgs, ZIOAppDefault, ZLayer}

object Main extends ZIOAppDefault {

  val quillLayer = Quill.Postgres.fromNamingStrategy(SnakeCase)
  val dsLayer = Quill.DataSource.fromPrefix("database")

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = {
    App.server.provide(
      quillLayer,
      dsLayer,
      SubscriptionServiceLive.live,
      PriceHistoryServiceLive.live,
      AmazonPriceChangesServiceLive.live,
      AmazonPriceCollectServiceLive.live,
      AmazonPriceServerLive.live,
      HttpServerSettings.default,
      HistoryConfig.live,
      HttpConfig.live,
      PriceChangesConfig.live,
      ZLayer.Debug.tree)
  }
}