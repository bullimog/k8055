package model

import connectors.ConfigIO
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
    val oDeviceCollection: Option[DeviceCollection] = ConfigIO.readDeviceCollectionFromFile("devices.json")
    oDeviceCollection.fold(DeviceCollection("NoneRead", "Empty", List()))({
      deviceCollection => deviceCollection
    })
  }

  def addDevice(device: Device) = {
    val deviceCollection = getDeviceCollection
    val devices:List[Device] = deviceCollection.devices
    val deviceRemoved = devices.filter(d => d.id != device.id)
    val deviceAdded = deviceRemoved ::: List(device)
    val dc = deviceCollection.copy(devices = deviceAdded)
    putDeviceCollection(dc)
  }

  def deleteDevice(device: Device):Unit = {deleteDevice(device.id)}
  def deleteDevice(device: String):Unit = {
    val deviceCollection = getDeviceCollection
    val devices:List[Device] = deviceCollection.devices
    val deviceRemoved = devices.filter(d => d.id != device)
    val dc = deviceCollection.copy(devices = deviceRemoved)
    putDeviceCollection(dc)
  }

  def putDeviceCollection(deviceCollection: DeviceCollection):Unit = {
    ConfigIO.writeDeviceCollectionToFile("devices.json", deviceCollection)
  }

}
