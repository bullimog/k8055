package controllers


import connector.K8055Board
import model.{DeviceCache, Device}
import play.api.mvc._
import play.api.libs.json._

import scala.collection.mutable
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

  def readDevice(id:String) = Action.async(parse.json) {
    implicit request => {
      val device:Option[Device] = DeviceCache.devices.find(device => device.id == id)

      device.fold(Future.successful(BadRequest(Json.obj("result" -> "Can't find device")))) (
        d => d.deviceType match{
          case Device.ANALOGUE_IN => Future.successful(Ok(Json.obj("result" -> K8055Board.getAnalogueIn(d.port))))
          case _ => Future.successful(BadRequest(Json.obj("result" -> "Can't read from device")))
        }
      )
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

  def updateDevice() = Action.async(parse.json) {
    implicit request => request.body.validate[Device].fold(
      errors => {Future.successful(BadRequest(Json.obj("message" -> JsError.toJson(errors))))},
      device => {
        DeviceCache.addDevice(device)
        Future.successful(Ok(Json.obj("message" -> ("Device '"+device.description+"' saved.") )))
      }
    )
  }
}