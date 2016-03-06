package controllers

import manager.{DeviceManager, DeviceCollectionManager}
import model.{DeviceState, Device}
import model.Device._
import play.api.mvc._
import play.api.libs.json._
import scala.concurrent.Future
import DeviceManager._

class K8055Controller extends Controller {

  def deviceCollection() = Action.async {
    implicit request => {
      val json = Json.toJson(DeviceCollectionManager.readAndPopulateDevices(DeviceCollectionManager.getDeviceCollection))
      Future.successful(Ok(json))
    }
  }

  def getDevice(id:String) = Action.async {
    implicit request => {
      //Maybe find a device with the specified id
      val deviceCollection = DeviceCollectionManager.getDeviceCollection
      val device:Option[Device] = deviceCollection.devices.find(device => device.id == id)

      //When a device is found, check its type, populate the transient data and return it.
      device.fold(Future.successful(NotFound(Json.obj("result" -> "Can't find device")))) (
        d => d.deviceType match{
          case TIMER => populatedDeviceAsJson(d, readTimer)
          case ANALOGUE_IN => populatedDeviceAsJson(d, readAndPopulateAnalogueIn)
          case ANALOGUE_OUT => populatedDeviceAsJson(d, readAndPopulateAnalogueOut)
          case DIGITAL_IN => {populatedDeviceAsJson(d, readAndPopulateDigitalIn)}
          case DIGITAL_OUT => populatedDeviceAsJson(d, readAndPopulateDigitalOut)
          case MONITOR => populatedDeviceAsJson(d, readAndPopulateMonitor)
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
        if (DeviceCollectionManager.upsertDevice(device)) {
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
        if (DeviceCollectionManager.patchDevice(deviceState, isDelta)) {
          Future.successful(Ok(Json.obj("message" -> ("Device '"+deviceState.id+"' patched.") )))
        }
        else Future.successful(BadRequest(Json.obj("message" -> s"Could not delta patch device $deviceState.id")))
      }
    )
  }


  def deleteDevice(id:String) = Action.async {
    if (DeviceCollectionManager.deleteDevice(id))
      Future.successful(Ok(Json.obj("message" -> s"Deleted device $id")))
    else
      Future.successful(BadRequest(Json.obj("message" -> s"Could not delete device $id")))
  }
}