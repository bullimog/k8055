package model

import play.api.libs.functional.syntax._
import play.api.libs.json.{Json, JsPath, Reads}


case class Device(id: String, description: String, deviceType: Int, channel:Int, icon: Option[String],
                  units:Option[String] = None, conversionFactor:Option[Double] = None,
                  conversionOffset:Option[Double] = None, decimalPlaces:Option[Int] = None,
                  monitorSensor:Option[String] = None, monitorIncreaser:Option[String] = None,
                  monitorDecreaser:Option[String] = None, digitalState:Option[Boolean] = None,
                  flipDigitalState:Option[Boolean] = Some(false), analogueState:Option[Int] = None,
                  strobeOnTime:Option[Int] = None, strobeOffTime:Option[Int] = None)

object Device {
//  val TIMER = 0       // e.g. Clock
  val ANALOGUE_IN = 1   // e.g. Thermometer
  val ANALOGUE_OUT = 2  // e.g. Heater or Cooler
  val DIGITAL_IN = 3    // e.g. Button or Switch
  val DIGITAL_OUT = 4   // e.g. Pump
  val MONITOR = 5       // e.g. Thermostat or overflow guard
  val STROBE = 6        // e.g. Pump pulser

  implicit val deviceReads: Reads[Device] = (
    (JsPath \ "id").read[String] and
      (JsPath \ "description").read[String] and
      (JsPath \ "deviceType").read[Int] and
      (JsPath \ "channel").read[Int] and
      (JsPath \ "icon").readNullable[String] and
      (JsPath \ "units").readNullable[String] and
      (JsPath \ "conversionFactor").readNullable[Double] and
      (JsPath \ "conversionOffset").readNullable[Double] and
      (JsPath \ "decimalPlaces").readNullable[Int] and
      (JsPath \ "monitorSensor").readNullable[String] and
      (JsPath \ "monitorIncreaser").readNullable[String] and
      (JsPath \ "monitorDecreaser").readNullable[String] and
      (JsPath \ "digitalState").readNullable[Boolean] and
      (JsPath \ "flipDigitalState").readNullable[Boolean] and
      (JsPath \ "analogueState").readNullable[Int] and
      (JsPath \ "strobeOnTime").readNullable[Int] and
      (JsPath \ "strobeOffTime").readNullable[Int]
    )(Device.apply _)

  implicit val deviceWrites = Json.writes[Device]

}
