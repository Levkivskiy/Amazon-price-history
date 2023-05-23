package model

import java.time.LocalDate
import java.util.Date

case class PriceHistory(subscription_id: Int, price: Double, date: LocalDate)
