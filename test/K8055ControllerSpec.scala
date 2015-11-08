import connector.K8055Board

import model.{Device, DeviceCollection}
import model.Device._
import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc.Cookie

import play.api.test._
import play.api.test.Helpers._

import scala.concurrent.Future


@RunWith(classOf[JUnitRunner])
class K8055ControllerSpec extends Specification {

  "Application" should {

    "send 404 on a bad request" in new WithApplication{
      route(FakeRequest(GET, "/boum")) must beSome.which (status(_) == NOT_FOUND)
    }

    "render the index page" in new WithApplication{
      val home = route(FakeRequest(GET, "/")).get

      status(home) must equalTo(OK)
      contentType(home) must beSome.which(_ == "application/json")
      //contentAsString(home) must contain ("pump")
    }

    "add a Digital Out device" in new WithApplication {
      val pump = Device("DO-1", "pump", DIGITAL_OUT, 1)
      testDeviceAdd(pump)
    }

    "add an Analogue Out device" in new WithApplication {
      val heater = Device("AO-1", "heater", ANALOGUE_OUT, 1, Some("%"), Some(0))
      testDeviceAdd(heater)
    }

    "add a Digital In device" in new WithApplication {
      val switch = Device("DI-1", "switch", DIGITAL_IN, 1)
      testDeviceAdd(switch)
    }

    "add an Analogue In device" in new WithApplication {
      val thermometer = Device("AO-1", "thermometer", ANALOGUE_IN, 1, Some("%"), Some(0))
      testDeviceAdd(thermometer)
    }




//    val result = K8055Controller.createGroup()(fakeRequest).result.value.get
  }

  def testDeviceAdd(device:Device) = {
    val jDevice = Json.toJson(device)

    val req = FakeRequest(method = "POST", uri = controllers.routes.K8055Controller.addDevice().url,
      headers = FakeHeaders(Seq("Content-type"->"application/json")), body =  jDevice)

    val Some(result) = route(req)
    status(result) must equalTo(OK)

    val home = route(FakeRequest(GET, "/")).get
    status(home) must equalTo(OK)
    contentType(home) must beSome.which(_ == "application/json")
    contentAsString(home) must contain (device.description)
    contentAsString(home) must contain (device.id)

    val json: JsValue = Json.parse(contentAsString(home))
    val dc:DeviceCollection = json.validate[DeviceCollection] match {
      case s: JsSuccess[DeviceCollection] => s.get
      case e: JsError => println("jsError: "+e ); DeviceCollection("None", "Empty", List())
    }
    dc.devices.contains(device) must equalTo(true)


    //println(""+contentAsJson(home))
  }
}
