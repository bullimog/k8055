package model

import connector.K8055Board
import monitor.MonitorManager

import scala.collection.mutable
import play.api.libs.functional.syntax._
import play.api.libs.json.{Json, JsPath, Reads}


case class Device(id: String, description: String, deviceType: Int, channel:Int, units:Option[String] = None,
                  conversionFactor:Option[Double] = None, conversionOffset:Option[Double] = None,
                  decimalPlaces:Option[Int] = None, monitorSensor:Option[String] = None,
                  monitorIncreaser:Option[String] = None, monitorDecreaser:Option[String] = None,
                  digitalState:Option[Boolean] = None, analogueState:Option[Int] = None)

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
      (JsPath \ "channel").read[Int] and
      (JsPath \ "units").readNullable[String] and
      (JsPath \ "conversionFactor").readNullable[Double] and
      (JsPath \ "conversionOffset").readNullable[Double] and
      (JsPath \ "decimalPlaces").readNullable[Int] and
      (JsPath \ "monitorSensor").readNullable[String] and
      (JsPath \ "monitorIncreaser").readNullable[String] and
      (JsPath \ "monitorDecreaser").readNullable[String] and
      (JsPath \ "digitalState").readNullable[Boolean] and
      (JsPath \ "analogueState").readNullable[Int]
    )(Device.apply _)

  implicit val deviceWrites = Json.writes[Device]

  def populateAnalogueIn (device: Device):Device = {
    device.copy(analogueState = Some(K8055Board.getAnalogueIn(device.channel)))
  }

  def populateAnalogueOut(device: Device):Device = {
    device.copy(analogueState = Some(K8055Board.getAnalogueOut(device.channel)))
  }

  def populateDigitalIn(device: Device):Device = {
    device.copy(digitalState = Some(K8055Board.getDigitalIn(device.channel)))
  }

  def populateDigitalOut(device: Device):Device = {
    device.copy(digitalState = Some(K8055Board.getDigitalOut(device.channel)))
  }

  def populateMonitor(device: Device):Device = {
    device.copy(digitalState = Some(MonitorManager.getDigitalOut(device.id)),
               analogueState = Some(MonitorManager.getAnalogueOut(device.id)))
  }
}
