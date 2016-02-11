package controllers

import connectors.{FakeK8055Board, Configuration, FakeDeviceConfigIO, DeviceConfigIO}
import model.{Device, DeviceCollection}
import monitor.MonitorManager
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import play.api.test.Helpers._
import play.api.test.{FakeRequest, WithApplication}
import model.Device._
import monitor.FakeMonitorManager


@RunWith(classOf[JUnitRunner])
class DeviceCollectionControllerSpec extends Specification {

  object TestDeviceCollectionController extends DeviceCollectionController{
    override val deviceConfigIO = FakeDeviceConfigIO
    override val deviceController = FakeDeviceController
    override val monitorManager = FakeMonitorManager
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
      TestDeviceCollectionController.getDeviceCollection.name must equalTo("Fake Name")
    }

    "populate devices with transient data" in new WithApplication {
      val populatedDc = TestDeviceCollectionController.populateDevices(FakeDeviceConfigIO.deviceCollection)
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
      TestDeviceCollectionController.upsertDevice(pump)
      val updatedPump:Option[Device] = TestDeviceCollectionController.latestDeviceCollection.devices.find(d => d.id =="TEST-DO-1")
      val updatedDesc = updatedPump.fold("wrong!")(d=>d.description)
      updatedDesc must equalTo("updated-test-pump")
    }


    //updateTransientDigitalOutData

    //updateTransientAnalogueOutData

    //patchDevice

    //deleteDevice

    //putDeviceCollection

  }




}