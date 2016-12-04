package model

import play.api.libs.json.{Json, JsPath, Reads}
import play.api.libs.functional.syntax._

//Represents the transient state of a Device, passed to and from a client service
case class DeviceState(id:String, digitalState:Option[Boolean]=None, analogueState:Option[Int]=None,
                       strobeOnTime:Option[Int]=None, strobeOffTime:Option[Int]=None)

object DeviceState {
  implicit val deviceStateReads: Reads[DeviceState] = (
  (JsPath \ "id").read[String] and
  (JsPath \ "digitalState").readNullable[Boolean] and
  (JsPath \ "analogueState").readNullable[Int] and
  (JsPath \ "strobeOnTime").readNullable[Int] and
  (JsPath \ "strobeOffTime").readNullable[Int]
    )(DeviceState.apply _)

  implicit val deviceWrites = Json.writes[DeviceState]
}