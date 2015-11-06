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

//  def getDevice(id:String) = Action.async {
//    implicit request => {
//      val json = Json.toJson(DeviceCache.devices.filter(device => device.id == id))
//      Future.successful(Ok(json))
//    }
//  }

  def getDevice(id:String) = Action.async(parse.json) {
    implicit request => {
      //Maybe find a device with the specified id
      val device:Option[Device] = DeviceCache.devices.find(device => device.id == id)

      //When a device is found, check its type, populate the transient data and return it.
      device.fold(Future.successful(BadRequest(Json.obj("result" -> "Can't find device")))) (
        d => d.deviceType match{
          case Device.ANALOGUE_IN => readAnalogueIn(d)
          case _ => Future.successful(BadRequest(Json.obj("result" -> "Can't read from device")))
        }
      )
    }
  }

  private def readAnalogueIn(device: Device) = {
    val json = Json.toJson(device.copy(analogueState = Some(K8055Board.getAnalogueIn(device.port))))
    Future.successful(Ok(json))
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