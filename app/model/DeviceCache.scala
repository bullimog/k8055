package model

import play.api.libs.json.{Json, JsPath, Reads}
import play.api.libs.functional.syntax._
import scala.collection.mutable

object DeviceCache extends DeviceCache

case class DeviceCollection(name: String, description: String, devices: List[Device])
object DeviceCollection{
  //  implicit val componentCollectionFmt = Json.format[ComponentCollection]

  implicit val deviceCollectionReads: Reads[DeviceCollection] = (
    (JsPath \ "name").read[String] and
      (JsPath \ "description").read[String] and
      (JsPath \ "devices").read[List[Device]]
    )(DeviceCollection.apply _)

  /* Since this case class references Device and Monitor, the Json.writes has to be defined last! */
  implicit val deviceCollectionWrites = Json.writes[DeviceCollection]
}


trait DeviceCache {
  var emptyDevices: mutable.MutableList[Device] = mutable.MutableList()

  import Device._
  var devices: mutable.MutableList[Device] = emptyDevices
//  {
//    mutable.MutableList(
//      Device("1", "pump", DIGITAL_OUT, 1, None, None, None, None, None, None, None),
//      Device("2", "heater", ANALOGUE_OUT, 1, Some("%"), None, None, None, None, None, None)
//    )
//  }

  def addDevice(device:Device) = {
    devices = devices += device
  }
}
