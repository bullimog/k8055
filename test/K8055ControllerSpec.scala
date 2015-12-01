import connectors.Configuration
import model.{DeviceState, Device}
import model.Device._
import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.test._
import play.api.test.Helpers._

@RunWith(classOf[JUnitRunner])
class K8055ControllerSpec extends Specification {

  "K8055 Controller" should {

    "send 404 on a bad request" in new WithApplication{
      route(FakeRequest(GET, "/boum")) must beSome.which (status(_) == NOT_FOUND)
    }

    "render the index page" in new WithApplication{
      val home = route(FakeRequest(GET, "/")).get

      status(home) must equalTo(OK)
      contentType(home) must beSome.which(_ == "application/json")
      //contentAsString(home) must contain ("pump")
    }



    val pump = Device("TEST-DO-1", "test-pump", DIGITAL_OUT, 1, digitalState = Some(false))
    val heater = Device("TEST-AO-1", "test-heater", ANALOGUE_OUT, 1, Some("%"), Some(0), analogueState = Some(0))
    val switch = Device("TEST-DI-1", "test-switch", DIGITAL_IN, 1, digitalState = Some(false))
    val thermometer = Device("TEST-AI-1", "test-thermometer", ANALOGUE_IN, 1, Some("%"), Some(0), analogueState = Some(0))
    val thermostat = Device("MO-1", "Thermostat", MONITOR, 1, Some("c"), None, None, None, Some("TEST-AI-1"), Some("TEST-AO-1"), None, Some(false), Some(0) )

    val updatedPump = pump.copy(description = "changed-pump")
    val updatedHeater = heater.copy(description = "changed-heater")
    val updatedSwitch = switch.copy(description = "changed-switch")
    val updatedThermometer = thermometer.copy(description = "changed-thermometer")

    val shouldBeFound = true
    val shouldNotBeFound = false

    "test that we're unable to fetch a device, when there's bad config..." in {
      running(FakeApplication(additionalConfiguration = Map("file.name"->"no_devices.json"))){
        //println("############# = "+Configuration.filename)
        testDeviceGet(pump, shouldNotBeFound)
      }
    }

    // Device adds.
    "check a Digital Out device is not present" in new WithApplication {testDeviceGet(pump, shouldNotBeFound)}
    "add a Digital Out device" in new WithApplication {testDeviceAdd(pump)}
    "check a Digital Out device is present" in new WithApplication {testDeviceGet(pump, shouldBeFound)}

    "check a Analogue Out device is not present" in new WithApplication {testDeviceGet(heater, shouldNotBeFound)}
    "add an Analogue Out device" in new WithApplication {testDeviceAdd(heater)}
    "check a Analogue Out device is present" in new WithApplication {testDeviceGet(heater, shouldBeFound)}

    "check a Digital In device is not present" in new WithApplication {testDeviceGet(switch, shouldNotBeFound)}
    "add a Digital In device" in new WithApplication {testDeviceAdd(switch)}
    "check a Digital In device is present" in new WithApplication {testDeviceGet(switch, shouldBeFound)}

    "check a Analogue In device is not present" in new WithApplication {testDeviceGet(thermometer, shouldNotBeFound)}
    "add an Analogue In device" in new WithApplication {testDeviceAdd(thermometer)}
    "check a Analogue In device is present" in new WithApplication {testDeviceGet(thermometer, shouldBeFound)}

    //Device updates...
    "check a Original Digital Out device is present" in new WithApplication {testDeviceGetIsSame(pump, shouldBeFound)}
    "check a New Digital Out device is not present" in new WithApplication {testDeviceGetIsSame(updatedPump, shouldNotBeFound)}
    "update a Digital Out device" in new WithApplication {testDeviceUpdate(pump, updatedPump)}
    "check a Original Digital Out device is not present" in new WithApplication {testDeviceGetIsSame(pump, shouldNotBeFound)}
    "check a New Digital Out device is present" in new WithApplication {testDeviceGetIsSame(updatedPump, shouldBeFound)}

    "check a Original Analogue Out device is present" in new WithApplication {testDeviceGetIsSame(heater, shouldBeFound)}
    "check a New Analogue Out device is not present" in new WithApplication {testDeviceGetIsSame(updatedHeater, shouldNotBeFound)}
    "update an Analogue Out device" in new WithApplication {testDeviceUpdate(heater, updatedHeater)}
    "check a Original Analogue Out device is not present" in new WithApplication {testDeviceGetIsSame(heater, shouldNotBeFound)}
    "check a New Analogue Out device is present" in new WithApplication {testDeviceGetIsSame(updatedHeater, shouldBeFound)}

    "check a Original Digital In device is present" in new WithApplication {testDeviceGetIsSame(switch, shouldBeFound)}
    "check a New Digital In device is not present" in new WithApplication {testDeviceGetIsSame(updatedSwitch, shouldNotBeFound)}
    "update a Digital In device" in new WithApplication {testDeviceUpdate(switch, updatedSwitch)}
    "check a Original Digital In device is not present" in new WithApplication {testDeviceGetIsSame(switch, shouldNotBeFound)}
    "check a New Digital In device is present" in new WithApplication {testDeviceGetIsSame(updatedSwitch, shouldBeFound)}

    "check a Original Analogue In device is present" in new WithApplication {testDeviceGetIsSame(thermometer, shouldBeFound)}
    "check a New Analogue In device is not present" in new WithApplication {testDeviceGetIsSame(updatedThermometer, shouldNotBeFound)}
    "update an Analogue In device" in new WithApplication {testDeviceUpdate(thermometer, updatedThermometer)}
    "check a Original Analogue In device is not present" in new WithApplication {testDeviceGetIsSame(thermometer, shouldNotBeFound)}
    "check a New Analogue In device is present" in new WithApplication {testDeviceGetIsSame(updatedThermometer, shouldBeFound)}

    //Device patches...
    "check a Original Digital Out device is present" in new WithApplication {testDeviceGetIsSame(updatedPump, shouldBeFound)}
    "patch a Digital Out device" in new WithApplication {testDigitalDevicePatch(updatedPump, true, true)}
    "check a New Digital Out device is present" in new WithApplication {
      testDeviceGetIsSame(updatedPump.copy(digitalState=Some(true)), shouldBeFound)
    }
    "un-patch a Digital Out device" in new WithApplication {testDigitalDevicePatch(updatedPump, false, true)}

    "check a Original Analogue Out device is present" in new WithApplication {testDeviceGetIsSame(updatedHeater, shouldBeFound)}
    "patch an Analogue Out device" in new WithApplication {testAnalogueDevicePatch(updatedHeater, 44, true)}
    "check a New Analogue Out device is present" in new WithApplication {
      testDeviceGetIsSame(updatedHeater.copy(analogueState=Some(44)), shouldBeFound)
    }
    "un-patch an Analogue Out device" in new WithApplication {testAnalogueDevicePatch(updatedHeater, 0, true)}

    "check an Original Digital In device is present" in new WithApplication {testDeviceGetIsSame(updatedSwitch, shouldBeFound)}
    "ensure we can't patch a Digital In device" in new WithApplication {testDigitalDeviceInPatch(updatedSwitch, true, false)}

    "check a Original Analogue In device is present" in new WithApplication {testDeviceGetIsSame(updatedThermometer, shouldBeFound)}
    "ensure we can't patch an Analogue In device" in new WithApplication {testAnalogueInDevicePatch(updatedThermometer, 44, false)}


    //Device deletes...
    "check a Digital Out device is present" in new WithApplication {testDeviceGet(updatedPump, shouldBeFound)}
    "delete a Digital Out device" in new WithApplication {testDeviceDelete(pump)}
    "check a Digital Out device is not present" in new WithApplication {testDeviceGet(pump, shouldNotBeFound)}

    "check an Analogue Out device is present" in new WithApplication {testDeviceGet(updatedHeater, shouldBeFound)}
    "delete an Analogue Out device" in new WithApplication {testDeviceDelete(heater)}
    "check an Analogue Out device is not present" in new WithApplication {testDeviceGet(heater, shouldNotBeFound)}

    "check a Digital In device is present" in new WithApplication {testDeviceGet(updatedSwitch, shouldBeFound)}
    "delete a Digital In device" in new WithApplication {testDeviceDelete(switch)}
    "check a Digital In device is not present" in new WithApplication {testDeviceGet(switch, shouldNotBeFound)}

    "check an Analogue In device is present" in new WithApplication {testDeviceGet(updatedThermometer, shouldBeFound)}
    "delete an Analogue In device" in new WithApplication {testDeviceDelete(thermometer)}
    "check an Analogue In device is not present" in new WithApplication {testDeviceGet(thermometer, shouldNotBeFound)}
  }


  def testDeviceAdd(device:Device) = {
    val jDevice = Json.toJson(device)

    //Test that the POST is successful
    val req = FakeRequest(method = "POST", uri = controllers.routes.K8055Controller.addDevice().url,
      headers = FakeHeaders(Seq("Content-type"->"application/json")), body =  jDevice)
    val Some(result) = route(req)
    status(result) must equalTo(OK)
  }

  def testDeviceUpdate(originalDevice: Device, updatedDevice: Device) = {
    val jDevice = Json.toJson(updatedDevice)
    val req = FakeRequest(method = "PUT", uri = controllers.routes.K8055Controller.updateDevice().url,
      headers = FakeHeaders(Seq("Content-type"->"application/json")), body =  jDevice)
    val Some(result) = route(req)
    status(result) must equalTo(OK)
  }

  def testDigitalDevicePatch(originalDevice: Device, state:Boolean, shouldSucceed:Boolean) = {
    val deviceState=DeviceState("TEST-DO-1", digitalState = Some(state))
    val jDeviceState = Json.toJson(deviceState)
    val req = FakeRequest(method = "PUT", uri = controllers.routes.K8055Controller.patchDevice().url,
      headers = FakeHeaders(Seq("Content-type"->"application/json")), body =  jDeviceState)
    val Some(result) = route(req)
    if(shouldSucceed)
      status(result) must equalTo(OK)
    else
      status(result) must equalTo(BAD_REQUEST)
  }

  def testDigitalDeviceInPatch(originalDevice: Device, state:Boolean, shouldSucceed:Boolean) = {
    val deviceState=DeviceState("TEST-DI-1", digitalState = Some(state))
    val jDeviceState = Json.toJson(deviceState)
    val req = FakeRequest(method = "PUT", uri = controllers.routes.K8055Controller.patchDevice().url,
      headers = FakeHeaders(Seq("Content-type"->"application/json")), body =  jDeviceState)
    val Some(result) = route(req)
    if(shouldSucceed)
      status(result) must equalTo(OK)
    else
      status(result) must equalTo(BAD_REQUEST)
  }



  def testAnalogueDevicePatch(originalDevice: Device, state:Int, shouldSucceed:Boolean) = {
    val deviceState=DeviceState("TEST-AO-1", analogueState = Some(state))
    val jDeviceState = Json.toJson(deviceState)
    val req = FakeRequest(method = "PUT", uri = controllers.routes.K8055Controller.patchDevice().url,
      headers = FakeHeaders(Seq("Content-type"->"application/json")), body =  jDeviceState)
    val Some(result) = route(req)
    if(shouldSucceed)
      status(result) must equalTo(OK)
    else
      status(result) must equalTo(BAD_REQUEST)
  }

  def testAnalogueInDevicePatch(originalDevice: Device, state:Int, shouldSucceed:Boolean) = {
    val deviceState=DeviceState("TEST-AI-1", analogueState = Some(state))
    val jDeviceState = Json.toJson(deviceState)
    val req = FakeRequest(method = "PUT", uri = controllers.routes.K8055Controller.patchDevice().url,
      headers = FakeHeaders(Seq("Content-type"->"application/json")), body =  jDeviceState)
    val Some(result) = route(req)
    if(shouldSucceed)
      status(result) must equalTo(OK)
    else
      status(result) must equalTo(BAD_REQUEST)
  }

  def testDeviceDelete(device:Device) = {
    val jDevice = Json.toJson(device)
    //Test that the POST is successful
    val req = route(FakeRequest(DELETE, "/device/"+device.id)).get
    status(req) must equalTo(OK)
  }

  def testDeviceGet(device: Device, shouldBeThere:Boolean) = {
    //Test the the device is actually in there
    val home = route(FakeRequest(GET, "/device/"+device.id)).get

    if(shouldBeThere){
      status(home) must equalTo(OK)

      contentType(home) must beSome.which(_ == "application/json")
      val json: JsValue = Json.parse(contentAsString(home))
      val d:Device = json.validate[Device] match {
        case s: JsSuccess[Device] => s.get
        case e: JsError => println("jsError: "+e ); Device("None", "Empty",0,0)
      }
      d must equalTo(device)
    }
    else{
      status(home) must not equalTo OK
    }
  }

  def testDeviceGetIsSame(device: Device, shouldBeSame:Boolean) = {
    //Test the the device is actually in there
    val home = route(FakeRequest(GET, "/device/"+device.id)).get
    status(home) must equalTo(OK)

    contentType(home) must beSome.which(_ == "application/json")
    val json: JsValue = Json.parse(contentAsString(home))
    val d:Device = json.validate[Device] match {
      case s: JsSuccess[Device] => s.get
      case e: JsError => println("jsError: "+e ); Device("None", "Empty",0,0)
    }

    if(shouldBeSame) {
      d must equalTo(device)
    }
    else{
      d must not equalTo(device)
    }
  }
}
