package controllers

import model.{Device, DeviceCollection}
import model.Device._
import play.api.mvc._
import play.api.libs.json._
import scala.concurrent.Future

//object K8055Controller extends K8055Controller

class K8055Controller extends Controller {

  def deviceCollection() = Action.async {
    implicit request => {
      val json = Json.toJson(DeviceCollection.populateDevices(DeviceCollection.getDeviceCollection))
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

  def returnPopulatedDevice(device: Device, populateFn: Device => Device):Future[Result] = {
    val json = Json.toJson(populateFn(device))
    Future.successful(Ok(json))
  }

  def addDevice() = Action.async(parse.json) {
    implicit request => request.body.validate[Device].fold(
      errors => {Future.successful(BadRequest(Json.obj("message" -> JsError.toJson(errors))))},
      device => {
        if (DeviceCollection.upsertDevice(device)) {
          Future.successful(Ok(Json.obj("message" -> ("Device '"+device.description+"' saved.") )))
        }
        else Future.successful(BadRequest(Json.obj("message" -> s"Could not add device $device")))
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