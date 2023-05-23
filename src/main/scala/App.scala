import config.HttpConfig
import server.AmazonPriceServer
import service.AmazonPriceCollectService
import zhttp.service.{EventLoopGroup, Server, ServerChannelFactory}
import zio.ZIO

object App {
    def server: ZIO[HttpConfig with EventLoopGroup with AmazonPriceServer with AmazonPriceCollectService with ServerChannelFactory, Exception, Unit] = ZIO.scoped {
      for {
        config  <- ZIO.service[HttpConfig]
        httpApp <- AmazonPriceServer.httpRoutes
        start <- Server(httpApp).withBinding(config.host, config.port).make.orDie
        _ <- ZIO.logInfo(s"Server started on port: ${start.port}")
        _ <- AmazonPriceCollectService.collectPriceChanges
        _ <- ZIO.never
      } yield ()
    }
}