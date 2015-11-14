package model

import connectors.{Configuration, DeviceConfigIO}
import play.api.Play
import play.api.libs.json.{Json, JsPath, Reads}
import play.api.libs.functional.syntax._


case class RawDeviceCollection(name: String, description: String, devices: List[RawDevice])

object RawDeviceCollection{
  implicit val deviceCollectionReads: Reads[RawDeviceCollection] = (
      (JsPath \ "name").read[String] and
      (JsPath \ "description").read[String] and
      (JsPath \ "devices").read[List[RawDevice]]
    )(RawDeviceCollection.apply _)

  implicit val deviceCollectionWrites = Json.writes[RawDeviceCollection]

  def getDeviceCollection:RawDeviceCollection = {
    val oDeviceCollection: Option[RawDeviceCollection] = DeviceConfigIO.readDeviceCollectionFromFile(Configuration.filename)
    oDeviceCollection.fold(RawDeviceCollection("NoneRead", "Empty", List()))({
      deviceCollection => deviceCollection
    })
  }

  def upsertDevice(device: RawDevice):Boolean = {
    val deviceCollection = getDeviceCollection
    val devices:List[RawDevice] = deviceCollection.devices
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

  def deleteDevice(device: RawDevice):Boolean = {deleteDevice(device.id)}
  def deleteDevice(device: String):Boolean = {
    val deviceCollection = getDeviceCollection
    val devices:List[RawDevice] = deviceCollection.devices
    val deviceRemoved = devices.filter(d => d.id != device)
    val dc = deviceCollection.copy(devices = deviceRemoved)
    putDeviceCollection(dc)
  }

  def putDeviceCollection(deviceCollection: RawDeviceCollection):Boolean = {
    DeviceConfigIO.writeDeviceCollectionToFile(Configuration.filename, deviceCollection)
  }

}
