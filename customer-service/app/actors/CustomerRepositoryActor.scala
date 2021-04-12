package actors

import akka.actor.Actor
import model.Customer
import model.commands.{ AddCustomer, DeleteCustomer, RetrieveCustomers, UpdateCustomer }
import model.events.{ CustomerAdded, CustomerDeleted, CustomerNotFound, CustomerUpdated, CustomersRetrieved }
import play.api.Logger

class CustomerRepositoryActor extends Actor  {

  private val logger = Logger ( getClass )

  private var customers = Map [Int, Customer] ()

  override def receive: Receive = {

    case RetrieveCustomers => {

      logger.info ( s"Retrieving customers" )

      sender () ! CustomersRetrieved ( customers.values.toSeq )
    }

    case AddCustomer ( customer ) => {

      val customerId = customers.size + 1
      val customerToAdd = Customer ( customerId, customer.name )
      customers = customers + ( customerId -> customerToAdd )
      logger.info ( s"Adding customer $customerToAdd with customerId $customerId" )

      sender () ! CustomerAdded ( customerToAdd )
    }

    case UpdateCustomer ( customerId, customer ) => {

      customers.get ( customerId ) match {

        case Some ( foundCustomer ) => {

          customers = customers + ( customerId -> customer )
          logger.info ( s"Updating customer, customerId: $customerId with value $customer" )

          sender () ! CustomerUpdated ( customer )
        }

        case None => {

          logger.info ( s"Unable to find customer, customerId: $customerId" )

          sender () ! CustomerNotFound ( customerId )
        }
      }
    }

    case DeleteCustomer ( customerId: Int ) => {

      logger.info ( s"Deleting customer with customerId $customerId" )
      customers = customers - customerId

      sender () ! CustomerDeleted ( customerId )
    }

    case message => logger.warn ( s"Unsupported command $message" )
  }
}