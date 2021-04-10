package model.events

import model.Customer

case class CustomersRetrieved ( customers: Seq [Customer] )