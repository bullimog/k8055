package controllers


import model.{DeviceCache, Device}
import play.api.mvc._
import play.api.libs.json._

import scala.concurrent.Future

class Application extends Controller {


  def allDevices() = Action.async {
    implicit request => {
      val json = Json.toJson(DeviceCache.devices)
      Future.successful(Ok(json))
    }
  }

  def getDevice(id:String) = Action.async {
    implicit request => {
      val json = Json.toJson(DeviceCache.devices.filter(device => device.id == id))
      Future.successful(Ok(json))
    }
  }

  def addDevice1() = Action.async(parse.json) {
    implicit request => {
      (request.body \ "name").asOpt[String].map { name =>
        Future.successful(Ok("Hello " + name))
      }.getOrElse {
        Future.successful(BadRequest("Missing parameter [name]"))
      }

      //      Future.successful(Ok("Done"))
    }
  }

  def addDevice() = Action.async(parse.json) {
    implicit request => request.body.validate[Device].fold(
      errors => {
        Future.successful(BadRequest(Json.obj("status" ->"KO", "message" -> JsError.toJson(errors))))
      },
      device => {
        Future.successful(Ok(Json.obj("status" ->"OK", "message" -> ("Place '"+device.description+"' saved.") )))
      }
    )

  }
}