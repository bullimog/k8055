package controllers

import model.{RawDevice, RawDeviceCollection}
import model.RawDevice._
import play.api.mvc._
import play.api.libs.json._
import scala.concurrent.Future

class K8055Controller extends Controller {

  def deviceCollection() = Action.async {
    implicit request => {
      val json = Json.toJson(RawDeviceCollection.getDeviceCollection)
      Future.successful(Ok(json))
    }
  }

  def getDevice(id:String) = Action.async {
    implicit request => {
      //Maybe find a device with the specified id
      val deviceCollection = RawDeviceCollection.getDeviceCollection
      val device:Option[RawDevice] = deviceCollection.devices.find(device => device.id == id)

      //When a device is found, check its type, populate the transient data and return it.
      device.fold(Future.successful(BadRequest(Json.obj("result" -> "Can't find device")))) (
        d => d.deviceType match{
          case ANALOGUE_IN => returnPopulatedDevice(d, populateAnalogueIn)
          case ANALOGUE_OUT => returnPopulatedDevice(d, populateAnalogueOut)
          case DIGITAL_IN => returnPopulatedDevice(d, populateDigitalIn)
          case DIGITAL_OUT => returnPopulatedDevice(d, populateDigitalOut)
          case MONITOR => returnPopulatedDevice(d, populateMonitor)
          case _ => Future.successful(BadRequest(Json.obj("result" -> "Can't read from device")))
        }
      )
    }
  }

  def returnPopulatedDevice(device: RawDevice, populateFn: RawDevice => RawDevice):Future[Result] = {
    val json = Json.toJson(populateFn(device))
    Future.successful(Ok(json))
  }

  def addDevice() = Action.async(parse.json) {
    implicit request => request.body.validate[RawDevice].fold(
      errors => {Future.successful(BadRequest(Json.obj("message" -> JsError.toJson(errors))))},
      device => {
        if (RawDeviceCollection.upsertDevice(device)) {
          Future.successful(Ok(Json.obj("message" -> ("Device '"+device.description+"' saved.") )))
        }
        else Future.successful(BadRequest(Json.obj("message" -> s"Could not add device $device")))
      }
    )
  }

  def updateDevice() = addDevice()


  def deleteDevice(id:String) = Action.async {
    if (RawDeviceCollection.deleteDevice(id))
      Future.successful(Ok(Json.obj("message" -> s"Deleted device $id")))
    else
      Future.successful(BadRequest(Json.obj("message" -> s"Could not delete device $id")))
  }
}