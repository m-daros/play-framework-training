package model

import java.util.Date

case class Order ( id: Int, customer: Customer, books: Seq [Book], date: Date )