package controllers

import connector.K8055Board
import connectors.{Configuration, FakeDeviceConfigIO, DeviceConfigIO}
import monitor.MonitorManager
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import play.api.test.Helpers._
import play.api.test.{FakeRequest, WithApplication}
import model.Device._


@RunWith(classOf[JUnitRunner])
class DeviceCollectionControllerSpec extends Specification {

  object TestDeviceCollectionController extends DeviceCollectionController{
    override val deviceConfigIO = FakeDeviceConfigIO
    override val deviceController = FakeDeviceController
    override val monitorManager = MonitorManager
    override val configuration = Configuration
    override val k8055Board = K8055Board
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

    //upsertDevice

    //updateTransientDigitalOutData

    //updateTransientAnalogueOutData

    //patchDevice

    //deleteDevice

    //putDeviceCollection

  }




}