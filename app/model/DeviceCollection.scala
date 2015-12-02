package model

import connector.K8055Board
import connectors.{Configuration, DeviceConfigIO}
import model.Device._
import play.api.Play
import play.api.libs.json.{Json, JsPath, Reads}
import play.api.libs.functional.syntax._

import scala.concurrent.Future


case class DeviceCollection(name: String, description: String, devices: List[Device])

object DeviceCollection{
  implicit val deviceCollectionReads: Reads[DeviceCollection] = (
      (JsPath \ "name").read[String] and
      (JsPath \ "description").read[String] and
      (JsPath \ "devices").read[List[Device]]
    )(DeviceCollection.apply _)

  implicit val deviceCollectionWrites = Json.writes[DeviceCollection]

  def getDeviceCollection:DeviceCollection = {
    val oDeviceCollection: Option[DeviceCollection] = DeviceConfigIO.readDeviceCollectionFromFile(Configuration.filename)
    oDeviceCollection.fold(DeviceCollection("No Devices", "Error", List()))({
      deviceCollection => deviceCollection
    })
  }

  def populateDevices(deviceCollection: DeviceCollection):DeviceCollection = {
    val populatedDevices = deviceCollection.devices.map(device =>
      device.deviceType match {
        case ANALOGUE_IN => Device.populateAnalogueIn(device)
        case ANALOGUE_OUT => Device.populateAnalogueOut(device)
        case DIGITAL_IN => Device.populateDigitalIn(device)
        case DIGITAL_OUT => Device.populateDigitalOut(device)
        case MONITOR => Device.populateMonitor(device)
        case _ => device
      }
    )
    deviceCollection.copy(devices = populatedDevices)
  }

  def upsertDevice(device: Device):Boolean = {
    val deviceCollection = getDeviceCollection
    val devices:List[Device] = deviceCollection.devices
    val deviceRemoved = devices.filter(d => d.id != device.id)
    val deviceAdded = deviceRemoved ::: List(device)
    val dc = deviceCollection.copy(devices = deviceAdded)
    
    updateTransientOutData(device)
    putDeviceCollection(dc)
  }

  def updateTransientOutData(device: Device) = {
    if(device.deviceType == Device.ANALOGUE_OUT)
      K8055Board.setAnalogueOut(device.channel, device.analogueState.getOrElse(0))

    if (device.deviceType == Device.DIGITAL_OUT)
      K8055Board.setDigitalOut(device.channel, device.digitalState.getOrElse(false))
  }

  def patchDevice(deviceState: DeviceState, delta:Boolean):Boolean = {
    val deviceCollection = getDeviceCollection
    val devices:List[Device] = deviceCollection.devices
    devices.find(d => d.id == deviceState.id).exists(device =>
      device.deviceType match {
        case Device.ANALOGUE_OUT =>
          val aState = if(delta) Some(device.analogueState.getOrElse(0) + deviceState.analogueState.getOrElse(0))
                       else deviceState.analogueState
          updateTransientOutData(device.copy(analogueState = aState))
          true
        case Device.DIGITAL_OUT =>
          updateTransientOutData(device.copy(digitalState = deviceState.digitalState))
          true
        case _ => false
      }
    )
  }

//  def addDevice(device: Device):Boolean = {
//    val deviceCollection = getDeviceCollection
//    val devices:List[Device] = deviceCollection.devices
//    if(devices.exists(d => d.id == device.id)) false
//    else {
//      val deviceAdded = devices ::: List(device)
//      val dc = deviceCollection.copy(devices = deviceAdded)
//      putDeviceCollection(dc)
//    }
//  }

  def deleteDevice(device: Device):Boolean = {deleteDevice(device.id)}
  def deleteDevice(device: String):Boolean = {
    val deviceCollection = getDeviceCollection
    val devices:List[Device] = deviceCollection.devices
    val deviceRemoved = devices.filter(d => d.id != device)
    val dc = deviceCollection.copy(devices = deviceRemoved)
    putDeviceCollection(dc)
  }

  def putDeviceCollection(deviceCollection: DeviceCollection):Boolean = {
    DeviceConfigIO.writeDeviceCollectionToFile(Configuration.filename, deviceCollection)
  }

}
