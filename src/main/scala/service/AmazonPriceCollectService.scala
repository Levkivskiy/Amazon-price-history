package service

import config.PriceChangesConfig
import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import model.PriceHistory
import org.jsoup.Jsoup
import zio._

import java.time.LocalDate

trait AmazonPriceCollectService {
  def collectPriceChanges: ZIO[Any, Exception, Long]
}

object AmazonPriceCollectService {
  def collectPriceChanges: ZIO[AmazonPriceCollectService, Exception, Long] =
    ZIO.serviceWithZIO[AmazonPriceCollectService](_.collectPriceChanges)
}

final case class AmazonPriceCollectServiceLive(priceChangesConfig: PriceChangesConfig,
                                               priceHistoryService: PriceHistoryService,
                                               subscriptionService: SubscriptionService
                                              ) extends AmazonPriceCollectService {

  private def getPrice(url: String): ZIO[Any, RuntimeException, Double] = ZIO.blocking {
    val document = Jsoup.connect(url).userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.61 Safari/537.36").get
    val priceElement = document.getElementById("priceValue")
    ZIO.fromEither(
      Option(priceElement) match {
        case Some(element) =>
          Right(element.attr("value").toDouble)
        case None =>
          Left(new RuntimeException("Failed to extract price from Amazon product page"))
      })
  }

  private val task: ZIO[Any, Exception, Unit] = for {
    allSubscription <- subscriptionService.getAllSubscriptions
    prices <- ZIO.collectAllPar(allSubscription.map {
      sub =>
        getPrice(sub.link).map {
          (sub.id, _)
        }
    })
    _ <- ZIO.collectAllParDiscard(prices.map { case (id, price) =>
      priceHistoryService.createPriceHistory(PriceHistory(id, price, LocalDate.now()))
    })
  } yield ()

  private val schedule = Schedule.fixed(priceChangesConfig.intervalS.second)

  val collectPriceChanges: ZIO[Any, Exception, Long] = task.repeat(schedule)

}

object AmazonPriceCollectServiceLive {
  val live: ZLayer[PriceChangesConfig with PriceHistoryService with SubscriptionService, Nothing, AmazonPriceCollectServiceLive] =
    ZLayer.fromFunction(AmazonPriceCollectServiceLive.apply _)
}

object MainApp extends ZIOAppDefault {

  val quillLayer = Quill.Postgres.fromNamingStrategy(SnakeCase)
  val dsLayer = Quill.DataSource.fromPrefix("database")

  def run = AmazonPriceCollectService.collectPriceChanges
    .provide(
      quillLayer,
      dsLayer,
      PriceChangesConfig.live,
      SubscriptionServiceLive.live,
      PriceHistoryServiceLive.live,
      AmazonPriceCollectServiceLive.live)
}