package controllers

import model.ApiError
import play.api.libs.json.{ JsValue, Json, OWrites }
import play.api.mvc.BaseController

trait MyBaseController extends BaseController {

  protected implicit val apiErrorWrites: OWrites [ApiError] = Json.writes [ApiError]

  protected def jsonApiError ( status: Int, message: String ): JsValue = {

    Json.toJson ( ApiError ( status, message ) )
  }
}