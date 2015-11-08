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
      val pump = Device("TEST-DO-1", "test-pump", DIGITAL_OUT, 1, digitalState = Some(false))
      testDeviceAdd(pump)
    }

    "add an Analogue Out device" in new WithApplication {
      val heater = Device("TEST-AO-1", "test-heater", ANALOGUE_OUT, 1, Some("%"), Some(0), analogueState = Some(0))
      testDeviceAdd(heater)
    }

    "add a Digital In device" in new WithApplication {
      val switch = Device("TEST-DI-1", "test-switch", DIGITAL_IN, 1, digitalState = Some(false))
      testDeviceAdd(switch)
    }

    "add an Analogue In device" in new WithApplication {
      val thermometer = Device("TEST-AI-1", "test-thermometer", ANALOGUE_IN, 1, Some("%"), Some(0), analogueState = Some(0))
      testDeviceAdd(thermometer)
    }




//    val result = K8055Controller.createGroup()(fakeRequest).result.value.get
  }

  def testDeviceAdd(device:Device) = {
    val jDevice = Json.toJson(device)

    //Test that the POST is successful
    val req = FakeRequest(method = "POST", uri = controllers.routes.K8055Controller.addDevice().url,
      headers = FakeHeaders(Seq("Content-type"->"application/json")), body =  jDevice)
    val Some(result) = route(req)
    status(result) must equalTo(OK)

    //Test the the device is actually in there
    val home = route(FakeRequest(GET, "/device/"+device.id)).get
    status(home) must equalTo(OK)
    contentType(home) must beSome.which(_ == "application/json")

    val json: JsValue = Json.parse(contentAsString(home))
    val d:Device = json.validate[Device] match {
      case s: JsSuccess[Device] => s.get
      case e: JsError => println("jsError: "+e ); Device("None", "Empty",0,0)
    }
    d must equalTo(device)

    DeviceCollection.deleteDevice(device.id)
  }
}
