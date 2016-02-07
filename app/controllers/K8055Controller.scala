package controllers

import model.{DeviceState, Device}
import model.Device._
import play.api.mvc._
import play.api.libs.json._
import scala.concurrent.Future
import controllers.DeviceController._

class K8055Controller extends Controller {

  def deviceCollection() = Action.async {
    implicit request => {
      val json = Json.toJson(DeviceCollectionController.populateDevices(DeviceCollectionController.getDeviceCollection))
      Future.successful(Ok(json))
    }
  }

  def getDevice(id:String) = Action.async {
    implicit request => {
      //Maybe find a device with the specified id
      val deviceCollection = DeviceCollectionController.getDeviceCollection
      val device:Option[Device] = deviceCollection.devices.find(device => device.id == id)

      //When a device is found, check its type, populate the transient data and return it.
      device.fold(Future.successful(BadRequest(Json.obj("result" -> "Can't find device")))) (
        d => d.deviceType match{
          case ANALOGUE_IN => populatedDeviceAsJson(d, populateAnalogueIn)
          case ANALOGUE_OUT => populatedDeviceAsJson(d, populateAnalogueOut)
          case DIGITAL_IN => {populatedDeviceAsJson(d, populateDigitalIn)}
          case DIGITAL_OUT => populatedDeviceAsJson(d, populateDigitalOut)
          case MONITOR => populatedDeviceAsJson(d, populateMonitor)
          case _ => Future.successful(BadRequest(Json.obj("result" -> "Can't read from device")))
        }
      )
    }
  }

  def populatedDeviceAsJson(device: Device, populateFn: Device => Device):Future[Result] = {
    val json = Json.toJson(populateFn(device))
    Future.successful(Ok(json))
  }

  def addDevice() = Action.async(parse.json) {
    implicit request => request.body.validate[Device].fold(
      errors => {Future.successful(BadRequest(Json.obj("message" -> JsError.toJson(errors))))},
      device => {
        if (DeviceCollectionController.upsertDevice(device)) {
          Future.successful(Ok(Json.obj("message" -> ("Device '"+device.description+"' saved.") )))
        }
        else Future.successful(BadRequest(Json.obj("message" -> s"Could not add device $device")))
      }
    )
  }

  def updateDevice() = addDevice() //TODO Does this need a different implementation?


  def patchDevice():Action[JsValue] =patchTheDevice(false)
  def patchDeviceDelta():Action[JsValue] =patchTheDevice(true)
  def patchTheDevice(isDelta:Boolean):Action[JsValue] = Action.async(parse.json) {
    implicit request => request.body.validate[DeviceState].fold(
      errors => {Future.successful(BadRequest(Json.obj("message" -> JsError.toJson(errors))))},
      deviceState => {
        if (DeviceCollectionController.patchDevice(deviceState, isDelta)) {
          Future.successful(Ok(Json.obj("message" -> ("Device '"+deviceState.id+"' patched.") )))
        }
        else Future.successful(BadRequest(Json.obj("message" -> s"Could not delta patch device $deviceState.id")))
      }
    )
  }


  def deleteDevice(id:String) = Action.async {
    if (DeviceCollectionController.deleteDevice(id))
      Future.successful(Ok(Json.obj("message" -> s"Deleted device $id")))
    else
      Future.successful(BadRequest(Json.obj("message" -> s"Could not delete device $id")))
  }
}