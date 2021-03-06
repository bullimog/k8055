package connectors

import java.io.{File, PrintWriter, FileNotFoundException}
import model.DeviceCollection
import play.Logger
import play.api.libs.json.{Json, JsError, JsSuccess, JsValue}
import scala.io.Source

object DeviceConfigIO extends DeviceConfigIO
trait DeviceConfigIO {

  def parseDeviceCollection(json: JsValue):Option[DeviceCollection] = {
    json.validate[DeviceCollection] match {
      case s: JsSuccess[DeviceCollection] => Some(s.get)
      case e: JsError => None
    }
  }

  def readDeviceCollectionFromFile(fileName:String):Option[DeviceCollection] = {
    try{
      val source = Source.fromFile(fileName, "UTF-8")
      val json: JsValue = Json.parse(source.mkString)
      source.close()
      parseDeviceCollection(json)
    }catch{
      case e:FileNotFoundException => None
    }
  }

  def writeDeviceCollectionToFile(fileName: String, deviceCollection: DeviceCollection):Boolean = {
    try{
      val writer = new PrintWriter(new File(fileName))
      writer.write(Json.prettyPrint(Json.toJson(deviceCollection)))
      writer.close()
      true
    }
    catch{
      case e: Exception => Logger.error("Could not write to file: "+e); false
    }
  }

}
