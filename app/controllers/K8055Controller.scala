package controllers

import model.{DeviceCollection, Device}
import play.api.mvc._
import play.api.libs.json._
import scala.concurrent.Future

class K8055Controller extends Controller {

  def deviceCollection() = Action.async {
    implicit request => {
      val json = Json.toJson(DeviceCollection.getDeviceCollection)
      Future.successful(Ok(json))
    }
  }

  def getDevice(id:String) = Action.async {
    implicit request => {
      //Maybe find a device with the specified id
      val deviceCollection = DeviceCollection.getDeviceCollection
      val device:Option[Device] = deviceCollection.devices.find(device => device.id == id)

      //When a device is found, check its type, populate the transient data and return it.
      device.fold(Future.successful(BadRequest(Json.obj("result" -> "Can't find device")))) (
        d => d.deviceType match{
          case Device.ANALOGUE_IN => returnPopulatedDevice(d, Device.populateAnalogueIn)
          case Device.ANALOGUE_OUT => returnPopulatedDevice(d, Device.populateAnalogueOut)
          case Device.DIGITAL_IN => returnPopulatedDevice(d, Device.populateDigitalIn)
          case Device.DIGITAL_OUT => returnPopulatedDevice(d, Device.populateDigitalOut)
          case _ => Future.successful(BadRequest(Json.obj("result" -> "Can't read from device")))
        }
      )
    }
  }

  def returnPopulatedDevice(device: Device, fn: Device => Device):Future[Result] = {
    val json = Json.toJson(fn(device))
    Future.successful(Ok(json))
  }

  def addDevice() = Action.async(parse.json) {
    implicit request => request.body.validate[Device].fold(
      errors => {Future.successful(BadRequest(Json.obj("message" -> JsError.toJson(errors))))},
      device => {
        DeviceCollection.upsertDevice(device)
        Future.successful(Ok(Json.obj("message" -> ("Device '"+device.description+"' saved.") )))
      }
    )
  }

  def updateDevice() = addDevice()


  def deleteDevice(id:String) = Action.async {
    if (DeviceCollection.deleteDevice(id))
      Future.successful(Ok(Json.obj("message" -> s"Deleted device $id")))
    else
      Future.successful(BadRequest(Json.obj("message" -> s"Could not delete device $id")))
  }
}