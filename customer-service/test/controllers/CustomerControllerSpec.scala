package controllers

import akka.actor.ActorSystem
import akka.util.Timeout
import model.Customer
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.libs.json.{ Json, Reads }
import play.api.mvc.Results
import play.api.test._
import play.api.test.Helpers._

import scala.concurrent.ExecutionContext.global._
import java.util.concurrent.Executors
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ Await, ExecutionContext }


class CustomerControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting with Results {

  val system: ActorSystem = ActorSystem ( "CustomerControllerSpec" )

  val customerReads = Json.reads [Customer]
  val customerWrites = Json.writes [Customer]
  val customersReads = Reads.seq ( customerReads )

  implicit val ec: ExecutionContext = ExecutionContext.fromExecutor ( Executors.newFixedThreadPool ( 10 ) )


  "CustomerController GET /api/v1/customers" should {

    "retrieve all the customers" in {

      val Some ( result ) = route ( app, FakeRequest ( GET, routes.CustomerController.getCustomers ().path () ) )

      val customers = customersReads.reads ( contentAsJson ( result ) ).get

      assert ( status ( result ).equals ( OK ) )
      assert ( contentType ( result ).contains ( "application/json" ) )
      assert ( customers.isEmpty )
    }
  }

  "CustomerController POST /api/v1/customers" should {

    "add a customer" in {

      val customer = Customer ( 0, "Jack" )

      val request = FakeRequest ( POST, routes.CustomerController.createCustomer ().path () ).withJsonBody ( customerWrites.writes ( customer ) )

      val Some ( result ) = route ( app, request )

      val addedCustomer = customerReads.reads ( contentAsJson ( result ) ).get

      assert ( status ( result ).equals ( OK ) )
      assert ( contentType ( result ).contains ( "application/json" ) )
      assert ( addedCustomer.equals ( Customer ( 1, "Jack" ) ) )
    }
  }

  "CustomerController GET /api/v1/customers after POST /api/v1/customers" should {

    "retrieve all the customers" in {

      // GIVEN I created one customer
      val customer = Customer ( 0, "Jack" )
      val request = FakeRequest ( POST, routes.CustomerController.createCustomer ().path () ).withJsonBody ( customerWrites.writes ( customer ) )
      val Some ( res ) = route ( app, request )
      Await.result ( res, 5.seconds )

      // WHEN I ask to get all customers
      val Some ( result ) = route ( app, FakeRequest ( GET, routes.CustomerController.getCustomers ().path () ) )

      val customers = customersReads.reads ( contentAsJson ( result ) ).get

      // THEN I get a response with 200 status code, application/json Content-Type with a single customer
      assert ( status ( result ).equals ( OK ) )
      assert ( contentType ( result ).contains ( "application/json" ) )
      assert ( customers.equals ( Seq ( Customer ( 1, "Jack" ) ) ) )
    }
  }
}