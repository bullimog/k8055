package connectors

import java.io.{File, PrintWriter, FileNotFoundException}
import model.RawDeviceCollection
import play.Logger
import play.api.libs.json.{Json, JsError, JsSuccess, JsValue}
import scala.io.Source


object DeviceConfigIO {

  def parseDeviceCollection(json: JsValue):Option[RawDeviceCollection] = {
    json.validate[RawDeviceCollection] match {
      case s: JsSuccess[RawDeviceCollection] => Some(s.get)
      case e: JsError => None
    }
  }

  def readDeviceCollectionFromFile(fileName:String):Option[RawDeviceCollection] = {
    try{
      val source = Source.fromFile(fileName, "UTF-8")
      val json: JsValue = Json.parse(source.mkString)
      parseDeviceCollection(json)
    }catch{
      case e:FileNotFoundException => None
    }
  }

  def writeDeviceCollectionToFile(fileName: String, deviceCollection: RawDeviceCollection):Boolean = {
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
