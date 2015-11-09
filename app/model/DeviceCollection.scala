package model

import connectors.{Configuration, DeviceConfigIO}
import play.api.Play
import play.api.libs.json.{Json, JsPath, Reads}
import play.api.libs.functional.syntax._


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
    oDeviceCollection.fold(DeviceCollection("NoneRead", "Empty", List()))({
      deviceCollection => deviceCollection
    })
  }

  def upsertDevice(device: Device):Boolean = {
    val deviceCollection = getDeviceCollection
    val devices:List[Device] = deviceCollection.devices
    val deviceRemoved = devices.filter(d => d.id != device.id)
    val deviceAdded = deviceRemoved ::: List(device)
    val dc = deviceCollection.copy(devices = deviceAdded)
    putDeviceCollection(dc)
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
