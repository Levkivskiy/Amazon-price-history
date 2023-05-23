package service

import config.HistoryConfig
import exception.{PriceHistoryError, SubscriptionError}
import model.{PriceChangesHistory, SubscriptionId}
import zio.{ZIO, ZLayer}

import java.time.{Clock, LocalDate}

trait AmazonPriceChangesService {
  def subscribePriceChanges(link: String): ZIO[Any, SubscriptionError, SubscriptionId]

  def getPriceChangesHistory(id: Int): ZIO[Any, PriceHistoryError, List[PriceChangesHistory]]

  def deletePriceChangesSubscriptions(id: Int): ZIO[Any, SubscriptionError, Unit]
}

object AmazonPriceChangesService {
  def subscribePriceChanges(link: String): ZIO[AmazonPriceChangesService, SubscriptionError, SubscriptionId] =
    ZIO.serviceWithZIO[AmazonPriceChangesService](_.subscribePriceChanges(link))

  def getPriceChangesHistory(id: Int): ZIO[AmazonPriceChangesService, PriceHistoryError, List[PriceChangesHistory]] =
    ZIO.serviceWithZIO[AmazonPriceChangesService](_.getPriceChangesHistory(id))

  def deletePriceChangesSubscriptions(id: Int): ZIO[AmazonPriceChangesService, SubscriptionError, Unit] =
    ZIO.serviceWithZIO[AmazonPriceChangesService](_.deletePriceChangesSubscriptions(id))
}

final case class AmazonPriceChangesServiceLive(historyConfig: HistoryConfig,
                                               subscriptionService: SubscriptionService,
                                               priceHistoryService: PriceHistoryService
                                              ) extends AmazonPriceChangesService {

  def subscribePriceChanges(link: String): ZIO[Any, SubscriptionError, SubscriptionId] =
    link match {
      case s"https://www.amazon.com/$_" =>
        subscriptionService.createSubscription(link)
          .map(s => SubscriptionId(s.id))
          .mapError(_ => SubscriptionError.InvalidInput.link(link))
      case _ => ZIO.fail(SubscriptionError.ValidationFailed("Link must start with https://www.amazon.com/"))
    }

  def getPriceChangesHistory(id: Int): ZIO[Any, PriceHistoryError, List[PriceChangesHistory]] =
    priceHistoryService.getPricesHistory(id, LocalDate.now(Clock.systemUTC()).minusDays(historyConfig.daysUntil))
      .map(_.map {
        p => PriceChangesHistory(p.price, p.date)
      })
      .mapError(_ => PriceHistoryError.InvalidInput.id(id))

  def deletePriceChangesSubscriptions(id: Int): ZIO[Any, SubscriptionError, Unit] =
    subscriptionService.deleteSubscription(id)
      .map(_ => ())
      .mapError(_ => SubscriptionError.InvalidInput.id(id))
}

object AmazonPriceChangesServiceLive {
  val live: ZLayer[HistoryConfig with SubscriptionService with PriceHistoryService, Nothing, AmazonPriceChangesService] =
    ZLayer.fromFunction(AmazonPriceChangesServiceLive.apply _)
}
