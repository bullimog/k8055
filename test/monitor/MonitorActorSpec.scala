package monitor

import akka.actor.{Props, ActorRef, ActorSystem}
import controllers.FakeDeviceCollectionController
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import org.specs2.mock._
import play.api.test.WithApplication


@RunWith(classOf[JUnitRunner])
class MonitorActorSpec extends Specification{

  object TestMonitorActor extends MonitorActorTrait{
    override val deviceCollectionController = FakeDeviceCollectionController
  }


  "MonitorActor" should{
    "result in no change, when there are no active Monitors" in new WithApplication{
      FakeDeviceCollectionController.heaterAnalogueState = 0
      FakeDeviceCollectionController.thermometerAnalogueState = 0
      FakeDeviceCollectionController.thermostatAnalogueState = 0
      FakeDeviceCollectionController.thermostatDigitalState = false
      TestMonitorActor.processActiveMonitors()
      FakeDeviceCollectionController.lastDeviceState must equalTo(null)
    }


    "Not change anything, when there is one active Monitor which is on target" in new WithApplication{
      FakeDeviceCollectionController.heaterAnalogueState = 0
      FakeDeviceCollectionController.thermometerAnalogueState = 2000
      FakeDeviceCollectionController.thermostatAnalogueState = 2000
      FakeDeviceCollectionController.thermostatDigitalState = true
      TestMonitorActor.processActiveMonitors()
      FakeDeviceCollectionController.lastDeviceState.analogueState must equalTo(Some(0))
    }

    "Set Monitor increaser, when there is one active Monitor which is below target" in new WithApplication{
      FakeDeviceCollectionController.heaterAnalogueState = 0
      FakeDeviceCollectionController.thermometerAnalogueState = 2000
      FakeDeviceCollectionController.thermostatAnalogueState = 2001
      FakeDeviceCollectionController.thermostatDigitalState = true
      TestMonitorActor.processActiveMonitors()
      FakeDeviceCollectionController.lastDeviceState.analogueState must equalTo(Some(40))
    }

    "Switch off Monitor increaser, when there is an active Monitor which is above target" in new WithApplication {
      FakeDeviceCollectionController.heaterAnalogueState = 0
      FakeDeviceCollectionController.thermometerAnalogueState = 2000
      FakeDeviceCollectionController.thermostatAnalogueState = 1999
      FakeDeviceCollectionController.thermostatDigitalState = true
      TestMonitorActor.processActiveMonitors()
      FakeDeviceCollectionController.lastDeviceState.analogueState must equalTo(Some(0))
    }


    "calculate the output setting properly when it is a lot above target " in {
      val result = TestMonitorActor.calculateOutputSetting(8)
      result must equalTo(255)
    }
    "calculate the output setting properly when it is above target " in {
      val result = TestMonitorActor.calculateOutputSetting(3)
      result must equalTo(120)
    }

    "calculate the output setting properly when there is a small drift from target " in {
      val result = TestMonitorActor.calculateOutputSetting(2)
      result must equalTo(80)
    }

    "calculate the output setting properly when there is a tiny drift from target " in {
      val result = TestMonitorActor.calculateOutputSetting(1)
      result must equalTo(40)
    }

    "calculate the output setting properly when it is on target " in {
      val result = TestMonitorActor.calculateOutputSetting(0)
      result must equalTo(0)
    }

    "calculate the output setting properly when it is below target " in {
      val result = TestMonitorActor.calculateOutputSetting(-1)
      result must equalTo(0)
    }


  }
}
