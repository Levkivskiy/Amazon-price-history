package server

import exception.{PriceHistoryError, SubscriptionError}
import model.{PriceChangesHistory, SubscriptionId, SubscriptionLink}
import service.AmazonPriceChangesService
import sttp.apispec.openapi.circe.yaml.RichOpenAPI
import sttp.model.StatusCode
import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
import sttp.tapir.json.zio._
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import sttp.tapir.swagger.SwaggerUI
import sttp.tapir.ztapir._
import zhttp.http.{Http, HttpApp, Request, Response}
import zio.{Task, ZIO, ZLayer}

import java.time.LocalDate

trait AmazonPriceServer {
  def httpRoutes: ZIO[Any, Nothing, HttpApp[Any, Throwable]]
}

object AmazonPriceServer {
  def httpRoutes: ZIO[AmazonPriceServer, Nothing, HttpApp[Any, Throwable]] =
    ZIO.serviceWithZIO[AmazonPriceServer](_.httpRoutes)
}

final case class AmazonPriceServerLive(amazonPriceChangesService: AmazonPriceChangesService) extends AmazonPriceServer {

  private val priceChangesHistory = List(
    PriceChangesHistory(10, LocalDate.of(2023, 7, 1)),
    PriceChangesHistory(11, LocalDate.of(2023, 8, 1)),
    PriceChangesHistory(12, LocalDate.of(2023, 9, 1))
  )

  private val baseEndpoint = endpoint.in("api").in("v1")

  private val validationFailedSubscriptionErrorOut = oneOf[SubscriptionError](
    oneOfVariant(StatusCode.BadRequest, jsonBody[SubscriptionError.ValidationFailed].example(SubscriptionError.ValidationFailed("Validation Error")))
  )
  private val invalidInputSubscriptionErrorOut = oneOf[SubscriptionError](
    oneOfVariant(StatusCode.BadRequest, jsonBody[SubscriptionError.InvalidInput].example(SubscriptionError.InvalidInput.link("http://foo")))
  )
  private val invalidInputPriceChangesErrorOut = oneOf[PriceHistoryError](
    oneOfVariant(StatusCode.BadRequest, jsonBody[PriceHistoryError.InvalidInput].example(PriceHistoryError.InvalidInput.id(1)))
  )

  private val priceChangesHistoryList = jsonBody[List[PriceChangesHistory]].example(priceChangesHistory)
  private val subscriptionId = jsonBody[SubscriptionId].example(SubscriptionId(1))
  private val subscriptionLink = jsonBody[SubscriptionLink].example(SubscriptionLink("https://www.amazon.com/Functional-Programming-Scala-Paul-Chiusano/dp/1617290653"))

  private val subscribePriceChangesEndpoint =
    baseEndpoint.post
      .in("subscribe")
      .in(subscriptionLink)
      .out(subscriptionId)
      .errorOut(validationFailedSubscriptionErrorOut)

  private val deletePriceChangesSubscriptions =
    baseEndpoint.delete
      .in("subscribe")
      .in(path[Int]("id"))
      .errorOut(invalidInputSubscriptionErrorOut)

  private val getPriceChangesHistory =
    baseEndpoint.get
      .in("history")
      .in(path[Int]("id"))
      .out(priceChangesHistoryList)
      .errorOut(invalidInputPriceChangesErrorOut)

  private val allRoutes: Http[Any, Throwable, Request, Response] =
    ZioHttpInterpreter().toHttp {
      List(subscribePriceChangesEndpoint.zServerLogic(subscription => amazonPriceChangesService.subscribePriceChanges(subscription.link)),
        getPriceChangesHistory.zServerLogic(id => amazonPriceChangesService.getPriceChangesHistory(id)),
        deletePriceChangesSubscriptions.zServerLogic(id => amazonPriceChangesService.deletePriceChangesSubscriptions(id)))
    }

  private val endpoints = {
    val endpoints = List(
      subscribePriceChangesEndpoint,
      getPriceChangesHistory,
      deletePriceChangesSubscriptions
    )
    endpoints.map(_.tags(List("Amazon Price History Endpoints")))
  }

  override def httpRoutes: ZIO[Any, Nothing, HttpApp[Any, Throwable]] =
    for {
      openApi <- ZIO.succeed(OpenAPIDocsInterpreter().toOpenAPI(endpoints, "Amazon Price History Service", "0.1"))
      routesHttp <- ZIO.succeed(allRoutes)
      endPointsHttp <- ZIO.succeed(ZioHttpInterpreter().toHttp(SwaggerUI[Task](openApi.toYaml)))
    } yield routesHttp ++ endPointsHttp
}

object AmazonPriceServerLive {
  val live: ZLayer[AmazonPriceChangesService, Nothing, AmazonPriceServerLive] = ZLayer.fromFunction(AmazonPriceServerLive.apply _)
}