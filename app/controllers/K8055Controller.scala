package controllers


import connector.K8055Board
import connectors.ConfigIO
import model.{DeviceCollection, Device}
import play.api.mvc._
import play.api.libs.json._

import scala.collection.mutable
import scala.concurrent.Future

class K8055Controller extends Controller {


  def deviceCollection() = Action.async {
    implicit request => {
      val json = Json.toJson(DeviceCollection.getDeviceCollection())
      Future.successful(Ok(json))

//      val oDeviceCollection:Option[DeviceCollection] = ConfigIO.readDeviceCollectionFromFile("devices.json")
//      oDeviceCollection.fold(Future.successful(Ok("None Found!!"))) ({
//        deviceCollection => val json = Json.toJson(deviceCollection.devices)
//          Future.successful(Ok(json))
//      })
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
      val deviceCollection = DeviceCollection.getDeviceCollection()
      val device:Option[Device] = deviceCollection.devices.find(device => device.id == id)

      //When a device is found, check its type, populate the transient data and return it.
      device.fold(Future.successful(BadRequest(Json.obj("result" -> "Can't find device")))) (
        d => d.deviceType match{
          case Device.ANALOGUE_IN => returnPopulatedDevice(d, populateAnalogueIn)
          case Device.ANALOGUE_OUT => returnPopulatedDevice(d, populateAnalogueOut)
          case Device.DIGITAL_IN => returnPopulatedDevice(d, populateDigitalIn)
          case Device.DIGITAL_OUT => returnPopulatedDevice(d, populateDigitalOut)
          case _ => Future.successful(BadRequest(Json.obj("result" -> "Can't read from device")))
        }
      )
    }
  }

  def returnPopulatedDevice(device: Device, fn: Device => Device):Future[Result] = {
    val json = Json.toJson(fn(device))
    Future.successful(Ok(json))
  }

  //Populate the case class with AnalogueIn data
  def populateAnalogueIn (device: Device):Device = {
    device.copy(analogueState = Some(K8055Board.getAnalogueIn(device.channel)))
  }

  //Populate the case class with AnalogueOut data
  def populateAnalogueOut(device: Device):Device = {
    device.copy(analogueState = Some(K8055Board.getAnalogueOut(device.channel)))
  }

  //Populate the case class with DigitalOut data
  def populateDigitalIn(device: Device):Device = {
    device.copy(digitalState = Some(K8055Board.getDigitalIn(device.channel)))
  }

  //Populate the case class with DigitalOut data
  def populateDigitalOut(device: Device):Device = {
    device.copy(digitalState = Some(K8055Board.getDigitalOut(device.channel)))
  }



  def addDevice() = Action.async(parse.json) {
    implicit request => request.body.validate[Device].fold(
      errors => {Future.successful(BadRequest(Json.obj("message" -> JsError.toJson(errors))))},
      device => {
        DeviceCollection.addDevice(device)
        Future.successful(Ok(Json.obj("message" -> ("Device '"+device.description+"' saved.") )))
      }
    )
  }

  def updateDevice() = Action.async(parse.json) {
    implicit request => request.body.validate[Device].fold(
      errors => {Future.successful(BadRequest(Json.obj("message" -> JsError.toJson(errors))))},
      device => {
        DeviceCollection.addDevice(device)
        Future.successful(Ok(Json.obj("message" -> ("Device '"+device.description+"' saved.") )))
      }
    )
  }
}