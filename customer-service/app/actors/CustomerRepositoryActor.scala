package actors

import slick.jdbc.PostgresProfile.api._
import akka.actor.Actor
import model.Customer
import model.Tables.{ Customers, CustomersRow }
import model.commands.{ AddCustomer, DeleteCustomer, RetrieveCustomers, UpdateCustomer }
import model.events.{ CustomerAdded, CustomerDeleted, CustomerNotFound, CustomerUpdated, CustomersRetrieved }
import play.api.Logger
import play.api.db.slick.{ DatabaseConfigProvider, HasDatabaseConfigProvider }
import slick.jdbc.JdbcProfile

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.util.{ Failure, Success }

class CustomerRepositoryActor @Inject () ( protected val dbConfigProvider: DatabaseConfigProvider ) ( implicit ec: ExecutionContext )
  extends Actor
  with HasDatabaseConfigProvider [JdbcProfile] {

  private val logger = Logger ( getClass )

  override def receive: Receive = {

    case RetrieveCustomers => {

      logger.info ( s"Retrieving customers" )

      val replyTo = sender ()

      val allCustomers = db.run ( Customers.result )
        .map ( customerRows => customerRows.map ( customerRow => Customer ( customerRow.id, customerRow.name ) ) )

      allCustomers.onComplete {

        // TODO Manage Failure
        case Success ( customers ) => {

          replyTo ! CustomersRetrieved ( customers )
        }

        case Failure ( exception ) => logger.error ( s"Unable to find customers", exception )

        case message => logger.error ( s"Unable to understand $message" )
      }
    }

    case AddCustomer ( customer ) => {

      val replyTo = sender ()

      val customerId = db.run { ( Customers returning Customers.map ( _.id ) ) += CustomersRow ( customer.id, customer.name ) }

      customerId onComplete {

        case Success ( id ) => {

          val addedCustomer = Customer ( id, customer.name )
          logger.info ( s"Adding customer $addedCustomer with customerId $customerId" )
          replyTo ! CustomerAdded ( addedCustomer )
        }

        case Failure ( exception ) => logger.error ( s"Unable to create customer $customer", exception )

        case message => logger.error ( s"Unable to understand $message" )
      }
    }

    case UpdateCustomer ( customerId, customer ) => {

      val replyTo = sender ()

      val numUpdated = db.run ( Customers.filter ( _.id === customerId ).update ( CustomersRow ( customerId, customer.name ) ) )

      numUpdated.onComplete {

        case Success ( 0 ) => {

          logger.info ( s"Unable to find customer, customerId: $customerId" )
          replyTo ! CustomerNotFound ( customerId )
        }

        case Success ( n ) if n > 0 => {

          val updatedCustomer = Customer ( customerId, customer.name )
          logger.info ( s"Updating customer, customerId: $customerId with value $updatedCustomer" )

          replyTo ! CustomerUpdated ( updatedCustomer )
        }

        case Failure ( exception ) => {

          logger.error ( s"Unable to update customer customer, customerId due to ${exception.getMessage}" )

          // TODO Send a message to notify the caller
        }
      }
    }

    case DeleteCustomer ( customerId: Int ) => {

      logger.info ( s"Deleting customer with customerId $customerId" )
      val replyTo = sender ()

      val numDeleted = db.run ( Customers.filter ( _.id === customerId ).delete )

      numDeleted.onComplete {

        case Success ( 0 ) => {

          // TODO maybe it's better to avoid customer not found when deleting
          logger.info ( s"Unable to find customer, customerId: $customerId" )
          replyTo ! CustomerNotFound ( customerId )
        }

        case Success ( n ) if n > 0 => {

          logger.info ( s"Deleting customer with customerId: $customerId" )

          replyTo ! CustomerDeleted ( customerId )
        }

        case Failure ( exception ) => {

          logger.error ( s"Unable to delete customer, $customerId due to ${exception.getMessage}" )

          // TODO Send a message to notify the caller
        }
      }
    }

    case message => logger.warn ( s"Unsupported command $message" )
  }
}