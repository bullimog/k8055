package connectors

import java.io.{File, PrintWriter, FileNotFoundException}
import model.DeviceCollection
import play.api.libs.json.{Json, JsError, JsSuccess, JsValue}
import scala.io.Source


object ConfigIO {

  def parseComponentCollection(json: JsValue):Option[DeviceCollection] = {
    json.validate[DeviceCollection] match {
      case s: JsSuccess[DeviceCollection] => Some(s.get)
      case e: JsError => None
    }
  }

  def readComponentCollectionFromFile(fileName:String):Option[DeviceCollection] = {
    try{
      val source = Source.fromFile(fileName, "UTF-8")
      val json: JsValue = Json.parse(source.mkString)
      parseComponentCollection(json)
    }catch{
      case e:FileNotFoundException => None
    }
  }

  def writeComponentCollectionToFile(fileName: String, componentCollection: DeviceCollection):Unit = {
    val writer = new PrintWriter(new File(fileName))
    writer.write(Json.prettyPrint(Json.toJson(componentCollection)))
    writer.close()
  }
}
