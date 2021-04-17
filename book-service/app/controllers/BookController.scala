package controllers

import actors.BookRepositoryActor
import akka.actor.{ ActorSystem, Props }
import model.{ ApiError, Book }
import model.commands.{ AddBook, DeleteBook, RetrieveBooks, UpdateBook }

import javax.inject._
import play.api._
import play.api.libs.json._
import play.api.mvc._
import akka.pattern.ask
import akka.util.Timeout
import model.events.{ BookAdded, BookDeleted, BookNotFound, BookUpdated, BooksRetrieved }

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.duration._

@Singleton
class BookController @Inject () ( val controllerComponents: ControllerComponents, val system: ActorSystem ) ( implicit ec: ExecutionContext ) extends MyBaseController {

  private implicit val timeout: Timeout = 5.seconds
  private implicit val bookReads: Reads [Book] = Json.reads [Book]
  private implicit val bookWrites: OWrites [Book] = Json.writes [Book]

  private val logger = Logger ( getClass )

  private val bookRepositoryActor = system.actorOf ( Props [BookRepositoryActor] )


  def getBooks (): Action [AnyContent] = Action.async { implicit request =>

    logger.info ( "getBooks ()" )

    ( bookRepositoryActor ? RetrieveBooks ).map {

      case BooksRetrieved ( books ) => Ok ( Json.toJson ( books ) )

      case message => InternalServerError ( jsonApiError ( 500, s"Unable to retrieve books" ) )
    }
  }

  def createBook (): Action [AnyContent ] = Action.async { implicit request =>

    request.body.asJson.map { body =>

      Json.fromJson ( body ) match {

        case JsSuccess ( book, path ) => {

          logger.info ( s"createBook ( $book )" )

          ( bookRepositoryActor ? AddBook ( book ) ).map {

            case BookAdded ( savedbook ) => Ok ( Json.toJson ( savedbook ) )

            case message => InternalServerError ( jsonApiError ( 500, s"Unable to create book $book" ) )
          }
        }

        case e @ JsError ( _ )  => {

          logger.error ( s"Unable to create book $request.body.asJson" )
          Future { BadRequest ( jsonApiError ( 400, s"Invalid body" ) ) }
        }
      }
    }.getOrElse ( Future { InternalServerError ( jsonApiError ( 500, s"Unable to create book $request.body.asJson" ) ) } )
  }

  def updateBook (id: Int ): Action [AnyContent] = Action.async { implicit request =>

    request.body.asJson.map { body =>

      Json.fromJson ( body ) match {

        case JsSuccess ( book, path ) => {

          logger.info ( s"updateBook ( $id )" )

          ( bookRepositoryActor ? UpdateBook ( id, book ) ).map {

            case BookUpdated ( updatedbook ) => Ok ( Json.toJson ( updatedbook ) )

            case BookNotFound ( bookId ) => NotFound ( jsonApiError ( 404, s"Book bookId $bookId not found" ) )

            case message => {

              InternalServerError ( jsonApiError ( 500, s"Unable to update book, bookId $id" ) )
            }
          }
        }

        case e @ JsError ( _ )  => {

          logger.error ( s"Unable to update book $id ERRORS: ${e.errors}" )
          Future { BadRequest ( jsonApiError ( 400, s"Invalid body" ) ) }
        }
      }
    }.getOrElse ( {

      logger.error ( s"Bad request. Unable to update book $id" )
      Future { BadRequest ( jsonApiError ( 400, s"Unable to update book $id" ) ) }
    } )
  }

  def deleteBook ( bookId: Int ): Action [AnyContent] = Action.async { implicit request =>

    logger.info ( s"deleteBook ( $bookId )" )

    ( bookRepositoryActor ? DeleteBook ( bookId ) ).map {

      case BookDeleted ( bookId ) => Ok ( Json.toJson ( bookId ) )

      case message => InternalServerError ( jsonApiError ( 500, s"Unable to delete book $bookId" ) )
    }
  }
}