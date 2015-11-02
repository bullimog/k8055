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

  def addDevice() = Action.async(parse.json) {
    implicit request => request.body.validate[Device].fold(
      errors => {Future.successful(BadRequest(Json.obj("message" -> JsError.toJson(errors))))},
      device => {
        DeviceCache.addDevice(device)
        Future.successful(Ok(Json.obj("message" -> ("Device '"+device.description+"' saved.") )))
      }
    )
  }
}