package model

import scala.collection.mutable
import play.api.libs.functional.syntax._
import play.api.libs.json.{Json, JsPath, Reads}


case class Device(id: String, description: String, deviceType: Int, port:Int, units:Option[String],
                  conversionFactor:Option[Double], conversionOffset:Option[Double], decimalPlaces:Option[Int],
                  monitorSensor:Option[String], monitorIncreaser:Option[String], monitorDecreaser:Option[String])

object Device {
  val TIMER = 0         // e.g. Clock
  val ANALOGUE_IN = 1   // e.g. Thermometer
  val ANALOGUE_OUT = 2  // e.g. Heater or Cooler
  val DIGITAL_IN = 3    // e.g. Button or Switch
  val DIGITAL_OUT = 4   // e.g. Pump
  val MONITOR = 5       // e.g. Thermostat

  implicit val deviceReads: Reads[Device] = (
    (JsPath \ "id").read[String] and
      (JsPath \ "description").read[String] and
      (JsPath \ "deviceType").read[Int] and
      (JsPath \ "port").read[Int] and
      (JsPath \ "units").readNullable[String] and
      (JsPath \ "conversionFactor").readNullable[Double] and
      (JsPath \ "conversionOffset").readNullable[Double] and
      (JsPath \ "decimalPlaces").readNullable[Int] and
      (JsPath \ "monitorSensor").readNullable[String] and
      (JsPath \ "monitorIncreaser").readNullable[String] and
      (JsPath \ "monitorDecreaser").readNullable[String]
    )(Device.apply _)

  implicit val deviceWrites = Json.writes[Device]
}

object DeviceCache {
  var emptyDevices: mutable.MutableList[Device] = mutable.MutableList()

  import Device._
  var devices: mutable.MutableList[Device] = {
    mutable.MutableList(
      Device("1", "pump", DIGITAL_OUT, 1, None, None, None, None, None, None, None),
      Device("2", "heater", ANALOGUE_OUT, 1, Some("%"), None, None, None, None, None, None)
    )
  }

  def addDevice(device:Device) = {
    devices = devices += device
  }



}



//case class DeviceCollection(name: String, devices: List[Device]){}
