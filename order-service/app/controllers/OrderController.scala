package controllers

import akka.actor.ActorSystem
import play.api.mvc.{ Action, AnyContent, ControllerComponents }

import javax.inject.{ Inject, Singleton }
import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class OrderController @Inject () ( val controllerComponents: ControllerComponents, val system: ActorSystem ) ( implicit ec: ExecutionContext ) extends MyBaseController {

  def createOrder (): Action [AnyContent] = Action.async { implicit request =>

    // TODO ...
    Future { Ok }
  }
}
