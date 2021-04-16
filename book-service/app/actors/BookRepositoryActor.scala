package actors

import akka.actor.Actor
import model.Book
import model.commands.{ AddBook, DeleteBook, RetrieveBooks, UpdateBook }
import model.events.{ BookAdded, BookDeleted, BookNotFound, BookUpdated, BooksRetrieved }
import play.api.Logger

class BookRepositoryActor extends Actor  {

  private val logger = Logger ( getClass )

  private var books = Map [Int, Book] ()

  override def receive: Receive = {

    case RetrieveBooks => {

      logger.info ( s"Retrieving books" )

      sender () ! BooksRetrieved ( books.values.toSeq )
    }

    case AddBook ( book ) => {

      val bookId = books.size + 1
      val bookToAdd = Book ( bookId, book.name )
      books = books + ( bookId -> bookToAdd )
      logger.info ( s"Adding book $bookToAdd with bookId $bookId" )

      sender () ! BookAdded ( bookToAdd )
    }

    case UpdateBook ( bookId, book ) => {

      books.get ( bookId ) match {

        case Some ( foundBook ) => {

          books = books + ( bookId -> book )
          logger.info ( s"Updating book, bookId: $bookId with value $book" )

          sender () ! BookUpdated ( book )
        }

        case None => {

          logger.info ( s"Unable to find book, bookId: $bookId" )

          sender () ! BookNotFound ( bookId )
        }
      }
    }

    case DeleteBook ( bookId: Int ) => {

      logger.info ( s"Deleting book with bookId $bookId" )
      books = books - bookId

      sender () ! BookDeleted ( bookId )
    }

    case message => logger.warn ( s"Unsupported command $message" )
  }
}