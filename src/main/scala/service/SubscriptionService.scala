package service

import io.getquill.{Delete, SnakeCase}
import io.getquill.jdbczio.Quill
import model.Subscription
import zio.{ZIO, ZLayer}

import java.sql.SQLException

trait SubscriptionService {
  def createSubscription(link: String): ZIO[Any, SQLException, Subscription]

  def getSubscription(id: Int): ZIO[Any, SQLException, Option[Subscription]]

  def deleteSubscription(id: Int): ZIO[Any, SQLException, Subscription]

  def getAllSubscriptions: ZIO[Any, SQLException, List[Subscription]]
}

object SubscriptionService {
  def createSubscription(link: String): ZIO[SubscriptionService, SQLException, Subscription] =
    ZIO.serviceWithZIO[SubscriptionService](_.createSubscription(link))

  def getSubscription(id: Int): ZIO[SubscriptionService, SQLException, Option[Subscription]] =
    ZIO.serviceWithZIO[SubscriptionService](_.getSubscription(id))

  def deleteSubscription(id: Int): ZIO[SubscriptionService, SQLException, Subscription] =
    ZIO.serviceWithZIO[SubscriptionService](_.deleteSubscription(id))

  def getAllSubscriptions: ZIO[SubscriptionService, SQLException, List[Subscription]] =
    ZIO.serviceWithZIO[SubscriptionService](_.getAllSubscriptions)
}

final case class SubscriptionServiceLive(quill: Quill.Postgres[SnakeCase]) extends SubscriptionService {

  import quill._

  override def createSubscription(link: String): ZIO[Any, SQLException, Subscription] = run {
    quote {
      query[Subscription].insert(_.link -> lift(link)).returning(x => x)
    }
  }

  override def getSubscription(id: Int): ZIO[Any, SQLException, Option[Subscription]] = run {
    quote {
      query[Subscription].filter(_.id == lift(id))
    }
  }.map(_.headOption)

  override def deleteSubscription(id: Int): ZIO[Any, SQLException, Subscription] = run {
    quote {
      query[Subscription].filter(_.id == lift(id)).delete.returning(x => x)
    }
  }

  override def getAllSubscriptions: ZIO[Any, SQLException, List[Subscription]] = run {
    quote {
      query[Subscription]
    }
  }
}

object SubscriptionServiceLive {
  val live: ZLayer[Quill.Postgres[SnakeCase], Nothing, SubscriptionService] = ZLayer.fromFunction(SubscriptionServiceLive.apply _)
}