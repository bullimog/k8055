package model

import connectors.ConfigIO
import play.api.libs.json.{Json, JsPath, Reads}
import play.api.libs.functional.syntax._
import scala.collection.mutable
import scala.concurrent.Future

//object DeviceCache extends DeviceCache

case class DeviceCollection(name: String, description: String, devices: List[Device])

object DeviceCollection{
  implicit val deviceCollectionReads: Reads[DeviceCollection] = (
    (JsPath \ "name").read[String] and
      (JsPath \ "description").read[String] and
      (JsPath \ "devices").read[List[Device]]
    )(DeviceCollection.apply _)

  implicit val deviceCollectionWrites = Json.writes[DeviceCollection]

  def getDeviceCollection() = {
    val oDeviceCollection: Option[DeviceCollection] = ConfigIO.readDeviceCollectionFromFile("devices.json")
    oDeviceCollection.fold(DeviceCollection("None", "Empty", List()))({
      deviceCollection => deviceCollection
    })
  }

  def addDevice(device: Device) = {}
}


//trait DeviceCache {
//  var emptyDevices: mutable.MutableList[Device] = mutable.MutableList()
//
//  import Device._
//  var devices: mutable.MutableList[Device] = emptyDevices
////  {
////    mutable.MutableList(
////      Device("1", "pump", DIGITAL_OUT, 1, None, None, None, None, None, None, None),
////      Device("2", "heater", ANALOGUE_OUT, 1, Some("%"), None, None, None, None, None, None)
////    )
////  }
//
//  def addDevice(device:Device) = {
//    devices = devices += device
//  }
//}
