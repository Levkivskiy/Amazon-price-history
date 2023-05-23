package service

import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import model.PriceHistory
import zio._

import java.sql.SQLException
import java.time.LocalDate

trait PriceHistoryService {
  def createPriceHistory(price: PriceHistory): ZIO[Any, SQLException, PriceHistory]

  def getPricesHistory(id: Int, untilDate: LocalDate): ZIO[Any, SQLException, List[PriceHistory]]
}

object PriceHistoryService {
  def createPriceHistory(price: PriceHistory): ZIO[PriceHistoryService, SQLException, PriceHistory] =
    ZIO.serviceWithZIO[PriceHistoryService](_.createPriceHistory(price))

  def getPricesHistory(id: Int, untilDate: LocalDate): ZIO[PriceHistoryService, SQLException, List[PriceHistory]] =
    ZIO.serviceWithZIO[PriceHistoryService](_.getPricesHistory(id, untilDate))
}

final case class PriceHistoryServiceLive(quill: Quill.Postgres[SnakeCase]) extends PriceHistoryService {

  import quill._
  import extras._

  override def createPriceHistory(price: PriceHistory): ZIO[Any, SQLException, PriceHistory] = run {
    quote {
      query[PriceHistory].insertValue(lift(price)).returning(x => x)
    }
  }

  override def getPricesHistory(id: Int, untilDate: LocalDate): ZIO[Any, SQLException, List[PriceHistory]] = run {
    quote {
      query[PriceHistory].filter { history =>
        history.subscription_id == lift(id) && history.date > lift(untilDate)
      }
    }
  }
}

object PriceHistoryServiceLive {
  val live: ZLayer[Quill.Postgres[SnakeCase], Nothing, PriceHistoryService] = ZLayer.fromFunction(PriceHistoryServiceLive.apply _)
}
