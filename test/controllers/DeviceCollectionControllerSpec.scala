package controllers

import connectors.{FakeK8055Board, Configuration, FakeDeviceConfigIO, DeviceConfigIO}
import model.{DeviceState, Device, DeviceCollection}
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import play.api.test.{WithApplication}
import model.Device._
import manager.{DeviceCollectionManager, FakeMonitorAndStrobeManager}


@RunWith(classOf[JUnitRunner])
class DeviceCollectionControllerSpec extends Specification {

  object TestDeviceCollectionManager extends DeviceCollectionManager{
    override val deviceConfigIO = FakeDeviceConfigIO
    override val deviceController = FakeDeviceManager
    override val monitorManager = FakeMonitorAndStrobeManager
    override val configuration = Configuration
    override val k8055Board = FakeK8055Board

    var latestDeviceCollection:DeviceCollection = null
    override def putDeviceCollection(deviceCollection: DeviceCollection):Boolean = {
      latestDeviceCollection = deviceCollection
      true
    }
  }

  "DeviceCollectionController" should {

    "retrieve a device collection" in new WithApplication{
      TestDeviceCollectionManager.getDeviceCollection.name must equalTo("Fake Name")
    }

    "populate devices with transient data" in new WithApplication {
      val populatedDc = TestDeviceCollectionManager.readAndPopulateDevices(FakeDeviceConfigIO.deviceCollection)
      populatedDc.devices.foreach(device =>

        device.deviceType match{
          case DIGITAL_OUT => device.id must equalTo("TEST-DO-1")
          case ANALOGUE_OUT => device.id must equalTo("TEST-AO-1")
          case DIGITAL_IN => device.id must equalTo("TEST-DI-1")
          case ANALOGUE_IN => device.id must equalTo("TEST-AI-1")
          case MONITOR => device.id must equalTo("TEST-MO-1")
        }
      )
    }

    "upsert a device in the device collection" in new WithApplication {
      val pump = Device("TEST-DO-1", "updated-test-pump", DIGITAL_OUT, 1, digitalState = Some(false))
      TestDeviceCollectionManager.upsertDevice(pump)
      val updatedPump:Option[Device] = TestDeviceCollectionManager.latestDeviceCollection.devices.find(d => d.id =="TEST-DO-1")
      val updatedDesc = updatedPump.fold("wrong!")(d=>d.description)
      updatedDesc must equalTo("updated-test-pump")
    }


    "update transient digital data for a digital out should succeed" in new WithApplication {
      val pump = Device("TEST-DO-1", "updated-test-pump", DIGITAL_OUT, 1, digitalState = Some(false))
      val result = TestDeviceCollectionManager.updateTransientDigitalOutData(pump)
      result must equalTo(true)
    }

    "update transient digital data for a monitor should succeed" in new WithApplication {
      val thermostat = Device("TEST-MO-1", "Thermostat", MONITOR, 1, Some("c"), None, None, None, Some("TEST-AI-1"), Some("TEST-AO-1"), None, Some(false), None, Some(0) )
      val result = TestDeviceCollectionManager.updateTransientDigitalOutData(thermostat)
      result must equalTo(true)
    }

    "update transient digital data for an analogue out should fail" in new WithApplication {
      val heater = Device("TEST-AO-1", "test-heater", ANALOGUE_OUT, 1, Some("%"), Some(0), analogueState = Some(22))
      val result = TestDeviceCollectionManager.updateTransientDigitalOutData(heater)
      result must equalTo(false)
    }

    "update transient digital data for an analogue in should fail" in new WithApplication {
      val thermometer = Device("TEST-AI-1", "test-thermometer", ANALOGUE_IN, 1, Some("%"), Some(0), analogueState = Some(0))
      val result = TestDeviceCollectionManager.updateTransientDigitalOutData(thermometer)
      result must equalTo(false)
    }

    "update transient digital data for an digital in should fail" in new WithApplication {
      val switch = Device("TEST-DI-1", "test-switch", DIGITAL_IN, 1, digitalState = Some(false))
      val result = TestDeviceCollectionManager.updateTransientDigitalOutData(switch)
      result must equalTo(false)
    }


    "update transient analogue data for a digital out should fail" in new WithApplication {
      val pump = Device("TEST-DO-1", "updated-test-pump", DIGITAL_OUT, 1, digitalState = Some(false))
      val result = TestDeviceCollectionManager.updateTransientAnalogueOutData(pump)
      result must equalTo(false)
    }

    "update transient analogue data for a monitor should succeed" in new WithApplication {
      val thermostat = Device("TEST-MO-1", "Thermostat", MONITOR, 1, Some("c"), None, None, None, Some("TEST-AI-1"), Some("TEST-AO-1"), None, Some(false), None, Some(0) )
      val result = TestDeviceCollectionManager.updateTransientAnalogueOutData(thermostat)
      result must equalTo(true)
    }

    "update transient analogue data for an analogue out should pass" in new WithApplication {
      val heater = Device("TEST-AO-1", "test-heater", ANALOGUE_OUT, 1, Some("%"), Some(0), analogueState = Some(22))
      val result = TestDeviceCollectionManager.updateTransientAnalogueOutData(heater)
      result must equalTo(true)
    }

    "update transient analogue data for an analogue in should fail" in new WithApplication {
      val thermometer = Device("TEST-AI-1", "test-thermometer", ANALOGUE_IN, 1, Some("%"), Some(0), analogueState = Some(0))
      val result = TestDeviceCollectionManager.updateTransientAnalogueOutData(thermometer)
      result must equalTo(false)
    }

    "update transient analogue data for an digital in should fail" in new WithApplication {
      val switch = Device("TEST-DI-1", "test-switch", DIGITAL_IN, 1, digitalState = Some(false))
      val result = TestDeviceCollectionManager.updateTransientAnalogueOutData(switch)
      result must equalTo(false)
    }

    object TestDeviceCollectionManager2 extends DeviceCollectionManager{
      override val deviceConfigIO = FakeDeviceConfigIO
      override val deviceController = FakeDeviceManager
      override val monitorManager = FakeMonitorAndStrobeManager
      override val configuration = Configuration
      override val k8055Board = FakeK8055Board

      override def updateTransientAnalogueOutData(device: Device) = {
        latestDevice = if(latestDevice == null) device
        else latestDevice.copy(analogueState = device.analogueState)
       true
      }
      override def updateTransientDigitalOutData(device: Device) = {
        latestDevice = if(latestDevice == null) device
          else latestDevice.copy(digitalState = device.digitalState)
        true
      }


      var latestDevice:Device = null
    }

    //Patch Devices
    "delta patch transient analogue out data should update the device correctly" in new WithApplication {
      val heaterState =  DeviceState("TEST-AO-1", None, Some(11))
      TestDeviceCollectionManager2.patchDevice(heaterState, true)
      val result = TestDeviceCollectionManager2.latestDevice.analogueState
      result must equalTo(Some(33))
    }

    "patch transient analogue out data should update the device correctly" in new WithApplication {
      val heaterState =  DeviceState("TEST-AO-1", None, Some(11))
      val succeeded = TestDeviceCollectionManager2.patchDevice(heaterState, false)
      succeeded must equalTo(true)
      val result = TestDeviceCollectionManager2.latestDevice.analogueState
      result must equalTo(Some(11))
    }

    "patch transient analogue in data should fail" in new WithApplication {
      val thermometerState =  DeviceState("TEST-AI-1", None, Some(11))
      val succeeded = TestDeviceCollectionManager2.patchDevice(thermometerState, false)
      succeeded must equalTo(false)
    }

    "patch transient digital in data should fail" in new WithApplication {
      val switchState =  DeviceState("TEST-DI-1", None, Some(11))
      val succeeded = TestDeviceCollectionManager2.patchDevice(switchState, false)
      succeeded must equalTo(false)
    }

    "patch transient digital out data should succeed" in new WithApplication {
      val pumpState =  DeviceState("TEST-DO-1", Some(true), None)
      val succeeded = TestDeviceCollectionManager2.patchDevice(pumpState, false)
      succeeded must equalTo(true)
      val result = TestDeviceCollectionManager2.latestDevice.digitalState
      result must equalTo(Some(true))
    }

    "delta patch transient analogue monitor data should update the device correctly" in new WithApplication {
      val thermometerState =  DeviceState("TEST-MO-1", None, Some(11))
      val succeeded = TestDeviceCollectionManager2.patchDevice(thermometerState, true)
      succeeded must equalTo(true)
      val aResult = TestDeviceCollectionManager2.latestDevice.analogueState
      aResult must equalTo(Some(12)) //FakeMonitorManager.getAnalogueOut returns 1, so 11+1=12
    }

    "patch transient analogue monitor data should update the device correctly" in new WithApplication {
      val thermostatState =  DeviceState("TEST-MO-1", None, Some(11))
      val succeeded = TestDeviceCollectionManager2.patchDevice(thermostatState, false)
      succeeded must equalTo(true)
      val aResult = TestDeviceCollectionManager2.latestDevice.analogueState
      aResult must equalTo(Some(11))
    }

    "patch transient digital monitor data should update the device correctly" in new WithApplication {
      val thermostatState =  DeviceState("TEST-MO-1", Some(true), None)
      TestDeviceCollectionManager2.patchDevice(thermostatState, false)
      val dResult = TestDeviceCollectionManager2.latestDevice.digitalState
      dResult must equalTo(Some(true))
    }

    //putDeviceCollection
    "almost pointless test, to ensure writeDeviceCollectionToFile is invoked" in new WithApplication {
      val dc = TestDeviceCollectionManager2.getDeviceCollection
      val result = TestDeviceCollectionManager2.putDeviceCollection(dc)
      result must equalTo(true)
    }


    object TestDeviceCollectionManager3 extends DeviceCollectionManager{
      override val deviceConfigIO = FakeDeviceConfigIO
      override val deviceController = FakeDeviceManager
      override val monitorManager = FakeMonitorAndStrobeManager
      override val configuration = Configuration
      override val k8055Board = FakeK8055Board

      var latestDeviceCollection:DeviceCollection = null

      override def putDeviceCollection(deviceCollection: DeviceCollection):Boolean = {
        latestDeviceCollection = deviceCollection
        true
      }
    }

    //deleteDevice
    "delete device that exists should update the device collection correctly" in new WithApplication {
      val deviceId = "TEST-DI-1"
      val isInThere = TestDeviceCollectionManager3.getDeviceCollection.devices.find(device => device.id==deviceId)
      isInThere must not equals(None)
      val switch = Device(deviceId, "test-switch", DIGITAL_IN, 1, digitalState = Some(false))
      TestDeviceCollectionManager3.deleteDevice(switch)
      val dResult = TestDeviceCollectionManager3.latestDeviceCollection.devices.find(device => device.id==deviceId)
      dResult must equalTo(None)
    }

    "delete device (by id) that exists should update the device collection correctly" in new WithApplication {
      val deviceId = "TEST-AI-1"
      val isInThere = TestDeviceCollectionManager3.getDeviceCollection.devices.find(device => device.id==deviceId)
      isInThere must not equals(None)
      TestDeviceCollectionManager3.deleteDevice(deviceId)
      val dResult = TestDeviceCollectionManager3.latestDeviceCollection.devices.find(device => device.id==deviceId)
      dResult must equalTo(None)
    }

    "handle a request to delete device that does not exist should leave device collection" in new WithApplication {
      val deviceId = "FAKE-AI-1"
      val isInThere = TestDeviceCollectionManager3.getDeviceCollection.devices.find(device => device.id==deviceId)
      isInThere must equalTo(None)
      val thermometer = Device(deviceId, "test-thermometer", ANALOGUE_IN, 1, Some("%"), Some(0), analogueState = Some(0))
      val success = TestDeviceCollectionManager3.deleteDevice(thermometer)
      success must equalTo(true)
      val dResult = TestDeviceCollectionManager3.latestDeviceCollection.devices.find(device => device.id==deviceId)
      dResult must equalTo(None)
    }
  }
}
