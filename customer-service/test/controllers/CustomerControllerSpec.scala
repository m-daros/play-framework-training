package controllers

import akka.actor.ActorSystem
import model.{ ApiError, Customer }
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.http.Status.NOT_FOUND
import play.api.libs.json.{ Json, Reads }
import play.api.mvc.Results
import play.api.test._
import play.api.test.Helpers._

//import scala.concurrent.ExecutionContext.global._
import java.util.concurrent.Executors
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ Await, ExecutionContext }

// TODO Mock CustomerRepositoryActor
class CustomerControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting with Results {

  val system: ActorSystem = ActorSystem ( "CustomerControllerSpec" )

  val customerReads = Json.reads [Customer]
  val customerWrites = Json.writes [Customer]
  val customersReads = Reads.seq ( customerReads )
  val apiErrorReads = Json.reads [ApiError]

  implicit val ec: ExecutionContext = ExecutionContext.fromExecutor ( Executors.newFixedThreadPool ( 10 ) )


  "CustomerController GET /api/v1/customers" should {

    "retrieve all the customers" in {

      // GIVEN I have no initial customers

      // WHEN I ask to get all the customers
      val Some ( result ) = route ( app, FakeRequest ( GET, routes.CustomerController.getCustomers ().path () ) )

      val customers = customersReads.reads ( contentAsJson ( result ) ).get

      // THEN I expect a response with 200 status code, application/json Content-Type
      assert ( status ( result ).equals ( OK ) )
      assert ( contentType ( result ).contains ( "application/json" ) )

      // AND the body contains no customers
      assert ( customers.isEmpty )
    }
  }

  "CustomerController GET /api/v1/customers after POST /api/v1/customers" should {

    "retrieve all the customers" in {

      // GIVEN I have no initial customers
      // AND I created one customer
      addCustomer ( Customer ( 0, "Jack" ) )

      // WHEN I ask to get all customers
      val Some ( result ) = route ( app, FakeRequest ( GET, routes.CustomerController.getCustomers ().path () ) )

      val customers = customersReads.reads ( contentAsJson ( result ) ).get

      // THEN I expect a response with 200 status code, application/json Content-Type
      assert ( status ( result ).equals ( OK ) )
      assert ( contentType ( result ).contains ( "application/json" ) )

      // AND the body contains a list with only the customer I've created
      assert ( customers.equals ( Seq ( Customer ( 1, "Jack" ) ) ) )
    }
  }

  "CustomerController POST /api/v1/customers" should {

    "add a customer" in {

      val customer = Customer ( 0, "Jack" )

      val request = FakeRequest ( POST, routes.CustomerController.createCustomer ().path () ).withJsonBody ( customerWrites.writes ( customer ) )

      // WHEN I ask to create a customer
      val Some ( result ) = route ( app, request )

      val addedCustomer = customerReads.reads ( contentAsJson ( result ) ).get

      // THEN I expect a response with 200 status code, application/json Content-Type with
      assert ( status ( result ).equals ( OK ) )
      assert ( contentType ( result ).contains ( "application/json" ) )

      // AND the body contains the customer I added
      assert ( addedCustomer.equals ( Customer ( 1, "Jack" ) ) )
    }
  }

  "CustomerController PUT /api/v1/customers/1 after POST /api/v1/customers" should {

    "update the customer with id 1" in {

      // GIVEN I have no initial customers
      // AND I created one customer
      addCustomer ( Customer ( 0, "Mark" ) )

      val customer = Customer ( 1, "Luke" )

      // WHEN I ask to update customer with id 1
      val Some ( result ) = route ( app, FakeRequest ( PUT, routes.CustomerController.updateCustomer ( 1 ).path () ).withJsonBody ( customerWrites.writes ( customer ) ) )

      val updatedCustomer = customerReads.reads ( contentAsJson ( result ) ).get

      // THEN I expect a response with 200 status code, application/json Content-Type
      assert ( status ( result ).equals ( OK ) )
      assert ( contentType ( result ).contains ( "application/json" ) )

      // AND the body contains the customer I updated
      assert ( updatedCustomer.equals ( customer ) )
    }
  }

  "CustomerController PUT /api/v1/customers/1 having no customers" should {

    "return NotFound 404" in {

      // GIVEN I have no initial customers

      val customer = Customer ( 1, "Luke" )

      // WHEN I ask to update customer with id 1
      val Some ( result ) = route ( app, FakeRequest ( PUT, routes.CustomerController.updateCustomer ( 1 ).path () ).withJsonBody ( customerWrites.writes ( customer ) ) )

      val apiError = apiErrorReads.reads ( contentAsJson ( result ) ).get

      // THEN I expect a response with 404 status code, application/json Content-Type
      assert ( status ( result ).equals ( NOT_FOUND ) )
      assert ( contentType ( result ).contains ( "application/json" ) )

      // AND the body contains an ApiError with status 404 and the message contains the custoimerId 1
      assert ( apiError.status.equals ( NOT_FOUND ) )
      assert ( apiError.message.contains ( "customerId 1 not found" ) )
    }
  }

  "CustomerController DELETE /api/v1/customers/1 after POST /api/v1/customers" should {

    "delete the customer with id 1" in {

      // GIVEN I have no initial customers
      // AND I created one customer
      addCustomer ( Customer ( 0, "Mark" ) )

      // WHEN I ask to delete customer with id 1
      val Some ( result ) = route ( app, FakeRequest ( DELETE, routes.CustomerController.updateCustomer ( 1 ).path () ) )

      // THEN I expect a response with 200 status code, application/json Content-Type
      assert ( status ( result ).equals ( OK ) )
      assert ( contentType ( result ).contains ( "application/json" ) )

      // AND there are no customers
      assert ( getCustomers ().equals ( Seq () ) )
    }
  }

  private def addCustomer ( customer: Customer ): Unit = {

    val request = FakeRequest ( POST, routes.CustomerController.createCustomer ().path () ).withJsonBody ( customerWrites.writes ( customer ) )
    val Some ( res ) = route ( app, request )
    Await.result ( res, 5.seconds )
  }

  private def getCustomers (): Seq [Customer] = {

    val Some ( result ) = route ( app, FakeRequest ( GET, routes.CustomerController.getCustomers ().path () ) )

    customersReads.reads ( contentAsJson ( result ) ).get
  }
}