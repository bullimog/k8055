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

  def addDevice(id:String, description: String, deviceType: Int, port: Int) = Action.async {
    implicit request => {
      DeviceCache.addDevice(id, description, deviceType, port)
      Future.successful(Ok())
    }
  }


}
