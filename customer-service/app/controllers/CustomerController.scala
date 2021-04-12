package controllers

import actors.CustomerRepositoryActor
import akka.actor.{ ActorSystem, Props }
import model.{ ApiError, Customer }
import model.commands.{ AddCustomer, DeleteCustomer, RetrieveCustomers, UpdateCustomer }

import javax.inject._
import play.api._
import play.api.libs.json._
import play.api.mvc._
import akka.pattern.ask
import akka.util.Timeout
import model.events.{ CustomerAdded, CustomerDeleted, CustomerNotFound, CustomerUpdated, CustomersRetrieved }

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.duration._

@Singleton
class CustomerController @Inject () ( val controllerComponents: ControllerComponents, val system: ActorSystem ) ( implicit ec: ExecutionContext ) extends BaseController {

  implicit val timeout: Timeout = 5.seconds
  implicit val customerReads = Json.reads [Customer]
  implicit val customerWrites = Json.writes [Customer]
  implicit val apiErrorWrites = Json.writes [ApiError]

  private val logger = Logger ( getClass )

  private val customerRepositoryActor = system.actorOf ( Props [CustomerRepositoryActor] )


  def getCustomers () = Action.async { implicit request =>

    logger.info ( "getCustomers ()" )

    ( customerRepositoryActor ? RetrieveCustomers ).map {

      case CustomersRetrieved ( customers ) => Ok ( Json.toJson ( customers ) )

      case message => InternalServerError ( Json.toJson ( ApiError ( 500, s"Unable to retrieve customers" ) ) )
    }
  }

  def createCustomer () = Action.async { implicit request =>

    request.body.asJson.map { body =>

      Json.fromJson ( body ) match {

        case JsSuccess ( customer, path ) => {

          logger.info ( s"createCustomer ( $customer )" )

          ( customerRepositoryActor ? AddCustomer ( customer ) ).map {

            case CustomerAdded ( savedCustomer ) => Ok ( Json.toJson ( savedCustomer ) )

            case message => InternalServerError ( Json.toJson ( ApiError ( 500, s"Unable to create customer $customer" ) ) )
          }
        }

        case e @ JsError ( _ )  => {

          logger.error ( s"Unable to create customer $request.body.asJson" )
          Future { BadRequest ( Json.toJson ( ApiError ( 400, s"Invalid body" ) ) ) }
        }
      }
    }.getOrElse ( Future { InternalServerError ( Json.toJson ( ApiError ( 500, s"Unable to create customer $request.body.asJson" ) ) ) } )
  }

  def updateCustomer ( id: Int ) = Action.async { implicit request =>

    request.body.asJson.map { body =>

      Json.fromJson ( body ) match {

        case JsSuccess ( customer, path ) => {

          logger.info ( s"updateCustomer ( $id )" )

          ( customerRepositoryActor ? UpdateCustomer ( id, customer ) ).map {

            case CustomerUpdated ( updatedCustomer ) => Ok ( Json.toJson ( updatedCustomer ) )

            case CustomerNotFound ( customerId ) => NotFound ( Json.toJson ( ApiError ( 404, s"Customer customerId $customerId not found" ) ) )

            case message => {

              InternalServerError ( Json.toJson ( ApiError ( 500, s"Unable to update customer, customerId $id" ) ) )
            }
          }
        }

        case e @ JsError ( _ )  => {

          logger.error ( s"Unable to update customer $id ERRORS: ${e.errors}" )
          Future { BadRequest ( Json.toJson ( ApiError ( 400, s"Invalid body" ) ) ) }
        }
      }
    }.getOrElse ( {

      logger.error ( s"Bad request. Unable to update customer $id" )
      Future { BadRequest ( Json.toJson ( ApiError ( 400, s"Unable to update customer $id" ) ) ) }
    } )
  }

  def deleteCustomer ( customerId: Int ) = Action.async { implicit request =>

    logger.info ( s"deleteCustomer ( $customerId )" )

    ( customerRepositoryActor ? DeleteCustomer ( customerId ) ).map {

      case CustomerDeleted ( customerId ) => Ok ( Json.toJson ( customerId ) )

      case message => InternalServerError ( Json.toJson ( ApiError ( 500, s"Unable to delete customer $customerId" ) ) )
    }
  }
}