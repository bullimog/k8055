package model

import play.api.libs.json.{Json, JsPath, Reads}
import play.api.libs.functional.syntax._

case class DeviceState(id:String, digitalState:Option[Boolean]=None, analogueState:Option[Int]=None,
                       analogueState2:Option[Int]=None)

object DeviceState {
  implicit val deviceStateReads: Reads[DeviceState] = (
  (JsPath \ "id").read[String] and
  (JsPath \ "digitalState").readNullable[Boolean] and
  (JsPath \ "analogueState").readNullable[Int] and
  (JsPath \ "analogueState2").readNullable[Int]
    )(DeviceState.apply _)

  implicit val deviceWrites = Json.writes[DeviceState]
}