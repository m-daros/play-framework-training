package controllers

import akka.actor.ActorSystem
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.http.MediaRange.parse
import play.api.test._
import play.api.test.Helpers._

import scala.concurrent.ExecutionContext.global._
import java.util.concurrent.Executors
import scala.concurrent.{ ExecutionContext, Future }

/**
 */
class CustomerControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {

  val system: ActorSystem = ActorSystem ( "CustomerControllerSpec" )

  implicit val ec: ExecutionContext = ExecutionContext.fromExecutor ( Executors.newFixedThreadPool ( 10 ) )


  "CustomerController GET /api/v1/customers" should {

    "retrieve all the customers" in {

      val Some ( result ) = route ( app, FakeRequest ( GET, routes.CustomerController.getCustomers ().path () ) )

      status ( result ) mustBe OK
      contentType ( result ) must contain ( "application/json" )

      val customers = parse ( contentAsString ( result ) )

      customers mustBe empty

      println ( s"customers: $customers" )
    }
  }
}