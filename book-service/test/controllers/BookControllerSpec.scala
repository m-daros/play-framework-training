package controllers

import akka.actor.ActorSystem
import model.{ ApiError, Book }
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.http.Status.NOT_FOUND
import play.api.libs.json.{ Json, Reads }
import play.api.mvc.Results
import play.api.test._
import play.api.test.Helpers._

import scala.concurrent.ExecutionContext.global._
import java.util.concurrent.Executors
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ Await, ExecutionContext }


class BookControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting with Results {

  val system: ActorSystem = ActorSystem ( "BookControllerSpec" )

  val bookReads = Json.reads [Book]
  val bookWrites = Json.writes [Book]
  val booksReads = Reads.seq ( bookReads )
  val apiErrorReads = Json.reads [ApiError]

  implicit val ec: ExecutionContext = ExecutionContext.fromExecutor ( Executors.newFixedThreadPool ( 10 ) )


  "BookController GET /api/v1/books" should {

    "retrieve all the books" in {

      // GIVEN I have no initial books

      // WHEN I ask to get all the books
      val Some ( result ) = route ( app, FakeRequest ( GET, routes.BookController.getBooks ().path () ) )

      val books = booksReads.reads ( contentAsJson ( result ) ).get

      // THEN I expect a response with 200 status code, application/json Content-Type
      assert ( status ( result ).equals ( OK ) )
      assert ( contentType ( result ).contains ( "application/json" ) )

      // AND the body contains no books
      assert ( books.isEmpty )
    }
  }

  "BookController GET /api/v1/books after POST /api/v1/books" should {

    "retrieve all the books" in {

      // GIVEN I have no initial books
      // AND I created one book
      addbook ( Book ( 0, "Jack" ) )

      // WHEN I ask to get all books
      val Some ( result ) = route ( app, FakeRequest ( GET, routes.BookController.getBooks ().path () ) )

      val books = booksReads.reads ( contentAsJson ( result ) ).get

      // THEN I expect a response with 200 status code, application/json Content-Type
      assert ( status ( result ).equals ( OK ) )
      assert ( contentType ( result ).contains ( "application/json" ) )

      // AND the body contains a list with only the book I've created
      assert ( books.equals ( Seq ( Book ( 1, "Jack" ) ) ) )
    }
  }

  "BookController POST /api/v1/books" should {

    "add a book" in {

      val book = Book ( 0, "Jack" )

      val request = FakeRequest ( POST, routes.BookController.createBook ().path () ).withJsonBody ( bookWrites.writes ( book ) )

      // WHEN I ask to create a book
      val Some ( result ) = route ( app, request )

      val addedbook = bookReads.reads ( contentAsJson ( result ) ).get

      // THEN I expect a response with 200 status code, application/json Content-Type with
      assert ( status ( result ).equals ( OK ) )
      assert ( contentType ( result ).contains ( "application/json" ) )

      // AND the body contains the book I added
      assert ( addedbook.equals ( Book ( 1, "Jack" ) ) )
    }
  }

  "BookController PUT /api/v1/books/1 after POST /api/v1/books" should {

    "update the book with id 1" in {

      // GIVEN I have no initial books
      // AND I created one book
      addbook ( Book ( 0, "Mark" ) )

      val book = Book ( 1, "Luke" )

      // WHEN I ask to update book with id 1
      val Some ( result ) = route ( app, FakeRequest ( PUT, routes.BookController.updateBook ( 1 ).path () ).withJsonBody ( bookWrites.writes ( book ) ) )

      val updatedbook = bookReads.reads ( contentAsJson ( result ) ).get

      // THEN I expect a response with 200 status code, application/json Content-Type
      assert ( status ( result ).equals ( OK ) )
      assert ( contentType ( result ).contains ( "application/json" ) )

      // AND the body contains the book I updated
      assert ( updatedbook.equals ( book ) )
    }
  }

  "BookController PUT /api/v1/books/1 having no books" should {

    "return NotFound 404" in {

      // GIVEN I have no initial books

      val book = Book ( 1, "Luke" )

      // WHEN I ask to update book with id 1
      val Some ( result ) = route ( app, FakeRequest ( PUT, routes.BookController.updateBook ( 1 ).path () ).withJsonBody ( bookWrites.writes ( book ) ) )

      val apiError = apiErrorReads.reads ( contentAsJson ( result ) ).get

      // THEN I expect a response with 404 status code, application/json Content-Type
      assert ( status ( result ).equals ( NOT_FOUND ) )
      assert ( contentType ( result ).contains ( "application/json" ) )

      // AND the body contains an ApiError with status 404 and the message contains the custoimerId 1
      assert ( apiError.status.equals ( NOT_FOUND ) )
      assert ( apiError.message.contains ( "bookId 1 not found" ) )
    }
  }

  "BookController DELETE /api/v1/books/1 after POST /api/v1/books" should {

    "delete the book with id 1" in {

      // GIVEN I have no initial books
      // AND I created one book
      addbook ( Book ( 0, "Mark" ) )

      // WHEN I ask to delete book with id 1
      val Some ( result ) = route ( app, FakeRequest ( DELETE, routes.BookController.updateBook ( 1 ).path () ) )

      // THEN I expect a response with 200 status code, application/json Content-Type
      assert ( status ( result ).equals ( OK ) )
      assert ( contentType ( result ).contains ( "application/json" ) )

      // AND there are no books
      assert ( getbooks ().equals ( Seq () ) )
    }
  }

  private def addbook ( book: Book ): Unit = {

    val request = FakeRequest ( POST, routes.BookController.createBook ().path () ).withJsonBody ( bookWrites.writes ( book ) )
    val Some ( res ) = route ( app, request )
    Await.result ( res, 5.seconds )
  }

  private def getbooks (): Seq [Book] = {

    val Some ( result ) = route ( app, FakeRequest ( GET, routes.BookController.getBooks ().path () ) )

    booksReads.reads ( contentAsJson ( result ) ).get
  }
}